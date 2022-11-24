package cn.xunhou.xbbcloud.common.enums;

public enum EnumSalaryBatchStatus implements IEnum<Integer> {
    PAYING(0, "支付处理中"),
    ALL_SUCCESS(1, "已发薪"),
    PART_FAIL(2, "部分失败"),
    ALL_FAIL(3, "全部失败");

    private final Integer code;

    private final String message;

    EnumSalaryBatchStatus(Integer code, String message) {
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
