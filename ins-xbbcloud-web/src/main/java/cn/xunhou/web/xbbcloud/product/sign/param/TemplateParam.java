package cn.xunhou.web.xbbcloud.product.sign.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class TemplateParam {
    /**
     * 模板id
     */
    private Long templateId;
    /**
     * 类型 1合同，2协议
     */
    private Integer type;
}
