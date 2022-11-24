package cn.xunhou.web.xbbcloud.product.schedule.controller;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.cloud.redis.lock.IRedisLockService;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerGrpc;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerProto;
import cn.xunhou.web.xbbcloud.product.schedule.param.ScheduleLoopParam;
import cn.xunhou.web.xbbcloud.product.schedule.param.ScheduleQueryParam;
import cn.xunhou.web.xbbcloud.product.schedule.param.ScheduleSaveParam;
import cn.xunhou.web.xbbcloud.product.schedule.result.ScheduleDateResult;
import cn.xunhou.web.xbbcloud.product.schedule.result.ScheduleResult;
import cn.xunhou.web.xbbcloud.product.schedule.result.ScheduleSettingResult;
import cn.xunhou.web.xbbcloud.product.schedule.service.ScheduleService;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.UnexpectedTypeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 排班云
 *
 * @author litb
 * @date 2022/9/15 16:43
 * <p>
 */
@RestController
@RequestMapping("/schedule")
@SuppressWarnings("ResultOfMethodCallIgnored")
public class ScheduleController {

    @GrpcClient("ins-xbbcloud-platform")
    private ScheduleServerGrpc.ScheduleServerBlockingStub scheduleServerBlockingStub;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private IRedisLockService redisLockService;

    /**
     * 排班详情
     *
     * @param param 参数
     * @return 结果
     */
    @PostMapping("/detail")
    public JsonResponse<ScheduleResult> detail(@Validated @RequestBody ScheduleQueryParam param) {
        checkPeriod(param.getPeriodStartAt(), param.getPeriodEndAt());
        return JsonResponse.success(scheduleService.detail(param));
    }

    /**
     * 保存排班信息
     *
     * @param param 入参
     * @return 结果
     */
    @PostMapping
    public JsonResponse<Long> save(@Validated @RequestBody ScheduleSaveParam param) {
        if (param.getPeriodEndAt().atStartOfDay().isBefore(LocalDateTime.now().toLocalDate().atStartOfDay())) {
            throw new UnexpectedTypeException("历史排班不可新增、编辑");
        }
        String lockKey = "Schedule_Lock_Key_" + param.getOrgId();
        try {
            redisLockService.tryLock(lockKey, TimeUnit.SECONDS, 3, 3);
            checkPeriod(param.getPeriodStartAt(), param.getPeriodEndAt());
            return JsonResponse.success(scheduleService.save(param));
        } finally {
            redisLockService.unlock(lockKey);
        }
    }

    private void checkPeriod(@NonNull LocalDate start, @NonNull LocalDate end) {
        DayOfWeek startDayOfWeek = start.getDayOfWeek();
        DayOfWeek endDayOfWeek = end.getDayOfWeek();

        if (!(DayOfWeek.MONDAY.equals(startDayOfWeek) & DayOfWeek.SUNDAY.equals(endDayOfWeek))) {
            throw new SystemRuntimeException("日期范围只能是周一开始周日结束");
        }

        if (ChronoUnit.DAYS.between(start, end) != 6) {
            throw new SystemRuntimeException("日期范围必须是同一周");
        }
    }

    /**
     * 发布排班
     *
     * @param scheduleId 排班id
     * @return 结果
     */
    @PostMapping("/{schedule_id}/publish")
    public JsonResponse<Void> publish(@PathVariable(value = "schedule_id") Long scheduleId) {
        scheduleService.publish(scheduleId);
        return JsonResponse.success();
    }

    /**
     * 解锁排班
     *
     * @param scheduleId 排班id
     * @return 结果
     */
    @PostMapping("/{schedule_id}/unlock")
    public JsonResponse<Void> unlock(@PathVariable(value = "schedule_id") Long scheduleId) {
        scheduleServerBlockingStub.unlock(ScheduleServerProto.ScheduleId.newBuilder().setWorkScheduleId(scheduleId).build());
        return JsonResponse.success();
    }

    /**
     * 锁定排班
     *
     * @param scheduleId 排班id
     * @return 结果
     */
    @PostMapping("/{schedule_id}/lock")
    public JsonResponse<Void> lock(@PathVariable(value = "schedule_id") Long scheduleId) {
        scheduleServerBlockingStub.lock(ScheduleServerProto.ScheduleId.newBuilder().setWorkScheduleId(scheduleId).build());
        return JsonResponse.success();
    }

    /**
     * 循环排班
     *
     * @param param 参数
     * @return 结果
     */
    @PostMapping("/loop")
    public JsonResponse<Void> loop(@Validated @RequestBody ScheduleLoopParam param) {
        scheduleServerBlockingStub.loop(ScheduleServerProto.LoopScheduleRequest.newBuilder()
                .setWorkScheduleId(param.getScheduleId())
                .setOrgId(param.getOrgId())
                .setCoverWhenExist(Opt.ofNullable(param.getCover()).orElse(false))
                .build());
        return JsonResponse.success();
    }

    /**
     * 从服务端获取周期范围
     *
     * @param type 类型 0向前翻日期 1向后翻日期 可不传,不传则查询当前日期所在周期
     * @param date 日期,如果是向前翻则需要传当前周期的开始时间,如果是向后翻则需要传当前周期的结束时间
     * @return 周期范围
     */
    @GetMapping("/date_range")
    public JsonResponse<ScheduleDateResult> dateList(@RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                                     @RequestParam(value = "type", required = false) Integer type) {
        if (type != null && date == null) {
            throw new UnexpectedTypeException("日期必填");
        }

        ScheduleDateResult result = new ScheduleDateResult();
        List<String> dateList = new ArrayList<>();
        LocalDate start;
        LocalDate end;
        //当前周期
        if (type == null) {
            int dayOfWeekOffset = LocalDate.now().getDayOfWeek().getValue() - 1;
            start = LocalDate.now().minusDays(dayOfWeekOffset);
            end = start.plusDays(6);

            result.setScheduleStartAt(start);
            result.setScheduleEndAt(end);

            //指定周期
        } else {

            //向前翻
            int dayOfWeek = date.getDayOfWeek().getValue();
            if (type == 0) {
                if (dayOfWeek != 1) {
                    throw new UnexpectedTypeException("向前翻起始时间必须是周一");
                }
                start = date.minusDays(7);
                end = date.minusDays(1);
                result.setScheduleStartAt(start);
                result.setScheduleEndAt(end);
                //向后翻
            } else {
                if (dayOfWeek != 7) {
                    throw new UnexpectedTypeException("向后翻起始时间必须是周日");
                }
                start = date.plusDays(1);
                end = date.plusDays(7);
                result.setScheduleStartAt(start);
                result.setScheduleEndAt(end);
            }
        }
        for (; start.compareTo(end) <= 0; start = start.plusDays(1)) {
            dateList.add(LocalDateTimeUtil.format(start, "MM.dd"));
        }
        result.setScheduleDateList(dateList);
        return JsonResponse.success(result);
    }

    /**
     * 查询排班设置
     *
     * @return
     */
    @GetMapping("/setting")
    public JsonResponse<ScheduleSettingResult> getSetting() {
        return scheduleService.getSetting();
    }

    /**
     * 保存排班设置
     *
     * @param result
     * @return
     */
    @PostMapping("/setting")
    public JsonResponse<Void> saveSetting(@RequestBody ScheduleSettingResult result) {
        return scheduleService.saveSetting(result);
    }


}
