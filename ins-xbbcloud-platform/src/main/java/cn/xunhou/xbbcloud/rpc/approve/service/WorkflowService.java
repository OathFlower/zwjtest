package cn.xunhou.xbbcloud.rpc.approve.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.xunhou.cloud.constant.dto.RegionDto;
import cn.xunhou.cloud.constant.utils.AreaUtil;
import cn.xunhou.cloud.core.util.SystemUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.redis.lock.IRedisLockService;
import cn.xunhou.grpc.proto.approve.WorkflowServerProto;
import cn.xunhou.grpc.proto.portal.PortalServiceGrpc;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import cn.xunhou.xbbcloud.common.constants.RedisConstant;
import cn.xunhou.xbbcloud.common.constants.WorkflowConstant;
import cn.xunhou.xbbcloud.common.enums.EnumRunStatus;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.rpc.approve.bean.WorkflowConvert;
import cn.xunhou.xbbcloud.rpc.approve.bean.WorkflowCountResult;
import cn.xunhou.xbbcloud.rpc.approve.bean.WorkflowPageResult;
import cn.xunhou.xbbcloud.rpc.approve.config.*;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowFormRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowInstanceRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowNodeRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowProcessRepository;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowNodeEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowProcessEntity;
import cn.xunhou.xbbcloud.rpc.approve.param.WorkflowNodeQueryParam;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Empty;
import com.google.protobuf.util.Timestamps;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.xunhou.xbbcloud.common.constants.WorkflowConstant.SYSTEM_HANDLE_ID;

/**
 * @Author: chenning
 * @Date: 2022/09/26/16:22
 * @Description:
 */
@Service
@Slf4j
public class WorkflowService {
    @Autowired
    private StateMachine stateMachine;
    @Autowired
    private TemplateConfig templateConfig;
    @Autowired
    private FormTemplateConfig formTemplateConfig;
    @Autowired
    private WorkflowFormRepository workflowFormRepository;
    @Autowired
    private WorkflowNodeRepository workflowNodeRepository;
    @Autowired
    private WorkflowProcessRepository workflowProcessRepository;
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    @Autowired
    private IRedisLockService redisLockService;
    @GrpcClient("ins-xhportal-platform")
    private PortalServiceGrpc.PortalServiceBlockingStub portalServiceBlockingStub;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查看工作流模板
     *
     * @param request
     * @return
     */
    public WorkflowServerProto.QueryFlowTemplateResponse queryFlowTemplate(WorkflowServerProto.QueryFlowTemplateRequest request) {
        List<WorkflowTemplate> templates = templateConfig.getTemplates();
        for (WorkflowTemplate template : templates) {
            if (Objects.equals(request.getFlowTemplateId(), template.getTemplateId())) {
                return WorkflowConvert.convert2Response(template);
            }
        }
        return WorkflowServerProto.QueryFlowTemplateResponse.newBuilder().build();
    }

    /**
     * 开始工作流
     *
     * @param request
     * @return
     */
    @Transactional
    public WorkflowServerProto.InstanceId startFlowInstance(WorkflowServerProto.StartFlowInstanceRequest request) {
        //创建实例
        WorkflowInstanceEntity instanceEntity = new WorkflowInstanceEntity();
        instanceEntity.setApplicantId(request.getApplicantId());
        if (request.hasTenantId()) {
            instanceEntity.setTenantId(request.getTenantId());
        }
        if (request.hasStartTime()) {
            instanceEntity.setStartTime(DateUtil.parse(request.getStartTime()));
        }
        instanceEntity.setStatus(EnumState.NOT_APPLY.getCode());
        instanceEntity.setRunStatus(EnumRunStatus.RUNNING.getCode());
        if (request.hasFlowTemplateInfo()) {
            instanceEntity.setFlowTempId(request.getFlowTemplateInfo().getFlowTemplateId());
        } else {
            instanceEntity.setFlowTempId(request.getFlowTemplateId());
        }
        long insId = (long) workflowInstanceRepository.insert(instanceEntity);
        //创建节点
        if (request.hasFlowTemplateInfo()) {
            addNodeByTemplateInfo(request, insId);
        } else {
            addNodeByConfig(request, insId);
        }
        // 创建form数据
        workflowFormRepository.insert(WorkflowConvert.convert2Entity(request, insId));
        return WorkflowServerProto
                .InstanceId
                .newBuilder()
                .setInstanceId(insId)
                .build();
    }

    /**
     * 工作流状态转换
     *
     * @param request
     * @return
     */
    public Empty process(WorkflowServerProto.FlowRequest request) {
        log.info("审批流状态变更入参:"+request.toString());
        String key = RedisConstant.WORKFLOW_INSTANCE_LOCK_KEY + request.getInstanceId();
        try {
            if (!redisLockService.tryLock(key, TimeUnit.SECONDS, 5, 5)) {
                log.error("未获取到锁,key = {}", key);
                throw GrpcException.asRuntimeException("请稍后重试");
            }
            stateMachine.trigger(WorkflowConvert.convert2Param(request));
        } finally {
            redisLockService.unlock(key);
        }
        return Empty.newBuilder().build();
    }

    /**
     * 查询实例
     *
     * @param request
     * @return
     */
    public WorkflowServerProto.FlowInstanceListBeResponse queryFlowInstancePageList(WorkflowServerProto.FlowInstanceBeRequest request) {
        log.info("查询实例入参:"+request.toString());
        // 查看多级审批
        List<Long> filterInsIds = null;
        if (request.hasApproveNodeLevel()) {
            // 获取传输的节点level
            int approveNodeLevel = request.getApproveNodeLevel();
            // 由于初始节点是审批节点故而需要加1
            if (request.hasStatus() && request.getStatus() == WorkflowServerProto.EnumStatus.PASS) {
                approveNodeLevel += 1;
            }
            filterInsIds = workflowProcessRepository.findInsIdByNodeLevel(approveNodeLevel, request.getFlowTemplateIdList());
        }
        if (filterInsIds != null && filterInsIds.size() == 0) {
            log.info("没有符合等级的审批");
            return WorkflowServerProto.FlowInstanceListBeResponse.newBuilder().build();
        }
        PagePojoList<WorkflowPageResult> pagePojo = workflowFormRepository.findList(WorkflowConvert.convert2Param(request, filterInsIds));
        List<WorkflowPageResult> dataList = pagePojo.getData();
        if (CollectionUtils.isEmpty(dataList)) {
            return WorkflowServerProto.FlowInstanceListBeResponse.newBuilder().build();
        }
        return convert2Response(pagePojo, request.getFormTempId());
    }

