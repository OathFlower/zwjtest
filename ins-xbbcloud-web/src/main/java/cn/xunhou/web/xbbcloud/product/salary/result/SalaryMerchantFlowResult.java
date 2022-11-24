package cn.xunhou.web.xbbcloud.product.salary.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryMerchantFlowResult {
    /**
     * 交易状态
     * operationType = 1 默认 成功
     * operationType = 2 请参考批次状态
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
     * 操作金额（元）
     */
    private String operationAmount;
    /**
     * 备注
     */
    private String remarks;
    /**
     * 交易批次id
     */
    private Long salaryBatchId;

    /**
     * 流水id
     */
    private Long id;

    /**
     * 创建时间
     */
    private String updatedAt;

    /**
     * 操作人名
     */
    private String operatorName;
}
