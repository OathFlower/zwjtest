package cn.xunhou.web.xbbcloud.product.salary.enums;

public enum EnumTemplateStatus {

    USABLE(1, "可用"),
    DISABLED(99, "作废"),
    ;
    private Integer code;
    private String message;

    EnumTemplateStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static EnumTemplateStatus getEnum(Integer value) {
        if (value != null) {
            for (EnumTemplateStatus e : EnumTemplateStatus.values()) {
                if (e.getCode().equals(value)) {
                    return e;
                }
            }
        }
        return null;
    }
}