    /**
     * 查看数量
     *
     * @param request
     * @return
     */
    public WorkflowServerProto.StatusCountResponse queryStatusCount(WorkflowServerProto.FlowInstanceBeRequest request) {
        // 查看多级审批
        List<Long> filterInsIds = null;
        if (request.hasApproveNodeLevel()) {
            // 获取传输的节点level
            int approveNodeLevel = request.getApproveNodeLevel();
            // 由于初始节点是审批节点故而需要加1
            if (request.hasStatus() && request.getStatus() == WorkflowServerProto.EnumStatus.PASS) {
                approveNodeLevel += 1;
            }
            filterInsIds = workflowProcessRepository.findInsIdByNodeLevel(approveNodeLevel, request.getFlowTemplateIdList());
        }
        if (filterInsIds != null && filterInsIds.size() == 0) {
            log.info("没有符合等级的审批");
            return WorkflowServerProto.StatusCountResponse.newBuilder().build();
        }
        List<WorkflowCountResult> countResult = workflowFormRepository.findCount(WorkflowConvert.convert2Param(request, filterInsIds));
        Map<Integer, Integer> map = countResult.stream().collect(Collectors.toMap(WorkflowCountResult::getStatus, WorkflowCountResult::getCount));
        return WorkflowServerProto.StatusCountResponse.newBuilder()
                .setPassCount(map.getOrDefault(EnumState.PASS.getCode(), 0))
                .setRejectCount(map.getOrDefault(EnumState.REJECT.getCode(), 0))
                .setTodoCount(map.getOrDefault(EnumState.TODO.getCode(), 0))
                .setRevokeCount(map.getOrDefault(EnumState.REVOKE.getCode(), 0)).build();
    }

    /**
     * 查询当前进程
     *
     * @param request
     * @return
     */
    public WorkflowServerProto.QueryProcessListBeResponse queryProcess(WorkflowServerProto.QueryProcessRequest request) {
        List<WorkflowProcessEntity> processEntity = workflowProcessRepository.findByInsId(request.getInsId(), null);
        return WorkflowConvert.convert2Response(processEntity.get(0));
    }

    /**
     * 查询节点
     *
     * @param request
     * @return
     */
    public WorkflowServerProto.QueryNodeListBeResponse queryNode(WorkflowServerProto.QueryNodeRequest request) {
        List<WorkflowNodeEntity> entity = workflowNodeRepository.findByInsId(request.getInsId());
        return WorkflowConvert.convert2Response(entity);
    }

    /**
     * 生成节点
     *
     * @param node
     * @param insId
     * @param nodeEntities
     * @param assigneeIds
     * @return
     */
    private void generateNodes(WorkflowTemplate.Node node, Long insId, List<WorkflowNodeEntity> nodeEntities, List<Long> assigneeIds) {
        // 初始节点
        if (node.getLevel() == 0) {
            nodeEntities.add(WorkflowConvert.convert2Entity(node, insId));
            return;
        }

        for (Long id : assigneeIds) {
            WorkflowNodeEntity nodeEntity = WorkflowConvert.convert2Entity(node, insId);
            nodeEntity.setAssigneeId(id);
            nodeEntity.setApproveMode(node.getApproveType());
            nodeEntities.add(nodeEntity);
        }
    }

    /**
     * 生成节点
     *
     * @param node
     * @param insId
     * @param nodeEntities
     * @return
     */
    private void generateNodes(WorkflowServerProto.NodeInfo node, Long insId, List<WorkflowNodeEntity> nodeEntities) {
        if (node.getNodeLevel() == 0) {
            nodeEntities.add(WorkflowConvert.convert2Entity(node, insId));
            return;
        }
        for (Long id : node.getAssigneeIdsList()) {
            WorkflowNodeEntity nodeEntity = WorkflowConvert.convert2Entity(node, insId);
            nodeEntity.setAssigneeId(id);
            nodeEntity.setApproveMode(node.getApproveType());
            nodeEntities.add(nodeEntity);
        }
    }

    /**
     * 获取有权限审批的id
     *
     * @param auditType
     * @return
     */
    private List<Long> getAssigneeIds(WorkflowTemplate.AuditType auditType) {

        if (auditType.getType() == WorkflowConstant.ASSIGNEE_TYPE_CODE) {
            PortalServiceProto.FindPermissionIdsRequest permissionIdsRequest = PortalServiceProto.FindPermissionIdsRequest.newBuilder()
                    .addPermissionCode(auditType.getCode())
                    .build();
            PortalServiceProto.FindPermissionIdsResponse permissionIds = portalServiceBlockingStub.findPermissionIds(permissionIdsRequest);
            IAssert.notEmpty(permissionIds.getPermissionIdList(), "根据权限码未找到权限ID");
            PortalServiceProto.FindAccountIdsBeRequest accountIdsBeRequest = PortalServiceProto.FindAccountIdsBeRequest.newBuilder()
                    .setPermissionId(String.valueOf(permissionIds.getPermissionId(0)))
                    .build();
            PortalServiceProto.FindAccountIdsBeResponses accountIdsBeResponses = portalServiceBlockingStub.findAccountByPermissionId(accountIdsBeRequest);
            log.info("有权限的accountId:" + accountIdsBeResponses.getAccountIdList());
            List<Long> ids = new ArrayList<>();
            ids.add(SystemUtil.isOnline() ? 10003L : 212L);
            if (!CollectionUtils.isEmpty(accountIdsBeResponses.getAccountIdList())) {
                ids.addAll(accountIdsBeResponses.getAccountIdList().stream().map(Long::valueOf).collect(Collectors.toList()));
            }
            return ids;
        }
        return Collections.emptyList();
    }

