package cn.xunhou.xbbcloud.common.enums;

/**
 * @Author: chenning
 * @Date: 2022/09/26/11:06
 * @Description:
 */
public enum EnumEventType implements IEnum<Integer> {

    FIRST_COMMIT(10,"初次提交"),
    CONFIRM(20, "修改后提交不审核"),
    COMMIT(30, "修改后提交审核"),
    AUDIT_PASS(40, "审核通过"),
    AUDIT_REJECT(50, "审核不通过"),
    REVOKE(60, "撤销");

    private Integer type;
    private String desc;

    EnumEventType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    @Override
    public Integer getCode() {
        return this.type;
    }

    @Override
    public String getMessage() {
        return this.desc;
    }
}
