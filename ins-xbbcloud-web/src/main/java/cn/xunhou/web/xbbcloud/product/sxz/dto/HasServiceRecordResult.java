package cn.xunhou.web.xbbcloud.product.sxz.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * 服务订单
 */
@ToString
@Data
@Accessors(chain = true)
public class HasServiceRecordResult {
    /**
     * 产品名称
     */
    private String title;


    /**
     * 产品id
     */
    private Long productId;


    /**
     * 数量
     */
    private Long count;
}
