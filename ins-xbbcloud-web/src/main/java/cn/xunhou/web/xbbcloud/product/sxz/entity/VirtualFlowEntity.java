package cn.xunhou.web.xbbcloud.product.sxz.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbEntity;
import lombok.Data;

@Data
@XbbTable(table = "virtual_flow")
public class VirtualFlowEntity extends XbbEntity {
    /**
     * 客户表id
     */
    private Long customerId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 状态 0充值 1消费
     */
    private Integer flowType;
    /**
     * 业务id
     */
    private Long objectId;

    /**
     * 虚拟币
     */
    private Integer coin;

}
