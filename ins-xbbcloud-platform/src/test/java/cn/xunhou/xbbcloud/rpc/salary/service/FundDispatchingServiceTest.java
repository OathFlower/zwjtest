package cn.xunhou.xbbcloud.rpc.salary.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;
import cn.xunhou.cloud.framework.XbbApplication;
import cn.xunhou.xbbcloud.common.constants.CommonConst;
import cn.xunhou.xbbcloud.common.enums.EnumDispatchStatus;
import cn.xunhou.xbbcloud.rpc.salary.entity.FundDispatchingEntity;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantInfoEntity;
import cn.xunhou.xbbcloud.rpc.salary.handler.event.FundDispatchingEvent;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = XbbApplication.class)
public class FundDispatchingServiceTest {
    static {
        System.setProperty("SystemRuntimeEnvironment", "qa");
        System.setProperty("area", "QA39");
    }

    @Resource
    private FundDispatchingService fundDispatchingService;

    @Resource
    private SalaryService salaryService;


    @Test
    public void fundDispatching() {
        SalaryMerchantInfoEntity salaryMerchantInfo = salaryService.queryMerchantInfo(10023L);
        BigDecimal f = new BigDecimal(1);
        BigDecimal s = new BigDecimal(2);
        BigDecimal a = new BigDecimal(3);

        fundDispatchingService.fundDispatching(salaryMerchantInfo, "wkm0000000004", f, s, a, 10001L);
        ThreadUtil.sleep(1000 * 60);
    }

    @Test
    public void fundDispatchingBack() {
        SalaryMerchantInfoEntity salaryMerchantInfo = salaryService.queryMerchantInfo(10023L);
        BigDecimal f = new BigDecimal(1);
        BigDecimal s = new BigDecimal(2);
        BigDecimal a = new BigDecimal(3);
        fundDispatchingService.fundDispatchingBack(salaryMerchantInfo, "BACKDWKM" + CommonConst.UNDERLINE + "00000010", f, s, a, 10001L);
    }

    @Test
    public void breakpointRetry() {
        FundDispatchingEvent fundDispatchingEvent = fundDispatchingService.getFundDispatchingEvent("BACKDWKM" + CommonConst.UNDERLINE + "00000010");

        List<FundDispatchingEntity> fundDispatchingEntityList = fundDispatchingEvent.getFundDispatchingEntityList();
        List<FundDispatchingEntity> retryFundDispatchingEntityList = Lists.newArrayList();
        for (FundDispatchingEntity fundDispatching : fundDispatchingEntityList) {
            if (EnumDispatchStatus.FAILURE.getCode().equals(fundDispatching.getDispatchStatus())) {
                fundDispatching.setDispatchStatus(EnumDispatchStatus.INIT.getCode())
                        .setRetryCount(fundDispatching.getRetryCount() + 1)
                        .setUpdatedAt(null);
                retryFundDispatchingEntityList.add(fundDispatching);
            }
        }
        if (CollUtil.isEmpty(retryFundDispatchingEntityList)) {
            throw new SystemRuntimeException("异常数据，请联系管理员！");
        }
        fundDispatchingService.retryFundDispatchStatus(retryFundDispatchingEntityList);
        fundDispatchingService.executeFundDispatch(retryFundDispatchingEntityList, 10001L);
    }
}