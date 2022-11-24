package cn.xunhou.web.xbbcloud.config;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceInstanceDiscovery {

    private CuratorFramework curatorFramework;

    private String currentArea = "qapub";

    private static final String PREFIX_SERVICE = "/config/public/rpc/registry";

    private static final ConcurrentHashMap<String, List<String>> CLUSTER = new ConcurrentHashMap<>();

    private List<String> services = new ArrayList<>();

    private ServiceInstanceDiscovery() {
    }

    public ServiceInstanceDiscovery(CuratorFramework curatorFramework, String currentArea) {
        this.curatorFramework = curatorFramework;
        this.currentArea = currentArea;
    }

    public ServiceInstanceDiscovery(CuratorFramework curatorFramework, String currentArea, String... serviceName) {
        this.curatorFramework = curatorFramework;
        this.currentArea = currentArea;
        this.services.addAll(Arrays.asList(serviceName));
    }

    public void addService(String serviceName) {
        this.services.add(serviceName);
    }

    public void addService(String... serviceNames) {
        for (int i = 0; i < serviceNames.length; i++) {
            this.services.add(serviceNames[i]);
        }
    }

    public void load() {
        new Thread(() -> {
            this.services.forEach(s -> {
                discovery(s);
            });

        }).start();
    }


    public String getBalanceInstance(String serviceName) throws Exception {
        List<String> instances = CLUSTER.get(serviceName);
        if (instances == null || instances.size() <= 0) {
            throw new Exception("当前服务不可用,未查询到注册的服务节点:" + serviceName);
        }
        Random random = new Random(instances.size());
        return instances.get(random.nextInt(instances.size()));
    }

    private void discovery(String serviceName) {
        List<String> instances = new ArrayList<>();
        try {
            List<String> serviceInstances = this.curatorFramework.getZookeeperClient().getZooKeeper().getChildren(PREFIX_SERVICE + "/" + serviceName, watchedEvent -> {
                discovery(serviceName);
            });
            serviceInstances.forEach(s -> {
                if (isCurrentArea(serviceName, s)) {
                    instances.add("http://" + s);
                }
            });
            CLUSTER.put(serviceName, instances);
        } catch (Exception e) {
            log.warn("discovery err:", e);
        }
    }

    private boolean isCurrentArea(String serviceName, String instance) {
        try {
            byte[] buff = this.curatorFramework.getZookeeperClient().getZooKeeper().getData(PREFIX_SERVICE + "/" + serviceName + "/" + instance, false, null);
            if (buff != null) {
                JSON jsonObject = JSONUtil.parse(new String(buff, StandardCharsets.UTF_8));
                Object areaObj = jsonObject.getByPath("area");
                String area = "qapub";
                if (areaObj != null) {
                    area = String.valueOf(areaObj);
                }
                if (currentArea.equals(area)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("isCurrentArea err:", e);
        }
        return false;
    }

}