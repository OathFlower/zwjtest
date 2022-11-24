package cn.xunhou.web.xbbcloud.product.hrm.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CustomerResult {
    /**
     * 客户id
     */
    private Long id;
    /**
     * 客户名称
     */
    private String customerName;
}
