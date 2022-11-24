package cn.xunhou.xbbcloud.common.enums;

import java.util.ArrayList;
import java.util.List;

public enum EnumCommercialInsuranceType {
    NOT_HAVE(0, "不缴纳", 18, true),
    GENERAL_EMPLOYER_LIABILITY_INSURANCE(1, "一般雇主责任保险", 16, false),
    EMPLOYER_LIABILITY_INSURANCE_4_SINGLE_INDUSTRIAL_INJURY(2, "单工伤（含雇主20W)-30.8元/月", 3, false),
    SUPPLEMENTARY_MEDICAL_TREATMENT(3, "补充医疗-50元/月", 14, true),
    GROUP_ACCIDENT_INSURANCE_50W(4, "团意50万-17元/月", 8, true),
    OTHER(5, "其它", 17, true),
    DIMENSION_EMPLOYER_INSURANCE(6, "维度雇主险", 15, true),
    GENERAL_EMPLOYER_LIABILITY_INSURANCE_30W(7, "雇主30万（16元-30元）", 1, true),
    GENERAL_EMPLOYER_LIABILITY_INSURANCE_20W(8, "雇主20万（9.8元）", 2, true),
    GROUP_ACCIDENT_INSURANCE_10W(9, "团意10万-6元/月", 4, false),
    GROUP_ACCIDENT_INSURANCE_20W(10, "团意20万-12元/月", 5, true),
    GROUP_ACCIDENT_INSURANCE_30W(11, "团意30万-14元/月", 6, true),
    GROUP_ACCIDENT_INSURANCE_40W(12, "团意40万-15元/月", 7, false),
    GROUP_ACCIDENT_INSURANCE_60W(13, "团意60万-25元/月", 9, false),
    GROUP_ACCIDENT_INSURANCE_70W(14, "团意70万-29元/月", 10, false),
    GROUP_ACCIDENT_INSURANCE_80W(15, "团意80万-38元/月", 11, false),
    GROUP_ACCIDENT_INSURANCE_90W(16, "团意90万-48元/月", 12, false),
    GROUP_ACCIDENT_INSURANCE_100W(17, "团意100万-55元/月", 13, false),
    SINGLE_INDUCTRIAL_INJURY(18, "单工伤（含雇主20W)-37.8元/月", 14, false),
    SINGLE_INDUCTRIAL_INJURY_65W(19, "单工伤雇主65万", 19, false),
    GENERAL_EMPLOYER_LIABILITY_INSURANCE_80W(20, "雇主80W-39元/人/月", 20, true),
    SINGLE_INDUCTRIAL_INJURY_NEW(21, "单工伤", 21, true),
    GENERAL_EMPLOYER_LIABILITY_INSURANCE_65W(22, "雇主65万-24元/月", 22, true),
    GROUP_ACCIDENT_9M9(23, "九毛九团体意外10-14元/月", 23, true),
    HIGH_EMPLOYER_80W(24, "高空雇主80W-195元/月", 24, true),
    GROUP_MEDICAL(25, "团体医疗-50元/月", 25, true),
    EMPLOYER_INSURANCE_80W(26, "雇主80万无免赔50元/月", 26, true),
    ;

    EnumCommercialInsuranceType(Integer code, String message, Integer sort, boolean usableFlag) {
        this.code = code;
        this.message = message;
        this.sort = sort;
        this.usableFlag = usableFlag;
    }

    private Integer code;
    private String message;
    private Integer sort;
    /**
     * 是否可用标识
     */
    private boolean usableFlag;

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

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public boolean getUsableFlag() {
        return usableFlag;
    }

    public void setUsableFlag(boolean usableFlag) {
        this.usableFlag = usableFlag;
    }

    public static EnumCommercialInsuranceType getEnumByCode(Integer code) {
        for (EnumCommercialInsuranceType insuranceType : EnumCommercialInsuranceType.values()) {
            if (code.equals(insuranceType.code)) {
                return insuranceType;
            }
        }
        return null;
    }

    public static EnumCommercialInsuranceType getEnumByMessage(String msg) {
        for (EnumCommercialInsuranceType insuranceType : EnumCommercialInsuranceType.values()) {
            if (msg.equals(insuranceType.message)) {
                return insuranceType;
            }
        }
        return null;
    }

    public static List<EnumCommercialInsuranceType> getUsableCommercialInsuranceTypes() {
        List<EnumCommercialInsuranceType> list = new ArrayList<>();
        for (EnumCommercialInsuranceType e : EnumCommercialInsuranceType.values()) {
            if (e.usableFlag) {
                list.add(e);
            }
        }
        return list;
    }


}
