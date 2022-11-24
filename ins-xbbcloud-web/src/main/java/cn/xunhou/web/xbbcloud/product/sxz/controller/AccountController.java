package cn.xunhou.web.xbbcloud.product.sxz.controller;

import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.config.ServiceInstanceDiscovery;
import cn.xunhou.web.xbbcloud.config.WxConfig;
import cn.xunhou.web.xbbcloud.config.xhrpc.XhRpcComponent;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumProject;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumXhTenant;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhR;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhRpcParam;
import cn.xunhou.web.xbbcloud.config.xhrpc.r.XhTotal;
import cn.xunhou.web.xbbcloud.product.hrm.result.CustomerResult;
import cn.xunhou.web.xbbcloud.product.hrm.result.ProjectPositionResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.JwtResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.RecommendResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.SmsAccountResult;
import cn.xunhou.web.xbbcloud.product.sxz.param.LoginParam;
import cn.xunhou.web.xbbcloud.product.sxz.param.SendSmsVerifyParam;
import cn.xunhou.web.xbbcloud.product.sxz.service.AccountService;
import cn.xunhou.web.xbbcloud.product.sxz.service.UserService;
import cn.xunhou.web.xbbcloud.product.sxz.service.WxPayService;
import cn.xunhou.web.xbbcloud.util.HttpUtils;
import cn.xunhou.web.xbbcloud.util.WechatPay2ValidatorForRequest;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.shade.org.apache.commons.codec.digest.DigestUtils;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 省薪招-账号相关
 */
@RequestMapping("/accounts")
@Slf4j
@RestController
public class AccountController {

    @Resource
    private XhRpcComponent xhRpcComponent;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;

    RestTemplate restTemplate = new RestTemplate();
    @Resource
    private WxConfig wxConfig;
    @Resource
    private ServiceInstanceDiscovery serviceInstanceDiscovery;

    /**
     * 微信分享  签名
     *
     * @param reqJson
     * @return
     */

    @PostMapping("/wechat/share/sign")
    public JsonResponse<JSONObject> getWXSign(@RequestBody JSONObject reqJson) {
        String url = (String) reqJson.get("url");
        long timeStampSec = System.currentTimeMillis() / 1000;
        String timestamp = String.format("%010d", timeStampSec);
        String nonceStr = getRandomStr(8);
        String[] urls = url.split("#");
        String newUrl = urls[0];
        JSONObject respJson = new JSONObject();
        String ticket = getWXJsapiTicket(getWXaccessToken());
        String[] signArr = new String[]{"url=" + newUrl, "jsapi_ticket=" + ticket, "noncestr=" + nonceStr, "timestamp=" + timestamp};
        Arrays.sort(signArr);
        String signStr = StringUtils.join(signArr, "&");
        String resSign = DigestUtils.sha1Hex(signStr);
        respJson.put("appId", wxConfig.getAppId());
        respJson.put("timestamp", timestamp);
        respJson.put("nonceStr", nonceStr);
        respJson.put("signature", resSign);
        respJson.put("ticket", ticket);

        return JsonResponse.success(respJson);
    }


    /**
     * 入参为token，返回ticket
     *
     * @param token
     * @return
     */
    public String getWXJsapiTicket(String token) {
        String ticket = null;
        if (StringUtils.isBlank(ticket)) {
            String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + token + "&type=jsapi";
            String resp = restTemplate.getForObject(url, String.class);
            JSONObject resJson = JSONObject.parseObject(resp);
            return resJson.getString("ticket");
        }
        return ticket;
    }


    /**
     * 获取token
     *
     * @return
     */

