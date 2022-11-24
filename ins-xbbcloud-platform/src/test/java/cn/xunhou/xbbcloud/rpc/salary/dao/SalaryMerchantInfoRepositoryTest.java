package cn.xunhou.xbbcloud.rpc.salary.dao;

import cn.xunhou.cloud.framework.XbbApplication;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantInfoEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.QuerySalaryMerchantInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = XbbApplication.class)
public class SalaryMerchantInfoRepositoryTest {
    static {
        System.setProperty("SystemRuntimeEnvironment", "qa");
        System.setProperty("area", "QA39");
    }

    @Resource
    private SalaryMerchantInfoRepository salaryMerchantInfoRepository;


    @Test
    public void query() {
        List<SalaryMerchantInfoEntity> list = salaryMerchantInfoRepository.query(new QuerySalaryMerchantInfo().setTenantTypes(Collections.singletonList(SalaryServerProto.EnumTenantType.SAAS_VALUE)));

        log.info("r = {}", list);

    }
}