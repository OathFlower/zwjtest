package cn.xunhou.web.xbbcloud.product.hrm.enums;

public enum EnumEmploymentType {


    // 全职外包
    FULL_TIME_OUTSOURCING(1, EnumBusinessType.FULL_TIME_OUTSOURCING.getCode()),
    //全职外包(半风险)
    @Deprecated
    FULL_TIME_HALF(2, EnumBusinessType.FULL_TIME_HALF.getCode()),
    //兼职外包
    PART_TIIME(3, EnumBusinessType.PART_TIIME.getCode()),
    //实习外包
    TRAINEE(4, EnumBusinessType.TRAINEE.getCode()),
    //转移外包
    TRANSFER_OUTSOURCING(5, EnumBusinessType.TRANSFER_OUTSOURCING.getCode()),
    //转移外包(半风险)
    @Deprecated
    TRANSFER_OUTSOURCING_FULL_TIME_HALF(6, EnumBusinessType.TRANSFER_OUTSOURCING_FULL_TIME_HALF.getCode()),
    //RPO月返
    @Deprecated
    RPO_MONTH(7, EnumBusinessType.RPO_MONTH.getCode()),
    //人事代理
    @Deprecated
    PERSONNEL_AGENCY(8, EnumBusinessType.PERSONNEL_AGENCY.getCode()),
    //税优(全职)
    @Deprecated
    TAX_OPTIMIZATION_FULL(9, EnumBusinessType.TAX_OPTIMIZATION_FULL.getCode()),
    //劳务派遣
    LABOR_DISPATCH(10, EnumBusinessType.LABOR_DISPATCH.getCode()),
    // //全职外包(猎聘外包)
    // LIEPIN_LABOR_CONTRACT(11, EnumBusinessType.FULL_TIME_OUTSOURCING, EnumTenant.LP_HRO),
    // //劳务派遣(猎聘外包)
    // LIEPIN_LABOR_CONTRACT_DISPATCH(12, EnumBusinessType.LABOR_DISPATCH, EnumTenant.LP_HRO),
    // //兼职外包(猎聘外包)
    // LIEPIN_LABOR_PART_TIIME(13, EnumBusinessType.PART_TIIME, EnumTenant.LP_HRO),
    // //实习外包(猎聘外包)
    // LIEPIN_LABOR_TRAINEE(14, EnumBusinessType.TRAINEE, EnumTenant.LP_HRO),
    //实习(寒假工)
    TRAINEE_WINTER_VACATION(15, EnumBusinessType.TRAINEE_WINTER_VACATION.getCode()),
    //实习(寒假工)
    TRAINEE_SUMMER_VACATION(16, EnumBusinessType.TRAINEE_SUMMER_VACATION.getCode()),
    //劳务外包
    LABOR_OUTSOURCING(17, EnumBusinessType.LABOR_OUTSOURCING.getCode()),
    ;

    private Integer code;
    private Integer businessType;

    EnumEmploymentType(Integer code, Integer businessType) {
        this.code = code;
        this.businessType = businessType;

    }

    public static EnumEmploymentType getEnum(Integer value) {
        if (value != null) {
            for (EnumEmploymentType e : EnumEmploymentType.values()) {
                if (e.getCode().equals(value)) {
                    return e;
                }
            }
        }
        return null;
    }


    public static EnumEmploymentType getEnumByBusinessType(Integer businessType) {
        if (businessType == null) {
            return null;
        }
        for (EnumEmploymentType e : EnumEmploymentType.values()) {
            if (e.getBusinessType() == businessType) {
                return e;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }
}
