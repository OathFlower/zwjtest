package cn.xunhou.xbbcloud.rpc.approve.handle;

import cn.xunhou.xbbcloud.rpc.approve.bean.Context;

/**
 * @Author: chenning
 * @Date: 2022/09/28/13:46
 * @Description: 触发事件
 */
public interface HandleEvent {

    /**
     * 增加操作
     * @param ctx
     */
    void addHandle(Context ctx);

    /**
     * 通过操作
     * @param ctx
     */
    void passHandle(Context ctx);

    /**
     * 拒绝
     * @param ctx
     */
    void rejectHandle(Context ctx);

    /**
     * 更新操作
     * @param ctx
     */
    void editHandle(Context ctx);
}
