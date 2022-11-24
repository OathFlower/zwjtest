package cn.xunhou.web.xbbcloud.product.hrm.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ContractResult {
    /**
     * 合同名称
     */
    private String name;
    /**
     * 合同模板id
     */
    private Long templateId;
    /**
     * 类型 0合同，1协议
     */
    private Integer contractType;
    /**
     * 合同oss id
     */
    private String contractOssId;

    /**
     * id
     */
    private Long id;
}
