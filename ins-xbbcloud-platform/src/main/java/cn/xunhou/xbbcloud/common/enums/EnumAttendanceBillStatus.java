package cn.xunhou.xbbcloud.common.enums;

public enum EnumAttendanceBillStatus implements IEnum<Integer> {
    BILL_WAIT_SEND(0, "待发送"),

    BILL_WAIT_REVIEW(1, "发薪待审核"),

    BILL_REVIEWED(2, "发薪已审核"),

    ;

    private final Integer code;

    private final String message;

    EnumAttendanceBillStatus(Integer code, String message) {
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
