package cn.xunhou.xbbcloud.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangkm
 */
@Getter
@AllArgsConstructor
public enum EnumCapitalType implements IEnum<Integer> {
    /**
     * 10,"税费"
     */
    TAXES(10, "税费"),

    /**
     * 11,"退回税费"
     */
    BACK_TAXES(11, "退回税费"),


    /**
     * 20,"服务费"
     */
    SERVICE_CHARGE(20, "服务费"),
    /**
     * 21,"退回服务费"
     */
    BACK_SERVICE_CHARGE(21, "退回服务费"),

    /**
     * 30,"实发金额"
     */
    PAID_AMOUNT(30, "实发金额"),

    /**
     * 31，"退回实发金额"
     */
    BACK_PAID_AMOUNT(31, "退回实发金额"),
    /**
     * 99,"其他"
     */
    OTHER(99, "其他");
    private final Integer code;
    private final String message;

    /**
     * 获取枚举的code
     *
     * @return code
     */
    @Override
    public Integer getCode() {
        return code;
    }

    /**
     * 获取枚举的value
     *
     * @return value
     */
    @Override
    public String getMessage() {
        return message;
    }
}
