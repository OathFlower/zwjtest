package cn.xunhou.web.xbbcloud.product.sign.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.xunhou.cloud.constant.utils.AreaUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;
import cn.xunhou.cloud.core.snow.SnowflakeIdGenerator;
import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.cloud.framework.util.XbbProtoJsonUtil;
import cn.xunhou.grpc.proto.crm.CrmServiceGrpc;
import cn.xunhou.grpc.proto.crm.CrmServiceProto;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.subject.SubjectServiceGrpc;
import cn.xunhou.grpc.proto.subject.SubjectServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.SignServerGrpc;
import cn.xunhou.grpc.proto.xbbcloud.SignServerProto;
import cn.xunhou.web.xbbcloud.common.constants.CommonConst;
import cn.xunhou.web.xbbcloud.config.xhrpc.XhRpcComponent;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumProject;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumXhTenant;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhR;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhRpcParam;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhTotal;
import cn.xunhou.web.xbbcloud.product.manage.result.CustomerContractResult;
import cn.xunhou.web.xbbcloud.product.salary.enums.EnumTemplateStatus;
import cn.xunhou.web.xbbcloud.product.salary.service.SalaryService;
import cn.xunhou.web.xbbcloud.product.sign.convert.SignConvert;
import cn.xunhou.web.xbbcloud.product.sign.enums.EnumContractTemplateType;
import cn.xunhou.web.xbbcloud.product.sign.enums.ReadingStatus;
import cn.xunhou.web.xbbcloud.product.sign.param.*;
import cn.xunhou.web.xbbcloud.product.sign.result.*;
import cn.xunhou.web.xbbcloud.util.RedisUtil;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.grpc.Status;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SignService {
    @Resource
    private XhRpcComponent xhRpcComponent;

    @Resource
    private SalaryService salaryService;

    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();
    @GrpcClient("ins-xbbcloud-platform")
    private SignServerGrpc.SignServerBlockingStub signServerBlockingStub;
    @GrpcClient("ins-xhcrm-platform")
    private CrmServiceGrpc.CrmServiceBlockingStub crmServiceBlockingStub;
    @GrpcClient("ins-xhportal-platform")
    private static SubjectServiceGrpc.SubjectServiceBlockingStub subjectServiceBlockingStub;


    @GrpcClient("ins-xhportal-platform")
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;

    public JsonListResponse<ContractResult> contractList(QueryContractListParam param) {
        List<ContractResult> contractResultList = new ArrayList<>();
        SignServerProto.ContractListRequest.Builder builder = SignServerProto.ContractListRequest.newBuilder();
        builder.addAllStatusList(param.getStatusList());
        builder.setSourceBusinessId(param.getSourceBusinessId());
        builder.setPageSize(param.getPageSize());
        builder.setCurPage(param.getCurPage());
        SignServerProto.ContractPageListResponse contractPageListResponse = signServerBlockingStub.contractList(builder.build());

        for (SignServerProto.ContractListResponse contractListResponse : contractPageListResponse.getDataList()) {
            contractResultList.add(SignConvert.response2Result(contractListResponse));
        }

        return JsonListResponse.success(contractResultList, contractPageListResponse.getTotal());

    }

    public JsonResponse savePositionQrcode(SavePositionQrcodeParam param) {
        SignServerProto.PositionQrcodeSaveRequest.Builder builder = SignServerProto.PositionQrcodeSaveRequest.newBuilder();
        try {
            builder = SignConvert.param2Request(param);
        } catch (Exception e) {
            log.info("因签约云项目配置更改，该二维码匹配不到原有项目");
            throw new SystemRuntimeException("未找到对应项目");
        }

        builder.setOperatorId(XBB_USER_CONTEXT.get().getUserId());
        builder.setTenantId(XBB_USER_CONTEXT.tenantId());

        //岗位二维码编辑
        if (param.getId() != null) {

            signServerBlockingStub.saveOrUpdateQrcode(builder.build());
            SignServerProto.ContractListRequest.Builder contractListBuilder = SignServerProto.ContractListRequest.newBuilder();
            contractListBuilder.setStatus(SignServerProto.EnumContractStatus.WAIT_SIGN_VALUE);
            contractListBuilder.setSource(SignServerProto.EnumContractSource.LABOR_SALARY_VALUE);
            contractListBuilder.setSourceBusinessId(param.getId());
            //查全部
            contractListBuilder.setPageSize(0);
            SignServerProto.ContractPageListResponse contractPageListResponse = signServerBlockingStub.contractList(contractListBuilder.build());
            //需要处理的红点的map
            Map<String, List<String>> idCardAndContractMap = new HashMap<>();

            if (CollectionUtils.isNotEmpty(contractPageListResponse.getDataList())) {
                List<SignServerProto.UpdateContractRequest> updateContractRequestList = new ArrayList<>();
                for (SignServerProto.ContractListResponse contractListResponse :
                        contractPageListResponse.getDataList()) {
                    SignServerProto.UpdateContractRequest.Builder updateContractRequestBuild = SignServerProto.UpdateContractRequest.newBuilder();
                    updateContractRequestBuild.setId(contractListResponse.getId());
                    updateContractRequestBuild.setDeletedFlag(1);
                    updateContractRequestList.add(updateContractRequestBuild.build());
                    List<String> contractNoList = idCardAndContractMap.get(contractListResponse.getIdCardNo());
                    if (CollectionUtils.isEmpty(contractNoList)) {
                        contractNoList = new ArrayList<>();
                        idCardAndContractMap.put(contractListResponse.getIdCardNo(), contractNoList);
                    }
                    contractNoList.add(contractListResponse.getContractNo());
                }
                //先逻辑删除原有合同
                SignServerProto.BatchUpdateContractRequest.Builder batchUpdateContractRequestBuild = SignServerProto.BatchUpdateContractRequest.newBuilder();
                signServerBlockingStub.batchUpdateContract(batchUpdateContractRequestBuild.addAllUpdateContractRequest(updateContractRequestList).build());
                try {
                    updateReadingStatus(idCardAndContractMap);
                } catch (Exception e) {
                    log.info("删除合同时更新红点异常");
                }


                //编辑才重新去生成合同
                if (param.getDeleteflag() == 0) {
                    Set<String> idCardNoList = contractPageListResponse.getDataList().stream().map(SignServerProto.ContractListResponse::getIdCardNo).distinct().collect(Collectors.toSet());
                    //新插入合同数据
                    generateContractForLabor(param, idCardNoList);

                }

            }

        } else {
            signServerBlockingStub.saveOrUpdateQrcode(builder.build());
        }
        return JsonResponse.success();
    }

    private void updateReadingStatus(Map<String, List<String>> idCardAndContractMap) {
        //删除合同后 删除红点
        Map<String, List<UserXhCResult>> idCardAndUserXh = getIdCardAndUserXh(idCardAndContractMap.keySet());

        for (String idCardNo :
                idCardAndContractMap.keySet()) {


            List<UserXhCResult> userXhcResults = idCardAndUserXh.get(idCardNo);
            for (UserXhCResult userXhCResult :
                    userXhcResults) {

                for (String contractNo :
                        idCardAndContractMap.get(idCardNo)) {
                    ReadingStatusParam readingStatusParam = new ReadingStatusParam();
                    readingStatusParam.setSourceId(contractNo);

                    readingStatusParam.setUpdateReadingState(ReadingStatus.ReadingState.READ);
                    readingStatusParam.setSourceType(ReadingStatus.SourceType.TYPE_3);
                    readingStatusParam.setOwnerId(userXhCResult.getId());
                    XhRpcParam xhRpcParamAddRead = new XhRpcParam();
                    Map<String, Object> updateReadStatusParam = new HashMap<>();
                    updateReadStatusParam.put("updateReadingStatus", readingStatusParam);


                    xhRpcParamAddRead.setRequest(updateReadStatusParam)
                            .setServiceProject(EnumProject.XBB)
                            .setXhTenant(EnumXhTenant.XUNHOU)
                            .setUri("ICommonReadingStatusService/updates");
                    xhRpcComponent.sendForList(xhRpcParamAddRead, Integer.class);
                }

            }
        }
    }


    public void generateContractForLabor(SavePositionQrcodeParam param, Collection<String> idCardNoList) {
        SignServerProto.BatchInsertContractRequest.Builder batchInsertContractBuild = SignServerProto.BatchInsertContractRequest.newBuilder();
        Map<String, List<String>> idCardAndContractMap = new HashMap<>();

        for (String idCardNo :
                idCardNoList) {
            List<String> contractNos = new ArrayList<>();
            idCardAndContractMap.put(idCardNo, contractNos);
            if (param != null) {
                if (CollectionUtils.isNotEmpty(param.getContractTemplateList())) {

                    for (TemplateParam templateParam :
                            param.getContractTemplateList()) {
                        SignServerProto.InsertContractRequest.Builder insertContractReqBuild = SignServerProto.InsertContractRequest.newBuilder();
                        insertContractReqBuild.setId(SnowflakeIdGenerator.getId());
                        String contractNo = RedisUtil.generateContractNo();
                        insertContractReqBuild.setContractNo(contractNo);
                        contractNos.add(contractNo);
                        insertContractReqBuild.setIdCardNo(idCardNo);
                        insertContractReqBuild.setTemplateId(templateParam.getTemplateId());
                        insertContractReqBuild.setSubjectId(param.getSubjectId());
                        insertContractReqBuild.setTenantId(XBB_USER_CONTEXT.tenantId());
                        insertContractReqBuild.setType(templateParam.getType());
                        insertContractReqBuild.setStatus(SignServerProto.EnumContractStatus.WAIT_SIGN_VALUE);
                        insertContractReqBuild.setTemplateJson(param.getTemplateJson());
                        insertContractReqBuild.setSource(SignServerProto.EnumContractSource.LABOR_SALARY_VALUE);
                        insertContractReqBuild.setSourceBusinessId(param.getId());
                        batchInsertContractBuild.addInsertContractRequest(insertContractReqBuild);
                    }

                }

            }
        }
        SignServerProto.BatchInsertContractRequest request = batchInsertContractBuild.build();
        log.info("要插入的合同list" + XbbProtoJsonUtil.toJsonString(request));
        signServerBlockingStub.batchInsertContract(request);
        try {
            saveReadingStatus(idCardNoList, idCardAndContractMap);
        } catch (Exception e) {
            log.info("插入合同的时候保存红点出错");
        }

    }

    private void saveReadingStatus(Collection<String> idCardNoList, Map<String, List<String>> idCardAndContractMap) {
        Map<String, List<UserXhCResult>> userXhResultMap = getIdCardAndUserXh(idCardNoList);


        List<ReadingStatusResult> readingStatusResults = new ArrayList<>();
        for (String idCardNo :
                idCardAndContractMap.keySet()) {
            List<UserXhCResult> userXhcResults = userXhResultMap.get(idCardNo);
            for (String contractNo :
                    idCardAndContractMap.get(idCardNo)) {

                for (UserXhCResult userXhCResult :
                        userXhcResults) {
                    //插入红点数据
                    ReadingStatusResult readingStatusDto = new ReadingStatusResult();
                    readingStatusDto.setTel(userXhCResult.getTel());
                    readingStatusDto.setSourceId(contractNo);
                    readingStatusDto.setSourceType(ReadingStatus.SourceType.TYPE_3);
                    readingStatusDto.setReadingState(ReadingStatus.ReadingState.UNREAD);
                    readingStatusDto.setOwnerId(userXhCResult.getId());
                    readingStatusDto.setCreateId(userXhCResult.getId());
                    readingStatusResults.add(readingStatusDto);
                }


            }
        }

        XhRpcParam xhRpcParamAddRead = new XhRpcParam();
        Map<String, Object> addReadStatusParam = new HashMap<>();
        addReadStatusParam.put("addReadingStatusList", readingStatusResults);


        xhRpcParamAddRead.setRequest(addReadStatusParam)
                .setServiceProject(EnumProject.XBB)
                .setXhTenant(EnumXhTenant.XUNHOU)
                .setUri("ICommonReadingStatusService/adds");
        xhRpcComponent.sendForList(xhRpcParamAddRead, Long.class);
    }

    public Map<String, List<UserXhCResult>> getIdCardAndUserXh(Collection<String> idCardNoList) {
        Map<String, Object> getUserXhC = new HashMap<>();
        getUserXhC.put("idCardNos", idCardNoList);
        getUserXhC.put("_realNameCertStatusList", Collections.singleton("CERTIFY_PASS"));
        getUserXhC.put("withIdCardInfo", true);
        getUserXhC.put("withBankCardInfo", false);
        XhRpcParam xhRpcParam = new XhRpcParam();
        xhRpcParam.setRequest(getUserXhC)
                .setServiceProject(EnumProject.USERXH)
                .setXhTenant(EnumXhTenant.XUNHOU)
                .setUri("IUserXhCService/getUserXhCDtosByIdCardNos");
        XhR<List<UserXhCResult>> userXhcXhR = xhRpcComponent.sendForList(xhRpcParam, UserXhCResult.class);
        if (CollectionUtils.isEmpty(userXhcXhR.getData())) {
            throw new SystemRuntimeException("找不到对应的身份信息");
        }
        return userXhcXhR.getData().parallelStream().collect(Collectors.groupingBy(UserXhCResult::getIdCardNo));
    }


    /**
     * 查询基本信息
     *
     * @return 基本信息
     */
    public SignInfoResult info(Long tenantId) {
        SignServerProto.SignInfoResponse response = signServerBlockingStub.signInfo(SignServerProto.SignInfoIdRequest.newBuilder()
                .setTenantId(tenantId == null ? ((long) XBB_USER_CONTEXT.get().getTenantId()) : tenantId)
                .build());
        HrmServiceProto.findXgkRoleByTenantBeResponses roleByTenantBeResponses = hrmServiceBlockingStub.findXgkRoleByTenant(HrmServiceProto.SwitchRoleRequest.newBuilder()
                .setRoleName("HRM-签约云角色")
                .setTenantId(tenantId == null ? XBB_USER_CONTEXT.get().getTenantId() : tenantId.intValue())
                .build());
        return SignConvert.response2Result(response).setIsUse(roleByTenantBeResponses.getType());
    }

    /**
     * 查询基本信息
     *
     * @param param 入参
     * @return 基本信息
     */
    public SignInfoResult saveInfo(SignInfoParam param) {
        if (CollUtil.isEmpty(param.getProjectIds())) {
            throw new SystemRuntimeException("项目不能为空！");
        }
        List<Long> customerIds = salaryService.getCustomerIds(param.getTenantId().intValue());
        Map<String, Object> customerContractParam = new HashMap<>();
        customerContractParam.put("contractId", param.getBusinessContractId());
        customerContractParam.put("tenant", EnumXhTenant.XUNHOU);
        XhRpcParam xhRpcParam = new XhRpcParam();
        xhRpcParam.setRequest(customerContractParam)
                .setServiceProject(EnumProject.USERXH)
                .setUri("ICustomerContractService/getCustomerContractDtoById");
        XhR<CustomerContractResult> resultXhR = xhRpcComponent.send(xhRpcParam, CustomerContractResult.class);
        if (resultXhR.getData() == null) {
            throw new SystemRuntimeException("当前商务合同未找到！");
        }
        Long customerId = resultXhR.getData().getCustomerId();
        if (!customerIds.contains(customerId)) {
            throw new SystemRuntimeException("租户下客户商业合同数据异常！");
        }
        List<ProjectPositionResult> positionList = getPositionList(param.getProjectIds());
        signServerBlockingStub.saveSignInfo(
                SignConvert.param2Request(param, customerId, XBB_USER_CONTEXT.get().getUserId())
                        .addAllPositionIds(positionList.stream().map(ProjectPositionResult::getId).collect(Collectors.toSet()))
                        .build());
        return info(param.getTenantId());
    }

    /**
     * 通过项目id查岗位信息
     *
     * @param projectList
     * @return
     */
    private List<ProjectPositionResult> getPositionList(List<Long> projectList) {
        XhRpcParam xhRpcParam = new XhRpcParam();
        Map<String, Object> getProjectListParams = new HashMap<>();
        getProjectListParams.put("projectIds", projectList);
        getProjectListParams.put("tenant", "XUNHOU");
        xhRpcParam.setRequest(getProjectListParams)
                .setServiceProject(EnumProject.HROSTAFF)
                .setXhTenant(EnumXhTenant.XUNHOU)
                .setUri("IProjectPositionService/getProjectPositionDtosByProjectIds");
        XhR<List<ProjectPositionResult>> listXhR = xhRpcComponent.sendForList(xhRpcParam, ProjectPositionResult.class);
        return listXhR.getData();
    }

    public JsonResponse<List<PostAndProjectResult>> postSelectByHro() {
        List<PostAndProjectResult> resultList = new ArrayList<>();
        SignServerProto.SignInfoIdRequest.Builder signInfoIdRequestBuild = SignServerProto.SignInfoIdRequest.newBuilder();
        signInfoIdRequestBuild.setTenantId(XBB_USER_CONTEXT.tenantId());
        SignServerProto.SignInfoResponse signInfoResponse = signServerBlockingStub.signInfo(signInfoIdRequestBuild.build());
        List<Long> projectList = signInfoResponse.getProjectIdsList();
        //根据项目列表查出来  项目信息
        CrmServiceProto.ProjectDetailPageListBeRequest.Builder builder = CrmServiceProto.ProjectDetailPageListBeRequest.newBuilder();
        builder.addAllProjectIds(projectList);
        builder.setPaged(false);
        CrmServiceProto.ProjectDetailPageListBeResponse projectDetailPageList = crmServiceBlockingStub.findProjectDetailPageList(builder.build());
        //根据项目列表查出来岗位信息
        if (CollectionUtils.isEmpty(projectDetailPageList.getProjectListBeResponseList())) {
            log.info("未查询到对应项目列表");
            return JsonResponse.success(resultList);
        }
        //后续匹配循环岗位的项目时用
        Map<Long, CrmServiceProto.ProjectBeResponse> projectDtoMap = projectDetailPageList.getProjectListBeResponseList().stream().collect(Collectors.toMap(CrmServiceProto.ProjectBeResponse::getProjectId, Function.identity()));
        XhRpcParam xhRpcParam = new XhRpcParam();
        Map<String, Object> getProjectListParams = new HashMap<>();
        getProjectListParams.put("projectIds", projectList);
        getProjectListParams.put("tenant", "XUNHOU");


        xhRpcParam.setRequest(getProjectListParams)
                .setServiceProject(EnumProject.HROSTAFF)
                .setXhTenant(EnumXhTenant.XUNHOU)
                .setUri("IProjectPositionService/getProjectPositionDtosByProjectIds");

        XhR<List<ProjectPositionResult>> listXhR = xhRpcComponent.sendForList(xhRpcParam, ProjectPositionResult.class);

        List<ProjectPositionResult> projectPositionResultList = listXhR.getData();
        if (CollectionUtils.isEmpty(projectPositionResultList)) {
            log.info("未查询到对应岗位列表");
            return JsonResponse.success(resultList);
        }
        for (ProjectPositionResult projectPositionResult : projectPositionResultList) {
            PostAndProjectResult postAndProjectResult = new PostAndProjectResult();
            //赋值岗位相关信息
            BeanUtils.copyProperties(projectPositionResult, postAndProjectResult);
            postAndProjectResult.setPostId(projectPositionResult.getId());
            postAndProjectResult.setAreaCode(AreaUtil.getRegionByCode(projectPositionResult.getAreaCode()).getName());
            log.info("查出来的项目列表" + XbbProtoJsonUtil.toJsonString(projectDetailPageList));
            for (CrmServiceProto.ProjectBeResponse projectBeResponse :
                    projectDetailPageList.getProjectListBeResponseList()) {
                //循环匹配对应的项目 然后查主体
                if (projectBeResponse.getProjectId() == projectPositionResult.getProjectId()) {
                    //对应的项目的业务类型
                    postAndProjectResult.setBusinessType(projectBeResponse.getProjectBasicInformation().getBusinessType().getNumber());
                    //查询用户主体
                    Map<String, Object> getSubjectByIdParams = new HashMap<>();
                    getSubjectByIdParams.put("customerContractId", projectBeResponse.getCustomerContractIdsList().get(0));
                    XhRpcParam xhRpcParamGetSubjectById = new XhRpcParam();
                    xhRpcParamGetSubjectById.setRequest(getSubjectByIdParams)
                            .setServiceProject(EnumProject.HROSTAFF)
                            .setXhTenant(EnumXhTenant.XUNHOU)
                            .setUri("IContractSubjectService/getSubjectById");
                    XhR<CustomerContractSubjectResult> getSubjectByIdResult = xhRpcComponent.send(xhRpcParamGetSubjectById, CustomerContractSubjectResult.class);

                    CustomerContractSubjectResult customerContractSubjectResult = getSubjectByIdResult.getData();
                    if (customerContractSubjectResult != null) {
                        List<SubjectConfigurationResult> subjectConfigurationDtoList = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(customerContractSubjectResult.getSubjectConfigurationDtoList())) {
                            log.info("查出来的主体列表" + JSONArray.toJSONString(customerContractSubjectResult.getSubjectConfigurationDtoList()));

                            for (SubjectConfigurationResult subjectConfigurationDto :
                                    customerContractSubjectResult.getSubjectConfigurationDtoList()) {
                                SubjectConfigurationResult subjectConfigurationResult = new SubjectConfigurationResult();
                                SubjectServiceProto.SubjectDetailBeResponse subjectDetailBeResponse = subjectServiceBlockingStub
                                        .getSubjectObjectById(SubjectServiceProto.IdBeRequest.newBuilder().setId(subjectConfigurationDto.getSubjectId()).build());
                                subjectConfigurationResult.setSubjectId(subjectConfigurationDto.getSubjectId());
                                subjectConfigurationResult.setSocialInsuranceType(subjectConfigurationDto.getSocialInsuranceType());
                                subjectConfigurationResult.setSubjectName(subjectDetailBeResponse.getSubjectName());
                                subjectConfigurationDtoList.add(subjectConfigurationResult);
                            }
                            postAndProjectResult.setSubjectConfigurationDtoList(subjectConfigurationDtoList);
                        }
                    }

                    break;
                }
            }

            resultList.add(postAndProjectResult);
        }

        return JsonResponse.success(resultList);
    }

    public JsonListResponse<PositionQrcodeResult> positionQrcodes(@NonNull PositionQrcodePageParam param) {
        SignServerProto.QrcodeListQueryRequest.Builder request = SignServerProto.QrcodeListQueryRequest.newBuilder();


        List<PositionQrcodeResult> resultList = Lists.newArrayList();

        if (StringUtils.isNotBlank(param.getPositionName())) {
            List<String> positionNameList = new ArrayList<>();
            positionNameList.add(param.getPositionName());
            XhRpcParam xhRpcParam = new XhRpcParam();
            Map<String, Object> getProjectPositionDtosByPositionNamesParams = new HashMap<>();
            getProjectPositionDtosByPositionNamesParams.put("positionNames", positionNameList);
            getProjectPositionDtosByPositionNamesParams.put("tenant", "XUNHOU");


            xhRpcParam.setRequest(getProjectPositionDtosByPositionNamesParams)
                    .setServiceProject(EnumProject.HROSTAFF)
                    .setXhTenant(EnumXhTenant.XUNHOU)
                    .setUri("IProjectPositionService/getProjectPositionDtosByPositionNames");

            XhR<List<ProjectPositionResult>> listXhR = xhRpcComponent.sendForList(xhRpcParam, ProjectPositionResult.class);
            List<ProjectPositionResult> projectPositionResultList = listXhR.getData();
            if (CollectionUtils.isNotEmpty(projectPositionResultList)) {
                List<Long> positionIds = projectPositionResultList.stream().map(ProjectPositionResult::getId).collect(Collectors.toList());
                request.addAllHroPositionId(positionIds);
            } else {
                return JsonListResponse.success(resultList, 0);
            }
        }
        request.setCurPage(param.getCurPage());
        request.setPageSize(param.getPageSize());
        if (CharSequenceUtil.isNotBlank(param.getCreateDateStart())) {
            request.setCreateDateStart(param.getCreateDateStart());
        }
        if (CharSequenceUtil.isNotBlank(param.getCreateDateEnd())) {
            request.setCreateDateEnd(param.getCreateDateEnd());
        }
        request.setTenantId(XBB_USER_CONTEXT.tenantId());
        SignServerProto.PagePositionQrcodeResponse pagePositionQrcodeResponse = signServerBlockingStub.positionQrcodeList(request.build());

        if (CollectionUtils.isEmpty(pagePositionQrcodeResponse.getDataList())) {
            return JsonListResponse.success(resultList, pagePositionQrcodeResponse.getTotal());
        }
        //获取操作人
        Map<Long, HrmServiceProto.AccountDetailBeResponse> accountDetailBeResponseMap =
                accountDetailBeResponseMap(pagePositionQrcodeResponse.getDataList().stream().map(SignServerProto.PositionQrcodeResponse::getOperatorId).collect(Collectors.toList()));
        for (SignServerProto.PositionQrcodeResponse positionQrcodeResponse : pagePositionQrcodeResponse.getDataList()) {
            PositionQrcodeResult positionQrcodeResult = new PositionQrcodeResult();
            positionQrcodeResult.setId(positionQrcodeResponse.getId());
            positionQrcodeResult.setHroPositionId(positionQrcodeResponse.getHroPositionId());
            positionQrcodeResult.setCreatedAt(positionQrcodeResponse.getCreatedAt());
            positionQrcodeResult.setUpdatedAt(positionQrcodeResponse.getUpdatedAt());
            positionQrcodeResult.setRemark(positionQrcodeResponse.getRemark());
            if (StringUtils.isNotBlank(positionQrcodeResponse.getTemplateJson())) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(positionQrcodeResponse.getTemplateJson());
                    positionQrcodeResult.setPositionSalary(jsonObject.getBigDecimal("salary"));
                    positionQrcodeResult.setProbation(jsonObject.getInteger("probationPeriod"));
                } catch (Exception e) {
                    log.info("二维码转换json串出错" + positionQrcodeResponse.getId() + "串的值为" + positionQrcodeResponse.getTemplateJson());
                }
            }
            //根据岗位查出对应的项目
            Map<String, Object> getProjectParams = new HashMap<>();
            getProjectParams.put("positionId", positionQrcodeResponse.getHroPositionId());
            getProjectParams.put("tenant", "XUNHOU");
            XhRpcParam xhRpcParam = new XhRpcParam();
            xhRpcParam.setRequest(getProjectParams)
                    .setServiceProject(EnumProject.HROSTAFF)
                    .setXhTenant(EnumXhTenant.XUNHOU)
                    .setUri("IProjectPositionService/getProjectPositionDtoById");
            XhR<ProjectPositionResult> getProjectResult = xhRpcComponent.send(xhRpcParam, ProjectPositionResult.class);

            if (getProjectResult.getData() != null) {
                positionQrcodeResult.setPositionName(getProjectResult.getData().getName());
                positionQrcodeResult.setPositionAddr(AreaUtil.getRegionByCode(getProjectResult.getData().getAreaCode()).getName());
            }
            positionQrcodeResult.setOperatorName(accountDetailBeResponseMap.get(positionQrcodeResponse.getOperatorId()).getNickName());
            resultList.add(positionQrcodeResult);
        }

        return JsonListResponse.success(resultList, pagePositionQrcodeResponse.getTotal());
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

    public JsonResponse<PositionQrcodeResult> qrcodeDetail(Long id) {
        PositionQrcodeResult positionQrcodeResult = new PositionQrcodeResult();
        SignServerProto.PositionQrcodeResponse positionQrcodeResponse = signServerBlockingStub.positionQrcodeDetail(SignServerProto.QrcodeDetailQueryRequest.newBuilder().setId(id).build());
        positionQrcodeResult.setId(positionQrcodeResponse.getId());
        positionQrcodeResult.setContractTemplateType(positionQrcodeResponse.getContractTemplateType());
        positionQrcodeResult.setHroPositionId(positionQrcodeResponse.getHroPositionId());
        positionQrcodeResult.setSubjectId(positionQrcodeResponse.getSubjectId());
        positionQrcodeResult.setSubjectName(subjectServiceBlockingStub
                .getSubjectObjectById(SubjectServiceProto.IdBeRequest.newBuilder().setId(positionQrcodeResponse.getSubjectId()).build()).getSubjectName());
        positionQrcodeResult.setSocialInsurance(positionQrcodeResponse.getSocialInsurance());
        positionQrcodeResult.setExpireDate(positionQrcodeResponse.getExpireDate());
        positionQrcodeResult.setRemark(positionQrcodeResponse.getRemark());
        positionQrcodeResult.setTemplateJson(positionQrcodeResponse.getTemplateJson());
        List<TemplateResult> templateResultList = new ArrayList<>();
        for (SignServerProto.ContractTemplate contractTemplate :
                positionQrcodeResponse.getContractTemplateList()) {
            TemplateResult templateResult = new TemplateResult();
            templateResult.setType(contractTemplate.getType());
            templateResult.setContractTemplateId(contractTemplate.getTemplateId());
            templateResultList.add(templateResult);
        }
        positionQrcodeResult.setTemplateResultList(templateResultList);
        positionQrcodeResult.setUserTelList(positionQrcodeResponse.getTelList());
        return JsonResponse.success(positionQrcodeResult);
    }


    public JsonResponse<List<TextValueResult>> templateSuggest(Long customerId, Integer contractTemplateType, Integer templateType) {

        List<TextValueResult> result = new ArrayList<>();
        XhRpcParam xhRpcContractParam = new XhRpcParam();
        XhRpcParam xhRpcProtocolParam = new XhRpcParam();
        Map<String, Object> contractParams = new HashMap<>();
        Map<String, Object> protocolParams = new HashMap<>();
        if (contractTemplateType != null) {
            contractParams.put("contractTemplateType", EnumContractTemplateType.getEnum(contractTemplateType));
        }
        Map<String, Object> contractParamsMap = new HashMap<>();
        contractParams.put("pageSize", 1000);
        contractParams.put("curPage", 0);
        contractParams.put("customerId", customerId);
        contractParamsMap.put("queryConditionDto", contractParams);
        //构建合同模板查询条件
        xhRpcContractParam.setRequest(contractParamsMap)
                .setServiceProject(EnumProject.HROSTAFF)
                .setXhTenant(EnumXhTenant.XUNHOU)
                .setUri("IContractTemplateService/queryContractTemplate");
        //构建协议模板查询条件
        Map<String, Object> protocolParamsMap = new HashMap<>();
        protocolParams.put("pageSize", 1000);
        protocolParams.put("curPage", 0);
        protocolParams.put("customerId", customerId);
        protocolParamsMap.put("queryConditionDto", protocolParams);
        xhRpcProtocolParam.setRequest(protocolParamsMap)
                .setServiceProject(EnumProject.HROSTAFF)
                .setXhTenant(EnumXhTenant.XUNHOU)
                .setUri("IProtocolTemplateService/queryProtocolTemplate");


        if (SignServerProto.EnumTemplateType.CONTRACT_VALUE == templateType) {//只查询合同
            XhR<XhTotal<ContractTemplateResult>> r = xhRpcComponent.sendForTotal(xhRpcContractParam, ContractTemplateResult.class);
            if (null == r || CollectionUtils.isEmpty(r.getData().getList())) {
                return JsonResponse.success(result);
            }
            List<ContractTemplateResult> list = r.getData().getList();
            log.info(JSONObject.toJSONString(list));
            List<ContractTemplateResult> collect = list.stream().filter(t -> t.getCustomerIds().contains(customerId)).collect(Collectors.toList());
            list.removeAll(collect);
            list.addAll(0, collect);
            for (ContractTemplateResult contractTemplateResult : list) {
                if (contractTemplateResult.getStatus() == EnumTemplateStatus.DISABLED) {
                    continue;
                }
                TextValueResult textValueResult = new TextValueResult();
                textValueResult.setValue(contractTemplateResult.getId().toString());
                textValueResult.setText(contractTemplateResult.getName());
                result.add(textValueResult);
            }
        } else if (SignServerProto.EnumTemplateType.PROTOCOL_VALUE == templateType) {//只查询协议
            XhR<XhTotal<ProtocolTemplateResult>> r = xhRpcComponent.sendForTotal(xhRpcProtocolParam, ProtocolTemplateResult.class);
            if (null == r || CollectionUtils.isEmpty(r.getData().getList())) {
                return JsonResponse.success(result);
            }
            List<ProtocolTemplateResult> list = r.getData().getList();
            for (ProtocolTemplateResult protocolTemplateResult : list) {
                if (protocolTemplateResult.getStatus() == EnumTemplateStatus.DISABLED) {
                    continue;
                }
                TextValueResult textValueResult = new TextValueResult();
                textValueResult.setValue(protocolTemplateResult.getId().toString());
                textValueResult.setText(protocolTemplateResult.getName());
                result.add(textValueResult);
            }
        }
        return JsonResponse.success(result);
    }

    public void checkIsUse() {
        SignInfoResult result = info(null);
        if (result == null || result.getIsUse() == null || result.getIsUse() != CommonConst.ONE) {
            throw Status.INTERNAL.withDescription("当前商户签约云功能未启用").asRuntimeException();
        }
    }
}
