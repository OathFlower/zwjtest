package cn.xunhou.web.xbbcloud.product.sxz.enums;

/**
 * rmb购买包
 */
public enum RmbPackageEnum {

    PACKAGE_1(1, "套餐一", 1050, 1000),
    PACKAGE_2(2, "套餐二", 3200, 3000),
    PACKAGE_3(3, "套餐三", 5500, 5000);

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

    RmbPackageEnum(Integer code, String msg, Integer originCoin, Integer nowCoin) {
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

    public static RmbPackageEnum getEnum(Integer code) {
        if (code != null) {
            for (RmbPackageEnum value : RmbPackageEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
        }
        return null;
    }
}
