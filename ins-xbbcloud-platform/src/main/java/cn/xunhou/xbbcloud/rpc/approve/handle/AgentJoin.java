package cn.xunhou.xbbcloud.rpc.approve.handle;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.xunhou.xbbcloud.common.constants.WorkflowConstant;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendMessage;
import cn.xunhou.xbbcloud.middleware.rocket.producer.RocketMsgService;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowInstanceRepository;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/09/28/13:42
 * @Description:
 */
@Component
@Slf4j
public class AgentJoin extends AbstractHandleEvent {

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    @Autowired
    private RocketMsgService rocketMsgService;
    @Override
    public void addHandle(Context ctx) {
        List<WorkflowInstanceEntity> list = workflowInstanceRepository.findList(null, EnumState.TODO.getCode(),
                null, null, null, null, ctx.getFlowTemplateId());
        String now = DateUtil.now();
        ctx.setMsg(String.format("有新的推荐官申请入驻，请尽快前往审核。（当前未处理的入驻申请：%s条）\n申请时间:%s", list.size(),now));
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_AGENT_JOIN_ADD_CODE);
        JSONObject jsonObject = new JSONObject().set("count", list.size()).set("time", now);
        ctx.setSmsMsg(jsonObject.toString());
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);
    }

    @Override
    public void passHandle(Context ctx) {
        String now = DateUtil.now();
        ctx.setMsg(String.format("您的入驻申请已通过，请联系对应运营完成后续操作。\n审批时间:%s",now));
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_AGENT_JOIN_PASS_CODE);
        ctx.setSmsMsg(new JSONObject().set("time", now).toString());
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);
    }

    @Override
    public void rejectHandle(Context ctx) {
        List<WorkflowInstanceEntity> list = workflowInstanceRepository.findList(null, EnumState.REJECT.getCode(),
                null, null, null, null, ctx.getFlowTemplateId());
        String now = DateUtil.now();
        ctx.setMsg(String.format("您的入驻申请失败，请尽快前往修改资料后重新提交申请。（已有未通过的入驻申请%s条，请尽快处理）\n审批时间:%s", list.size(),now));
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_AGENT_JOIN_REJECT_CODE);
        JSONObject jsonObject = new JSONObject().set("count", list.size()).set("time", now);
        ctx.setSmsMsg(jsonObject.toString());
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);
    }

    @Override
    public void editHandle(Context ctx) {
        return;
    }

}
