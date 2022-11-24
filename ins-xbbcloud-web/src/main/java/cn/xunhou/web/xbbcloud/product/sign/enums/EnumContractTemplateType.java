package cn.xunhou.web.xbbcloud.product.sign.enums;

public enum EnumContractTemplateType {

    FULL_TIME(1, "全职合同"),
    PART_TIME(2, "兼职合同"),
    TRAINEE(3, "实习合同"),
    OUT_SOURCING_FULL_TIME(4, "转移外包全职合同"),
    OUT_SOURCING_PART_TIME(5, "转移外包兼职合同"),
    OUT_SOURCING_TRAINEE(6, "转移外包实习合同"),
    LABOR_OUTSOURCING_TIME(7, "劳务外包合同"),
    XBB_SAAS(8, "薪班班SAAS合同"),
    XBB_FLEXIBLE_EMPLOYMENT(9, "灵活用工合同"),
    ALL(10, "全部"),
    LABOR_DISPATCHING(11, "劳务派遣合同"),
    ;
    private Integer code;
    private String message;

    EnumContractTemplateType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

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

    public static EnumContractTemplateType getEnum(Integer value) {
        if (value != null) {
            for (EnumContractTemplateType e : EnumContractTemplateType.values()) {
                if (e.getCode().equals(value)) {
                    return e;
                }
            }
        }
        return null;
    }
}
