package cn.xunhou.web.xbbcloud.product.sxz.entity;


import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbEntity;
import lombok.Data;

@Data
@XbbTable(table = "`order`")
public class OrderEntity extends XbbEntity {
    /**
     * 订单标题
     */
    private String title;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 支付产品id
     */
    private Long productId;
    /**
     * 发票id
     */
    private Long receiptId;

    /**
     * 应付金额(分)
     */
    private Integer payableFee;


    /**
     * 实付金额(分)
     */
    private Integer paymentFee;

    /**
     * 预支付号
     */
    private String prepayId;


    /**
     * 订单状态
     */
    private String wxStatus;
}
