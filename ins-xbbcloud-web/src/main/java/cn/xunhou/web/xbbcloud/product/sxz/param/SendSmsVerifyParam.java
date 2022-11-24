package cn.xunhou.web.xbbcloud.product.sxz.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;


/**
 * 验证码入参
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SendSmsVerifyParam {
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;


    /**
     * 验证码
     */
    private Integer code;
}
