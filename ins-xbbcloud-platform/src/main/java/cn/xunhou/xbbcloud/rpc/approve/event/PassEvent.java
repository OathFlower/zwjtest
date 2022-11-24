package cn.xunhou.xbbcloud.rpc.approve.event;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.grpc.proto.portal.PortalServiceGrpc;
import cn.xunhou.xbbcloud.common.constants.RedisConstant;
import cn.xunhou.xbbcloud.common.constants.WorkflowConstant;
import cn.xunhou.xbbcloud.common.enums.EnumEventType;
import cn.xunhou.xbbcloud.common.enums.EnumRunStatus;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.middleware.rocket.producer.RocketMsgService;
import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import cn.xunhou.xbbcloud.rpc.approve.config.TemplateConfig;
import cn.xunhou.xbbcloud.rpc.approve.config.WorkflowTemplate;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowFormEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowNodeEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowProcessEntity;
import cn.xunhou.xbbcloud.rpc.approve.handle.Condition;
import cn.xunhou.xbbcloud.rpc.approve.handle.HandleEvent;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author: chenning
 * @Date: 2022/09/26/13:20
 * @Description: 通过事件
 */
@Slf4j
@Component
public class PassEvent extends AbstractEvent {

    @Autowired
    private RocketMsgService rocketMsgService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TemplateConfig templateConfig;

    @Override
    public void beforeTransit(Context ctx) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transit(Context ctx) {
        Object redisToken = redisTemplate.opsForValue().get(RedisConstant.WORKFLOW_INSTANCE_TODO_VERSION_KEY + ctx.getInstanceId());
        if ((redisToken == null || !Objects.equal(redisToken,ctx.getToken())) && !ctx.isAutoPass() ){
            throw GrpcException.asRuntimeException("该申请状态已经发生变更，请退出刷新列表确认最新状态");
        }
        WorkflowInstanceEntity currentInstance = getCurrentInstance(ctx);
        Integer currentStatus = currentInstance.getStatus();
        if (!EnumState.TODO.getCode().equals(currentStatus) && !ctx.isAutoPass()) {
            log.info(String.format("当前状态%s,无法转换到目标状态%s", EnumState.getEnum(currentStatus).getMessage(),
                    EnumState.PASS.getMessage()));
            throw GrpcException.asRuntimeException("该申请状态已经发生变更，请退出刷新列表确认最新状态");
        }
        // 如果是自动通过需要 更新表单
        if (ctx.isAutoPass()){
            WorkflowFormEntity formEntity = getWorkflowFormRepository().findByInsId(ctx.getInstanceId());
            IAssert.notNull(formEntity,"没有找到表单");
            WorkflowFormEntity update = new WorkflowFormEntity();
            update.setId(formEntity.getId());
            BeanUtils.copyProperties(ctx.getFormField(),update);
            getWorkflowFormRepository().updateById(formEntity.getId(),update);
            List<WorkflowProcessEntity> processEntities = workflowProcessRepository.findByInsId(ctx.getInstanceId(), EnumRunStatus.RUNNING.getCode());
            // 如果为空 是从新建直接通过 需要增加过程节点
            if (CollectionUtils.isEmpty(processEntities)){
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
            }
        }
        // 查看目前进行到的进程
        WorkflowProcessEntity process = getProcess(ctx);
        Long preNode = process.getNodeId();
        WorkflowNodeEntity byId = getWorkflowNodeRepository().findById(preNode, WorkflowNodeEntity.class);

        // 根据审批人查看到节点
        List<WorkflowNodeEntity> handlerNodes = getWorkflowNodeRepository().findByAssigneeId(ctx.getInstanceId(), ctx.getAssigneeId(), byId.getNodeLevel() + 1);
        // 不存在的话去再次查询
        if (CollectionUtils.isEmpty(handlerNodes)){
            log.info("该审核人没有权限审批"+ctx.getAssigneeId());
            // accountId
            WorkflowNodeEntity entity = null;
            if (ctx.getAssigneeId().toString().length() > 5) {
                 entity = handlerIsExist(ctx, byId.getNodeLevel() + 1, ctx.getAssigneeId());
            }else {
                // xhId 不效验
                handlerNodes.add(buildWorkflowNodeEntity(ctx,byId.getNodeLevel() + 1, ctx.getAssigneeId()));
            }
            if (entity!= null){
                handlerNodes.add(entity);
            }else if (ctx.isAutoPass()){
                handlerNodes.add(buildWorkflowNodeEntity(ctx,byId.getNodeLevel() + 1,WorkflowConstant.SYSTEM_HANDLE_ID));
            }else if (ctx.getAssigneeId().toString().length() > 5){
                throw GrpcException.asRuntimeException("该用户没有权限审批");
            }
        }

        // 检查状态
        WorkflowNodeEntity handlerNode = CollUtil.getFirst(handlerNodes);
        if (!EnumState.TODO.getCode().equals(currentStatus) && !ctx.isAutoPass()) {
            log.info(String.format("当前状态%s,无法转换到目标状态%s", EnumState.getEnum(currentStatus).getMessage(),
                    EnumState.PASS.getMessage()));
            throw GrpcException.asRuntimeException("该申请状态已经发生变更，请退出刷新列表确认最新状态");
        }
        // 查看是否或签还是会签
//        if (nodeEntity.getSignType() == 2){
        // 更新节点表
        List<WorkflowNodeEntity> allNodes = getWorkflowNodeRepository().findByInsId(ctx.getInstanceId());
        List<WorkflowNodeEntity> list = allNodes.stream().filter(v -> v.getNodeLevel().equals(handlerNode.getNodeLevel())).collect(Collectors.toList());

        // 查看是否有下一个节点，如果没有结束实例
        if (handlerNode.getNextNode() == 0) {
            overInstance(ctx,process,handlerNode,allNodes);
        } else {
            updateNode(ctx,process,handlerNode,allNodes,currentInstance);
        }
        // 发送MQ
        rocketMsgService.sendPassMsg(buildFormMessage(currentInstance,ctx));
    }

