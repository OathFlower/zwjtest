package cn.xunhou.xbbcloud.rpc.approve.event;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.xunhou.cloud.core.util.SystemUtil;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.grpc.proto.portal.PortalServiceGrpc;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import cn.xunhou.xbbcloud.common.constants.WorkflowConstant;
import cn.xunhou.xbbcloud.common.enums.EnumRunStatus;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.WorkflowFormMessage;
import cn.xunhou.xbbcloud.rpc.approve.bean.FormField;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowFormRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowInstanceRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowNodeRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowProcessRepository;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowFormEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowNodeEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowProcessEntity;
import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import cn.xunhou.xbbcloud.rpc.approve.config.TemplateConfig;
import cn.xunhou.xbbcloud.rpc.approve.config.WorkflowTemplate;
import jodd.util.StringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenning
 * @Date: 2022/09/26/11:13
 * @Description: 事件抽象类(事件在发生状态流转之前，可能会有一些前置和后置的操作)
 */
@Slf4j
@Getter
public abstract class AbstractEvent implements Event {

    protected WorkflowFormRepository workflowFormRepository;
    protected WorkflowNodeRepository workflowNodeRepository;
    protected WorkflowProcessRepository workflowProcessRepository;
    protected WorkflowInstanceRepository workflowInstanceRepository;
    protected TemplateConfig templateConfig;
    @GrpcClient("ins-xhportal-platform")
    private PortalServiceGrpc.PortalServiceBlockingStub portalServiceBlockingStub;

    @Override
    public void init(){
        this.workflowFormRepository= SpringContextUtil.getBean(WorkflowFormRepository.class);
        this.workflowInstanceRepository = SpringContextUtil.getBean(WorkflowInstanceRepository.class);
        this.workflowNodeRepository = SpringContextUtil.getBean(WorkflowNodeRepository.class);
        this.workflowProcessRepository = SpringContextUtil.getBean(WorkflowProcessRepository.class);
        this.templateConfig = SpringContextUtil.getBean(TemplateConfig.class);
    }
    /**
     * 状态流转前操作
     */
    @Override
    public abstract void beforeTransit(Context ctx);

    /**
     * 状态流转
     */
    @Override
    public abstract void transit(Context ctx);

    /**
     * 状态流转后操作
     */
    @Override
    public abstract void postTransit(Context ctx);


    protected WorkflowInstanceEntity getCurrentInstance(Context ctx){
        WorkflowInstanceEntity instanceEntity = workflowInstanceRepository.findById(ctx.getInstanceId(),WorkflowInstanceEntity.class);
        IAssert.notNull(instanceEntity,"没有查找到工作流实例");
        if (ctx.getFlowTemplateId() == null || ctx.getFlowTemplateId() == 0) {
            ctx.setFlowTemplateId(instanceEntity.getFlowTempId());
        }
        return instanceEntity;
    }

    protected WorkflowProcessEntity getProcess(Context ctx){
        List<WorkflowProcessEntity> processEntities = workflowProcessRepository.findByInsId(ctx.getInstanceId(), EnumRunStatus.RUNNING.getCode());
        IAssert.notEmpty(processEntities, "未找到当前运行中的节点");
        return CollUtil.getFirst(processEntities);
    }

    protected WorkflowTemplate getTemplateConfig(Context ctx){
        WorkflowTemplate template = templateConfig.getTemplateById(ctx.getFlowTemplateId());
        IAssert.notNull(template,"没有查找到工作流模板");
        return template;
    }

    protected void insertProcess(Context ctx, WorkflowNodeEntity node, Integer runStatus, Integer state) {
        WorkflowProcessEntity processEntity = new WorkflowProcessEntity();
        processEntity.setFlowTempId(ctx.getFlowTemplateId());
        processEntity.setAssigneeId(ctx.getAssigneeId());
        processEntity.setNodeId(node.getId());
        processEntity.setNodeLevel(node.getNodeLevel());
        processEntity.setRunStatus(runStatus);
        processEntity.setApproveTime(DateUtil.date());
        processEntity.setInsId(ctx.getInstanceId());
        processEntity.setStatus(state);
        processEntity.setNodeType(node.getNodeType());
        getWorkflowProcessRepository().insert(processEntity);
    }

    protected String[] getHandleEvent(Context ctx){
        WorkflowTemplate workflowTemplate = getTemplateConfig(ctx);
        String postHandle = workflowTemplate.getPostHandle();
        String[] beanNames = postHandle.split(",");
        if (StringUtil.isBlank(postHandle)){
            log.info("没有配置后置处理");
            return null;
        }
        return beanNames;
    }

