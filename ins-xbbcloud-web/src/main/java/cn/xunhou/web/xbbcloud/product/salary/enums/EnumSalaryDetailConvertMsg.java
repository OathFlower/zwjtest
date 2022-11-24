package cn.xunhou.web.xbbcloud.product.salary.enums;


/**
 * 该Enum做数据库与前端文案映射用
 */
public enum EnumSalaryDetailConvertMsg {
    PAYING_NOT_AUTH(0, "支付处理中"),//未认证 无openId 多openId
    PAYING_ALREADY_HANDLE(1, "支付处理中"),// 已经调用发薪接口
    ALREADY_PAID(2, "发薪成功"),
    PAY_FAIL(3, "发薪失败"),
    WAIT_WITHDRAW(4, "发薪成功"),
    WITHDRAWING(5, "发薪成功"),
    CANCELING(6, "发薪成功"),
    CANCELLED(7, "发薪失败"),
    CANCEL_FAILED(8, "发薪成功"),
    WITHDRAW_FAILED(9, "发薪成功"),
    WITHDRAW_SUCCESS(10, "发薪成功");

    private final Integer code;

    private final String message;

    EnumSalaryDetailConvertMsg(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    public Integer getCode() {
        return this.code;
    }


    public String getMessage() {
        return this.message;
    }

    public static EnumSalaryDetailConvertMsg getEnum(Integer code) {
        if (code != null) {
            for (EnumSalaryDetailConvertMsg value : EnumSalaryDetailConvertMsg.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return null;
    }
}
