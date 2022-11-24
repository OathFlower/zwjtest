package cn.xunhou.xbbcloud.rpc.approve.bean;

import cn.xunhou.xbbcloud.rpc.approve.message.IMessage;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author: chenning
 * @Date: 2022/09/26/11:20
 * @Description:
 */
@Data
public class Context {

    /**
     * 事件
     */
    private Integer eventType;

    /**
     * 工作流模板ID
     */
    private Long flowTemplateId;
    /**
     * 实例ID
     */
    private Long instanceId;
    /**
     * 进程ID
     */
    private Long processId;
    /**
     * 节点ID
     */
    private Long nodeId;
    /**
     * 表单模板ID
     */
    private Long formTemplateId;
    /**
     * 原因
     */
    private String reason;
    /**
     * 描述
     */
    private String description;

    /**
     * 申请人
     */
    private Long applicantId;

    /**
     * 审批人
     */
    private Long assigneeId;

    /**
     * 通知人
     */
    private List<Long> noticeTo;

    /**
     * 通知电话
     */
    private List<String> noticeTels;

    private String msg;

    /**
     * 通知方式 0钉钉 1企微 2短信
     */
    private List<Integer> noticeFun;
    /**
     * 字段
     */
    private FormField formField;

    /**
     * 编辑目标ID
     */
    private Long editTargetId;

    private Map<Integer, IMessage> messageMap;

    private String smsTemplateCode;

    private String smsMsg;

    private String token;

    private String noticeContext;

    private boolean autoPass =false;
}
