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
 * @Date: 2022/09/27/16:06
 * @Description:
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "workflow_instances")
public class WorkflowInstanceEntity extends XbbSnowTimeTenantEntity {



    /**
     * 流编号
     */
    private String flowNo;

    /**
     * 审批实例名称
     */
    private String flowTitle;

    /**
     * 流程模板id
     */
    private Long flowTempId;

    /**
     * 申请人id
     */
    private Long applicantId;

    /**
     * 审核撤销人
     */
    private Long assigneeId;

    /**
     * 编辑目标ID
     */
    private Long editTargetId;

    /**
     * 审批状态 0未发起 10审批中 20审批通过 30审批驳回 40撤销
     */
    private Integer status;

    /**
     * 实例状态 1运行中 2挂起 3已结束
     */
    private Integer runStatus;

    /**
     * 来源 0自有系统 1钉钉 2企微
     */
    private Integer source;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 审核时间
     */
    private Date approveTime;

    /**
     * 申请时间
     */
    private Date applyTime;

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
     * 质保时间
     */
    private Date qualityDate;
}
