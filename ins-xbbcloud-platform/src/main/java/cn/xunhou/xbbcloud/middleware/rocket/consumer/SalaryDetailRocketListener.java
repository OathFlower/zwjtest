package cn.xunhou.xbbcloud.middleware.rocket.consumer;

import cn.hutool.core.collection.CollectionUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.rocketmq.AbstractXbbMessageListener;
import cn.xunhou.cloud.rocketmq.XbbCommonRocketListener;
import cn.xunhou.cloud.rocketmq.XbbMessageBuilder;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.grpc.proto.asset.AssetXhServerProto;
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
import java.util.ArrayList;
import java.util.List;


@Slf4j
@XbbCommonRocketListener(tag = "XCY_WITHOUT_CARD_PAY", applicationName = RocketConstant.APPLICATION_NAME)
public class SalaryDetailRocketListener extends AbstractXbbMessageListener {
    @Resource
    private SalaryService salaryService;
    @Resource
    private SalaryDetailRepository salaryDetailRepository;

    @Override
    public void dispose(XbbMessageBuilder.XbbMessage xbbMessage, ConsumeContext context) {

        TransactionRocketMessage transactionRocketMessage = XbbJsonUtil.fromJsonBytes(xbbMessage.getBody(), TransactionRocketMessage.class);
        log.info("处理发薪明细解析后Obj" + XbbJsonUtil.toJsonString(transactionRocketMessage.toString()));
        SpringContextUtil.getBean(SalaryDetailRocketListener.class).handler(transactionRocketMessage);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void handler(TransactionRocketMessage transactionRocketMessage) {
        if (AssetXhServerProto.EnumSystemPayType.SP_XCY_WITHOUT_CARD_PAY_VALUE != transactionRocketMessage.getSystemPayType()) {
            return;
        }
        List<SalaryDetailEntity> salaryDetailEntityList = new ArrayList<>();
        //根据明细id 批量更新状态
        for (TransactionRocketMessage.TransactionDetailMessage transactionDetailMessage :
                transactionRocketMessage.getDetailMessageList()) {
            SalaryDetailEntity salaryDetailEntity = new SalaryDetailEntity();
            //处理推送过来带_重试次数的detailNo
            String afterSubDetailId = transactionDetailMessage.getDetailNo().substring(0, transactionDetailMessage.getDetailNo().indexOf("_"));
            salaryDetailEntity.setId(Long.valueOf(afterSubDetailId));
            if (transactionDetailMessage.getDetailFailed()) {
                salaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.PAY_FAIL.getNumber());
                salaryDetailEntity.setFailureReason(transactionDetailMessage.getErrorMessage());

            } else {
                salaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.ALREADY_PAID.getNumber());
                //记录提现编号
                SalaryDetailEntity.ExpandInfo expandInfo = new SalaryDetailEntity.ExpandInfo();
                expandInfo.setWithdrawalNo(transactionDetailMessage.getDetailNo());
                salaryDetailEntity.setExpandJson(XbbJsonUtil.toJsonString(expandInfo));
            }
            salaryDetailEntityList.add(salaryDetailEntity);
        }
        if (CollectionUtil.isNotEmpty(salaryDetailEntityList)) {
            SalaryDetailEntity byId = salaryDetailRepository.findById(salaryDetailEntityList.get(0).getId(), SalaryDetailEntity.class);
            for (SalaryDetailEntity salaryDetailEntity :
                    salaryDetailEntityList) {
                salaryDetailEntity.setBatchId(byId.getBatchId());
            }
            salaryService.updateDetailStatus(salaryDetailEntityList);
        }

    }

}
