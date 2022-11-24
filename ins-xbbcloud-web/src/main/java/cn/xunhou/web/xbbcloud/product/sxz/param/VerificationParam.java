package cn.xunhou.web.xbbcloud.product.sxz.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 核销入参
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class VerificationParam {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 服务产品id
     */
    private Long productId;

    /**
     * 使用企业
     */
    private String customerName;
    /**
     * 备注
     */
    private String remark;

}
