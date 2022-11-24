package cn.xunhou.xbbcloud.rpc.approve.handle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.xunhou.grpc.proto.portal.PortalServiceGrpc;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import cn.xunhou.xbbcloud.common.constants.WorkflowConstant;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendMessage;
import cn.xunhou.xbbcloud.middleware.rocket.producer.RocketMsgService;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowInstanceRepository;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/09/28/15:39
 * @Description:
 */
@Component
@Slf4j
public class AccountApply extends AbstractHandleEvent {

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    @GrpcClient("ins-xhportal-platform")
    private PortalServiceGrpc.PortalServiceBlockingStub portalServiceBlockingStub;
    @Autowired
    private RocketMsgService rocketMsgService;
    @Override
    public void addHandle(Context ctx) {
        List<WorkflowInstanceEntity> list = workflowInstanceRepository.findList(null, EnumState.TODO.getCode(),
                null, null, null, null, ctx.getFlowTemplateId());
        String now = DateUtil.now();
        ctx.setMsg(String.format("有新的子账号申请加入，请尽快前往审核。（当前待审核的账号创建：%s条）\n申请时间:%s", list.size(),now));
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_ACCOUNT_ADD_CODE);
        JSONObject jsonObject = new JSONObject().set("count", list.size()).set("time", now);
        ctx.setSmsMsg(jsonObject.toString());
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);
    }

    @Override
    public void passHandle(Context ctx) {
        ctx.setMsg("你申请的子账号创建/编辑已通过审核，请登录推荐官交付系统查看详情。");
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_ACCOUNT_PASS_CODE);
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);

    }

    @Override
    public void rejectHandle(Context ctx) {
        ctx.setMsg("你申请的子账号创建/编辑未通过审核，请尽快前往修改资料后重新提交申请。");
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_ACCOUNT_REJECT_CODE);
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);
    }

    @Override
    public void editHandle(Context ctx) {
        List<WorkflowInstanceEntity> list = workflowInstanceRepository.findList(null, EnumState.TODO.getCode(),
                null, null, null, null, ctx.getFlowTemplateId());
        PortalServiceProto.TenantPageQueryBeRequest request = PortalServiceProto.TenantPageQueryBeRequest.newBuilder().setProductId(WorkflowConstant.PRODUCT_ID)
                .setStatus(PortalServiceProto.EnableEnum.ALL)
                .setTenantSource(PortalServiceProto.TenantSourceEnum.AGENT)
                .addTenantIds(ctx.getAssigneeId())
                .build();
        PortalServiceProto.TenantBeResponses tenantPageList = portalServiceBlockingStub.findTenantPageList(request);
        String name = "";
        if (ObjectUtil.isNotEmpty(tenantPageList) && ObjectUtil.isNotEmpty(tenantPageList.getDataList())){
            name=CollUtil.getFirst(tenantPageList.getDataList()).getAlias();
        }
        String now = DateUtil.now();
        ctx.setMsg(String.format("子账号%s修改了账号信息，为避免影响账号使用，请尽快前往审核。（当前待审核的账号编辑：%s条）\n申请时间:%s",name ,list.size(), now));
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_ACCOUNT_MODIFY_CODE);
        JSONObject jsonObject = new JSONObject().set("name",name).set("count", list.size()).set("time", now);
        ctx.setSmsMsg(jsonObject.toString());
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);
    }

}
