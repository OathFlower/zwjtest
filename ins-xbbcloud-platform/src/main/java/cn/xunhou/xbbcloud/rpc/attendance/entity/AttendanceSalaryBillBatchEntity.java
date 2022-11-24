
package cn.xunhou.xbbcloud.rpc.attendance.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * ENGINE=InnoDB default charset=utf8mb4 comment='考勤打卡-薪资账单批次表'
 *
 * @author system
 * @since 2022-08-03 16:03:26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "attendance_salary_bill_batch")
public class AttendanceSalaryBillBatchEntity extends XbbSnowTimeTenantEntity {

    /**
     * 状态（0待发送，1发薪待审核，2发薪已审核，3失败）
     */
    private Integer status;

}
