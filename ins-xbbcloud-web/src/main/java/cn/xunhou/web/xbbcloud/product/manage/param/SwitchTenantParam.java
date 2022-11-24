package cn.xunhou.web.xbbcloud.product.manage.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@ToString
@Accessors(chain = true)
public class SwitchTenantParam {
    /**
     * 租户id
     */
    @NotNull(message = "租户id不能为空")
    private Long tenantId;
    /**
     * 是否启用 1启用 2不启用
     */
    @NotNull(message = "是否启用不能为空")
    private Integer isUse;

    /**
     * 类型
     * 薪酬云：1
     * 签约云：2
     */
    @NotNull(message = "类型不能为空")
    private Integer type;
}
