package cn.xunhou.web.xbbcloud.product.sign.enums;

/**
 * 薪班班签署人类型
 */
public enum EnumXbbSigner {

    UNKNOWN(0, "未知"),
    FIRST_PARTY(1, "甲方"),
    SECOND_PARTY(2, "乙方"),
    Null;

    private Integer code;
    private String message;

    EnumXbbSigner(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    EnumXbbSigner() {
    }

    public static EnumXbbSigner valueOfCode(Integer code) {
        for (EnumXbbSigner obj : EnumXbbSigner.values()) {
            if (java.util.Objects.equals(obj.code, code)) {
                return obj;
            }
        }
        return Null;
    }

    public static EnumXbbSigner valueOfMessage(String message) {
        for (EnumXbbSigner obj : EnumXbbSigner.values()) {
            if (java.util.Objects.equals(obj.message, message)) {
                return obj;
            }
        }
        return Null;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
