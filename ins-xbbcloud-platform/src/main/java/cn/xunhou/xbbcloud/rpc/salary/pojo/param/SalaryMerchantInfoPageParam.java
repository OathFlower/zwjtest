package cn.xunhou.xbbcloud.rpc.salary.pojo.param;

import cn.xunhou.xbbcloud.rpc.other.pojo.param.PageBaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collection;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryMerchantInfoPageParam extends PageBaseParam {

    /**
     * 租户类型
     */
    private Collection<Integer> tenantTypes;

    /**
     * 租户ids
     */
    private Collection<Long> ids;
}
