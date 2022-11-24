package cn.xunhou.web.xbbcloud.product.salary.result;

import cn.xunhou.cloud.task.core.XbbTableField;
import cn.xunhou.cloud.task.core.XbbTemplateEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 运营平台明细导出
 */
@Getter
@Setter
@ToString
public class OperationSalaryDetailExportData extends XbbTemplateEntity {
    /**
     * 批次id
     */
    @XbbTableField(headName = "批次号")
    private String batchId;


    /**
     * 姓名
     */
    @XbbTableField(headName = "姓名")
    private String name;
    /**
     * 身份证号
     */
    @XbbTableField(headName = "身份证号")
    private String idCardNo;

    /**
     * 手机号
     */
    @XbbTableField(headName = "手机号")
    private String phone;
    /**
     * 租户名称
     */
    @XbbTableField(headName = "客户")
    private String tenantName;

    /**
     * 项目名称
     */
    @XbbTableField(headName = "项目")
    private String productName;

    /**
     * 发薪主体名称
     */
    @XbbTableField(headName = "发薪账户")
    private String subjectName;

    /**
     * 应发金额（元）
     */
    @XbbTableField(headName = "应发金额")
    private String payableAmount;


    /**
     * 服务费
     */
    @XbbTableField(headName = "服务费")
    private String serviceAmount;
    /**
     * 实发金额（元）
     */
    @XbbTableField(headName = "实发金额")
    private String paidInAmount;

    /**
     * 创建时间 yyyy-MM-dd HH:mm:ss
     */
    @XbbTableField(headName = "发薪时间")
    private String createdAt;
    /**
     * 计薪年月 yyyyMM
     */
    @XbbTableField(headName = "考勤月")
    private String month;
    /**
     * 业务流水号
     */
    @XbbTableField(headName = "业务流水号")
    private String assetDetailNo;

    /**
     * 更新时间 yyyy-MM-dd HH:mm:ss
     */
    @XbbTableField(headName = "更新时间")
    private String updatedAt;
    /**
     * 状态
     */
    @XbbTableField(headName = "提现状态")
    private String statusMsg;


    /**
     * 失败原因
     */
    @XbbTableField(headName = "备注")
    private String failureReason;


}
