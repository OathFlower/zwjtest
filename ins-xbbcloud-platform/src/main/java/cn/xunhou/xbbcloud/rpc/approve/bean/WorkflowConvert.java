package cn.xunhou.xbbcloud.rpc.approve.bean;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.grpc.proto.approve.WorkflowServerProto;
import cn.xunhou.xbbcloud.common.enums.EnumRunStatus;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowFormEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowNodeEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowProcessEntity;
import cn.xunhou.xbbcloud.rpc.approve.param.WorkflowFormQueryParam;
import cn.xunhou.xbbcloud.rpc.approve.config.FormDetail;
import cn.xunhou.xbbcloud.rpc.approve.config.FormTemplate;
import cn.xunhou.xbbcloud.rpc.approve.config.WorkflowTemplate;
import groovyjarjarpicocli.CommandLine;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @Author: chenning
 * @Date: 2022/09/30/11:24
 * @Description:
 */
public class WorkflowConvert {


    public static WorkflowFormQueryParam convert2Param(WorkflowServerProto.FlowInstanceBeRequest request,List<Long> filterInsIds) {
        WorkflowFormQueryParam queryParam = new WorkflowFormQueryParam();
        if (request.hasStatus()) {
            queryParam.setStatus(request.getStatus().getNumber());
        }
        queryParam.setCurPage(request.getCurPage());
        queryParam.setPageSize(request.getPageSize());
        if (request.hasInstanceId()) {
            queryParam.setInsId(request.getInstanceId());
        }
        if (!CollectionUtils.isEmpty(request.getEditTargetIdsList())) {
            queryParam.setEditTargetIds(request.getEditTargetIdsList());
        }
        if (request.hasStartTime() || request.hasEndTime()) {
            queryParam.setStartTime(DateUtil.parse(request.getStartTime()).toTimestamp());
            queryParam.setEndTime(DateUtil.parse(request.getEndTime()).toTimestamp());
        }
        if (!CollectionUtils.isEmpty(request.getApplicantIdsList())) {
            queryParam.setApplicantIds(request.getApplicantIdsList());
        }
        if (request.hasAuditTime()) {
            queryParam.setApprovalTime(DateUtil.parse(request.getAuditTime()).toTimestamp());
        }
        if (request.hasFormField()) {
            convert2Param(queryParam, request.getFormField());
        }
        if (!CollectionUtils.isEmpty(request.getFlowTemplateIdList())) {
            queryParam.setFlowTemplateId(request.getFlowTemplateIdList());
        }
        if (!CollectionUtils.isEmpty(request.getTenantIdList())) {
            queryParam.setTenantIds(request.getTenantIdList());
        }
        if (filterInsIds != null && filterInsIds.size() > 0) {
            queryParam.setFilterInsIds(filterInsIds);
        }
        if (request.hasShowDataByStartTimeFlag() && request.getShowDataByStartTimeFlag()){
            queryParam.setFlowStartTime(DateUtil.date().toTimestamp());
        }
        if (request.hasSortField()){
            queryParam.setSortField(request.getSortField());
        }
        if (request.hasFlowStartTime()){
            queryParam.setFlowStartTime(DateUtil.parse(request.getFlowStartTime()).toTimestamp());
        }
        if (CollectionUtils.isEmpty(request.getStatusesList())){
            queryParam.setStatuses(request.getStatusesList());
        }
        if (!CollectionUtils.isEmpty(request.getExcludeApplicantIdsList())) {
            queryParam.setExcludeApplicantIds(request.getExcludeApplicantIdsList());
        }
        return queryParam;
    }


    public static WorkflowServerProto.QueryProcessListBeResponse convert2Response(WorkflowProcessEntity processEntity) {
        WorkflowServerProto.QueryProcessResponse build = WorkflowServerProto.QueryProcessResponse.newBuilder()
                .setAssigneeId(processEntity.getAssigneeId())
                .setNodeId(processEntity.getNodeId()).build();
        return WorkflowServerProto.QueryProcessListBeResponse.newBuilder().addQueryProcessResponse(build).build();
    }


    public static WorkflowServerProto.QueryFlowTemplateResponse convert2Response(WorkflowTemplate template) {
        WorkflowServerProto.QueryFlowTemplateResponse.Builder builder = WorkflowServerProto.QueryFlowTemplateResponse.newBuilder();
        builder.setFlowTemplateId(template.getTemplateId());
        builder.setTemplateName(template.getTemplateName());
        builder.setDescribe(template.getDescription());

        for (WorkflowTemplate.Node node : template.getNode()) {
            WorkflowServerProto.NodeInfo.Builder nodeBuilder = WorkflowServerProto.NodeInfo.newBuilder();
            nodeBuilder.setNodeLevel(node.getLevel());
            nodeBuilder.setDescribe(node.getDescription());
            nodeBuilder.setCondition(node.getCondition());
            nodeBuilder.setApproveType(node.getApproveType());
            nodeBuilder.setNodeType(node.getNodeType());
            nodeBuilder.setAssigneeType(node.getAssigneeType());
            nodeBuilder.setEvent(node.getEvent());
            nodeBuilder.setNextNode(node.getNextNode());
            nodeBuilder.setPreNode(node.getPreNode());
            nodeBuilder.setListener(node.getListener());
            builder.addNodeInfos(nodeBuilder.build());
        }
        return builder.build();
    }

