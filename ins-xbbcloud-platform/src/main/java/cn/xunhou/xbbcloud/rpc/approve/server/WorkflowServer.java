package cn.xunhou.xbbcloud.rpc.approve.server;

import cn.xunhou.grpc.proto.approve.AbstractWorkflowServerImplBase;
import cn.xunhou.grpc.proto.approve.WorkflowServerProto;
import cn.xunhou.xbbcloud.rpc.approve.service.WorkflowService;
import com.google.protobuf.Empty;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: chenning
 * @Date: 2022/09/26/16:20
 * @Description:
 */
@GrpcService
@Slf4j
public class WorkflowServer extends AbstractWorkflowServerImplBase {

    @Autowired
    private WorkflowService workflowService;

    @Override
    protected WorkflowServerProto.QueryFlowTemplateResponse queryFlowTemplate(WorkflowServerProto.QueryFlowTemplateRequest request) {
        return workflowService.queryFlowTemplate(request);
    }

    @Override
    protected WorkflowServerProto.InstanceId startFlowInstance(WorkflowServerProto.StartFlowInstanceRequest request) {
        return workflowService.startFlowInstance(request);
    }


    @Override
    protected Empty process(WorkflowServerProto.FlowRequest request) {
        return workflowService.process(request);
    }

    @Override
    protected WorkflowServerProto.FlowInstanceListBeResponse queryFlowInstancePageList(WorkflowServerProto.FlowInstanceBeRequest request) {
        return workflowService.queryFlowInstancePageList(request);
    }

    @Override
    protected WorkflowServerProto.StatusCountResponse queryStatusCount(WorkflowServerProto.FlowInstanceBeRequest request) {
        return workflowService.queryStatusCount(request);
    }

    @Override
    protected WorkflowServerProto.QueryProcessListBeResponse queryProcess(WorkflowServerProto.QueryProcessRequest request) {
        return workflowService.queryProcess(request);
    }

    @Override
    protected WorkflowServerProto.QueryNodeListBeResponse queryNode(WorkflowServerProto.QueryNodeRequest request) {
        return workflowService.queryNode(request);
    }

    @Override
    protected WorkflowServerProto.QueryFormTemplateResponse queryFormTemplate(WorkflowServerProto.QueryFormTemplateRequest request) {
        return workflowService.queryFormTemplate(request);
    }



}
