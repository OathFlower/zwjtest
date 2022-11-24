package cn.xunhou.xbbcloud.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.xbbcloud.common.bean.XhTreeNode;
import cn.xunhou.xbbcloud.rpc.salary.entity.FundDispatchingEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.SalaryConvert;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

@Slf4j
public class TreeNodeUtilTest {

    @Test
    public void buildTree() {
        FundDispatchingEntity f0 = new FundDispatchingEntity();
        f0.setId(1L);

        FundDispatchingEntity f1 = new FundDispatchingEntity();
        f1.setId(2L);
        f1.setParentId(1L);

        FundDispatchingEntity f2 = new FundDispatchingEntity();
        f2.setId(3L);
        f2.setParentId(2L);

        List<FundDispatchingEntity> list = Lists.newArrayList();
        list.add(f1);
        list.add(f2);
        list.add(f0);
        List<XhTreeNode<FundDispatchingEntity>> nodeList = Lists.newArrayList();
        for (FundDispatchingEntity fundDispatching : list) {
            nodeList.add(SalaryConvert.entity2Node(fundDispatching));
        }

        List<XhTreeNode<FundDispatchingEntity>> treeList = TreeNodeUtil.buildTree(nodeList, null);
        log.info("r = {}", JSON.toJSONString(treeList));
        XhTreeNode<FundDispatchingEntity> tree = CollUtil.getFirst(treeList);
        List<XhTreeNode<FundDispatchingEntity>> treeNodes = TreeNodeUtil.buildList(tree);
        log.info("list = {}", JSON.toJSONString(treeNodes));
    }

}