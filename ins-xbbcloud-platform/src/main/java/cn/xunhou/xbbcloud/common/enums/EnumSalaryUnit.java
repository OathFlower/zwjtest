package cn.xunhou.xbbcloud.common.enums;

public enum EnumSalaryUnit implements IEnum<Integer> {
    BY_PIECE(1, "件"),

    HOUR(2, "时"),

    DAY(3, "日"),

    WEEK(4, "周"),

    MONTH(5, "月"),

    QUARTER(6, "季度");

    private final Integer code;

    private final String message;

    EnumSalaryUnit(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }


}
