package cn.xunhou.web.xbbcloud.product.attendance.dto;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerProto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 日工时确认dto
 *
 * @author EDZ
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class AttendanceRecordResult {


    private static final String yyyyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";
    private static final String yyyyMMdd = "yyyy-MM-dd";

    private Long id;
    /**
     * 员工编号
     */
    private String staffNo;
    /**
     * 员工姓名
     */
    private String name;
    /**
     * 部门名称
     */
    private String orgName;
    /**
     * 排班时间开始
     */
    private String scheduleTimeStart;
    /**
     * 排班时间结束
     */
    private String scheduleTimeEnd;
    /**
     * 上班时间
     */
    private String clockInTime;
    /**
     * 下班时间
     */
    private String clockOutTime;
    /**
     * 上班打卡地址
     */
    private String punchInAddress;
    /**
     * 下班打卡地址
     */
    private String punchOutAddress;
    /**
     * 排班工时
     */
    private float scheduleWorkHours;
    /**
     * 调整工时
     */
    private float actualHour;
    /**
     * 实际工时
     */
    private float workHour;
    /**
     * 工时计算单位code
     */
    private Integer calculateUnitCode;
    private String calculateUnit;
    /**
     * 日期
     */
    private String date;
    /**
     * 是否勋厚员工
     */
    private boolean fromXhFlag;
    /**
     * 操作人id
     */
    private Long accountsId;
    /**
     * 操作人姓名
     */
    private String accountsName;
    /**
     * 操作时间
     */
    private String updatedAt;

    public static List<AttendanceRecordResult> convertResponse2Result(List<AttendanceServerProto.RecordDetailBeResponse> responses,
                                                                      Map<Long, HrmServiceProto.EmployeePageResponse> employeePageResponseMap,
                                                                      Long dictionaryId,
                                                                      Map<Long, HrmServiceProto.AccountDetailBeResponse> accountDetailBeResponseMap
    ) {
        if (CollectionUtils.isEmpty(responses)) {
            return Collections.emptyList();
        }
        List<AttendanceRecordResult> resultList = new ArrayList<>();
        for (AttendanceServerProto.RecordDetailBeResponse response : responses) {
            HrmServiceProto.EmployeePageResponse employeePageResponse = employeePageResponseMap.get(response.getEmpId());
            Date clockInDate = new Date(response.getClockIn().getSeconds() * 1000);
            AttendanceRecordResult result = new AttendanceRecordResult()
                    .setId(response.getAttendanceRecordId())
                    .setStaffNo(employeePageResponse.getPersonNumber())
                    .setName(employeePageResponse.getName())
                    .setOrgName(employeePageResponse.getOrgName())
                    .setClockInTime(DateUtil.format(clockInDate, yyyyMMddHHmmss))
                    .setPunchInAddress(response.getPunchInAddress())
                    .setPunchOutAddress(response.getPunchOutAddress())
                    .setActualHour((float) response.getActualHour())
                    .setWorkHour((float) response.getWorkHour())
                    .setCalculateUnitCode(response.getAttendanceCalculateUnit().getNumber())
                    .setDate(DateUtil.format(clockInDate, yyyyMMdd))
                    .setFromXhFlag((dictionaryId.equals(employeePageResponse.getInviteType()) && employeePageResponse.getEmployeeSource() == 2));
            if (response.hasClockOut()) {
                result.setClockOutTime(DateUtil.format(new Date(response.getClockOut().getSeconds() * 1000), yyyyMMddHHmmss));
            }
            String calculateUnit = "";
            switch (response.getAttendanceCalculateUnit()) {
                case PC_MINUTES_UNIT:
                    calculateUnit = "分钟";
                    break;
                case PC_HALF_HOUR_UNIT:
                    calculateUnit = "半小时";
                    break;
                case PC_HOURS_UNIT:
                    calculateUnit = "小时";
                    break;
                default:
                    break;
            }
            result.setCalculateUnit(calculateUnit);
            if (response.hasScheduleTimeStart()) {
                Date scheduleTimeStart = new Date(response.getScheduleTimeStart().getSeconds() * 1000);
                result.setScheduleTimeStart(DateUtil.format(scheduleTimeStart, yyyyMMddHHmmss));
            }
            if (response.hasScheduleTimeEnd()) {
                Date scheduleTimeEnd = new Date(response.getScheduleTimeEnd().getSeconds() * 1000);
                result.setScheduleTimeEnd(DateUtil.format(scheduleTimeEnd, yyyyMMddHHmmss));
            }
            if (response.hasScheduleTimeStart() && response.hasScheduleTimeEnd()) {
                Date scheduleTimeStart = new Date(response.getScheduleTimeStart().getSeconds() * 1000);
                Date scheduleTimeEnd = new Date(response.getScheduleTimeEnd().getSeconds() * 1000);
                result.setScheduleWorkHours(DateUtil.between(scheduleTimeStart, scheduleTimeEnd, DateUnit.HOUR));
            }
            HrmServiceProto.AccountDetailBeResponse accountDetailBeResponse = accountDetailBeResponseMap.get(response.getModifyBy());
            if (accountDetailBeResponse != null) {
                result.setAccountsId(accountDetailBeResponse.getId());
                result.setAccountsName(accountDetailBeResponse.getNickName());
                result.setUpdatedAt(DateUtil.format(new Date(response.getUpdatedAt().getSeconds() * 1000), yyyyMMddHHmmss));
            }
            resultList.add(result);
        }
        return resultList;
    }
}
