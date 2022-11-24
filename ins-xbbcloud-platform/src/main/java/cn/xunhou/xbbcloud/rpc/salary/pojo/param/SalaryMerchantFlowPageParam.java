package cn.xunhou.xbbcloud.rpc.salary.pojo.param;

import cn.xunhou.xbbcloud.rpc.other.pojo.param.PageBaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryMerchantFlowPageParam extends PageBaseParam {
    /**
     * 租户id
     */
    private Long tenantId;
    /**
     * 流水操作类型
     */
    private List<Integer> flowOperationTypes;
    /**
     * 操作金额(分)
     */
    private Integer operationAmount;
    /**
     * 交易状态
     */
    private List<Integer> tradingStatus;
    /**
     * 备注
     */
    private String remarks;
    /**
     * 操作人
     */
    private Long operatorId;

    /**
     * 开始时间
     */
    private Timestamp startTime;

    /**
     * 结束时间
     */
    private Timestamp endTime;
}
