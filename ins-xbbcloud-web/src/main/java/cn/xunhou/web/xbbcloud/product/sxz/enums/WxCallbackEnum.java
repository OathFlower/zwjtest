package cn.xunhou.web.xbbcloud.product.sxz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WxCallbackEnum {

    /**
     * 支付通知
     */
    JSAPI_NOTIFY("/accounts/wechat/callback");


    /**
     * 类型
     */
    private final String type;
}
