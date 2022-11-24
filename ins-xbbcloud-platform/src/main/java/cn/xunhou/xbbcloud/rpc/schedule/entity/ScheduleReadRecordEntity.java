
package cn.xunhou.xbbcloud.rpc.schedule.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * comment '排班C端用户已读记录表'
 *
 * @author system
 * @since 2022-08-03 16:03:26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "schedule_read_record")
public class ScheduleReadRecordEntity extends XbbSnowTimeTenantEntity {


    /**
     * 排班班次id
     */
    private Long workScheduleId;

    /**
     * 员工id
     */
    private Long employeeId;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long modifyBy;

}
