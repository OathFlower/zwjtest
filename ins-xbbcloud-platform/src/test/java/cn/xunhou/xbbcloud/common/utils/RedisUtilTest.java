package cn.xunhou.xbbcloud.common.utils;

import cn.xunhou.cloud.framework.XbbApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = XbbApplication.class)
public class RedisUtilTest {
    static {
        System.setProperty("SystemRuntimeEnvironment", "qa");
        System.setProperty("area", "QA39");
    }

    @Test
    public void generateNo() {
        long max = 1000000;
        for (int i = 0; i < 10; i++) {
            Long v = max + i;
            log.info((v + "").substring(1, (max + "").length()));
        }
    }

    @Test
    public void generateNos() {
        List<String> r = RedisUtil.generateNos("xb", 13);
        log.info("r = {}", r);
    }
}