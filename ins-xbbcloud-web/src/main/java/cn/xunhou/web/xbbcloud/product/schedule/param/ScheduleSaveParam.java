package cn.xunhou.web.xbbcloud.product.schedule.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author litb
 * @date 2022/9/15 19:41
 * <p>
 * 排班保存入参
 */

@Getter
@Setter
@ToString
public class ScheduleSaveParam {

    /**
     * 当前周期排班id,编辑时传,新增时不传
     */
    private Long scheduleId;


    /**
     * 周期的开始时间 yyyy-MM-dd
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "开始时间不能为空")
    private LocalDate periodStartAt;

    /**
     * 周期的结束时间 yyyy-MM-dd
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "结束时间不能为空")
    private LocalDate periodEndAt;

    @NotNull(message = "组织id不能为空")
    private Long orgId;


    /**
     * 每个员工的排班信息
     */
    @Valid
    private List<EmployeeScheduleDetailParam> employeeScheduleList;


    /**
     * 员工排班详情
     */
    @Getter
    @Setter
    @ToString
    public static class EmployeeScheduleDetailParam {

        /**
         * 员工id
         */
        @NotNull(message = "员工id不能为空")
        private Long employeeId;

        /**
         * 排班详情id,编辑时传,新增则不传
         */
        private Long scheduleDetailId;

        /**
         * 排班开始时间 yyyy-MM-dd HH:mm"
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "utf8")
        @NotNull(message = "每日排班开始时间不能为空")
        private LocalDateTime startAt;

        /**
         * 排班结束时间 yyyy-MM-dd HH:mm"
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "utf8")
        @NotNull(message = "每日排班结束时间不能为空")
        private LocalDateTime endAt;

    }
}
