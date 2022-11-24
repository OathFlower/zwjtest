package cn.xunhou.web.xbbcloud.config;


import cn.xunhou.common.tools.util.ZKClientHelper;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Map;


@Slf4j
@Data
@Configuration
@DependsOn("springContextUtil")
public class WxConfiguration {
    private static final String WX_PAY_CONFIG = "/config/public/common/mobile/xbbcloud/wxpay/shxp";
    private static final String WX_OPEN_CONFIG = "/config/public/common/mobile/xbbcloud/wxopen/shxh";

    @Bean
    public WxConfig wxConfig() {

        Map<String, Object> wxPayMap = ZKClientHelper.getData4Map(WX_PAY_CONFIG);
        Map<String, Object> wxOpenMap = ZKClientHelper.getData4Map(WX_OPEN_CONFIG);
        log.info("读取Zookeeper的wxPayMap配置信息" + JSONObject.toJSONString(wxPayMap));
        log.info("读取Zookeeper的wxOpenMap配置信息" + JSONObject.toJSONString(wxOpenMap));
        WxConfig wxConfig = new WxConfig();
        wxConfig.setPrivateKey(wxPayMap.get("privateKey").toString());
        wxConfig.setMchId(wxPayMap.get("mchId").toString());
        wxConfig.setApiV3Key(wxPayMap.get("apiV3Key").toString());
        wxConfig.setMchSerialNo(wxPayMap.get("mchSerialNo").toString());
        wxConfig.setAppId(wxOpenMap.get("appId").toString());
        wxConfig.setSecret(wxOpenMap.get("secret").toString());
        return wxConfig;
    }

}
