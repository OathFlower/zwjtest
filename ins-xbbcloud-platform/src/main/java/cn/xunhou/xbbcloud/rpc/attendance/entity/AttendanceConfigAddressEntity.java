
package cn.xunhou.xbbcloud.rpc.attendance.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * ENGINE=InnoDB default charset=utf8mb4 comment='考勤打卡配置-考勤地点表'
 *
 * @author system
 * @since 2022-08-03 16:03:26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "attendance_config_address")
public class AttendanceConfigAddressEntity extends XbbSnowTimeEntity {


    /**
     * 考勤配置id
     */
    private Long commonConfigId;

    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 部门id
     */
    private Long orgId;

    /**
     * 偏移距离
     */
    private Integer offsetDistance;

    /**
     * 地址经度
     */
    private Double longitude;

    /**
     * 地址纬度
     */
    private Double latitude;

    /**
     * 定位地址
     */
    private String locationAddress;

    /**
     * 地址名称
     */
    private String addressName;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long modifiedBy;

}
