package cn.xunhou.xbbcloud.rpc.dao;

import cn.xunhou.xbbcloud.rpc.schedule.dao.ScheduleDetailRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Date;

/**
 * @author litb
 * @date 2022/9/20 16:26
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = cn.xunhou.cloud.framework.XbbApplication.class)
class ScheduleDetailRepositoryTest {

    @Autowired
    private ScheduleDetailRepository scheduleDetailRepository;

    @Test
    void findList() {
        scheduleDetailRepository.findList(null, null, null, new Date(10000), new Date(2000), Collections.singletonList(12L), null);
    }

    @Test
    void findScheduleIds() {
        System.out.println(scheduleDetailRepository.findScheduleIds(744L, 1));
        ;
    }
}