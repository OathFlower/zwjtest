package cn.xunhou.web.xbbcloud.product.sxz.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 保存发票入参
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SaveReceiptParam {
    /**
     * 要开具发表的订单id
     */
    private List<Long> orderIds;

    /**
     * 公司名称
     */
    private String customerName;
    /**
     * 公司税号
     */
    private String taxNo;
    /**
     * 邮寄地址
     */
    private String address;

    /**
     * 开票金额
     */
    private Integer totalFee;

}
