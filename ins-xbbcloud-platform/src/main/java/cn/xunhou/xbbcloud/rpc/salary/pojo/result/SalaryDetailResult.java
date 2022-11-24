package cn.xunhou.xbbcloud.rpc.salary.pojo.result;

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
public class SalaryDetailResult implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 批次id
     */
    private Long batchId;
    /**
     * 明细id
     */
    private Long detailId;

    /**
     * 状态 0支付处理中（未认证） 1支付处理中（已下单） 1已发薪 2支付失败
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
     * 项目名称
     */
    private String productName;

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
     * 实发金额（分）
     */
    private Integer paidInAmount;

    /**
     * 服务费(分)
     */
    private Integer serviceAmount;
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
     * 操作人id
     */
    private Long operatorId;
    /**
     * 失败原因
     */
    private String failureReason;
    /**
     * 租户id
     */
    private Long tenantId;
    private Long id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Integer deletedFlag;

    /**
     * 计薪年月
     */
    private String salaryMonth;

    /**
     * 业务流水号
     */
    private String assetDetailNo;

    /**
     * 扩展数据json
     */
    private String expandJson;
}