    public static Context convert2Param(WorkflowServerProto.FlowRequest request) {
        Context context = new Context();
        context.setEventType(request.getEnumEventStatus().getNumber());
        context.setInstanceId(request.getInstanceId());
        context.setApplicantId(request.getApplicantId());
        context.setAssigneeId(request.getHandler());
        if (request.hasReason()) {
            context.setReason(request.getReason());
        }
        if (request.hasToken()){
            context.setToken(request.getToken());
        }
        context.setNoticeTo(request.getNoticeToList());
        context.setNoticeFun(request.getNoticeFunList());
        context.setNoticeTels(request.getNoticeTelsList());
        context.setFlowTemplateId(request.getFlowTemplateId());
        context.setFormField(convert(new FormField(), request.getFormFiled()));
        if (request.hasEditTargetId()) {
            context.setEditTargetId(request.getEditTargetId());
        }
        if (request.hasNoticeContext()){
            context.setNoticeContext(request.getNoticeContext());
        }
        if (request.hasAutoPass() && request.getAutoPass()){
            context.setAutoPass(request.getAutoPass());
        }
        return context;
    }

    public static WorkflowFormEntity convert2Entity(WorkflowServerProto.StartFlowInstanceRequest request, Long insId) {
        WorkflowFormEntity formEntity = new WorkflowFormEntity();
        formEntity.setInsId(insId);
        formEntity.setFormTempId(request.getFormTempId());
        formEntity.setFlowTempId(request.getFlowTemplateInfo().getFlowTemplateId());
        formEntity.setV0(request.getFormField().getV0());
        formEntity.setV1(request.getFormField().getV1());
        formEntity.setV2(request.getFormField().getV2());
        formEntity.setV3(request.getFormField().getV3());
        formEntity.setV4(request.getFormField().getV4());
        formEntity.setV5(request.getFormField().getV5());
        formEntity.setV6(request.getFormField().getV6());
        formEntity.setV7(request.getFormField().getV7());
        formEntity.setV8(request.getFormField().getV8());
        formEntity.setV9(request.getFormField().getV9());
        if (request.getFormField().hasExt()) {
            formEntity.setExt(request.getFormField().getExt());
        }
        if (request.hasFormJson()) {
            formEntity.setFormJson(request.getFormJson());
        }
        return formEntity;
    }


    public static void convert2Param(WorkflowFormQueryParam queryParam, WorkflowServerProto.FormField formField) {
        if (formField.hasV0()) {
            queryParam.setV0(formField.getV0());
        }
        if (formField.hasV1()) {
            queryParam.setV1(formField.getV1());
        }
        if (formField.hasV2()) {
            queryParam.setV2(formField.getV2());
        }
        if (formField.hasV3()) {
            queryParam.setV3(formField.getV3());
        }
        if (formField.hasV4()) {
            queryParam.setV4(formField.getV4());
        }
        if (formField.hasV5()) {
            queryParam.setV5(formField.getV5());
        }
        if (formField.hasV6()) {
            queryParam.setV6(formField.getV6());
        }
        if (formField.hasV7()) {
            queryParam.setV7(formField.getV7());
        }
        if (formField.hasV8()) {
            queryParam.setV8(formField.getV8());
        }
        if (formField.hasV9()) {
            queryParam.setV9(formField.getV9());
        }
        if (formField.hasExt()) {
            queryParam.setExt(formField.getExt());

        }
    }

    public static FormField convert(FormField queryParam, WorkflowServerProto.FormField formField) {
        if (formField.hasV0()) {
            queryParam.setV0(formField.getV0());
        }
        if (formField.hasV1()) {
            queryParam.setV1(formField.getV1());
        }
        if (formField.hasV2()) {
            queryParam.setV2(formField.getV2());
        }
        if (formField.hasV3()) {
            queryParam.setV3(formField.getV3());
        }
        if (formField.hasV4()) {
            queryParam.setV4(formField.getV4());
        }
        if (formField.hasV5()) {
            queryParam.setV5(formField.getV5());
        }
        if (formField.hasV6()) {
            queryParam.setV6(formField.getV6());
        }
        if (formField.hasV7()) {
            queryParam.setV7(formField.getV7());
        }
        if (formField.hasV8()) {
            queryParam.setV8(formField.getV8());
        }
        if (formField.hasV9()) {
            queryParam.setV9(formField.getV9());
        }
        if (formField.hasExt()) {
            queryParam.setExt(formField.getExt());

        }
        return queryParam;
    }

