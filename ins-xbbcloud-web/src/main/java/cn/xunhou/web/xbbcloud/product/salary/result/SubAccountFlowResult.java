package cn.xunhou.web.xbbcloud.product.salary.result;

import cn.xunhou.grpc.proto.asset.AssetXhServerProto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 子账户流水
 *
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SubAccountFlowResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 子账户id
     */
    private Long subAccountId;
    /**
     * 子账户编号
     */
    private String subAccountNo;
    /**
     * 主账户id
     */
    private Long subjectId;
    /**
     * 流水id
     */
    private Long flowId;
    /**
     * 创建时间 yyyy-MM-dd hh:mm:ss
     */
    private String createdAt;
    /**
     * 收支类型  0-借方（支出）  1-贷方（收入）
     */
    private Integer operationType;
    /**
     * 操作金额(元)
     */
    private String operationAmount;
    /**
     * 交易状态
     * //锁定
     * FT_LOCKED = 0;
     * //取消
     * FT_CANCELED = 1;
     * //完成
     * FT_COMPLETED = 2;
     * //处理中
     * FT_IN_PROGRESS = 3;
     * //等待处理
     * FT_WAITING = 4;
     *
     * @see AssetXhServerProto.EnumSubAccountFlowState
     */
    private Integer flowState;
    /**
     * 交易详情编号
     */
    private String detailNo;

    /**
     * //来源类型
     * //结算薪资代发
     * SP_SETTLEMENT_SALARY = 0;
     * //结算转账到商户号
     * SP_SETTLEMENT_MERCHANT = 1;
     * //结算提现
     * SP_SETTLEMENT_WITHDRAWAL = 2;
     * //推荐官钱包对公转账
     * SP_WALLET_TRANSFER = 3;
     * //有卡发薪
     * SP_XBB_SALARY = 4;
     * //薪班班用户提现
     * SP_XBB_WITHDRAWAL = 5;
     * //薪酬云-无卡发薪
     * SP_XCY_WITHOUT_CARD_PAY = 6;
     * //薪酬云-代发-资金调度
     * SP_XCY_DF_FUND_DISPATCHING = 7;
     * //薪酬云-xbb零钱提现
     * SP_XCY_XBB_WITHDRAWAL = 8;
     * //薪酬云-资金撤回
     * SP_XCY_FUND_BACK = 9;
     *
     * @see AssetXhServerProto.EnumSystemPayType
     */
    private Integer sourceSysType;
    /**
     * 操作人id
     */
    private Long operator;
    /**
     * 操作人
     */
    private String operatorName;
    /**
     * 交易备注
     */
    private String operationRemark;
}
