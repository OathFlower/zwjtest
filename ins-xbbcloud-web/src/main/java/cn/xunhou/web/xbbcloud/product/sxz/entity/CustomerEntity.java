package cn.xunhou.web.xbbcloud.product.sxz.entity;


import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbEntity;
import lombok.Data;

/**
 * 客户表
 */
@Data
@XbbTable(table = "customer")
public class CustomerEntity extends XbbEntity {

    /**
     * 公司名称
     */
    private String customerName;
    /**
     * 公司税号
     */
    private String taxNo;
    /**
     * 地址
     */
    private String address;
    /**
     * 操作人员id
     */
    private Long operatorId;
    /**
     * 余额
     */
    private Integer coin;

}
