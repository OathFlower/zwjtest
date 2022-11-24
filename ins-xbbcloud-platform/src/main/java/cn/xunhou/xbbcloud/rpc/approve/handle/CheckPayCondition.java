package cn.xunhou.xbbcloud.rpc.approve.handle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.xbbcloud.rpc.approve.bean.WorkflowPageResult;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowFormRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowInstanceRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowNodeRepository;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowNodeEntity;
import cn.xunhou.xbbcloud.rpc.approve.param.WorkflowFormQueryParam;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/10/25/20:12
 * @Description:
 */
@Component
@Slf4j
public class CheckPayCondition implements  Condition{

    @Autowired
    private WorkflowFormRepository workflowFormRepository;
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    @Autowired
    private WorkflowNodeRepository workflowNodeRepository;
    @Override
    public boolean doConditionCheck(Object obj) {
        if (obj == null){
            return true;
        }
        WorkflowNodeEntity entity = (WorkflowNodeEntity)obj;
        WorkflowFormQueryParam param = new WorkflowFormQueryParam();
        param.setInsId(entity.getInsId());
        PagePojoList<WorkflowPageResult> pagePojo = workflowFormRepository.findList(param);
        List<WorkflowPageResult> data = pagePojo.getData();
        if (CollectionUtils.isEmpty(data)) {
            return true;
        }
        WorkflowPageResult first = CollUtil.getFirst(data);
        String ext = first.getExt();
        JSONObject jsonObject = JSON.parseObject(ext);
        // 是内部 直接变为通过
        log.info("条件节点数据:"+ jsonObject.get("v24"));
        return Objects.equal(1,jsonObject.get("v24"));
    }
}
