package cn.xunhou.web.xbbcloud.product.salary.result;

import cn.xunhou.cloud.task.core.XbbTableField;
import cn.xunhou.cloud.task.core.XbbTemplateEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SalaryDetailExportData extends XbbTemplateEntity {
    /**
     * 批次id
     */
    @XbbTableField(headName = "批次号")
    private String batchId;

    /**
     * 状态 0支付处理中（未认证） 1支付处理中（已下单） 2已发薪 3支付失败
     */
    @XbbTableField(headName = "状态")
    private String statusMsg;

    /**
     * 姓名
     */
    @XbbTableField(headName = "姓名")
    private String name;


    /**
     * 项目名称
     */
    @XbbTableField(headName = "项目名称")
    private String productName;

    /**
     * 身份证号 加解密
     */
    @XbbTableField(headName = "身份证号")
    private String idCardNo;

    /**
     * 操作人姓名
     */
    @XbbTableField(headName = "操作人")
    private String operatorName;
    /**
     * 应发金额（元）
     */
    @XbbTableField(headName = "应发金额(个税加实发)")
    private String payableAmount;

    /**
     * 实发金额（元）
     */
    @XbbTableField(headName = "实发金额")
    private String paidInAmount;

    /**
     * 个税（元）
     */
    @XbbTableField(headName = "个税金额")
    private String taxAmount;


    /**
     * 失败原因
     */
    @XbbTableField(headName = "失败原因")
    private String failureReason;

    /**
     * 创建时间 yyyy-MM-dd HH:mm:ss
     */
    @XbbTableField(headName = "发薪时间")
    private String createdAt;

}
