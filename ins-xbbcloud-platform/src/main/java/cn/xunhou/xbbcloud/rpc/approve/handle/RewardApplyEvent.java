package cn.xunhou.xbbcloud.rpc.approve.handle;

import cn.xunhou.cloud.rocketmq.XbbCustomRocketTemplate;
import cn.xunhou.xbbcloud.common.constants.WorkflowConstant;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendMessage;
import cn.xunhou.xbbcloud.middleware.rocket.producer.RocketMsgService;
import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowInstanceRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowNodeRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowProcessRepository;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowProcessEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenning
 * @Date: 2022/10/25/21:19
 * @Description:
 */
@Component
@Slf4j
public class RewardApplyEvent extends AbstractHandleEvent {
    @Autowired
    private RocketMsgService rocketMsgService;
    @Autowired
    private WorkflowProcessRepository workflowProcessRepository;
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    @Override
     public void addHandle(Context ctx) {
//        SendMessage sendMessage = buildSendMessage(ctx);
//        rocketMsgService.sendNotice(sendMessage);
    }

    @Override
    public void passHandle(Context ctx) {
//        // 设置当前数据属于的审批等级
//        List<WorkflowProcessEntity> processEntities = workflowProcessRepository.findByInsId(ctx.getInstanceId(), null);
//        WorkflowInstanceEntity instanceEntity = workflowInstanceRepository.findById(ctx.getInstanceId(), WorkflowInstanceEntity.class);
//        processEntities = processEntities.stream().filter(v-> WorkflowConstant.APPROVER.equals(v.getNodeType()))
//                .collect(Collectors.toList());
//        int nodeLevel = processEntities.size();
//        if (EnumState.PASS.getCode().equals(instanceEntity.getStatus())){
//            return;
//        }
//        log.info(String.format("审批是%s级审批",nodeLevel));
//        if (nodeLevel == 2) {
//            SendMessage sendMessage = buildSendMessage(ctx);
//            rocketMsgService.sendNotice(sendMessage);
//        }
    }

    @Override
    public void rejectHandle(Context ctx) {

    }

    @Override
    public void editHandle(Context ctx) {

    }
}
