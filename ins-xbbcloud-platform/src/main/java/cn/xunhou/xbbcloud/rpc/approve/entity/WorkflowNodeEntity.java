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
@XbbTable(table = "workflow_node")
public class WorkflowNodeEntity extends XbbSnowTimeTenantEntity {


    /**
     * 审批实例表id
     */
    private Long insId;

    /**
     * 节点名称
     */
    private String nodeTitle;

    /**
     * 签署类型 1会签，2. 或签
     */
    private Integer signType;

    /**
     * 审批模式 1人工，2自动通过，3自动拒绝
     */
    private Integer approveMode;

    /**
     * 节点类型 1. 审批人，2.抄送人，3. 办理人，4条件分支
     */
    private Integer nodeType;

    /**
     * 审核人id
     */
    private Long assigneeId;

    /**
     * 审批状态 0未发起 10审批中 20审批通过 30审批驳回
     */
    private Integer status;

    /**
     * 节点状态 0未发起 1运行中 2挂起 3已结束
     */
    private Integer runStatus;

    /**
     * 审核时间
     */
    private Date approveTime;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 原因
     */
    private String reason;

    /**
     * 描述
     */
    private String description;

    /**
     * 节点优先级 0为初始节点
     */
    private Integer nodeLevel;

    /**
     * 上一个节点 初始节点为0
     */
    private Integer preNode;

    /**
     * 下一个节点 末节点为0表示结束
     */
    private Integer nextNode;

    /**
     * 子节点
     */
    private Integer subNodeLevel;

}
