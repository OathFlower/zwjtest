package cn.xunhou.web.xbbcloud.product.manage.controller;


import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.manage.param.SwitchTenantParam;
import cn.xunhou.web.xbbcloud.product.manage.result.BusinessContractResult;
import cn.xunhou.web.xbbcloud.product.manage.result.ContractProjectResult;
import cn.xunhou.web.xbbcloud.product.manage.service.ManageService;
import cn.xunhou.web.xbbcloud.product.salary.param.SalaryMerchantInfoParam;
import cn.xunhou.web.xbbcloud.product.salary.param.SalaryOperateMerchantFlowParam;
import cn.xunhou.web.xbbcloud.product.salary.result.SalaryMerchantFlowResult;
import cn.xunhou.web.xbbcloud.product.salary.result.SalaryMerchantInfoResult;
import cn.xunhou.web.xbbcloud.product.salary.result.SubjectAndSubAccountResult;
import cn.xunhou.web.xbbcloud.product.salary.result.SubjectInfoResult;
import cn.xunhou.web.xbbcloud.product.sign.param.SignInfoParam;
import cn.xunhou.web.xbbcloud.product.sign.result.SignInfoResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * saas信息管理
 *
 * @author wangkm
 */
@RestController
@RequestMapping("/api/manages")
public class ManageController {
    @Resource
    private ManageService manageService;

    /**
     * 查询商户信息
     *
     * @param tenantId 租户id
     * @return 商户信息
     */
    @GetMapping("/merchant")
    public JsonResponse<SalaryMerchantInfoResult> merchantInfo(@RequestParam(value = "tenant_id") Long tenantId) {
        return JsonResponse.success(manageService.merchantInfo(tenantId));
    }

    /**
     * 启用｜关闭薪酬云能力
     *
     * @param param 参数
     * @return 成功
     */
    @PostMapping("/switch")
    public JsonResponse<Boolean> switchTenant(@Validated @RequestBody SwitchTenantParam param) {
        return JsonResponse.success(manageService.switchRolePermission(param));
    }


    /**
     * 新增|修改商户信息（变更信息时需校验使用场景）
     *
     * @param param 入参
     * @return 返回商户信息
     */
    @PostMapping("/merchant")
    public JsonResponse<SalaryMerchantInfoResult> saveMerchantInfo(@Validated @RequestBody SalaryMerchantInfoParam param) {
        return JsonResponse.success(manageService.saveMerchantInfo(param));
    }


    /**
     * 查询商户余额
     *
     * @param tenantId 租户id
     * @return 商户信息
     */
    @GetMapping("/merchant/balance")
    public JsonResponse<SalaryMerchantInfoResult> findMerchantBalance(@RequestParam(value = "tenant_id") Long tenantId) {
        return JsonResponse.success(manageService.findMerchantBalance(tenantId));
    }


    /**
     * 商户流水操作
     *
     * @param param 操作流水
     * @return 最新操作流水
     */
    @PostMapping("/operate/merchant/flow")
    public JsonResponse<SalaryMerchantFlowResult> operateMerchantFlow(@Validated @RequestBody SalaryOperateMerchantFlowParam param) {
        return JsonResponse.success(manageService.operateMerchantFlow(param));
    }

    /**
     * 根据租户查询收款主体和子账户信息
     *
     * @return 结果
     */
    @GetMapping("/subject/subAccount")
    public JsonResponse<List<SubjectAndSubAccountResult>> subjectAndSubAccountInfo(@RequestParam(name = "tenant_id") Integer tenantId) {
        return JsonResponse.success(manageService.subjectAndSubAccountInfo(tenantId));
    }


    /**
     * 查询发薪户主体信息
     */
    @GetMapping("/payer/subject")
    public JsonResponse<List<SubjectInfoResult>> payerSubject() {
        return JsonResponse.success(manageService.payerSubject());
    }

    /**
     * 商务合同
     */
    @GetMapping("/business/contract")
    public JsonResponse<List<BusinessContractResult>> businessContract(@RequestParam(name = "tenant_id") Integer tenantId) {
        return JsonResponse.success(manageService.businessContract(tenantId));
    }


    /**
     * 合同项目
     */
    @GetMapping("/contract/project")
    public JsonResponse<List<ContractProjectResult>> contractProject(@RequestParam(name = "business_contract_id") Long businessContractId) {
        return JsonResponse.success(manageService.contractProject(businessContractId));
    }

    /**
     * 查询签约云基本信息
     *
     * @return 签约云基本信息
     */
    @GetMapping("/sign")
    public JsonResponse<SignInfoResult> info(@RequestParam(name = "tenant_id") Long tenantId) {
        return JsonResponse.success(manageService.info(tenantId));
    }

    /**
     * 新增｜保存 签约云基本信息
     *
     * @param param 签约云信息
     * @return 签约云基本信息
     */
    @PostMapping("/sign")
    public JsonResponse<SignInfoResult> saveInfo(@Validated @RequestBody SignInfoParam param) {
        return JsonResponse.success(manageService.saveInfo(param));
    }
}
