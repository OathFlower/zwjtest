package cn.xunhou.xbbcloud.rpc.sign.pojo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.framework.util.DesPlus;
import cn.xunhou.grpc.proto.xbbcloud.SignServerProto;
import cn.xunhou.xbbcloud.rpc.sign.entity.ContractEntity;
import cn.xunhou.xbbcloud.rpc.sign.entity.PositionQrcodeEntity;
import cn.xunhou.xbbcloud.rpc.sign.entity.SignInfoEntity;
import cn.xunhou.xbbcloud.rpc.sign.pojo.param.ContractPageParam;
import cn.xunhou.xbbcloud.rpc.sign.pojo.param.PositionQrcodePageParam;
import cn.xunhou.xbbcloud.rpc.sign.pojo.result.ContractResult;
import cn.xunhou.xbbcloud.rpc.sign.pojo.result.PositionQrcodeResult;

/**
 * 数据转换
 */
public class SignConvert {

    public static PositionQrcodeEntity request2Entity(SignServerProto.PositionQrcodeSaveRequest request) {
        PositionQrcodeEntity entity = new PositionQrcodeEntity();
        if (request.hasTenantId()) {
            entity.setTenantId(request.getTenantId());
        }
        if (request.hasHroPositionId()) {
            entity.setHroPositionId(request.getHroPositionId());
        }
        if (request.hasContractTemplateType()) {
            entity.setContractTemplateType(request.getContractTemplateType());
        }
        if (request.hasSocialInsurance()) {
            entity.setSocialInsurance(request.getSocialInsurance());
        }
        if (request.hasTemplateJson()) {
            entity.setTemplateJson(request.getTemplateJson());
        }
        if (request.hasExpireDate()) {
            entity.setExpireDate(DateUtil.parse(request.getExpireDate()).toTimestamp());
        }
        if (request.hasSubjectId()) {
            entity.setSubjectId(request.getSubjectId());
        }
        if (request.hasOperatorId()) {
            entity.setOperatorId(request.getOperatorId());
        }

        if (request.hasRemark()) {
            entity.setRemark(request.getRemark());
        }
        if (request.hasDeletedFlag() && request.getDeletedFlag() == 1) {
            entity.setDeletedFlag(request.getDeletedFlag());
        }
        return entity;
    }


    public static SignInfoEntity request2Entity(SignServerProto.SignInfoRequest request) {
        SignInfoEntity signInfoEntity = new SignInfoEntity();
        signInfoEntity.setUseToDate(DateUtil.parse(request.getUseToDate()).toTimestamp());
        signInfoEntity.setCustomerType(request.getCustomerTypeValue());
        signInfoEntity.setOperatorId(request.getOperatorId());
        signInfoEntity.setId(request.getTenantId());
        signInfoEntity.setBusinessContractId(request.getBusinessContractId());
        SignInfoEntity.ExtInfo extInfo = new SignInfoEntity.ExtInfo();
        extInfo.setBusinessContractCustomerId(request.getBusinessContractCustomerId());
        signInfoEntity.setExtInfo(XbbJsonUtil.toJsonString(extInfo));
        return signInfoEntity;
    }


    public static ContractEntity request2Entity(SignServerProto.InsertContractRequest request) {
        ContractEntity entity = new ContractEntity();
        if (request.hasId()) {
            entity.setId(request.getId());
        }
        entity.setContractNo(request.getContractNo());
        entity.setTemplateId(request.getTemplateId());
        entity.setIdCardNo(DesPlus.getInstance().encrypt(request.getIdCardNo()));
        entity.setSubjectId(request.getSubjectId());
        entity.setTenantId(request.getTenantId());
        entity.setType(request.getType());
        entity.setStatus(request.getStatus());
        entity.setSource(request.getSource());
        entity.setSourceBusinessId(request.getSourceBusinessId());
        entity.setTemplateJson(request.getTemplateJson());
        return entity;
    }

    public static SignServerProto.PositionQrcodeResponse result2Response(PositionQrcodeResult result) {
        SignServerProto.PositionQrcodeResponse.Builder builder = SignServerProto.PositionQrcodeResponse.newBuilder();

        builder.setId(result.getId());
        builder.setHroPositionId(result.getHroPositionId());
        builder.setRemark(result.getRemark());

        builder.setTemplateJson(result.getTemplateJson());

        builder.setCreatedAt(DateUtil.format(result.getCreatedAt(), DatePattern.NORM_DATETIME_PATTERN));

        builder.setUpdatedAt(DateUtil.format(result.getUpdatedAt(), DatePattern.NORM_DATETIME_PATTERN));

        builder.setOperatorId(result.getOperatorId());

        return builder.build();

    }