    private WorkflowNodeEntity buildWorkflowNodeEntity(Context ctx, int level, long handleId) {
        WorkflowTemplate.Node node = getNodeFromTemplate(ctx, level);
        WorkflowNodeEntity nodeEntity = new WorkflowNodeEntity();
        nodeEntity.setInsId(ctx.getInstanceId());
        nodeEntity.setNodeType(node.getNodeType());
        nodeEntity.setPreNode(node.getPreNode());
        nodeEntity.setNextNode(node.getNextNode());
        nodeEntity.setSignType(node.getSignType());
        nodeEntity.setStatus(EnumState.TODO.getCode());
        nodeEntity.setRunStatus(EnumRunStatus.RUNNING.getCode());
        nodeEntity.setNodeTitle(node.getNodeName());
        nodeEntity.setNodeLevel(node.getLevel());
        nodeEntity.setAssigneeId(handleId);
        nodeEntity.setApproveMode(node.getApproveType());
        long id = (long)workflowNodeRepository.insert(nodeEntity);
        nodeEntity.setId(id);
        return nodeEntity;
    }

    private void updateNode(Context ctx, WorkflowProcessEntity process, WorkflowNodeEntity handlerNode,  List<WorkflowNodeEntity> allNodes, WorkflowInstanceEntity currentInstance) {
        List<WorkflowNodeEntity> sameLevelNode = allNodes.stream().filter(v -> v.getNodeLevel().equals(handlerNode.getNodeLevel())).collect(Collectors.toList());

        // 防重令牌(防止表单重复提交)
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(RedisConstant.WORKFLOW_INSTANCE_TODO_VERSION_KEY+ctx.getInstanceId()
                ,token);
        // 更新进程表 将原来的状态结束
        getWorkflowProcessRepository()
                .updateById(process.getId(), new WorkflowProcessEntity().setRunStatus(EnumRunStatus.OVER.getCode()));
        // 插入进程表
        insertProcess(ctx, handlerNode, EnumRunStatus.RUNNING.getCode(), EnumState.PASS.getCode());
        // 更新当前节点
        handlerNode.setRunStatus(EnumRunStatus.OVER.getCode());
        handlerNode.setStatus(EnumState.PASS.getCode());
        handlerNode.setApproveTime(DateUtil.date());
        workflowNodeRepository.updateById(handlerNode.getId(), handlerNode);
        // 删除当前同级节点数据
        sameLevelNode.remove(handlerNode);
        // 将所有同级节点删除
        List<Long> sameLevelIds = sameLevelNode.stream().filter(v->v.getNodeLevel().equals(handlerNode.getNodeLevel()))
                .map(WorkflowNodeEntity::getId).collect(Collectors.toList());
        log.info("删除的节点"+sameLevelIds);
        if (!CollectionUtils.isEmpty(sameLevelIds)){
            workflowNodeRepository.deleteByIds(sameLevelIds);
        }
        List<WorkflowNodeEntity> nodes = allNodes.stream().filter(v -> v.getNodeLevel() == handlerNode.getNodeLevel() + 1)
                .collect(Collectors.toList());
        IAssert.notEmpty(nodes,"没有找到下一级节点");
        WorkflowNodeEntity nextNode = nodes.get(0);
        if (nodes.size() == 1 && WorkflowConstant.CONDITION.equals(nodes.get(0).getNodeType())){

            WorkflowTemplate template = templateConfig.getTemplateById(currentInstance.getFlowTempId());
            List<WorkflowTemplate.Node> node = template.getNode();
            List<WorkflowTemplate.Node> nodeList = node.stream().filter(v -> v.getLevel().equals(nextNode.getNodeLevel()) ).collect(Collectors.toList());
            WorkflowTemplate.Node conditionNode = nodeList.get(0);

            String conditionName = conditionNode.getCondition();
            Condition condition = (Condition) SpringContextUtil.getBean(conditionName);
            boolean result = condition.doConditionCheck(nextNode);
            log.info("条件分支结果:"+result);
            // 满足走trueNode
            if (result){
                // 表示结束
                if (conditionNode.getNextTrueSubNode() == 0){
                    // 更新实例为通过
                    WorkflowInstanceEntity instanceEntity = new WorkflowInstanceEntity();
                    instanceEntity.setStatus(EnumState.PASS.getCode());
                    instanceEntity.setRunStatus(EnumRunStatus.OVER.getCode());
                    instanceEntity.setAssigneeId(ctx.getAssigneeId());
                    instanceEntity.setApproveTime(DateUtil.date());
                    getWorkflowInstanceRepository().updateById(ctx.getInstanceId(), instanceEntity);
                    // 更新条件节点
                    nextNode.setRunStatus(EnumRunStatus.OVER.getCode());
                    nextNode.setStatus(EnumState.PASS.getCode());
                    nextNode.setApproveTime(DateUtil.date());
                    workflowNodeRepository.updateById(nextNode.getId(), nextNode);
                    // 将下级节点删除
                    List<WorkflowNodeEntity> allNextNode = allNodes.stream()
                            .filter(v->v.getNodeLevel().equals(nextNode.getNodeLevel() + 1))
                            .collect(Collectors.toList());
                    List<Long> ids = allNextNode.stream().map(WorkflowNodeEntity::getId).collect(Collectors.toList());
                    log.info("删除的节点"+ids);
                    if (!CollectionUtils.isEmpty(ids)){
                        workflowNodeRepository.deleteByIds(ids);
                    }
                    // 更新进程表 将原来的状态结束
                    // 查看目前进行到的进程
                    getWorkflowProcessRepository()
                            .updateById(handlerNode.getId(), new WorkflowProcessEntity().setRunStatus(EnumRunStatus.OVER.getCode()));
                    // 插入进程表
                    insertProcess(ctx, nextNode, EnumRunStatus.OVER.getCode(), EnumState.PASS.getCode());
                    // 令牌验证通过 进行删除
                    redisTemplate.delete(RedisConstant.WORKFLOW_INSTANCE_TODO_VERSION_KEY + ctx.getInstanceId());
                }else {
                    // 更新条件节点
                    nextNode.setRunStatus(EnumRunStatus.OVER.getCode());
                    nextNode.setStatus(EnumState.PASS.getCode());
                    nextNode.setApproveTime(DateUtil.date());
                    workflowNodeRepository.updateById(nextNode.getId(), nextNode);
                    // 删除根据条件判断结果的非true节点
                    List<Long> isNotTrueNodes = sameLevelNode.stream()
                            .filter(v -> Objects.equal(v.getSubNodeLevel(),conditionNode.getNextFalseSubNode()))
                            .map(WorkflowNodeEntity::getId).collect(Collectors.toList());
                    log.info("删除的节点"+isNotTrueNodes);
                    if (!CollectionUtils.isEmpty(isNotTrueNodes)){
                        workflowNodeRepository.deleteByIds(isNotTrueNodes);
                    }
                    // todo 寻找再下一级节点 判断节点类型
                    // 更新进程表 将原来的状态结束
                    // 查看目前进行到的进程
                    getWorkflowProcessRepository()
                            .updateById(handlerNode.getId(), new WorkflowProcessEntity().setRunStatus(EnumRunStatus.OVER.getCode()));
                    // 插入进程表
                    insertProcess(ctx, nextNode, EnumRunStatus.RUNNING.getCode(), EnumState.PASS.getCode());
                }
                // 不满足走FalseNode
            }else {
                // 表示结束
                if (conditionNode.getNextFalseSubNode() == 0){
                    // 更新实例为通过
                    WorkflowInstanceEntity instanceEntity = new WorkflowInstanceEntity();
                    instanceEntity.setStatus(EnumState.PASS.getCode());
                    instanceEntity.setRunStatus(EnumRunStatus.OVER.getCode());
                    instanceEntity.setAssigneeId(ctx.getAssigneeId());
                    instanceEntity.setApproveTime(DateUtil.date());
                    getWorkflowInstanceRepository().updateById(ctx.getInstanceId(), instanceEntity);
                    // 更新条件节点
                    nextNode.setRunStatus(EnumRunStatus.OVER.getCode());
                    nextNode.setStatus(EnumState.PASS.getCode());
                    nextNode.setApproveTime(DateUtil.date());
                    workflowNodeRepository.updateById(nextNode.getId(), nextNode);
                    // 将下级节点删除
                    List<WorkflowNodeEntity> allNextNode = allNodes.stream()
                            .filter(v->v.getNodeLevel().equals(nextNode.getNodeLevel() + 1))
                            .collect(Collectors.toList());
                    List<Long> ids = allNextNode.stream().map(WorkflowNodeEntity::getId).collect(Collectors.toList());
                    log.info("删除的节点"+ids);
                    if (!CollectionUtils.isEmpty(ids)){
                        workflowNodeRepository.deleteByIds(ids);
                    }
                    WorkflowProcessEntity currentProcess = getProcess(ctx);
                    getWorkflowProcessRepository()
                            .updateById(currentProcess.getId(), new WorkflowProcessEntity().setRunStatus(EnumRunStatus.OVER.getCode()));
                    // 插入进程表
                    insertProcess(ctx, nextNode, EnumRunStatus.OVER.getCode(), EnumState.PASS.getCode());
                    // 令牌验证通过 进行删除
                    redisTemplate.delete(RedisConstant.WORKFLOW_INSTANCE_TODO_VERSION_KEY + ctx.getInstanceId());
                }else {
                    // 更新条件节点
                    nextNode.setRunStatus(EnumRunStatus.OVER.getCode());
                    nextNode.setStatus(EnumState.PASS.getCode());
                    nextNode.setApproveTime(DateUtil.date());
                    workflowNodeRepository.updateById(nextNode.getId(), nextNode);
                    // 删除根据条件判断结果的非true节点
                    List<Long> isNotTrueNodes = sameLevelNode.stream()
                            .filter(v -> Objects.equal(v.getSubNodeLevel(),conditionNode.getNextFalseSubNode()))
                            .map(WorkflowNodeEntity::getId).collect(Collectors.toList());
                    log.info("删除的节点"+isNotTrueNodes);
                    if (!CollectionUtils.isEmpty(isNotTrueNodes)){
                        workflowNodeRepository.deleteByIds(isNotTrueNodes);
                    }
                    WorkflowProcessEntity currentProcess = getProcess(ctx);
                    getWorkflowProcessRepository()
                            .updateById(currentProcess.getId(), new WorkflowProcessEntity().setRunStatus(EnumRunStatus.OVER.getCode()));
                    // 插入进程表
                    insertProcess(ctx, nextNode, EnumRunStatus.RUNNING.getCode(), EnumState.PASS.getCode());
                }
            }
        }else if (WorkflowConstant.APPROVER.equals(nextNode.getNodeType())){
            // todo
        }
    }

