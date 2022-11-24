package cn.xunhou.xbbcloud.rpc.dao;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.framework.XbbApplication;
import cn.xunhou.cloud.framework.util.SystemUtil;
import cn.xunhou.xbbcloud.rpc.salary.dao.SalaryMerchantFlowRepository;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantFlowEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.SalaryMerchantFlowPageParam;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryMerchantFlowResult;
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
import java.util.List;


@Slf4j
@SpringBootTest(classes = XbbApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(loader = SwiftApplicationUnit.class)
@RunWith(PowerMockRunner.class) // 告诉JUnit使用PowerMockRunner进行测试
@PrepareForTest({SystemUtil.class})
// 所有需要测试的类列在此处，适用于模拟final类或有final, private, static, native方法的类
@PowerMockRunnerDelegate(SpringRunner.class) //RunWith依然是PowerMock，那这里Delegate委托给spring
@PowerMockIgnore({"javax.*.*", "com.sun.*", "org.xml.*", "org.apache.*"})
public class SalaryMerchantFlowRepositoryTest {
    static {
        System.setProperty("SystemRuntimeEnvironment", "qa");
        System.setProperty("area", "QA39");
    }

    @Resource
    private SalaryMerchantFlowRepository salaryMerchantFlowRepository;

    @Test
    public void findByIdNull() {
        SalaryMerchantFlowEntity r = salaryMerchantFlowRepository.findById(0L);
        log.info("r = {}", JSONUtil.toJsonStr(r));
    }

    @Test
    public void findById() {
        SalaryMerchantFlowEntity r = salaryMerchantFlowRepository.findById(41L);
        log.info("r = {}", JSONUtil.toJsonStr(r));
    }

    @Test
    public void findByIdForResultNull() {
        SalaryMerchantFlowResult r = salaryMerchantFlowRepository.findByIdForResult(0L);
        log.info("r = {}", JSONUtil.toJsonStr(r));
    }

    @Test
    public void findByIdForResult() {
        SalaryMerchantFlowResult r = salaryMerchantFlowRepository.findByIdForResult(41L);
        log.info("r = {}", JSONUtil.toJsonStr(r));
    }

    @Test
    public void findPageList() {
        SalaryMerchantFlowPageParam param = new SalaryMerchantFlowPageParam();
        param.setTenantId(1L);
        param.setEndTime(DateUtil.parse("2022-09-20 16:30:00").toTimestamp());
        param.setPage(2).setPageSize(2);
        PagePojoList<SalaryMerchantFlowResult> r = salaryMerchantFlowRepository.findPageList(param);
        log.info("r1 = {}", JSONUtil.toJsonStr(r));
//        param.setPageSize(null);
//        r = salaryMerchantFlowRepository.findPageList2(param);
//        log.info("r2 = {}", JSONUtil.toJsonStr(r));
//        param.setPageSize(4);
//        r = salaryMerchantFlowRepository.findPageList2(param);
//        log.info("r3 = {}", JSONUtil.toJsonStr(r));
    }

    @Test
    public void queryPageList() {
        List<SalaryMerchantFlowEntity> r = salaryMerchantFlowRepository.queryPageList(null);
        log.info("r total = {}, r = {}", r.size(), JSONUtil.toJsonStr(r));
    }
}