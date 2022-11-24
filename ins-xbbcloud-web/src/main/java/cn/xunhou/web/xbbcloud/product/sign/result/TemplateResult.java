package cn.xunhou.web.xbbcloud.product.sign.result;


import lombok.Data;

@Data
public class TemplateResult {
    /**
     * 合同模板(合同/协议)ID
     */
    private Long contractTemplateId;
    /**
     * 类型 1合同 2协议
     */
    private Integer type;
}
