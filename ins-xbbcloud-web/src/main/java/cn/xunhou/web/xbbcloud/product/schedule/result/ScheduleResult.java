package cn.xunhou.web.xbbcloud.product.schedule.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author litb
 * @date 2022/9/15 16:59
 * <p>
 * 排班结果
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResult {

    /**
     * 当前周期排班id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 上一个周期的排班id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long preScheduleId;

    /**
     * 上一个周期是否有有效排班,根据该字段判断是否可以复制上一周排班
     */
    private boolean prePeriodHasValidSchedule;

    /**
     * 循环排班开始日期 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate loopPeriodStartDate;

    /**
     * 循环排班结束日期 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate loopPeriodEndDate;

    /**
     * 上个周期的开始时间 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate prePeriodStartDate;

    /**
     * 上个周期的结束时间 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate prePeriodEndDate;

    /**
     * 当前周期是否有有效排班,根据该字段判断是否可以循环排班
     */
    private boolean currentPeriodHasValidSchedule;

    /**
     * 当前周期内的排班总人数
     */
    private Integer scheduledEmployeeCount = 0;

    /**
     * 当前周所有员工的排班总工时
     */
    private String allEmployeeWorkingHours = "0";

    /**
     * 排班发布状态 0未发布 1发布
     */
    private Integer publishState;

    /**
     * 排班锁定状态 0未锁定 1锁定
     */
    private Integer lockState;

    /**
     * 该周期内的日期信息
     */
    private List<PerDayResult> scheduleDates;

    /**
     * 每个员工的排班信息
     */
    private List<EmployeeScheduleResult> employeeScheduleList;

    /**
     * 周期内每日信息
     */
    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerDayResult {

        /**
         * 日期 yyyy-MM-dd
         */
        private LocalDate scheduleDate;

        /**
         * 是否是过去时间
         */
        private Boolean passedBy;
    }

    /**
     * 员工排班信息
     */
    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class EmployeeScheduleResult {

        /**
         * 员工id
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long employeeId;

        /**
         * 休息天数
         */
        private Integer restDays = 7;

        /**
         * 工作天数
         */
        private Integer workingDays = 0;

        /**
         * 总工时
         */
        private String totalWorkingHours = "0";

        private Long totalWorkingSeconds = 0L;

        /**
         * 员工类型字符串
         */
        private String employeeTypeStr;

        /**
         * 员工编号
         */
        private String employeeNo;

        /**
         * 员工姓名
         */
        private String employeeName;

        /**
         * 每日的排班详情,如果该集合为空,则表示当前员工在此周没有任何排班,如果不为空,则固定为周一到周日
         */
        private List<EmployeeScheduleDetailResult> scheduleDetailList;

        /**
         * 周排班是否合法
         */
        private Boolean weekScheduleValid = true;

        /**
         * 是否已读,只有当前周有排班数据时该值才有意义 true已读 false未读
         */
        private Boolean hasRead;

        /**
         * 员工是否离职
         */
        private Boolean hasLeave = false;

        /**
         * 是否调换部门
         */
        private Boolean hasExchangeDept = false;

        /**
         * 离职时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate leaveDate;

    }

    /**
     * 员工排班详情
     */
    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class EmployeeScheduleDetailResult {

        /**
         * 当日是否有排班
         */
        private boolean hasScheduled;

        /**
         * 排班详情id
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long scheduleDetailId;

        /**
         * 周一-周日 1-7
         */
        private Integer dayOfWeek;

        /**
         * 排班开始时间 yyyy-MM-dd HH:mm"
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime startAt;

        /**
         * 排班结束时间 yyyy-MM-dd HH:mm"
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime endAt;

        /**
         * 给前端展示的排班时间范围,例如: "09:00-次日02:00"
         */
        private String displayTime;

        /**
         * 是否跨夜
         */
        private Boolean crossNight;

        /**
         * 当日工时
         */
        private String workHours;

        /**
         * 是否是合理排班
         */
        private Boolean valid;

        /**
         * 当日是否已离职
         */
        private Boolean todayHasLeave = false;
    }

}
