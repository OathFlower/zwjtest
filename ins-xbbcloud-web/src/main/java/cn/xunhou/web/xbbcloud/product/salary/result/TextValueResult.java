package cn.xunhou.web.xbbcloud.product.salary.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 响应结果
 *
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class TextValueResult {
    /**
     * id
     */
    private Long id;
    /**
     * 文案
     */
    private String text;
    /**
     * 值
     */
    private String value;
}
