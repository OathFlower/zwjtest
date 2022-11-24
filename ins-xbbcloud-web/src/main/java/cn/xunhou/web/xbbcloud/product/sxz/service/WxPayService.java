package cn.xunhou.web.xbbcloud.product.sxz.service;


import cn.xunhou.cloud.core.context.UserParam;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.util.SystemUtil;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.cloud.redis.lock.IRedisLockService;
import cn.xunhou.web.xbbcloud.config.WxConfig;
import cn.xunhou.web.xbbcloud.config.WxPayConfig;
import cn.xunhou.web.xbbcloud.product.sxz.entity.OrderEntity;
import cn.xunhou.web.xbbcloud.product.sxz.enums.WxApiEnum;
import cn.xunhou.web.xbbcloud.product.sxz.enums.WxCallbackEnum;
import cn.xunhou.web.xbbcloud.product.sxz.enums.WxTradeStateEnum;
import cn.xunhou.web.xbbcloud.util.WechatPay2ValidatorForRequest;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class WxPayService {

    @Resource
    private WxPayConfig wxPayConfig;

    @Resource
    private CloseableHttpClient wxPayClient;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private UserService userService;


    @Autowired
    private IRedisLockService redisLockService;

    @Autowired
    private RedissonClient redissonClient;

    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();
    @Resource
    private WxConfig wxConfig;

    /**
     * 创建订单
     *
     * @param productId
     * @return 签名信息 和 订单号，预支付订单号
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public JsonResponse<Map<String, Object>> jsapiPay(Long productId, String openId) throws Exception {
        UserParam userParam = XBB_USER_CONTEXT.get();
        log.info("生成订单");

        //生成订单
        OrderEntity orderInfo = orderInfoService.createOrderByProductId(userParam.getUserId(), productId);



        log.info("调用统一下单API");

        //调用统一下单API
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiEnum.JSAPI_PAY.getType()));

        // 请求body参数
        Gson gson = new Gson();
        Map paramsMap = new HashMap();
        paramsMap.put("appid", wxConfig.getAppId());
        paramsMap.put("mchid", wxConfig.getMchId());
        paramsMap.put("description", orderInfo.getTitle());
        paramsMap.put("out_trade_no", orderInfo.getId().toString());
        if (SystemUtil.isOffline()) {
            paramsMap.put("notify_url", wxPayConfig.getQaNotifyDomain().concat(WxCallbackEnum.JSAPI_NOTIFY.getType()));
        } else {
            paramsMap.put("notify_url", wxPayConfig.getOnlineNotifyDomain().concat(WxCallbackEnum.JSAPI_NOTIFY.getType()));
        }


        Map amountMap = new HashMap();
        amountMap.put("total", orderInfo.getPaymentFee());
        amountMap.put("currency", "CNY");
        paramsMap.put("amount", amountMap);
        Map payerMap = new HashMap();
        payerMap.put("openid", openId);
        paramsMap.put("payer", payerMap);

        //将参数转换成json字符串
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数 ===> {}" + jsonParams);

        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());//响应体
            int statusCode = response.getStatusLine().getStatusCode();//响应状态码
            if (statusCode == 200) { //处理成功
                log.info("成功, 返回结果 = " + bodyAsString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功");
            } else {
                log.info("下单失败,响应码 = " + statusCode + ",返回结果 = " + bodyAsString);
                return JsonResponse.systemError("下单失败,响应码 = " + statusCode + ",返回结果 = " + bodyAsString);
            }

            //响应结果
            Map<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
            //预支付id
            String prepayId = resultMap.get("prepay_id");

            //保存预支付id
            Long orderId = orderInfo.getId();
            orderInfoService.savePrepayId(orderId, prepayId);

            //返回签名参数 前端用来调起支付

            return JsonResponse.success(jsapiSign(prepayId));

        } finally {
            response.close();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException {
        log.info("处理订单");

        //解密报文
        String plainText = decryptFromResource(bodyMap);

        //将明文转换成map
        Gson gson = new Gson();
        HashMap plainTextMap = gson.fromJson(plainText, HashMap.class);
        Long orderId = Long.valueOf(plainTextMap.get("out_trade_no").toString());


        /*在对业务数据进行状态检查和处理之前，
        要采用数据锁进行并发控制，
        以避免函数重入造成的数据混乱*/
        //尝试获取锁：
        // 成功获取则立即返回true，获取失败则立即返回false。不必一直等待锁的释放
        String lockKey = orderId.toString();

        RLock lock = null;


        try {
            lock = redissonClient.getLock(lockKey);
            //加锁
            if (!redisLockService.tryLock(lockKey, TimeUnit.SECONDS, 10, 5)) {
                return;
            }
            //处理重复的通知
            //接口调用的幂等性：无论接口被调用多少次，产生的结果是一致的。
            String orderStatus = orderInfoService.getOrderStatus(orderId);
            if (!WxTradeStateEnum.NOTPAY.getType().equals(orderStatus)) {
                return;
            }

        /*    //模拟通知并发
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            //更新订单状态
            orderInfoService.updateStatusByOrderId(orderId, WxTradeStateEnum.SUCCESS);

            //记录支付日志
            paymentInfoService.createPaymentInfo(plainText);

            //增加班点余额
            userService.updateCoin(plainText);


        } finally {
            //要主动释放锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }


    /**
     * 对称解密
     *
     * @param bodyMap
     * @return
     */
    private String decryptFromResource(Map<String, Object> bodyMap) throws GeneralSecurityException {

        log.info("密文解密");

        //通知数据
        Map<String, String> resourceMap = (Map) bodyMap.get("resource");
        //数据密文
        String ciphertext = resourceMap.get("ciphertext");
        //随机串
        String nonce = resourceMap.get("nonce");
        //附加数据
        String associatedData = resourceMap.get("associated_data");

        log.info("密文 ===> {}", ciphertext);
        AesUtil aesUtil = new AesUtil(wxConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        String plainText = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8),
                ciphertext);

        log.info("明文 ===> {}", plainText);

        return plainText;
    }


    public Map<String, Object> jsapiSign(String prepayIdStr) {
        String signType = "RSA";
        String appId = wxConfig.getAppId();
        String nonceStr = WechatPay2ValidatorForRequest.generateNonceStr();
        long timeStamp = WechatPay2ValidatorForRequest.getCurrentTimestamp();
        String prepayId = "prepay_id=" + prepayIdStr;
        String paySign = null;
        String signatureStr = Stream.of(appId, String.valueOf(timeStamp), nonceStr, prepayId)
                .collect(Collectors.joining("\n", "", "\n"));
        try {
            paySign = WechatPay2ValidatorForRequest.getSign(signatureStr, wxPayConfig.getPrivateKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("appId", appId);
        returnMap.put("timeStamp", timeStamp);
        returnMap.put("nonceStr", nonceStr);
        returnMap.put("package", prepayId);
        returnMap.put("signType", signType);
        returnMap.put("paySign", paySign);
        return returnMap;
    }

    public String queryOrder(Long orderId) throws Exception {

        log.info("查单接口调用 ===> {}", orderId);

        String url = String.format(WxApiEnum.ORDER_QUERY_BY_NO.getType(), orderId);
        url = wxPayConfig.getDomain().concat(url).concat("?mchid=").concat(wxConfig.getMchId());

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpGet);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());//响应体
            int statusCode = response.getStatusLine().getStatusCode();//响应状态码
            if (statusCode == 200) { //处理成功
                log.info("成功, 返回结果 = " + bodyAsString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功");
            } else {
                log.info("查单接口调用,响应码 = " + statusCode + ",返回结果 = " + bodyAsString);
                throw new IOException("request failed");
            }

            return bodyAsString;

        } finally {
            response.close();
        }

    }


    /**
     * 关单接口的调用
     *
     * @param orderId
     */
    private void closeOrder(Long orderId) throws Exception {

        log.info("关单接口的调用，订单号 ===> {}", orderId);

        //创建远程请求对象
        String url = String.format(WxApiEnum.CLOSE_ORDER_BY_NO.getType(), orderId);
        url = wxPayConfig.getDomain().concat(url);
        HttpPost httpPost = new HttpPost(url);

        //组装json请求体
        Gson gson = new Gson();
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("mchid", wxConfig.getMchId());
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数 ===> {}", jsonParams);

        //将请求参数设置到请求对象中
        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        try {
            int statusCode = response.getStatusLine().getStatusCode();//响应状态码
            if (statusCode == 200) { //处理成功
                log.info("成功200");
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功204");
            } else {
                log.info("Native下单失败,响应码 = " + statusCode);
                throw new IOException("request failed");
            }

        } finally {
            response.close();
        }
    }

    /**
     * 根据订单号查询微信支付查单接口，核实订单状态
     * 如果订单已支付，则更新商户端订单状态，并记录支付日志
     * 如果订单未支付，则调用关单接口关闭订单，并更新商户端订单状态
     *
     * @param orderId
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkOrderStatus(Long orderId) throws Exception {

        log.warn("根据订单号核实订单状态 ===> {}", orderId);

        //调用微信支付查单接口
        String result = this.queryOrder(orderId);

        Gson gson = new Gson();
        Map<String, String> resultMap = gson.fromJson(result, HashMap.class);

        //获取微信支付端的订单状态
        String tradeState = resultMap.get("trade_state");

        //判断订单状态
        if (WxTradeStateEnum.SUCCESS.getType().equals(tradeState)) {

            log.warn("核实订单已支付 ===> {}", orderId);

            //如果确认订单已支付则更新本地订单状态
            orderInfoService.updateStatusByOrderId(orderId, WxTradeStateEnum.SUCCESS);
            //记录支付日志
            paymentInfoService.createPaymentInfo(result);
        }

        if (WxTradeStateEnum.NOTPAY.getType().equals(tradeState)) {
            log.warn("核实订单未支付 ===> {}", orderId);

            //如果订单未支付，则调用关单接口
            this.closeOrder(orderId);

            //更新本地订单状态
            orderInfoService.updateStatusByOrderId(orderId, WxTradeStateEnum.CLOSED);
        }

    }
}
