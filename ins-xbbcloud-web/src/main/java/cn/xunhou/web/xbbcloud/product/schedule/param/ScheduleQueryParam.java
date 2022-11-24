package cn.xunhou.web.xbbcloud.product.schedule.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @author litb
 * @date 2022/9/15 19:21
 * <p>
 * 排班查询入参
 */
@Getter
@Setter
@ToString
public class ScheduleQueryParam {

    /**
     * 部门id
     */
    @NotNull(message = "部门id不能为空")
    private Long orgId;

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
}