    /**
     * 获取表单模板
     *
     * @param request
     * @return
     */
    public WorkflowServerProto.QueryFormTemplateResponse queryFormTemplate(WorkflowServerProto.QueryFormTemplateRequest request) {
        List<FormTemplate> templates = formTemplateConfig.getTemplates();
        for (FormTemplate template : templates) {
            if (Objects.equals(request.getFormTempId(), template.getTemplateId())) {
                return WorkflowConvert.convert2Response(template);
            }
        }

        return WorkflowServerProto.QueryFormTemplateResponse.newBuilder().build();
    }

    /**
     * 构建返回值
     *
     * @param pagePojo
     * @param formTempId
     * @return
     */
    public WorkflowServerProto.FlowInstanceListBeResponse convert2Response(PagePojoList<WorkflowPageResult> pagePojo, long formTempId) {
        WorkflowServerProto.FlowInstanceListBeResponse.Builder builder = WorkflowServerProto.FlowInstanceListBeResponse.newBuilder();
        List<WorkflowPageResult> data = pagePojo.getData();
        // 获取审批人姓名
        List<Long> assigneeIds = data.stream()
                .filter(v -> v.getAssigneeId() != null && v.getAssigneeId() > 0).map(WorkflowPageResult::getAssigneeId).collect(Collectors.toList());
        Map<Long, String> assigneeNames = getNameByAccountIds(assigneeIds);
        assigneeNames.put(SYSTEM_HANDLE_ID, "系统");
        //获取申请人姓名
        List<Long> applicantIds = data.stream()
                .filter(v -> v.getApplicantId() != null && v.getApplicantId() > 0).map(WorkflowPageResult::getApplicantId).collect(Collectors.toList());
        Map<Long, String> applicantNames = getNameByAccountIds(applicantIds);
        // 获取代理商姓名
        Set<Long> tenantIds = data.stream()
                .map(WorkflowPageResult::getTenantId)
                .filter(tenantId -> tenantId != null && tenantId != 0)
                .collect(Collectors.toSet());
        Map<Integer, WorkflowServerProto.AgentInfo> agentMap = getAgentInfo(tenantIds);

        // 获取当前数据属于的审批等级
        List<WorkflowProcessEntity> processEntities = workflowProcessRepository.findList(data.stream()
                .map(WorkflowPageResult::getId).collect(Collectors.toList()));
        Map<Long, List<WorkflowProcessEntity>> processMap = processEntities.stream().filter(v -> WorkflowConstant.APPROVER.equals(v.getNodeType()))
                .filter(v->v.getNodeLevel() != 0)
                .collect(Collectors.groupingBy(WorkflowProcessEntity::getInsId));

        for (WorkflowPageResult result : pagePojo.getData()) {

            WorkflowServerProto.FlowInstanceDetailBeResponse.Builder detail = WorkflowServerProto.FlowInstanceDetailBeResponse.newBuilder()
                    .setApplicant(result.getApplicantId())
                    .setApplyTime(Timestamps.fromMillis(result.getApplyTime().getTime()))
                    .setInstanceId(result.getId())
                    .setTenantId(result.getTenantId())
                    .setEnumStatusValue(result.getStatus())
                    .setUpdateTime(Timestamps.fromMillis(result.getUpdatedAt().getTime()))
                    .setFlowTemplateId(result.getFlowTempId());
            // 设置当前数据属于的审批等级
            if (result.getStatus() != EnumState.PASS.getCode()){
                detail.setApproveNodeLevel(processMap.getOrDefault(result.getId() , new ArrayList<>()).size()+1);
            }else {
                detail.setApproveNodeLevel(processMap.getOrDefault(result.getId(),new ArrayList<>()).size());
            }
            if (processMap.containsKey(result.getId())) {
                List<WorkflowProcessEntity> entities = processMap.get(result.getId());
                for (int i = 0; i < entities.size(); i++) {
                    WorkflowServerProto.MultiLevelApprovalInfo.Builder builder1 = WorkflowServerProto.MultiLevelApprovalInfo.newBuilder()
                            .setCurrentLevel(i + 1);
                    if (entities.get(i).getApproveTime() != null) {
                        builder1.setApproveTime(Timestamps.fromMillis(entities.get(i).getApproveTime().getTime()));
                    }
                    if (entities.get(i).getAssigneeId() != null) {
                        builder1.setHandlerId(entities.get(i).getAssigneeId());
                    }
                    detail.addMultiLevelApprovalInfo(builder1.build());
                }
            } else {
                detail.addMultiLevelApprovalInfo(WorkflowServerProto.MultiLevelApprovalInfo.newBuilder().setCurrentLevel(1).build());
            }

            if (Objects.equals(result.getStatus(), EnumState.TODO.getCode())) {
                Object redisToken = redisTemplate.opsForValue().get(RedisConstant.WORKFLOW_INSTANCE_TODO_VERSION_KEY + result.getId());
                if (redisToken != null) {
                    detail.setToken(redisToken.toString());
                }
            }
            if (result.getStartTime() != null) {
                detail.setStartTime(Timestamps.fromMillis(result.getStartTime().getTime()));
            }
            if (result.getAssigneeId() != null && assigneeNames.containsKey(result.getAssigneeId())) {
                detail.setHandlerName(assigneeNames.get(result.getAssigneeId()));
            }
            if (result.getApplicantId() != null && applicantNames.containsKey(result.getApplicantId())) {
                detail.setCreateName(applicantNames.get(result.getApplicantId()));
            }
            if (result.getApproveTime() != null) {
                detail.setAuditTime(Timestamps.fromMillis(result.getApproveTime().getTime()));
            }
            if (result.getAssigneeId() != null) {
                detail.setHandlerId(result.getAssigneeId());
            }
            if (agentMap.containsKey(result.getTenantId().intValue())) {
                detail.setAgentInfo(agentMap.get(result.getTenantId().intValue()));
            }
            if (result.getEditTargetId() != null) {
                detail.setEditTargetId(result.getEditTargetId());
            }
            if (result.getReason() != null) {
                detail.setReason(result.getReason());
            }
            if (result.getFormJson() != null) {
                detail.setFormJson(result.getFormJson());
            }

            FormTemplate template = formTemplateConfig.getTemplateById(formTempId);
            if (template == null) {
                log.info("没有找到form模板");
                buildFormFieldResponse(result, new HashMap<>(),
                        new HashMap<>(), new HashMap<>(), new HashMap<>(), Collections.emptyList(), detail);
                builder.addData(detail.build());
                continue;
            }

            List<FormDetail> forms = template.getForm();

            Map<Integer, List<FormDetail>> map =
                    forms.stream().collect(Collectors.groupingBy(FormDetail::getApplyType));
            // 枚举
            Map<String, Map<String, String>> enumMap = new HashMap<>();
            // 查询类型
            Map<String, Integer> queryType = new HashMap<>();
            // 控件显示
            Map<String, Boolean> controlMap = new HashMap<>();
            // 脱敏
            Map<String, Integer> maskingMap = new HashMap<>();
            if (map.size() == 1) {
                enumMap = map.get(0).stream().filter(v -> v.getEnumValue() != null)
                        .collect(Collectors.toMap(FormDetail::getColumn, FormDetail::getEnumValue));
                queryType = map.get(0).stream().filter(v -> v.getFieldType() != null)
                        .collect(Collectors.toMap(FormDetail::getColumn, FormDetail::getFieldType));
                controlMap = map.get(0).stream().filter(FormDetail::isControlFlag)
                        .collect(Collectors.toMap(FormDetail::getColumn, FormDetail::isControlFlag));
                maskingMap = map.get(0).stream().filter(v -> v.getMaskingType() != null)
                        .collect(Collectors.toMap(FormDetail::getColumn, FormDetail::getMaskingType));
            } else {
                enumMap = map.get(result.getFlowTempId().intValue()).stream().filter(v -> v.getEnumValue() != null)
                        .collect(Collectors.toMap(FormDetail::getColumn, FormDetail::getEnumValue));
                queryType = map.get(result.getFlowTempId().intValue()).stream().filter(v -> v.getFieldType() != null)
                        .collect(Collectors.toMap(FormDetail::getColumn, FormDetail::getFieldType));
                controlMap = map.get(result.getFlowTempId().intValue()).stream().filter(FormDetail::isControlFlag)
                        .collect(Collectors.toMap(FormDetail::getColumn, FormDetail::isControlFlag));
                maskingMap = map.get(result.getFlowTempId().intValue()).stream().filter(v -> v.getMaskingType() != null)
                        .collect(Collectors.toMap(FormDetail::getColumn, FormDetail::getMaskingType));
            }
            // 根据类型查找value
            buildFormFieldResponse(result, enumMap, queryType, controlMap, maskingMap, forms, detail);
            builder.addData(detail.build());

        }
        builder.setTotal(pagePojo.getTotal());
        log.info("查询实例返回值:"+builder.build().toString());
        return builder.build();
    }

