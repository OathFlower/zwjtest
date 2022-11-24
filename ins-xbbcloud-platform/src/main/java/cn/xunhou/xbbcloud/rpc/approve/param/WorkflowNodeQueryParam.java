package cn.xunhou.xbbcloud.rpc.approve.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/09/28/20:27
 * @Description:
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class WorkflowNodeQueryParam {

    /**
     * 实例ID
     */
    private Long insId;

    /**
     * 节点等级
     */
    private Integer nodeLevel;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 节点等级
     */
    private Integer allLevel;

    /**
     * 实例Id
     */
    private List<Long> insIds;

    /**
     * nodeIds
     */
    private List<Long> ids;
}
