package cn.xunhou.web.xbbcloud.listener;

import cn.xunhou.web.xbbcloud.common.IThreadLocalPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.RequestHandledEvent;

import java.util.List;

/**
 * 监听请求结束事件
 *
 * @author sha.li
 * @since 2021-03-05
 */
@Component
@Slf4j
public class RequestHandledEventListener implements ApplicationListener<RequestHandledEvent> {
    @Autowired
    List<IThreadLocalPool> threadLocalPools;

    @Override
    public void onApplicationEvent(@NonNull RequestHandledEvent event) {
        // 清空threadLocal 中信息，防止线程池重复使用与内存泄漏
        threadLocalPools.forEach(IThreadLocalPool::remove);
    }
}
