package cn.xunhou.web.xbbcloud.product.sxz.controller;

import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.sxz.dto.ConsumeRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.HasServiceRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.RechargeRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.VerificationRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.param.ConsumeRecordPageParam;
import cn.xunhou.web.xbbcloud.product.sxz.param.RechargeRecordPageParam;
import cn.xunhou.web.xbbcloud.product.sxz.param.VerificationParam;
import cn.xunhou.web.xbbcloud.product.sxz.param.VerificationRecordParam;
import cn.xunhou.web.xbbcloud.product.sxz.service.ServiceOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 省薪招-服务订单相关
 */
@RequestMapping("/api/service_orders")
@Slf4j
@RestController
public class ServiceOrderController {
    @Autowired
    private ServiceOrderService serviceOrderService;

    /**
     * 兑换服务
     *
     * @param productId
     * @return
     */
    @PostMapping("/{productId}")
    public JsonResponse saveServiceOrder(@PathVariable(value = "productId") Long productId) {
        return serviceOrderService.saveServiceOrder(productId);
    }

    /**
     * 核销服务
     *
     * @param param
     * @return
     */
    @PostMapping("/write_off")
    public JsonResponse writeOff(@RequestBody VerificationParam param) {
        return serviceOrderService.writeOff(param);
    }

    /**
     * 已兑换服务 H5
     *
     * @return
     */
    @GetMapping("/records")
    public JsonResponse<List<HasServiceRecordResult>> hasServiceRecordH5() {
        return serviceOrderService.hasServiceRecord(null);
    }

    /**
     * 已兑换服务 PC
     *
     * @return
     */
    @GetMapping("/{user_id}/records")
    public JsonResponse<List<HasServiceRecordResult>> hasServiceRecordPC(@PathVariable(value = "user_id") Long userId) {
        return serviceOrderService.hasServiceRecord(userId);
    }

    /**
     * 充值记录
     *
     * @param param
     * @return
     */
    @GetMapping("/recharge/records")
    public JsonListResponse<RechargeRecordResult> rechargeRecord(@Validated RechargeRecordPageParam param) {
        return serviceOrderService.rechargeRecord(param);
    }


    /**
     * 消费记录
     *
     * @param param
     * @return
     */
    @GetMapping("/consume/records")
    public JsonListResponse<ConsumeRecordResult> consumeRecord(@Validated ConsumeRecordPageParam param) {
        return serviceOrderService.consumeRecord(param);
    }


    /**
     * 核销记录
     *
     * @param param
     * @return
     */
    @GetMapping("/write_off/records")
    public JsonListResponse<VerificationRecordResult> writeOffRecord(VerificationRecordParam param) {
        return serviceOrderService.writeOffRecord(param);
    }
}
