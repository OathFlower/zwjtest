package cn.xunhou.xbbcloud.common.exception;

import cn.xunhou.xbbcloud.common.enums.IEnum;

/**
 * @author litb
 * @date 2022/9/7 14:03
 * <p>
 * xbbcloud 错误枚举
 */
public enum XbbCloudErrorCode implements IEnum<Integer> {

    SYSTEM_ERROR(10000, "系统异常"),
    NOT_FOUND_EMP(10001, "未找到员工信息"),
    EMP_ALREADY_QUIT(10002, "员工已离职"),
    NOT_SUPPORT(10003, "暂不支持该功能"),


    ;

    private final Integer code;

    private final String value;

    XbbCloudErrorCode(Integer code, String value) {
        this.code = code;
        this.value = value;
    }


    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.value;
    }
}
