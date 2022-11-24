package cn.xunhou.web.xbbcloud.product.sxz.enums;

/**
 * 服务权益包
 */
public enum ServicePackageEnum {

    PROMISE_INTERVIEW(1, "保到面", 375, 300),
    PROMISE_ENTRY_FIVE_DAYS(2, "保入职5天", 2250, 1800),
    OFFLINE_TWO_WAY(3, "线下双选会", 800, 600),
    ONLINE_RECRUITMENT(4, "线上招聘会", 200, 100),
    OFFLINE_SPECIAL(5, "线下专场", 12000, 6000),
    LIVE_SPECIAL(6, "直播专场", 2000, 1000);

    private Integer code;

    private String msg;
    /**
     * 原价
     */
    private Integer originCoin;
    /**
     * 现价
     */
    private Integer nowCoin;

    ServicePackageEnum(Integer code, String msg, Integer originCoin, Integer nowCoin) {
        this.code = code;
        this.msg = msg;
        this.originCoin = originCoin;
        this.nowCoin = nowCoin;
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

    public Integer getOriginCoin() {
        return originCoin;
    }

    public void setOriginCoin(Integer originCoin) {
        this.originCoin = originCoin;
    }

    public Integer getNowCoin() {
        return nowCoin;
    }

    public void setNowCoin(Integer nowCoin) {
        this.nowCoin = nowCoin;
    }

    public static ServicePackageEnum getEnum(Integer code) {
        if (code != null) {
            for (ServicePackageEnum value : ServicePackageEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return null;
    }
}
