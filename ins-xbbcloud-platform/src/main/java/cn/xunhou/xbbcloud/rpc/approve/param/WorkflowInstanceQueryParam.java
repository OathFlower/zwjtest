package cn.xunhou.xbbcloud.rpc.approve.param;

import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: chenning
 * @Date: 2022/09/28/20:27
 * @Description:
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class WorkflowInstanceQueryParam extends PageInfo {


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
     * 审批状态 0未发起 1审批中 2审批通过 3审批驳回 4撤销
     */
    private int status;

    /**
     * 实例状态 1运行中 2挂起 3已结束
     */
    private int runStatus;


    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;



}
