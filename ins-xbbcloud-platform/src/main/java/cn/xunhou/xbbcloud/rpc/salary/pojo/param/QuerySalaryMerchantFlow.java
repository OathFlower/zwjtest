package cn.xunhou.xbbcloud.rpc.salary.pojo.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collection;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class QuerySalaryMerchantFlow {
    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 租户ids
     */
    private Collection<Long> tenantIds;
    /**
     * 流水编号
     */
    private Collection<String> flowNos;
    /**
     * 批次id查询
     */
    private Collection<Long> batchIds;

}
