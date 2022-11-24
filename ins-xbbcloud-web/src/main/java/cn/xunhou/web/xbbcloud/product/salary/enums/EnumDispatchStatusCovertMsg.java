package cn.xunhou.web.xbbcloud.product.salary.enums;


public enum EnumDispatchStatusCovertMsg {
    SUCCESSFUL(1, "成功"),
    FAILURE(2, "失败"),
    PROCESSING(0, "处理中");
    private final Integer code;
    private final String message;

    EnumDispatchStatusCovertMsg(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    public Integer getCode() {
        return this.code;
    }


    public String getMessage() {
        return this.message;
    }

    public static EnumDispatchStatusCovertMsg getEnum(Integer code) {
        if (code != null) {
            for (EnumDispatchStatusCovertMsg value : EnumDispatchStatusCovertMsg.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return null;
    }
}
