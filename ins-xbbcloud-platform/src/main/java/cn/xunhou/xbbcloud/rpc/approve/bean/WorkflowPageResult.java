package cn.xunhou.xbbcloud.rpc.approve.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * @Author: chenning
 * @Date: 2022/09/29/17:13
 * @Description:
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class WorkflowPageResult {

    private Long id;
    /**
     * 申请人id
     */
    private Long applicantId;

    /**
     * 审核撤销人
     */
    private Long assigneeId;

    /**
     * 审批状态 0未发起 1审批中 2审批通过 3审批驳回 4撤销
     */
    private int status;

    /**
     * 开始时间
     */
    private Timestamp startTime;

    /**
     * 租户ID
     */
    private Long tenantId ;

    /**
     * 结束时间
     */
    private Timestamp endTime;

    /**
     * 审核时间
     */
    private Timestamp approveTime;

    /**
     * 申请时间
     */
    private Timestamp applyTime;

    /**
     * 流程模板id
     */
    private Long flowTempId;

    /**
     * 原因
     */
    private String reason;

    /**
     * 编辑目标ID
     */
    private Long editTargetId;

    /**
     * 虚拟字段
     */
    private String v0;

    /**
     * 虚拟字段
     */
    private String v1;

    /**
     * 虚拟字段
     */
    private String v2;

    /**
     * 虚拟字段
     */
    private String v3;

    /**
     * 虚拟字段
     */
    private String v4;

    /**
     * 虚拟字段
     */
    private String v5;

    /**
     * 虚拟字段
     */
    private String v6;

    /**
     * 虚拟字段
     */
    private String v7;

    /**
     * 虚拟字段
     */
    private String v8;

    /**
     * 虚拟字段
     */
    private String v9;

    /**
     * 虚拟扩展字段
     */
    private String ext;

    /**
     * 旧表单数据
     */
    private String formJson;

    private Timestamp updatedAt;

    private Integer nodeLevel;

}
