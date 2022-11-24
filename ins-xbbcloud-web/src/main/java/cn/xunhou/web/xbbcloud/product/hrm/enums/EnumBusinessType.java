package cn.xunhou.web.xbbcloud.product.hrm.enums;

import org.apache.commons.lang3.StringUtils;

public enum EnumBusinessType {

    FULL_TIME_OUTSOURCING(1, "全职外包"),
    @Deprecated
    FULL_TIME_HALF(2, "全职外包(半风险)"),
    PART_TIIME(3, "兼职外包"),
    TRAINEE(4, "实习外包"),
    TRANSFER_OUTSOURCING(5, "转移外包"),
    @Deprecated
    TRANSFER_OUTSOURCING_FULL_TIME_HALF(6, "转移外包(半风险)"),
    RPO_MONTH(7, "RPO月返"),
    PERSONNEL_AGENCY(8, "人事代理"),
    @Deprecated
    TAX_OPTIMIZATION_FULL(9, "税优(全职)"),
    LABOR_DISPATCH(10, "劳务派遣"),
    TRAINEE_WINTER_VACATION(11, "实习(寒假工)"),
    TRAINEE_SUMMER_VACATION(12, "实习(暑假工)"),

    ONCE_RPO(13, "一次性RPO"),
    PLATFORM_RPO(14, "平台RPO"),
    FLEXIBLE_EMPLOYMENT(15, "灵活用工"),
    COLLECTION_AND_PAYMENT(16, "代征代缴"),
    //    LABOR_SENDSAKLARY(17,"劳务发薪"),
    DINGDING_RPO(18, "钉钉RPO"),
    LABOR_OUTSOURCING(19, "劳务外包"),
    BPO_OR_OTHER(99, "BPO/其他");

    private final Integer code;
    private final String message;

    EnumBusinessType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static EnumBusinessType getEnum(Integer value) {
        if (value == null) {
            return null;
        }
        for (EnumBusinessType e : EnumBusinessType.values()) {
            if (e.getCode().equals(value)) {
                return e;
            }
        }
        return null;
    }

    public static EnumBusinessType getEnumMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return null;
        }
        for (EnumBusinessType e : EnumBusinessType.values()) {
            if (e.getMessage().equals(message)) {
                return e;
            }
        }
        return null;
    }
}
