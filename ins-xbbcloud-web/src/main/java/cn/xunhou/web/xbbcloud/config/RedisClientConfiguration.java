package cn.xunhou.web.xbbcloud.config;

import cn.xunhou.cloud.core.datasource.IXbbDataSourceProperties;
import cn.xunhou.cloud.redis.configuration.RedisConfiguration;
import cn.xunhou.cloud.redis.configuration.XbbRedisTemplateFactory;
import cn.xunhou.cloud.redis.generate.RedisIDWorker;
import cn.xunhou.cloud.redis.lock.IRedisLockService;
import cn.xunhou.cloud.redis.lock.impl.RedisLockServiceImpl;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author sha.li
 * @since 2022-6-21
 */
@Configuration
public class RedisClientConfiguration {

    @Bean
    public RedissonClient redissonClient(IXbbDataSourceProperties xbbDataSourceProperties, RedisConfiguration redisConfiguration) {
        return XbbRedisTemplateFactory.buildRedisson(xbbDataSourceProperties, redisConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplateForObject(IXbbDataSourceProperties xbbDataSourceProperties, RedisConfiguration redisConfiguration) {
        return XbbRedisTemplateFactory.builderRedisTemplate(xbbDataSourceProperties, redisConfiguration);
    }


    @Bean(name = "redisTemplate")
    public RedisTemplate<String, String> redisTemplate(IXbbDataSourceProperties xbbDataSourceProperties, RedisConfiguration redisConfiguration) {
        return XbbRedisTemplateFactory.builderStringRedisTemplate(xbbDataSourceProperties, redisConfiguration);
    }

    @Bean
    public RedisIDWorker redisIDWorker(RedisTemplate<String, String> redisTemplate) {
        return new RedisIDWorker(redisTemplate);
    }


    @Bean
    @DependsOn(value = {"redissonClient"})
    public IRedisLockService getRedisLockService(RedissonClient redissonClient) {
        return new RedisLockServiceImpl(redissonClient);
    }
}

