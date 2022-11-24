package cn.xunhou.web.xbbcloud.product.schedule.result;

import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerProto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ScheduleSettingResult {
    private Long id;
    /**
     * 周排班总工时 全职
     */
    private Integer fullTimeTotalWeeklyHours;
    /**
     * 日排班工时 全职
     */
    private Integer fullTimeDailyHours;
    /**
     * 每周休息天数
     */
    private Integer weeklyRestDays;
    /**
     * 周排班总工时 兼职
     */
    private Integer partTimeTotalWeeklyHours;
    /**
     * 日排班总工时 兼职
     */
    private Integer partTimeDailyHours;
    /**
     * 周排班总工时 全职(开关)
     */
    private boolean fullTimeTotalWeeklyHoursOpenFlag;
    /**
     * 日排班工时 全职(开关)
     */
    private boolean fullTimeDailyHoursOpenFlag;
    /**
     * 每周休息天数(开关)
     */
    private boolean weeklyRestDaysOpenFlag;
    /**
     * 连续上班天数(开关)
     */
    private boolean continuousWorkingDaysOpenFlag;
    /**
     * 周排班总工时 兼职(开关)
     */
    private boolean partTimeTotalWeeklyHoursOpenFlag;
    /**
     * 日排班总工时 兼职(开关)
     */
    private boolean partTimeDailyHoursOpenFlag;

    public static ScheduleSettingResult convertResponse2Result(ScheduleServerProto.ScheduleSetting scheduleSetting) {
        ScheduleSettingResult scheduleSettingResult = new ScheduleSettingResult();
        scheduleSettingResult.setId(scheduleSetting.getCommonSettingId());
        scheduleSettingResult.setFullTimeTotalWeeklyHours((int) scheduleSetting.getFullTimeTotalWeeklyHours());
        scheduleSettingResult.setFullTimeDailyHours((int) scheduleSetting.getFullTimeDailyHours());
        scheduleSettingResult.setWeeklyRestDays((int) scheduleSetting.getWeeklyRestDays());
        scheduleSettingResult.setPartTimeTotalWeeklyHours((int) scheduleSetting.getPartTimeTotalWeeklyHours());
        scheduleSettingResult.setPartTimeDailyHours((int) scheduleSetting.getPartTimeDailyHours());
        scheduleSettingResult.setFullTimeTotalWeeklyHoursOpenFlag(scheduleSetting.getFullTimeTotalWeeklyHoursOpenFlag());
        scheduleSettingResult.setFullTimeDailyHoursOpenFlag(scheduleSetting.getFullTimeDailyHoursOpenFlag());
        scheduleSettingResult.setWeeklyRestDaysOpenFlag(scheduleSetting.getWeeklyRestDaysOpenFlag());
        scheduleSettingResult.setContinuousWorkingDaysOpenFlag(scheduleSetting.getContinuousWorkingDaysOpenFlag());
        scheduleSettingResult.setPartTimeTotalWeeklyHoursOpenFlag(scheduleSetting.getPartTimeTotalWeeklyHoursOpenFlag());
        scheduleSettingResult.setPartTimeDailyHoursOpenFlag(scheduleSetting.getPartTimeDailyHoursOpenFlag());
        return scheduleSettingResult;

    }

    public static ScheduleServerProto.ScheduleSetting.Builder convertResult2Request(ScheduleSettingResult result) {
        ScheduleServerProto.ScheduleSetting.Builder builder = ScheduleServerProto.ScheduleSetting.newBuilder();
        builder.setCommonSettingId(result.getId());
        builder.setFullTimeTotalWeeklyHours(result.getFullTimeTotalWeeklyHours());
        builder.setFullTimeDailyHours(result.getFullTimeDailyHours());
        builder.setWeeklyRestDays(result.getWeeklyRestDays());
        builder.setPartTimeTotalWeeklyHours(result.getPartTimeTotalWeeklyHours());
        builder.setPartTimeDailyHours(result.getPartTimeDailyHours());
        builder.setFullTimeTotalWeeklyHoursOpenFlag(result.isFullTimeTotalWeeklyHoursOpenFlag());
        builder.setFullTimeDailyHoursOpenFlag(result.isFullTimeDailyHoursOpenFlag());
        builder.setWeeklyRestDaysOpenFlag(result.isWeeklyRestDaysOpenFlag());
        builder.setContinuousWorkingDaysOpenFlag(result.isContinuousWorkingDaysOpenFlag());
        builder.setPartTimeTotalWeeklyHoursOpenFlag(result.isPartTimeTotalWeeklyHoursOpenFlag());
        builder.setPartTimeDailyHoursOpenFlag(result.isPartTimeDailyHoursOpenFlag());
        return builder;
    }
}
