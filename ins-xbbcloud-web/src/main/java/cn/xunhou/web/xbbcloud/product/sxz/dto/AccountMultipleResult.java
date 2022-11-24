package cn.xunhou.web.xbbcloud.product.sxz.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * 用户综合信息
 */
@ToString
@Data
@Accessors(chain = true)
public class AccountMultipleResult {
    /**
     * 用户id
     */
    private Long id;

    /**
     * 手机号
     */
    private String tel;


    /**
     * 累计实付金额(分)
     */
    private Integer paymentFeeCount;


    /**
     * 开票金额(分)
     */
    private Integer receiptFeeCount;


    /**
     * 累计充值币
     */
    private Integer rechargeCoinCount;


    /**
     * 余额（剩余币）
     */
    private Integer accountBalance;

    /**
     * 享有权益
     */
    private List<HasServiceRecordResult> hasServiceRecordResultList;
}
