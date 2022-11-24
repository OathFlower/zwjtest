package cn.xunhou.xbbcloud.middleware.rocket.pojo;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 发薪dto
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class XcyPayrollMessageDto {

    @NotNull(message = "来源id不能为空")
    private Long sourceId;
    /**
     * 员工姓名
     */
    @NotBlank(message = "员工姓名不能为空")
    private String name;
    /**
     * 发薪金额
     */
    @NotNull(message = "金额不能为空")
    private BigDecimal money;
    /**
     * 身份证
     */
    @NotBlank(message = "身份证不能为空")
    private String idCardNo;
    /**
     *
     */
    @NotBlank(message = "openId不能为空")
    private String openId;
}
