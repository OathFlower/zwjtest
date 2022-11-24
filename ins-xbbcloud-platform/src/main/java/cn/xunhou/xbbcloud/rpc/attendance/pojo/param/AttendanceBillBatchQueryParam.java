package cn.xunhou.xbbcloud.rpc.attendance.pojo.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class AttendanceBillBatchQueryParam {

    //租户id
    private Integer tenantId;

    //状态（-1失败，0待发送，1发薪待审核，2发薪已审核）
    private Integer status;


}
