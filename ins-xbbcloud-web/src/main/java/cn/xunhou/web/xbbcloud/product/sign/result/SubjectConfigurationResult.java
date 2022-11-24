package cn.xunhou.web.xbbcloud.product.sign.result;


import lombok.Data;

/**
 * 主体
 */
@Data
public class SubjectConfigurationResult {
    /**
     * 是否缴纳社保
     */
    private Integer socialInsuranceType;
    /**
     * 主体id
     */
    private Integer subjectId;
    /**
     * 主体名称
     */
    private String subjectName;
}
