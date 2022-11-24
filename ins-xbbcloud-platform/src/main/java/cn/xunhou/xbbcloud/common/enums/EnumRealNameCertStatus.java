package cn.xunhou.xbbcloud.common.enums;

public enum EnumRealNameCertStatus {

    /**
     * 0：未认证
     */
    UNCERTIFY(0, "未认证"),
    /**
     * 1：认证通过
     */
    CERTIFY_PASS(1, "认证通过"),
    /**
     * 10：认证审核中
     */
    CERTIFY_AUDITING(10, "认证审核中"),
    /**
     * 11：认证审核不通过
     */
    CERTIFY_FAIL(11, "审核不通过"),
    /**
     * -1：掉验
     */
    INVALID(-1, "掉验"),
    ;

    EnumRealNameCertStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    private Integer code;
    private String message;

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

    public static EnumRealNameCertStatus getEnum(Integer code) {
        if (code != null) {
            for (EnumRealNameCertStatus value : EnumRealNameCertStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return null;
    }
}
