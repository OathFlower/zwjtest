package cn.xunhou.web.xbbcloud.product.salary.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class IdNameMapperResult {
    /**
     * userXhCid
     */
    private Long id;
    /**
     * userXhCid 姓名
     */
    private String name;

    /**
     * 客户id 客户名称
     */
    private String customerName;
}
