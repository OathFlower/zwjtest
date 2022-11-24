package cn.xunhou.xbbcloud.config;

import cn.xunhou.cloud.core.datasource.IXbbDataSourceProperties;
import cn.xunhou.cloud.core.datasource.RocketDataSourceProperties;
import cn.xunhou.cloud.rocketmq.RocketProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

/**
 * @author sha.li
 * @since 2022-6-21
 */
@Configuration
public class RocketConfiguration {
    @Bean
    public RocketProperties rocketProperties(IXbbDataSourceProperties xbbDataSourceProperties) {
        RocketDataSourceProperties rocketDataSourceProperties = xbbDataSourceProperties.findRocketMQDataSourceProperties();
        if (ObjectUtils.isEmpty(rocketDataSourceProperties)) {
            throw new RuntimeException("rocket config is nil");
        }
        RocketProperties rocketProperties = new RocketProperties();
        rocketProperties.setNamesrvAddr(rocketDataSourceProperties.getAddress());
        rocketProperties.setSecretKey(rocketDataSourceProperties.getAccessSecret());
        rocketProperties.setAccessKey(rocketDataSourceProperties.getAccessKey());
        return rocketProperties;
    }

}

