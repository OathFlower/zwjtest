package cn.xunhou.web.xbbcloud.product.sxz.entity;


import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbEntity;
import lombok.Data;

@Data
@XbbTable(table = "service_order")
public class ServiceOrderEntity extends XbbEntity {
    /**
     * 订单标题
     */
    private String title;

    /**
     * 客户表id
     */
    private Long customerId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 支付产品id
     */
    private Long productId;
    /**
     * 虚拟币
     */
    private Integer coin;

    /**
     * 订单状态  未使用0 核销1
     */
    private Integer status;


    /**
     * 服务类型  购买服务0 赠送服务1
     */
    private Integer serviceType;
    /**
     * 操作人
     */
    private Long operatorId;
    /**
     * 使用企业
     */
    private String customerName;
    /**
     * 备注
     */
    private String remark;
}
