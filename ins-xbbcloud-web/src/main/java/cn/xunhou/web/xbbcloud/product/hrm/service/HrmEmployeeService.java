package cn.xunhou.web.xbbcloud.product.hrm.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.context.UserParam;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.util.XbbRpcConvert;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.cloud.redis.generate.RedisIDWorker;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.portal.PortalServiceGrpc;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import cn.xunhou.grpc.proto.universal.UniversalServiceGrpc;
import cn.xunhou.grpc.proto.universal.UniversalServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerGrpc;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerProto;
import cn.xunhou.grpc.proto.xbbcloud.SignServerGrpc;
import cn.xunhou.grpc.proto.xbbcloud.SignServerProto;
import cn.xunhou.web.xbbcloud.common.helper.NotifyHelper;
import cn.xunhou.web.xbbcloud.config.xhrpc.XhRpcComponent;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumProject;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhR;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhRpcParam;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhTotal;
import cn.xunhou.web.xbbcloud.product.hrm.constant.ConstantData;
import cn.xunhou.web.xbbcloud.product.hrm.param.EmployeeDictionarySaveParam;
import cn.xunhou.web.xbbcloud.product.hrm.param.HrmSaveTenantParam;
import cn.xunhou.web.xbbcloud.product.hrm.param.ImportStaffEmployeeCheckQueryParam;
import cn.xunhou.web.xbbcloud.product.hrm.param.ImportStaffEmployeeSubmitParam;
import cn.xunhou.web.xbbcloud.product.hrm.result.*;
import com.google.protobuf.Empty;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author sha.li
 * @since 2022/9/14
 */
@Slf4j
@Service
public class HrmEmployeeService {

    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    @GrpcClient("ins-xhportal-platform")
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;

    @GrpcClient("ins-xhportal-platform")
    private static PortalServiceGrpc.PortalServiceBlockingStub portalServiceBlockingStub;

    @GrpcClient("ins-xhwallet-platform")
    private UniversalServiceGrpc.UniversalServiceBlockingStub universalServiceBlockingStub;

    @GrpcClient("ins-xbbcloud-platform")
    private ScheduleServerGrpc.ScheduleServerBlockingStub scheduleServerBlockingStub;

    @GrpcClient("ins-xbbcloud-platform")
    private SignServerGrpc.SignServerBlockingStub signServerBlockingStub;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private NotifyHelper notifyHelper;

    @Autowired
    private RedisIDWorker redisIDWorker;

    @Resource
    private XhRpcComponent xhRpcComponent;


    public JsonResponse<Boolean> isActivate() {
        String key = ConstantData.ACTIVATE_MOBILE_EMPLOYEE;
        Long accountId = XBB_USER_CONTEXT.get().getUserId();
        LocalDate now = LocalDate.now();
        Object object = redisTemplate.opsForValue().get(key + now + accountId);
        if (Objects.nonNull(object)) {
            return JsonResponse.success(true);
        }
        return JsonResponse.success(false);
    }


    /**
     * 员工管理->激活员工(给员工发短信)
     *
     * @return
     */
    public JsonResponse<?> activateMobileEmployee() {
        String key = ConstantData.ACTIVATE_MOBILE_EMPLOYEE;
        Long accountId = XBB_USER_CONTEXT.get().getUserId();
        LocalDate now = LocalDate.now();
        Object object = redisTemplate.opsForValue().get(key + now + accountId);
        if (Objects.nonNull(object)) {
            throw Status.INVALID_ARGUMENT.withDescription("non-repeatable send").asRuntimeException();
        }
        HrmServiceProto.EmployeePageResponses employeePageResponses = hrmServiceBlockingStub.activateMobileEmployee(Empty.getDefaultInstance());
        if (CollUtil.isEmpty(employeePageResponses.getDataList())) {
            return JsonResponse.success();
        }
        List<String> mobiles = employeePageResponses.getDataList().stream().map(HrmServiceProto.EmployeePageResponse::getMobile).collect(Collectors.toList());
        //发送短信
        for (String mobile : mobiles) {
            UniversalServiceProto.SmsVerifyMessageBeRequest build = UniversalServiceProto.SmsVerifyMessageBeRequest.newBuilder()
                    .setTel(mobile)
                    .setTemplateCode("T00043")
                    .setTenantId(XBB_USER_CONTEXT.get().getTenantId())
                    .build();
            universalServiceBlockingStub.sendSmsVerify(build);
        }
        redisTemplate.opsForValue().set(key + now + accountId, accountId, 24, TimeUnit.HOURS);
        return JsonResponse.success();
    }


