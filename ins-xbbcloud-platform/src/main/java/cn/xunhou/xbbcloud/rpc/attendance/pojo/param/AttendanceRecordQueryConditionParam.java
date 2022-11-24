package cn.xunhou.xbbcloud.rpc.attendance.pojo.param;

import cn.xunhou.cloud.core.page.PageInfo;
import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerProto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class AttendanceRecordQueryConditionParam extends PageInfo {

    //租户id
    private Long tenantId;
    //部门id
    private Long orgId;
    //企业员工id
    private Long empId;
    private List<Long> empIds;
    //状态
    Integer attendanceRecordStatus;
    //查询日期开始
    private String dateStart;
    //查询日期结束
    private String dateEnd;
    //打卡记录id
    private Long attendanceRecordId;
    private List<Long> attendanceRecordIds;
    //考勤是否结束
    private Boolean attendanceFinishFlag;
    //打卡时间 格式：yyyy-MM-dd
    private String punchDate;

    public static AttendanceRecordQueryConditionParam of(AttendanceServerProto.RecordQueryConditionBeRequest request) {
        if (request == null) {
            return null;
        }
        AttendanceRecordQueryConditionParam param = new AttendanceRecordQueryConditionParam();
        param.setTenantId(request.getTenantId());
        param.setOrgId(request.getOrgId());
        param.setEmpId(request.getEmpId());
        param.setDateStart(request.getClockInStart());
        param.setDateEnd(request.getClockInEnd());
        param.setAttendanceRecordId(request.getAttendanceRecordId());
        param.setAttendanceRecordIds(request.getAttendanceRecordIdsList());
        if (request.hasAttendanceFinishFlag()) {
            param.setAttendanceFinishFlag(request.getAttendanceFinishFlag());
        }
        if (request.hasAttendanceRecordStatusEnum()) {
            param.setAttendanceRecordStatus(request.getAttendanceRecordStatusEnumValue());
        }
        param.setEmpIds(request.getEmpIdsList());
        param.setPaged(request.getPaged());
        param.setCurPage(request.getCurPage());
        param.setPageSize(request.getPageSize());
        param.setPunchDate(request.getPunchDate());
        return param;
    }
}
