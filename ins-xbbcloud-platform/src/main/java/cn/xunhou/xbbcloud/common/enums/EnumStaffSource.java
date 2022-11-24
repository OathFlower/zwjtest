package cn.xunhou.xbbcloud.common.enums;

public enum EnumStaffSource {

    UNKNOWN(0, "未知"),
    STARPRO(1, "勋厚交付"),
    HROSTAFF(2, "勋厚人力"),
    IMPORT(3, "导入"),
    XBB(4, "薪班班");
    private Integer code = null;
    private String message = null;

    EnumStaffSource(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public String message() {
        return this.getMessage();
    }

    public String code() {
        return this.getCode().toString();
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static EnumStaffSource getEnum(Integer value) {
        for (EnumStaffSource em : EnumStaffSource.values()) {
            if (em.getCode().equals(value)) {
                return em;
            }
        }
        return null;
    }

}
