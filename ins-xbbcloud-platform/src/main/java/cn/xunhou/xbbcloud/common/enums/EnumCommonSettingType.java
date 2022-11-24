package cn.xunhou.xbbcloud.common.enums;

public enum EnumCommonSettingType implements IEnum<Integer> {
    DEFAULT(0, "默认"),
    SCHEDULE_WORN(1,"排班预警设置"),
    CLOCK_IN(2,"打卡设置")
    ;

    private final Integer code;

    private final String message;

    EnumCommonSettingType(Integer code, String message) {
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
