package cn.xunhou.xbbcloud.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class XhTreeNode<T> {
    /**
     * 节点数据
     */
    private T sourceNode;
    /**
     * id
     */
    private Long id;
    /**
     * 父节点id
     */
    private Long pid;
    /**
     * 深度
     */
    private Integer level;

    /**
     * 子节点
     */
    private List<XhTreeNode<T>> childNodes;
}
