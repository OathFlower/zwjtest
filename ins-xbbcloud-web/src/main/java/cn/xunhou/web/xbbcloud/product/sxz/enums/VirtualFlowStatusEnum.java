package cn.xunhou.web.xbbcloud.product.sxz.enums;

/**
 * 虚拟币流水状态
 */
public enum VirtualFlowStatusEnum {

    RECHARGE(0, "充值"),
    CONSUME(1, "消费");

    private Integer code;

    private String msg;


    VirtualFlowStatusEnum(Integer code, String msg) {
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

    public static VirtualFlowStatusEnum getEnum(Integer code) {
        if (code != null) {
            for (VirtualFlowStatusEnum value : VirtualFlowStatusEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return null;
    }
}