    private Map<Long, String> getNameByAccountIds(List<Long> assigneeIds) {

        if (CollectionUtils.isEmpty(assigneeIds)) {
            return new HashMap<>();
        }
        PortalServiceProto.AccountPageBeRequest accountPageBeRequest = PortalServiceProto.AccountPageBeRequest.newBuilder()
                .addAllAccountId(assigneeIds)
                .build();
        PortalServiceProto.AccountPageListResponses accountPageList = portalServiceBlockingStub.findAccountPageList(accountPageBeRequest);
        return accountPageList.getDataList().stream()
                .collect(Collectors.toMap(PortalServiceProto.AccountDetailResponses::getAccountId, PortalServiceProto.AccountDetailResponses::getNickName));

    }

    /**
     * 获取推荐官信息
     *
     * @param tenantIds
     * @return
     */
    private Map<Integer, WorkflowServerProto.AgentInfo> getAgentInfo(Set<Long> tenantIds) {
        PortalServiceProto.TenantPageQueryBeRequest request = PortalServiceProto.TenantPageQueryBeRequest.newBuilder().setProductId(WorkflowConstant.PRODUCT_ID)
                .setStatus(PortalServiceProto.EnableEnum.ALL)
                .setTenantSource(PortalServiceProto.TenantSourceEnum.AGENT)
                .addAllTenantIds(tenantIds)
                .build();
        PortalServiceProto.TenantBeResponses tenantPageList = portalServiceBlockingStub.findTenantPageList(request);

        List<PortalServiceProto.TenantBeResponse> dataList = tenantPageList.getDataList();
        if (CollectionUtils.isEmpty(dataList)) {
            return new HashMap<>();
        }
        Map<Integer, WorkflowServerProto.AgentInfo> agentMap = new HashMap<>();
        dataList.forEach(v -> {
            WorkflowServerProto.AgentInfo build = WorkflowServerProto.AgentInfo.newBuilder().setAgentId(v.getTenantId())
                    .setAgentName(v.getTenantName())
                    .setAgentNo(v.getTenantNumber()).build();
            agentMap.put(v.getTenantId(), build);
        });
        return agentMap;
    }

    /**
     * 根据动态字段类型获取值
     *
     * @param type
     * @param value
     * @return
     */
    private String getValueByType(Integer type, String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        switch (type) {
            case 1:
                return getAgentName(value);
            case 4:
                return getNameByAccountId(value);
            case 10:
                return getCity(value);
            case 20:
                return splitValue(value);

            default:
                return value;
        }
    }

