package cn.xunhou.web.xbbcloud.product.sign.param;


import lombok.Data;


@Data
public class DynamicFormsParam {

    /**
     * List<ContractDynamicQueryForm> 的Json串
     */
    private String contractDynamicQueryFormListJson;

    /**
     * 签约主体
     */
    private Long subjectId;
}
