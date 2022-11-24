package cn.xunhou.web.xbbcloud.product.hrm.param;

import cn.xunhou.web.xbbcloud.product.hrm.enums.EnumEmploymentType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class StaffImportParam {
    /**
     * 员工姓名
     */
    private String name;

    /**
     * 联系方式
     */
    private String telephone;

    /**
     * 证件类型
     */
    private Integer idCardType;

    /**
     * 证件号
     */
    private String idcardNo;

    /**
     * 银行卡号
     */
    private String bankcardNum;

    /**
     * 开户行
     */
    private String bankName;

    /**
     * 开户城市编码
     */
    private String bankCity;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 公司Id
     */
    private Long companyId;

    /**
     * 职位名称
     */
    private String jobName;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 岗位id
     */
    private Long postId;

    /**
     * 入职日期
     */
    private String onboardDate;

    /**
     * 在职状态
     */
    private Integer status;

    /**
     * 来源
     */
    private Integer source;

    /**
     * 员工类型
     */
    private EnumEmploymentType staffType;
}
