package cn.xunhou.web.xbbcloud.product.schedule.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.universal.UniversalServiceGrpc;
import cn.xunhou.grpc.proto.universal.UniversalServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerGrpc;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerProto;
import cn.xunhou.web.xbbcloud.product.hrm.result.EmployeeDictionaryResult;
import cn.xunhou.web.xbbcloud.product.schedule.param.ScheduleQueryParam;
import cn.xunhou.web.xbbcloud.product.schedule.param.ScheduleSaveParam;
import cn.xunhou.web.xbbcloud.product.schedule.result.ScheduleResult;
import cn.xunhou.web.xbbcloud.product.schedule.result.ScheduleSettingResult;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.validation.UnexpectedTypeException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author litb
 * @date 2022/9/16 10:50
 * <p>
 * 排班Service
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Service
@Slf4j
public class ScheduleService {

    @GrpcClient("ins-xhportal-platform")
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;
    @GrpcClient("ins-xbbcloud-platform")
    private ScheduleServerGrpc.ScheduleServerBlockingStub scheduleServerBlockingStub;
    @GrpcClient("ins-xhwallet-platform")
    private UniversalServiceGrpc.UniversalServiceBlockingStub universalServiceBlockingStub;

    private final static XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    /**
     * 排班详情
     *
     * @param param 参数
     * @return 结果
     */
    public ScheduleResult detail(ScheduleQueryParam param) {
        ScheduleServerProto.ScheduleResponse scheduleResponse = scheduleServerBlockingStub.querySchedule(ScheduleServerProto.ScheduleRequest.newBuilder()
                .setScheduleStartTime(Timestamp.newBuilder().setSeconds(param.getPeriodStartAt().atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8))).build())
                .setScheduleEndTime(Timestamp.newBuilder().setSeconds(param.getPeriodEndAt().atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8))).build())
                .setOrgId(param.getOrgId())
                .build());

        ScheduleResult scheduleResult = new ScheduleResult();
        fillPeriodInfo(param, scheduleResult);

        //查询部门下的所有员工
        HrmServiceProto.EmployeePageResponses employeeByOrgListResponse =
                hrmServiceBlockingStub.findEmployeeByOrgList(HrmServiceProto.FindEmployeeByOrgRequest.newBuilder()
                        .setId(param.getOrgId())
                        .setIsLeave(0)
                        .build());

        scheduleResult.setScheduleId(scheduleResponse.getWorkScheduleId() == 0 ? null : scheduleResponse.getWorkScheduleId());
        if (scheduleResponse.getPreWorkScheduleId() != 0) {
            scheduleResult.setPreScheduleId(scheduleResponse.getPreWorkScheduleId());
        }
        scheduleResult.setPrePeriodHasValidSchedule(scheduleResponse.getPrePeriodHasValidSchedule());

        LocalDate currentDate = LocalDate.now();
        LocalDate nextWeekMonday = currentDate.plusDays(8 - currentDate.getDayOfWeek().getValue());
        LocalDate lastSunday = nextWeekMonday.plusDays(27);
        scheduleResult.setLoopPeriodStartDate(nextWeekMonday);
        scheduleResult.setLoopPeriodEndDate(lastSunday);
        //是否有有效排班
        scheduleResult.setCurrentPeriodHasValidSchedule(!(scheduleResponse.getWorkScheduleId() == 0 | CollectionUtils.isEmpty(scheduleResponse.getEmployeeScheduleListList())));
        if (CollectionUtils.isEmpty(employeeByOrgListResponse.getDataList())) {
            return scheduleResult;
        }
        //排班设置
        ScheduleServerProto.ScheduleSetting scheduleSetting = scheduleServerBlockingStub.querySetting(ScheduleServerProto.QueryScheduleSettingsRequest.newBuilder().build());

        //员工排班映射
        Map<Long, ScheduleServerProto.EmployeeSchedule> employeeScheduleMap =
                scheduleResponse.getEmployeeScheduleListList().stream().collect(Collectors.toMap(ScheduleServerProto.EmployeeSchedule::getEmployeeId, v -> v));
        Set<Long> scheduledEmployeeIds = scheduleResponse.getEmployeeScheduleListList().stream().map(ScheduleServerProto.EmployeeSchedule::getEmployeeId).collect(Collectors.toSet());

        List<ScheduleResult.EmployeeScheduleResult> employeeScheduleList = new ArrayList<>();
        Map<Long, EmployeeDictionaryResult> employeeTypeMap = findEmployeeDictList(1).stream().collect(Collectors.toMap(EmployeeDictionaryResult::getId, v -> v));

        int scheduledEmployeeCount = 0;
        Long allEmployeeWorkingSeconds = 0L;
        LocalDateTime periodStartDateTime = param.getPeriodStartAt().atStartOfDay();
        LocalDateTime periodEndDateTime = param.getPeriodEndAt().plusDays(1).atStartOfDay();
        //遍历所有的员工
        for (HrmServiceProto.EmployeePageResponse employeePageResponse : employeeByOrgListResponse.getDataList()) {
            scheduledEmployeeIds.remove(employeePageResponse.getId());
            ScheduleServerProto.EmployeeSchedule employeeSchedule = employeeScheduleMap.get(employeePageResponse.getId());
            //如果有排班或者未离职
            if (employeeSchedule != null || employeePageResponse.getStatus() != 4) {
                long lastDismissDate = employeePageResponse.getLastDismissDate();
                //如果当前未离职(将来离职)且当前周期没有排班
                if (lastDismissDate != 0 && employeeSchedule == null) {
                    LocalDateTime dismissDateNextDay = LocalDateTimeUtil.of(lastDismissDate, ZoneOffset.ofHours(8)).plusDays(1).toLocalDate().atStartOfDay();
                    //到达了离职时间
                    if (param.getPeriodStartAt().atStartOfDay().isAfter(dismissDateNextDay)) {
                        continue;
                    }
                }
                ScheduleResult.EmployeeScheduleResult employeeScheduleResult = buildEmployeeScheduleResult(employeePageResponse, scheduleSetting, employeeTypeMap,
                        employeeSchedule, periodStartDateTime, periodEndDateTime, param.getOrgId());
                if (employeeScheduleResult.getTotalWorkingSeconds() > 0) {
                    scheduledEmployeeCount++;
                    allEmployeeWorkingSeconds += employeeScheduleResult.getTotalWorkingSeconds();
                }
                employeeScheduleList.add(employeeScheduleResult);
            }
        }
        //调换了部门的已排班员工
        if (CollectionUtils.isNotEmpty(scheduledEmployeeIds)) {
            HrmServiceProto.EmployeePageResponses employeePageList = hrmServiceBlockingStub.findEmployeePageList(HrmServiceProto.EmployeePageBeRequest.newBuilder().addAllId(scheduledEmployeeIds).setPaged(false).setType(1).build());
            if (CollectionUtils.isNotEmpty(employeePageList.getDataList())) {
                for (HrmServiceProto.EmployeePageResponse employeePageResponse : employeePageList.getDataList()) {
                    ScheduleResult.EmployeeScheduleResult employeeScheduleResult = buildEmployeeScheduleResult(employeePageResponse, scheduleSetting, employeeTypeMap,
                            employeeScheduleMap.get(employeePageResponse.getId()), periodStartDateTime, periodEndDateTime, param.getOrgId());
                    if (employeeScheduleResult.getTotalWorkingSeconds() > 0) {
                        scheduledEmployeeCount++;
                        allEmployeeWorkingSeconds += employeeScheduleResult.getTotalWorkingSeconds();
                    }
                    employeeScheduleList.add(employeeScheduleResult);
                }
            }
        }
        scheduleResult.setEmployeeScheduleList(employeeScheduleList);
        scheduleResult.setPublishState(scheduleResponse.getPublishState());
        scheduleResult.setLockState(scheduleResponse.getLockState());
        scheduleResult.setScheduledEmployeeCount(scheduledEmployeeCount);
        if (allEmployeeWorkingSeconds != 0) {
            scheduleResult.setAllEmployeeWorkingHours(new BigDecimal(scheduleResponse.getAllEmployeeWorkingSeconds()).divide(new BigDecimal(3600), 2, RoundingMode.HALF_UP).toString());
        }
        return scheduleResult;
    }


    /**
     * 构造每个员工的排班结果
     *
     * @param employeePageResponse 员工信息
     * @param scheduleSetting      排班预警设置
     * @param employeeTypeMap      员工类型map
     * @param employeeSchedule     员工排班信息
     * @param periodStartDateTime  排班周期开始时间零点
     * @param periodEndDateTime    排班周期结束时间第二天零点
     * @param orgId                组织id
     * @return 结果
     */
    private ScheduleResult.EmployeeScheduleResult buildEmployeeScheduleResult(HrmServiceProto.EmployeePageResponse employeePageResponse,
                                                                              ScheduleServerProto.ScheduleSetting scheduleSetting,
                                                                              Map<Long, EmployeeDictionaryResult> employeeTypeMap,
                                                                              ScheduleServerProto.EmployeeSchedule employeeSchedule,
                                                                              LocalDateTime periodStartDateTime,
                                                                              LocalDateTime periodEndDateTime,
                                                                              Long orgId) {
        long employeeWorkingSeconds = 0;

        EmployeeDictionaryResult employeeDictionaryResult = employeeTypeMap.get(employeePageResponse.getJobType());
        int employeeType = employeeDictionaryResult != null ? employeeDictionaryResult.getCode() : 0;
        ScheduleResult.EmployeeScheduleResult employeeScheduleResult = new ScheduleResult.EmployeeScheduleResult()
                .setEmployeeNo(employeePageResponse.getPersonNumber())
                .setEmployeeTypeStr(employeeDictionaryResult == null ? "" : employeeDictionaryResult.getName())
                .setEmployeeId(employeePageResponse.getId())
                //离职状态
                .setHasLeave(employeePageResponse.getStatus() == 4)
                .setEmployeeName(employeePageResponse.getName());
        //离职日期的下一天零点
        LocalDateTime dismissDateTime;
        LocalDateTime dismissDateNextDay = null;

        //离职时间
        long lastDismissDate = employeePageResponse.getLastDismissDate();
        if (lastDismissDate != 0) {
            dismissDateTime = LocalDateTimeUtil.of(lastDismissDate, ZoneOffset.ofHours(8));
            dismissDateNextDay = dismissDateTime.plusDays(1).toLocalDate().atStartOfDay();
            employeeScheduleResult.setLeaveDate(dismissDateTime.toLocalDate());
            employeeScheduleResult.setHasLeave(dismissDateTime.isBefore(periodStartDateTime) || (dismissDateTime.isAfter(periodStartDateTime) && dismissDateTime.isBefore(periodEndDateTime)));
        }

        List<ScheduleResult.EmployeeScheduleDetailResult> scheduleDetailList = new ArrayList<>();

        if (employeeSchedule != null) {
            employeeScheduleResult.setHasRead(employeeSchedule.getHasRead());
            for (ScheduleServerProto.SingleDaySchedule singleDaySchedule : employeeSchedule.getDayScheduleListList()) {
                ScheduleResult.EmployeeScheduleDetailResult employeeScheduleDetailResult = new ScheduleResult.EmployeeScheduleDetailResult()
                        .setHasScheduled(singleDaySchedule.getHasScheduled())
                        .setDayOfWeek(singleDaySchedule.getDayOfWeek());
                //如果当日有排班
                if (singleDaySchedule.getHasScheduled()) {
                    long dailyWorkingSeconds = singleDaySchedule.getEndAt().getSeconds() - singleDaySchedule.getStartAt().getSeconds();
                    employeeWorkingSeconds += dailyWorkingSeconds;
                    //根据员工类型判断当日工时是否超过限制
                    employeeScheduleDetailResult.setValid(checkScheduleIsValid(employeeType, dailyWorkingSeconds, 0, scheduleSetting));
                    employeeScheduleDetailResult.setScheduleDetailId(singleDaySchedule.getWorkScheduleDetailId());
                    LocalDateTime startTime = LocalDateTimeUtil.of(singleDaySchedule.getStartAt().getSeconds() * 1000, ZoneOffset.ofHours(8));
                    LocalDateTime endTime = LocalDateTimeUtil.of(singleDaySchedule.getEndAt().getSeconds() * 1000, ZoneOffset.ofHours(8));
                    boolean crossNight = !LocalDateTimeUtil.format(startTime, "yyyy-MM-dd").equals(LocalDateTimeUtil.format(endTime, "yyyy-MM-dd"));
                    employeeScheduleDetailResult.setWorkHours(new BigDecimal(dailyWorkingSeconds).divide(new BigDecimal(3600), 2, RoundingMode.HALF_UP).toString());
                    if (dismissDateNextDay != null) {
                        employeeScheduleDetailResult.setTodayHasLeave(startTime.isBefore(dismissDateNextDay));
                    }
                    employeeScheduleDetailResult.setCrossNight(crossNight);
                    employeeScheduleDetailResult.setStartAt(startTime);
                    employeeScheduleDetailResult.setEndAt(endTime);
                    employeeScheduleDetailResult.setDisplayTime(LocalDateTimeUtil.format(startTime, "HH:mm") + (crossNight ? "-次日" : "-") + LocalDateTimeUtil.format(endTime, "HH:mm"));
                }
                scheduleDetailList.add(employeeScheduleDetailResult);
            }
            employeeScheduleResult.setRestDays(employeeSchedule.getRestDays());
            employeeScheduleResult.setWorkingDays(employeeSchedule.getWorkingDays());
        }
        employeeScheduleResult.setScheduleDetailList(scheduleDetailList);

        if (employeeWorkingSeconds != 0) {
            float employeeScheduleWorkingHours = new BigDecimal(employeeWorkingSeconds).divide(new BigDecimal(3600), 2, RoundingMode.HALF_UP).floatValue();
            employeeScheduleResult.setTotalWorkingHours(employeeScheduleWorkingHours + "");
            employeeScheduleResult.setTotalWorkingSeconds(employeeWorkingSeconds);
        }
        employeeScheduleResult.setWeekScheduleValid(checkScheduleIsValid(employeeType, employeeWorkingSeconds, 1, scheduleSetting));
        employeeScheduleResult.setHasExchangeDept(!orgId.equals(employeePageResponse.getOrgId()));

        return employeeScheduleResult;
    }


    /**
     * 校验排班工时是否合法
     *
     * @param employeeType   员工类型 0全职 1兼职
     * @param workingSeconds 时长 秒计
     * @param type           类型 0按天 1按周
     * @return 合法true false非法
     */
    private boolean checkScheduleIsValid(int employeeType,
                                         long workingSeconds,
                                         int type,
                                         ScheduleServerProto.ScheduleSetting scheduleSetting) {

        //按天
        BigDecimal divide = new BigDecimal(workingSeconds)
                .divide(new BigDecimal(3600), 2, RoundingMode.HALF_UP);
        if (type == 0) {
            //员工类型 全职
            if (employeeType == 0) {
                return !scheduleSetting.getFullTimeDailyHoursOpenFlag() | (divide.floatValue() <= scheduleSetting.getFullTimeDailyHours());
                //兼职
            } else {
                return !scheduleSetting.getPartTimeDailyHoursOpenFlag() | (divide.floatValue() <= scheduleSetting.getPartTimeDailyHours());
            }
            //按周
        } else {
            //员工类型 全职
            if (employeeType == 0) {
                return !scheduleSetting.getFullTimeTotalWeeklyHoursOpenFlag() | (divide.floatValue() <= scheduleSetting.getFullTimeTotalWeeklyHours());
                //兼职
            } else {
                return !scheduleSetting.getPartTimeTotalWeeklyHoursOpenFlag() | (divide.floatValue() <= scheduleSetting.getPartTimeTotalWeeklyHours());
            }
        }
    }

    /**
     * 填充周期内的日期信息
     *
     * @param param          参数
     * @param scheduleResult 结果
     */
    private void fillPeriodInfo(@NonNull ScheduleQueryParam param, @NonNull ScheduleResult scheduleResult) {
        scheduleResult.setPrePeriodEndDate(param.getPeriodStartAt().minusDays(1));
        scheduleResult.setPrePeriodStartDate(param.getPeriodStartAt().minusDays(7));

        List<ScheduleResult.PerDayResult> scheduleDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            ScheduleResult.PerDayResult perDay = new ScheduleResult.PerDayResult();
            LocalDate currentDate = param.getPeriodStartAt().plusDays(i);
            perDay.setScheduleDate(currentDate);
            perDay.setPassedBy(currentDate.minusDays(1).isBefore(today));
            scheduleDates.add(perDay);
        }
        scheduleResult.setScheduleDates(scheduleDates);
    }

    /**
     * 保存排班信息
     *
     * @param param 入参
     */
    public Long save(ScheduleSaveParam param) {
        ScheduleServerProto.ScheduleSaveRequest.Builder builder = ScheduleServerProto.ScheduleSaveRequest.newBuilder();
        builder.setOrgId(param.getOrgId());
        builder.setScheduleStartTime(Timestamp.newBuilder().setSeconds(param.getPeriodStartAt().atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8))).build());
        builder.setScheduleEndTime(Timestamp.newBuilder().setSeconds(param.getPeriodEndAt().atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8))).build());
        if (param.getScheduleId() != null) {
            builder.setWorkScheduleId(param.getScheduleId());
        }
        if (CollectionUtils.isNotEmpty(param.getEmployeeScheduleList())) {
            //将排班按照 员工 日 两个维度进行分组
            Map<Long, Map<String, List<ScheduleSaveParam.EmployeeScheduleDetailParam>>> employeeDayScheduleMap = param.getEmployeeScheduleList()
                    .stream()
                    .collect(Collectors.groupingBy(ScheduleSaveParam.EmployeeScheduleDetailParam::getEmployeeId,
                            Collectors.groupingBy(k -> LocalDateTimeUtil.format(k.getStartAt(), "yyyy-MM-dd"))));

            employeeDayScheduleMap.forEach((employeeId, employeeScheduleMap) -> employeeScheduleMap.forEach((dayString, dayScheduleList) -> {
                if (dayScheduleList.size() > 1) {
                    throw new UnexpectedTypeException("一天只能有一个排班");
                }
                ScheduleSaveParam.EmployeeScheduleDetailParam employeeScheduleDetailParam = dayScheduleList.get(0);
                if (employeeScheduleDetailParam.getStartAt().compareTo(employeeScheduleDetailParam.getEndAt()) >= 0) {
                    throw new UnexpectedTypeException("排班结束时间必须大于排班开始时间");
                }
                //排班时间
                if (ChronoUnit.DAYS.between(employeeScheduleDetailParam.getStartAt(), employeeScheduleDetailParam.getEndAt()) != 0) {
                    //结束时间必须是0-7
                    if (employeeScheduleDetailParam.getEndAt().getMinute() > 0 && employeeScheduleDetailParam.getEndAt().getHour() >= 7) {
                        throw new UnexpectedTypeException("排班时间范围只能是当前0点至第二天早上七点之间");
                    }
                }
                //校验
                if (employeeScheduleDetailParam.getStartAt().isBefore(param.getPeriodStartAt().atStartOfDay())
                        || employeeScheduleDetailParam.getEndAt().isAfter(param.getPeriodEndAt().plusDays(2).atStartOfDay())) {
                    throw new UnexpectedTypeException("排班时间不在所属周期内");
                }
            }));

            List<ScheduleServerProto.SingleDaySchedule> dayScheduleList = new ArrayList<>();
            param.getEmployeeScheduleList().forEach(scheduleDetail -> {
                ScheduleServerProto.SingleDaySchedule.Builder singleDayScheduleBuilder = ScheduleServerProto.SingleDaySchedule.newBuilder()
                        .setCurrentDate(Timestamp.newBuilder().setSeconds(scheduleDetail.getStartAt().toLocalDate().atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8))).build())
                        .setStartAt(Timestamp.newBuilder().setSeconds(scheduleDetail.getStartAt().toEpochSecond(ZoneOffset.ofHours(8))).build())
                        .setEndAt(Timestamp.newBuilder().setSeconds(scheduleDetail.getEndAt().toEpochSecond(ZoneOffset.ofHours(8))).build())
                        .setEmployeeId(scheduleDetail.getEmployeeId());
                if (scheduleDetail.getScheduleDetailId() != null) {
                    singleDayScheduleBuilder.setWorkScheduleDetailId(scheduleDetail.getScheduleDetailId());
                }
                dayScheduleList.add(singleDayScheduleBuilder.build());
            });
            builder.addAllScheduleList(dayScheduleList);
        }
        return scheduleServerBlockingStub.saveSchedule(builder.build()).getScheduleId();
    }

    public JsonResponse<ScheduleSettingResult> getSetting() {
        ScheduleServerProto.ScheduleSetting scheduleSetting = scheduleServerBlockingStub.querySetting(ScheduleServerProto.QueryScheduleSettingsRequest.newBuilder()
                .setTenantId(XBB_USER_CONTEXT.get().getTenantId())
                .build());
        return JsonResponse.success(ScheduleSettingResult.convertResponse2Result(scheduleSetting));
    }

    public JsonResponse<Void> saveSetting(ScheduleSettingResult result) {
        scheduleServerBlockingStub.saveSetting(ScheduleSettingResult.convertResult2Request(result).build());
        return JsonResponse.success();
    }

    /**
     * 发布排班
     *
     * @param scheduleId 排班id
     */
    public void publish(Long scheduleId) {
        ScheduleServerProto.ScheduleResponse scheduleResponse = scheduleServerBlockingStub.querySchedule(ScheduleServerProto.ScheduleRequest.newBuilder().setWorkScheduleId(scheduleId).build());
        if (scheduleResponse.getPublishState() == 1) {
            throw new UnexpectedTypeException("已发布的排班不能再次发布");
        }
        scheduleServerBlockingStub.publish(ScheduleServerProto.ScheduleId.newBuilder().setWorkScheduleId(scheduleId).build());
        //允许失败
        try {
            Set<Long> employeeIds = new HashSet<>();
            //员工手机号
            for (ScheduleServerProto.EmployeeSchedule employeeSchedule : scheduleResponse.getEmployeeScheduleListList()) {
                employeeIds.add(employeeSchedule.getEmployeeId());
            }
            log.info("排班短信员工id,{}", employeeIds);

            if (CollectionUtils.isNotEmpty(employeeIds)) {
                HrmServiceProto.EmployeePageResponses employeePageList =
                        hrmServiceBlockingStub.findEmployeePageList(HrmServiceProto.EmployeePageBeRequest.newBuilder().setType(1).setPaged(false).addAllId(new ArrayList<>(employeeIds)).build());
                List<String> telList = employeePageList.getDataList().stream().map(HrmServiceProto.EmployeePageResponse::getMobile).distinct().collect(Collectors.toList());
                UniversalServiceProto.SendMessageBeRequest.Builder sendBuilder = UniversalServiceProto.SendMessageBeRequest.newBuilder();
                sendBuilder.setTemplateCode("T00048");
                sendBuilder.setTenantId(XBB_USER_CONTEXT.get().getTenantId());
                String period = LocalDateTimeUtil.format(LocalDateTimeUtil.of(scheduleResponse.getScheduleStartTime(), ZoneOffset.ofHours(8)), "MM.dd") +
                        "-" + LocalDateTimeUtil.format(LocalDateTimeUtil.of(scheduleResponse.getScheduleEndTime(), ZoneOffset.ofHours(8)), "MM.dd");
                sendBuilder.setContent(new JSONObject().set("period", period).toString());
                sendBuilder.addAllTels(telList);
                log.info("排班短信发送,tels = {}", telList);
                universalServiceBlockingStub.sendSmsMessage(sendBuilder.build());
            }
        } catch (Exception e) {
            log.error("短信发送失败,{}", e.getMessage());
        }
    }

    /**
     * 员工字典类型 1用工类型 2用工来源
     *
     * @param type 类型
     * @return 结果
     */
    @NonNull
    public List<EmployeeDictionaryResult> findEmployeeDictList(@NonNull Integer type) {
        ScheduleServerProto.DictionaryListResponse dictionaryList = scheduleServerBlockingStub.findDictionaryList(ScheduleServerProto.DictionaryListRequest.newBuilder()
                .setDictionaryTypeValue(type)
                .build());
        List<EmployeeDictionaryResult> results = new ArrayList<>();
        if (CollUtil.isNotEmpty(dictionaryList.getDictionaryListList())) {
            dictionaryList.getDictionaryListList().forEach(dictionaryResponse -> {
                EmployeeDictionaryResult employeeDictionaryResult = new EmployeeDictionaryResult();
                employeeDictionaryResult.setId(dictionaryResponse.getId());
                employeeDictionaryResult.setCode(dictionaryResponse.getCode());
                employeeDictionaryResult.setName(dictionaryResponse.getName());
                employeeDictionaryResult.setDescription(dictionaryResponse.getDescription());
                employeeDictionaryResult.setEditable(dictionaryResponse.getTenantId() == 0);
                results.add(employeeDictionaryResult);
            });
        }
        return results;
    }

}
