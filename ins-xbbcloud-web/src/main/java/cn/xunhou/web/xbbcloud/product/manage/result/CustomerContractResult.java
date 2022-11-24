package cn.xunhou.web.xbbcloud.product.manage.result;

import cn.xunhou.web.xbbcloud.product.hrm.enums.EnumTenant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;


/**
 * @author wangkm
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
public class CustomerContractResult {
    /**
     * 主健
     */
    private Long id;

    /**
     * 合同名称
     */
    private String contractName;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 销售账号ID
     */
    private Long saleAccountId;

    /**
     * 客服人员Id
     */
    private Long serviceId;

    /**
     * 合同签署主体
     */
//    @Deprecated
//    private EnumCustomerContractSubject contractSubject;

    /**
     * 合同签署主体ID-主体配置
     */
    private Long contractSubjectId;

    /**
     * 合作时间
     */
    private String cooperateTime;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 计薪方式
     */
    private String salaryUnit;

    /**
     * 是否归属猎聘
     */
    private String belongLiepinFlag;

    /**
     * 商机来源Id
     */
    private Long businessSourceId;

    /**
     * 商机来源名称
     */
    private String businessSourceName;

    /**
     * 猎聘销售名称
     */
    private String liepinSaleName;

    /**
     * 猎聘TL名称
     */
    private String liepinTlName;

    /**
     * 合同是否已回标志
     */
    private String returnFlag;

    /**
     * 合同开始时间
     */
    private String contractStartTime;

    /**
     * 合同结束时间
     */
    private String contractEndTime;

    /**
     * 合同状态
     */
    private String status;

    /**
     * 曾经已审核标识
     */
    private String onceCheckedFlag;

    /**
     * 签署时间
     */
    private String signTime;

    /**
     * 营业执照猎聘法师ID
     */
    private String businessLicenseLpfsId;

    /**
     * 合同文件猎聘法师ID
     */
    private List<String> contractFileLpfsIds;

    /**
     * 提前执行证明
     */
    private String advanceExecProofLpfsId;

    /**
     * 租户ID
     */
    private EnumTenant tenant;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 测试数据标识
     */
    private String testFlag;

    /**
     * 创建时间
     */
    private String createtime;

    /**
     * SaaS服务标识
     */
    private Integer saasServiceFlag;

    /**
     * 服务费模式
     */
    private String serviceFeeType;

    /**
     * 企业承担税率
     */
    private BigDecimal corporateTaxRate;

    /**
     * 服务费基数
     */
    private BigDecimal serviceFeeBase;

    /**
     * 企业承担商保
     */
    private BigDecimal enterpriseUndertakesCommercialInsurance;

}
