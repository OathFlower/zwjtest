package cn.xunhou.xbbcloud.rpc.salary.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Getter
@Setter
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@XbbTable(table = "salary_merchant_flow")
public class SalaryMerchantFlowEntity extends XbbSnowTimeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 流水编号 支出类 批次的编号 收入类是 微信充值流水(支出传assetId)
     */
    private String flowNo;

    /**
     * 操作类型 1收入 2支出
     */
    private Integer operationType;

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

    /**
     * 主体名称
     */
    private String subjectName;

    /**
     * 代发客户(子账户id) - saas(特约商户id)
     */
    private String payeeInfoId;
}
