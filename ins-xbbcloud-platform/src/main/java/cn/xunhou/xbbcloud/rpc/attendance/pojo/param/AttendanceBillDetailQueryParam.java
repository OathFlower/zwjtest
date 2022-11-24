package cn.xunhou.xbbcloud.rpc.attendance.pojo.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class AttendanceBillDetailQueryParam {

    //薪资账单批次id
    private List<Long> attendanceBillBatchIds;
    //租户id
    private List<Long> tenantIds;
    //考勤记录id
    private List<Long> attendanceRecordIds;
    //状态（0失败，1待发送，2发薪待审核，3发薪已审核）
    private Integer status;

}
