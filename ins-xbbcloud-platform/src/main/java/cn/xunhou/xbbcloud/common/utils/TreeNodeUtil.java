package cn.xunhou.xbbcloud.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.xbbcloud.common.bean.XhTreeNode;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author wangkm
 */
public class TreeNodeUtil {
    private TreeNodeUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 把一个List转成树
     */
    public static <T> List<XhTreeNode<T>> buildTree(List<XhTreeNode<T>> list, Long pid) {
        List<XhTreeNode<T>> tree = new ArrayList<>();
        int level = 0;
        for (XhTreeNode<T> node : list) {
            node.setLevel(level);
            if (Objects.equals(node.getPid(), pid)) {
                tree.add(findChild(node, list, level + 1));
            }
        }
        return tree;
    }

    private static <T> XhTreeNode<T> findChild(XhTreeNode<T> node, List<XhTreeNode<T>> list, Integer level) {
        node.setLevel(level);
        for (XhTreeNode<T> n : list) {
            if (Objects.equals(n.getPid(), node.getId())) {
                if (node.getChildNodes() == null) {
                    node.setChildNodes(new ArrayList<>());
                }
                node.getChildNodes().add(findChild(n, list, level + 1));
            }
        }
        return node;
    }

    /**
     * 将tree压成List
     *
     * @param tree
     * @param <T>
     * @return
     */
    public static <T> List<XhTreeNode<T>> buildList(XhTreeNode<T> tree) {
        List<XhTreeNode<T>> list = Lists.newArrayList();
        list.add(tree);
        if (CollUtil.isNotEmpty(tree.getChildNodes())) {
            for (XhTreeNode<T> childNode : tree.getChildNodes()) {
                list.addAll(buildList(childNode));
            }
        }
        list.sort(Comparator.comparing(XhTreeNode::getLevel));
        return list;
    }
}
