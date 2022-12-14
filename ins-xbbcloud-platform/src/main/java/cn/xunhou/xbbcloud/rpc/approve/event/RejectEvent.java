package cn.xunhou.xbbcloud.rpc.approve.event;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.xbbcloud.common.constants.RedisConstant;
import cn.xunhou.xbbcloud.common.enums.EnumEventType;
import cn.xunhou.xbbcloud.common.enums.EnumRunStatus;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.middleware.rocket.producer.RocketMsgService;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowNodeRepository;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowNodeEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowProcessEntity;
import cn.xunhou.xbbcloud.rpc.approve.param.WorkflowNodeQueryParam;
import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import cn.xunhou.xbbcloud.rpc.approve.handle.HandleEvent;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenning
 * @Date: 2022/09/26/13:20
 * @Description:
 */

@Slf4j
@Component
public class RejectEvent extends AbstractEvent {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RocketMsgService rocketMsgService;
    @Override
    public void beforeTransit(Context ctx) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transit(Context ctx) {
        Object redisToken = redisTemplate.opsForValue().get(RedisConstant.WORKFLOW_INSTANCE_TODO_VERSION_KEY + ctx.getInstanceId());
        if (redisToken == null || !Objects.equal(redisToken, ctx.getToken())) {
            throw GrpcException.asRuntimeException("???????????????????????????????????????????????????????????????????????????");
        }
        WorkflowInstanceEntity currentInstance = getCurrentInstance(ctx);
        Integer currentStatus = getCurrentInstance(ctx).getStatus();


        if (!EnumState.TODO.getCode().equals(currentStatus)) {
            log.info(String.format("????????????%s,???????????????????????????%s", EnumState.getEnum(currentStatus).getMessage(),
                    EnumState.REJECT.getMessage()));
            throw GrpcException.asRuntimeException("???????????????????????????????????????????????????????????????????????????");
        }

        // ??????????????????????????????
        WorkflowProcessEntity process = getProcess(ctx);
        Long preNode = process.getNodeId();
        // ??????????????????????????????
        WorkflowNodeRepository workflowNodeRepository = getWorkflowNodeRepository();
        WorkflowNodeEntity byId = getWorkflowNodeRepository().findById(preNode, WorkflowNodeEntity.class);

        List<WorkflowNodeEntity> handlerNodes = getWorkflowNodeRepository().findByAssigneeId(ctx.getInstanceId(), ctx.getAssigneeId(), byId.getNodeLevel() + 1);
        // ??????????????????????????????
        if (CollectionUtils.isEmpty(handlerNodes)){
            log.info("??????????????????????????????"+ctx.getAssigneeId());
            WorkflowNodeEntity entity = handlerIsExist(ctx, byId.getNodeLevel() + 1, ctx.getAssigneeId());
            if (entity!= null){
                handlerNodes.add(entity);
            }else {
                throw GrpcException.asRuntimeException("???????????????????????????");
            }
        }
        // ????????????
        WorkflowNodeEntity nodeEntity = CollUtil.getFirst(handlerNodes);
        // ????????????
        if (!EnumState.TODO.getCode().equals(nodeEntity.getStatus())) {
            log.info(String.format("????????????%s,???????????????????????????%s", EnumState.getEnum(currentStatus).getMessage(),
                    EnumState.REJECT.getMessage()));
            throw GrpcException.asRuntimeException("???????????????????????????????????????????????????????????????????????????");
        }
        if (ctx.getFlowTemplateId() != 1006) {
            // ???????????????
            WorkflowNodeQueryParam param = new WorkflowNodeQueryParam();
            param.setInsId(ctx.getInstanceId());
            param.setNodeLevel(nodeEntity.getNodeLevel());
            List<WorkflowNodeEntity> list = getWorkflowNodeRepository().findList(param);
            // ?????????????????????????????????
            for (WorkflowNodeEntity entity : list) {
                WorkflowNodeEntity update = new WorkflowNodeEntity();
                update.setRunStatus(EnumRunStatus.HANG.getCode());
                update.setStatus(EnumState.REJECT.getCode());
                update.setApproveTime(DateUtil.date());
                workflowNodeRepository.updateById(entity.getId(), update);
            }
            //??????????????????????????????
            WorkflowInstanceEntity instanceEntity = new WorkflowInstanceEntity();
            instanceEntity.setStatus(EnumState.REJECT.getCode());
            instanceEntity.setApproveTime(DateUtil.date());
            instanceEntity.setRunStatus(EnumRunStatus.HANG.getCode());
            instanceEntity.setReason(ctx.getReason());
            instanceEntity.setAssigneeId(ctx.getAssigneeId());
            getWorkflowInstanceRepository().updateById(ctx.getInstanceId(), instanceEntity);

            // ??????????????????????????????
            insertProcess(ctx, nodeEntity, EnumRunStatus.OVER.getCode(), EnumState.REJECT.getCode());
        } else {
            // ???????????????
            WorkflowNodeQueryParam param = new WorkflowNodeQueryParam();
            param.setInsId(ctx.getInstanceId());
            param.setNodeLevel(nodeEntity.getNodeLevel());

            List<WorkflowNodeEntity> list = getWorkflowNodeRepository().findList(param);
            List<WorkflowNodeEntity> rejectNode = list.stream().filter(v -> Objects.equal(v.getAssigneeId(), ctx.getAssigneeId())).collect(Collectors.toList());
            // ????????????????????????
            list.removeAll(rejectNode);
            // ?????????????????????????????????
            List<Long> ids = list.stream().map(WorkflowNodeEntity::getId).collect(Collectors.toList());
            // ???????????????
            WorkflowNodeEntity rejectUpdate = new WorkflowNodeEntity();
            rejectUpdate.setRunStatus(EnumRunStatus.OVER.getCode());
            rejectUpdate.setStatus(EnumState.REJECT.getCode());
            rejectUpdate.setApproveTime(DateUtil.date());
            workflowNodeRepository.updateById(CollUtil.getFirst(rejectNode).getId(), rejectUpdate);
            // ?????????????????????????????????
            for (WorkflowNodeEntity entity : list) {
                WorkflowNodeEntity update = new WorkflowNodeEntity();
                update.setRunStatus(EnumRunStatus.OVER.getCode());
                update.setStatus(EnumState.REJECT.getCode());
                update.setApproveTime(DateUtil.date());
                workflowNodeRepository.updateById(entity.getId(), update);
            }

            //??????????????????????????????
            WorkflowInstanceEntity instanceEntity = new WorkflowInstanceEntity();
            instanceEntity.setStatus(EnumState.REJECT.getCode());
            instanceEntity.setApproveTime(DateUtil.date());
            instanceEntity.setRunStatus(EnumRunStatus.OVER.getCode());
            instanceEntity.setReason(ctx.getReason());
            instanceEntity.setAssigneeId(ctx.getAssigneeId());
            getWorkflowInstanceRepository().updateById(ctx.getInstanceId(), instanceEntity);

            // ??????????????? ????????????????????????
            getWorkflowProcessRepository()
                    .updateById(process.getId(), new WorkflowProcessEntity().setRunStatus(EnumRunStatus.OVER.getCode()));
            // ??????????????????
            insertProcess(ctx, nodeEntity, EnumRunStatus.OVER.getCode(), EnumState.REJECT.getCode());

        }
        // ??????MQ
        rocketMsgService.sendRejectMsg(buildFormMessage(currentInstance,ctx));
        // ?????????????????? ????????????
        redisTemplate.delete(RedisConstant.WORKFLOW_INSTANCE_TODO_VERSION_KEY + ctx.getInstanceId());
    }

    @Override
    public void postTransit(Context ctx) {
        String[] beanNames = getHandleEvent(ctx);
        if (beanNames == null) {
            return;
        }
        for (String beanName : beanNames) {
            HandleEvent bean = (HandleEvent) SpringContextUtil.getBean(beanName);
            bean.rejectHandle(ctx);
        }
    }

    @Override
    public Integer getType() {
        return EnumEventType.AUDIT_REJECT.getCode();
    }

}
