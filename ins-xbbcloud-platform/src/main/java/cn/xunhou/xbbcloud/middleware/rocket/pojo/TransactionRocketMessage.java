package cn.xunhou.xbbcloud.middleware.rocket.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

/**
 * @author litb
 * @date 2022/8/9 16:38
 * <p>
 * 在rocket中传递的rocket消息
 * 注：-------------------------------------打印日志，请用toString------------------------
 */
@Getter
@Setter
//@ToString
@Accessors(chain = true)
@Slf4j
public class TransactionRocketMessage implements Serializable {
    private static final long serialVersionUID = 9188534174343021957L;

    /**
     * 各个调用方的交易批次编号
     */
    private String batchNo;
    /**
     * 数据来源
     * AssetXhServerProto.EnumSystemPayType
     */
    private Integer systemPayType;
    /**
     * 支付模块系统内部的交易批次编号
     */
    private String assetBatchNo;

    /**
     * 支付模块内部的交易id,用于串联多个交易
     */
    private Long assetTransactionId;

    /**
     * 批次交易结果
     * <p>
     * 0 未发起 1交易中 2交易成功 3部分成功 4交易失败
     */
    private Integer batchTransactionResult;

    /**
     * 如果整个批次都失败,有次字段,表示批次失败的原因
     */
//    @JsonProperty(value = "globalFailMessage")
    private String globalErrorMessage;

    /**
     * 交易批次下的交易详情列表
     */
    private List<TransactionDetailMessage> detailMessageList;

    @Override
    public String toString() {
        return "TransactionRocketMessage{" +
                "batchNo='" + batchNo + '\'' +
                ", assetBatchNo='" + assetBatchNo + '\'' +
                ", assetTransactionId=" + assetTransactionId +
                ", batchTransactionResult=" + batchTransactionResult +
                ", globalFailMessage='" + globalErrorMessage + '\'' +
                ", detailMessageList=" + detailMessageList +
                '}';
    }

    @Getter
    @Setter
//    @ToString
    @Accessors(chain = true)
    public static class TransactionDetailMessage {

        /**
         * 各个调用方的交易详情编号
         */
        private String detailNo;

        /**
         * 支付模块内部的交易详情编号
         */
        private String assetDetailNo;

        /**
         * 明细状态
         */
        private Integer status;
        /**
         * 是否失败
         */
        private Boolean detailFailed;

        /**
         * 转账详情失败对应的错误信息
         */
//        @JsonProperty(value = "failMessage")
        private String errorMessage;

        /**
         *
         */
        private Long assetTransactionId;

        @Override
        public String toString() {
            return "TransactionDetailMessage{" +
                    "detailNo='" + detailNo + '\'' +
                    ", assetDetailNo='" + assetDetailNo + '\'' +
                    ", detailFailed=" + detailFailed +
                    ", failMessage='" + errorMessage + '\'' +
                    ", assetTransactionId=" + assetTransactionId +
                    '}';
        }
    }
}
