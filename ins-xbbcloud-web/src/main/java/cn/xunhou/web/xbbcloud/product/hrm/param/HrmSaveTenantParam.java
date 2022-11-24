package cn.xunhou.web.xbbcloud.product.hrm.param;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @program: ins-xhportal-web
 * @description: MenusPageListParam
 * @author: jiang_tian
 * @create: 2022-08-04 15:42
 **/
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class HrmSaveTenantParam {

    private Long id;
    /**
     * 公司名称
     */
    @NotBlank
    private String company;
    /**
     * logo
     */
    private String logo = "";
    /**
     * 域名
     */
    private String domain = "";
    /**
     * 描述
     */
    private String description = "";
    /**
     * 0启用 1禁用
     */
    private Integer status = 0;
    /**
     * 部门编码
     */
    private String tenantNumber = "";
    /**
     * 手机号码
     */
    @NotBlank
    private String mobile;

    private Integer productId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否创建主账号
     */
    private Boolean isCreateAccount = true;
    /**
     * 是否创建root级部门
     */
    private Boolean isCreateRootOrg = true;

}
