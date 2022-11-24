package cn.xunhou.web.xbbcloud.product.salary.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SubjectInfoResult {
    /**
     * 主体id
     */
    private Long subjectId;
    /**
     * 主体code
     */
    private String subjectCode;
    /**
     * 主体名称
     */
    private String subjectName;
    /**
     * 主体标识 1 勋厚体系 2 非勋厚体系 3 SAAS服务 4 猎聘外包
     */
    private Integer subjectType;
    /**
     * 主体适用场景1 商务合同 2 劳动合同 3 普通收款 4 薪班班收款 5 普通发薪 6 薪班班发薪
     */
    private String scenes;
    /**
     * 注册地址
     */
    private String subjectAddress;
    /**
     * 统一社会信用代码
     */
    private String identifyNum;
    /**
     * 法人
     */
    private String legalPersonName;
    /**
     * 法人身份证
     */
    private String idcardNo;
    /**
     * 法人电话号
     */
    private String telephone;
    /**
     * 公司简称
     */
    private String companyShortName;
    /**
     * 银行账号
     */
    private String bankCardNum;
    /**
     * 开户行名称
     */
    private String bankName;
    /**
     * 开户行行号
     */
    private String bankCode;
    /**
     * 银行卡类型： 0其他 1招行
     */
    private Integer bankType;
    /**
     * 服务商账号
     */
    private String serviceProviderAccount;
    /**
     * 商户号账号
     */
    private String merchantAccount;
    /**
     * 充值账户类型 1 普通 2 运营
     */
    private Integer topUpAccountType;
    /**
     * 商户号出款银行账号
     */
    private String wxPayBankCardNum;
    /**
     * 商户号收款银行账号
     */
    private String wxCollectionBankCardNum;
    /**
     * 超网代发 0不支持 1支持
     */
    private boolean isReplacePay;
    /**
     * 支付转账 0不支持 1支持
     */
    private boolean isTransferPay;
    /**
     * 使用子账户功能：0使用 1不使用
     */
    private boolean useSubAccount;
    /**
     * 劳动合同-上上签
     */
    private String bestSignEnterpriseAccount;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建人id
     */
    private Integer creatorId;
    /**
     * 修改人id
     */
    private Integer modifyId;
}
