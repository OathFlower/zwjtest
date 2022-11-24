package cn.xunhou.web.xbbcloud.product.sign.enums;

/**
 * 薪班班签署类型
 */
public enum EnumXbbSignType {

    CONTRACT(1, "合同"),
    PROTOCOL(2, "协议"),
    ;

    private Integer code;
    private String message;

    EnumXbbSignType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    EnumXbbSignType() {
    }

    public static EnumXbbSignType valueOfCode(Integer code) {
        for (EnumXbbSignType obj : EnumXbbSignType.values()) {
            if (java.util.Objects.equals(obj.code, code)) {
                return obj;
            }
        }
        return null;
    }

    public static EnumXbbSignType valueOfMessage(String message) {
        for (EnumXbbSignType obj : EnumXbbSignType.values()) {
            if (java.util.Objects.equals(obj.message, message)) {
                return obj;
            }
        }
        return null;
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
