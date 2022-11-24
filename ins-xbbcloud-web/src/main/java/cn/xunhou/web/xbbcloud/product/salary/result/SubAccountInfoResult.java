package cn.xunhou.web.xbbcloud.product.salary.result;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SubAccountInfoResult {

    /**
     * 子账户id
     */
    private Long subAccountId;

    /**
     * 客户id
     */
    private Long customerId;

    /**
     * 主账户主键id
     */
    private Long subjectInfoId;

    /**
     * 账户编号：银行卡号6位对齐，微信自建维护
     */
    private String subAccountNo;

    /**
     * 余额
     */
    private String balance;

    /**
     * 冻结金额(正在交易中的支出金额)
     */
    private String frozenAmount;

    /**
     * 总收入
     */
    private String totalRevenue;

    /**
     * 总支出
     */
    private String totalExpenditure;

    /**
     * 类型：0银行卡 1微信商户号
     */
    private Integer type;

    /**
     * 是否允许透支：0不允许 1允许
     */
    private Integer useOverdraft;

    /**
     * 子账号对应的系统类型
     */
    private Integer customerType;

    /**
     * 备注
     */
    private String remark;
}