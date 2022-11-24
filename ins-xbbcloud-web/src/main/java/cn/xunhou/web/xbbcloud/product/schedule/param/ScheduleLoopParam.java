package cn.xunhou.web.xbbcloud.product.schedule.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * @author litb
 * @date 2022/9/15 20:05
 * <p>
 * 循环排班入参
 */
@Getter
@Setter
@ToString
public class ScheduleLoopParam {

    /**
     * 循环排班的id
     */
    @NotNull(message = "循环排班id不能为空")
    private Long scheduleId;

    /**
     * 组织id
     */
    @NotNull(message = "组织id不能为空")
    private Long orgId;

    /**
     * 是否覆盖已有排班
     */
    @NotNull(message = "是否覆盖已有排班不能为空")
    private Boolean cover;
}