    public static SignServerProto.ContractListResponse result2Response(ContractResult result) {
        SignServerProto.ContractListResponse.Builder builder = SignServerProto.ContractListResponse.newBuilder();
        builder.setIdCardNo(result.getIdCardNo());
        builder.setEmployeeId(result.getEmployeeId());
        builder.setSourceBusinessId(result.getSourceBusinessId());
        builder.setTemplateId(result.getTemplateId());
        builder.setSubjectId(result.getSubjectId());
        builder.setTenantId(result.getTenantId());
        builder.setType(result.getType());
        builder.setContractNo(result.getContractNo());
        if (result.getSignDate() != null) {
            builder.setSignDate(DateUtil.format(result.getSignDate(), DatePattern.NORM_DATETIME_PATTERN));
        }
        builder.setStatus(result.getStatus());
        builder.setTemplateJson(result.getTemplateJson());
        builder.setContractOssId(result.getContractOssId());
        builder.setServiceBusinessId(result.getServiceBusinessId());
        builder.setCreateTime(DateUtil.format(result.getCreatedAt(), DatePattern.NORM_DATETIME_PATTERN));
        builder.setModifytime(DateUtil.format(result.getUpdatedAt(), DatePattern.NORM_DATETIME_PATTERN));
        builder.setId(result.getId());
        return builder.build();

    }


    public static ContractPageParam request2Param(SignServerProto.ContractListRequest request) {
        ContractPageParam param = new ContractPageParam();
        param.setIds(request.getIdsList());

        if (request.hasIdCardNo()) {
            param.setIdCardNo(DesPlus.getInstance().encrypt(request.getIdCardNo()));
        }
        if (CollectionUtil.isNotEmpty(request.getIdCardNosList())) {
            param.setIdCardNos(request.getIdCardNosList());
        }
        if (request.hasEmployeeId()) {
            param.setEmployeeId(request.getEmployeeId());
        }
        if (CollectionUtil.isNotEmpty(request.getEmployeeIdsList())) {
            param.setEmployeeIds(request.getEmployeeIdsList());
        }
        if (request.hasSource()) {
            param.setSource(request.getSource());
        }
        if (request.hasSourceBusinessId()) {
            param.setSourceBusinessId(request.getSourceBusinessId());
        }

        if (request.hasTenantId()) {
            param.setTenantId(request.getTenantId());
        }

        if (request.hasType()) {
            param.setType(request.getType());
        }

        if (request.hasStatus()) {
            param.setStatus(request.getStatus());
        }
        if (CollectionUtil.isNotEmpty(request.getStatusListList())) {
            param.setStatusList(request.getStatusListList());
        }
        if (CollectionUtil.isNotEmpty(request.getExcludeIdsList())) {
            param.setExcludeIds(request.getExcludeIdsList());
        }
        if (request.hasId()) {
            param.setId(request.getId());
        }
        if (CollectionUtil.isNotEmpty(request.getTypesList())) {
            param.setTypes(request.getTypesList());
        }
        param.setPage(request.getCurPage()).setPageSize(request.getPageSize());
        return param;
    }


    public static PositionQrcodePageParam request2Param(SignServerProto.QrcodeListQueryRequest request) {
        PositionQrcodePageParam param = new PositionQrcodePageParam();
        if (request.hasCreateDateStart()) {
            param.setCreateDateStart(DateUtil.parse(request.getCreateDateStart()).toTimestamp());
        }
        if (request.hasCreateDateEnd()) {
            param.setCreateDateEnd(DateUtil.parse(request.getCreateDateEnd()).toTimestamp());
        }
        if (CollUtil.isNotEmpty(request.getHroPositionIdList())) {
            param.setHroPositionIds(request.getHroPositionIdList());
        }
        if (request.hasTenantId()) {
            param.setTenantId(request.getTenantId());
        }
        param.setPage(request.getCurPage()).setPageSize(request.getPageSize());
        return param;
    }

    public static SignServerProto.SignInfoResponse.Builder entity2Response(SignInfoEntity signInfoEntity) {
        SignServerProto.SignInfoResponse.Builder builder = SignServerProto.SignInfoResponse.newBuilder();
        if (signInfoEntity == null) {
            return builder;
        }
        builder.setTenantId(signInfoEntity.getId());
        builder.setBusinessContractId(signInfoEntity.getBusinessContractId());
        builder.setCustomerTypeValue(signInfoEntity.getCustomerType());
        builder.setUpdateTime(DateUtil.format(signInfoEntity.getUpdatedAt(), DatePattern.NORM_DATETIME_PATTERN));
        builder.setUseToDate(DateUtil.format(signInfoEntity.getUseToDate(), DatePattern.NORM_DATETIME_PATTERN));
        if (CharSequenceUtil.isNotBlank(signInfoEntity.getExtInfo())) {
            SignInfoEntity.ExtInfo extInfo = XbbJsonUtil.fromJsonString(signInfoEntity.getExtInfo(), SignInfoEntity.ExtInfo.class);
            builder.setBusinessContractCustomerId(extInfo.getBusinessContractCustomerId());
        }
        return builder;
    }
}
