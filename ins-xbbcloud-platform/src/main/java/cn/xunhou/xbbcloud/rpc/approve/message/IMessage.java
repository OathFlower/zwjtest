package cn.xunhou.xbbcloud.rpc.approve.message;

import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendMessage;

/**
 * @Author: chenning
 * @Date: 2022/09/28/14:22
 * @Description:
 */
public interface IMessage {

    void send(SendMessage context);

    Integer getType();
}
