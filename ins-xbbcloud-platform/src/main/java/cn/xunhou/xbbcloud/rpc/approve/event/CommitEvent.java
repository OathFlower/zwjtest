package cn.xunhou.xbbcloud.rpc.approve.event;

import cn.xunhou.cloud.redis.lock.IRedisLockService;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.xbbcloud.common.constants.RedisConstant;
import cn.xunhou.xbbcloud.common.enums.EnumEventType;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.middleware.rocket.producer.RocketMsgService;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowFormEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowNodeEntity;
import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import cn.xunhou.xbbcloud.rpc.approve.handle.HandleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: chenning
 * @Date: 2022/09/27/14:06
 * @Description: 修改保存提交审核
 */
@Slf4j
@Component
public class CommitEvent extends AbstractEvent {
    @Autowired
    private RocketMsgService rocketMsgService;
    @Autowired
    private IRedisLockService redisLockService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public void beforeTransit(Context ctx) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transit(Context ctx) {
        Integer currentStatus = getCurrentInstance(ctx).getStatus();

        if (!(EnumState.REJECT.getCode().equals(currentStatus) ||EnumState.REVOKE.getCode().equals(currentStatus))){
            log.info(String.format("当前状态%s,无法编辑",EnumState.getEnum(currentStatus).getMessage()));
            throw GrpcException.asRuntimeException("该申请状态已经发生变更，请退出刷新列表确认最新状态");
        }
        WorkflowFormEntity formEntity = getWorkflowFormRepository().findByInsId(ctx.getInstanceId());
        if (formEntity == null){
            throw GrpcException.asRuntimeException("没有找到表单");
        }
        WorkflowFormEntity update = new WorkflowFormEntity();
        update.setId(formEntity.getId());
        BeanUtils.copyProperties(ctx.getFormField(),update);
        getWorkflowFormRepository().updateById(formEntity.getId(),update);
        //修改实例状态
        WorkflowInstanceEntity instanceEntity = new WorkflowInstanceEntity();
        instanceEntity.setStatus(EnumState.TODO.getCode());
        getWorkflowInstanceRepository().updateById(ctx.getInstanceId(),instanceEntity);
        // 将拒绝的节点转换为待审批
        List<WorkflowNodeEntity> nodeEntities =
                getWorkflowNodeRepository().findByInsId(ctx.getInstanceId());
        nodeEntities.stream().filter(v->EnumState.REJECT.getCode().equals(v.getStatus())).forEach(v->{
            v.setStatus(EnumState.TODO.getCode());
            getWorkflowNodeRepository().updateById(v.getId(),v);
        });
        // 防重令牌(防止表单重复提交)
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(RedisConstant.WORKFLOW_INSTANCE_TODO_VERSION_KEY+ctx.getInstanceId()
                ,token);
    }

    @Override
    public void postTransit(Context ctx) {
        String[] beanNames = getHandleEvent(ctx);
        if (beanNames == null){
            return;
        }
        for (String beanName : beanNames){
            HandleEvent bean = (HandleEvent) SpringContextUtil.getBean(beanName);
            bean.editHandle(ctx);
        }
    }

    @Override
    public Integer getType() {
        return EnumEventType.COMMIT.getCode();
    }
}
