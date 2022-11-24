package cn.xunhou.web.xbbcloud.product.sign.param;


import cn.xunhou.web.xbbcloud.product.sign.enums.EnumXbbSignType;
import cn.xunhou.web.xbbcloud.product.sign.enums.EnumXbbSigner;
import lombok.Data;


@Data
public class GetContractDynamicParam {

    /**
     * 模板（合同）id
     */
    private Long templateId;
    /**
     * 类型
     */
    private EnumXbbSignType signType;

    /**
     * 签署人类型
     */
    private EnumXbbSigner xbbSigner;

    /**
     * 用户id
     */
    private Long userXhCId;

    /**
     * 薪班班客户id
     */
    private Long customerXbbId;

    /**
     * 合同签约主体
     */
    private Long contractSubjectId;


    /**
     * 是否为saas客户
     */
    private Boolean isSaasFlag = true;

}
