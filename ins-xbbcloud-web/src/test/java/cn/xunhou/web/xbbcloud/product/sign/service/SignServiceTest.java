package cn.xunhou.web.xbbcloud.product.sign.service;

import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.framework.XbbApplication;
import cn.xunhou.web.xbbcloud.product.user.param.UserBalanceQueryParam;
import cn.xunhou.web.xbbcloud.product.user.result.UserBalanceDetailResult;
import cn.xunhou.web.xbbcloud.product.user.result.UserBalanceResult;
import cn.xunhou.web.xbbcloud.product.user.service.UserBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = XbbApplication.class)
public class SignServiceTest {
    static {
        System.setProperty("SystemRuntimeEnvironment", "qa");
        System.setProperty("area", "QA39");
    }

    @Resource
    private SignService signService;
    @Resource
    private UserBalanceService userBalanceService;

    @Test
    public void getIdCardAndUserXh() {
        UserBalanceQueryParam userBalanceQueryParam = new UserBalanceQueryParam();
        JsonListResponse<UserBalanceResult> page = userBalanceService.page(userBalanceQueryParam);
//        UserBalanceQueryParam param = new UserBalanceQueryParam();
//        JsonListResponse<UserBalanceDetailResult> detail = userBalanceService.detail(5213L);
    }
}