    /**
     * 员工字典保存
     *
     * @param param 参数
     * @return id
     */
    public Long saveDict(EmployeeDictionarySaveParam param) {
        ScheduleServerProto.DictionarySaveRequest.Builder builder = ScheduleServerProto.DictionarySaveRequest.newBuilder();
        if (param.getId() != null) {
            builder.setId(param.getId());
        }
        builder.setDictionaryName(param.getName());
        builder.setDictionaryTypeValue(param.getType());
        if (StringUtils.isNotBlank(param.getDescription())) {
            builder.setDictionaryDesc(param.getDescription());
        }
        return scheduleServerBlockingStub.saveDictionary(builder.build()).getId();
    }

    /**
     * 员工字典类型 1用工类型 2用工来源
     *
     * @param type 类型
     * @return 结果
     */
    @NonNull
    public List<EmployeeDictionaryResult> findEmployeeDictList(@NonNull Integer type) {
        ScheduleServerProto.DictionaryListResponse dictionaryList = scheduleServerBlockingStub.findDictionaryList(ScheduleServerProto.DictionaryListRequest.newBuilder()
                .setDictionaryTypeValue(type)
                .build());
        List<EmployeeDictionaryResult> results = new ArrayList<>();
        if (CollUtil.isNotEmpty(dictionaryList.getDictionaryListList())) {
            dictionaryList.getDictionaryListList().forEach(dictionaryResponse -> {
                EmployeeDictionaryResult employeeDictionaryResult = new EmployeeDictionaryResult();
                employeeDictionaryResult.setId(dictionaryResponse.getId());
                employeeDictionaryResult.setCode(dictionaryResponse.getCode());
                employeeDictionaryResult.setName(dictionaryResponse.getName());
                employeeDictionaryResult.setDescription(dictionaryResponse.getDescription());
                employeeDictionaryResult.setEditable(dictionaryResponse.getEditable());
                results.add(employeeDictionaryResult);
            });
        }
        return results;
    }

    /**
     * 企业创建并生成 主账号 角色 部门
     *
     * @param param
     * @return
     */
    public JsonResponse<?> saveTenant(@NonNull HrmSaveTenantParam param) {
        HrmServiceProto.SaveTenantRequest build = HrmServiceProto.SaveTenantRequest.newBuilder()
                .setCompany(param.getCompany())
                .setAlias(param.getCompany())
                .setLogo(param.getLogo())
                .setDescription(param.getDescription())
                .setTenantNumber(param.getTenantNumber())
                .setMobile(param.getMobile())
                .setProductId(param.getProductId())
                .setRemark(param.getRemark())
                .setIsCreateAccount(param.getIsCreateAccount())
                .setIsCreateRootOrg(param.getIsCreateRootOrg())
                .build();
        HrmServiceProto.SaveTenantBeResponses saveTenantBeResponses = hrmServiceBlockingStub.saveTenant(build);
        //创建角色和权限
        int tenantId = saveTenantBeResponses.getTenantId();
        Long accountId = saveTenantBeResponses.getAccountId();
        if (accountId == 0L) {
            throw Status.INVALID_ARGUMENT.withDescription("创建主账号失败").asRuntimeException();
        }
        hrmServiceBlockingStub.createAccountPermission(HrmServiceProto.SaveTenantBeResponses.newBuilder()
                .setTenantId(tenantId)
                .setAccountId(accountId).build());
        return JsonResponse.success();
    }

