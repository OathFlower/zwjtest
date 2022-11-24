package cn.xunhou.web.xbbcloud.product.sign.controller;

import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.grpc.proto.xbbcloud.SignServerGrpc;
import cn.xunhou.web.xbbcloud.config.xhrpc.XhRpcComponent;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumProject;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumXhTenant;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhR;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhRpcParam;
import cn.xunhou.web.xbbcloud.product.sign.enums.EnumXbbSignType;
import cn.xunhou.web.xbbcloud.product.sign.enums.EnumXbbSigner;
import cn.xunhou.web.xbbcloud.product.sign.param.*;
import cn.xunhou.web.xbbcloud.product.sign.result.*;
import cn.xunhou.web.xbbcloud.product.sign.service.SignService;
import com.google.common.collect.Lists;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 签约云相关
 *
 * @author wkm
 */
@RestController
@RequestMapping("/api/signs")
@Validated
public class SignController {
    @ModelAttribute
    public void check(HttpServletRequest request, HttpServletResponse response) {
        //下列接口不校验商户号签约云功能开启和关闭
        List<String> notFilter = Lists.newArrayList(

        );
        String uri = request.getRequestURI();
        if (!notFilter.contains(uri)) {
            signService.checkIsUse();
        }
    }

    @Resource
    private SignService signService;
    @Resource
    private XhRpcComponent xhRpcComponent;
    @GrpcClient("ins-xbbcloud-platform")
    private SignServerGrpc.SignServerBlockingStub signServerBlockingStub;

    /**
     * 岗位下拉
     *
     * @return
     */
    @RequestMapping("/position/select")
    public JsonResponse<List<PostAndProjectResult>> positionSelect() {
        return signService.postSelectByHro();
    }

    /**
     * 条件分页查询合同
     *
     * @param param
     * @return
     */
    @RequestMapping("/contracts")
    public JsonListResponse<ContractResult> contractList(@Validated @RequestBody QueryContractListParam param) {
        return signService.contractList(param);
    }

    /**
     * 条件分页查询劳务发薪岗位二维码列表
     *
     * @param param
     * @return
     */
    @RequestMapping("/position/qrcodes")
    public JsonListResponse<PositionQrcodeResult> positionQrcodes(@Validated @RequestBody PositionQrcodePageParam param) {
        return signService.positionQrcodes(param);
    }

    /**
     * 劳务发薪岗位二维码详情
     *
     * @param id
     * @return
     */
    @RequestMapping("/position/qrcode/detail")
    public JsonResponse<PositionQrcodeResult> qrcodeDetail(@RequestParam(name = "id") Long id) {
        return signService.qrcodeDetail(id);
    }

    /**
     * 客户模板suggest
     *
     * @param customerId           客户id
     * @param contractTemplateType 合同模板类型
     * @param templateType         类型 1合同 2协议
     * @return
     */
    @RequestMapping("/template/suggest")
    public JsonResponse<List<TextValueResult>> templateSuggest(@RequestParam(name = "customer_id") Long customerId, @RequestParam(name = "contract_template_type", required = false) Integer contractTemplateType, @RequestParam(name = "template_type") Integer templateType) {
        return signService.templateSuggest(customerId, contractTemplateType, templateType);
    }

    /**
     * 批量生成合同动态表单
     *
     * @param param
     * @return
     */
    @PostMapping("/init/dynamicforms")
    public JsonResponse<List<ContractDynamicResult>> batchGetContractDynamicForm(@Validated @RequestBody DynamicFormsParam param) {

        List<ContractDynamicQueryParam> dynamicQueryForms = JSONUtil.toList(param.getContractDynamicQueryFormListJson(), ContractDynamicQueryParam.class);
        if (CollectionUtils.isEmpty(dynamicQueryForms)) {
            return JsonResponse.success();
        }
        List<GetContractDynamicParam> getContractDynamicParamList = new ArrayList<>();
        for (ContractDynamicQueryParam dynamicQueryForm : dynamicQueryForms) {
            if (dynamicQueryForm.getTemplateId() == null) {
                continue;
            }
            GetContractDynamicParam getContractDynamicParam = new GetContractDynamicParam();
            getContractDynamicParam.setTemplateId(dynamicQueryForm.getTemplateId());
            getContractDynamicParam.setSignType(EnumXbbSignType.valueOfCode(dynamicQueryForm.getXbbSignType()));
            getContractDynamicParam.setXbbSigner(EnumXbbSigner.FIRST_PARTY);
            getContractDynamicParam.setContractSubjectId(param.getSubjectId());
            getContractDynamicParam.setIsSaasFlag(true);
            getContractDynamicParamList.add(getContractDynamicParam);
        }

        XhRpcParam xhRpcParam = new XhRpcParam();
        Map<String, Object> getContractDynamicParams = new HashMap<>();
        getContractDynamicParams.put("contractDynamicQueryDtos", getContractDynamicParamList);

        xhRpcParam.setRequest(getContractDynamicParams)
                .setServiceProject(EnumProject.XBB)
                .setXhTenant(EnumXhTenant.XUNHOU)
                .setUri("IUsercXbbContractService/batchGetContractDynamicDto");

        XhR<List<ContractDynamicResult>> listXhR = xhRpcComponent.sendForList(xhRpcParam, ContractDynamicResult.class);
        return JsonResponse.success(listXhR.getData());


    }
    /**
     * 保存岗位二维码
     *
     * @param param 入参
     * @return
     */
    @PostMapping("/position/qrcode")
    public JsonResponse savePositionQrcode(@Validated @RequestBody SavePositionQrcodeParam param) {
        return signService.savePositionQrcode(param);
    }


    /**
     * 查询签约云基本信息
     * @return 签约云基本信息
     */
    @GetMapping("/info")
    public JsonResponse<SignInfoResult> info(){
        return JsonResponse.success(signService.info(null));
    }
}
