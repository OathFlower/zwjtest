package cn.xunhou.xbbcloud.rpc.approve.event;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.cloud.redis.lock.IRedisLockService;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.xbbcloud.common.constants.RedisConstant;
import cn.xunhou.xbbcloud.common.constants.WorkflowConstant;
import cn.xunhou.xbbcloud.common.enums.EnumEventType;
import cn.xunhou.xbbcloud.common.enums.EnumRunStatus;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.middleware.rocket.producer.RocketMsgService;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowNodeEntity;
import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import cn.xunhou.xbbcloud.rpc.approve.handle.HandleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: chenning
 * @Date: 2022/09/26/11:44
 * @Description: 提交审核
 */
@Slf4j
@Component
public class FirstCommitEvent extends AbstractEvent {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RocketMsgService rocketMsgService;
    @Override
    public void beforeTransit(Context ctx) {

    }

    /**
     * 状态流转
     * @param ctx
     */
    @Transactional(rollbackFor = Exception.class)
    public void transit(Context ctx) {
        WorkflowInstanceEntity currentInstance = getCurrentInstance(ctx);
        Integer currentStatus = currentInstance.getStatus();
        if (!EnumState.NOT_APPLY.getCode().equals(currentStatus)){
            log.info(String.format("当前状态%s,无法转换到目标状态%s",EnumState.getEnum(currentStatus).getMessage(),
                    EnumState.TODO.getMessage()));
            throw GrpcException.asRuntimeException("该申请状态已经发生变更，请退出刷新列表确认最新状态");
        }

        // 将工作流状态变更为待审核
        WorkflowInstanceEntity instanceEntity = new WorkflowInstanceEntity();
        if (ctx.getEditTargetId() != null ){
            instanceEntity.setEditTargetId(ctx.getEditTargetId());
        }
        instanceEntity.setStatus(EnumState.TODO.getCode());

        getWorkflowInstanceRepository().updateById(ctx.getInstanceId(),instanceEntity);

        // 获取第一个节点id
        List<WorkflowNodeEntity> nodeEntities = getWorkflowNodeRepository().findByInsId(ctx.getInstanceId());
        List<WorkflowNodeEntity> initNode = nodeEntities.stream().filter(v -> v.getNodeLevel() == 0).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(initNode)){
            throw GrpcException.asRuntimeException("没有查找到初始节点");
        }
        WorkflowNodeEntity first = CollUtil.getFirst(initNode);
        // 查看一级是否是审批节点
        List<WorkflowNodeEntity> firstNodes = nodeEntities.stream().filter(v -> v.getNodeLevel() == 1).collect(Collectors.toList());
        IAssert.notEmpty(firstNodes,"没有查找到一级节点");
        // 如果一级节点不是审批节点进行 逻辑
        if (!WorkflowConstant.APPROVER.equals(firstNodes.get(0).getNodeType())){
            // todo
        }
        // 创建进程节点
        insertProcess(ctx,first,EnumRunStatus.RUNNING.getCode(), EnumState.TODO.getCode());
        // 将level1的节点转为待审批
        nodeEntities.forEach(v->{
            v.setStatus(EnumState.TODO.getCode());
            getWorkflowNodeRepository().updateById(v.getId(),v);
        });

        // 发送MQ
        rocketMsgService.sendAddMsg(buildFormMessage(currentInstance,ctx));
        // 防重令牌(防止表单重复提交)
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(RedisConstant.WORKFLOW_INSTANCE_TODO_VERSION_KEY+ctx.getInstanceId()
                ,token);
    }

    /**
     * 后置处理
     * @param ctx
     */
    @Override
    public void postTransit(Context ctx) {
        String[] beanNames = getHandleEvent(ctx);
        if (beanNames == null){
            return;
        }
        for (String beanName : beanNames){
            HandleEvent bean = (HandleEvent)SpringContextUtil.getBean(beanName);
            bean.addHandle(ctx);
        }
    }

    @Override
    public Integer getType() {
        return EnumEventType.FIRST_COMMIT.getCode();
    }
}
