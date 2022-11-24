package cn.xunhou.xbbcloud.rpc.attendance.pojo.result;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.cloud.core.page.PageInfo;
import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerProto;
import com.google.protobuf.util.Timestamps;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
public class AttendanceRecordResult extends PageInfo {

    //租户id
    private Long tenantId;
    //部门id
    private Long orgId;
    //企业员工id
    private Long empId;
    //排班详情id
    private Long workScheduleDetailId;
    //状态
    private Integer status;
    //是否调整过工时 （0否，1是）
    private Integer adjustWorkHourFlag;
    //上班打卡时间
    private Timestamp clockIn;
    //下班打卡时间
    private Timestamp clockOut;
    //打卡记录id
    private Long id;
    //上班打卡地址
    private String punchInAddress;
    //下班打卡地址
    private String punchOutAddress;
    //打卡工时
    private Double workHour;
    //实际工时
    private Double actualHour;
    //工时计算单位（分钟、半小时、小时）
    private Integer calculateUnit;
    // 调整工时备注
    private String adjustWorkHourRemark;
    //创建人
    private Long createBy;
    //更新人
    private Long modified_by;
    //考勤是否结束
    private boolean attendance_finish_flag;
    //更新时间
    private Timestamp updatedAt;


    public static AttendanceServerProto.RecordDetailBeResponse toAttendanceBeResponse(AttendanceRecordResult result) {
        if (result == null) {
            return null;
        }
        AttendanceServerProto.RecordDetailBeResponse.Builder builder = AttendanceServerProto.RecordDetailBeResponse.newBuilder()
                .setAttendanceRecordId(result.getId())
                .setActualHour(result.getActualHour())
                .setTenantId(result.getTenantId())
                .setEmpId(result.getEmpId())
                .setOrgId(result.getOrgId())
                .setWorkScheduleDetailId(result.getWorkScheduleDetailId())
                .setAttendanceRecordStatusEnum(AttendanceServerProto.AttendanceRecordStatusEnum.forNumber(result.getStatus()))
                .setAttendanceFinishFlag(result.attendance_finish_flag)
                .setAdjustWorkHourFlag(result.getAdjustWorkHourFlag() == 1)
                .setClockIn(Timestamps.fromMillis(result.getClockIn().getTime()))
                .setPunchInAddress(result.getPunchInAddress())
                .setPunchOutAddress(result.getPunchOutAddress())
                .setWorkHour(result.getWorkHour())
                .setAttendanceCalculateUnit(AttendanceServerProto.AttendanceCalculateUnit.forNumber(result.getCalculateUnit()))
                .setAdjustWorkHourRemark(result.getAdjustWorkHourRemark())
                .setCreateBy(result.getCreateBy())
                .setModifyBy(result.getModified_by())
                .setUpdatedAt(Timestamps.fromMillis(result.getUpdatedAt().getTime()));
        if (result.getClockOut() != null) {
            builder.setClockOut(Timestamps.fromMillis(result.getClockOut().getTime()));
        }
        return builder.build();
    }

    public static List<AttendanceServerProto.RecordDetailBeResponse> toRecordDetailBeResponseList(List<AttendanceRecordResult> param) {
        if (CollUtil.isEmpty(param)) {
            return Collections.emptyList();
        }
        return param.stream().map(AttendanceRecordResult::toAttendanceBeResponse).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
