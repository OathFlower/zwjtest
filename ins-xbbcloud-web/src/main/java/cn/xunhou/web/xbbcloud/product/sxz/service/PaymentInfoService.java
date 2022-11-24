package cn.xunhou.web.xbbcloud.product.sxz.service;


import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.web.xbbcloud.product.sxz.dao.WxPaymentRepository;
import cn.xunhou.web.xbbcloud.product.sxz.entity.WxPaymentEntity;
import cn.xunhou.web.xbbcloud.product.sxz.dto.WeChatNotify;
import cn.xunhou.web.xbbcloud.product.sxz.enums.PayTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PaymentInfoService {
    @Autowired
    private WxPaymentRepository wxPaymentRepository;

    /**
     * 记录支付日志
     *
     * @param plainText
     */
    @Transactional(rollbackFor = Exception.class)
    public void createPaymentInfo(String plainText) {

        log.info("记录支付日志");


        WeChatNotify weChatNotify = XbbJsonUtil.fromJsonString(plainText, WeChatNotify.class);


        //订单号
        Long orderId = Long.valueOf(weChatNotify.getOut_trade_no());
        //业务编号
        String transactionId = weChatNotify.getTransaction_id();
        //支付类型
        String tradeType = weChatNotify.getTrade_type();
        //交易状态
        String tradeState = weChatNotify.getTrade_state();
        //用户实际支付金额
        Integer payerTotal = weChatNotify.getAmount().getPayer_total();
        WxPaymentEntity paymentInfo = new WxPaymentEntity();


        paymentInfo.setOrderId(orderId);
        paymentInfo.setPaymentType(PayTypeEnum.WXPAY.getType());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType(tradeType);
        paymentInfo.setTradeState(tradeState);
        paymentInfo.setPayerTotal(payerTotal);
        paymentInfo.setContent(plainText);

        wxPaymentRepository.insert(paymentInfo);
    }
}