    protected WorkflowFormMessage buildFormMessage(WorkflowInstanceEntity currentInstance, Context ctx) {
        WorkflowFormEntity formEntity = getWorkflowFormRepository().findByInsId(ctx.getInstanceId());
        IAssert.notNull(formEntity, "未找到指定表单数据");
        WorkflowFormMessage formMessage = new WorkflowFormMessage();
        FormField formField = new FormField();
        BeanUtils.copyProperties(formEntity, formField);
        formMessage.setFormField(formField);
        formMessage.setTenantId(currentInstance.getTenantId());
        formMessage.setEditTargetId(currentInstance.getEditTargetId());
        formMessage.setApplicantId(currentInstance.getApplicantId());
        formMessage.setInsId(currentInstance.getId());
        formMessage.setHandler(ctx.getAssigneeId());
        formMessage.setFlowTempId(currentInstance.getFlowTempId());
        formMessage.setNodeLevel(getNodeLevel(ctx));
        return formMessage ;
    }

    protected int getNodeLevel(Context ctx){
        List<WorkflowProcessEntity> processEntities = workflowProcessRepository.findByInsId(ctx.getInstanceId(), null);
        return (int)processEntities.stream().filter(v-> WorkflowConstant.APPROVER.equals(v.getNodeType()))
                .filter(v->v.getNodeLevel() != 0).count();

    }

    protected WorkflowNodeEntity handlerIsExist(Context ctx, int level, Long assigneeId){
        WorkflowTemplate.Node node = getNodeFromTemplate(ctx, level);
        if (node.getAuditType().getType() == WorkflowConstant.ASSIGNEE_TYPE_CODE) {
            PortalServiceProto.FindPermissionIdsRequest permissionIdsRequest = PortalServiceProto.FindPermissionIdsRequest.newBuilder()
                    .addPermissionCode(node.getAuditType().getCode())
                    .build();
            PortalServiceProto.FindPermissionIdsResponse permissionIds = portalServiceBlockingStub.findPermissionIds(permissionIdsRequest);
            IAssert.notEmpty(permissionIds.getPermissionIdList(), "根据权限码未找到权限ID");
            PortalServiceProto.FindAccountIdsBeRequest accountIdsBeRequest = PortalServiceProto.FindAccountIdsBeRequest.newBuilder()
                    .setPermissionId(String.valueOf(permissionIds.getPermissionId(0)))
                    .build();
            PortalServiceProto.FindAccountIdsBeResponses accountIdsBeResponses = portalServiceBlockingStub.findAccountByPermissionId(accountIdsBeRequest);
            log.info("有权限的accountId:" + accountIdsBeResponses.getAccountIdList());
            if (!CollectionUtils.isEmpty(accountIdsBeResponses.getAccountIdList()) && accountIdsBeResponses.getAccountIdList().contains(String.valueOf(assigneeId))) {
                WorkflowNodeEntity entity = new WorkflowNodeEntity();
                entity.setInsId(ctx.getInstanceId());
                entity.setNodeType(node.getNodeType());
                entity.setPreNode(node.getPreNode());
                entity.setNextNode(node.getNextNode());
                entity.setSignType(node.getSignType());
                entity.setStatus(EnumState.TODO.getCode());
                entity.setRunStatus(EnumRunStatus.RUNNING.getCode());
                entity.setNodeTitle(node.getNodeName());
                entity.setNodeLevel(node.getLevel());
                entity.setAssigneeId(assigneeId);
                entity.setApproveMode(node.getApproveType());
                long id =(long) workflowNodeRepository.insert(entity);
                entity.setId(id);
                return entity;
            }
            return null;
        }
        return null;
    }

    protected WorkflowTemplate.Node getNodeFromTemplate(Context ctx, int level) {

        WorkflowTemplate workflowTemplate = templateConfig.getTemplateById(ctx.getFlowTemplateId());
        IAssert.notNull(workflowTemplate, "没有找到工作流模板");
        List<WorkflowTemplate.Node> nodeInfosList = workflowTemplate.getNode();
        List<WorkflowTemplate.Node> nodes = nodeInfosList.stream().filter(v -> v.getLevel() == level).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(nodes)){
            return null;
        }
        return CollUtil.getFirst(nodes);
    }
}
