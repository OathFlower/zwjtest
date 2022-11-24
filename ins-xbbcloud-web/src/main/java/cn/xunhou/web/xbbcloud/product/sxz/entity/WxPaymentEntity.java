package cn.xunhou.web.xbbcloud.product.sxz.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbEntity;
import lombok.Data;

@Data
@XbbTable(table = "wx_payment")
public class WxPaymentEntity extends XbbEntity {
    /**
     * 商户支付订单编号
     */
    private Long orderId;//商品订单编号

    private String transactionId;//支付系统交易编号

    private String paymentType;//支付类型

    private String tradeType;//交易类型

    private String tradeState;//交易状态

    private Integer payerTotal;//支付金额(分)

    private String content;//通知参数
}
