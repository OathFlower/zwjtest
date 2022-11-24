package cn.xunhou.xbbcloud.rpc.schedule.pojo.result;

import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerProto;
import cn.xunhou.xbbcloud.rpc.other.pojo.result.CommonSettingResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ScheduleSettingResult extends CommonSettingResult {
    //周排班总工时 全职
    private float fullTimeTotalWeeklyHours = 0;
    //日排班工时 全职
    private float fullTimeDailyHours = 0;
    //每周休息天数
    private float weeklyRestDays = 0;
    //连续上班天数
    private float continuousWorkingDays = 0;
    //周排班总工时 兼职
    private float partTimeTotalWeeklyHours = 0;
    //日排班总工时 兼职
    private float partTimeDailyHours = 0;
    //周排班总工时 全职(开关)
    private boolean fullTimeTotalWeeklyHoursOpenFlag = false;
    //日排班工时 全职(开关)
    private boolean fullTimeDailyHoursOpenFlag = false;
    //每周休息天数(开关)
    private boolean weeklyRestDaysOpenFlag = false;
    //连续上班天数(开关)
    private boolean continuousWorkingDaysOpenFlag = false;
    //周排班总工时 兼职(开关)
    private boolean partTimeTotalWeeklyHoursOpenFlag = false;
    //日排班总工时 兼职(开关)
    private boolean partTimeDailyHoursOpenFlag = false;


    public static ScheduleServerProto.ScheduleSetting convert2Response(ScheduleSettingResult result) {
        if (result == null) {
            return ScheduleServerProto.ScheduleSetting.newBuilder().build();
        }
        return ScheduleServerProto.ScheduleSetting.newBuilder()
                .setFullTimeTotalWeeklyHours(result.getFullTimeTotalWeeklyHours())
                .setFullTimeDailyHours(result.getFullTimeDailyHours())
                .setWeeklyRestDays(result.getWeeklyRestDays())
                .setContinuousWorkingDays(result.getContinuousWorkingDays())
                .setPartTimeTotalWeeklyHours(result.getPartTimeTotalWeeklyHours())
                .setPartTimeDailyHours(result.getPartTimeDailyHours())
                .setFullTimeTotalWeeklyHoursOpenFlag(result.isFullTimeTotalWeeklyHoursOpenFlag())
                .setFullTimeDailyHoursOpenFlag(result.isFullTimeDailyHoursOpenFlag())
                .setWeeklyRestDaysOpenFlag(result.isWeeklyRestDaysOpenFlag())
                .setContinuousWorkingDaysOpenFlag(result.isContinuousWorkingDaysOpenFlag())
                .setPartTimeTotalWeeklyHoursOpenFlag(result.isPartTimeTotalWeeklyHoursOpenFlag())
                .setPartTimeDailyHoursOpenFlag(result.isPartTimeDailyHoursOpenFlag())
                .setCommonSettingId(result.getCommonSettingId())
                .setCreateBy(result.getCreateBy())
                .build();
    }

}
