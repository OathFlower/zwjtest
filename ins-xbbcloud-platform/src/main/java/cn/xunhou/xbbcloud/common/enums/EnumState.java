package cn.xunhou.xbbcloud.common.enums;

/**
 * @Author: chenning
 * @Date: 2022/09/26/10:54
 * @Description:
 */
public enum EnumState implements IEnum<Integer> {

    DEFAULT(-1,"未知"),
    //0未发起
    NOT_APPLY(0,"未发起"),
    //1审批中
    TODO(10,"待审批"),
    //2审批通过
    PASS(20,"通过"),
    //3审批驳回
    REJECT(30,"驳回"),
    //4撤销
    REVOKE(40,"撤销");


    private Integer state;
    private String desc;

    EnumState(Integer state, String desc) {
        this.state = state;
        this.desc = desc;
    }
    @Override
    public Integer getCode() {
        return this.state;
    }

    @Override
    public String getMessage() {
        return this.desc;
    }

    public static EnumState getEnum(Integer value) {
        for (EnumState em : EnumState.values()) {
            if (em.getCode().equals(value)) {
                return em;
            }
        }
        return DEFAULT;
    }
}
