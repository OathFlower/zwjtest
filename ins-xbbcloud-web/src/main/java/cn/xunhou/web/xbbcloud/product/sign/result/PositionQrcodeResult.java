package cn.xunhou.web.xbbcloud.product.sign.result;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PositionQrcodeResult {
    /**
     * 岗位二维码ID
     */
    private Long id;

    /**
     * 人力岗位ID
     */
    private Long hroPositionId;
    /**
     * 主体id
     */
    private Long subjectId;
    /**
     * 主体名称
     */
    private String subjectName;
    /**
     * 岗位名称
     */
    private String positionName;

    /**
     * 岗位地址
     */
    private String positionAddr;

    /**
     * 岗位薪资
     */
    private BigDecimal positionSalary;
    /**
     * 合同模板类型
     */
    private Integer contractTemplateType;
    /**
     * 试用期
     */
    private Integer probation;


    /**
     * 过期时间
     */
    private String expireDate;

    /**
     * 备注
     */
    private String remark;
    /**
     * 企业动态表单json
     */
    private String templateJson;
    /**
     * 0不缴纳社保 1缴纳社保
     */
    private Integer socialInsurance;
    /**
     * 创建时间
     */
    private String createdAt;
    /**
     * 更新日期
     */
    private String updatedAt;
    /**
     * 操作人
     */
    private String operatorName;

    /**
     * 合同和协议列表
     */
    private List<TemplateResult> templateResultList;
    /**
     * 可签署user手机号
     */
    private List<String> userTelList;

}
