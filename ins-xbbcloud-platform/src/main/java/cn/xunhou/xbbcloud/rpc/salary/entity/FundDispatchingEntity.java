package cn.xunhou.xbbcloud.rpc.salary.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeEntity;
import cn.xunhou.grpc.proto.asset.AssetXhServerProto;
import cn.xunhou.xbbcloud.common.enums.EnumCapitalType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@XbbTable(table = "fund_dispatching")
public class FundDispatchingEntity extends XbbSnowTimeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long tenantId;
    /**
     * 交易主键-串起交易
     */
    private String transactionMain;

    /**
     * 交易类型-税费10；服务费20；实发金额30；退回税费11；退回服务费21；退回实发金额31； 其它99;
     *
     * @see EnumCapitalType
     */
    private Integer capitalType;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 状态-未发起10；挂起11；交易中20；成功30；失败40
     *
     * @see cn.xunhou.xbbcloud.common.enums.EnumDispatchStatus
     */
    private Integer dispatchStatus;

    /**
     * 上级交易节点id
     */
    private Long parentId;

    /**
     * 操作人
     */
    private Long operatorId;

    /**
     * 交易备注-双方可见
     */
    private String remark1;

    /**
     * 交易备注2 支付方可见
     */
    private String remark2;

    /**
     * 交易金额
     */
    private Integer amount;

    /**
     * 付方
     */
    private Long payerId;

    /**
     * 付方信息
     */
    private String payerConfig;

    /**
     * 收方 主账号-1
     */
    private Long payeeId;

    /**
     * 收方信息
     */
    private String payeeConfig;

    /**
     * 数据来源-薪酬云(代发客户)
     */
    private Integer sourceType;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 调度方向 B2B
     *
     * @see AssetXhServerProto.EnumBusinessType
     */
    private Integer dispatchDirection;

    /**
     * 资金交易id
     */
    private Long assetTransactionId;

    /**
     * 顺序
     */
    private Integer orderValue;

    /**
     * 扩展信息
     */
    private String extInfo;

    /**
     * 请注意默认值
     */
    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class ExtInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 实际三方交易编号
         */
        private String assetDetailNo;
    }
}