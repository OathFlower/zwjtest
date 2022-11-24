package cn.xunhou.web.xbbcloud.product.salary.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 主体流水信息结果
 *
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SubjectFlowInfoResult {
    /**
     * 子账户id
     */
    private Long subAccountId;

    /**
     * 主体名称
     */
    private String subjectName;

    /**
     * 银行卡号
     */
    private String bankCardNo;
    /**
     * 客户id
     */
    private Long customerId;
    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 余额(元)
     */
    private String balance;

    /**
     * 结算中金额(元)
     */
    private String settlementAmount;
    /**
     * 租户信息
     */
    private List<TenantInfoResult> tenantInfoResults;

    /**
     * 更新时间 yyyy-MM-dd hh:mm:ss
     */
    private String updateTime;

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class TenantInfoResult {
        /**
         * 租户id
         */
        private Long tenantId;
        /**
         * 租户名
         */
        private String tenantName;
    }
}
