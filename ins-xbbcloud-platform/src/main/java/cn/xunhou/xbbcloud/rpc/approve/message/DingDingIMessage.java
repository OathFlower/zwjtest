package cn.xunhou.xbbcloud.rpc.approve.message;

import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.core.util.SystemUtil;
import cn.xunhou.cloud.rocketmq.XbbCustomRocketTemplate;
import cn.xunhou.common.tools.dingtalk.DtUniversalMessage;
import cn.xunhou.common.tools.dingtalk.DtUniversalMessageHelper;
import cn.xunhou.xbbcloud.common.constants.WorkflowConstant;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: chenning
 * @Date: 2022/09/28/14:23
 * @Description:
 */
@Slf4j
@Component
public class DingDingIMessage implements IMessage {
    @Autowired
    private XbbCustomRocketTemplate xbbCustomRocketTemplate;
    @Override
    public void send(SendMessage context) {
        log.info("发送钉钉提醒给:"+ XbbJsonUtil.toJsonString(context.getNoticeTo()));
        // 消息类型 -文本消息
        DtUniversalMessage request = new DtUniversalMessage();
        request.setAgentId(SystemUtil.isOffline()?1815071728L:1784229985L);
        request.setUserXhIdList(context.getNoticeTo());//userxhid
        DtUniversalMessage.Msg msg = new DtUniversalMessage.Msg();
        DtUniversalMessage.Text text = new DtUniversalMessage.Text().setContent(context.getMsg());
        msg.setText(text);
        request.setMsg(msg);
        new DtUniversalMessageHelper(xbbCustomRocketTemplate).sendMsg(request);
    }

    @Override
    public Integer getType() {
        return WorkflowConstant.DINGDING;
    }
}
