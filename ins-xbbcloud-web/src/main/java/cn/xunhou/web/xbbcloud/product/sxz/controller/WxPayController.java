package cn.xunhou.web.xbbcloud.product.sxz.controller;

import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.sxz.service.WxPayService;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;


/**
 * 省薪招-支付相关
 */
@CrossOrigin //跨域
@RestController
@RequestMapping("/api/wx_pay")
@Slf4j
public class WxPayController {

    @Resource
    private WxPayService wxPayService;

    @Resource
    private Verifier verifier;


    /**
     * jsapi前端支付
     *
     * @param productId
     * @return
     * @throws Exception
     */
    @PostMapping("/{productId}/front_pay/{open_id}")
    public JsonResponse<Map<String, Object>> nativePay(@PathVariable Long productId, @PathVariable(value = "open_id") String openId) throws Exception {

        log.info("发起支付请求 v3");

        //返回支付二维码连接和订单号
        return wxPayService.jsapiPay(productId, openId);
    }



}
