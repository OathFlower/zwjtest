package cn.xunhou.xbbcloud.rpc.salary.pojo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.framework.util.DesPlus;
import cn.xunhou.grpc.proto.asset.AssetXhServerProto;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.xbbcloud.common.bean.XhTreeNode;
import cn.xunhou.xbbcloud.common.constants.CommonConst;
import cn.xunhou.xbbcloud.rpc.salary.entity.*;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.*;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryBatchResult;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryDetailResult;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryMerchantFlowResult;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.HashSet;
import java.util.List;

/**
 * 数据转换
 */
public class SalaryConvert {
    public static SalaryServerProto.MerchantInfoResponse.Builder entity2Response(SalaryMerchantInfoEntity entity) {
        SalaryServerProto.MerchantInfoResponse.Builder builder = SalaryServerProto.MerchantInfoResponse.newBuilder();
        if (entity == null) {
            return builder;
        }
        builder.setTenantId(entity.getId());
        builder.setUseToDate(DateUtil.format(entity.getUseToDate(), DatePattern.NORM_DATETIME_PATTERN));
        builder.setTenantTypeValue(entity.getTenantType());
        builder.setPayeeMerchantNo(entity.getPayeeMerchantNo());
        builder.setPayeeMerchantName(entity.getPayeeMerchantName());
        builder.setContractFileId(entity.getContractFileId());
        builder.setServiceRate(entity.getServiceRate());
        builder.setIsApproval(entity.getIsApproval() == CommonConst.ONE);
        builder.setIndividualTax(entity.getIndividualTax() == CommonConst.ONE);
        builder.addAllPayrollMethodsValue(Convert.toList(Integer.class, CharSequenceUtil.split(entity.getPayrollMethod(), ",")));
        builder.setCertificationTypeValue(entity.getCertificationType());
        builder.setSpecialMerchantId(entity.getSpecialMerchantId());
        if (entity.getRemarks() != null) {
            builder.setRemarks(entity.getRemarks());
        }
        if (null != entity.getPayeeSubjectId()) {
            builder.setPayeeSubjectId(entity.getPayeeSubjectId());
        }
        if (null != entity.getPayerSubjectId()) {
            builder.setPayerSubjectId(entity.getPayerSubjectId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getExtInfo())) {
            SalaryMerchantInfoEntity.ExtInfo extInfo = XbbJsonUtil.fromJsonString(entity.getExtInfo(), SalaryMerchantInfoEntity.ExtInfo.class);
            builder.setOpenBankName(extInfo.getOpenBankName());
            builder.setOpenBankNo(extInfo.getOpenBankNo());
            builder.setOpenBankAddress(extInfo.getOpenBankAddress());
            builder.setXcxWithdraw(extInfo.getXcxWithdraw() == CommonConst.ONE);

            if (CharSequenceUtil.isNotBlank(extInfo.getBusinessContractId())) {
                builder.setContractId(extInfo.getBusinessContractId());
            }
            if (CharSequenceUtil.isNotBlank(extInfo.getPayeeSubAccountNo())) {
                builder.setPayeeSubAccountNo(extInfo.getPayeeSubAccountNo());
            }
            if (CharSequenceUtil.isNotBlank(extInfo.getProjectId())) {
                builder.setProjectId(extInfo.getProjectId());
            }
            List<Long> useCustomerIds = Lists.newArrayList();
            if (null != extInfo.getBusinessContractCustomerId()) {
                builder.setBusinessContractCustomerId(extInfo.getBusinessContractCustomerId());
                if (extInfo.getBusinessContractCustomerId() != CommonConst.ZERO) {
                    useCustomerIds.add(extInfo.getBusinessContractCustomerId());
                }
            }
            if (null != extInfo.getPayeeCustomerId()) {
                builder.setPayeeCustomerId(extInfo.getPayeeCustomerId());
                if (extInfo.getPayeeCustomerId() != CommonConst.ZERO) {
                    useCustomerIds.add(extInfo.getPayeeCustomerId());
                }
            }
            builder.addAllUseCustomerIds(useCustomerIds);
        }
        if (entity.getPayeeSubAccountId() != null) {
            builder.setPayeeSubAccountId(entity.getPayeeSubAccountId());
        }
        return builder;
    }

