package cn.xunhou.xbbcloud.middleware.rocket.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@NoArgsConstructor
public class ImportBillMessage {
    //第三方详情id（或详情批次号）
    private Long thirdPartyId;
    //关联账单明细id
    private Long staffBillId;
    //收款账号
    private String receivingAccount;
    //开户行
    private String bankName;
    //发薪方式
    private Integer payRollType;
    //成功 true 失败 false
    private Boolean resultFlag = Boolean.FALSE;
    //失败原因
    private String errorMsg;
    //金额
    private BigDecimal amount;
}
