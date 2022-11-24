package cn.xunhou.xbbcloud.rpc.approve.handle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.xbbcloud.common.constants.WorkflowConstant;
import cn.xunhou.xbbcloud.common.enums.EnumState;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendMessage;
import cn.xunhou.xbbcloud.middleware.rocket.producer.RocketMsgService;
import cn.xunhou.xbbcloud.rpc.approve.bean.Context;
import cn.xunhou.xbbcloud.rpc.approve.bean.WorkflowPageResult;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowFormRepository;
import cn.xunhou.xbbcloud.rpc.approve.dao.WorkflowInstanceRepository;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.param.WorkflowFormQueryParam;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/10/20/16:32
 * @Description:
 */
@Service
public class ProjectApplyEvent extends AbstractHandleEvent {
    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;
    @Autowired
    private WorkflowFormRepository workflowFormRepository;
    @Autowired
    private RocketMsgService rocketMsgService;
    @Override
    public void addHandle(Context ctx) {
        List<WorkflowInstanceEntity> list = workflowInstanceRepository.findList(null, EnumState.TODO.getCode(),
                null, null, null, null, ctx.getFlowTemplateId());
        String now = DateUtil.now();
        ctx.setMsg(String.format("有新的项目申请，请尽快前往审核。（当前未处理的项目审核：%s条）\n申请时间:%s", list.size(),now));
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_PROJECT_ADD_CODE);
        JSONObject jsonObject = new JSONObject().set("count", list.size()).set("time", now);
        ctx.setSmsMsg(jsonObject.toString());
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);
    }

    @Override
    public void passHandle(Context ctx) {
        String projectName = "";
        WorkflowFormQueryParam param = new WorkflowFormQueryParam();
        param.setInsId(ctx.getInstanceId());
        PagePojoList<WorkflowPageResult> pojo = workflowFormRepository.findList(param);
        if (!CollectionUtils.isEmpty(pojo.getData())){
            projectName = CollUtil.getFirst(pojo.getData()).getV1();
        }
        String now = DateUtil.now();
        ctx.setMsg(String.format("你申请的项目审核已通过，请前往人力系统-项目管理查看（项目名称：{%s}）\n审批时间:%s",projectName,now));
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_PROJECT_PASS_CODE);
        ctx.setSmsMsg(new JSONObject().set("code",projectName).set("time", now).toString());
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);
    }

    @Override
    public void rejectHandle(Context ctx) {
        String now = DateUtil.now();
        ctx.setMsg(String.format("你申请的项目审核未通过，请尽快前往人力系统-项目管理-待审项目，重新编辑申请。\n审批时间:%s",now));
        ctx.setSmsTemplateCode(WorkflowConstant.SMS_PROJECT_REJECT_CODE);
        JSONObject jsonObject = new JSONObject().set("time", now);
        ctx.setSmsMsg(jsonObject.toString());
        SendMessage sendMessage = buildSendMessage(ctx);
        rocketMsgService.sendNotice(sendMessage);
    }

    @Override
    public void editHandle(Context ctx) {

    }
}