    public static SalaryMerchantInfoEntity request2Entity(SalaryServerProto.SaveMerchantInfoRequest request) {
        SalaryMerchantInfoEntity entity = new SalaryMerchantInfoEntity();
        entity.setUseToDate(DateUtil.parse(request.getUseToDate()).toTimestamp());
        entity.setTenantType(request.getTenantTypeValue());
        entity.setPayeeMerchantNo(request.getPayeeMerchantNo());
        entity.setPayeeMerchantName(request.getPayeeMerchantName());
        entity.setContractFileId(request.getContractFileId());
        entity.setServiceRate(request.getServiceRate());
        entity.setIsApproval(request.getIsApproval() ? CommonConst.ONE : CommonConst.TWO);
        entity.setIndividualTax(request.getIndividualTax() ? CommonConst.ONE : CommonConst.TWO);
        entity.setPayrollMethod(CollUtil.join(request.getPayrollMethodsValueList(), ","));
        entity.setCertificationType(request.getCertificationTypeValue());
        entity.setSpecialMerchantId(request.getSpecialMerchantId());
        entity.setRemarks(request.getRemarks());
        entity.setOperatorId(request.getOperatorId());
        entity.setId(request.getTenantId());
        if(request.hasPayerSubjectId()){
            entity.setPayerSubjectId(request.getPayerSubjectId());
        }

        SalaryMerchantInfoEntity.ExtInfo extInfo = new SalaryMerchantInfoEntity.ExtInfo();
        extInfo.setOpenBankName(request.getOpenBankName());
        extInfo.setOpenBankNo(request.getOpenBankNo());
        extInfo.setOpenBankAddress(request.getOpenBankAddress());
        extInfo.setXcxWithdraw(request.getXcxWithdraw() ? CommonConst.ONE : CommonConst.TWO);
        extInfo.setBusinessContractId(request.getContractId());
        extInfo.setProjectId(request.getProjectId());
        extInfo.setPayeeSubAccountNo(request.getPayeeSubAccountNo());
        extInfo.setBusinessContractCustomerId(request.getBusinessContractCustomerId());
        extInfo.setPayeeCustomerId(request.getPayeeCustomerId());

        entity.setExtInfo(XbbJsonUtil.toJsonString(extInfo));
        entity.setPayeeSubAccountId(request.getPayeeSubAccountId());
        entity.setPayeeSubjectId(request.getPayeeSubjectId());
        return entity;
    }

    public static SalaryMerchantFlowEntity request2Entity(SalaryServerProto.OperateMerchantFlowRequest request) {
        SalaryMerchantFlowEntity entity = new SalaryMerchantFlowEntity();
        entity.setTenantId(request.getTenantId());
        entity.setFlowNo(request.getFlowNo());
        entity.setOperationType(request.getFlowOperationTypeValue());
        entity.setOperatorId(request.getOperatorId());
        if (CharSequenceUtil.isNotBlank(request.getOperationAmount())) {
            entity.setOperationAmount(Integer.valueOf(request.getOperationAmount()));
        }
        entity.setRemarks(request.getRemarks());
        if (request.hasSalaryBatchId()) {
            entity.setSalaryBatchId(request.getSalaryBatchId());
        }
        return entity;
    }
    
    public static SalaryMerchantFlowPageParam request2Param(SalaryServerProto.MerchantFlowPageRequest request) {
        SalaryMerchantFlowPageParam param = new SalaryMerchantFlowPageParam();
        param.setPage(request.getPage()).setPageSize(request.getSize());
        param.setTenantId(request.getTenantId());
        param.setFlowOperationTypes(request.getFlowOperationTypesValueList());
        param.setTradingStatus(request.getTradingStatusValueList());
        if (request.hasEndTime()) {
            param.setEndTime(DateUtil.parse(request.getEndTime()).toTimestamp());
        }
        if (request.hasStartTime()) {
            param.setStartTime(DateUtil.parse(request.getStartTime()).toTimestamp());
        }
        if (request.hasOperationAmount()) {
            param.setOperationAmount(Integer.valueOf(request.getOperationAmount()));
        }
        if (request.hasRemarks()) {
            param.setRemarks(request.getRemarks());
        }
        if (request.hasOperatorId()) {
            param.setOperatorId(param.getOperatorId());
        }
        return param;
    }


