package cn.xunhou.web.xbbcloud.product.sign.param;

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
public class QueryContractListParam extends PageInfo {
    /**
     * 岗位二维码id
     */
    private Long sourceBusinessId;
    /**
     * 状态list //待签署
     * WAIT_SIGN = 0;
     * //待确认
     * WAIT_CONFIRM = 1;
     * //待生效
     * WAIT_EFFECT = 2;
     * <p>
     * //生效中
     * EFFECTING = 3;
     * <p>
     * //已到期
     * OVERTIME = 4;
     * <p>
     * //已作废
     * CANCEL = 5;
     * <p>
     * //提前终止
     * EARLY_TERMINATION = 6;
     */

    private List<Integer> statusList;
}
