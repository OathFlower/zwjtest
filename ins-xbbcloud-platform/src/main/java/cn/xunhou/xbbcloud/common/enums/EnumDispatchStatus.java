package cn.xunhou.xbbcloud.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangkm
 */
@Getter
@AllArgsConstructor
public enum EnumDispatchStatus implements IEnum<Integer> {
    /**
     * 未发起10
     */
    INIT(10, "未发起"),

    /**
     * 挂起
     */
    PENDING(11, "挂起"),
    /**
     * 交易中20
     */
    PROCESSING(20, "处理中"),
    /**
     * 成功30
     */
    SUCCESSFUL(30, "成功"),

    /**
     * 失败40
     */
    FAILURE(40, "失败");
    private final Integer code;
    private final String message;

    /**
     * 获取枚举的code
     *
     * @return code
     */
    @Override
    public Integer getCode() {
        return code;
    }

    /**
     * 获取枚举的value
     *
     * @return value
     */
    @Override
    public String getMessage() {
        return message;
    }
}
