package cn.xunhou.xbbcloud.rpc.salary.pojo.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.List;

/**
 * 查询资金调度
 *
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class QueryFundDispatchingParam {
    private Integer tenantId;
    /**
     * 交易主键-串起交易
     */
    private String transactionMain;

    /**
     * 交易主键-串起交易 s
     */
    private List<String> transactionMains;

    private Collection<Long> ids;

    /**
     * 交易类型-税费10；服务费20；实发金额30；其它40
     */
    private Collection<Integer> transactionTypes;
}
