package cn.xunhou.xbbcloud.rpc.salary.pojo.result;

import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryDetailEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;


@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryBatchResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long tenantId;
    /**
     * 发薪主体id
     */
    private Long subjectId;
    /**
     * 项目名称
     */
    private String productName;

    /**
     * 计薪年月 yyyymm
     */
    private String month;

    /**
     * 状态  0进行中 1全部成功 2部分成功 3全部失败
     */
    private Integer status;
    /**
     * 发薪人数
     */
    private Integer peopleCount;
    /**
     * 操作人id
     */
    private Long operatorId;
    /**
     * 应发总金额
     */
    private Integer payableAmount;
    /**
     * 服务费总金额
     */
    private Integer serviceAmount;
    /**
     * 批次号
     */
    private Long batchId;
    /**
     * 创建时间
     */
    private Timestamp createdAt;
    /**
     * 发薪方式 1小程序提现  2微信转账
     */
    private Integer payMethod;
    /**
     * 商户信息json
     */
    private String expandJson;
    private List<SalaryDetailEntity> salaryDetailEntityList;

    /**
     * 扣费状态
     */
    private Integer deductionStatus;

    /**
     * 扣费失败原因
     */
    private String deductionFailureReason;

}
