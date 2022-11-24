package cn.xunhou.xbbcloud.middleware.rocket.consumer;

import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.rocketmq.AbstractXbbMessageListener;
import cn.xunhou.cloud.rocketmq.XbbCommonRocketListener;
import cn.xunhou.cloud.rocketmq.XbbMessageBuilder;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.xbbcloud.common.constants.RocketConstant;
import cn.xunhou.xbbcloud.config.JdbcConfiguration;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.TransactionRocketMessage;
import cn.xunhou.xbbcloud.rpc.salary.dao.SalaryDetailRepository;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryDetailEntity;
import cn.xunhou.xbbcloud.rpc.salary.service.SalaryService;
import com.aliyun.openservices.ons.api.ConsumeContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@XbbCommonRocketListener(tag = "XBB_WITHDRAWL_RESULT_TAG", applicationName = RocketConstant.APPLICATION_NAME)
public class WithDrawRocketListener extends AbstractXbbMessageListener {
    @Resource
    private SalaryService salaryService;
    @Resource
    private SalaryDetailRepository salaryDetailRepository;

    @Override
    public void dispose(XbbMessageBuilder.XbbMessage xbbMessage, ConsumeContext context) {
        String jsonStr = new String(xbbMessage.getBody(), StandardCharsets.UTF_8);
        log.info("XBB_WITHDRAWL_RESULT_TAG ---->jsonStr = " + jsonStr);
        List<TransactionRocketMessage.TransactionDetailMessage> transactionDetailMessageList = JSONUtil.toList(jsonStr, TransactionRocketMessage.TransactionDetailMessage.class);

        SpringContextUtil.getBean(WithDrawRocketListener.class).handler(transactionDetailMessageList);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void handler(List<TransactionRocketMessage.TransactionDetailMessage> transactionDetailMessageList) {
        List<SalaryDetailEntity> salaryDetailEntityList = new ArrayList<>();
        //根据明细id 批量更新状态
        for (TransactionRocketMessage.TransactionDetailMessage transactionDetailMessage :
                transactionDetailMessageList) {
            SalaryDetailEntity salaryDetailEntity = new SalaryDetailEntity();
            salaryDetailEntity.setId(Long.valueOf(transactionDetailMessage.getDetailNo()));
            //接收的C端的 提现中(5)  提现成功(10) 提现失败(9)
            salaryDetailEntity.setStatus(transactionDetailMessage.getStatus());
            //提现失败的 需要记录失败原因
            if (SalaryServerProto.EnumSalaryDetailStatus.WITHDRAW_FAILED.getNumber() == transactionDetailMessage.getStatus()) {
                salaryDetailEntity.setFailureReason(transactionDetailMessage.getErrorMessage());
            }
            if (SalaryServerProto.EnumSalaryDetailStatus.WITHDRAW_SUCCESS.getNumber() == transactionDetailMessage.getStatus()) {
                //记录提现编号
                SalaryDetailEntity.ExpandInfo expandInfo = new SalaryDetailEntity.ExpandInfo();
                expandInfo.setWithdrawalNo(transactionDetailMessage.getAssetDetailNo());
                salaryDetailEntity.setExpandJson(XbbJsonUtil.toJsonString(expandInfo));
            }
            salaryDetailEntity.setUpdatedAt(null);
            salaryDetailRepository.updateById(salaryDetailEntity.getId(), salaryDetailEntity);
        }
    }

}
