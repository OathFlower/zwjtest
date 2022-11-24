package cn.xunhou.web.xbbcloud.product.attendance.service;

import cn.xunhou.cloud.task.core.AbstractXbbExportService;
import cn.xunhou.cloud.task.core.XbbTableTemplate;
import cn.xunhou.web.xbbcloud.product.attendance.dto.AttendanceRecordExportData;
import cn.xunhou.web.xbbcloud.product.attendance.param.AttendanceRecordParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
@XbbTableTemplate(templateName = "AttendanceRecordExportService")
public class AttendanceRecordExportService extends AbstractXbbExportService<AttendanceRecordExportData, AttendanceRecordParam> {

    @Autowired
    private AttendanceRecordService attendanceRecordService;

    @Override
    public List<AttendanceRecordExportData> exportData(List<String> list, AttendanceRecordParam param, boolean b, int i, int i1) {
        log.info("查询导出参数" + param);
        return attendanceRecordService.getExportData(param);
    }

}
