package cn.xunhou.web.xbbcloud.product.attendance.controller;

import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.attendance.dto.AttendanceRecordResult;
import cn.xunhou.web.xbbcloud.product.attendance.dto.AttendanceSettingResult;
import cn.xunhou.web.xbbcloud.product.attendance.dto.GeoKeywordsQueryResult;
import cn.xunhou.web.xbbcloud.product.attendance.param.AttendanceRecordParam;
import cn.xunhou.web.xbbcloud.product.attendance.param.AttendanceSettingParam;
import cn.xunhou.web.xbbcloud.product.attendance.param.DailyConfirmAdjustParam;
import cn.xunhou.web.xbbcloud.product.attendance.service.AttendanceRecordService;
import cn.xunhou.web.xbbcloud.util.GeoCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 考勤云
 *
 * @author zhouyitian
 */
@RequestMapping("/attendance")
@Slf4j
@RestController
public class AttendanceRecordController {

    @Resource
    private AttendanceRecordService attendanceRecordService;


    /**
     * 调整工时
     *
     * @param adjustParam
     * @return
     */
    @PostMapping("/daily_work_hour/adjust")
    public JsonResponse<Void> adjustWorkHours(@RequestBody @Valid DailyConfirmAdjustParam adjustParam) {
        return attendanceRecordService.adjustWorkHours(adjustParam);
    }

    /**
     * 确认工时
     *
     * @param id
     * @return
     */
    @PostMapping("/daily_work_hour/{id}/confirm")
    public JsonResponse<Void> confirmWorkHours(@PathVariable("id") Long id) {
        return attendanceRecordService.confirmWorkHours(id);
    }

    /**
     * 日工时确认，打卡记录列表
     *
     * @param param
     * @return
     */
    @GetMapping("/record/list")
    public JsonListResponse<AttendanceRecordResult> recordList(@Validated AttendanceRecordParam param) {
        param.setPaged(true);
        return attendanceRecordService.recordList(param);
    }

    /**
     * 保存设置信息
     *
     * @param settingParam
     * @return
     */
    @PostMapping("/setting")
    public JsonResponse<Void> saveSetting(@RequestBody @Validated AttendanceSettingParam settingParam) {
        return attendanceRecordService.saveSetting(settingParam);
    }

    /**
     * 获取设置信息
     *
     * @return
     */
    @GetMapping("/setting")
    public JsonResponse<AttendanceSettingResult> getSetting() {
        return attendanceRecordService.getSetting();
    }

    /**
     * 根据关键字查询地址
     *
     * @param keyword
     * @return
     */
    @GetMapping("/geo_map")
    public JsonListResponse<GeoKeywordsQueryResult> queryByKeyWord(String keyword) {
        return JsonListResponse.success(GeoCodeUtils.queryByKeyWord(keyword, 1, 25));
    }

    /**
     * 删除考勤地址
     * @param id
     * @return
     */
    @DeleteMapping("/address/{id}")
    public JsonResponse<Void> deleteAddress(@PathVariable("id") Long id) {
        return attendanceRecordService.deleteAddress(id);
    }

    /**
     * 导出
     * @param param
     * @return
     */
    @GetMapping("/record/export")
    public JsonResponse<String> export(AttendanceRecordParam param){
       return attendanceRecordService.export(param);
    }

}
