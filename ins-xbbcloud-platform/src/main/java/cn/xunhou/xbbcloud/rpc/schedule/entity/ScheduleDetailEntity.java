
package cn.xunhou.xbbcloud.rpc.schedule.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * comment '排班详情表'
 *
 * @author system
 * @since 2022-08-03 16:03:26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "schedule_detail")
public class ScheduleDetailEntity extends XbbSnowTimeTenantEntity {


    /**
     * 部门id
     */
    private Long orgId;

    /**
     * 排班班次id
     */
    private Long workScheduleId;

    /**
     * 员工id
     */
    private Long employeeId;

    /**
     * 开始时间
     */
    private Timestamp startDatetime;

    /**
     * 结束时间
     */
    private Timestamp endDatetime;

    /**
     * 周一-周日 从1开始
     */
    private Integer dayOfWeek;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long modifyBy;


}
