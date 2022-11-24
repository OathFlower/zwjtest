package cn.xunhou.xbbcloud.middleware.rocket.consumer;

import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.rocketmq.AbstractXbbMessageListener;
import cn.xunhou.cloud.rocketmq.XbbCommonRocketListener;
import cn.xunhou.cloud.rocketmq.XbbMessageBuilder;
import cn.xunhou.xbbcloud.common.constants.RocketConstant;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendMessage;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.WorkflowResultMessage;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowFormRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowInstanceRepository;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowFormEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @Author: chenning
 * @Date: 2022/10/08/11:26
 * @Description: 操作结果监听
 */
@Slf4j
@XbbCommonRocketListener(tag = "WORKFLOW_CREATE_SUCCESS_TAG", applicationName = RocketConstant.APPLICATION_NAME)
public class WorkflowResultListener  extends AbstractXbbMessageListener {

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    @Autowired
    private WorkflowFormRepository workflowFormRepository;

    @Override
    public void dispose(XbbMessageBuilder.XbbMessage xbbMessage, ConsumeContext context) {

        try {
            WorkflowResultMessage message = XbbJsonUtil.fromJsonBytes(xbbMessage.getBody(), WorkflowResultMessage.class);
            log.info("处理审批结束后操作结果监听obj" + XbbJsonUtil.toJsonString(message.toString()));
            if (Objects.isNull(message.getEditTargetId()) || Objects.isNull(message.getInsId())){
                return;
            }
            if (StringUtils.isNotBlank(message.getExt())){
                WorkflowFormEntity workflowFormEntity = workflowFormRepository.findByInsId(message.getInsId());
                JSONObject jsonObject = JSON.parseObject(message.getExt());
                if (jsonObject.getString("v0") != null){
                    workflowFormEntity.setV0(jsonObject.getString("v0"));
                }
                workflowFormRepository.updateById(workflowFormEntity.getId(),workflowFormEntity);
            }
            workflowInstanceRepository.updateById(message.getInsId(),new WorkflowInstanceEntity().setEditTargetId(message.getEditTargetId()));
        } catch (Exception e) {
            log.info("处理审批结束后操作结果失败",e);
        }
    }
}
