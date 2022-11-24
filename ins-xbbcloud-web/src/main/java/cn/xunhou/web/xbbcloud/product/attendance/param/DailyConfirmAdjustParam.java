package cn.xunhou.web.xbbcloud.product.attendance.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
public class DailyConfirmAdjustParam {
    @NotNull(message = "id不可为空")
    private Long id;
    /**
     * 调整工时
     */
    @NotNull(message = "调整工时不可为空")
    private String newWorkHours;
    /**
     * 备注
     */
    @Size(max = 100, message = "备注长度不能超过100字")
    private String remark;
}