    public JsonResponse<ImportStaffEmployeeCheckResult> importStaffEmployeeCheck(ImportStaffEmployeeCheckQueryParam param) {
        ImportStaffEmployeeCheckResult result = new ImportStaffEmployeeCheckResult();
        if (!ObjectUtils.isEmpty(param.getKey())) {
            Object cacheOb = redisTemplate.opsForValue().get(param.getKey());
            if (!ObjectUtils.isEmpty(cacheOb)) {
                List<ImportStaffEmployeeCheckResult.EmployeeInfo> employeeInfoList = JSONUtil.toList(cacheOb.toString(), ImportStaffEmployeeCheckResult.EmployeeInfo.class);
                result.setKey(param.getKey());
                result.setMatchNum(employeeInfoList.size());
                if (ObjectUtils.isEmpty(employeeInfoList)) {
                    result.setList(employeeInfoList);
                    return JsonResponse.success(result);
                }
                result.setList(employeeInfoList.stream().skip(param.getCurPage() * param.getPageSize()).limit(param.getPageSize()).collect(Collectors.toList()));
                return JsonResponse.success(result);
            }
        }
        // 查询客户id
        UserParam userParam = XBB_USER_CONTEXT.get();
        if (ObjectUtils.isEmpty(userParam)) {
            throw Status.NOT_FOUND.withDescription("查询用户信息失败").asRuntimeException();
        }
        PortalServiceProto.TenantCustomerRelationQueryBeRequest.Builder builder = PortalServiceProto.TenantCustomerRelationQueryBeRequest.newBuilder();
        builder.addTenantId(userParam.getTenantId());
        PortalServiceProto.TenantCustomerRelationListBeResponse tenantCustomerRelationList = portalServiceBlockingStub.findTenantCustomerRelationList(builder.build());
        List<ImportStaffEmployeeCheckResult.EmployeeInfo> employeeInfoList = new ArrayList<>();
        for (PortalServiceProto.TenantCustomerRelationBeResponse tenantCustomerRelationBeResponse : tenantCustomerRelationList.getDataList()) {
            int currPage = 0;
            while (true) {
                Map<String, Object> params = new HashMap<>();
                params.put("pageSize", 500);
                params.put("currPage", currPage++);
                params.put("quitStatus", "NO");
                params.put("compId", tenantCustomerRelationBeResponse.getCustomerId());
                List<StaffResult> staffList = getStaffList(params);
                if (ObjectUtils.isEmpty(staffList)) {
                    break;
                }
                employeeInfoList.addAll(handleEmployeeInfo(staffList));
            }
        }
        String key = UUID.fastUUID().toString(true);
        // 排序.更新在前，判断逻辑：employeeId不为空在前
        Comparator<ImportStaffEmployeeCheckResult.EmployeeInfo> comparing = Comparator.comparing(t -> t.getEmployeeId() == null ? 0 : t.getEmployeeId());
        employeeInfoList = employeeInfoList.stream().sorted(comparing.reversed()).collect(Collectors.toList());
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(employeeInfoList), 60 * 15, TimeUnit.SECONDS);
        result.setKey(key);
        result.setMatchNum(employeeInfoList.size());
        result.setList(employeeInfoList.stream().skip(param.getCurPage() * param.getPageSize()).limit(param.getPageSize()).collect(Collectors.toList()));
        return JsonResponse.success(result);
    }

    private Collection<? extends ImportStaffEmployeeCheckResult.EmployeeInfo> handleEmployeeInfo(List<StaffResult> list) {
        if (ObjectUtils.isEmpty(list)) {
            log.info("handleEmployeeInfo-empty");
            return new ArrayList<>();
        }
        List<String> certificateNos = list.stream().map(StaffResult::getCertificateNo).distinct().filter(StrUtil::isNotBlank).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(certificateNos)) {
            log.info("handleEmployeeInfo-certificateNos-empty");
            return new ArrayList<>();
        }
        HrmServiceProto.EmployeePageBeRequest.Builder builder = HrmServiceProto.EmployeePageBeRequest.newBuilder();
        builder.addAllIdCards(certificateNos);
        builder.setTenantId(XBB_USER_CONTEXT.get().getTenantId());
        HrmServiceProto.EmployeePageResponses employeePageList = hrmServiceBlockingStub.withDeadlineAfter(30, TimeUnit.SECONDS).findEmployeePageList(builder.build());
        Map<String, HrmServiceProto.EmployeePageResponse> existEmployeeMap = employeePageList.getDataList().stream().collect(Collectors.toMap(HrmServiceProto.EmployeePageResponse::getIdCard, Function.identity(), (x, y) -> x));
        List<ImportStaffEmployeeCheckResult.EmployeeInfo> employeeInfoList = new ArrayList<>();
        for (StaffResult staffResult : list) {
            if (ObjectUtils.isEmpty(staffResult.getCertificateNo())) {
                continue;
            }
            ImportStaffEmployeeCheckResult.EmployeeInfo employeeInfo = new ImportStaffEmployeeCheckResult.EmployeeInfo();
            employeeInfo.setStaff(staffResult);
            HrmServiceProto.EmployeePageResponse employeePageResponse = existEmployeeMap.get(staffResult.getCertificateNo());
            if (!ObjectUtils.isEmpty(employeePageResponse)) {
                if (staffResult.getName() != null && staffResult.getName().equals(employeePageResponse.getName())
                        && staffResult.getTelephone() != null && staffResult.getTelephone().equals(employeePageResponse.getMobile())){
                    // 姓名，手机号相同需要过滤掉
                    continue;
                }
                employeeInfo.setEmployeeName(employeePageResponse.getName());
                employeeInfo.setEmployeeTel(employeePageResponse.getMobile());
                employeeInfo.setEmployeeIdCardNum(employeePageResponse.getIdCard());
                employeeInfo.setEmployeeIdCardTypeCode(employeePageResponse.getIdCardType());
                employeeInfo.setEmployeeId(employeePageResponse.getId());
                employeeInfo.setEmployeeOrgId(employeePageResponse.getOrgId());
            }
            employeeInfoList.add(employeeInfo);
        }
        return employeeInfoList;
    }

    private List<StaffResult> getStaffList(Map<String, Object> params3) {
        Map<String, Object> params2 = new HashMap<>();
        params2.put("queryStaffDto", params3);
        XhRpcParam xhRpcParam = new XhRpcParam();
        xhRpcParam.setRequest(params2)
                .setServiceProject(EnumProject.HROSTAFF)
                .setUri("IStaffService/listStaff");
        XhR<XhTotal<StaffResult>> xhR = xhRpcComponent.sendForTotal(xhRpcParam, StaffResult.class);
        if (xhR.getStatus() != 0) {
            throw Status.INTERNAL.withDescription(xhR.getMessage()).asRuntimeException();
        }
        return xhR.getData().getList();
    }

    public JsonResponse<?> importStaffEmployeeSubmit(ImportStaffEmployeeSubmitParam param) {
        UserParam userParam = XBB_USER_CONTEXT.get();
        if (ObjectUtils.isEmpty(userParam)) {
            throw Status.NOT_FOUND.withDescription("查询用户信息失败").asRuntimeException();
        }
        Object cacheOb = redisTemplate.opsForValue().get(param.getKey());
        if (ObjectUtils.isEmpty(cacheOb)) {
            throw Status.NOT_FOUND.withDescription("缓存过期，请重新拉取").asRuntimeException();
        }
        if (ObjectUtils.isEmpty(param.getIndexList())) {
            throw Status.DATA_LOSS.withDescription("请选择数据").asRuntimeException();
        }
        List<ImportStaffEmployeeCheckResult.EmployeeInfo> employeeInfoList = JSONUtil.toList(cacheOb.toString(), ImportStaffEmployeeCheckResult.EmployeeInfo.class);
        if (ObjectUtils.isEmpty(employeeInfoList)) {
            return JsonResponse.success("空数据");
        }
        List<Long> indexList = param.getIndexList().stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<ImportStaffEmployeeCheckResult.EmployeeInfo> list = employeeInfoList.stream().filter(t -> t.getStaff() != null && indexList.contains(t.getStaff().getId())).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(list)) {
            return JsonResponse.success("空数据");
        }
        if (list.size() > 200) {
            log.info(String.format("importStaffEmployeeSubmit-size:%s", list.size()));
            try {
                notifyHelper.sendDdMessage(String.format("导入勋厚人力人数超限：%s", list.size()), "15375370103");
            } catch (Exception e) {
                log.info("importStaffEmployeeSubmit", e);
            }
        }
        // employeeId不为空是更新，为空是新增
        for (ImportStaffEmployeeCheckResult.EmployeeInfo employeeInfo : list) {
            HrmServiceProto.SaveOrUpdateEmployeeListRequest.Builder builder = HrmServiceProto.SaveOrUpdateEmployeeListRequest.newBuilder();
            StaffResult staff = employeeInfo.getStaff();
            if (ObjectUtils.isEmpty(staff)) {
                log.warn("importNewStaffEmployeeSubmit-staff-empty");
                continue;
            }
            HrmServiceProto.SaveOrUpdateEmployeeRequest.Builder saveOrUpdateEmployeeRequest = HrmServiceProto.SaveOrUpdateEmployeeRequest.newBuilder();
            saveOrUpdateEmployeeRequest.setName(staff.getName())
                    .setOrgId(XbbRpcConvert.toLong(employeeInfo.getEmployeeOrgId()))
                    .setOperationId(userParam.getUserId())
                    .setMobile(staff.getTelephone());
            if (ObjectUtils.isEmpty(employeeInfo.getEmployeeId())) {
                saveOrUpdateEmployeeRequest.setJobType(4)//4#其他
                        // .setOrgId(0L)//初始值
                        .setInviteType(3L)//勋厚人力
                        .setSource(4)//saas创建
                        .setIdCardType(XbbRpcConvert.toInt(staff.getCertificateCode()))
                        .setIdCard(XbbRpcConvert.toString(staff.getCertificateNo()));
                if (!ObjectUtils.isEmpty(staff.getOnboardDate())) {
                    saveOrUpdateEmployeeRequest.setLastEntryDate(new DateTime(staff.getOnboardDate()).getTime());
                }
                if (!ObjectUtils.isEmpty(staff.getCertificateCode()) && staff.getCertificateCode() > 0) {
                    saveOrUpdateEmployeeRequest.setIdCardType(staff.getCertificateCode());
                } else {
                    saveOrUpdateEmployeeRequest.setIdCardType(1);//身份证
                }
            }
            if (Objects.nonNull(employeeInfo.getEmployeeId()) && employeeInfo.getEmployeeId() > 0) {
                saveOrUpdateEmployeeRequest.setId(employeeInfo.getEmployeeId());
            }
            builder.addData(saveOrUpdateEmployeeRequest.build());
            try {
                hrmServiceBlockingStub.saveOrUpdateEmployee(builder.build());
            } catch (Exception e) {
                log.warn("importStaffEmployeeSubmit-hrmServiceBlockingStub.saveOrUpdateEmployee", e);
            }
        }
        redisTemplate.delete(param.getKey());
        return JsonResponse.success();
    }

    public List<ContractResult> findContractByEmployeeId(Long employeeId) {
        SignServerProto.ContractPageListResponse contractPageListResponse = signServerBlockingStub.contractList(SignServerProto.ContractListRequest.newBuilder().setEmployeeId(employeeId).build());
        // 查询协议
        List<Long> agreementTemplateIds = contractPageListResponse.getDataList().stream().filter(t -> t != null && t.getType() == 2).map(t -> t.getTemplateId())
                .distinct().filter(t -> t != null && t > 0).collect(Collectors.toList());
        Map<Long, String> agreementNameByTemplateIdMap = new HashMap<>();
        if (!ObjectUtils.isEmpty(agreementTemplateIds)) {
            HashMap<Object, Object> params = new HashMap<>();
            params.put("ids", agreementTemplateIds);
            XhRpcParam xhRpcParam = new XhRpcParam()
                    .setRequest(params)
                    .setServiceProject(EnumProject.HROSTAFF)
                    .setUri("IProtocolTemplateService/batchGetProtocolTemplateByIds");
            XhR<List<ContractResult>> xhR = xhRpcComponent.sendForList(xhRpcParam, ContractResult.class);
            if (xhR.getStatus() != 0) {
                throw Status.INTERNAL.withDescription(xhR.getMessage()).asRuntimeException();
            }
            agreementNameByTemplateIdMap.putAll(xhR.getData().stream().collect(Collectors.toMap(ContractResult::getId, ContractResult::getName, (x, y) -> x)));
        }
        return contractPageListResponse.getDataList().stream().map(t -> {
            ContractResult result = new ContractResult();
            if (t.getType() == 1) {
                HashMap<Object, Object> params = new HashMap<>();
                params.put("id", t.getTemplateId());
                XhRpcParam xhRpcParam = new XhRpcParam()
                        .setRequest(params)
                        .setServiceProject(EnumProject.HROSTAFF)
                        .setUri("IContractTemplateService/getContractTemplateById");
                XhR<ContractResult> xhR = xhRpcComponent.send(xhRpcParam, ContractResult.class);
                if (xhR.getStatus() != 0) {
                    throw Status.INTERNAL.withDescription(xhR.getMessage()).asRuntimeException();
                }
                result.setName(xhR.getData() != null ? xhR.getData().getName() : "");
            } else {
                result.setName(agreementNameByTemplateIdMap.get(t.getTemplateId()));
            }
            result.setTemplateId(t.getTemplateId());
            result.setContractType(t.getType());
            if (!ObjectUtils.isEmpty(t.getContractOssId())) {
                result.setContractOssId(String.format(ConstantData.END_FRONT_STATIC_RESOURCE_URL_PREFIX, t.getContractOssId()));
            }
            result.setId(t.getId());
            return result;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
