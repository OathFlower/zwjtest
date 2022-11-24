package cn.xunhou.web.xbbcloud.product.salary.param;


import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryMerchantFlowPageParam extends PageInfo {
    /**
     * 租户id
     */
    private Long tenantId;
    /**
     * 流水操作类型
     */
    private List<Integer> flowOperationTypes;
    /**
     * 操作金额(元)
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
     * 开始时间 yyyy-MM-dd hh:MM:ss
     */
    private String startTime;

    /**
     * 结束时间 yyyy-MM-dd hh:MM:ss
     */
    private String endTime;
}
