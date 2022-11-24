package cn.xunhou.web.xbbcloud.product.salary.result;

import cn.xunhou.cloud.task.core.XbbTableField;
import cn.xunhou.cloud.task.core.XbbTemplateEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 运营平台批次导出
 */
@Getter
@Setter
@ToString
public class OperationSalaryBatchExportData extends XbbTemplateEntity {
    /**
     * 批次id
     */
    @XbbTableField(headName = "批次号")
    private String batchId;
    /**
     * 租户名称
     */
    @XbbTableField(headName = "租户名称")
    private String tenantName;


    /**
     * 应发金额（元）
     */
    @XbbTableField(headName = "发薪金额")
    private String payableAmount;
    /**
     * 服务费
     */
    @XbbTableField(headName = "服务费")
    private String serviceAmount;

    /**
     * 发薪主体
     */
    @XbbTableField(headName = "发薪主体")
    private String subjectName;


    /**
     * 发薪来源
     */
    @XbbTableField(headName = "发薪来源")
    private String payMethodMsg;
    /**
     * 发薪状态
     */
    @XbbTableField(headName = "发薪状态")
    private String statusMsg;

    /**
     * 扣费状态
     */
    @XbbTableField(headName = "扣费状态")
    private String deductionStatusMsg;


    /**
     * 创建时间 yyyy-MM-dd HH:mm:ss
     */
    @XbbTableField(headName = "创建时间")
    private String createdAt;

    /**
     * 操作人姓名
     */
    @XbbTableField(headName = "操作人")
    private String operatorName;


    /**
     * 备注  失败原因（数量） 扣费原因
     */
    @XbbTableField(headName = "备注")
    private String remark;

}
