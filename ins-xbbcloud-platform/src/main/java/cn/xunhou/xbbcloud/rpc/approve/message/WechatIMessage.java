package cn.xunhou.xbbcloud.rpc.approve.message;

import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.core.util.SystemUtil;
import cn.xunhou.cloud.rocketmq.XbbCustomRocketTemplate;
import cn.xunhou.common.tools.ewechat.WxUniversalMessage;
import cn.xunhou.common.tools.ewechat.WxUniversalMessageHelper;
import cn.xunhou.xbbcloud.common.constants.WorkflowConstant;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendMessage;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: chenning
 * @Date: 2022/09/28/14:23
 * @Description:
 */
@Slf4j
@Component
public class WechatIMessage implements IMessage {
    @Autowired
    private XbbCustomRocketTemplate xbbCustomRocketTemplate;
    @Override
    public void send(SendMessage context) {
        log.info("发送企微提醒给:"+ XbbJsonUtil.toJsonString(context.getNoticeTo()));
        //企微消息发送
        if (StringUtils.isNotBlank(context.getWeChatContext())){
            JSONObject jsonObject = JSON.parseObject(context.getWeChatContext());
            log.info("发送的企微内容:"+jsonObject.toString());
            //企微消息发送
            WxUniversalMessage wxUniversalMessage = WxUniversalMessage
                    .TEXTCARD()//文本卡片信息
                    .sourceType(1)//来源
                    .userXhIds(context.getNoticeTo())//接收人的userxhid
                    .agentId(SystemUtil.isOffline()?1000014:1000043)//由[企业微信服务提醒及预警小程序]应用发送消息
                    .title(jsonObject.getString("title"))//文本卡片标题
                    .description(jsonObject.getString("description"))//文本卡片内容
                    .btnTxt(jsonObject.getString("btnTxt"))//文本卡片按钮
                    .url(jsonObject.getString("url"))//文本卡片按钮点击后跳转的url
                    .build();
            WxUniversalMessageHelper wxUniversalMessageHelper = new WxUniversalMessageHelper(xbbCustomRocketTemplate);
            wxUniversalMessageHelper.sendMsg(wxUniversalMessage);
        }else {
            WxUniversalMessage wxUniversalMessage = WxUniversalMessage
                    .TEXT()//文本消息
                    .content(context.getMsg())//消息内容
                    .userXhIds(context.getNoticeTo())
                    .agentId(SystemUtil.isOffline() ? 1000008 : 1000025)
                    .sourceType(1)
                    .build();
            WxUniversalMessageHelper wxUniversalMessageHelper = new WxUniversalMessageHelper(xbbCustomRocketTemplate);
            wxUniversalMessageHelper.sendMsg(wxUniversalMessage);//发送

        }

    }

    @Override
    public Integer getType() {
        return WorkflowConstant.WECHAT;
    }
}
