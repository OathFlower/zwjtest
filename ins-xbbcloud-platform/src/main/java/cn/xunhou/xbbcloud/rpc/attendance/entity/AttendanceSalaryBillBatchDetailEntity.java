
package cn.xunhou.xbbcloud.rpc.attendance.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * ENGINE=InnoDB default charset=utf8mb4 comment='考勤打卡-薪资账单详情表'
 *
 * @author system
 * @since 2022-08-03 16:03:26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "attendance_salary_bill_batch_detail")
public class AttendanceSalaryBillBatchDetailEntity extends XbbSnowTimeTenantEntity {


    /**
     * 薪资账单批次id
     */
    private Long salaryAttendanceBillBatchId;

    /**
     * 考勤打卡记录id
     */
    private Long attendanceRecordId;

    /**
     * 金额
     */
    private BigDecimal money;

    /**
     * 发薪方式
     */
    private Integer payType;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    //银行名称
    private String bankName;

    /**
     * 状态（0待发送，1发薪待审核，2发薪已审核）
     */
    private Integer status;

    /**
     * 子状态（0失败，1成功）
     */
    private Integer sub_status;

    /**
     * 失败原因
     */
    private String fail_reason;

}
