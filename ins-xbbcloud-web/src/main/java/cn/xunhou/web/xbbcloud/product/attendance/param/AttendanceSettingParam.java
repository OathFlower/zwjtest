package cn.xunhou.web.xbbcloud.product.attendance.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class AttendanceSettingParam {
    /**
     * 通用设置id
     */
    @NotNull(message = "id不可为空")
    private Long commonSettingId;
    /**
     * 工时计算单位
     */
    private Integer attendanceCalculateUnit;
    /**
     * 最高结算工时
     */
    @Max(24)
    private Integer maxSettlementHour;
    @Valid
    private AttendanceAddressParam addressParam;
}
