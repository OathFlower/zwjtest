package cn.xunhou.xbbcloud.rpc.approve.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeEntity;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: chenning
 * @Date: 2022/09/27/16:21
 * @Description:
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "workflow_process")
public class WorkflowProcessEntity extends XbbSnowTimeTenantEntity {

    /**
     * 审批实例表id
     */
    private Long insId;

    /**
     * 当前运行到的节点id
     */
    private Long nodeId;

    /**
     * 审核时间
     */
    private Date approveTime;

    /**
     * 审核人id
     */
    private Long assigneeId;

    /**
     * 审批状态 10审批中 20审批通过 30审批驳回
     */
    private Integer status;

    /**
     * 节点状态 1运行中 2挂起 3已结束
     */
    private Integer runStatus;

    /**
     * 节点等级
     */
    private Integer nodeLevel;

    private Integer nodeType;

    private Long flowTempId;
}