    public static SalaryDetailEntity request2Entity(SalaryServerProto.SalaryDetailRequest request) {
        SalaryDetailEntity entity = new SalaryDetailEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setOtherDeduct(request.getOtherDeduct());
        entity.setIdCardNo(DesPlus.getInstance().encrypt(request.getIdCardNo()));
        return entity;
    }

    public static SalaryDetailPageParam request2Param(SalaryServerProto.SalaryDetailConditionBeRequest request) {
        SalaryDetailPageParam param = new SalaryDetailPageParam();
        param.setPage(request.getCurPage()).setPageSize(request.getPageSize());
        if (CollectionUtil.isNotEmpty(request.getDetailStatusList())) {
            param.setDetailStatus(request.getDetailStatusList());
        }
        if (request.hasTenantId()) {
            param.setTenantId(request.getTenantId());
        }
        if (request.hasId()) {
            param.setId(request.getId());
        }
        if (request.hasBatchId()) {
            param.setBatchId(request.getBatchId());
        }
        if (request.hasEndSubmitTime()) {
            param.setEndSubmitTime(DateUtil.parse(request.getEndSubmitTime()).toTimestamp());
        }
        if (request.hasProductName()) {
            param.setProductName(request.getProductName());
        }
        if (request.hasStartSubmitTime()) {
            param.setStartSubmitTime(DateUtil.parse(request.getStartSubmitTime()).toTimestamp());
        }

        if (request.hasUpdateTimeStart()) {
            param.setUpdateTimeStart(DateUtil.parse(request.getUpdateTimeStart()).toTimestamp());
        }

        if (request.hasUpdateTimeEnd()) {
            param.setUpdateTimeEnd(DateUtil.parse(request.getUpdateTimeEnd()).toTimestamp());
        }
        if (request.hasIdCardNo()) {
            param.setIdCardNo(DesPlus.getInstance().encrypt(request.getIdCardNo()));
        }
        if (request.hasStaffName()) {
            param.setStaffName(request.getStaffName());
        }
        if (request.hasPhone()) {
            param.setPhone(request.getPhone());
        }

        if (CollectionUtil.isNotEmpty(request.getTenantIdsList())) {
            param.setTenantIdList(request.getTenantIdsList());
        }
        if (CollectionUtil.isNotEmpty(request.getIdCardNosList())) {
            param.setIdCardNoList(request.getIdCardNosList());
        }

        if (CollectionUtil.isNotEmpty(request.getNotInDetailStatusList())) {
            param.setNotInDetailStatus(request.getNotInDetailStatusList());
        }

        if (CollectionUtil.isNotEmpty(request.getBatchIdsList())) {
            param.setBatchIdList(request.getBatchIdsList());
        }

        if (request.hasIsOperation()) {
            param.setOperation(request.getIsOperation());
        }
        param.setIds(request.getIdsList());
        return param;
    }

    public static SalaryBatchPageParam request2Param(SalaryServerProto.SalaryBatchConditionBeRequest request) {
        SalaryBatchPageParam param = new SalaryBatchPageParam();
        param.setPage(request.getCurPage()).setPageSize(request.getPageSize());
        if (request.hasStatus()) {
            param.setStatus(request.getStatus());
        }
        if (request.hasStartSubmitTime()) {
            param.setStartSubmitTime(DateUtil.parse(request.getStartSubmitTime()).toTimestamp());
        }
        if (request.hasEndSubmitTime()) {
            param.setEndSubmitTime(DateUtil.parse(request.getEndSubmitTime()).toTimestamp());
        }
        if (request.hasBatchId()) {
            param.setBatchId(request.getBatchId());
        }
        if (request.hasProductName()) {
            param.setProductName(request.getProductName());
        }
        if (request.hasPayMethod()) {
            param.setPayMethod(request.getPayMethod());
        }
        if (request.hasSubjectId()) {
            param.setSubjectId(request.getSubjectId());
        }
        if (request.hasTenantId()) {
            param.setTenantId(request.getTenantId());
        }
        if (request.hasIsOperation()) {
            param.setOperation(request.getIsOperation());
        }
        param.setDeductionStatusList(request.getDeductionStatusList());
        param.setBatchIdList(request.getBatchIdsList());
        param.setStatusList(request.getStatusListList());
        return param;
    }

