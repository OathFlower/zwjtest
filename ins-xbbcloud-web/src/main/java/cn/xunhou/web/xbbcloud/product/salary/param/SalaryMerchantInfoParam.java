package cn.xunhou.web.xbbcloud.product.salary.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryMerchantInfoParam {
    /**
     * 租户id
     */
    @NotNull(message = "租户id不能为空")
    private Long tenantId;


    /**
     * 到期时间 yyyy-MM-dd hh:mm:ss
     */
    @NotBlank(message = "到期时间不能为空")
    private String useToDate;

    /**
     * 租户类型
     * <p>
     * SAAS = 0;
     * //代发类型
     * BEHALF_ISSUED = 1;
     */
    @NotNull(message = "租户类型不能为空")
    private Integer tenantType;

    /**
     * 收款子账户id
     */
    private Long payeeSubAccountId;
    /**
     * 收款子账户编号
     */
    private String payeeSubAccountNo;

    /**
     * saas-商户号收款账号 ｜ 代发-收款主体账号
     */
    @NotBlank(message = "收款账号不能为空")
    private String payeeMerchantNo;

    /**
     * 收款主体id
     */
    private Long payeeSubjectId;
    /**
     * saas-商户号收款主体 ｜ 代发-收款主体名
     */
    @NotBlank(message = "收款主体不能为空")
    private String payeeMerchantName;

    /**
     * 商户合同文件id
     */
    private String contractFileId;

    /**
     * 服务费率
     */
    private Float serviceRate;

    /**
     * 审批能力 1启用 2不启用
     */
    @NotNull(message = "审批能力不能为空")
    private Integer isApproval;

    /**
     * 个税能力 1启用(公式计算) 2不启用
     */
    @NotNull(message = "个税能力不能为空")
    private Integer individualTax;

    /**
     * 发薪方式 1无卡发薪、2有卡发薪 多选
     */
    @NotEmpty(message = "发薪方式不能为空")
    private List<Integer> payrollMethods;

    /**
     * 认证类型 1二要素认证、2信息人脸认证
     */
    @NotNull(message = "认证类型不能为空")
    private Integer certificationType;

    /**
     * 服务商信息
     */
    private String serviceMerchantNo;

    /**
     * 特约商户id
     */
    private String specialMerchantId;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 发薪主体
     */
    private Long payerSubjectId;


    /**
     * 开户银行名
     */
    @NotBlank(message = "开户银行名不能为空")
    private String openBankName;
    /**
     * 开户行号
     */
    @NotBlank(message = "开户行号不能为空")
    private String openBankNo;
    /**
     * 开户地
     */
    @NotBlank(message = "开户地不能为空")
    private String openBankAddress;
    /**
     * 小程序提现 1启用 2不启用
     */
    @NotNull(message = "小程序提现不能为空")
    private Integer xcxWithdraw;

    /**
     * 合同id
     */
    private String contractId;

    /**
     * 项目id
     */
    private String projectId;
}
