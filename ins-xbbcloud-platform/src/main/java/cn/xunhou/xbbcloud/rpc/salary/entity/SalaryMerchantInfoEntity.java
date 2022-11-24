package cn.xunhou.xbbcloud.rpc.salary.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeEntity;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;


@Getter
@Setter
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@XbbTable(table = "salary_merchant_info")
public class SalaryMerchantInfoEntity extends XbbSnowTimeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 到期时间
     */
    private Timestamp useToDate;

    /**
     * 租户类型
     *   SAAS = 0;
     *   //代发类型
     *   BEHALF_ISSUED = 1;
     *
     * @see SalaryServerProto.EnumTenantType
     */
    private Integer tenantType;

    /**
     * 商户号
     */
    private String payeeMerchantNo;

    /**
     * 商户号收款主体
     */
    private String payeeMerchantName;

    /**
     * 收款子账户id
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
    private String payrollMethod;

    /**
     * 认证类型 1二要素认证、2信息人脸认证
     */
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
     * 操作人
     */
    private Long operatorId;


    /**
     * 发薪主体
     */
    private Long payerSubjectId;

    /**
     * 收款主体
     */
    private Long payeeSubjectId;

    /**
     * 扩展信息
     */
    private String extInfo;

    /**
     * 请注意默认值
     */
    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class ExtInfo implements Serializable {
        private static final long serialVersionUID = 1L;

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
         * 小程序提现（1：是 2：否）
         */
        private Integer xcxWithdraw;

        /**
         * 商务合同id
         */
        private String businessContractId;

        /**
         * 项目id
         */
        private String projectId;

        /**
         * 收款子账户编号
         */
        private String payeeSubAccountNo;

        /**
         * 代发-收款主体客户id
         */
        private Long payeeCustomerId;
        /**
         * 代发-商务合同客户id
         */
        private Long businessContractCustomerId;
    }
}
