package cn.xunhou.web.xbbcloud.product.salary.enums;


/**
 * 该Enum做数据库与前端文案映射用
 */
public enum EnumSalaryBatchConvertMsg {
    PAYING_NOT_AUTH(0, "支付处理中"),//未认证 无openId 多openId
    PAYING_ALREADY_HANDLE(1, "已发薪"),// 已经调用发薪接口
    ALREADY_PAID(2, "部分失败"),
    PAY_FAIL(3, "全部失败");

    private final Integer code;

    private final String message;

    EnumSalaryBatchConvertMsg(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    public Integer getCode() {
        return this.code;
    }


    public String getMessage() {
        return this.message;
    }

    public static EnumSalaryBatchConvertMsg getEnum(Integer code) {
        if (code != null) {
            for (EnumSalaryBatchConvertMsg value : EnumSalaryBatchConvertMsg.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return null;
    }
}
