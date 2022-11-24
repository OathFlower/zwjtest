package cn.xunhou.web.xbbcloud.product.manage.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 商务合同基本信息
 *
 * @author wangkm
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
public class BusinessContractResult {
    /**
     * 商户合同id
     */
    private Long businessContractId;
    /**
     * 商务合同主题
     */
    private String title;
}
