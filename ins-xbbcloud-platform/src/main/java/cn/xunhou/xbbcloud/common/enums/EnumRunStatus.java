package cn.xunhou.xbbcloud.common.enums;

/**
 * @Author: chenning
 * @Date: 2022/09/27/20:23
 * @Description:
 */
public enum EnumRunStatus implements IEnum<Integer> {
    NOT_APPLY(0,"未发起"),
    RUNNING(1,"运行中"),
    HANG(2,"挂起"),
    OVER(3,"已结束");

    private final Integer code;

    private final String message;

    EnumRunStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