    public static SalaryServerProto.SalaryProductResponse.Builder entity2Response(SalaryProductEntity entity) {
        SalaryServerProto.SalaryProductResponse.Builder builder = SalaryServerProto.SalaryProductResponse.newBuilder();
        builder.setName(entity.getName());
        builder.setId(entity.getId());
        return builder;
    }

    public static SalaryServerProto.SalaryDetailBeResponse.Builder result2Response(SalaryDetailResult result) {
        SalaryServerProto.SalaryDetailBeResponse.Builder builder = SalaryServerProto.SalaryDetailBeResponse.newBuilder();
        builder.setBatchId(result.getBatchId());
        builder.setDetailId(result.getDetailId());
        builder.setDetailStatus(result.getStatus());
        builder.setSubjectId(result.getSubjectId());
        builder.setCreateTime(DateUtil.format(result.getCreatedAt(), DatePattern.NORM_DATETIME_PATTERN));
        builder.setUpdateTime(DateUtil.format(result.getUpdatedAt(), DatePattern.NORM_DATETIME_PATTERN));
        builder.setIdCardNo(result.getIdCardNo());
        builder.setStaffName(result.getName());
        builder.setPaidInAmount(Opt.ofNullable(result.getPaidInAmount()).orElse(0).toString());
        builder.setServiceAmount(Opt.ofNullable(result.getServiceAmount()).orElse(0).toString());
        builder.setOperatorId(result.getOperatorId());
        builder.setProductName(result.getProductName());
        builder.setRemark(result.getRemark());
        builder.setFailureReason(result.getFailureReason());
        builder.setTaxAmount(Opt.ofNullable(result.getTaxAmount()).orElse(0).toString());
        builder.setPayableAmount(Opt.ofNullable(result.getPayableAmount()).orElse(0).toString());
        builder.setOtherDeduct(Opt.ofNullable(result.getOtherDeduct()).orElse(0).toString());
        builder.setTenantId(result.getTenantId());
        builder.setPhone(result.getPhone());
        builder.setMonth(result.getSalaryMonth());
        builder.setRetryCount(result.getRetryCount());
        if (StringUtils.isNotBlank(result.getExpandJson())) {
            builder.setExpandJson(result.getExpandJson());
        }
        if (StringUtils.isNotBlank(result.getAssetDetailNo())) {
            builder.setAssetDetailNo(result.getAssetDetailNo());
        }
        return builder;
    }

    public static SalaryServerProto.SalaryBatchBeResponse.Builder result2Response(SalaryBatchResult result) {
        SalaryServerProto.SalaryBatchBeResponse.Builder builder = SalaryServerProto.SalaryBatchBeResponse.newBuilder();
        builder.setCreatedAt(DateUtil.format(result.getCreatedAt(), DatePattern.NORM_DATETIME_PATTERN));
        builder.setProductName(result.getProductName());
        builder.setMonth(result.getMonth());
        builder.setBatchId(result.getBatchId());
        builder.setPeopleCount(result.getPeopleCount());
        builder.setPayableAmount(Opt.ofNullable(result.getPayableAmount()).orElse(0).toString());
        builder.setServiceAmount(Opt.ofNullable(result.getServiceAmount()).orElse(0).toString());
        builder.setStatus(result.getStatus());
        builder.setOperatorId(result.getOperatorId());
        builder.setTenantId(result.getTenantId());
        builder.setSubjectId(result.getSubjectId());
        builder.setPayMethod(result.getPayMethod());
        if (StringUtils.isNotBlank(result.getExpandJson())) {
            builder.setExpandJson(result.getExpandJson());
        }
        builder.setDeductionStatus(result.getDeductionStatus());
        if (CharSequenceUtil.isNotBlank(result.getDeductionFailureReason())) {
            List<String> failureReasonList = CharSequenceUtil.split(result.getDeductionFailureReason(), ",");
            builder.setDeductionFailureReason(CharSequenceUtil.join(",", new HashSet<>(failureReasonList)));
        }
        return builder;
    }

