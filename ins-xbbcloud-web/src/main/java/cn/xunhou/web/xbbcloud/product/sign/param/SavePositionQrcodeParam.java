package cn.xunhou.web.xbbcloud.product.sign.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SavePositionQrcodeParam {
    /**
     * 岗位二维码ID
     */
    private Long id;

    /**
     * 人力岗位ID
     */
    private Long hroPositionId;

    /**
     * 0不缴纳社保 1缴纳社保
     */
    private Integer socialInsurance;
    /**
     * 合同模板类型
     */
    private Integer contractTemplateType;
    /**
     * 过期时间
     */
    private String expireDate;

    /**
     * 岗位备注
     */
    private String remark;
    /**
     * 企业动态表单json
     */
    private String templateJson;
    /**
     * 企业合同主体id
     */
    private Long subjectId;
    /**
     * 合同和协议id列表
     */
    private List<TemplateParam> contractTemplateList;
    /**
     * 手机号列表
     */
    private List<String> telList;
    /**
     * 假删标志
     */
    private Integer deleteflag = 0;

}
