package cn.xunhou.xbbcloud.rpc.salary.pojo.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class QuerySalaryMerchantInfo {

    /**
     * 租户类型
     */
    private List<Integer> tenantTypes;

    /**
     * 收款子账户id
     */
    private Collection<Long> payeeSubAccountIds;

    /**
     * 租户ids
     */
    private Collection<Long> ids;
}