    public static WorkflowServerProto.QueryFormTemplateResponse convert2Response(FormTemplate template) {

        WorkflowServerProto.QueryFormTemplateResponse.Builder builder = WorkflowServerProto.QueryFormTemplateResponse.newBuilder();
        List<FormDetail> table = template.getTable();
        List<FormDetail> form = template.getForm();

        WorkflowServerProto.FormMapping.Builder formMapping = WorkflowServerProto.FormMapping.newBuilder();
        for (FormDetail detail : form) {
            buildFormMapping(detail, formMapping);
        }
        builder.setForm(formMapping.build());
        Map<Integer, List<FormDetail>> map = table.stream().collect(Collectors.groupingBy(FormDetail::getStatus));
        TreeMap<Integer, List<FormDetail>> treeMap = new TreeMap(map);
        List<FormDetail> common = new ArrayList<>();
        boolean isFirst = true;
        for (Map.Entry<Integer, List<FormDetail>> entry : treeMap.entrySet()) {
            WorkflowServerProto.TableMapping.Builder tableMapping = WorkflowServerProto.TableMapping.newBuilder();
            tableMapping.setFormTempId(template.getTemplateId());
            if (isFirst) {
                common = entry.getValue();
                for (FormDetail detail : common) {
                    build(detail, tableMapping, entry.getKey());
                }
                builder.addTable(tableMapping.build());
                isFirst = false;
                continue;
            }
            List<FormDetail> value = entry.getValue();
            value.addAll(common);
            for (FormDetail detail : value) {
                build(detail, tableMapping, entry.getKey());
            }
            builder.addTable(tableMapping.build());
        }
        return builder.build();
    }

    private static void buildFormMapping(FormDetail detail, WorkflowServerProto.FormMapping.Builder formMapping) {
        WorkflowServerProto.Mapping.Builder form = WorkflowServerProto.Mapping.newBuilder();
        form.setColumn(detail.getColumn());
        form.setLabel(detail.getLabel());
        form.setDisplayFlag(detail.isDisplayFlag());
        form.setIndex(detail.getIndex());
        form.setFieldType(Opt.ofNullable(detail.getFieldType()).orElse(0));
        form.setApplyType(detail.getApplyType());
        formMapping.addFormMapping(form.build());
    }

    private static void build(FormDetail detail, WorkflowServerProto.TableMapping.Builder detailResponse, Integer key) {
        WorkflowServerProto.Mapping.Builder form = WorkflowServerProto.Mapping.newBuilder();
        form.setColumn(detail.getColumn());
        form.setLabel(detail.getLabel());
        form.setDisplayFlag(detail.isDisplayFlag());
        form.setIndex(detail.getIndex());
        form.setFieldType(Opt.ofNullable(detail.getFieldType()).orElse(0));
        form.setApplyType(detail.getApplyType());
        detailResponse.addTableMapping(form.build()).setStatus(key);
    }


    public static WorkflowNodeEntity convert2Entity(WorkflowServerProto.NodeInfo node, Long insId) {
        WorkflowNodeEntity nodeEntity = new WorkflowNodeEntity();

        nodeEntity.setInsId(insId);
        nodeEntity.setNodeType(node.getNodeType());
        nodeEntity.setPreNode(node.getPreNode());
        nodeEntity.setNextNode(node.getNextNode());
        nodeEntity.setSignType(node.getSignType());
        nodeEntity.setStatus(EnumState.NOT_APPLY.getCode());
        nodeEntity.setRunStatus(EnumRunStatus.RUNNING.getCode());
        nodeEntity.setNodeTitle(node.getName());
        nodeEntity.setNodeLevel(node.getNodeLevel());
        return nodeEntity;
    }

    public static WorkflowNodeEntity convert2Entity(WorkflowTemplate.Node node, Long insId) {
        WorkflowNodeEntity nodeEntity = new WorkflowNodeEntity();
        nodeEntity.setInsId(insId);
        nodeEntity.setNodeType(node.getNodeType());
        nodeEntity.setPreNode(node.getPreNode());
        nodeEntity.setNextNode(node.getNextNode());
        nodeEntity.setSignType(node.getSignType());
        nodeEntity.setStatus(EnumState.NOT_APPLY.getCode());
        nodeEntity.setRunStatus(EnumRunStatus.RUNNING.getCode());
        nodeEntity.setNodeTitle(node.getNodeName());
        nodeEntity.setNodeLevel(node.getLevel());
        return nodeEntity;
    }
    public static WorkflowServerProto.QueryNodeListBeResponse convert2Response(List<WorkflowNodeEntity> entity) {
        return null;
    }
}