    private void overInstance(Context ctx, WorkflowProcessEntity process, WorkflowNodeEntity handlerNode, List<WorkflowNodeEntity> allNodes) {
        List<WorkflowNodeEntity> list = allNodes.stream().filter(v -> v.getNodeLevel().equals(handlerNode.getNodeLevel())).collect(Collectors.toList());
        // 更新实例为通过
        WorkflowInstanceEntity instanceEntity = new WorkflowInstanceEntity();
        instanceEntity.setStatus(EnumState.PASS.getCode());
        instanceEntity.setRunStatus(EnumRunStatus.OVER.getCode());
        instanceEntity.setAssigneeId(ctx.getAssigneeId());
        instanceEntity.setApproveTime(DateUtil.date());
        getWorkflowInstanceRepository().updateById(ctx.getInstanceId(), instanceEntity);
        // 更新节点表
        WorkflowNodeEntity update = new WorkflowNodeEntity();
        update.setRunStatus(EnumRunStatus.OVER.getCode());
        update.setStatus(EnumState.PASS.getCode());
        update.setApproveTime(DateUtil.date());
        workflowNodeRepository.updateById(handlerNode.getId(), update);
        // 删除所有同节点表
        list.remove(handlerNode);
        // 将所有同级节点置为结束
        List<Long> ids = list.stream().map(WorkflowNodeEntity::getId).collect(Collectors.toList());
        log.info("删除的节点"+ids);
        if (!CollectionUtils.isEmpty(ids)){
            workflowNodeRepository.deleteByIds(ids);
        }
        // 更新进程表 将原来的状态结束
        getWorkflowProcessRepository()
                .updateById(process.getId(), new WorkflowProcessEntity().setRunStatus(EnumRunStatus.OVER.getCode()));
        // 插入进程表
        insertProcess(ctx, handlerNode, EnumRunStatus.OVER.getCode(), EnumState.PASS.getCode());
        // 令牌验证通过 进行删除
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
            bean.passHandle(ctx);
        }
    }

    @Override
    public Integer getType() {
        return EnumEventType.AUDIT_PASS.getCode();
    }
}
