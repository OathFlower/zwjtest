
package cn.xunhou.xbbcloud.rpc.schedule.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.sql.Date;

/**
 * comment '排班表'
 *
 * @author system
 * @since 2022-08-03 16:03:26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "schedule")
public class ScheduleEntity extends XbbSnowTimeTenantEntity {


    /**
     * 部门id
     */
    private Long orgId;

    /**
     * 开始日期 yyyyMMdd
     */
    private Date startDate;

    /**
     * 结束日期 yyyyMMdd
     */
    private Date endDate;

    /**
     * 发布状态 0未发布 1发布
     */
    private Integer publishState;

    /**
     * 锁定状态 0未锁定 1锁定
     */
    private Integer lockState;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long modifyBy;

}