    public static SalaryServerProto.MerchantFlowsResponse.Builder result2Response(SalaryMerchantFlowResult result) {
        SalaryServerProto.MerchantFlowsResponse.Builder builder = SalaryServerProto.MerchantFlowsResponse.newBuilder();
        builder.setTenantId(result.getTenantId());
        builder.setFlowOperationTypeValue(result.getOperationType());
        builder.setOperationAmount(Opt.ofNullable(result.getOperationAmount()).orElse(0).toString());
        builder.setTradingStatusValue(result.getTradingStatus());
        builder.setRemarks(result.getRemarks());
        builder.setOperatorId(result.getOperatorId());
        builder.setFlowNo(result.getFlowNo());
        if (result.getSalaryBatchId() != null) {
            builder.setSalaryBatchId(result.getSalaryBatchId());
        }
        builder.setId(result.getId());
        builder.setUpdatedAt(DateUtil.format(result.getUpdatedAt(), DatePattern.NORM_DATETIME_PATTERN));
        return builder;
    }

    public static SalaryMerchantFlowEntity response2Entity(AssetXhServerProto.WxMerchantRechargeFlowResponse response, Long tenantId, String subjectName, String payeeInfoId) {
        SalaryMerchantFlowEntity entity = new SalaryMerchantFlowEntity();
        entity.setTenantId(tenantId);
        entity.setFlowNo(response.getIncomeRecordId());
        entity.setOperationType(CommonConst.ONE);
        entity.setOperatorId(0L);
        entity.setOperationAmount(Integer.valueOf(response.getAmount()));
        entity.setRemarks(response.getRechargeRemark());
        entity.setSubjectName(subjectName);
        entity.setPayeeInfoId(payeeInfoId);
//        entity.setSalaryBatchId();
//        entity.setId();
        return entity;
    }

    public static XhTreeNode<FundDispatchingEntity> entity2Node(FundDispatchingEntity fundDispatching) {
        XhTreeNode<FundDispatchingEntity> xhTreeNode = new XhTreeNode<>();
        xhTreeNode.setSourceNode(fundDispatching);
        xhTreeNode.setId(fundDispatching.getId());
        xhTreeNode.setPid(fundDispatching.getParentId());
        return xhTreeNode;
    }

    public static AssetXhServerProto.FundDispatchingRequest.DispatchingNode.Builder entity2Request(FundDispatchingEntity entity) {
        AssetXhServerProto.FundDispatchingRequest.DispatchingNode.Builder builder = AssetXhServerProto.FundDispatchingRequest.DispatchingNode.newBuilder();
        builder.setNo(entity.getId() + CommonConst.UNDERLINE + entity.getRetryCount());
        builder.setOrder(entity.getOrderValue());
        builder.setBusinessTypeValue(entity.getDispatchDirection());
        builder.setAmount(String.valueOf(entity.getAmount()));
        builder.setRemark1(entity.getRemark1());
        builder.setRemark2(entity.getRemark2());
        builder.setPayerSubAccountId(entity.getPayerId());
        builder.setPayeeSubAccountId(entity.getPayeeId());
        return builder;
    }

    public static SalaryMerchantFlowEntity response2Entity(AssetXhServerProto.CmbBillFlowResponse response, Long tenantId, String subjectName, Long subAccountId) {
        SalaryMerchantFlowEntity entity = new SalaryMerchantFlowEntity();
        entity.setTenantId(tenantId);
        entity.setFlowNo(response.getTrxnbr());
        entity.setOperationType(SalaryServerProto.EnumFlowOperationType.INCOME_VALUE);
        entity.setOperatorId(0L);
        entity.setOperationAmount(NumberUtil.mul(response.getTrxamt(), "100").intValue());
        entity.setRemarks(response.getTrxtxt());
        entity.setSubjectName(subjectName);
        entity.setPayeeInfoId(subAccountId == null ? null : subAccountId + "");
        return entity;
    }

