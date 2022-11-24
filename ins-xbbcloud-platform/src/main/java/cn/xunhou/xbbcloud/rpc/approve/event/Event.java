package cn.xunhou.xbbcloud.rpc.approve.event;

import cn.xunhou.xbbcloud.rpc.approve.bean.Context;

/**
 * @Author: chenning
 * @Date: 2022/09/26/14:32
 * @Description: 工作流审批事件
 */
public interface Event {

    /**
     * 状态流转前操作
     */
    void beforeTransit(Context ctx);

    /**
     * 状态流转
     */
    void transit(Context ctx);

    /**
     * 状态流转后操作
     */
    void postTransit(Context ctx);

    void init();

    Integer getType();

}
