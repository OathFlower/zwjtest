package cn.xunhou.xbbcloud.middleware.rocket.producer;

import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.rocketmq.XbbCustomRocketTemplate;
import cn.xunhou.cloud.rocketmq.XbbMessageBuilder;
import cn.xunhou.xbbcloud.common.constants.RocketConstant;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class RocketMsgService {
    @Autowired(required = false)
    private XbbCustomRocketTemplate rocketTemplate;


    /**
     * 定时发送结算考勤账单
     *
     * @param sendMsg 消息内容
     */
    public void sendAttendanceBillBatchMsg(List<AttendanceBillBatchMessage> sendMsg) {
        log.info("sendAttendanceBillBatchMsg ---> 定时发送结算考勤账单，mes = {}", JSONUtil.toJsonStr(sendMsg));
        rocketTemplate.sendCommonSharedKey(RocketConstant.ATTENDANCE_BILL_TAG,
                RocketConstant.ATTENDANCE_BILL_TAG_SHARDING_KEY,
                XbbMessageBuilder.newBuilder()
                        .setBody(XbbJsonUtil.toJsonBytes(sendMsg)).build());
        log.info("sendAttendanceBillBatchMsg ---> 定时发送结算考勤账单成功");
    }

    /**
     * 发送审批结束
     *
     * @param sendMsg 消息内容
     */
    public void sendPassMsg(WorkflowFormMessage sendMsg) {
        log.info("sendPassMsg ---> 审批通过，mes = {}", JSONUtil.toJsonStr(sendMsg));
        rocketTemplate.sendCommon(RocketConstant.WORKFLOW_PASS_TAG,
                XbbMessageBuilder.newBuilder()
                        .setBody(XbbJsonUtil.toJsonBytes(sendMsg)).build());
        log.info("sendPassMsg ---> 发送审批通过成功");
    }

    /**
     * 发送审批拒绝
     *
     * @param sendMsg 消息内容
     */
    public void sendRejectMsg(WorkflowFormMessage sendMsg) {
        log.info("sendRejectMsg ---> 审批拒绝，mes = {}", JSONUtil.toJsonStr(sendMsg));
        rocketTemplate.sendCommon(RocketConstant.WORKFLOW_REJECT_TAG,
                XbbMessageBuilder.newBuilder()
                        .setBody(XbbJsonUtil.toJsonBytes(sendMsg)).build());
        log.info("sendPassMsg ---> 发送审批拒绝成功");
    }

    /**
     * 发送审批更新
     *
     * @param sendMsg 消息内容
     */
    public void sendUpdateMsg(WorkflowFormMessage sendMsg) {
        log.info("sendPassMsg ---> 审批更新，mes = {}", JSONUtil.toJsonStr(sendMsg));
        rocketTemplate.sendCommon(RocketConstant.WORKFLOW_UPDATE_TAG,
                XbbMessageBuilder.newBuilder()
                        .setBody(XbbJsonUtil.toJsonBytes(sendMsg)).build());
        log.info("sendPassMsg ---> 发送审批更新成功");
    }

    /**
     * 发送审批更新
     *
     * @param sendMsg 消息内容
     */
    public void sendAddMsg(WorkflowFormMessage sendMsg) {
        log.info("sendPassMsg ---> 审批新增，mes = {}", JSONUtil.toJsonStr(sendMsg));
        rocketTemplate.sendCommon(RocketConstant.WORKFLOW_ADD_TAG,
                XbbMessageBuilder.newBuilder()
                        .setBody(XbbJsonUtil.toJsonBytes(sendMsg)).build());
        log.info("sendPassMsg ---> 发送审批新增成功");
    }

    /**
     * 发送信息
     *
     * @param sendMsg 消息内容
     */
    public void sendNotice(SendMessage sendMsg) {
        log.info("sendPassMsg ---> 发送短信，mes = {}", JSONUtil.toJsonStr(sendMsg));
        rocketTemplate.sendCommon(RocketConstant.WORKFLOW_SEND_MESSAGE_TAG,
                XbbMessageBuilder.newBuilder()
                        .setBody(XbbJsonUtil.toJsonBytes(sendMsg)).build());
        log.info("sendPassMsg ---> 发送短信");
    }

    /**
     * 等待调度完成
     *
     * @param sendMsg
     */
    public void sendWaitFundDispatching(SendWaitFundDispatchingMessage sendMsg) {
        log.info("sendWaitFundDispatching ---> mes = {}", JSONUtil.toJsonStr(sendMsg));
        if (sendMsg.getCount() == null) {
            sendMsg.setCount(0);
        }
        rocketTemplate.sendCommon(RocketConstant.WAIT_FUND_DISPATCHING,
                XbbMessageBuilder.newBuilder()
                        .setKey(UUID.randomUUID().toString())
                        .setDelayTime(sendMsg.getDelayTime())
                        .setBody(XbbJsonUtil.toJsonBytes(sendMsg)).build());
        log.info("sendWaitFundDispatching ---> 成功");
    }

    /**
     * 发送到余额
     *
     * @param sendMsg
     */
    public void sendToPayroll(List<XcyPayrollMessage> sendMsg) {
        log.info("sendToPayroll ---> mes = {}", JSONUtil.toJsonStr(sendMsg));
        rocketTemplate.sendCommon(RocketConstant.SEND_TO_PAYROLL_TAG,
                XbbMessageBuilder.newBuilder()
                        .setKey(UUID.randomUUID().toString())
                        .setBody(XbbJsonUtil.toJsonBytes(sendMsg)).build());
        log.info("sendToPayroll ---> 成功");
    }

    /**
     * 薪酬云发薪撤回
     */
    public void xcy2xbbBackDetailTag(Xcy2XbbBackDetailMessage message) {
        log.info("xcy2xbbBackDetailTag ---> message = {}", JSONUtil.toJsonStr(message));
        rocketTemplate.sendCommon(RocketConstant.XCY2XBB_BACK_DETAIL_TAG,
                XbbMessageBuilder.newBuilder()
                        .setKey(message.getDetailId() + "")
                        .setBody(XbbJsonUtil.toJsonBytes(message)).build());
        log.info("sendToPayroll ---> 成功");
    }
}
