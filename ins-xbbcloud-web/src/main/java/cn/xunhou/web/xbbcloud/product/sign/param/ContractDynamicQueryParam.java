package cn.xunhou.web.xbbcloud.product.sign.param;


import lombok.Data;


@Data
public class ContractDynamicQueryParam {

    /**
     * 模板id
     */
    private Long templateId;

    /**
     * 类型
     */
    private Integer xbbSignType;
}
