package cn.xunhou.web.xbbcloud.product.salary.convert;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.NumberUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.common.tools.util.DesPlus;
import cn.xunhou.grpc.proto.asset.AssetXhServerProto;
import cn.xunhou.grpc.proto.crm.CrmServiceProto;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import cn.xunhou.grpc.proto.subject.SubjectServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.web.xbbcloud.common.constants.CommonConst;
import cn.xunhou.web.xbbcloud.product.manage.result.BusinessContractResult;
import cn.xunhou.web.xbbcloud.product.manage.result.ContractProjectResult;
import cn.xunhou.web.xbbcloud.product.manage.result.CustomerContractResult;
import cn.xunhou.web.xbbcloud.product.salary.enums.EnumDispatchStatusCovertMsg;
import cn.xunhou.web.xbbcloud.product.salary.enums.EnumOperationSalaryDetailConvertMsg;
import cn.xunhou.web.xbbcloud.product.salary.enums.EnumSalaryBatchConvertMsg;
import cn.xunhou.web.xbbcloud.product.salary.enums.EnumSalaryDetailConvertMsg;
import cn.xunhou.web.xbbcloud.product.salary.param.*;
import cn.xunhou.web.xbbcloud.product.salary.result.*;
import cn.xunhou.web.xbbcloud.util.ProtoConvert;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class SalaryConvert {
    public static SalaryMerchantInfoResult response2Result(SalaryServerProto.MerchantInfoResponse response) {
        SalaryMerchantInfoResult result = new SalaryMerchantInfoResult();
        result.setId(response.getTenantId());
        result.setUseToDate(response.getUseToDate());
        result.setTenantType(response.getTenantTypeValue());
        result.setPayeeMerchantNo(response.getPayeeMerchantNo());
        result.setPayeeMerchantName(response.getPayeeMerchantName());
        result.setPayeeSubAccountId(response.getPayeeSubAccountId());
        result.setPayeeSubAccountNo(response.getPayeeSubAccountNo());
        result.setContractFileId(response.getContractFileId());
        result.setServiceRate(response.getServiceRate());
        result.setIsApproval(response.getIsApproval() ? CommonConst.ONE : CommonConst.TWO);
        result.setIndividualTax(response.getIndividualTax() ? CommonConst.ONE : CommonConst.TWO);
        result.setPayrollMethods(response.getPayrollMethodsValueList());
        result.setCertificationType(response.getCertificationTypeValue());
        result.setSpecialMerchantId(response.getSpecialMerchantId());
        result.setRemarks(response.getRemarks());
        if (CharSequenceUtil.isBlank(response.getBalance())) {
            result.setBalance("0.00");
        } else {
            result.setBalance(NumberUtil.div(response.getBalance(), "100", 2).toPlainString());
        }
        result.setQueryTime(response.getQueryTime());

        result.setPayerSubjectId(response.getPayerSubjectId());
        result.setPayeeSubjectId(response.getPayeeSubjectId());
        result.setOpenBankName(response.getOpenBankName());
        result.setOpenBankNo(response.getOpenBankNo());
        result.setOpenBankAddress(response.getOpenBankAddress());
        result.setXcxWithdraw(response.getXcxWithdraw() ? CommonConst.ONE : CommonConst.TWO);
        result.setContractId(response.getContractId());
        result.setProjectId(response.getProjectId());
        result.setUpdateTime(response.getUpdateTime());
        return result;
    }

    public static SalaryServerProto.SaveMerchantInfoRequest.Builder param2Request(SalaryMerchantInfoParam param,
                                                                                  Long payeeCustomerId, Long businessContractCustomerId, Long operatorId) {
        SalaryServerProto.SaveMerchantInfoRequest.Builder builder = SalaryServerProto.SaveMerchantInfoRequest.newBuilder();
        builder.setTenantId(param.getTenantId());
        builder.setUseToDate(param.getUseToDate());
        builder.setTenantTypeValue(param.getTenantType());
        builder.setPayeeMerchantNo(param.getPayeeMerchantNo());
        if (null != param.getPayeeSubjectId()) {
            builder.setPayeeSubjectId(param.getPayeeSubjectId());
        }
        builder.setPayeeMerchantName(param.getPayeeMerchantName());
        if (CharSequenceUtil.isNotBlank(param.getContractFileId())) {
            builder.setContractFileId(param.getContractFileId());
        }
        if (param.getServiceRate() != null) {
            builder.setServiceRate(param.getServiceRate());
        } else {
            builder.setServiceRate(0);
        }
        builder.setIsApproval(param.getIsApproval() == CommonConst.ONE);
        builder.setIndividualTax(param.getIndividualTax() == CommonConst.ONE);
        if (param.getPayrollMethods() != null) {
            builder.addAllPayrollMethodsValue(param.getPayrollMethods());
        }
        builder.setCertificationTypeValue(param.getCertificationType());
        if (CharSequenceUtil.isNotBlank(param.getRemarks())) {
            builder.setRemarks(param.getRemarks());
        }
        builder.setOperatorId(operatorId);
        if (CharSequenceUtil.isNotBlank(param.getServiceMerchantNo())) {
            builder.setServiceMerchantNo(param.getServiceMerchantNo());
        }
        builder.setSpecialMerchantId(ProtoConvert.string(param.getSpecialMerchantId()));
        if (param.getPayerSubjectId() != null) {
            builder.setPayerSubjectId(param.getPayerSubjectId());
        }
        builder.setOpenBankName(ProtoConvert.string(param.getOpenBankName()));
        builder.setOpenBankNo(ProtoConvert.string(param.getOpenBankNo()));
        builder.setOpenBankAddress(ProtoConvert.string(param.getOpenBankAddress()));
        builder.setXcxWithdraw(param.getXcxWithdraw() == CommonConst.ONE);

        builder.setContractId(ProtoConvert.string(param.getContractId()));
        builder.setProjectId(ProtoConvert.string(param.getProjectId()));
        if (param.getPayeeSubAccountId() != null) {
            builder.setPayeeSubAccountId(param.getPayeeSubAccountId());
        }
        if (CharSequenceUtil.isNotBlank(param.getPayeeSubAccountNo())) {
            builder.setPayeeSubAccountNo(param.getPayeeSubAccountNo());
        }
        if (null != payeeCustomerId) {
            builder.setPayeeCustomerId(payeeCustomerId);
        }
        if (null != payeeCustomerId) {
            builder.setBusinessContractCustomerId(businessContractCustomerId);
        }
        return builder;
    }

    public static SalaryServerProto.MerchantFlowPageRequest.Builder param2Request(SalaryMerchantFlowPageParam param, Long tenantId, Long operatorId) {
        SalaryServerProto.MerchantFlowPageRequest.Builder builder = SalaryServerProto.MerchantFlowPageRequest.newBuilder();

        builder.setPage(param.getCurPage());
        builder.setSize(param.getPageSize());
        builder.setTenantId(tenantId);
        if (CollUtil.isNotEmpty(param.getFlowOperationTypes())) {
            builder.addAllFlowOperationTypesValue(param.getFlowOperationTypes());
        }

        if (CharSequenceUtil.isNotBlank(param.getStartTime())) {
            builder.setStartTime(param.getStartTime());
        }
        if (CharSequenceUtil.isNotBlank(param.getEndTime())) {
            builder.setEndTime(param.getEndTime());
        }
        if (CharSequenceUtil.isNotBlank(param.getRemarks())) {
            builder.setRemarks(param.getRemarks());
        }
        if (CollUtil.isNotEmpty(param.getTradingStatus())) {
            builder.addAllTradingStatusValue(param.getTradingStatus());
        }
        if (operatorId != null) {
            builder.setOperatorId(operatorId);
        }
        if (param.getOperationAmount() != null) {
            builder.setOperationAmount(String.valueOf(NumberUtil.mul(param.getOperationAmount(), Integer.valueOf(100)).intValue()));
        }
        return builder;
    }

    public static SalaryServerProto.SalaryBatchConditionBeRequest.Builder param2Request(SalaryBatchPageParam param) {
        SalaryServerProto.SalaryBatchConditionBeRequest.Builder builder = SalaryServerProto.SalaryBatchConditionBeRequest.newBuilder();
        builder.setCurPage(param.getCurPage());
        builder.setPageSize(param.getPageSize());
        if (param.getStatus() != null) {
            builder.setStatus(param.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(param.getStartSubmitTime())) {
            builder.setStartSubmitTime(param.getStartSubmitTime());
        }
        if (CharSequenceUtil.isNotBlank(param.getEndSubmitTime())) {
            builder.setEndSubmitTime(param.getEndSubmitTime());
        }
        if (param.getBatchId() != null) {
            builder.setBatchId(param.getBatchId());
        }

        if (CharSequenceUtil.isNotBlank(param.getProductName())) {
            builder.setProductName(param.getProductName());
        }

        if (param.getPayMethod() != null) {
            builder.setPayMethod(param.getPayMethod());
        }
        if (param.getSubjectId() != null) {
            builder.setSubjectId(param.getSubjectId());
        }
        if (CollectionUtils.isNotEmpty(param.getStatusList())) {
            builder.addAllStatusList(param.getStatusList());
        }
        if (CollectionUtils.isNotEmpty(param.getDeductionStatusList())) {
            builder.addAllDeductionStatus(param.getDeductionStatusList());
        }
        return builder;
    }

    public static SalaryServerProto.SalaryDetailConditionBeRequest.Builder param2Request(SalaryDetailPageParam param) {
        SalaryServerProto.SalaryDetailConditionBeRequest.Builder builder = SalaryServerProto.SalaryDetailConditionBeRequest.newBuilder();
        builder.setCurPage(param.getCurPage());
        builder.setPageSize(param.getPageSize());
        if (CharSequenceUtil.isNotBlank(param.getIdCardNo())) {
            builder.setIdCardNo(param.getIdCardNo());
        }
        if (CharSequenceUtil.isNotBlank(param.getStaffName())) {
            builder.setStaffName(param.getStaffName());
        }
        if (CollectionUtils.isNotEmpty(param.getDetailStatusList())) {
            builder.addAllDetailStatus(param.getDetailStatusList());
        }
        if (CharSequenceUtil.isNotBlank(param.getPhone())) {
            builder.setPhone(param.getPhone());
        }
        if (CharSequenceUtil.isNotBlank(param.getStartSubmitTime())) {
            builder.setStartSubmitTime(param.getStartSubmitTime());
        }
        if (CharSequenceUtil.isNotBlank(param.getEndSubmitTime())) {
            builder.setEndSubmitTime(param.getEndSubmitTime());
        }
        if (CharSequenceUtil.isNotBlank(param.getUpdateTimeStart())) {
            builder.setUpdateTimeStart(param.getUpdateTimeStart());
        }
        if (CharSequenceUtil.isNotBlank(param.getUpdateTimeEnd())) {
            builder.setUpdateTimeEnd(param.getUpdateTimeEnd());
        }
        if (param.getBatchId() != null) {
            builder.setBatchId(param.getBatchId());
        }

        if (CharSequenceUtil.isNotBlank(param.getProductName())) {
            builder.setProductName(param.getProductName());
        }
        return builder;
    }

    public static SalaryBatchResult response2Result(SalaryServerProto.SalaryBatchBeResponse response, SubjectServiceProto.SubjectDetailBeResponse subjectDetailBeResponse, PortalServiceProto.TenantBeResponse tenantBeResponse, List<SalaryServerProto.SalaryDetailBeResponse> salaryDetailBeResponseList, HrmServiceProto.AccountDetailBeResponse accountDetailBeResponse) {
        SalaryBatchResult result = new SalaryBatchResult();
        result.setBatchId(String.valueOf(response.getBatchId()));
        result.setMonth(response.getMonth());
        result.setCreatedAt(response.getCreatedAt());
        result.setPeopleCount(response.getPeopleCount());
        result.setProductName(response.getProductName());
        result.setStatus(response.getStatus());
        result.setOperatorId(response.getOperatorId());
        result.setPayableAmount(NumberUtil.div(response.getPayableAmount(), "100", 2).toPlainString());
        result.setServiceAmount(NumberUtil.div(response.getServiceAmount(), "100", 2).toPlainString());
        result.setTenantName(tenantBeResponse.getTenantName());
        if (subjectDetailBeResponse != null) {
            result.setSubjectName(subjectDetailBeResponse.getSubjectName());
        }
        result.setPayMethod(response.getPayMethod());
        result.setDeductionFailureReason(response.getDeductionFailureReason());
        result.setDeductionStatus(response.getDeductionStatus());
        if (response.getPayMethod() == CommonConst.ONE) {
            result.setPayMethodMsg("小程序提现");
        }
        if (response.getPayMethod() == CommonConst.TWO) {
            result.setPayMethodMsg("余额到账");
        }
        result.setStatusMsg(EnumSalaryBatchConvertMsg.getEnum(result.getStatus()).getMessage());
        result.setDeductionStatusMsg(EnumDispatchStatusCovertMsg.getEnum(result.getDeductionStatus()).getMessage());
        if (null != accountDetailBeResponse) {
            result.setOperatorName(accountDetailBeResponse.getNickName());
        }
        //按失败原因分组
        Map<String, List<SalaryServerProto.SalaryDetailBeResponse>> mapByFailureReason = salaryDetailBeResponseList.stream().collect(Collectors.groupingBy(SalaryServerProto.SalaryDetailBeResponse::getFailureReason));
        Map<String, Integer> mapDetailFailureReason = new HashMap<>();
        for (Map.Entry<String, List<SalaryServerProto.SalaryDetailBeResponse>> entry : mapByFailureReason.entrySet()) {
            if (StringUtils.isNotBlank(entry.getKey())) {
                mapDetailFailureReason.put(entry.getKey(), entry.getValue().size());
            }
        }
        result.setDetailFailureReasonMap(mapDetailFailureReason);
        return result;
    }


    public static SalaryDetailResult response2Result(boolean isOperation, SubjectServiceProto.SubjectDetailBeResponse subjectDetailBeResponse, SalaryServerProto.SalaryDetailBeResponse response, PortalServiceProto.TenantBeResponse tenantBeResponse, HrmServiceProto.AccountDetailBeResponse accountDetailBeResponse) {
        SalaryDetailResult result = new SalaryDetailResult();
        result.setBatchId(String.valueOf(response.getBatchId()));
        result.setDetailId(String.valueOf(response.getDetailId()));
        result.setStatus(response.getDetailStatus());
        result.setName(response.getStaffName());
        result.setProductName(response.getProductName());
        result.setIdCardNo(DesPlus.getInstance().decrypt(response.getIdCardNo()));
        result.setPayableAmount(NumberUtil.div(response.getPayableAmount(), "100", 2).toPlainString());
        result.setPaidInAmount(NumberUtil.div(response.getPaidInAmount(), "100", 2).toPlainString());
        result.setTaxAmount(NumberUtil.div(response.getTaxAmount(), "100", 2).toPlainString());
        result.setServiceAmount(NumberUtil.div(response.getServiceAmount(), "100", 2).toPlainString());
        result.setOtherDeduct(NumberUtil.div(response.getOtherDeduct(), "100", 2).toPlainString());
        result.setRemark(response.getRemark());
        result.setFailureReason(response.getFailureReason());
        result.setOperatorId(response.getOperatorId());
        result.setCreatedAt(response.getCreateTime());
        result.setUpdatedAt(response.getUpdateTime());
        result.setTenantName(tenantBeResponse.getTenantName());
        result.setPhone(response.getPhone());
        if (StringUtils.isNotBlank(response.getExpandJson())) {
            result.setExpandInfo(XbbJsonUtil.fromJsonString(response.getExpandJson(), SalaryDetailResult.ExpandInfo.class));
        }
        if (subjectDetailBeResponse != null) {
            result.setSubjectName(subjectDetailBeResponse.getSubjectName());
        }
        result.setMonth(response.getMonth());
        result.setAssetDetailNo(response.getAssetDetailNo());
        //身份证脱敏
        result.setMaskIdCardNo(DesensitizedUtil.idCardNum(response.getIdCardNo(), 3, 4));
        if (null != accountDetailBeResponse) {
            result.setOperatorName(accountDetailBeResponse.getNickName());
        }

        if (isOperation) {
            result.setStatusMsg(EnumOperationSalaryDetailConvertMsg.getEnum(result.getStatus()).getMessage());
        } else {
            result.setStatusMsg(EnumSalaryDetailConvertMsg.getEnum(result.getStatus()).getMessage());
        }
        return result;
    }
    public static SalaryMerchantFlowResult response2Result(SalaryServerProto.MerchantFlowsResponse response) {
        SalaryMerchantFlowResult result = new SalaryMerchantFlowResult();
        result.setTradingStatus(response.getTradingStatusValue());
        result.setTenantId(response.getTenantId());
        result.setFlowNo(response.getFlowNo());
        result.setOperationType(response.getFlowOperationTypeValue());
        result.setOperatorId(response.getOperatorId());
        result.setOperationAmount(NumberUtil.div(response.getOperationAmount(), "100", 2).toPlainString());
        result.setRemarks(response.getRemarks());
        result.setSalaryBatchId(response.getSalaryBatchId());
        result.setId(response.getId());
        result.setUpdatedAt(response.getUpdatedAt());
        return result;
    }

    public static SalaryServerProto.OperateMerchantFlowRequest.Builder param2Request(SalaryOperateMerchantFlowParam param, Long operatorId) {
        SalaryServerProto.OperateMerchantFlowRequest.Builder builder = SalaryServerProto.OperateMerchantFlowRequest.newBuilder();
        builder.setTenantId(param.getTenantId());
        builder.setFlowOperationTypeValue(param.getFlowOperationType());
        builder.setOperationAmount(NumberUtil.mul(param.getOperationAmount(), "100").toPlainString());
        if (CharSequenceUtil.isNotBlank(param.getRemarks())) {
            builder.setRemarks(param.getRemarks());
        }
        builder.setOperatorId(operatorId);
        builder.setFlowNo(param.getFlowNo());

        if (param.getSalaryBatchId() != null) {
            builder.setSalaryBatchId(param.getSalaryBatchId());
        }
        return builder;
    }

    public static SubAccountInfoResult response2Result(AssetXhServerProto.SubAccountResponse response) {
        SubAccountInfoResult result = new SubAccountInfoResult();
        result.setSubAccountId(response.getSubAccountId());
        result.setCustomerId(response.getCustomerId());
        result.setSubjectInfoId(response.getSubjectInfoId());
        result.setSubAccountNo(response.getSubAccountNo());
        result.setBalance(response.getBalance());
        result.setFrozenAmount(response.getFrozenAmount());
        result.setTotalRevenue(response.getTotalRevenue());
        result.setTotalExpenditure(response.getTotalExpenditure());
        result.setType(response.getSubAccountTypeValue());
        result.setUseOverdraft(response.getUserOverdraftValue());
        result.setCustomerType(response.getCustomerSubAccountTypeValue());
        result.setRemark(response.getRemark());
        return result;
    }

    public static SubjectAndSubAccountResult response2Result(SubjectServiceProto.SubjectDetailBeResponse response, List<SubAccountInfoResult> subAccountInfoResultList) {
        SubjectAndSubAccountResult result = new SubjectAndSubAccountResult();
        result.setSubjectId(response.getSubjectId());
        result.setSubjectCode(response.getSubjectCode());
        result.setSubjectName(response.getSubjectName());
        result.setSubjectType(response.getSubjectType().getNumber());
        result.setScenes(response.getScenes());
        result.setSubjectAddress(response.getSubjectAddress());
        result.setIdentifyNum(response.getIdentifyNum());
        result.setLegalPersonName(response.getLegalPersonName());
        result.setIdcardNo(response.getIdcardNo());
        result.setTelephone(response.getTelephone());
        result.setCompanyShortName(response.getCompanyShortName());
        result.setBankCardNum(response.getBankCardNum());
        result.setBankName(response.getBankName());
        result.setBankCode(response.getBankCode());
        result.setBankType(response.getBankType().getNumber());
        result.setServiceProviderAccount(response.getServiceProviderAccount());
        result.setMerchantAccount(response.getMerchantAccount());
        result.setTopUpAccountType(response.getTopUpAccountType().getNumber());
        result.setWxPayBankCardNum(response.getWxPayBankCardNum());
        result.setWxCollectionBankCardNum(response.getWxCollectionBankCardNum());
        result.setReplacePay(response.getIsReplacePay());
        result.setTransferPay(response.getIsTransferPay());
        result.setUseSubAccount(response.getUseSubAccount());
        result.setBestSignEnterpriseAccount(response.getBestSignEnterpriseAccount());
        result.setRemark(response.getRemark());
        result.setCreatorId(response.getCreatorId());
        result.setModifyId(response.getModifyId());
        result.setSubAccountInfoList(subAccountInfoResultList);
        return result;
    }

    public static ContractProjectResult response2Result(CrmServiceProto.ProjectBeResponse response) {
        ContractProjectResult contractProjectResult = new ContractProjectResult();
        contractProjectResult.setProjectId(response.getProjectId());
        contractProjectResult.setProjectName(response.getProjectBasicInformation().getProjectName());
        return contractProjectResult;
    }

    public static BusinessContractResult result2Result(CustomerContractResult customerContractResult) {
        BusinessContractResult businessContractResult = new BusinessContractResult();
        businessContractResult.setBusinessContractId(customerContractResult.getId());
        businessContractResult.setTitle(customerContractResult.getContractName());
        return businessContractResult;

    }

    public static SubjectInfoResult response2Result(SubjectServiceProto.SubjectDetailBeResponse response) {
        SubjectInfoResult result = new SubjectInfoResult();
        result.setSubjectId(response.getSubjectId());
        result.setSubjectCode(response.getSubjectCode());
        result.setSubjectName(response.getSubjectName());
        result.setSubjectType(response.getSubjectType().getNumber());
        result.setScenes(response.getScenes());
        result.setSubjectAddress(response.getSubjectAddress());
        result.setIdentifyNum(response.getIdentifyNum());
        result.setLegalPersonName(response.getLegalPersonName());
        result.setIdcardNo(response.getIdcardNo());
        result.setTelephone(response.getTelephone());
        result.setCompanyShortName(response.getCompanyShortName());
        result.setBankCardNum(response.getBankCardNum());
        result.setBankName(response.getBankName());
        result.setBankCode(response.getBankCode());
        result.setBankType(response.getBankType().getNumber());
        result.setServiceProviderAccount(response.getServiceProviderAccount());
        result.setMerchantAccount(response.getMerchantAccount());
        result.setTopUpAccountType(response.getTopUpAccountType().getNumber());
        result.setWxPayBankCardNum(response.getWxPayBankCardNum());
        result.setWxCollectionBankCardNum(response.getWxCollectionBankCardNum());
        result.setReplacePay(response.getIsReplacePay());
        result.setTransferPay(response.getIsTransferPay());
        result.setUseSubAccount(response.getUseSubAccount());
        result.setBestSignEnterpriseAccount(response.getBestSignEnterpriseAccount());
        result.setRemark(response.getRemark());
        result.setCreatorId(response.getCreatorId());
        result.setModifyId(response.getModifyId());
        return result;
    }

    public static SalaryServerProto.TenantAccountPageRequest.Builder param2Request(SubjectFlowPageParam param) {
        SalaryServerProto.TenantAccountPageRequest.Builder builder = SalaryServerProto.TenantAccountPageRequest.newBuilder();
        builder.setPage(param.getCurPage());
        builder.setSize(param.getPageSize());
        builder.addAllSubjectIds(param.getSubjectIds() == null ? Collections.emptyList() : param.getSubjectIds());
        builder.addAllTenantIds(param.getTenantIds() == null ? Collections.emptyList() : param.getTenantIds());
        builder.addAllSubAccountIds(param.getSubAccountIds() == null ? Collections.emptyList() : param.getSubAccountIds());
        return builder;
    }

    public static SubjectFlowInfoResult response2Result(SalaryServerProto.TenantAccountResponse response) {
        SubjectFlowInfoResult subjectFlowInfoResult = new SubjectFlowInfoResult();
        subjectFlowInfoResult.setSubAccountId(response.getSubAccountId());
        subjectFlowInfoResult.setSubjectName(response.getSubjectName());
        subjectFlowInfoResult.setBankCardNo(response.getBankCardNo());
        subjectFlowInfoResult.setBalance(response.getBalance());
        subjectFlowInfoResult.setSettlementAmount(response.getSettlementAmount());
        List<SubjectFlowInfoResult.TenantInfoResult> tenantInfoResultList = Lists.newArrayList();
        for (SalaryServerProto.TenantBeResponse tenantBeResponse : response.getTenantInfoList()) {
            tenantInfoResultList.add(response2Result(tenantBeResponse));
        }
        subjectFlowInfoResult.setCustomerId(response.getCustomerId());
        subjectFlowInfoResult.setTenantInfoResults(tenantInfoResultList);
        subjectFlowInfoResult.setUpdateTime("-");
        subjectFlowInfoResult.setBalance("-");
        return subjectFlowInfoResult;
    }

    public static SubjectFlowInfoResult.TenantInfoResult response2Result(SalaryServerProto.TenantBeResponse response) {
        SubjectFlowInfoResult.TenantInfoResult tenantInfoResult = new SubjectFlowInfoResult.TenantInfoResult();
        tenantInfoResult.setTenantId((long) response.getTenantId());
        tenantInfoResult.setTenantName(response.getTenantName());
        return tenantInfoResult;

    }

    public static AssetXhServerProto.SubAccountFlowPageRequest.Builder param2Request(SubAccountFlowPageParam param) {
        AssetXhServerProto.SubAccountFlowPageRequest.Builder builder = AssetXhServerProto.SubAccountFlowPageRequest.newBuilder();
        builder.setPage(param.getCurPage());
        builder.setSize(param.getPageSize());
        builder.addAllSubAccountIds(CollUtil.isEmpty(param.getSubAccountIds()) ? Collections.emptyList() : param.getSubAccountIds());
        builder.addAllOperationTypeValue(CollUtil.isEmpty(param.getOperationType()) ? Collections.emptyList() : param.getOperationType());
        if (CharSequenceUtil.isNotBlank(param.getStartTime())) {
            builder.setStartTime(param.getStartTime());
        }
        if (CharSequenceUtil.isNotBlank(param.getEndTime())) {
            builder.setEndTime(param.getEndTime());
        }
        builder.addAllFlowStateValue(CollUtil.isEmpty(param.getFlowStates()) ? Collections.emptyList() : param.getFlowStates());
        return builder;

    }


    public static SubAccountFlowResult response2Result(AssetXhServerProto.SubAccountFlowInfoResponse flowInfoResponse) {
        SubAccountFlowResult subAccountFlowResult = new SubAccountFlowResult();
        subAccountFlowResult.setSubAccountId(flowInfoResponse.getSubAccountId());
        subAccountFlowResult.setSubAccountNo(flowInfoResponse.getSubAccountNo());
        subAccountFlowResult.setSubjectId(flowInfoResponse.getSubjectId());
        subAccountFlowResult.setFlowId(flowInfoResponse.getId());
        subAccountFlowResult.setCreatedAt(flowInfoResponse.getCreatedAt());
        subAccountFlowResult.setOperationType(flowInfoResponse.getOperationTypeValue());
        subAccountFlowResult.setOperationAmount(flowInfoResponse.getOperationAmount());
        subAccountFlowResult.setFlowState(flowInfoResponse.getFlowStateValue());
        subAccountFlowResult.setDetailNo(flowInfoResponse.getDetailNo());
        subAccountFlowResult.setSourceSysType(flowInfoResponse.getSourceSysTypeValue());
        subAccountFlowResult.setOperator(flowInfoResponse.getOperator());
//        subAccountFlowResult.setOperatorName();
        subAccountFlowResult.setOperationRemark(flowInfoResponse.getOperationRemark());
        return subAccountFlowResult;
    }


}
