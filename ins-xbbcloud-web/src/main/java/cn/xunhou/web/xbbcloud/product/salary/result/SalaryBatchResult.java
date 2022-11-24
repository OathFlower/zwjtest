package cn.xunhou.web.xbbcloud.product.salary.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;


@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryBatchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 项目名称
     */
    private String productName;

    /**
     * 计薪年月 yyyymm
     */
    private String month;

    /**
     * 状态  0进行中 1全部成功 2部分失败 3全部失败
     */
    private Integer status;

    /**
     * 状态意义
     */
    private String statusMsg;
    /**
     * 发薪人数
     */
    private Integer peopleCount;
    /**
     * 操作人id
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;
    /**
     * 应发总金额
     */
    private String payableAmount;
    /**
     * 服务费总金额
     */
    private String serviceAmount;
    /**
     * 批次编号
     */
    private String batchId;
    /**
     * 创建时间 yyyy-MM-dd HH:mm:ss
     */
    private String createdAt;
    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 发薪主体名称
     */
    private String subjectName;

    /**
     * 发薪方式 1小程序提现  2微信转账
     */
    private Integer payMethod;

    /**
     * 发薪方式意义
     */
    private String payMethodMsg;

    private Map<String, Integer> detailFailureReasonMap;

    /**
     * 扣费状态
     */
    private Integer deductionStatus;
    /**
     * 扣费状态意义
     */
    private String deductionStatusMsg;
    /**
     * 扣费失败原因
     */
    private String deductionFailureReason;
}
