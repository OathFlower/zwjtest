package cn.xunhou.web.xbbcloud.product.salary.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryDetailResult implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 批次id
     */
    private String batchId;
    /**
     * 批次id
     */
    private String detailId;
    /**
     * 状态 0支付处理中（未认证） 1支付处理中（已下单） 2已发薪 3支付失败
     */
    private Integer status;
    /**
     * 状态意义
     */
    private String statusMsg;
    /**
     * 姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;

    /**
     * 项目名称
     */
    private String productName;

    /**
     * 身份证号 加解密
     */
    private String idCardNo;

    /**
     * 身份证号 脱敏的
     */
    private String maskIdCardNo;

    /**
     * 操作人姓名
     */
    private String operatorName;
    /**
     * 应发金额（元）
     */
    private String payableAmount;

    /**
     * 实发金额（元）
     */
    private String paidInAmount;

    /**
     * 个税（元）
     */
    private String taxAmount;

    /**
     * 其他扣除（元）
     */
    private String otherDeduct;

    /**
     * 服务费(分)
     */
    private String serviceAmount;

    /**
     * 备注
     */
    private String remark;
    /**
     * 失败原因
     */
    private String failureReason;
    /**
     * 操作人id
     */
    private Long operatorId;
    /**
     * 创建时间 yyyy-MM-dd HH:mm:ss
     */
    private String createdAt;
    /**
     * 更新时间 yyyy-MM-dd HH:mm:ss
     */
    private String updatedAt;
    /**
     * 租户名称
     */
    private String tenantName;
    /**
     * 发薪主体名称
     */
    private String subjectName;
    /**
     * 计薪年月 yyyyMM
     */
    private String month;
    /**
     * 业务流水号
     */
    private String assetDetailNo;
    /**
     * 扩展字段
     */
    private ExpandInfo expandInfo;

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
