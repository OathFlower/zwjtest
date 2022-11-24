package cn.xunhou.web.xbbcloud.product.sign.convert;

import cn.hutool.core.collection.CollectionUtil;
import cn.xunhou.grpc.proto.xbbcloud.SignServerProto;
import cn.xunhou.web.xbbcloud.product.sign.param.SavePositionQrcodeParam;
import cn.xunhou.web.xbbcloud.product.sign.param.SignInfoParam;
import cn.xunhou.web.xbbcloud.product.sign.param.TemplateParam;
import cn.xunhou.web.xbbcloud.product.sign.result.ContractResult;
import cn.xunhou.web.xbbcloud.product.sign.result.SignInfoResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Slf4j
public class SignConvert {


    public static SignServerProto.PositionQrcodeSaveRequest.Builder param2Request(SavePositionQrcodeParam param) {
        SignServerProto.PositionQrcodeSaveRequest.Builder builder = SignServerProto.PositionQrcodeSaveRequest.newBuilder();
        if (param.getId() != null) {
            builder.setId(param.getId());
        }
        builder.setDeletedFlag(param.getDeleteflag());
        if (param.getDeleteflag() != 1) {
            builder.setHroPositionId(param.getHroPositionId());
            builder.setSocialInsurance(param.getSocialInsurance());
            builder.setExpireDate(param.getExpireDate());
            builder.setTemplateJson(param.getTemplateJson());
            builder.setSubjectId(param.getSubjectId());
            if (StringUtils.isNotBlank(param.getRemark())) {
                builder.setRemark(param.getRemark());
            }
            builder.setContractTemplateType(param.getContractTemplateType());


            if (CollectionUtil.isNotEmpty(param.getContractTemplateList())) {
                List<SignServerProto.ContractTemplate> templateRequestList = new ArrayList<>();
                for (TemplateParam templateParam :
                        param.getContractTemplateList()) {
                    SignServerProto.ContractTemplate.Builder templateBuild = SignServerProto.ContractTemplate.newBuilder();
                    templateBuild.setTemplateId(templateParam.getTemplateId());
                    templateBuild.setType(templateParam.getType());
                    templateRequestList.add(templateBuild.build());
                }

                builder.addAllContractTemplate(templateRequestList);
            }
            if (CollectionUtil.isNotEmpty(param.getTelList())) {
                builder.addAllTel(param.getTelList());
            }
        }
        return builder;
    }

    public static ContractResult response2Result(SignServerProto.ContractListResponse response) {
        ContractResult contractResult = new ContractResult();
        contractResult.setId(response.getId());
        return contractResult;
    }

    public static SignInfoResult response2Result(SignServerProto.SignInfoResponse response) {
        SignInfoResult signInfoResult = new SignInfoResult();
        signInfoResult.setTenantId(response.getTenantId());
        signInfoResult.setCustomerType(response.getCustomerTypeValue());
        signInfoResult.setProjectIds(response.getProjectIdsList());
        signInfoResult.setBusinessContractId(response.getBusinessContractId());
        signInfoResult.setUseToDate(response.getUseToDate());
        signInfoResult.setUpdateTime(response.getUpdateTime());
        return signInfoResult;
    }

    public static SignServerProto.SignInfoRequest.Builder param2Request(SignInfoParam param, Long businessContractCustomerId, Long operatorId) {
        SignServerProto.SignInfoRequest.Builder builder = SignServerProto.SignInfoRequest.newBuilder();
        builder.setTenantId(param.getTenantId());
        builder.setOperatorId(operatorId);
        builder.setCustomerTypeValue(param.getCustomerType());
        builder.addAllProjectIds(param.getProjectIds());
        builder.setUseToDate(param.getUseToDate());
        builder.setBusinessContractId(param.getBusinessContractId());
        builder.setBusinessContractCustomerId(businessContractCustomerId);
        return builder;

    }
}
