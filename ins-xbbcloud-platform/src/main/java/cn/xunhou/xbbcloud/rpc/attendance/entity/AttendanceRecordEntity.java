
package cn.xunhou.xbbcloud.rpc.attendance.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerProto;
import com.google.protobuf.util.Timestamps;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ENGINE=InnoDB default charset=utf8mb4 comment='考勤打卡记录表'
 *
 * @author system
 * @since 2022-08-03 16:03:26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "attendance_record")
public class AttendanceRecordEntity extends XbbSnowTimeTenantEntity {


    /**
     * 企业员工id
     */
    private Long empId;

    /**
     * 部门id
     */
    private Long orgId;

    /**
     * 排班详情id
     */
    private Long workScheduleDetailId;

    /**
     * 上班打卡时间
     */
    private Timestamp clockIn;

    /**
     * 下班打卡时间
     */
    private Timestamp clockOut;

    /**
     * 上班考勤打卡地址配置id
     */
    private Long punchInAttendanceConfigAddressId;

    /**
     * 下班考勤打卡地址配置id
     */
    private Long punchOutAttendanceConfigAddressId;

    /**
     * 上班打卡地址
     */
    private String punchInAddress;

    /**
     * 下班打卡地址
     */
    private String punchOutAddress;

    /**
     * 打卡工时
     */
    private Double workHour;

    /**
     * 实际工时
     */
    private Double actualHour;

    /**
     * 工时计算单位（分钟、半小时、小时）
     */
    private Integer calculateUnit;

    /**
     * 调整工时备注
     */
    private String adjustWorkHourRemark;

    /**
     * 是否调整过工时 （0否，1是）
     */
    private Integer adjustWorkHourFlag;

    /**
     * 状态（0上班打卡，1下班打卡(未审核)，2已审核）
     */
    private Integer status;

    /**
     * 考勤是否结束（0可用，1不可用）
     */
    private Integer attendanceFinishFlag;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long modifiedBy;

    public static AttendanceServerProto.RecordDetailBeResponse toRecordDetailBeResponse(AttendanceRecordEntity attendanceRecordEntity) {
        if (null == attendanceRecordEntity) {
            return null;
        }
        return AttendanceServerProto.RecordDetailBeResponse.newBuilder()
                .setAttendanceRecordId(attendanceRecordEntity.getId())
                .setTenantId(attendanceRecordEntity.getTenantId())
                .setEmpId(attendanceRecordEntity.getEmpId())
                .setOrgId(attendanceRecordEntity.getOrgId())
                .setWorkScheduleDetailId(attendanceRecordEntity.getWorkScheduleDetailId())
                .setClockOut(Timestamps.fromMillis(attendanceRecordEntity.getClockOut().getTime()))
                .setClockIn(Timestamps.fromMillis(attendanceRecordEntity.getClockIn().getTime()))
                .setPunchOutAddress(attendanceRecordEntity.getPunchOutAddress())
                .setPunchInAddress(attendanceRecordEntity.getPunchOutAddress())
                .setPunchInAttendanceConfigAddressId(attendanceRecordEntity.getPunchInAttendanceConfigAddressId())
                .setPunchOutAttendanceConfigAddressId(attendanceRecordEntity.getPunchInAttendanceConfigAddressId())
                .setWorkHour(attendanceRecordEntity.getWorkHour())
                .setActualHour(attendanceRecordEntity.getActualHour())
                .setAttendanceCalculateUnit(AttendanceServerProto.AttendanceCalculateUnit.forNumber(attendanceRecordEntity.getCalculateUnit()))
                .setAdjustWorkHourRemark(attendanceRecordEntity.getAdjustWorkHourRemark())
                .setAttendanceFinishFlag(attendanceRecordEntity.getAttendanceFinishFlag() == 1)
                .setCreateBy(attendanceRecordEntity.getCreateBy())
                .setModifyBy(attendanceRecordEntity.getModifiedBy())
                .setAdjustWorkHourFlag(attendanceRecordEntity.getAdjustWorkHourFlag() == 1).build();
    }

}
