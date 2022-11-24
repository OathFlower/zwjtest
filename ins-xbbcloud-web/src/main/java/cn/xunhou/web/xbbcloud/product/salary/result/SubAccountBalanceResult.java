package cn.xunhou.web.xbbcloud.product.salary.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SubAccountBalanceResult implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 子账户余额
     */
    private String balance;

    /**
     * 更新时间 yyyy-MM-dd hh:mm:ss
     */
    private String updateTime;

}
