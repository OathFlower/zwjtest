package cn.xunhou.xbbcloud.rpc.attendance.pojo.result;

import cn.xunhou.xbbcloud.rpc.other.pojo.result.CommonSettingResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class AttendanceSettingResult extends CommonSettingResult {
    //每日最高结算工时
    private Integer maxSettlementHour = 0;
    //工时计算单位(默认半小时)
    private Integer calculateUnit = 1;
    //打卡地点
    private List<AttendanceConfigAddressResult> addressList;
}
