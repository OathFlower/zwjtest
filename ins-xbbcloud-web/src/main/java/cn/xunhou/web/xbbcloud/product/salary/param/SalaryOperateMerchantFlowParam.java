package cn.xunhou.web.xbbcloud.product.salary.param;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryOperateMerchantFlowParam {

    @NotNull(message = "租户id不能为空")
    private Long tenantId;

    /**
     * 流水操作类型
     * 收入 INCOME = 1 ;
     * 支出 EXPENDITURE = 2;
     */
    @NotNull(message = "流水操作类型不能为空")
    private Integer flowOperationType;
    /**
     * 操作金额(元)
     */
    @NotBlank(message = "操作金额不能为空")
    private String operationAmount;
    /**
     *
     */
    @NotBlank(message = "备注不能为空")
    private String remarks;
    /**
     * 流水编号
     */
    @NotBlank(message = "流水编号不能为空")
    private String flowNo;
    /**
     * 交易批次
     */
    private Long salaryBatchId;
}
