package cn.xunhou.xbbcloud.rpc.salary.pojo.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;


@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryMerchantFlowResult implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * //待发薪
     * TO_BE_PAID = 0;
     * //进行中
     * IN_PROGRESS = 1;
     * //全部成功
     * ALL_SUCCEEDED = 2;
     * //部分成功
     * PARTIALLY_SUCCESSFUL = 3;
     * //全部失败
     * ALL_FAILED = 4;
     */
    Integer tradingStatus = 4;
    /**
     * 租户id
     */
    private Long tenantId;
    /**
     * 流水编号 支出类 批次的编号 收入类是 微信充值流水
     */
    private String flowNo;
    /**
     * 操作类型 1收入 2支出
     */
    private int operationType;
    /**
     * 操作人
     */
    private Long operatorId;
    /**
     * 操作金额（分）
     */
    private Integer operationAmount;
    /**
     * 备注
     */
    private String remarks;
    /**
     * 交易批次id
     */
    private Long salaryBatchId;
    private Long id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Integer deletedFlag;
}
