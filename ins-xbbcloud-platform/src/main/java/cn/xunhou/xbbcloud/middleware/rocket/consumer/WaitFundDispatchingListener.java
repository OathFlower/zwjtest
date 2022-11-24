package cn.xunhou.xbbcloud.middleware.rocket.consumer;

import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.redis.lock.IRedisLockService;
import cn.xunhou.cloud.rocketmq.AbstractXbbMessageListener;
import cn.xunhou.cloud.rocketmq.XbbCommonRocketListener;
import cn.xunhou.cloud.rocketmq.XbbMessageBuilder;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.xbbcloud.common.constants.RocketConstant;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.common.helper.NotifyHelper;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendWaitFundDispatchingMessage;
import cn.xunhou.xbbcloud.middleware.rocket.producer.RocketMsgService;
import cn.xunhou.xbbcloud.rpc.salary.handler.event.FundDispatchingEvent;
import cn.xunhou.xbbcloud.rpc.salary.service.FundDispatchingService;
import cn.xunhou.xbbcloud.rpc.salary.service.SalaryService;
import com.aliyun.openservices.ons.api.ConsumeContext;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;


@Slf4j
@XbbCommonRocketListener(tag = RocketConstant.WAIT_FUND_DISPATCHING, applicationName = RocketConstant.APPLICATION_NAME)
public class WaitFundDispatchingListener extends AbstractXbbMessageListener {
    @Resource
    private IRedisLockService redisLockService;
    @Resource
    private RocketMsgService rocketMsgService;

    @Resource
    private FundDispatchingService fundDispatchingService;

    @Resource
    private SalaryService salaryService;

    @Override
    public void dispose(XbbMessageBuilder.XbbMessage xbbMessage, ConsumeContext context) {
        String redisLockKey = xbbMessage.getKey();
        try {
            String jsonStr = new String(xbbMessage.getBody(), StandardCharsets.UTF_8);
            SendWaitFundDispatchingMessage fundDispatchingMessage = XbbJsonUtil.fromJsonString(jsonStr, SendWaitFundDispatchingMessage.class);

            log.info("WaitFundDispatchingListener监听obj" + XbbJsonUtil.toJsonString(fundDispatchingMessage.toString()));
            //校验交易编号
            if (!redisLockService.tryLock(redisLockKey, TimeUnit.SECONDS, 1, 5)) {
                throw GrpcException.runtimeException(Status.ALREADY_EXISTS, "消息正在处理中,请勿重复发起");
            }
            handler(fundDispatchingMessage);

        } catch (Exception e) {
            log.info("WaitFundDispatchingListener失败", e);
        } finally {
            redisLockService.unlock(redisLockKey);
        }
    }

    private void handler(SendWaitFundDispatchingMessage fundDispatchingMessage) {
        FundDispatchingEvent fundDispatchingEvent = fundDispatchingService.getFundDispatchingEvent(String.valueOf(fundDispatchingMessage.getBatchId()));
        if (SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE == fundDispatchingMessage.getTenantType()) {
            if (fundDispatchingEvent.getFundDispatchingEnd()) {
                fundDispatchingMessage.setDelayTime(System.currentTimeMillis() + (1000 * 60 * 5));
                if (fundDispatchingMessage.getCount() >= 5) {
                    //接钉钉报警
                    SpringContextUtil.getBean(NotifyHelper.class).sendDdMessage(String.format("等待资金调度完成超时：%s", JSONUtil.toJsonStr(fundDispatchingMessage)));
                } else {
                    fundDispatchingMessage.setCount(fundDispatchingMessage.getCount() + 1);
                    rocketMsgService.sendWaitFundDispatching(fundDispatchingMessage);
                }
            } else {
                Boolean dispatchSuccess = fundDispatchingEvent.getDispatchSuccess();
                if (Boolean.TRUE.equals(dispatchSuccess)) {
                    if (Boolean.TRUE.equals(fundDispatchingMessage.getXcxWithdraw())) {
                        salaryService.pushWithdraw(fundDispatchingMessage.getSalaryDetailEntityList(), fundDispatchingMessage.getSpecialMerchantId(), fundDispatchingMessage.getPayeeMerchantName(), fundDispatchingMessage.getPayeeMerchantNo());
                    } else {
                        salaryService.transferWx("2ND" + fundDispatchingMessage.getBatchId(), fundDispatchingMessage.getPayeeMerchantNo(), fundDispatchingMessage.getPayeeMerchantName(), fundDispatchingMessage.getSpecialMerchantId(), fundDispatchingMessage.getSalaryDetailEntityList());
                    }

                } else {
                    //调度失败  具体逻辑处理在监听器的失败方法 此处只做日志打印
                    log.info("认证完成之后，调度失败");
                }
            }
        }
    }
}
