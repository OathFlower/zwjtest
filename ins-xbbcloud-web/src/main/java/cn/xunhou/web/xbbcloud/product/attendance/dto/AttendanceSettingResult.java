package cn.xunhou.web.xbbcloud.product.attendance.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class AttendanceSettingResult {

    /**
     * 通用设置id
     */
    private Long commonSettingId;
    /**
     * 工时计算单位
     */
    private Integer attendanceCalculateUnit;
    /**
     * 最高结算工时
     */
    private Integer maxSettlementHour;
    /**
     * 打卡地址
     */
    private List<AttendanceAddressResult> addressResult;
}
