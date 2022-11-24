package cn.xunhou.xbbcloud.rpc.salary.pojo.result;

import cn.xunhou.xbbcloud.middleware.rocket.pojo.SendWaitFundDispatchingMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ExeWithdrawOrWxResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean isSendWaitFundDispatching = false;
    private SalaryBatchResult salaryBatchResult;

    private SendWaitFundDispatchingMessage fundDispatchingMessage;

}
