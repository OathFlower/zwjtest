package cn.xunhou.xbbcloud.middleware.rocket.pojo;

import cn.xunhou.xbbcloud.rpc.approve.bean.FormField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Author: chenning
 * @Date: 2022/10/08/10:59
 * @Description:
 */
@Data
@ToString
@NoArgsConstructor
public class WorkflowFormMessage implements Serializable {
    /**
     * 审批实例表id
     */
    private Long insId;

    /**
     * 表单json串
     */
    private FormField formField;

    /**
     * 目标ID
     */
    private Long editTargetId;
    /**
     * 用户ID
     */
    private Long applicantId;
    /**
     * 模板ID
     */
    private Long flowTempId;
    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 操作人
     */
    private Long handler;

    private Integer nodeLevel;

}
