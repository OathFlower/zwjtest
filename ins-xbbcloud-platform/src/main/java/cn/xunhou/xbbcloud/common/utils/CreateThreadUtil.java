package cn.xunhou.xbbcloud.common.utils;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CreateThreadUtil {
    public static ThreadPoolExecutor createThread(int runSize, String namePrefix) {
        //创建线程工厂
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNamePrefix(namePrefix).build();
        //创建线程池
        return new ThreadPoolExecutor(runSize,
                runSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024),
                namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }
}
