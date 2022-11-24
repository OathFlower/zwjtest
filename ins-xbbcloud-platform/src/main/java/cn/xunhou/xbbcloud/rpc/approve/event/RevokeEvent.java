package cn.xunhou.xbbcloud.rpc.approve.event;

import cn.hutool.core.date.DateUtil;
import cn.xunhou.xbbcloud.common.constants.RedisConstant;
import cn.xunhou.xbbcloud.common.enums.EnumEventType;
import cn.xunhou.xbbcloud.common.enums.EnumRunStatus;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowNodeEntity;
import cn.xunhou.xbbcloud.rpc.approve.param.WorkflowNodeQueryParam;
import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: chenning
 * @Date: 2022/09/26/13:21
 * @Description:
 */
@Component
@Slf4j
public class RevokeEvent extends AbstractEvent {
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public void beforeTransit(Context ctx) {
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transit(Context ctx) {
        Integer currentStatus = getCurrentInstance(ctx).getStatus();

        if (!(EnumState.TODO.getCode().equals(currentStatus) || EnumState.REJECT.getCode().equals(currentStatus))){
            log.info(String.format("当前状态%s,无法撤销",EnumState.getEnum(currentStatus).getMessage()));
            throw GrpcException.asRuntimeException("当前状态无法撤销");
        }
        //将实例状态转换为撤销
        WorkflowInstanceEntity instanceEntity = new WorkflowInstanceEntity();
        instanceEntity.setStatus(EnumState.REVOKE.getCode());
        instanceEntity.setRunStatus(EnumRunStatus.HANG.getCode());
        instanceEntity.setAssigneeId(ctx.getAssigneeId());
        instanceEntity.setApproveTime(DateUtil.date());
        getWorkflowInstanceRepository().updateById(ctx.getInstanceId(),instanceEntity);

        WorkflowNodeQueryParam param = new WorkflowNodeQueryParam();
        param.setInsId(ctx.getInstanceId());
        List<WorkflowNodeEntity> list = getWorkflowNodeRepository().findList(param);
        // 将所有同级节点置为结束
        for (WorkflowNodeEntity entity :list) {
            WorkflowNodeEntity update = new WorkflowNodeEntity();
            update.setRunStatus(EnumRunStatus.RUNNING.getCode());
            update.setStatus(EnumState.TODO.getCode());
            update.setApproveTime(DateUtil.date());
            workflowNodeRepository.updateById(entity.getId(), update);
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(RedisConstant.WORKFLOW_INSTANCE_TODO_VERSION_KEY+ctx.getInstanceId()
                ,token);
    }

    @Override
    public void postTransit(Context ctx) {
    }

    @Override
    public Integer getType() {
        return EnumEventType.REVOKE.getCode();
    }
}