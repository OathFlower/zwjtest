package cn.xunhou.web.xbbcloud.product.sxz.param;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * 登录入参
 */
@Data
@Accessors(chain = true)
public class LoginParam {
    /**
     * 账号
     */
    @NotBlank(message = "手机号不能为空")
    private String tel;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String verifyCode;


    /**
     * 邀请码
     */
    private String interviewCode;

    /**
     * 微信登录code
     */
    private String code;

}