    public String getWXaccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + wxConfig.getAppId() + "&secret=" + wxConfig.getSecret();
        String resp = restTemplate.getForObject(url, String.class);
        JSONObject resJson = JSONObject.parseObject(resp);
        return resJson.getString("access_token");
    }


    /**
     * 获取随机数
     *
     * @param length
     * @return
     */

    public String getRandomStr(int length) {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int randomNum;
        char randomChar;
        Random random = new Random();
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < length; i++) {
            randomNum = random.nextInt(base.length());
            randomChar = base.charAt(randomNum);
            str.append(randomChar);
        }
        return str.toString();
    }

    /**
     * 登录/注册
     *
     * @param param
     * @return
     */
    @PostMapping("/login")
    public JsonResponse<JwtResult> login(@RequestBody @Validated LoginParam param) {
        return accountService.login(param);
    }

    @PostMapping("/abc")
    public JsonResponse<?> abc(HttpServletRequest request) throws Exception {
//        String balanceInstance = serviceInstanceDiscovery.getBalanceInstance("ins-hrostaff-platform");
//        String url = String.format("%s/RPC/IStaffService/listStaff", balanceInstance);
//        log.info("abc url = {}", url);
//        Map<String, Object> params = new HashMap<>();
//        params.put("currentUserId", 0);
//        params.put("clientId", 70047);
//        Map<String, Object> params2 = new HashMap<>();
//        Map<String, Object> params3 = new HashMap<>();
//        params3.put("pageSize", 10);
//        params3.put("currPage", 0);
//        params.put("data", JSONUtil.toJsonStr(params2));
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//        HttpRequest httpRequest2 = HttpUtil.createRequest(Method.POST, url).clearHeaders().addHeaders(headers).form(params);
//        HttpResponse httpResponse2 = httpRequest2.execute();
//        log.info("httpResponse2 = " + httpResponse2);
//        String rspBody2 = httpResponse2.body();
//        log.info("rspBody2 = " + rspBody2);
//
        XhRpcParam xhRpcParam = new XhRpcParam();

        Map<String, Object> params2 = new HashMap<>();
        Map<String, Object> params3 = new HashMap<>();
        params3.put("pageSize", 0);
        params3.put("currPage", 0);
        params2.put("queryStaffDto", params3);

        xhRpcParam.setRequest(params2)
                .setServiceProject(EnumProject.HROSTAFF)
                .setXhTenant(EnumXhTenant.XUNHOU)
                .setUri("IStaffService/listStaff");
        XhR<XhTotal<Map>> r = xhRpcComponent.sendForTotal(xhRpcParam, Map.class);
        Map<String, Object> params4 = new HashMap<>();
        params4.put("projectId", 99712);
        params4.put("tenant", "XUNHOU");


        xhRpcParam.setRequest(params4)
                .setServiceProject(EnumProject.HROSTAFF)
                .setXhTenant(EnumXhTenant.XUNHOU)
                .setUri("IProjectPositionService/getProjectPositionDtosByProjectId");
        XhR<List<ProjectPositionResult>> listXhR = xhRpcComponent.sendForList(xhRpcParam, ProjectPositionResult.class);


        Map<String, Object> params5 = new HashMap<>();
        params5.put("customerId", 2747);
        params5.put("tenant", "XUNHOU");
        xhRpcParam.setRequest(params5)
                .setServiceProject(EnumProject.USERXH)
                .setXhTenant(EnumXhTenant.XUNHOU)
                .setUri("ICustomerService/getCustomerDtoById");
        XhR<CustomerResult> send = xhRpcComponent.send(xhRpcParam, CustomerResult.class);

        return JsonResponse.success(r);
    }

    /**
     * 发送短信验证码
     *
     * @param param
     * @return
     */
    @PostMapping("/send_sms_verify")
    public JsonResponse<SmsAccountResult> sendSmsVerify(
            @RequestBody @Validated SendSmsVerifyParam param) {
        return accountService.sendSmsVerify(param);
    }


    /**
     * 根据code获取openId
     *
     * @param code
     * @return openId
     * @throws Exception
     */
    @GetMapping("/{code}/open_id")
    public JsonResponse<String> getOpenId(@PathVariable(value = "code") String code) throws Exception {
        return accountService.getOpenId(code);
    }

    @Resource
    private Verifier verifier;

    @Resource
    private WxPayService wxPayService;

    /**
     * 支付通知
     * 微信支付通过支付通知接口将用户支付成功消息通知给商户
     */
    @PostMapping("/wechat/callback")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response) {

        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();//应答对象

        try {

            //处理通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            String requestId = (String) bodyMap.get("id");
            log.info("支付通知的id ===> {}", requestId);
            log.info("支付通知的完整数据 ===> {}", body);
            //int a = 9 / 0;

            //签名的验证
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest
                    = new WechatPay2ValidatorForRequest(verifier, requestId, body);
            if (!wechatPay2ValidatorForRequest.validate(request)) {

                log.error("通知验签失败");
                //失败应答
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return gson.toJson(map);
            }
            log.info("通知验签成功");

            //处理订单
            wxPayService.processOrder(bodyMap);

            //应答超时
            //模拟接收微信端的重复通知
            //TimeUnit.SECONDS.sleep(5);

            //成功应答
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            return gson.toJson(map);

        } catch (Exception e) {
            e.printStackTrace();
            //失败应答
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "失败");
            return gson.toJson(map);
        }

    }

    /**
     * 生成邀请码
     *
     * @return
     */
    @GetMapping("/interview_code")
    public JsonResponse<List<RecommendResult>> recommendList(@RequestParam String url, @RequestParam Integer count) throws Exception {
        return accountService.initRecommendResultList(url, count);
    }
}
