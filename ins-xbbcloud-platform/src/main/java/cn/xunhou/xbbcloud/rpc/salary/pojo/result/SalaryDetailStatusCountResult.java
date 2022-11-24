package cn.xunhou.xbbcloud.rpc.salary.pojo.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryDetailStatusCountResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer payingNotAuthCount;//未认证 无openId 多openId
    private Integer payingAlreadyHandleCount;// 已经调用发薪接口
    private Integer alreadyPaidCount;//已发薪
    private Integer payFailCount;//支付失败

    private Integer waitWithdrawCount;//待提现
    private Integer withdrawing;//提现中

    private Integer canceling;//撤回中
    private Integer cancelledCount; //已撤回

    private Integer cancelFailed;//撤回失败

    private Integer withdrawFailed; //提现失败
    private Integer withdrawSuccess; //提现成功
}