    private String getNameByAccountId(String value) {
        if (value.contains(",")) {
            String[] split = value.split(",");
            List<Long> ids = Arrays.stream(split).map(Long::parseLong).collect(Collectors.toList());
            PortalServiceProto.AccountPageBeRequest accountPageBeRequest = PortalServiceProto.AccountPageBeRequest.newBuilder()
                    .addAllAccountId(ids)
                    .build();
            PortalServiceProto.AccountPageListResponses accountPageList = portalServiceBlockingStub.findAccountPageList(accountPageBeRequest);
            if (CollectionUtils.isEmpty(accountPageList.getDataList())) {
                return value;
            }
            StringBuilder sb = new StringBuilder();
            accountPageList.getDataList().stream().map(PortalServiceProto.AccountDetailResponses::getNickName)
                    .forEach(v -> sb.append(v).append(","));
            if (sb.length() == 0) {
                return value;
            }
            return sb.substring(0, sb.length() - 1);
        }
        log.info("accountId+" + value);
        if (value.length() != 19) {
            return value;
        }
        PortalServiceProto.AccountPageBeRequest accountPageBeRequest = PortalServiceProto.AccountPageBeRequest.newBuilder()
                .addAccountId(Long.parseLong(value))
                .build();
        PortalServiceProto.AccountPageListResponses accountPageList = portalServiceBlockingStub.findAccountPageList(accountPageBeRequest);
        if (CollectionUtils.isEmpty(accountPageList.getDataList())) {
            return value;
        }
        return CollUtil.getFirst(accountPageList.getDataList()).getNickName();
    }

