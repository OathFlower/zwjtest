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
 * @Date: 2022/09/28/15:40
 * @Description:
 */
@Component
@Slf4j
public class ReceiptAccountApply extends AbstractHandleEvent{

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    @Autowired
    private RocketMsgService rocketMsgService;
    @Override
    public void addHandle(Context ctx) {
        String now = DateUtil.now();
        List<WorkflowInstanceEntity> list = workflowInstanceRepository.findList(null, EnumState.TODO.getCode(),
                null, null, null, null, ctx.getFlowTemplateId());
        ctx.setMsg(String.format("您已创建新的收款账户，请尽快前往审核。（当前待审核的收款账户创建：%s条）\n申请时间:%s", list.size(),now ));
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_BANK_ACCOUNT_ADD_CODE);
        JSONObject jsonObject = new JSONObject().set("count", list.size()).set("time", now);
        ctx.setSmsMsg(jsonObject.toString());

        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);

    }

    @Override
    public void passHandle(Context ctx) {
        ctx.setMsg("你申请的收款账户创建/编辑已通过审核，请登录推荐官交付系统查看详情。");
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_BANK_ACCOUNT_PASS_CODE);
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);

    }

    @Override
    public void rejectHandle(Context ctx) {
        ctx.setMsg("你申请的收款账户创建/编辑未通过审核，请尽快前往修改资料后重新提交申请。");
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_BANK_ACCOUNT_REJECT_CODE);
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);
    }

    @Override
    public void editHandle(Context ctx) {
        String now = DateUtil.now();
        List<WorkflowInstanceEntity> list = workflowInstanceRepository.findList(null, EnumState.TODO.getCode(),
                null, null, null, null, ctx.getFlowTemplateId());
        ctx.setMsg(String.format("您收款账户信息已修改，为避免钱包功能的使用，请尽快前往审核。（当前待审核的收款账户编辑：%s条）\n申请时间:%s", list.size(), now));
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_BANK_ACCOUNT_MODIFY_CODE);
        JSONObject jsonObject = new JSONObject().set("count", list.size()).set("time", now);
        ctx.setSmsMsg(jsonObject.toString());
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);
    }
}
