package cn.xunhou.web.xbbcloud.product.manage.service;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.web.xbbcloud.common.constants.CommonConst;
import cn.xunhou.web.xbbcloud.product.manage.param.SwitchTenantParam;
import cn.xunhou.web.xbbcloud.product.manage.result.BusinessContractResult;
import cn.xunhou.web.xbbcloud.product.manage.result.ContractProjectResult;
import cn.xunhou.web.xbbcloud.product.salary.param.SalaryMerchantInfoParam;
import cn.xunhou.web.xbbcloud.product.salary.param.SalaryOperateMerchantFlowParam;
import cn.xunhou.web.xbbcloud.product.salary.result.SalaryMerchantFlowResult;
import cn.xunhou.web.xbbcloud.product.salary.result.SalaryMerchantInfoResult;
import cn.xunhou.web.xbbcloud.product.salary.result.SubjectAndSubAccountResult;
import cn.xunhou.web.xbbcloud.product.salary.result.SubjectInfoResult;
import cn.xunhou.web.xbbcloud.product.salary.service.SalaryService;
import cn.xunhou.web.xbbcloud.product.sign.param.SignInfoParam;
import cn.xunhou.web.xbbcloud.product.sign.result.SignInfoResult;
import cn.xunhou.web.xbbcloud.product.sign.service.SignService;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author wangkm
 */
@Slf4j
@Service
public class ManageService {
    @GrpcClient("ins-xhportal-platform")
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;

    @Resource
    private SalaryService salaryService;

    @Resource
    private SignService signService;

    /**
     * 查询商户信息
     *
     * @param tenantId 租户id
     * @return 商户信息
     */
    public SalaryMerchantInfoResult merchantInfo(Long tenantId) {
        return salaryService.merchantInfo(tenantId);
    }

    /**
     * 启用｜关闭薪酬云能力
     *
     * @param param 参数
     * @return 成功
     */
    public Boolean switchRolePermission(SwitchTenantParam param) {

        String roleName = null;
        if (param.getType() == CommonConst.ONE) {
            roleName = "HRM-薪酬云角色";
        } else if (CommonConst.TWO == param.getType()) {
            //签约云开关
            roleName = "HRM-签约云角色";
        } else {
            throw new SystemRuntimeException("暂不支持其他类型开关！");
        }
        HrmServiceProto.findXgkRoleByTenantBeResponses roleByTenantBeResponses = hrmServiceBlockingStub.findXgkRoleByTenant(HrmServiceProto.SwitchRoleRequest.newBuilder()
                .setTenantId(param.getTenantId().intValue())
                .setRoleName(roleName)
                .build());
        //不显示能力
        if (param.getIsUse() == roleByTenantBeResponses.getType()) {
            return true;
        }
        hrmServiceBlockingStub.switchRolePermission(HrmServiceProto.SwitchRoleRequest.newBuilder()
                .setTenantId(param.getTenantId().intValue())
                .setRoleName(roleName)
                .setType(param.getIsUse())
                .build());
        return true;
    }


    /**
     * 新增|修改商户信息（变更信息时需校验使用场景）
     *
     * @param param 入参
     * @return 返回商户信息
     */
    public SalaryMerchantInfoResult saveMerchantInfo(SalaryMerchantInfoParam param) {
        if (NumberUtil.equals(SalaryServerProto.EnumTenantType.SAAS_VALUE, param.getTenantType())) {
            if (ObjectUtil.hasEmpty(
                    param.getSpecialMerchantId()
            )) {
                throw new SystemRuntimeException("saas客户 特约商户id 不能为空！");
            }
        } else if (NumberUtil.equals(SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE, param.getTenantType())) {
            if (ObjectUtil.hasEmpty(
                    param.getPayerSubjectId(),
                    param.getServiceRate(),
                    param.getContractId(),
                    param.getPayeeSubAccountId(),
                    param.getPayeeSubjectId(),
                    param.getProjectId()
            )) {
                throw new SystemRuntimeException("代发客户 收款主体，收款子账户,发薪主体，服务费率，商务合同，项目 不能为空！");
            } else {
                BigDecimal serviceRate = BigDecimal.valueOf(param.getServiceRate());
                if (BigDecimal.ZERO.compareTo(serviceRate) > 0) {
                    throw new SystemRuntimeException("服务费率>=0");
                }
            }
        }
        return salaryService.saveMerchantInfo(param);
    }


    /**
     * 查询商户余额
     *
     * @param tenantId 租户id
     * @return 商户信息
     */
    public SalaryMerchantInfoResult findMerchantBalance(Long tenantId) {
        return salaryService.findMerchantBalance(tenantId);
    }


    /**
     * 商户流水操作
     *
     * @param param 操作流水
     * @return 最新操作流水
     */
    public SalaryMerchantFlowResult operateMerchantFlow(SalaryOperateMerchantFlowParam param) {
        return salaryService.operateMerchantFlow(param);
    }

    /**
     * 根据租户查询收款主体和子账户信息
     *
     * @return 结果
     */
    public List<SubjectAndSubAccountResult> subjectAndSubAccountInfo(Integer tenantId) {
        return salaryService.subjectAndSubAccountInfo(tenantId);
    }


    /**
     * 查询发薪户主体信息
     */
    public List<SubjectInfoResult> payerSubject() {
        return salaryService.payerSubject();
    }

    /**
     * 商务合同
     */
    public List<BusinessContractResult> businessContract(Integer tenantId) {
        return salaryService.businessContract(tenantId);
    }


    /**
     * 合同项目
     */
    public List<ContractProjectResult> contractProject(Long businessContractId) {
        return salaryService.contractProject(businessContractId);
    }

    /**
     * 查询签约云基本信息
     *
     * @return 签约云基本信息
     */
    public SignInfoResult info(Long tenantId) {
        return signService.info(tenantId);
    }

    /**
     * 新增｜保存 签约云基本信息
     *
     * @param param 签约云信息
     * @return 签约云基本信息
     */
    public SignInfoResult saveInfo(SignInfoParam param) {
        return signService.saveInfo(param);
    }
}