    /**
     * 拼接日期
     *
     * @param value
     * @return
     */
    private String splitValue(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        String[] split = value.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            sb.append(split[i]);
            sb.append("~");
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * 获取推荐官名
     *
     * @param value
     * @return
     */
    private String getAgentName(String value) {
        PortalServiceProto.TenantPageQueryBeRequest request = PortalServiceProto.TenantPageQueryBeRequest.newBuilder().setProductId(WorkflowConstant.PRODUCT_ID)
                .setStatus(PortalServiceProto.EnableEnum.ALL)
                .setTenantSource(PortalServiceProto.TenantSourceEnum.AGENT)
                .addTenantIds(Long.parseLong(value))
                .build();
        PortalServiceProto.TenantBeResponses tenantPageList = portalServiceBlockingStub.findTenantPageList(request);
        PortalServiceProto.TenantBeResponse first = CollUtil.getFirst(tenantPageList.getDataList());
        if (first == null) {
            return value;
        }
        return StringUtils.isBlank(first.getAlias()) ? value : first.getAlias();
    }

    /**
     * 获取城市名
     *
     * @param value
     * @return
     */
    private String getCity(String value) {
        if (value.contains(",")) {
            String[] split = value.split(",");
            StringBuilder sb = new StringBuilder();
            for (String code : split) {
                RegionDto region = AreaUtil.getRegionByCode(code);
                if (region == null) {
                    continue;
                }
                sb.append(region.getName()).append(",");
            }
            if (sb.length() == 0) {
                return value;
            }
            return sb.substring(0, sb.length() - 1);
        }
        RegionDto region = AreaUtil.getRegionByCode(value);
        if (region == null) {
            return value;
        }
        String city = region.getName();
        if (value.length() == 3) {
            return city;
        }
        RegionDto province = AreaUtil.getRegionByCode(region.getParentCode());
        if (province != null) {
            return province.getName() + city;
        }
        return city;

    }

    /**
     * 设置动态字段的值
     *
     * @param result
     * @param enumMap
     * @param queryType
     * @param controlMap
     * @param maskingMap
     * @param forms
     * @param detail
     * @return
     */
    private void buildFormFieldResponse(WorkflowPageResult result,
                                        Map<String, Map<String, String>> enumMap,
                                        Map<String, Integer> queryType,
                                        Map<String, Boolean> controlMap,
                                        Map<String, Integer> maskingMap, List<FormDetail> forms, WorkflowServerProto.FlowInstanceDetailBeResponse.Builder detail) {
        WorkflowServerProto.FormFieldResponse.Builder formField = WorkflowServerProto.FormFieldResponse.newBuilder();
        if (StringUtils.isNotBlank(result.getV0())) {
            WorkflowServerProto.DetailInfo.Builder detailInfo = WorkflowServerProto.DetailInfo.newBuilder();
            detailInfo.setColumn("v0");
            detailInfo.setValue(result.getV0());
            detailInfo.setDisplayValue(result.getV0());
            WorkflowServerProto.DetailInfo.Builder builder = WorkflowServerProto.DetailInfo.newBuilder();
            builder.setDisplayValue(result.getV0()).setValue(result.getV0());
            if (enumMap.get("v0") != null) {
                builder.setDisplayValue(enumMap.get("v0").get(result.getV0() != null ? enumMap.get("v0").get(result.getV0()) : result.getV0()));
                detailInfo.setDisplayValue(enumMap.get("v0").get(result.getV0() != null ? enumMap.get("v0").get(result.getV0()) : result.getV0()));
            }
            if (queryType.get("v0") != null) {
                builder.setDisplayValue(getValueByType(queryType.get("v0"), result.getV0()));
                detailInfo.setDisplayValue(getValueByType(queryType.get("v0"), result.getV0()));
            }
            if (controlMap.get("v0") != null) {
                builder.setControlFlag(true);
                detailInfo.setControlFlag(true);
            }
            if (maskingMap.get("v0") != null) {
                builder.setDisplayValue(maskingByType(maskingMap.get("v0"), result.getV0()));
                detailInfo.setDisplayValue(maskingByType(maskingMap.get("v0"), result.getV0()));
            }
            formField.setV0(builder.build());
            detail.addDetailInfos(detailInfo);
        }
        if (StringUtils.isNotBlank(result.getV1())) {
            WorkflowServerProto.DetailInfo.Builder detailInfo = WorkflowServerProto.DetailInfo.newBuilder();
            detailInfo.setColumn("v1");
            detailInfo.setValue(result.getV1());
            detailInfo.setDisplayValue(result.getV1());
            WorkflowServerProto.DetailInfo.Builder builder = WorkflowServerProto.DetailInfo.newBuilder();
            builder.setDisplayValue(result.getV1()).setValue(result.getV1());
            if (enumMap.get("v1") != null) {
                builder.setDisplayValue(enumMap.get("v1").get(result.getV1()) != null ? enumMap.get("v1").get(result.getV1()) : result.getV1());
                detailInfo.setDisplayValue(enumMap.get("v1").get(result.getV1()) != null ? enumMap.get("v1").get(result.getV1()) : result.getV1());
            }
            if (queryType.get("v1") != null) {
                builder.setDisplayValue(getValueByType(queryType.get("v1"), result.getV1()));
                detailInfo.setDisplayValue(getValueByType(queryType.get("v1"), result.getV1()));
            }
            if (controlMap.get("v1") != null) {
                builder.setControlFlag(true);
                detailInfo.setControlFlag(true);
            }
            if (maskingMap.get("v1") != null) {
                builder.setDisplayValue(maskingByType(maskingMap.get("v1"), result.getV1()));
                detailInfo.setDisplayValue(maskingByType(maskingMap.get("v1"), result.getV1()));
            }
            formField.setV1(builder.build());
            detail.addDetailInfos(detailInfo);
        }
        if (StringUtils.isNotBlank(result.getV2())) {
            WorkflowServerProto.DetailInfo.Builder detailInfo = WorkflowServerProto.DetailInfo.newBuilder();
            detailInfo.setColumn("v2");
            detailInfo.setValue(result.getV2());
            detailInfo.setDisplayValue(result.getV2());
            WorkflowServerProto.DetailInfo.Builder builder = WorkflowServerProto.DetailInfo.newBuilder();
            builder.setDisplayValue(result.getV2()).setValue(result.getV2());

            if (enumMap.get("v2") != null) {
                builder.setDisplayValue(enumMap.get("v2").get(result.getV2()) != null ? enumMap.get("v2").get(result.getV2()) : result.getV2());
                detailInfo.setDisplayValue(enumMap.get("v2").get(result.getV2()) != null ? enumMap.get("v2").get(result.getV2()) : result.getV2());
            }
            if (queryType.get("v2") != null) {
                builder.setDisplayValue(getValueByType(queryType.get("v2"), result.getV2()));
                detailInfo.setDisplayValue(getValueByType(queryType.get("v2"), result.getV2()));
            }
            if (controlMap.get("v2") != null) {
                builder.setControlFlag(true);
                detailInfo.setControlFlag(true);
            }
            if (maskingMap.get("v2") != null) {
                builder.setDisplayValue(maskingByType(maskingMap.get("v2"), result.getV2()));
                detailInfo.setDisplayValue(maskingByType(maskingMap.get("v2"), result.getV2()));
            }
            formField.setV2(builder.build());
            detail.addDetailInfos(detailInfo);
        }
        if (StringUtils.isNotBlank(result.getV3())) {
            WorkflowServerProto.DetailInfo.Builder detailInfo = WorkflowServerProto.DetailInfo.newBuilder();

            detailInfo.setColumn("v3");
            detailInfo.setValue(result.getV3());
            detailInfo.setDisplayValue(result.getV3());
            WorkflowServerProto.DetailInfo.Builder builder = WorkflowServerProto.DetailInfo.newBuilder();
            builder.setDisplayValue(result.getV3()).setValue(result.getV3());
            if (enumMap.get("v3") != null) {
                builder.setDisplayValue(enumMap.get("v3").get(result.getV3()) != null ? enumMap.get("v3").get(result.getV3()) : result.getV3());
                detailInfo.setDisplayValue(enumMap.get("v3").get(result.getV3()) != null ? enumMap.get("v3").get(result.getV3()) : result.getV3());
            }
            if (queryType.get("v3") != null) {
                builder.setDisplayValue(getValueByType(queryType.get("v3"), result.getV3()));
                detailInfo.setDisplayValue(getValueByType(queryType.get("v3"), result.getV3()));
            }
            if (controlMap.get("v3") != null) {
                builder.setControlFlag(true);
                detailInfo.setControlFlag(true);
            }
            if (maskingMap.get("v3") != null) {
                builder.setDisplayValue(maskingByType(maskingMap.get("v3"), result.getV3()));
                detailInfo.setDisplayValue(maskingByType(maskingMap.get("v3"), result.getV3()));
            }
            formField.setV3(builder.build());
            detail.addDetailInfos(detailInfo);

        }
        if (StringUtils.isNotBlank(result.getV4())) {
            WorkflowServerProto.DetailInfo.Builder detailInfo = WorkflowServerProto.DetailInfo.newBuilder();
            detailInfo.setColumn("v4");
            detailInfo.setValue(result.getV4());
            detailInfo.setDisplayValue(result.getV4());
            WorkflowServerProto.DetailInfo.Builder builder = WorkflowServerProto.DetailInfo.newBuilder();
            builder.setDisplayValue(result.getV4()).setValue(result.getV4());
            if (enumMap.get("v4") != null) {
                builder.setDisplayValue(enumMap.get("v4").get(result.getV4()) != null ? enumMap.get("v4").get(result.getV4()) : result.getV4());
                detailInfo.setDisplayValue(enumMap.get("v4").get(result.getV4()) != null ? enumMap.get("v4").get(result.getV4()) : result.getV4());
            }
            if (queryType.get("v4") != null) {
                builder.setDisplayValue(getValueByType(queryType.get("v4"), result.getV4()));
                detailInfo.setDisplayValue(getValueByType(queryType.get("v4"), result.getV4()));
            }
            if (controlMap.get("v4") != null) {
                builder.setControlFlag(true);
                detailInfo.setControlFlag(true);
            }
            if (maskingMap.get("v4") != null) {
                builder.setDisplayValue(maskingByType(maskingMap.get("v4"), result.getV4()));
                detailInfo.setDisplayValue(maskingByType(maskingMap.get("v4"), result.getV4()));
            }
            formField.setV4(builder.build());
            detail.addDetailInfos(detailInfo);

        }
        if (StringUtils.isNotBlank(result.getV5())) {
            WorkflowServerProto.DetailInfo.Builder detailInfo = WorkflowServerProto.DetailInfo.newBuilder();

            detailInfo.setColumn("v5");
            detailInfo.setValue(result.getV5());
            detailInfo.setDisplayValue(result.getV5());
            WorkflowServerProto.DetailInfo.Builder builder = WorkflowServerProto.DetailInfo.newBuilder();
            builder.setDisplayValue(result.getV5()).setValue(result.getV5());

            if (enumMap.get("v5") != null) {
                builder.setDisplayValue(enumMap.get("v5").get(result.getV5()) != null ? enumMap.get("v5").get(result.getV5()) : result.getV5());
                detailInfo.setDisplayValue(enumMap.get("v5").get(result.getV5()) != null ? enumMap.get("v5").get(result.getV5()) : result.getV5());
            }
            if (queryType.get("v5") != null) {
                builder.setDisplayValue(getValueByType(queryType.get("v5"), result.getV5()));
                detailInfo.setDisplayValue(getValueByType(queryType.get("v5"), result.getV5()));
            }
            if (controlMap.get("v5") != null) {
                builder.setControlFlag(true);
                detailInfo.setControlFlag(true);
            }
            if (maskingMap.get("v5") != null) {
                builder.setDisplayValue(maskingByType(maskingMap.get("v5"), result.getV5()));
                detailInfo.setDisplayValue(maskingByType(maskingMap.get("v5"), result.getV5()));
            }
            formField.setV5(builder.build());
            detail.addDetailInfos(detailInfo);
        }
        if (StringUtils.isNotBlank(result.getV6())) {
            WorkflowServerProto.DetailInfo.Builder detailInfo = WorkflowServerProto.DetailInfo.newBuilder();

            detailInfo.setColumn("v6");
            detailInfo.setValue(result.getV6());
            detailInfo.setDisplayValue(result.getV6());
            WorkflowServerProto.DetailInfo.Builder builder = WorkflowServerProto.DetailInfo.newBuilder();
            builder.setDisplayValue(result.getV6()).setValue(result.getV6());

            if (enumMap.get("v6") != null) {
                builder.setDisplayValue(enumMap.get("v6").get(result.getV6()) != null ? enumMap.get("v6").get(result.getV6()) : result.getV6());
                detailInfo.setDisplayValue(enumMap.get("v6").get(result.getV6()) != null ? enumMap.get("v6").get(result.getV6()) : result.getV6());
            }
            if (queryType.get("v6") != null) {
                builder.setDisplayValue(getValueByType(queryType.get("v6"), result.getV6()));
                detailInfo.setDisplayValue(getValueByType(queryType.get("v6"), result.getV6()));
            }
            if (controlMap.get("v6") != null) {
                builder.setControlFlag(true);
                detailInfo.setControlFlag(true);
            }
            if (maskingMap.get("v6") != null) {
                builder.setDisplayValue(maskingByType(maskingMap.get("v6"), result.getV6()));
                detailInfo.setDisplayValue(maskingByType(maskingMap.get("v6"), result.getV6()));
            }
            formField.setV6(builder.build());
            detail.addDetailInfos(detailInfo);
        }
        if (StringUtils.isNotBlank(result.getV7())) {
            WorkflowServerProto.DetailInfo.Builder detailInfo = WorkflowServerProto.DetailInfo.newBuilder();

            detailInfo.setColumn("v7");
            detailInfo.setValue(result.getV7());
            detailInfo.setDisplayValue(result.getV7());
            WorkflowServerProto.DetailInfo.Builder builder = WorkflowServerProto.DetailInfo.newBuilder();
            builder.setDisplayValue(result.getV7()).setValue(result.getV7());

            if (enumMap.get("v7") != null) {
                builder.setDisplayValue(enumMap.get("v7").get(result.getV7()) != null ? enumMap.get("v7").get(result.getV7()) : result.getV7());
                detailInfo.setDisplayValue(enumMap.get("v7").get(result.getV7()) != null ? enumMap.get("v7").get(result.getV7()) : result.getV7());
            }
            if (queryType.get("v7") != null) {
                builder.setDisplayValue(getValueByType(queryType.get("v7"), result.getV7()));
                detailInfo.setDisplayValue(getValueByType(queryType.get("v7"), result.getV7()));
            }
            if (controlMap.get("v7") != null) {
                builder.setControlFlag(true);
                detailInfo.setControlFlag(true);
            }
            if (maskingMap.get("v7") != null) {
                builder.setDisplayValue(maskingByType(maskingMap.get("v7"), result.getV7()));
                detailInfo.setDisplayValue(maskingByType(maskingMap.get("v7"), result.getV7()));
            }
            formField.setV7(builder.build());
            detail.addDetailInfos(detailInfo);

        }
        if (StringUtils.isNotBlank(result.getV8())) {
            WorkflowServerProto.DetailInfo.Builder detailInfo = WorkflowServerProto.DetailInfo.newBuilder();

            detailInfo.setColumn("v8");
            detailInfo.setValue(result.getV8());
            detailInfo.setDisplayValue(result.getV8());
            WorkflowServerProto.DetailInfo.Builder builder = WorkflowServerProto.DetailInfo.newBuilder();
            builder.setDisplayValue(result.getV8()).setValue(result.getV8());

            if (enumMap.get("v8") != null) {
                builder.setDisplayValue(enumMap.get("v8").get(result.getV8()) != null ? enumMap.get("v8").get(result.getV8()) : result.getV8());
                detailInfo.setDisplayValue(enumMap.get("v8").get(result.getV8()) != null ? enumMap.get("v8").get(result.getV8()) : result.getV8());
            }
            if (queryType.get("v8") != null) {
                builder.setDisplayValue(getValueByType(queryType.get("v8"), result.getV8()));
                detailInfo.setDisplayValue(getValueByType(queryType.get("v8"), result.getV8()));
            }
            if (controlMap.get("v8") != null) {
                builder.setControlFlag(true);
                detailInfo.setControlFlag(true);
            }
            if (maskingMap.get("v8") != null) {
                builder.setDisplayValue(maskingByType(maskingMap.get("v8"), result.getV8()));
                detailInfo.setDisplayValue(maskingByType(maskingMap.get("v8"), result.getV8()));
            }
            formField.setV8(builder.build());
            detail.addDetailInfos(detailInfo);

        }
        if (StringUtils.isNotBlank(result.getV9())) {
            WorkflowServerProto.DetailInfo.Builder detailInfo = WorkflowServerProto.DetailInfo.newBuilder();

            detailInfo.setColumn("v9");
            detailInfo.setValue(result.getV9());
            detailInfo.setDisplayValue(result.getV9());
            WorkflowServerProto.DetailInfo.Builder builder = WorkflowServerProto.DetailInfo.newBuilder();
            builder.setDisplayValue(result.getV9()).setValue(result.getV9());

            if (enumMap.get("v9") != null) {
                builder.setDisplayValue(enumMap.get("v9").get(result.getV9()) != null ? enumMap.get("v9").get(result.getV9()) : result.getV9());
                detailInfo.setDisplayValue(enumMap.get("v9").get(result.getV9()) != null ? enumMap.get("v9").get(result.getV9()) : result.getV9());

            }
            if (queryType.get("v9") != null) {
                builder.setDisplayValue(getValueByType(queryType.get("v9"), result.getV9()));
                detailInfo.setDisplayValue(getValueByType(queryType.get("v9"), result.getV9()));

            }
            if (controlMap.get("v9") != null) {
                builder.setControlFlag(true);
                detailInfo.setControlFlag(true);
            }
            if (maskingMap.get("v9") != null) {
                builder.setDisplayValue(maskingByType(maskingMap.get("v9"), result.getV9()));
                detailInfo.setDisplayValue(maskingByType(maskingMap.get("v9"), result.getV9()));

            }
            formField.setV9(builder.build());
            detail.addDetailInfos(detailInfo);

        }
        // v11:123,v12:12345
        if (StringUtils.isNotBlank(result.getExt())) {
            formField.setExt(result.getExt());
            String ext = result.getExt();
            JSONObject jsonObject = JSON.parseObject(ext);
            for (FormDetail formDetail : forms) {
                WorkflowServerProto.DetailInfo.Builder detailInfo = WorkflowServerProto.DetailInfo.newBuilder();
                String value = jsonObject.getString(formDetail.getColumn());
                if (value != null) {
                    detailInfo.setColumn(formDetail.getColumn());
                    detailInfo.setValue(value);
                    detailInfo.setDisplayValue(value);
                    if (enumMap.get(formDetail.getColumn()) != null) {
                        detailInfo.setDisplayValue(enumMap.get(formDetail.getColumn()).get(value) != null ?
                                enumMap.get(formDetail.getColumn()).get(value) : result.getV9());
                    }
                    if (queryType.get(formDetail.getColumn()) != null) {
                        detailInfo.setDisplayValue(getValueByType(queryType.get(formDetail.getColumn()), value));
                    }
                    if (controlMap.get(formDetail.getColumn()) != null) {
                        detailInfo.setControlFlag(true);
                    }
                    if (maskingMap.get(formDetail.getColumn()) != null) {
                        detailInfo.setDisplayValue(maskingByType(maskingMap.get(formDetail.getColumn()), value));
                    }
                    detail.addDetailInfos(detailInfo);
                }
            }

        }
        detail.setFormField(formField);
    }

    private String maskingByType(Integer type, String value) {
        switch (type) {
            case 1:
                return DesensitizedUtil.idCardNum(value, 3, 3);
            case 2:
                return DesensitizedUtil.idCardNum(value, 6, 4);
            case 3:
                return DesensitizedUtil.bankCard(value);
            default:
                return value;
        }
    }

    private void addNodeByConfig(WorkflowServerProto.StartFlowInstanceRequest request, long insId) {
        List<WorkflowNodeEntity> nodeEntities = new ArrayList<>();

        WorkflowTemplate workflowTemplate = templateConfig.getTemplateById(request.getFlowTemplateId());
        IAssert.notNull(workflowTemplate, "没有找到工作流模板");
        List<WorkflowTemplate.Node> nodeInfosList = workflowTemplate.getNode();
        // 将有权限的用户设置到模板
        for (WorkflowTemplate.Node nodeInfo : nodeInfosList) {
            List<Long> assigneeIds = getAssigneeIds(nodeInfo.getAuditType());
            // 判断节点1. 审批人，2.抄送人，3. 办理人，4条件分支 暂时todo后可用策略模式
            if (WorkflowConstant.APPROVER.equals(nodeInfo.getNodeType())) {
                if (WorkflowConstant.PERSON.equals(nodeInfo.getApproveType())) {
                    generateNodes(nodeInfo, insId, nodeEntities, assigneeIds);
                }
            } else if (WorkflowConstant.CONDITION.equals(nodeInfo.getNodeType())) {
                WorkflowNodeEntity nodeEntity = WorkflowConvert.convert2Entity(nodeInfo, insId);
                nodeEntities.add(nodeEntity);
            }
        }
        workflowNodeRepository.batchInsert(nodeEntities);

    }

    private void addNodeByTemplateInfo(WorkflowServerProto.StartFlowInstanceRequest request, long insId) {
        List<WorkflowNodeEntity> nodeEntities = new ArrayList<>();

        WorkflowServerProto.FlowTemplateInfo flowTemplate = request.getFlowTemplateInfo();
        for (WorkflowServerProto.NodeInfo node : flowTemplate.getNodInfoList()) {
            // 判断节点1. 审批人，2.抄送人，3. 办理人，4条件分支 暂时todo后可用策略模式
            if (node.getNodeType() == WorkflowConstant.APPROVER) {
                if (node.getApproveType() == WorkflowConstant.PERSON) {
                    generateNodes(node, insId, nodeEntities);
                }
            }
        }
        workflowNodeRepository.batchInsert(nodeEntities);

    }

}