package cn.xunhou.web.xbbcloud.product.sign.result;


import cn.xunhou.web.xbbcloud.product.salary.enums.EnumTemplateStatus;
import lombok.Data;

import java.util.List;

@Data
public class ContractTemplateResult {

    private Long id;
    private List<Long> customerIds;
    /**
     * 合同名称
     */
    private String name;
    /**
     * 模板编号
     */
    private String templateNo;
    /**
     * 签署参数（逗号分隔）
     */
    private String params = "";

    /**
     * 签字字段参数（逗号分隔）
     */
    private String signParams = "";

    /**
     * 盖章字段参数（逗号分隔）
     */
    private String stampParams = "";

    /**
     * 参数字典值
     */
    private String paramsValue;

    /**
     * 状态
     */
    private EnumTemplateStatus status;


}
