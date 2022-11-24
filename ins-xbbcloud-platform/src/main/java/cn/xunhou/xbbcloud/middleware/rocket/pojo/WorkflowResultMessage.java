package cn.xunhou.xbbcloud.middleware.rocket.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.Map;

/**
 * @Author: chenning
 * @Date: 2022/10/08/11:33
 * @Description:
 */
@Data
@ToString
public class WorkflowResultMessage {

    /**
     * 创建完成的ID
     */
    private Long editTargetId;

    /**
     * 工作流id
     */
    private Long insId;

    private String ext;
}
