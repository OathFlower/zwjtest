package cn.xunhou.xbbcloud.rpc.dao;

import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.framework.XbbApplication;
import cn.xunhou.cloud.framework.util.SystemUtil;
import cn.xunhou.xbbcloud.rpc.salary.dao.SalaryMerchantInfoRepository;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@Slf4j
@SpringBootTest(classes = XbbApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(loader = SwiftApplicationUnit.class)
@RunWith(PowerMockRunner.class) // 告诉JUnit使用PowerMockRunner进行测试
@PrepareForTest({SystemUtil.class})
// 所有需要测试的类列在此处，适用于模拟final类或有final, private, static, native方法的类
@PowerMockRunnerDelegate(SpringRunner.class) //RunWith依然是PowerMock，那这里Delegate委托给spring
@PowerMockIgnore({"javax.*.*", "com.sun.*", "org.xml.*", "org.apache.*"})
public class SalaryMerchantInfoRepositoryTest {
    static {
        System.setProperty("SystemRuntimeEnvironment", "qa");
        System.setProperty("area", "QA39");
    }

    @Resource
    private SalaryMerchantInfoRepository salaryMerchantInfoRepository;

    @Test
    public void findByIdForNull() {
        SalaryMerchantInfoEntity r = salaryMerchantInfoRepository.findById(111L);
        log.info("r = {}", JSONUtil.toJsonStr(r));
    }

    @Test
    public void findByIdForHas() {
        SalaryMerchantInfoEntity r = salaryMerchantInfoRepository.findById(111L);
        log.info("r = {}", JSONUtil.toJsonStr(r));
    }

    //    @Test
    public void saveById() {
    }
}