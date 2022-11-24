package cn.xunhou.web.xbbcloud.product.salary.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryMerchantInfoResult {

    /**
     * 租户id
     */
    private Long id;

    /**
     * 是否启用 1启用 2不启用
     */
    private Integer isUse;

    /**
     * 到期时间
     */
    private String useToDate;

    /**
     * 租户类型 saas-0 代发-1
     */
    private Integer tenantType;

    /**
     * saas-商户号收款账号 ｜ 代发-收款子账号
     */
    private String payeeMerchantNo;

    /**
     * 代发-收款主体
     */
    private Long payeeSubjectId;

    /**
     * saas-商户号收款主体 ｜ 代发-收款主体id
     */
    private String payeeMerchantName;


    /**
     * 收款子账户id（仅代发类型使用，通过 /api/manages/subject/subAccount 关联显示基本信息）
     */
    private Long payeeSubAccountId;

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
    private Integer isApproval;

    /**
     * 个税能力 1启用 2不启用
     */
    private Integer individualTax;

    /**
     * 发薪方式 1无卡发薪、2有卡发薪
     */
    private List<Integer> payrollMethods;

    /**
     * 认证类型 1二要素认证、2信息人脸认证
     */
    private Integer certificationType;

    /**
     * 特约商户id（仅saas客户使用）
     */
    private String specialMerchantId;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 查询时间
     */
    private String queryTime;
    /**
     * 余额 元
     */
    private String balance;

    /**
     * 发薪主体（仅代发类型使用，通过 /api/manages/subject/subAccount 关联显示基本信息）
     */
    private Long payerSubjectId;


    /**
     * 开户银行名
     */
    private String openBankName;
    /**
     * 开户行号
     */
    private String openBankNo;
    /**
     * 开户地
     */
    private String openBankAddress;
    /**
     * 小程序提现 (1启用 2不启用)
     */
    private Integer xcxWithdraw;

    /**
     * 合同id
     */
    private String contractId;

    /**
     * 项目id
     */
    private String projectId;


    /**
     * 更新时间
     */
    private String updateTime;
    /**
     * 收款子账户编号
     */
    private String payeeSubAccountNo;
}
