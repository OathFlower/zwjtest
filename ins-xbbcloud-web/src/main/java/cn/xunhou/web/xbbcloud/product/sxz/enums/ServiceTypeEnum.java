package cn.xunhou.web.xbbcloud.product.sxz.enums;

/**
 * 服务权益类型
 */
public enum ServiceTypeEnum {

    BUY(0, "购买的服务"),
    PRESENT(1, "赠送的服务");

    private Integer code;

    private String msg;


    ServiceTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static ServiceTypeEnum getEnum(Integer code) {
        if (code != null) {
            for (ServiceTypeEnum value : ServiceTypeEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return null;
    }
}
