package cn.xunhou.web.xbbcloud.product.user.result;

import lombok.*;

import java.math.BigDecimal;

/**
 * 用户余额列表
 */
@Getter
@Setter
@ToString
public class UserBalanceResult {

    /**用户id*/
    private Long userXhCId;
    private String name;
    private String idCardNo;
    private String tel;
    /**薪酬云可提现余额*/
    private BigDecimal xcyAvailableBalance;
    /**结算发薪可提现余额*/
    private BigDecimal settlementAvailableBalance;
    /**其他发薪可提现余额*/
    private BigDecimal otherAvailableBalance;
    /**结算中金额（其实就是提现中余额，产品想不通非要叫这个）*/
    private BigDecimal lockingMoney;
    /**薪酬云累计收入*/
    private BigDecimal xcyTotalImcome;
    /**结算发薪累计收入*/
    private BigDecimal settlementTotalImcome;
    /**其他发薪累计收入*/
    private BigDecimal otherTotalImcome;
}

