package cn.xunhou.web.xbbcloud.product.salary.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.core.snow.SnowflakeIdGenerator;
import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.cloud.core.web.XbbWebStatus;
import cn.xunhou.cloud.task.bean.AddExportTaskParam;
import cn.xunhou.grpc.proto.asset.AssetXhServerGrpc;
import cn.xunhou.grpc.proto.asset.AssetXhServerProto;
import cn.xunhou.grpc.proto.crm.CrmServiceGrpc;
import cn.xunhou.grpc.proto.crm.CrmServiceProto;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.portal.PortalServiceGrpc;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import cn.xunhou.grpc.proto.subject.SubjectServiceGrpc;
import cn.xunhou.grpc.proto.subject.SubjectServiceProto;
import cn.xunhou.grpc.proto.template.task.TemplateTaskServiceGrpc;
import cn.xunhou.grpc.proto.template.task.TemplateTaskServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerGrpc;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.grpc.proto.xbbcloud.SignServerGrpc;
import cn.xunhou.grpc.proto.xbbcloud.SignServerProto;
import cn.xunhou.web.xbbcloud.common.constants.CommonConst;
import cn.xunhou.web.xbbcloud.common.constants.RedisConst;
import cn.xunhou.web.xbbcloud.config.xhrpc.XhRpcComponent;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumProject;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumXhTenant;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhR;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhRpcParam;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhTotal;
import cn.xunhou.web.xbbcloud.product.hrm.param.ImportSalaryRowData;
import cn.xunhou.web.xbbcloud.product.manage.param.CustomerContractParam;
import cn.xunhou.web.xbbcloud.product.manage.result.BusinessContractResult;
import cn.xunhou.web.xbbcloud.product.manage.result.ContractProjectResult;
import cn.xunhou.web.xbbcloud.product.manage.result.CustomerContractResult;
import cn.xunhou.web.xbbcloud.product.salary.convert.SalaryConvert;
import cn.xunhou.web.xbbcloud.product.salary.enums.EnumDispatchStatusCovertMsg;
import cn.xunhou.web.xbbcloud.product.salary.param.*;
import cn.xunhou.web.xbbcloud.product.salary.result.*;
import cn.xunhou.web.xbbcloud.util.XhFileUtils;
import cn.xunhou.web.xbbcloud.util.pojo.result.ZipFilesResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.Status;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SalaryService {

    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();
    @GrpcClient("ins-xbbcloud-platform")
    private SalaryServerGrpc.SalaryServerBlockingStub salaryServerBlockingStub;

    @GrpcClient("ins-xhportal-platform")
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;

    @GrpcClient("ins-xhportal-platform")
    private static SubjectServiceGrpc.SubjectServiceBlockingStub subjectServiceBlockingStub;

    @GrpcClient("ins-xhtask-platform")
    private TemplateTaskServiceGrpc.TemplateTaskServiceBlockingStub templateTaskServiceBlockingStub;
    @GrpcClient("ins-xhportal-platform")
    private PortalServiceGrpc.PortalServiceBlockingStub portalServiceBlockingStub;
    @GrpcClient("ins-assetxh-platform")
    private AssetXhServerGrpc.AssetXhServerBlockingStub assetXhServerBlockingStub;

    @GrpcClient("ins-xhcrm-platform")
    private CrmServiceGrpc.CrmServiceBlockingStub crmServiceBlockingStub;
    @GrpcClient("ins-xbbcloud-platform")
    private SignServerGrpc.SignServerBlockingStub signServerBlockingStub;

    @Resource
    private XhRpcComponent xhRpcComponent;
    @Resource
    private SalaryDetailExportService salaryDetailExportService;
    @Value("${spring.application.client_id}")
    private String clientId;
    @Resource
    private CommonService commonService;

    /**
     * 查询当前登录租户信息
     *
     * @return
     */
    public SalaryMerchantInfoResult merchantInfo() {
        return merchantInfo(Long.valueOf(XBB_USER_CONTEXT.get().getTenantId()));
    }

    /**
     * 管理端
     * 查询商户信息
     *
     * @return 商户信息
     */
    public SalaryMerchantInfoResult merchantInfo(Long tenantId) {
        SalaryServerProto.MerchantInfoResponse merchantInfoResponse = salaryServerBlockingStub.queryMerchantInfo(SalaryServerProto.MerchantInfoRequest.newBuilder()
                .setTenantId(tenantId)
                .build());
        HrmServiceProto.findXgkRoleByTenantBeResponses roleByTenantBeResponses = hrmServiceBlockingStub.findXgkRoleByTenant(HrmServiceProto.SwitchRoleRequest.newBuilder()
                .setTenantId(tenantId.intValue())
                .setRoleName("HRM-薪酬云角色")
                .build());
        return SalaryConvert.response2Result(merchantInfoResponse).setIsUse(roleByTenantBeResponses.getType());
    }

    /**
     * 校验当前商户是否开启薪酬云功能
     */
    public void checkMerchantIsUse() {
        SalaryMerchantInfoResult result = merchantInfo();
        if (result == null || result.getIsUse() == null || result.getIsUse() != CommonConst.ONE) {
            throw Status.INTERNAL.withDescription("当前商户薪酬云功能未启用").asRuntimeException();
        }
    }

    /**
     * 查询商户余额
     *
     * @return 商户余额信息
     */
    public SalaryMerchantInfoResult findMerchantBalance(Long tenantId) {
        if (tenantId == null) {
            tenantId = Long.valueOf(XBB_USER_CONTEXT.get().getTenantId());
        }
        SalaryServerProto.MerchantInfoResponse merchantInfoResponse = salaryServerBlockingStub.findMerchantBalance(SalaryServerProto.MerchantInfoRequest.newBuilder()
                .setTenantId(tenantId)
                .build());
        return SalaryConvert.response2Result(merchantInfoResponse);
    }

    /**
     * 新增｜保存 商户信息
     *
     * @param param 商户信息
     * @return 最新商户信息
     */
    public SalaryMerchantInfoResult saveMerchantInfo(SalaryMerchantInfoParam param) {
        Long payeeCustomerId = null, businessContractCustomerId = null;
        if (param.getTenantType() == SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE) {
            List<Long> customerIds = getCustomerIds(param.getTenantId().intValue());
            //子账户信息
            AssetXhServerProto.SubAccountResponse subAccountResponse = CollUtil.getFirst(assetXhServerBlockingStub.querySubAccount(AssetXhServerProto.SubAccountQueryRequest.newBuilder()
                    .addSubAccountIds(param.getPayeeSubAccountId())
                    .setPageable(false)
                    .build()).getSubAccountInfosList());
            payeeCustomerId = subAccountResponse.getCustomerId();
            if (!customerIds.contains(payeeCustomerId)) {
                throw new SystemRuntimeException("租户下客户子账户数据异常！");
            }

            Map<String, Object> customerContractParam = new HashMap<>();
            customerContractParam.put("contractId", param.getContractId());
            customerContractParam.put("tenant", EnumXhTenant.XUNHOU);
            XhRpcParam xhRpcParam = new XhRpcParam();
            xhRpcParam.setRequest(customerContractParam)
                    .setServiceProject(EnumProject.USERXH)
                    .setUri("ICustomerContractService/getCustomerContractDtoById");
            XhR<CustomerContractResult> resultXhR = xhRpcComponent.send(xhRpcParam, CustomerContractResult.class);
            if (resultXhR.getData() == null) {
                throw new SystemRuntimeException("当前商务合同未找到！");
            }
            businessContractCustomerId = resultXhR.getData().getCustomerId();
            if (!customerIds.contains(payeeCustomerId)) {
                throw new SystemRuntimeException("租户下客户商业合同数据异常！");
            }

        }
        SalaryServerProto.SaveMerchantInfoRequest.Builder request = SalaryConvert.param2Request(param,
                payeeCustomerId, businessContractCustomerId,
                XBB_USER_CONTEXT.get().getUserId());
        return SalaryConvert.response2Result(salaryServerBlockingStub.saveMerchantInfo(request.build()));
    }

    /**
     * 操作商户流水
     *
     * @param param 操作流水
     * @return 操作流水结果
     */
    public SalaryMerchantFlowResult operateMerchantFlow(SalaryOperateMerchantFlowParam param) {
        SalaryServerProto.OperateMerchantFlowRequest.Builder request = SalaryConvert.param2Request(param, XBB_USER_CONTEXT.get().getUserId());
        SalaryServerProto.MerchantFlowsResponse response = salaryServerBlockingStub.operateMerchantFlow(request.build());
        return SalaryConvert.response2Result(response);
    }

    /**
     * 商户分页
     *
     * @param param 查询入参
     * @return 分页结果
     */
    public JsonListResponse<SalaryMerchantFlowResult> findMerchantFlow(SalaryMerchantFlowPageParam param) {
        SalaryServerProto.MerchantFlowPageRequest.Builder request = SalaryConvert.param2Request(param, Long.valueOf(XBB_USER_CONTEXT.get().getTenantId()), XBB_USER_CONTEXT.get().getUserId());
        SalaryServerProto.MerchantFlowsPageResponse response = salaryServerBlockingStub.findMerchantFlow(request.build());
        List<SalaryMerchantFlowResult> resultList = Lists.newArrayList();
        List<Long> operatorIds = Lists.newArrayList();
        for (SalaryServerProto.MerchantFlowsResponse flowsResponse : response.getDataList()) {
            operatorIds.add(flowsResponse.getOperatorId());
            resultList.add(SalaryConvert.response2Result(flowsResponse));
        }
        Map<Long, HrmServiceProto.AccountDetailBeResponse> nameMap = accountDetailBeResponseMap(operatorIds);
        for (SalaryMerchantFlowResult result : resultList) {
            HrmServiceProto.AccountDetailBeResponse accountDetailBeResponse = nameMap.get(result.getOperatorId());
            result.setOperatorName(accountDetailBeResponse == null ? null : accountDetailBeResponse.getNickName());
        }

        return JsonListResponse.success(resultList, response.getTotal());
    }

    /**
     * 批次列表分页
     *
     * @param param
     * @return
     */
    public JsonListResponse<SalaryBatchResult> findSalaryBatchPageList(SalaryBatchPageParam param, boolean isOperation) {
        SalaryServerProto.SalaryBatchConditionBeRequest.Builder request = SalaryConvert.param2Request(param);
        if (isOperation) {
            request.setIsOperation(true);
            if (param.getTenantId() != null) {
                request.setTenantId(param.getTenantId());
            }
        } else {
            request.setTenantId(XBB_USER_CONTEXT.tenantId());
        }
        SalaryServerProto.SalaryBatchPageBeResponse response = salaryServerBlockingStub.findSalaryBatchPageList(request.build());
        List<SalaryBatchResult> resultList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(response.getDataList())) {
            return JsonListResponse.success(resultList, response.getTotal());
        }
        //获取操作人
        Map<Long, HrmServiceProto.AccountDetailBeResponse> accountDetailBeResponseMap =
                accountDetailBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryBatchBeResponse::getOperatorId).collect(Collectors.toList()));
        //获取对应的租户名称
        Map<Long, PortalServiceProto.TenantBeResponse> tenantBeResponseMap = tenantIdBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryBatchBeResponse::getTenantId).collect(Collectors.toList()));

        //获取发薪主体信息
        Map<Long, SubjectServiceProto.SubjectDetailBeResponse> longSubjectDetailBeResponseMap = subjectDetailBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryBatchBeResponse::getSubjectId).distinct().collect(Collectors.toList()));
        //根据查出来的批次id来聚合失败原因
        SalaryServerProto.SalaryDetailConditionBeRequest.Builder salaryDetailRequestBuild = SalaryServerProto.SalaryDetailConditionBeRequest.newBuilder();
        salaryDetailRequestBuild.setPageSize(0);//查全部
        salaryDetailRequestBuild.addAllBatchIds(response.getDataList().stream().map(SalaryServerProto.SalaryBatchBeResponse::getBatchId).collect(Collectors.toList()));
        SalaryServerProto.SalaryDetailPageBeResponse salaryDetailBeResponse = salaryServerBlockingStub.findSalaryDetailPageList(salaryDetailRequestBuild.build());
        Map<Long, List<SalaryServerProto.SalaryDetailBeResponse>> salaryDetailBeResponseMap = salaryDetailBeResponse.getDataList().stream().collect(Collectors.groupingBy(SalaryServerProto.SalaryDetailBeResponse::getBatchId));


        for (SalaryServerProto.SalaryBatchBeResponse salaryBatchBeResponse : response.getDataList()) {


            resultList.add(SalaryConvert.response2Result(salaryBatchBeResponse, longSubjectDetailBeResponseMap.get(salaryBatchBeResponse.getSubjectId()), tenantBeResponseMap.get(salaryBatchBeResponse.getTenantId()), salaryDetailBeResponseMap.get(salaryBatchBeResponse.getBatchId()), accountDetailBeResponseMap.get(salaryBatchBeResponse.getOperatorId())));
        }


        return JsonListResponse.success(resultList, response.getTotal());
    }

    public JsonListResponse<SalaryDetailResult> findSalaryDetailPageList(SalaryDetailPageParam param, boolean isOperation) {
        SalaryServerProto.SalaryDetailConditionBeRequest.Builder request = SalaryConvert.param2Request(param);
        //如果租户名称不为空 查出租户对应id列表
        /*if (StringUtils.isNotBlank(param.getTenantName())) {
            PortalServiceProto.TenantPageQueryBeRequest.Builder findTenantBuilder = PortalServiceProto.TenantPageQueryBeRequest.newBuilder();
            findTenantBuilder.setPaged(false);
            findTenantBuilder.setAccurateAliasName(param.getTenantName());
            PortalServiceProto.TenantBeResponses tenantPageListRep = portalServiceBlockingStub.findTenantPageList(findTenantBuilder.build());
            if (CollectionUtils.isNotEmpty(tenantPageListRep.getDataList())) {
                request.addAllTenantIds(tenantPageListRep.getDataList().stream().map(tenantBeResponse -> {
                    return Long.valueOf(tenantBeResponse.getTenantId());
                }).collect(Collectors.toList()));
            }
        }*/
        if (isOperation) {
            request.setIsOperation(true);
            if (param.getTenantId() != null) {
                request.setTenantId(param.getTenantId());
            }
        } else {
            request.setTenantId(XBB_USER_CONTEXT.tenantId());
        }
        SalaryServerProto.SalaryDetailPageBeResponse response = salaryServerBlockingStub.findSalaryDetailPageList(request.build());
        List<SalaryDetailResult> resultList = Lists.newArrayList();
        //获取操作人
        Map<Long, HrmServiceProto.AccountDetailBeResponse> accountDetailBeResponseMap =
                accountDetailBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryDetailBeResponse::getOperatorId).collect(Collectors.toList()));

        //获取对应的租户名称
        Map<Long, PortalServiceProto.TenantBeResponse> tenantBeResponseMap = tenantIdBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryDetailBeResponse::getTenantId).collect(Collectors.toList()));
        //获取发薪主体信息
        Map<Long, SubjectServiceProto.SubjectDetailBeResponse> longSubjectDetailBeResponseMap = subjectDetailBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryDetailBeResponse::getSubjectId).distinct().collect(Collectors.toList()));
        for (SalaryServerProto.SalaryDetailBeResponse salaryDetailBeResponse : response.getDataList()) {
            resultList.add(SalaryConvert.response2Result(isOperation, longSubjectDetailBeResponseMap.get(salaryDetailBeResponse.getSubjectId()), salaryDetailBeResponse, tenantBeResponseMap.get(salaryDetailBeResponse.getTenantId()), accountDetailBeResponseMap.get(salaryDetailBeResponse.getOperatorId())));
        }
        return JsonListResponse.success(resultList, response.getTotal());

    }

    /**
     * 获取操作人Map
     */
    private Map<Long, HrmServiceProto.AccountDetailBeResponse> accountDetailBeResponseMap(Collection<Long> operatorIds) {
        if (CollUtil.isEmpty(operatorIds)) {
            return Maps.newHashMap();
        }
        HrmServiceProto.AccountBeResponses accountBeResponses = hrmServiceBlockingStub.findAccountByIds(HrmServiceProto.SnowAccountRequest.newBuilder()
                .addAllId(Sets.newHashSet(operatorIds))
                .build());
        return accountBeResponses.getDataList().stream().collect(Collectors.toMap(HrmServiceProto.AccountDetailBeResponse::getId, Function.identity()));
    }

    /**
     * 获取发薪主体信息
     */
    private Map<Long, SubjectServiceProto.SubjectDetailBeResponse> subjectDetailBeResponseMap(Collection<Long> subjectIds) {
        if (CollUtil.isEmpty(subjectIds)) {
            return Maps.newHashMap();
        }
        //主体信息
        SubjectServiceProto.SubjectDetailBeResponses subjectDetailBeResponses = subjectServiceBlockingStub.getSubjectObjectByIds(SubjectServiceProto.IdBeRequests.newBuilder()
                .addAllId(subjectIds)
                .build());
        return subjectDetailBeResponses.getDataList().stream().collect(Collectors.toMap(SubjectServiceProto.SubjectDetailBeResponse::getSubjectId, Function.identity()));
    }

    /**
     * 获取租户名称Map
     */
    private Map<Long, PortalServiceProto.TenantBeResponse> tenantIdBeResponseMap(Collection<Long> tenantIds) {
        if (CollUtil.isEmpty(tenantIds)) {
            return Maps.newHashMap();
        }
        PortalServiceProto.TenantBeResponses tenantPageListRep = portalServiceBlockingStub.findTenantPageList(PortalServiceProto.TenantPageQueryBeRequest.newBuilder().setPaged(false).addAllTenantIds(tenantIds).build());
        return tenantPageListRep.getDataList().stream().collect(Collectors.toMap(o -> {
            return Long.valueOf(o.getTenantId());
        }, Function.identity()));
    }

    public JsonResponse<List<SalaryProductResult>> querySalaryProduct() {
        List<SalaryProductResult> resultList = new ArrayList<>();
        SalaryServerProto.SalaryProductListResponse salaryProductListResponse = salaryServerBlockingStub.querySalaryProduct(Empty.newBuilder().build());
        for (SalaryServerProto.SalaryProductResponse salaryProductResponse : salaryProductListResponse.getDataList()) {
            SalaryProductResult salaryProductResult = new SalaryProductResult();
            salaryProductResult.setName(salaryProductResponse.getName());
            resultList.add(salaryProductResult);
        }
        return JsonResponse.success(resultList);
    }

    public JsonResponse<SalaryBatchResult> saveSalaryBatch(SalaryBatchSaveParam param) {

        SalaryServerProto.SalaryBatchRequest.Builder builder = SalaryServerProto.SalaryBatchRequest.newBuilder();
        builder.setProductName(param.getProductName());
        builder.setMonth(param.getMonth());
        builder.setSalaryFile(param.getSalaryFile());
        List<ImportSalaryRowData> importSalaryRowData = XhFileUtils.readExcel(param.getSalaryFile(), ImportSalaryRowData.class);
        if (importSalaryRowData.size() >= 1000) {
            throw new SystemRuntimeException("单次上传发薪单，小于1000");
        }
        if (CollectionUtils.isNotEmpty(importSalaryRowData)) {
            List<SalaryServerProto.SalaryDetailRequest> salaryDetailRequests = new ArrayList<>();
            importSalaryRowData.forEach(importSalaryRowDataDto -> {
                SalaryServerProto.SalaryDetailRequest.Builder s = SalaryServerProto.SalaryDetailRequest.newBuilder()
                        .setName(importSalaryRowDataDto.getName()).setPhone(importSalaryRowDataDto.getPhone()).setIdCardNo(importSalaryRowDataDto.getIdCardNo())
                        .setTaxAmount(NumberUtil.mul(importSalaryRowDataDto.getTaxAmount() == null ? "0" : importSalaryRowDataDto.getTaxAmount(), "100").intValue()).setPayableAmount(NumberUtil.mul(importSalaryRowDataDto.getPaidAbleAmount(), "100").intValue());

                salaryDetailRequests.add(s.build());
            });
            builder.addAllData(salaryDetailRequests);
        }
        SalaryServerProto.saveSalaryBatchResponse saveSalaryBatchResponse = salaryServerBlockingStub.withDeadlineAfter(10, TimeUnit.SECONDS).saveSalaryBatch(builder.build());
        SalaryBatchResult salaryBatchResult = new SalaryBatchResult();
        salaryBatchResult.setBatchId(String.valueOf(saveSalaryBatchResponse.getBatchId()));
        return JsonResponse.success(salaryBatchResult);
    }

    public List<SalaryDetailExportData> getExportData(SalaryDetailPageParam param) {

        log.info("发薪明细导出参数" + param);
        JsonListResponse<SalaryDetailResult> response = findSalaryDetailPageList(param, false);
        if (CollectionUtils.isEmpty(response.getData())) {
            return Collections.emptyList();
        }
        List<SalaryDetailExportData> dataList = new ArrayList<>();
        response.getData().forEach(salaryDetailResult -> {
            SalaryDetailExportData salaryDetailExportData = new SalaryDetailExportData();
            BeanUtils.copyProperties(salaryDetailResult, salaryDetailExportData);
            dataList.add(salaryDetailExportData);
        });
        log.info("发薪明细导出 ===>查询出的数据" + XbbJsonUtil.toJsonString(dataList));
        return dataList;
    }


    @SneakyThrows
    public JsonResponse<String> export(SalaryDetailPageParam param) {
        String taskCode = RedisConst.EXP_SALARY_DETAIL + SnowflakeIdGenerator.getId();
        param.setPageSize(0);
        log.info("导出,taskCode=" + taskCode);
        log.info("导出查询条件,taskCode=" + param);
        AddExportTaskParam addExportTaskParam = AddExportTaskParam.newBuilder().tenantId(XBB_USER_CONTEXT.tenantId()).taskCode(taskCode).params(XbbJsonUtil.toJsonBytes(param)).creator(XBB_USER_CONTEXT.get().getUserId()).fileName("发薪明细").build();
        TemplateTaskServiceProto.TaskBeRequest.Builder builder = TemplateTaskServiceProto.TaskBeRequest.newBuilder().setTenantId(XBB_USER_CONTEXT.tenantId()).setClientId(this.clientId).setTemplateId(SalaryDetailExportService.class.getSimpleName()).setParams(ByteString.copyFrom(addExportTaskParam.getParams())).setTaskCode(taskCode).setTaskType(TemplateTaskServiceProto.TaskType.EXPORT).addAllField((Iterable) Opt.ofNullable(addExportTaskParam.getFields()).orElseGet(Collections::emptyList)).setFileName(StrUtil.nullToEmpty(addExportTaskParam.getFileName())).setCreator(addExportTaskParam.getCreator()).setStatus(TemplateTaskServiceProto.TaskStatus.CREATED);
        templateTaskServiceBlockingStub.addTask(builder.build());
        //salaryDetailExportService.addExportTask(addExportTaskParam);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            TemplateTaskServiceProto.TaskBeResponses responses = templateTaskServiceBlockingStub.findTasks(TemplateTaskServiceProto.QueryTaskBeRequest.newBuilder()
                    .setPage(0)
                    .setPageSize(1)
                    .setTaskCode(taskCode)
                    .build());
            TemplateTaskServiceProto.TaskBeResponse response = responses.getDataList().get(0);
            if (TemplateTaskServiceProto.TaskStatus.FAILED == response.getStatus()) {
                log.info("导出失败,taskCode=" + taskCode);
                return JsonResponse.failed(XbbWebStatus.System_Error);
            }
            if (TemplateTaskServiceProto.TaskStatus.COMPLETED == response.getStatus()) {
                return JsonResponse.success(response.getObjectKey());
            }
        }
        return JsonResponse.failed(XbbWebStatus.System_Error);
    }




    /**
     * 租户获取客户
     *
     * @param tenantId 租户
     * @return 客户id
     */
    public List<Long> getCustomerIds(Integer tenantId) {
        //获取saas客户绑定的人力客户
        PortalServiceProto.TenantCustomerRelationListBeResponse tenantCustomerRelationListBeResponse = portalServiceBlockingStub.findTenantCustomerRelationList(
                PortalServiceProto.TenantCustomerRelationQueryBeRequest.newBuilder()
                        .addTenantId(tenantId)
                        .build()
        );
        List<Long> customerIds = Lists.newArrayList();
        List<PortalServiceProto.TenantCustomerRelationBeResponse> tenantCustomerRelationBeResponses = tenantCustomerRelationListBeResponse.getDataList();
        for (PortalServiceProto.TenantCustomerRelationBeResponse customerRelationBeResponse : tenantCustomerRelationBeResponses) {
            customerIds.add(customerRelationBeResponse.getCustomerId());
        }
        if (CollUtil.isEmpty(customerIds)) {
            return Collections.emptyList();
        }
        return customerIds;
    }


    /**
     * 租户的所有主体下所有子账户
     *
     * @return 主体信息
     */
    public List<SubjectAndSubAccountResult> subjectAndSubAccountInfo(Integer tenantId) {
        List<Long> customerIds = getCustomerIds(tenantId == null ? XBB_USER_CONTEXT.get().getTenantId() : tenantId);
        //子账户信息
        AssetXhServerProto.SubAccountResponses subAccountResponses = assetXhServerBlockingStub.querySubAccount(AssetXhServerProto.SubAccountQueryRequest.newBuilder()
                .addAllCustomerIds(customerIds)
                .setSubAccountType(AssetXhServerProto.EnumSubAccountType.CM_BANK)
                .setCustomerSubAccountType(AssetXhServerProto.EnumCustomerSubAccountType.SA_LABOR_PAYROLL)
                .setPageable(false)
                .build());
        Map<Long, List<SubAccountInfoResult>> subjectSubAccountMap = Maps.newHashMap();
        for (AssetXhServerProto.SubAccountResponse subAccountResponse : subAccountResponses.getSubAccountInfosList()) {
            Long subjectInfoId = subAccountResponse.getSubjectInfoId();
            List<SubAccountInfoResult> subAccountInfoResultList = subjectSubAccountMap.get(subjectInfoId);
            SubAccountInfoResult accountInfoResult = SalaryConvert.response2Result(subAccountResponse);
            if (subAccountInfoResultList == null) {
                subAccountInfoResultList = Lists.newArrayList();
                subjectSubAccountMap.put(subjectInfoId, subAccountInfoResultList);
            }
            if (customerIds.contains(accountInfoResult.getCustomerId())) {
                subAccountInfoResultList.add(accountInfoResult);
            }
        }
        if (CollUtil.isEmpty(subjectSubAccountMap)) {
            return Collections.emptyList();
        }
        //主体信息
        SubjectServiceProto.SubjectDetailBeResponses subjectDetailBeResponses = subjectServiceBlockingStub.getSubjectObjectByIds(SubjectServiceProto.IdBeRequests.newBuilder()
                .addAllId(subjectSubAccountMap.keySet())
                .build());

        List<SubjectAndSubAccountResult> resultList = Lists.newArrayList();
        for (SubjectServiceProto.SubjectDetailBeResponse subjectDetailBeResponse : subjectDetailBeResponses.getDataList()) {
            List<SubAccountInfoResult> subAccountList = subjectSubAccountMap.get(subjectDetailBeResponse.getSubjectId());
            if (CollUtil.isNotEmpty(subAccountList)) {
                resultList.add(SalaryConvert.response2Result(subjectDetailBeResponse, subAccountList));
            }
        }
        return resultList;
    }


    /**
     * 1. 获取用户的所有劳动合同
     */
    public String checkUserPayerSubjectId(String idCardNo, Long payerSubjectId) {

        //通过身份证号获取所有合同
        SignServerProto.ContractListRequest.Builder contractListRequestBuild = SignServerProto.ContractListRequest.newBuilder();
        contractListRequestBuild.setPageSize(0);//不分页
        contractListRequestBuild.setIdCardNo(idCardNo);
        contractListRequestBuild.setType(SignServerProto.EnumTemplateType.CONTRACT_VALUE);
        contractListRequestBuild.addAllStatusList(Arrays.asList(SignServerProto.EnumContractStatus.EFFECTING_VALUE, SignServerProto.EnumContractStatus.OVERTIME_VALUE, SignServerProto.EnumContractStatus.EARLY_TERMINATION_VALUE));
        SignServerProto.ContractPageListResponse contractPageListResponse = signServerBlockingStub.contractList(contractListRequestBuild.build());
        if (CollectionUtils.isEmpty(contractPageListResponse.getDataList())) {
            return "没有签署过合同,请签署";
        }
        List<Long> subjectIds = contractPageListResponse.getDataList().stream().map(SignServerProto.ContractListResponse::getSubjectId).collect(Collectors.toList());

        //当前用户合同中，不存在当前发薪主体则走下列逻辑
        if (!subjectIds.contains(payerSubjectId)) {
            subjectIds.add(payerSubjectId);
            SubjectServiceProto.SubjectDetailBeResponse payerSubjectDto = null;
            //主体信息
            SubjectServiceProto.SubjectDetailBeResponses subjectDetailBeResponses = subjectServiceBlockingStub.getSubjectObjectByIds(SubjectServiceProto.IdBeRequests.newBuilder()
                    .addAllId(subjectIds)
                    .build());

            boolean isThrow = true;
            for (SubjectServiceProto.SubjectDetailBeResponse subjectDetailBeResponse : subjectDetailBeResponses.getDataList()) {
                if (payerSubjectId.equals(subjectDetailBeResponse.getSubjectId())) {
                    payerSubjectDto = subjectDetailBeResponse;
                } else {
                    //非发薪主体中存在内部 则不抛出异常
                    if (subjectDetailBeResponse.getSubjectType() == SubjectServiceProto.SubjectTypeEnum.XH) {
                        isThrow = false;
                    }
                }
            }
            if (payerSubjectDto == null) {
                return "参数异常，没有查到对应的发薪主体";
            }
            if (payerSubjectDto.getSubjectType() == SubjectServiceProto.SubjectTypeEnum.XH) {
                //发薪主体内部
                if (isThrow) {
                    return "该用户没有签约过内部主体的合同";
                }

            } else if (payerSubjectDto.getSubjectType() == SubjectServiceProto.SubjectTypeEnum.UN_XH) {
                return "该用户没有签约过对应外部主体的合同";
            } else {
                return "参数异常，发薪主体需要配置类型错误";
            }
        }
        return null;
    }

    /**
     * 获取当前租户的所有商务合同
     *
     * @return
     */
    public List<BusinessContractResult> businessContract(Integer tenantId) {
        List<Long> customerIds = getCustomerIds(tenantId);
        if (CollUtil.isEmpty(customerIds)) {
            return Collections.emptyList();
        }

        CustomerContractParam param = new CustomerContractParam();
        param.setTenant(EnumXhTenant.XUNHOU).setQueryDto(new CustomerContractParam.QueryDto().setCustomerIds(customerIds).setPageSize(500000));
        XhRpcParam xhRpcParam = new XhRpcParam();
        xhRpcParam.setRequest(param)
                .setServiceProject(EnumProject.USERXH)
                .setUri("ICustomerContractService/list");
        XhR<XhTotal<CustomerContractResult>> xhTotal = xhRpcComponent.sendForTotal(xhRpcParam, CustomerContractResult.class);

        List<BusinessContractResult> resultList = Lists.newArrayList();
        for (CustomerContractResult customerContractResult : xhTotal.getData().getList()) {
            resultList.add(SalaryConvert.result2Result(customerContractResult));
        }
        return resultList;
    }

    /**
     * 获取当前商务合同下所有项目
     *
     * @return
     */
    public List<ContractProjectResult> contractProject(Long businessContractId) {
        CrmServiceProto.ProjectDetailPageListBeResponse projectDetailPageListBeResponse = crmServiceBlockingStub.findProjectDetailPageList(CrmServiceProto.ProjectDetailPageListBeRequest.newBuilder()
                .setPaged(false)
                .setCustomerContractId(businessContractId)
                .build());
        List<ContractProjectResult> resultList = Lists.newArrayList();
        for (CrmServiceProto.ProjectBeResponse response : projectDetailPageListBeResponse.getProjectListBeResponseList()) {
            if (Lists.newArrayList(
                    CrmServiceProto.EnumBusinessType.LABOR_OUTSOURCING,
                    CrmServiceProto.EnumBusinessType.TRANSFER_OUTSOURCING
            ).contains(response.getProjectBasicInformation().getBusinessType())) {
                resultList.add(SalaryConvert.response2Result(response));
            }
        }
        return resultList;
    }

    public List<SubjectInfoResult> payerSubject() {
        SubjectServiceProto.SubjectBeResponses subjectBeResponses = subjectServiceBlockingStub.findSubjectPageList(SubjectServiceProto.QuerySubjectBeRequest.newBuilder()
                .setScenesAnd("6")
                .build());
        List<SubjectInfoResult> resultList = Lists.newArrayList();
        for (SubjectServiceProto.SubjectDetailBeResponse response : subjectBeResponses.getDataList()) {
            resultList.add(SalaryConvert.response2Result(response));
        }
        return resultList;
    }

    /**
     * 资金撤回
     *
     * @param detailIds
     */
    public void fundBack(Collection<Long> detailIds) {
        salaryServerBlockingStub.fundBack(SalaryServerProto.FundBackRequest.newBuilder()
                .addAllDetailIds(detailIds)
                .build());
    }

    /**
     * 断点重试
     *
     * @param batchId
     */
    public void breakpointRetry(Long batchId) {
        salaryServerBlockingStub.breakpointRetry(SalaryServerProto.BreakpointRetryRequest.newBuilder()
                .setBatchId(batchId)
                .build());
    }

    /**
     * 下载回执单
     *
     * @param param
     * @return
     */
    public ZipFilesResult downloadAck(SalaryDownloadAckParam param) {
        ZipFilesResult result = new ZipFilesResult();
        AssetXhServerProto.ReceiptListResponse receiptListResponse = assetXhServerBlockingStub.queryReceipt(AssetXhServerProto.QueryReceiptRequest.newBuilder().addAllDetailNos(param.getWithdrawalNos()).build());
        if (CollectionUtils.isNotEmpty(receiptListResponse.getReceiptListList())) {
            return XhFileUtils.zipUrlFiles(receiptListResponse.getReceiptListList().stream().map(AssetXhServerProto.ReceiptResponse::getFileId).collect(Collectors.toSet()), DateUtil.date().toString(DatePattern.PURE_DATETIME_PATTERN) + ".zip");
        } else {
            throw new SystemRuntimeException("回单生成中，请稍后再试吧");
        }
    }


    public JsonListResponse<SubjectFlowInfoResult> subjectFlow(SubjectFlowPageParam param) {
        JsonListResponse<SubjectFlowInfoResult> jsonListResponse = JsonListResponse.success();
        SalaryServerProto.TenantAccountPageRequest.Builder builder = SalaryConvert.param2Request(param);
        List<SalaryServerProto.EnumCustomerSubAccountType> customerSubAccountTypes = Lists.newArrayList(
                SalaryServerProto.EnumCustomerSubAccountType.SA_LABOR_PAYROLL,
                SalaryServerProto.EnumCustomerSubAccountType.SA_WALLET_PAY_DEDICATED
        );
        builder.addAllCustomerSubAccountTypes(customerSubAccountTypes);
        SalaryServerProto.TenantAccountPageResponse responsePage = salaryServerBlockingStub.findTenantAccount(builder.build());
        List<SalaryServerProto.TenantAccountResponse> responseList = responsePage.getDataList();
        List<SubjectFlowInfoResult> subjectFlowInfoResultList = Lists.newArrayList();
        Set<Long> customerIds = new HashSet<>();
        for (SalaryServerProto.TenantAccountResponse response : responseList) {
            subjectFlowInfoResultList.add(SalaryConvert.response2Result(response));
            customerIds.add(response.getCustomerId());
        }
        Map<Long, String> idMapperName = commonService.customerName(customerIds);
        for (SubjectFlowInfoResult result : subjectFlowInfoResultList) {
            result.setCustomerName(idMapperName.get(result.getCustomerId()));
        }
        jsonListResponse.setData(subjectFlowInfoResultList);
        jsonListResponse.setTotal(responsePage.getTotal());
        return jsonListResponse;
    }

    public SubAccountBalanceResult subAccountBalance(Long subAccountId) {
        AssetXhServerProto.ThirdPartyAccountsResponse balanceResponse = assetXhServerBlockingStub.queryThirdPartyAccountBalance(AssetXhServerProto.ThirdPartyAccountsRequest.newBuilder()
                .setOperator(XBB_USER_CONTEXT.get().getUserId())
                .setTransferWay(AssetXhServerProto.EnumTransferWay.WAY_CM_BANK)
                .addThirdPartyAccounts(AssetXhServerProto.ThirdPartyAccountRequest.newBuilder()
                        .setSubAccountId(subAccountId)
                        .build())
                .build());
        AssetXhServerProto.ThirdPartyAccountResponse thirdPartyAccountResponse = CollUtil.getFirst(balanceResponse.getThirdPartyAccountResponsesList());
        SubAccountBalanceResult result = new SubAccountBalanceResult();
        if (thirdPartyAccountResponse != null) {
            result.setBalance(thirdPartyAccountResponse.getBalance());
            result.setUpdateTime(DateUtil.date().toString(DatePattern.NORM_DATETIME_PATTERN));
        } else {
            result.setBalance("-");
            result.setUpdateTime(DateUtil.date().toString(DatePattern.NORM_DATETIME_PATTERN));
        }
        return result;
    }

    public JsonListResponse<SubAccountFlowResult> subAccountFlow(SubAccountFlowPageParam param) {
        JsonListResponse<SubAccountFlowResult> result = JsonListResponse.success();
        AssetXhServerProto.SubAccountFlowPageListResponse pageListResponse = assetXhServerBlockingStub.querySubAccountPageFlow(SalaryConvert.param2Request(param).build());
        List<SubAccountFlowResult> flowResultList = Lists.newArrayList();
        Map<Integer, Set<Long>> sourceSysTypeMap = Maps.newHashMap();
        for (AssetXhServerProto.SubAccountFlowInfoResponse flowInfoResponse : pageListResponse.getDataList()) {
            flowResultList.add(SalaryConvert.response2Result(flowInfoResponse));
            Set<Long> userSet = sourceSysTypeMap.computeIfAbsent(flowInfoResponse.getSourceSysType().getNumber(), k -> Sets.newHashSet());
            if (flowInfoResponse.getOperator() > 0) {
                userSet.add(flowInfoResponse.getOperator());
            }
        }
        Map<Integer, Map<Long, String>> sysUserIdForName = Maps.newHashMap();
        for (Integer sourceSysType : sourceSysTypeMap.keySet()) {
            AssetXhServerProto.EnumSystemPayType enumSystemPayType = AssetXhServerProto.EnumSystemPayType.forNumber(sourceSysType);
            Set<Long> userIds = sourceSysTypeMap.get(sourceSysType);
            switch (enumSystemPayType) {
                case SP_XBB_WITHDRAWAL:
                case SP_XCY_XBB_WITHDRAWAL:
                    sysUserIdForName.put(sourceSysType, commonService.userXbbCid(userIds));
                    break;
                case SP_XCY_FUND_BACK:
                case SP_XCY_WITHOUT_CARD_PAY:
                case SP_XCY_DF_FUND_DISPATCHING:
                    sysUserIdForName.put(sourceSysType, commonService.userSaas(userIds));
                    break;
                case SP_SETTLEMENT_WITHDRAWAL:
                case SP_SETTLEMENT_MERCHANT:
                case SP_SETTLEMENT_SALARY:
                case SP_WALLET_TRANSFER:
                    sysUserIdForName.put(sourceSysType, commonService.userXh(userIds));
                    break;
                case SP_XBB_SALARY:
                    //客户id
                    sysUserIdForName.put(sourceSysType, commonService.customerName(userIds));
                    break;
                default:
                    break;
            }
        }
        for (SubAccountFlowResult flowResult : flowResultList) {
            Map<Long, String> userNameMap = sysUserIdForName.get(flowResult.getSourceSysType());
            String name = userNameMap.get(flowResult.getOperator());
            flowResult.setOperatorName(name);
        }
        result.setData(flowResultList);
        result.setTotal(pageListResponse.getTotal());
        return result;
    }

    public List<OperationSalaryBatchExportData> getOperationSalaryBatchData(SalaryBatchPageParam param) {

        log.info("运营平台发薪批次导出参数" + param);
        JsonListResponse<SalaryBatchResult> response = findSalaryBatchPageList(param, true);
        if (CollectionUtils.isEmpty(response.getData())) {
            return Collections.emptyList();
        }
        List<OperationSalaryBatchExportData> dataList = new ArrayList<>();
        response.getData().forEach(salaryBatchResult -> {
            OperationSalaryBatchExportData operationSalaryBatchExportData = new OperationSalaryBatchExportData();

            BeanUtils.copyProperties(salaryBatchResult, operationSalaryBatchExportData);
            //set备注 为失败原因
            StringBuilder stringBuilderRemark = new StringBuilder();
            if (salaryBatchResult.getStatus() == SalaryServerProto.EnumSalaryDetailStatus.PAY_FAIL.getNumber()) {
                if (MapUtils.isNotEmpty(salaryBatchResult.getDetailFailureReasonMap())) {
                    stringBuilderRemark.append("发薪失败原因：");
                    for (Map.Entry<String, Integer> entry : salaryBatchResult.getDetailFailureReasonMap().entrySet()) {
                        stringBuilderRemark.append(entry.getKey() + "(" + entry.getValue() + ")");
                    }
                }
            }
            if (salaryBatchResult.getDeductionStatus() == EnumDispatchStatusCovertMsg.FAILURE.getCode()) {
                stringBuilderRemark.append("扣费失败原因：" + salaryBatchResult.getDeductionFailureReason());
            }
            operationSalaryBatchExportData.setRemark(stringBuilderRemark.toString());
            dataList.add(operationSalaryBatchExportData);
        });
        log.info("运营平台发薪批次导出 ===>查询出的数据" + XbbJsonUtil.toJsonString(dataList));
        return dataList;
    }
    public List<OperationSalaryDetailExportData> getOperationSalaryDetailData(SalaryDetailPageParam param) {

        log.info("运营平台发薪明细导出参数" + param);
        JsonListResponse<SalaryDetailResult> response = findSalaryDetailPageList(param, true);
        if (CollectionUtils.isEmpty(response.getData())) {
            return Collections.emptyList();
        }
        List<OperationSalaryDetailExportData> dataList = new ArrayList<>();
        response.getData().forEach(salaryDetailResult -> {
            OperationSalaryDetailExportData operationSalaryDetailExportData = new OperationSalaryDetailExportData();
            BeanUtils.copyProperties(salaryDetailResult, operationSalaryDetailExportData);
            dataList.add(operationSalaryDetailExportData);
        });
        log.info("运营平台发薪明细导出 ===>查询出的数据" + XbbJsonUtil.toJsonString(dataList));
        return dataList;
    }

    @SneakyThrows
    public JsonResponse<String> operationExportDetail(SalaryDetailPageParam param) {
        String taskCode = RedisConst.EXP_OPERATION_SALARY_DETAIL + SnowflakeIdGenerator.getId();
        log.info("运营平台发薪明细导出,taskCode=" + taskCode);
        log.info("运营平台发薪明细导出查询条件,taskCode=" + param);
        AddExportTaskParam addExportTaskParam = AddExportTaskParam.newBuilder().tenantId(XBB_USER_CONTEXT.tenantId()).taskCode(taskCode).params(XbbJsonUtil.toJsonBytes(param)).creator(XBB_USER_CONTEXT.get().getUserId()).fileName("发薪明细").build();
        TemplateTaskServiceProto.TaskBeRequest.Builder builder = TemplateTaskServiceProto.TaskBeRequest.newBuilder().setTenantId(XBB_USER_CONTEXT.tenantId()).setClientId(this.clientId).setTemplateId(OperationSalaryDetailExportService.class.getSimpleName()).setParams(ByteString.copyFrom(addExportTaskParam.getParams())).setTaskCode(taskCode).setTaskType(TemplateTaskServiceProto.TaskType.EXPORT).addAllField((Iterable) Opt.ofNullable(addExportTaskParam.getFields()).orElseGet(Collections::emptyList)).setFileName(StrUtil.nullToEmpty(addExportTaskParam.getFileName())).setCreator(addExportTaskParam.getCreator()).setStatus(TemplateTaskServiceProto.TaskStatus.CREATED);
        templateTaskServiceBlockingStub.addTask(builder.build());
        //salaryDetailExportService.addExportTask(addExportTaskParam);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            TemplateTaskServiceProto.TaskBeResponses responses = templateTaskServiceBlockingStub.findTasks(TemplateTaskServiceProto.QueryTaskBeRequest.newBuilder()
                    .setPage(0)
                    .setPageSize(1)
                    .setTaskCode(taskCode)
                    .build());
            TemplateTaskServiceProto.TaskBeResponse response = responses.getDataList().get(0);
            if (TemplateTaskServiceProto.TaskStatus.FAILED == response.getStatus()) {
                log.info("运营平台发薪明细导出失败,taskCode=" + taskCode);
                return JsonResponse.failed(XbbWebStatus.System_Error);
            }
            if (TemplateTaskServiceProto.TaskStatus.COMPLETED == response.getStatus()) {
                return JsonResponse.success(response.getObjectKey());
            }
        }
        return JsonResponse.failed(XbbWebStatus.System_Error);

    }

    @SneakyThrows
    public JsonResponse<String> operationExportBatch(SalaryBatchPageParam param) {

        String taskCode = RedisConst.EXP_OPERATION_SALARY_BATCH + SnowflakeIdGenerator.getId();
        log.info("运营平台发薪批次导出,taskCode=" + taskCode);
        log.info("运营平台发薪批次导出查询条件,taskCode=" + param);
        AddExportTaskParam addExportTaskParam = AddExportTaskParam.newBuilder().tenantId(XBB_USER_CONTEXT.tenantId()).taskCode(taskCode).params(XbbJsonUtil.toJsonBytes(param)).creator(XBB_USER_CONTEXT.get().getUserId()).fileName("发薪批次").build();
        TemplateTaskServiceProto.TaskBeRequest.Builder builder = TemplateTaskServiceProto.TaskBeRequest.newBuilder().setTenantId(XBB_USER_CONTEXT.tenantId()).setClientId(this.clientId).setTemplateId(OperationSalaryBatchExportService.class.getSimpleName()).setParams(ByteString.copyFrom(addExportTaskParam.getParams())).setTaskCode(taskCode).setTaskType(TemplateTaskServiceProto.TaskType.EXPORT).addAllField((Iterable) Opt.ofNullable(addExportTaskParam.getFields()).orElseGet(Collections::emptyList)).setFileName(StrUtil.nullToEmpty(addExportTaskParam.getFileName())).setCreator(addExportTaskParam.getCreator()).setStatus(TemplateTaskServiceProto.TaskStatus.CREATED);
        templateTaskServiceBlockingStub.addTask(builder.build());
        //salaryDetailExportService.addExportTask(addExportTaskParam);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            TemplateTaskServiceProto.TaskBeResponses responses = templateTaskServiceBlockingStub.findTasks(TemplateTaskServiceProto.QueryTaskBeRequest.newBuilder()
                    .setPage(0)
                    .setPageSize(1)
                    .setTaskCode(taskCode)
                    .build());
            TemplateTaskServiceProto.TaskBeResponse response = responses.getDataList().get(0);
            if (TemplateTaskServiceProto.TaskStatus.FAILED == response.getStatus()) {
                log.info("运营平台发薪批次导出失败,taskCode=" + taskCode);
                return JsonResponse.failed(XbbWebStatus.System_Error);
            }
            if (TemplateTaskServiceProto.TaskStatus.COMPLETED == response.getStatus()) {
                return JsonResponse.success(response.getObjectKey());
            }
        }
        return JsonResponse.failed(XbbWebStatus.System_Error);
    }

    public JsonResponse<List<TextValueResult>> subjectSuggest(String queryKeyword) {
        SubjectServiceProto.QuerySubjectBeRequest.Builder builder = SubjectServiceProto.QuerySubjectBeRequest.newBuilder()
                .setPaged(false)
                .setScenesAnd("6")
                .setSubjectName(CharSequenceUtil.isBlank(queryKeyword) ? "" : queryKeyword);
        SubjectServiceProto.SubjectBeResponses subjectBeResponses = subjectServiceBlockingStub.findSubjectPageList(builder.build());
        List<TextValueResult> textValueResults = new ArrayList<>();
        for (SubjectServiceProto.SubjectDetailBeResponse responses : subjectBeResponses.getDataList()) {
            TextValueResult textValueResult = new TextValueResult();
            textValueResult.setValue(responses.getBankCardNum());
            textValueResult.setText(responses.getSubjectName());
            textValueResult.setId(responses.getSubjectId());
            textValueResults.add(textValueResult);
        }
        return JsonResponse.success(textValueResults);
    }

    public JsonResponse<List<TextValueResult>> subAccountSuggest(Long subjectId) {
        AssetXhServerProto.SubAccountResponses subAccountResponses = assetXhServerBlockingStub.querySubAccount(AssetXhServerProto.SubAccountQueryRequest.newBuilder()
                .setPageable(false)
                .addSubjectInfoIds(subjectId)
                .build());

        SubjectServiceProto.SubjectDetailBeResponse subjectDetailBeResponse = subjectServiceBlockingStub.getSubjectObjectById(SubjectServiceProto.IdBeRequest.newBuilder().setId(subjectId).build());
        List<TextValueResult> textValueResults = new ArrayList<>();
        for (AssetXhServerProto.SubAccountResponse responses : subAccountResponses.getSubAccountInfosList()) {
            TextValueResult textValueResult = new TextValueResult();
            textValueResult.setValue(responses.getSubAccountNo());
            textValueResult.setText(subjectDetailBeResponse.getBankCardNum() + responses.getSubAccountNo());
            textValueResult.setId(responses.getSubAccountId());
            textValueResults.add(textValueResult);
        }
        return JsonResponse.success(textValueResults);
    }
}