    public static QuerySalaryMerchantInfo request2Param(SalaryServerProto.MerchantInfoListRequest request) {
        QuerySalaryMerchantInfo querySalaryMerchantInfo = new QuerySalaryMerchantInfo();
        querySalaryMerchantInfo.setTenantTypes(request.getTenantTypesValueList());
        querySalaryMerchantInfo.setPayeeSubAccountIds(request.getPayeeSubAccountIdsList());
        querySalaryMerchantInfo.setIds(request.getTenantIdsList());
        return querySalaryMerchantInfo;
    }

    public static SalaryServerProto.TenantAccountResponse.Builder response2Response(AssetXhServerProto.SubAccountResponse subAccountResponse) {
        SalaryServerProto.TenantAccountResponse.Builder builder = SalaryServerProto.TenantAccountResponse.newBuilder();
        builder.setSubjectId(subAccountResponse.getSubjectInfoId());
        builder.setBankCardNo(subAccountResponse.getSubAccountNo());
        builder.setBalance(subAccountResponse.getBalance());
        builder.setSettlementAmount(subAccountResponse.getFrozenAmount());
        builder.setSubAccountId(subAccountResponse.getSubAccountId());
        builder.setCustomerId(subAccountResponse.getCustomerId());
        return builder;
    }

    public static SalaryServerProto.TenantBeResponse.Builder response2Response(PortalServiceProto.TenantBeResponse response) {
        SalaryServerProto.TenantBeResponse.Builder builder = SalaryServerProto.TenantBeResponse.newBuilder();
        builder.setTenantId(response.getTenantId());
        builder.setTenantName(response.getTenantName());
        builder.setTenantNumber(response.getTenantNumber());
        builder.setAlias(response.getAlias());
        builder.setTenantSource(response.getTenantSource().getNumber());
        builder.setRunState(response.getRunState().getNumber());
        builder.setOtherState(response.getOtherState().getNumber());
        builder.setLogo(response.getLogo());
        builder.setProductId(response.getProductId());
        builder.setDomain(response.getDomain());
        builder.setDescription(response.getDescription());
        builder.setValidTimeStart(response.getValidTimeStart());
        builder.setValidTimeEnd(response.getValidTimeEnd());
        builder.setCreatorBy(response.getCreatorBy());
        builder.setModifiedBy(response.getModifiedBy());
        builder.setStatus(response.getStatus().getNumber());
        builder.setCreatedAt(response.getCreatedAt());
        builder.setUpdatedAt(response.getUpdatedAt());
        builder.setDeletedFlag(response.getDeletedFlag());
        builder.setRemark(response.getRemark());
        builder.setAdminId(response.getAdminId());
        builder.setAdminName(response.getAdminName());
        builder.setAdminTel(response.getAdminTel());
        builder.setContact(response.getContact());
        builder.setContactTel(response.getContactTel());
        builder.setContactIdCard(response.getContactIdCard());
        builder.setIndustry(response.getIndustry());
        builder.setAddress(response.getAddress());
        builder.setRegion(response.getRegion());
        builder.setCreator(response.getCreator());
        builder.setCooperativeInformationJson(response.getCooperativeInformationJson());
        builder.setAttachmentJson(response.getAttachmentJson());
        builder.setAdminUserXhId(response.getAdminUserXhId());
        builder.setCompany(response.getCompany());
        builder.setModified(response.getModified());
        return builder;
    }

    public static SalaryMerchantInfoPageParam request2Param(SalaryServerProto.MerchantInfoPageRequest request) {
        SalaryMerchantInfoPageParam salaryMerchantInfoPageParam = new SalaryMerchantInfoPageParam();
        salaryMerchantInfoPageParam.setTenantTypes(request.getTenantTypesValueList());
        salaryMerchantInfoPageParam.setIds(request.getTenantIdsList());
        salaryMerchantInfoPageParam.setPage(request.getPage());
        salaryMerchantInfoPageParam.setPageSize(request.getSize());
        return salaryMerchantInfoPageParam;
    }
}
