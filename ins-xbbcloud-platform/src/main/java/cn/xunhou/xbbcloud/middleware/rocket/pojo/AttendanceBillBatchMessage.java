package cn.xunhou.xbbcloud.middleware.rocket.pojo;


import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 考勤账单msg
 */
@Data
@Accessors(chain = true)
@Slf4j
public class AttendanceBillBatchMessage {

    //批次号（数据库雪花id主键）
    private String batchNo;
    //租户id
    private Integer tennatId;
    //绑定的人力客户
    private List<Long> customerIds;
    //考勤账单详情
    private List<AttendanceBillBatchDetailMessage> attendanceBillBatchDetailMessages;


    @Data
    @Accessors(chain = true)
    @Slf4j
    public static class AttendanceBillBatchDetailMessage {

        //详情批次号（数据库雪花id主键）
        private String detailBatchNo;
        //租户id
        private Integer tennatId;
        //考勤打卡id
        private Long attendanceRecordId;
        //身份证
        private String idCardNo;
        //计薪方式 1件/2时/3日/4周/5月/6季度
        private String salaryUnit;
        //考勤打卡日 yyyyMMdd
        private String punchDay;
        //应出勤
        private String requiredAttendance;
        //实际出勤
        private String realAttendance;

    }
}
