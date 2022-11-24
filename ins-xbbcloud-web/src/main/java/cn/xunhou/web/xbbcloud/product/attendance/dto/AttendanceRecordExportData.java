package cn.xunhou.web.xbbcloud.product.attendance.dto;

import cn.xunhou.cloud.task.core.XbbTableField;
import cn.xunhou.cloud.task.core.XbbTemplateEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AttendanceRecordExportData extends XbbTemplateEntity {
    /**
     * 员工编号
     */
    @XbbTableField(headName = "员工编号")
    private String staffNo;
    /**
     * 员工姓名
     */
    @XbbTableField(headName = "员工姓名")
    private String name;
    /**
     * 部门名称
     */
    @XbbTableField(headName = "部门名称")
    private String orgName;
    /**
     * 排班时间开始
     */
    @XbbTableField(headName = "排班时间开始")
    private String scheduleTimeStart;
    /**
     * 排班时间结束
     */
    @XbbTableField(headName = "排班时间结束")
    private String scheduleTimeEnd;
    /**
     * 上班时间
     */
    @XbbTableField(headName = "上班时间")
    private String clockInTime;
    /**
     * 下班时间
     */
    @XbbTableField(headName = "下班时间")
    private String clockOutTime;
    /**
     * 上班打卡地址
     */
    @XbbTableField(headName = "上班打卡地址")
    private String punchInAddress;
    /**
     * 下班打卡地址
     */
    @XbbTableField(headName = "下班打卡地址")
    private String punchOutAddress;
    /**
     * 排班工时
     */
    @XbbTableField(headName = "排班工时")
    private float scheduleWorkHours;
    /**
     * 实际工时
     */
    @XbbTableField(headName = "实际工时")
    private float actualHour;
    /**
     * 工时计算单位
     */
    @XbbTableField(headName = "工时计算单位")
    private String calculateUnit;
    /**
     * 日期
     */
    @XbbTableField(headName = "日期")
    private String date;
}
