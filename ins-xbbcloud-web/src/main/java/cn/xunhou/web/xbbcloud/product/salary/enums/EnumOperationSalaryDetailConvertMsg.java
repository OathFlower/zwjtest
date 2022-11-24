package cn.xunhou.web.xbbcloud.product.salary.enums;

/**
 * 该Enum做数据库与前端文案映射用
 */
public enum EnumOperationSalaryDetailConvertMsg {
    PAYING_NOT_AUTH(0, "支付处理中"),//未认证 无openId 多openId
    PAYING_ALREADY_HANDLE(1, "支付处理中"),// 已经调用发薪接口
    ALREADY_PAID(2, "发薪成功"),
    PAY_FAIL(3, "发薪失败"),
    WAIT_WITHDRAW(4, "待提现"),
    WITHDRAWING(5, "提现中"),
    CANCELING(6, "撤回中"),
    CANCELLED(7, "撤回成功"),
    CANCEL_FAILED(8, "撤回失败"),
    WITHDRAW_FAILED(9, "提现失败"),
    WITHDRAW_SUCCESS(10, "提现成功");

    private final Integer code;

    private final String message;

    EnumOperationSalaryDetailConvertMsg(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    public Integer getCode() {
        return this.code;
    }


    public String getMessage() {
        return this.message;
    }

    public static EnumOperationSalaryDetailConvertMsg getEnum(Integer code) {
        if (code != null) {
            for (EnumOperationSalaryDetailConvertMsg value : EnumOperationSalaryDetailConvertMsg.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return null;
    }
}
