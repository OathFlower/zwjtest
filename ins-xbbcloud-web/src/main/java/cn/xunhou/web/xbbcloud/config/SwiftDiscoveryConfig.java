package cn.xunhou.web.xbbcloud.config;

import cn.xunhou.cloud.core.util.SystemUtil;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


@Configuration
public class SwiftDiscoveryConfig {
    @Resource
    private CuratorFramework curatorFramework;

    @Bean
    public ServiceInstanceDiscovery serviceInstanceDiscovery() {
        ServiceInstanceDiscovery serviceInstanceDiscovery = new ServiceInstanceDiscovery(curatorFramework, SystemUtil.getLogicAreaStr());
        serviceInstanceDiscovery.addService("ins-hrostaff-platform");
        serviceInstanceDiscovery.addService("ins-userxh-platform");
        serviceInstanceDiscovery.addService("ins-starpro-platform");
        serviceInstanceDiscovery.addService("ins-xbb-platform");
        serviceInstanceDiscovery.load();
        return serviceInstanceDiscovery;
    }


}
