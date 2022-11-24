package cn.xunhou.xbbcloud.config;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class XhThreadPoolConfiguration {
    public static final String LAZY_TRACE_EXECUTOR = "lazyTraceExecutor";

    @Bean(LAZY_TRACE_EXECUTOR)
    public Executor lazyTraceExecutor(BeanFactory beanFactory) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // CUSTOMIZE HERE
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("LAZY_TRACE_EXECUTOR-");
        // DON'T FORGET TO INITIALIZE
        executor.initialize();
        return new LazyTraceExecutor(beanFactory, executor);
//        return XbbExecutorService.threadPoolExecutor(10, 10, 10, TimeUnit.SECONDS, 1000);
    }
}
