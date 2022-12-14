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
     * ??????????????????????????????
     *
     * @return
     */
    public SalaryMerchantInfoResult merchantInfo() {
        return merchantInfo(Long.valueOf(XBB_USER_CONTEXT.get().getTenantId()));
    }

    /**
     * ?????????
     * ??????????????????
     *
     * @return ????????????
     */
    public SalaryMerchantInfoResult merchantInfo(Long tenantId) {
        SalaryServerProto.MerchantInfoResponse merchantInfoResponse = salaryServerBlockingStub.queryMerchantInfo(SalaryServerProto.MerchantInfoRequest.newBuilder()
                .setTenantId(tenantId)
                .build());
        HrmServiceProto.findXgkRoleByTenantBeResponses roleByTenantBeResponses = hrmServiceBlockingStub.findXgkRoleByTenant(HrmServiceProto.SwitchRoleRequest.newBuilder()
                .setTenantId(tenantId.intValue())
                .setRoleName("HRM-???????????????")
                .build());
        return SalaryConvert.response2Result(merchantInfoResponse).setIsUse(roleByTenantBeResponses.getType());
    }

    /**
     * ?????????????????????????????????????????????
     */
    public void checkMerchantIsUse() {
        SalaryMerchantInfoResult result = merchantInfo();
        if (result == null || result.getIsUse() == null || result.getIsUse() != CommonConst.ONE) {
            throw Status.INTERNAL.withDescription("????????????????????????????????????").asRuntimeException();
        }
    }

    /**
     * ??????????????????
     *
     * @return ??????????????????
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
     * ??????????????? ????????????
     *
     * @param param ????????????
     * @return ??????????????????
     */
    public SalaryMerchantInfoResult saveMerchantInfo(SalaryMerchantInfoParam param) {
        Long payeeCustomerId = null, businessContractCustomerId = null;
        if (param.getTenantType() == SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE) {
            List<Long> customerIds = getCustomerIds(param.getTenantId().intValue());
            //???????????????
            AssetXhServerProto.SubAccountResponse subAccountResponse = CollUtil.getFirst(assetXhServerBlockingStub.querySubAccount(AssetXhServerProto.SubAccountQueryRequest.newBuilder()
                    .addSubAccountIds(param.getPayeeSubAccountId())
                    .setPageable(false)
                    .build()).getSubAccountInfosList());
            payeeCustomerId = subAccountResponse.getCustomerId();
            if (!customerIds.contains(payeeCustomerId)) {
                throw new SystemRuntimeException("???????????????????????????????????????");
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
                throw new SystemRuntimeException("??????????????????????????????");
            }
            businessContractCustomerId = resultXhR.getData().getCustomerId();
            if (!customerIds.contains(payeeCustomerId)) {
                throw new SystemRuntimeException("??????????????????????????????????????????");
            }

        }
        SalaryServerProto.SaveMerchantInfoRequest.Builder request = SalaryConvert.param2Request(param,
                payeeCustomerId, businessContractCustomerId,
                XBB_USER_CONTEXT.get().getUserId());
        return SalaryConvert.response2Result(salaryServerBlockingStub.saveMerchantInfo(request.build()));
    }

    /**
     * ??????????????????
     *
     * @param param ????????????
     * @return ??????????????????
     */
    public SalaryMerchantFlowResult operateMerchantFlow(SalaryOperateMerchantFlowParam param) {
        SalaryServerProto.OperateMerchantFlowRequest.Builder request = SalaryConvert.param2Request(param, XBB_USER_CONTEXT.get().getUserId());
        SalaryServerProto.MerchantFlowsResponse response = salaryServerBlockingStub.operateMerchantFlow(request.build());
        return SalaryConvert.response2Result(response);
    }

    /**
     * ????????????
     *
     * @param param ????????????
     * @return ????????????
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
     * ??????????????????
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
        //???????????????
        Map<Long, HrmServiceProto.AccountDetailBeResponse> accountDetailBeResponseMap =
                accountDetailBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryBatchBeResponse::getOperatorId).collect(Collectors.toList()));
        //???????????????????????????
        Map<Long, PortalServiceProto.TenantBeResponse> tenantBeResponseMap = tenantIdBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryBatchBeResponse::getTenantId).collect(Collectors.toList()));

        //????????????????????????
        Map<Long, SubjectServiceProto.SubjectDetailBeResponse> longSubjectDetailBeResponseMap = subjectDetailBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryBatchBeResponse::getSubjectId).distinct().collect(Collectors.toList()));
        //????????????????????????id?????????????????????
        SalaryServerProto.SalaryDetailConditionBeRequest.Builder salaryDetailRequestBuild = SalaryServerProto.SalaryDetailConditionBeRequest.newBuilder();
        salaryDetailRequestBuild.setPageSize(0);//?????????
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
        //??????????????????????????? ??????????????????id??????
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
        //???????????????
        Map<Long, HrmServiceProto.AccountDetailBeResponse> accountDetailBeResponseMap =
                accountDetailBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryDetailBeResponse::getOperatorId).collect(Collectors.toList()));

        //???????????????????????????
        Map<Long, PortalServiceProto.TenantBeResponse> tenantBeResponseMap = tenantIdBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryDetailBeResponse::getTenantId).collect(Collectors.toList()));
        //????????????????????????
        Map<Long, SubjectServiceProto.SubjectDetailBeResponse> longSubjectDetailBeResponseMap = subjectDetailBeResponseMap(response.getDataList().stream().map(SalaryServerProto.SalaryDetailBeResponse::getSubjectId).distinct().collect(Collectors.toList()));
        for (SalaryServerProto.SalaryDetailBeResponse salaryDetailBeResponse : response.getDataList()) {
            resultList.add(SalaryConvert.response2Result(isOperation, longSubjectDetailBeResponseMap.get(salaryDetailBeResponse.getSubjectId()), salaryDetailBeResponse, tenantBeResponseMap.get(salaryDetailBeResponse.getTenantId()), accountDetailBeResponseMap.get(salaryDetailBeResponse.getOperatorId())));
        }
        return JsonListResponse.success(resultList, response.getTotal());

    }

    /**
     * ???????????????Map
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
     * ????????????????????????
     */
    private Map<Long, SubjectServiceProto.SubjectDetailBeResponse> subjectDetailBeResponseMap(Collection<Long> subjectIds) {
        if (CollUtil.isEmpty(subjectIds)) {
            return Maps.newHashMap();
        }
        //????????????
        SubjectServiceProto.SubjectDetailBeResponses subjectDetailBeResponses = subjectServiceBlockingStub.getSubjectObjectByIds(SubjectServiceProto.IdBeRequests.newBuilder()
                .addAllId(subjectIds)
                .build());
        return subjectDetailBeResponses.getDataList().stream().collect(Collectors.toMap(SubjectServiceProto.SubjectDetailBeResponse::getSubjectId, Function.identity()));
    }

    /**
     * ??????????????????Map
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
            throw new SystemRuntimeException("??????????????????????????????1000");
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

        log.info("????????????????????????" + param);
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
        log.info("?????????????????? ===>??????????????????" + XbbJsonUtil.toJsonString(dataList));
        return dataList;
    }


    @SneakyThrows
    public JsonResponse<String> export(SalaryDetailPageParam param) {
        String taskCode = RedisConst.EXP_SALARY_DETAIL + SnowflakeIdGenerator.getId();
        param.setPageSize(0);
        log.info("??????,taskCode=" + taskCode);
        log.info("??????????????????,taskCode=" + param);
        AddExportTaskParam addExportTaskParam = AddExportTaskParam.newBuilder().tenantId(XBB_USER_CONTEXT.tenantId()).taskCode(taskCode).params(XbbJsonUtil.toJsonBytes(param)).creator(XBB_USER_CONTEXT.get().getUserId()).fileName("????????????").build();
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
                log.info("????????????,taskCode=" + taskCode);
                return JsonResponse.failed(XbbWebStatus.System_Error);
            }
            if (TemplateTaskServiceProto.TaskStatus.COMPLETED == response.getStatus()) {
                return JsonResponse.success(response.getObjectKey());
            }
        }
        return JsonResponse.failed(XbbWebStatus.System_Error);
    }




    /**
     * ??????????????????
     *
     * @param tenantId ??????
     * @return ??????id
     */
    public List<Long> getCustomerIds(Integer tenantId) {
        //??????saas???????????????????????????
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
     * ???????????????????????????????????????
     *
     * @return ????????????
     */
    public List<SubjectAndSubAccountResult> subjectAndSubAccountInfo(Integer tenantId) {
        List<Long> customerIds = getCustomerIds(tenantId == null ? XBB_USER_CONTEXT.get().getTenantId() : tenantId);
        //???????????????
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
        //????????????
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
     * 1. ?????????????????????????????????
     */
    public String checkUserPayerSubjectId(String idCardNo, Long payerSubjectId) {

        //????????????????????????????????????
        SignServerProto.ContractListRequest.Builder contractListRequestBuild = SignServerProto.ContractListRequest.newBuilder();
        contractListRequestBuild.setPageSize(0);//?????????
        contractListRequestBuild.setIdCardNo(idCardNo);
        contractListRequestBuild.setType(SignServerProto.EnumTemplateType.CONTRACT_VALUE);
        contractListRequestBuild.addAllStatusList(Arrays.asList(SignServerProto.EnumContractStatus.EFFECTING_VALUE, SignServerProto.EnumContractStatus.OVERTIME_VALUE, SignServerProto.EnumContractStatus.EARLY_TERMINATION_VALUE));
        SignServerProto.ContractPageListResponse contractPageListResponse = signServerBlockingStub.contractList(contractListRequestBuild.build());
        if (CollectionUtils.isEmpty(contractPageListResponse.getDataList())) {
            return "?????????????????????,?????????";
        }
        List<Long> subjectIds = contractPageListResponse.getDataList().stream().map(SignServerProto.ContractListResponse::getSubjectId).collect(Collectors.toList());

        //?????????????????????????????????????????????????????????????????????
        if (!subjectIds.contains(payerSubjectId)) {
            subjectIds.add(payerSubjectId);
            SubjectServiceProto.SubjectDetailBeResponse payerSubjectDto = null;
            //????????????
            SubjectServiceProto.SubjectDetailBeResponses subjectDetailBeResponses = subjectServiceBlockingStub.getSubjectObjectByIds(SubjectServiceProto.IdBeRequests.newBuilder()
                    .addAllId(subjectIds)
                    .build());

            boolean isThrow = true;
            for (SubjectServiceProto.SubjectDetailBeResponse subjectDetailBeResponse : subjectDetailBeResponses.getDataList()) {
                if (payerSubjectId.equals(subjectDetailBeResponse.getSubjectId())) {
                    payerSubjectDto = subjectDetailBeResponse;
                } else {
                    //?????????????????????????????? ??????????????????
                    if (subjectDetailBeResponse.getSubjectType() == SubjectServiceProto.SubjectTypeEnum.XH) {
                        isThrow = false;
                    }
                }
            }
            if (payerSubjectDto == null) {
                return "????????????????????????????????????????????????";
            }
            if (payerSubjectDto.getSubjectType() == SubjectServiceProto.SubjectTypeEnum.XH) {
                //??????????????????
                if (isThrow) {
                    return "?????????????????????????????????????????????";
                }

            } else if (payerSubjectDto.getSubjectType() == SubjectServiceProto.SubjectTypeEnum.UN_XH) {
                return "???????????????????????????????????????????????????";
            } else {
                return "???????????????????????????????????????????????????";
            }
        }
        return null;
    }

    /**
     * ???????????????????????????????????????
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
     * ???????????????????????????????????????
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
     * ????????????
     *
     * @param detailIds
     */
    public void fundBack(Collection<Long> detailIds) {
        salaryServerBlockingStub.fundBack(SalaryServerProto.FundBackRequest.newBuilder()
                .addAllDetailIds(detailIds)
                .build());
    }

    /**
     * ????????????
     *
     * @param batchId
     */
    public void breakpointRetry(Long batchId) {
        salaryServerBlockingStub.breakpointRetry(SalaryServerProto.BreakpointRetryRequest.newBuilder()
                .setBatchId(batchId)
                .build());
    }

    /**
     * ???????????????
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
            throw new SystemRuntimeException("????????????????????????????????????");
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
                    //??????id
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

        log.info("????????????????????????????????????" + param);
        JsonListResponse<SalaryBatchResult> response = findSalaryBatchPageList(param, true);
        if (CollectionUtils.isEmpty(response.getData())) {
            return Collections.emptyList();
        }
        List<OperationSalaryBatchExportData> dataList = new ArrayList<>();
        response.getData().forEach(salaryBatchResult -> {
            OperationSalaryBatchExportData operationSalaryBatchExportData = new OperationSalaryBatchExportData();

            BeanUtils.copyProperties(salaryBatchResult, operationSalaryBatchExportData);
            //set?????? ???????????????
            StringBuilder stringBuilderRemark = new StringBuilder();
            if (salaryBatchResult.getStatus() == SalaryServerProto.EnumSalaryDetailStatus.PAY_FAIL.getNumber()) {
                if (MapUtils.isNotEmpty(salaryBatchResult.getDetailFailureReasonMap())) {
                    stringBuilderRemark.append("?????????????????????");
                    for (Map.Entry<String, Integer> entry : salaryBatchResult.getDetailFailureReasonMap().entrySet()) {
                        stringBuilderRemark.append(entry.getKey() + "(" + entry.getValue() + ")");
                    }
                }
            }
            if (salaryBatchResult.getDeductionStatus() == EnumDispatchStatusCovertMsg.FAILURE.getCode()) {
                stringBuilderRemark.append("?????????????????????" + salaryBatchResult.getDeductionFailureReason());
            }
            operationSalaryBatchExportData.setRemark(stringBuilderRemark.toString());
            dataList.add(operationSalaryBatchExportData);
        });
        log.info("?????????????????????????????? ===>??????????????????" + XbbJsonUtil.toJsonString(dataList));
        return dataList;
    }
    public List<OperationSalaryDetailExportData> getOperationSalaryDetailData(SalaryDetailPageParam param) {

        log.info("????????????????????????????????????" + param);
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
        log.info("?????????????????????????????? ===>??????????????????" + XbbJsonUtil.toJsonString(dataList));
        return dataList;
    }

    @SneakyThrows
    public JsonResponse<String> operationExportDetail(SalaryDetailPageParam param) {
        String taskCode = RedisConst.EXP_OPERATION_SALARY_DETAIL + SnowflakeIdGenerator.getId();
        log.info("??????????????????????????????,taskCode=" + taskCode);
        log.info("??????????????????????????????????????????,taskCode=" + param);
        AddExportTaskParam addExportTaskParam = AddExportTaskParam.newBuilder().tenantId(XBB_USER_CONTEXT.tenantId()).taskCode(taskCode).params(XbbJsonUtil.toJsonBytes(param)).creator(XBB_USER_CONTEXT.get().getUserId()).fileName("????????????").build();
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
                log.info("????????????????????????????????????,taskCode=" + taskCode);
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
        log.info("??????????????????????????????,taskCode=" + taskCode);
        log.info("??????????????????????????????????????????,taskCode=" + param);
        AddExportTaskParam addExportTaskParam = AddExportTaskParam.newBuilder().tenantId(XBB_USER_CONTEXT.tenantId()).taskCode(taskCode).params(XbbJsonUtil.toJsonBytes(param)).creator(XBB_USER_CONTEXT.get().getUserId()).fileName("????????????").build();
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
                log.info("????????????????????????????????????,taskCode=" + taskCode);
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
