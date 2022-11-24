package cn.xunhou.web.xbbcloud.product.sxz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SmsAccountResult {
    /**
     * 手机号
     */
    private String mobile;

}
