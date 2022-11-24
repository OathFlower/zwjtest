package cn.xunhou.xbbcloud.rpc.salary.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "salary_detail")
public class SalaryDetailEntity extends XbbSnowTimeTenantEntity {
    /**
     * 批次id
     */
    private Long batchId;

    /**
     * 状态 0支付处理中（未认证） 1支付处理中（已下单） 2已发薪 3支付失败 4支付处理中（待提现）
     */
    private Integer status;

    /**
     * 发薪主体id
     */
    private Long subjectId;


    /**
     * 姓名
     */
    private String name;

    /**
     * 身份证号 加解密
     */
    private String idCardNo;

    /**
     * openid
     */
    private String openId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 应发金额（分）
     */
    private Integer payableAmount;
    /**
     * 服务费（分）
     */
    private Integer serviceAmount;

    /**
     * 实发金额（分）
     */
    private Integer paidInAmount;

    /**
     * 个税（分）
     */
    private Integer taxAmount;

    /**
     * 其他扣除（分）
     */
    private Integer otherDeduct;

    /**
     * 备注
     */
    private String remark;
    /**
     * 重试次数
     */
    private Integer retryCount;
    /**
     * 失败原因
     */
    private String failureReason;
    /**
     * 操作人id
     */
    private Long operatorId;
    /**
     * 扩展数据json
     */
    private String expandJson;


    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class ExpandInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 提现编号
         */
        private String withdrawalNo;
    }

}
