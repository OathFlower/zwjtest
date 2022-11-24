package cn.xunhou.xbbcloud.middleware.rocket.consumer;

import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.framework.util.SystemUtil;
import cn.xunhou.cloud.rocketmq.AbstractXbbMessageListener;
import cn.xunhou.cloud.rocketmq.XbbCommonRocketListener;
import cn.xunhou.cloud.rocketmq.XbbMessageBuilder;
import cn.xunhou.xbbcloud.common.constants.RocketConstant;
import cn.xunhou.xbbcloud.common.enums.EnumAttendanceBillStatus;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.ImportBillMessage;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceSalaryBillBatchDetailRepository;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceSalaryBillBatchDetailEntity;
import com.aliyun.openservices.ons.api.ConsumeContext;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@XbbCommonRocketListener(tag = RocketConstant.SETTLEMENT_SAAS_TENANT_SALARY, applicationName = RocketConstant.APPLICATION_NAME)
public class AttendanceBillBatchListener extends AbstractXbbMessageListener {

    @Resource
    private AttendanceSalaryBillBatchDetailRepository attendanceSalaryBillBatchDetailRepository;

    @Override
    public void dispose(XbbMessageBuilder.XbbMessage xbbMessage, ConsumeContext context) {
        String jsonStr = new String(xbbMessage.getBody(), StandardCharsets.UTF_8);
        log.info("AttendanceBillBatchListener --- > 结算审核返回结果jsonStr = " + jsonStr);
        List<ImportBillMessage> importBillMessages = JSONUtil.toList(jsonStr, ImportBillMessage.class);
        for (ImportBillMessage importBillMessage : importBillMessages) {
            handler(importBillMessage);
        }
    }

    public void handler(ImportBillMessage importBillMessage) {
        try {
            Long thirdPartyId = importBillMessage.getThirdPartyId();
            AttendanceSalaryBillBatchDetailEntity attendanceSalaryBillBatchDetailEntity = attendanceSalaryBillBatchDetailRepository.findById(thirdPartyId, AttendanceSalaryBillBatchDetailEntity.class);
            if (attendanceSalaryBillBatchDetailEntity == null) {
                log.info("AttendanceBillBatchListener --- > 未找到账单详情数据，importBillMessage = " + JSONUtil.toJsonStr(importBillMessage));
                return;
            }
            AttendanceSalaryBillBatchDetailEntity update = new AttendanceSalaryBillBatchDetailEntity();
            update.setId(attendanceSalaryBillBatchDetailEntity.getId());
            update.setStatus(EnumAttendanceBillStatus.BILL_REVIEWED.getCode());
            if (importBillMessage.getResultFlag()) {
                //发薪成功
                update.setBankName(importBillMessage.getBankName());
                update.setBankCardNo(importBillMessage.getReceivingAccount());
                update.setSub_status(1);
                update.setPayType(importBillMessage.getPayRollType());
                update.setMoney(importBillMessage.getAmount());
            } else {
                update.setFail_reason(importBillMessage.getErrorMsg());
            }
            attendanceSalaryBillBatchDetailRepository.updateById(attendanceSalaryBillBatchDetailEntity.getId(), update);
        } catch (Exception e) {
            log.info("AttendanceBillBatchListener ---> e" + e);
            if (SystemUtil.isOffline()) {
                throw e;
            }
        }
    }
}
