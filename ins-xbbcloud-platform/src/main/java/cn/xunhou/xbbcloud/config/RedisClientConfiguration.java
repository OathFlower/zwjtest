package cn.xunhou.xbbcloud.config;

import cn.xunhou.cloud.core.datasource.IXbbDataSourceProperties;
import cn.xunhou.cloud.redis.configuration.RedisConfiguration;
import cn.xunhou.cloud.redis.configuration.XbbRedisTemplateFactory;
import cn.xunhou.cloud.redis.generate.RedisIDWorker;
import cn.xunhou.cloud.redis.lock.IRedisLockService;
import cn.xunhou.cloud.redis.lock.impl.RedisLockServiceImpl;
import cn.xunhou.xbbcloud.common.constants.RedisConstant;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;

/**
 * @author litb
 * @since 2022-6-21
 * <p>
 * redis配置
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


    @Bean("redisTemplate")
    public RedisTemplate<String, String> redisTemplate(IXbbDataSourceProperties xbbDataSourceProperties, RedisConfiguration redisConfiguration) {
        return XbbRedisTemplateFactory.builderStringRedisTemplate(xbbDataSourceProperties, redisConfiguration);
    }

    @Bean
    public RedisIDWorker redisIDWorker(RedisTemplate<String, String> redisTemplate) {
        return new RedisIDWorker(redisTemplate);
    }

    @Bean
    public IRedisLockService redisLockService(RedissonClient redissonClient) {
        return new RedisLockServiceImpl(redissonClient);
    }
}

