package cn.xunhou.web.xbbcloud.config.xhrpc.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangkm
 */

@Getter
@AllArgsConstructor
public enum EnumXhTenant {
    /**
     * 1：勋厚
     */
    XUNHOU(1L, "勋厚"),
    /**
     * 3：猎聘外包
     */
    LP_HRO(3L, "猎聘外包"),
    /**
     * -1：其它 暂不支持 userxh
     */
    OTHER(-1L, "其它");

    private final Long id;
    private final String name;
}
