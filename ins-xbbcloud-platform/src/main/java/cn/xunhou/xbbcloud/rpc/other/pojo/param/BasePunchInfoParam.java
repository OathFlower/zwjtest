package cn.xunhou.xbbcloud.rpc.other.pojo.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
public class BasePunchInfoParam {

    //考勤打卡地址配置id
    private Long attendanceConfigAddressId;
    //打卡地址
    private String punchAddress;
    //打卡时间
    private Timestamp clock;
    //经度
    private Double longitude;
    //维度
    private Double latitude;
}
