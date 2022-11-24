package cn.xunhou.xbbcloud.middleware.rocket.consumer;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.redis.lock.IRedisLockService;
import cn.xunhou.cloud.rocketmq.AbstractXbbMessageListener;
import cn.xunhou.cloud.rocketmq.XbbCommonRocketListener;
import cn.xunhou.cloud.rocketmq.XbbMessageBuilder;
import cn.xunhou.xbbcloud.common.constants.CommonConst;
import cn.xunhou.xbbcloud.common.constants.RocketConstant;
import cn.xunhou.xbbcloud.common.enums.EnumDispatchStatus;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.TransactionRocketMessage;
import cn.xunhou.xbbcloud.rpc.salary.dao.FundDispatchingRepository;
import cn.xunhou.xbbcloud.rpc.salary.entity.FundDispatchingEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.QueryFundDispatchingParam;
import cn.xunhou.xbbcloud.rpc.salary.service.FundDispatchingService;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wangkm
 */
@Slf4j
@XbbCommonRocketListener(tag = RocketConstant.XCY_FUND_DISPATCHING_MSG_TAG, applicationName = RocketConstant.APPLICATION_NAME)
public class FundDispatchingNodeListener extends AbstractXbbMessageListener {

    @Resource
    private FundDispatchingRepository fundDispatchingRepository;
    @Resource
    private FundDispatchingService fundDispatchingService;
    @Resource
    private IRedisLockService redisLockService;

    /**
     * @param xbbMessage
     * @param consumeContext
     * @throws UnsupportedEncodingException
     */
    @Override
    public void dispose(XbbMessageBuilder.XbbMessage xbbMessage, ConsumeContext consumeContext) throws UnsupportedEncodingException {
        log.info("{}.dispose 资金调度节点MQ", this.getClass());
        log.info("message = {}", XbbJsonUtil.toJsonString(xbbMessage));
        String redisLockKey = RocketConstant.XCY_FUND_DISPATCHING_MSG_TAG + "::" + xbbMessage.getKey();
        try {
            //校验重试
            if (!redisLockService.tryLock(redisLockKey, TimeUnit.SECONDS, 1, 5)) {
                return;
            }
            handler(XbbJsonUtil.fromJsonBytes(xbbMessage.getBody(), TransactionRocketMessage.class));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("资金调度节点 异常", e);
        } finally {
            redisLockService.unlock(redisLockKey);
        }
    }

    public void handler(TransactionRocketMessage transactionRocketMessage) {
        log.info(" TransactionRocketMessage = {}", XbbJsonUtil.toJsonString(transactionRocketMessage));
        List<Long> ids = Lists.newArrayList();
        for (TransactionRocketMessage.TransactionDetailMessage detailMessage : transactionRocketMessage.getDetailMessageList()) {
            Long id = Long.valueOf(CollUtil.getFirst(CharSequenceUtil.split(detailMessage.getDetailNo(), CommonConst.UNDERLINE)));
            if (Boolean.TRUE.equals(detailMessage.getDetailFailed())) {
                fundDispatchingRepository.updateState(id, EnumDispatchStatus.FAILURE, detailMessage.getErrorMessage() == null ? transactionRocketMessage.getGlobalErrorMessage() : detailMessage.getErrorMessage());
            } else {
                fundDispatchingRepository.updateSuccessState(id, detailMessage.getAssetDetailNo());
            }
            ids.add(id);
        }
        List<FundDispatchingEntity> fundDispatchingEntityList = fundDispatchingRepository.query(new QueryFundDispatchingParam().setIds(ids));
        fundDispatchingService.pushFundDispatchProcessResult(CollUtil.getFirst(fundDispatchingEntityList).getTransactionMain());
    }
}
