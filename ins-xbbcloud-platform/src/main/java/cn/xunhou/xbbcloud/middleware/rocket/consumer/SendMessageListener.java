package cn.xunhou.xbbcloud.middleware.rocket.consumer;

import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.rocketmq.AbstractXbbMessageListener;
import cn.xunhou.cloud.rocketmq.XbbCommonRocketListener;
import cn.xunhou.cloud.rocketmq.XbbMessageBuilder;
import cn.xunhou.xbbcloud.common.constants.RocketConstant;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendMessage;
import cn.xunhou.xbbcloud.rpc.approve.message.IMessage;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author: chenning
 * @Date: 2022/10/08/11:26
 * @Description: 操作结果监听
 */
@Slf4j
@XbbCommonRocketListener(tag = RocketConstant.WORKFLOW_SEND_MESSAGE_TAG, applicationName = RocketConstant.APPLICATION_NAME)
public class SendMessageListener extends AbstractXbbMessageListener {
    @Autowired
    private List<IMessage> messageList;

    @Override
    public void dispose(XbbMessageBuilder.XbbMessage xbbMessage, ConsumeContext context) {

        try {
            String jsonStr = new String(xbbMessage.getBody(), StandardCharsets.UTF_8);
            SendMessage message = JSON.parseObject(jsonStr,SendMessage.class);
            log.info("处理审批结束后发送短信监听obj" + XbbJsonUtil.toJsonString(message.toString()));
            if ( CollectionUtils.isEmpty(message.getNoticeFun())){
                log.info("必要参数为空");
                return;
            }
            Map<Integer, IMessage> messageMap = new HashMap<>();
            for (IMessage m : messageList) {
                messageMap.put(m.getType(), m);
            }
            message.getNoticeFun().forEach(v->{
                if (messageMap.get(v) != null){
                    messageMap.get(v).send(message);
                }
            });
        } catch (Exception e) {
            log.info("处理审批结束后操作结果失败",e);
        }
    }
}
