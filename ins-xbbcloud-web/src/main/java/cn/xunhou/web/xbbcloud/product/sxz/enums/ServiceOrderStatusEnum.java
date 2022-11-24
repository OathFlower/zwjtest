package cn.xunhou.web.xbbcloud.product.sxz.enums;

/**
 * 服务权益状态
 */
public enum ServiceOrderStatusEnum {

    UN_USE(0, "未使用"),
    USED(1, "已核销");

    private Integer code;

    private String msg;


    ServiceOrderStatusEnum(Integer code, String msg) {
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

    public static ServiceOrderStatusEnum getEnum(Integer code) {
        if (code != null) {
            for (ServiceOrderStatusEnum value : ServiceOrderStatusEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return null;
    }
}
