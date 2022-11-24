package cn.xunhou.web.xbbcloud.product.hrm.enums;

/**
 * 租户
 */
public enum EnumTenant {

    /**
     * 1：勋厚
     */
    XUNHOU(1L, "勋厚"),
    /**
     * 3：猎聘外包
     */
    LP_HRO(3L, "猎聘外包");

    private Long id;
    private String name;


    EnumTenant(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static EnumTenant getEnum(Long id) {
        if (id != null) {
            for (EnumTenant value : EnumTenant.values()) {
                if (value.getId().equals(id)) {
                    return value;
                }
            }
        }
        return null;
    }
}