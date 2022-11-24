package cn.xunhou.web.xbbcloud.product.sxz.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbEntity;
import lombok.Data;


/**
 * 发票
 */
@Data
@XbbTable(table = "receipt")
public class ReceiptEntity extends XbbEntity {
    /**
     * 客户表id
     */
    private Long customerId;
    /**
     * 用户表id
     */
    private Long userId;
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
