package cn.xunhou.xbbcloud.rpc.schedule.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerProto;
import cn.xunhou.xbbcloud.common.enums.EnumCommonSettingType;
import cn.xunhou.xbbcloud.common.exception.XbbCloudException;
import cn.xunhou.xbbcloud.common.utils.Conditional;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.rpc.other.dao.CommonSettingRepository;
import cn.xunhou.xbbcloud.rpc.other.entity.CommonSettingEntity;
import cn.xunhou.xbbcloud.rpc.schedule.dao.ScheduleDetailRepository;
import cn.xunhou.xbbcloud.rpc.schedule.dao.ScheduleReadRecordRepository;
import cn.xunhou.xbbcloud.rpc.schedule.dao.ScheduleRepository;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleDetailEntity;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleEntity;
import cn.xunhou.xbbcloud.rpc.schedule.pojo.result.ScheduleSettingResult;
import com.google.protobuf.Empty;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static cn.xunhou.xbbcloud.common.utils.GrpcTimestampUtil.*;

/**
 * @author litb
 * @date 2022/9/13 14:20
 * <p>
 * 排班service
 */
@Service
@Slf4j
public class ScheduleService {

    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    @Resource
    private ScheduleRepository scheduleRepository;
    @Resource
    private ScheduleDetailRepository scheduleDetailRepository;
    @Resource
    private CommonSettingRepository commonSettingRepository;

    @Resource
    private ScheduleReadRecordRepository scheduleReadRecordRepository;

    @GrpcClient("ins-xhportal-platform")
    private static HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;

    /**
     * 保存排班设置
     *
     * @param request 参数
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ScheduleServerProto.ScheduleSettingResponse saveSetting(ScheduleServerProto.ScheduleSetting request) {
        ScheduleSettingResult settingResult = new ScheduleSettingResult()
                .setFullTimeTotalWeeklyHours(request.getFullTimeTotalWeeklyHours())
                .setFullTimeDailyHours(request.getFullTimeDailyHours())
                .setWeeklyRestDays(request.getWeeklyRestDays())
                .setContinuousWorkingDays(request.getContinuousWorkingDays())
                .setPartTimeTotalWeeklyHours(request.getPartTimeTotalWeeklyHours())
                .setPartTimeDailyHours(request.getPartTimeDailyHours())
                .setFullTimeTotalWeeklyHoursOpenFlag(request.getFullTimeTotalWeeklyHoursOpenFlag())
                .setFullTimeDailyHoursOpenFlag(request.getFullTimeDailyHoursOpenFlag())
                .setWeeklyRestDaysOpenFlag(request.getWeeklyRestDaysOpenFlag())
                .setContinuousWorkingDaysOpenFlag(request.getContinuousWorkingDaysOpenFlag())
                .setPartTimeTotalWeeklyHoursOpenFlag(request.getPartTimeTotalWeeklyHoursOpenFlag())
                .setPartTimeDailyHoursOpenFlag(request.getPartTimeDailyHoursOpenFlag());
        CommonSettingEntity commonSettingEntity = (CommonSettingEntity) new CommonSettingEntity()
                .setConfigInfo(XbbJsonUtil.toJsonString(settingResult))
                .setTenantId(XBB_USER_CONTEXT.get().getTenantId());
        if (request.hasCommonSettingId()) {
            commonSettingEntity.setId(request.getCommonSettingId());
            commonSettingEntity.setModifyBy(XBB_USER_CONTEXT.get().getUserId());
            commonSettingRepository.updateById(request.getCommonSettingId(), commonSettingEntity);
            return ScheduleServerProto.ScheduleSettingResponse.newBuilder()
                    .setCommonSettingId(request.getCommonSettingId())
                    .build();
        }
        commonSettingEntity.setCreateBy(XBB_USER_CONTEXT.get().getUserId());
        commonSettingEntity.setType(EnumCommonSettingType.SCHEDULE_WORN.getCode());
        return ScheduleServerProto.ScheduleSettingResponse.newBuilder()
                .setCommonSettingId((long) commonSettingRepository.insert(commonSettingEntity))
                .build();
    }

    public static void main(String[] args) {
        // 大
        LocalDateTime l1 = LocalDateTime.now();
        // 小
        LocalDateTime l2 = l1.minusDays(1L);
        long i1 = ChronoUnit.MILLIS.between(l2, l1);
        System.out.println(i1);
    }

    /**
     * 查询排班信息
     * <p>
     * 有排班id的情况下,根据id查找,如果没有id则根据其他参数进行查找
     *
     * @param request 参数
     * @return 结果
     */
    public ScheduleServerProto.ScheduleResponse querySchedule(ScheduleServerProto.ScheduleRequest request) {
        ScheduleEntity scheduleEntity;
        if (request.hasWorkScheduleId()) {
            scheduleEntity = scheduleRepository.findOneById(request.getWorkScheduleId());
        } else {
            scheduleEntity = scheduleRepository.findOne(request.getOrgId(),
                    new Date(request.getScheduleStartTime().getSeconds() * 1000),
                    new Date(request.getScheduleEndTime().getSeconds() * 1000));
        }
        ScheduleServerProto.ScheduleResponse.Builder builder = ScheduleServerProto.ScheduleResponse.newBuilder();
        if (scheduleEntity != null) {
            builder.setScheduleStartTime(scheduleEntity.getStartDate().getTime());
            builder.setScheduleEndTime(scheduleEntity.getEndDate().getTime());
            builder.setWorkScheduleId(scheduleEntity.getId());
            builder.setPublishState(scheduleEntity.getPublishState()).setLockState(scheduleEntity.getLockState());
            //查询排班详情
            List<ScheduleDetailEntity> workScheduleDetailEntityList = scheduleDetailRepository.findList(request.getOrgId(), Collections.singletonList(scheduleEntity.getId()), request.getEmployeeIdsList(), null, null, null, null);

            if (CollectionUtils.isNotEmpty(workScheduleDetailEntityList)) {
                //统计数据
                Map<Long, List<ScheduleDetailEntity>> employeeScheduleMap = workScheduleDetailEntityList
                        .stream()
                        .collect(Collectors.groupingBy(ScheduleDetailEntity::getEmployeeId));
                builder.setScheduledEmployeeCount(employeeScheduleMap.size());
                LongAdder allEmployeeWorkingSeconds = new LongAdder();

                List<ScheduleServerProto.EmployeeSchedule> employeeScheduleList = new ArrayList<>();
                employeeScheduleMap.forEach((employeeId, workScheduleDetailEntities) -> {
                    ScheduleServerProto.EmployeeSchedule.Builder employeeScheduleBuilder = ScheduleServerProto.EmployeeSchedule.newBuilder();
                    employeeScheduleBuilder.setEmployeeId(employeeId);
                    Map<Integer, List<ScheduleDetailEntity>> employeeScheduleDailyMap = workScheduleDetailEntities.stream().collect(Collectors.groupingBy(ScheduleDetailEntity::getDayOfWeek));
                    int resetDays = 7;
                    int workingDays = 0;
                    long totalWorkingSeconds = 0;
                    employeeScheduleBuilder.setHasRead(CollectionUtils.isNotEmpty(scheduleReadRecordRepository.findList(Collections.singletonList(scheduleEntity.getId()), employeeId)));
                    //周一至周日
                    for (int i = 1; i <= 7; i++) {
                        //目前一周只有一天排班,可以这么处理
                        ScheduleDetailEntity workScheduleDetailEntity = CollUtil.getFirst(employeeScheduleDailyMap.get(i));
                        //没有排班
                        if (workScheduleDetailEntity == null) {
                            employeeScheduleBuilder.addDayScheduleList(ScheduleServerProto.SingleDaySchedule.newBuilder()
                                    .setEmployeeId(employeeId)
                                    .setEndAt(com.google.protobuf.Timestamp.newBuilder().setSeconds(0L).build())
                                    .setStartAt(com.google.protobuf.Timestamp.newBuilder().setSeconds(0L).build())
                                    .setDayOfWeek(i)
                                    .setWorkScheduleDetailId(0L)
                                    .setCurrentDate(atStartOfDay(new Timestamp(request.getScheduleStartTime().getSeconds() * 1000)))
                                    .setHasScheduled(false)
                                    .build());
                        } else {
                            //目前一天只有一个排班,可以这么判断
                            resetDays--;
                            workingDays++;
                            long intervalSeconds = DateUtil.between(workScheduleDetailEntity.getEndDatetime(), workScheduleDetailEntity.getStartDatetime(), DateUnit.SECOND);
                            totalWorkingSeconds += intervalSeconds;
                            allEmployeeWorkingSeconds.add(intervalSeconds);
                            employeeScheduleBuilder.addDayScheduleList(ScheduleServerProto.SingleDaySchedule.newBuilder()
                                    .setEmployeeId(employeeId)
                                    .setEndAt(fromJavaUtilDate(workScheduleDetailEntity.getEndDatetime()))
                                    .setStartAt(fromJavaUtilDate(workScheduleDetailEntity.getStartDatetime()))
                                    .setDayOfWeek(workScheduleDetailEntity.getDayOfWeek())
                                    .setWorkScheduleDetailId(workScheduleDetailEntity.getId())
                                    .setCurrentDate(atStartOfDay(workScheduleDetailEntity.getStartDatetime()))
                                    .setHasScheduled(true)
                                    .build());
                        }
                        builder.setCurrentPeriodHasValidSchedule(true);
                    }
                    employeeScheduleList.add(employeeScheduleBuilder.setRestDays(resetDays).setWorkingDays(workingDays).setTotalWorkingSeconds(totalWorkingSeconds).build());
                });
                builder.setAllEmployeeWorkingSeconds(allEmployeeWorkingSeconds.longValue())
                        .addAllEmployeeScheduleList(employeeScheduleList);
            }
        }

        //上一周的排班id
        ScheduleEntity prePeriodSchedule = scheduleRepository.findOne(request.getOrgId(),
                subtractToSqlDate(request.getScheduleStartTime(), 7),
                subtractToSqlDate(request.getScheduleStartTime(), 1));
        builder.setPreWorkScheduleId(Opt.ofNullable(prePeriodSchedule)
                .orElse((ScheduleEntity) new ScheduleEntity().setId(0L)).getId());
        if (prePeriodSchedule != null) {
            builder.setPrePeriodHasValidSchedule(CollectionUtils.isNotEmpty(scheduleDetailRepository.findList(request.getOrgId(),
                    Collections.singletonList(prePeriodSchedule.getId()), null, null, null, null, null)));
        }

        return builder.build();
    }

    public ScheduleServerProto.ScheduleSetting querySetting(ScheduleServerProto.QueryScheduleSettingsRequest request) {
        Integer tenantId = null;
        if (request.hasTenantId()) {
            tenantId = request.getTenantId();
        } else {
            tenantId = XBB_USER_CONTEXT.get().getTenantId();
        }
        if (tenantId == null) {
            throw new XbbCloudException("缺少租户参数");
        }
        ScheduleSettingResult scheduleSetting = commonSettingRepository.findScheduleSetting(tenantId);
        if (scheduleSetting == null) {
            return ScheduleServerProto.ScheduleSetting.newBuilder().build();
        }
        return ScheduleSettingResult.convert2Response(scheduleSetting);
    }

    /**
     * 保存排班
     *
     * @param request 参数
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ScheduleServerProto.ScheduleSaveResponse saveSchedule(ScheduleServerProto.ScheduleSaveRequest request) {

        Long workScheduleId;

        List<ScheduleDetailEntity> addedScheduleDetailEntityList = new ArrayList<>();

        List<ScheduleDetailEntity> updatedScheduleDetailEntityList = new ArrayList<>();

        ScheduleEntity exist;

        if (!request.hasWorkScheduleId()) {
            //再查找一下
            exist = scheduleRepository.findOne(request.getOrgId(), new Date(request.getScheduleStartTime().getSeconds() * 1000), new Date(request.getScheduleEndTime().getSeconds() * 1000));

            if (exist == null) {
                //新增
                workScheduleId = (Long) scheduleRepository.insert((ScheduleEntity) new ScheduleEntity()
                        .setStartDate(toSqlDate(request.getScheduleStartTime()))
                        .setEndDate(toSqlDate(request.getScheduleEndTime()))
                        .setOrgId(request.getOrgId())
                        .setCreateBy(XBB_USER_CONTEXT.get().getUserId())
                        .setModifyBy(XBB_USER_CONTEXT.get().getUserId())
                        .setTenantId(XBB_USER_CONTEXT.get().getTenantId()));
            } else {
                workScheduleId = exist.getId();
            }
        } else {
            exist = scheduleRepository.findOneById(request.getWorkScheduleId());
            IAssert.notNull(exist, "未找到指定排班数据");
            workScheduleId = exist.getId();
            IAssert.state(exist.getLockState() == 0, "锁定状态的排班无法编辑");
        }

        Set<Long> existScheduleDetailIds = new HashSet<>();

        //更新
        if (exist != null) {
            IAssert.state(exist.getOrgId().equals(request.getOrgId()), "编辑排班不允许变更部门");
            ScheduleEntity updateScheduleEntity = new ScheduleEntity();
            updateScheduleEntity.setId(exist.getId());
            //updateScheduleEntity.setOrgId(request.getOrgId());
            updateScheduleEntity.setStartDate(toSqlDate(request.getScheduleStartTime()));
            updateScheduleEntity.setEndDate(toSqlDate(request.getScheduleEndTime()));
            updateScheduleEntity.setModifyBy(XBB_USER_CONTEXT.get().getUserId());
            //如果排班已发布且当前保存的不是空排班,则将其置为锁定状态
            if (exist.getPublishState() == 1 && CollectionUtils.isNotEmpty(request.getScheduleListList())) {
                updateScheduleEntity.setLockState(1);
            }
            scheduleRepository.updateById(request.getWorkScheduleId(), updateScheduleEntity);

            //已有排班
            List<ScheduleDetailEntity> existedScheduleDetailList = scheduleDetailRepository.findList(request.getOrgId(), Collections.singletonList(exist.getId()), null, null, null, null, null);
            existScheduleDetailIds = existedScheduleDetailList.stream().map(ScheduleDetailEntity::getId).collect(Collectors.toSet());

        }

        for (ScheduleServerProto.SingleDaySchedule singleDaySchedule : request.getScheduleListList()) {
            if (!singleDaySchedule.hasWorkScheduleDetailId()) {
                //TODO:时间校验
                addedScheduleDetailEntityList.add((ScheduleDetailEntity) new ScheduleDetailEntity()
                        .setWorkScheduleId(workScheduleId)
                        .setOrgId(request.getOrgId())
                        .setStartDatetime(toSqlTimestamp(singleDaySchedule.getStartAt()))
                        .setEndDatetime(toSqlTimestamp(singleDaySchedule.getEndAt()))
                        .setDayOfWeek(LocalDateTimeUtil.of(singleDaySchedule.getStartAt().getSeconds() * 1000, ZoneOffset.ofHours(8)).getDayOfWeek().getValue())
                        .setEmployeeId(singleDaySchedule.getEmployeeId())
                        .setCreateBy(XBB_USER_CONTEXT.get().getUserId())
                        .setModifyBy(XBB_USER_CONTEXT.get().getUserId())
                        .setTenantId(XBB_USER_CONTEXT.get().getTenantId()));
            } else {
                if (CollectionUtils.isNotEmpty(existScheduleDetailIds)) {
                    IAssert.state(existScheduleDetailIds.contains(singleDaySchedule.getWorkScheduleDetailId()), "不能编辑不属于当前排班周期内的排班详情");
                }

                updatedScheduleDetailEntityList.add((ScheduleDetailEntity) new ScheduleDetailEntity()
                        .setEndDatetime(toSqlTimestamp(singleDaySchedule.getEndAt()))
                        .setModifyBy(XBB_USER_CONTEXT.get().getUserId())
                        .setStartDatetime(toSqlTimestamp(singleDaySchedule.getStartAt()))
                        .setId(singleDaySchedule.getWorkScheduleDetailId()));
            }
        }

        Conditional.run(CollectionUtils.isNotEmpty(addedScheduleDetailEntityList), () -> scheduleDetailRepository.batchInsert(addedScheduleDetailEntityList));

        Conditional.run(CollectionUtils.isNotEmpty(updatedScheduleDetailEntityList), () -> updatedScheduleDetailEntityList.forEach(updatedScheduleEntity -> scheduleDetailRepository.updateById(updatedScheduleEntity.getId(), updatedScheduleEntity)));

        existScheduleDetailIds.removeAll(updatedScheduleDetailEntityList.stream().map(ScheduleDetailEntity::getId).collect(Collectors.toSet()));
        Set<Long> finalExistScheduleDetailIds = existScheduleDetailIds;
        Conditional.run(CollectionUtils.isNotEmpty(existScheduleDetailIds), () -> scheduleDetailRepository.removeByIds(finalExistScheduleDetailIds));

        return ScheduleServerProto.ScheduleSaveResponse.newBuilder().setScheduleId(workScheduleId).build();
    }

    /**
     * 循环排班,从下一周开始,循环排班四周
     *
     * @param request 参数
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Empty loop(ScheduleServerProto.LoopScheduleRequest request) {
        // 下一周周一
        LocalDate currentDate = LocalDate.now();
        LocalDate nextWeekMonday = currentDate.plusDays(8 - currentDate.getDayOfWeek().getValue());
        LocalDate nextWeekSunday = nextWeekMonday.plusDays(6L);
        ScheduleEntity scheduleEntity = scheduleRepository.findOneById(request.getWorkScheduleId());
        IAssert.notNull(scheduleEntity, "未找到需要复制的排班数据");
        //查询部门下的所有员工
        HrmServiceProto.EmployeePageResponses employeeByOrgListResponse =
                hrmServiceBlockingStub.findEmployeeByOrgList(HrmServiceProto.FindEmployeeByOrgRequest.newBuilder()
                        .setId(scheduleEntity.getOrgId())
                        .setIsLeave(1)
                        .build());
        if (CollectionUtils.isEmpty(employeeByOrgListResponse.getDataList())) {
            log.info("当前部门下无在职员工,循环排班中断,orgId = {}", scheduleEntity.getOrgId());
        }

        List<ScheduleDetailEntity> list = scheduleDetailRepository.findList(request.getOrgId(), Collections.singletonList(request.getWorkScheduleId()), null, null, null, null, null);
        IAssert.notEmpty(list, "当前排班为空,不可循环排班");
        //员工映射
        Map<Long, HrmServiceProto.EmployeePageResponse> employeePageResponseMap = employeeByOrgListResponse.getDataList()
                .stream()
                .collect(Collectors.toMap(HrmServiceProto.EmployeePageResponse::getId, v -> v));
        Map<Long, List<ScheduleDetailEntity>> copiedSchedule = list.stream().collect(Collectors.groupingBy(ScheduleDetailEntity::getEmployeeId));
        int copiedCount = 0;
        // 循环四周
        while (copiedCount < 4) {
            copyByWeek(copiedSchedule, nextWeekMonday, nextWeekSunday, request.getOrgId(), request.getCoverWhenExist(), employeePageResponseMap, scheduleEntity.getId());
            nextWeekMonday = nextWeekSunday.plusDays(1);
            nextWeekSunday = nextWeekMonday.plusDays(6);
            copiedCount++;
        }
        return Empty.newBuilder().build();
    }

    /**
     * 按周复制排班
     *
     * @param copiedSchedule          已有排班,按照员工id分组
     * @param weekMonday              星期一所在日期
     * @param weekSunday              星期日所在日期
     * @param orgId                   组织id
     * @param coverWhenExist          是否覆盖已有排班
     * @param employeePageResponseMap 在职员工map
     */
    private void copyByWeek(Map<Long, List<ScheduleDetailEntity>> copiedSchedule,
                            LocalDate weekMonday,
                            LocalDate weekSunday,
                            Long orgId,
                            boolean coverWhenExist,
                            Map<Long, HrmServiceProto.EmployeePageResponse> employeePageResponseMap,
                            Long copiedScheduleId) {

        // 查询此区间内是否有排班
        ScheduleEntity existingSchedule = scheduleRepository.findOne(orgId, new Date(weekMonday.atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8)) * 1000),
                new Date(weekSunday.atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8)) * 1000));
        // 是否已有排班
        if (existingSchedule != null) {
            if (existingSchedule.getId().equals(copiedScheduleId)) {
                return;
            }
            // 覆盖未发布
            if (coverWhenExist && existingSchedule.getLockState() == 0) {
                // 删除原有排班
                List<ScheduleDetailEntity> existingScheduleDetailList = scheduleDetailRepository.findList(null, Collections.singletonList(existingSchedule.getId()), null, null, null, null, null);
                Conditional.run(CollectionUtils.isNotEmpty(existingScheduleDetailList), () -> scheduleDetailRepository.removeByIds(existingScheduleDetailList.stream().map(ScheduleDetailEntity::getId).collect(Collectors.toSet())));
                insertScheduleForLoop(existingSchedule.getId(), copiedSchedule, weekMonday.atStartOfDay(), weekSunday.atStartOfDay(), orgId, employeePageResponseMap, copiedScheduleId);
            }
        } else {
            // 新增
            insertScheduleForLoop(null, copiedSchedule, weekMonday.atStartOfDay(), weekSunday.atStartOfDay(), orgId, employeePageResponseMap, copiedScheduleId);
        }
    }

    /**
     * 循环排班插入每周排班信息
     *
     * @param scheduleId     排班id,如果为null则先插入排班记录然后再插入对应的排班详情
     * @param copiedSchedule 复制的排班详情
     * @param startTime      排班开始日期
     * @param endTime        排班结束日期
     */
    private void insertScheduleForLoop(@Nullable Long scheduleId,
                                       @NonNull Map<Long, List<ScheduleDetailEntity>> copiedSchedule,
                                       @NonNull LocalDateTime startTime,
                                       @NonNull LocalDateTime endTime,
                                       @NonNull Long orgId,
                                       @NonNull Map<Long, HrmServiceProto.EmployeePageResponse> employeePageResponseMap,
                                       Long copiedScheduleId) {
        if (scheduleId == null) {
            scheduleId = (Long) scheduleRepository.insert((ScheduleEntity) new ScheduleEntity()
                    .setStartDate(new Date(startTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli()))
                    .setEndDate(new Date(endTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli()))
                    .setOrgId(orgId)
                    .setTenantId(XBB_USER_CONTEXT.get().getTenantId()));
        }
        // 跳过自身
        if (copiedScheduleId.equals(scheduleId)) {
            return;
        }
        List<ScheduleDetailEntity> insertScheduleDetailEntityList = new ArrayList<>();
        Long finalScheduleId = scheduleId;
        copiedSchedule.forEach((employeeId, scheduleDetailEntities) -> scheduleDetailEntities.forEach(scheduleDetailEntity -> {
            HrmServiceProto.EmployeePageResponse employeePageResponse = employeePageResponseMap.get(employeeId);
            //员工未离职
            if (employeePageResponse != null) {
                // 离职日期
                LocalDateTime dismissDate = null;
                if (employeePageResponse.getLastDismissDate() != 0) {
                    // 离职日期后一天的零点
                    dismissDate = LocalDateTimeUtil.of(employeePageResponse.getLastDismissDate(), ZoneOffset.ofHours(8)).toLocalDate().plusDays(1).atStartOfDay();
                }
                LocalDateTime scheduleStartTime = LocalDateTimeUtil.of(scheduleDetailEntity.getStartDatetime().getTime(), ZoneOffset.ofHours(8));
                // 周一
                LocalDateTime weekMonday = scheduleStartTime.minusDays(scheduleStartTime.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
                long interval = ChronoUnit.MILLIS.between(weekMonday, startTime);
                // 判断员工是否仍在部门内,如果不在则不需要排班
                LocalDateTime startDateTime = LocalDateTimeUtil.of(scheduleDetailEntity.getStartDatetime().getTime() + interval, ZoneOffset.ofHours(8));
                if (dismissDate == null || startDateTime.isBefore(dismissDate)) {
                    insertScheduleDetailEntityList.add((ScheduleDetailEntity) new ScheduleDetailEntity()
                            .setStartDatetime(new Timestamp(scheduleDetailEntity.getStartDatetime().getTime() + interval))
                            .setEndDatetime(new Timestamp(scheduleDetailEntity.getEndDatetime().getTime() + interval))
                            .setWorkScheduleId(finalScheduleId)
                            .setEmployeeId(employeeId)
                            .setOrgId(orgId)
                            .setDayOfWeek(scheduleDetailEntity.getDayOfWeek())
                            .setCreateBy(XBB_USER_CONTEXT.get().getUserId())
                            .setModifyBy(XBB_USER_CONTEXT.get().getUserId())
                            .setTenantId(XBB_USER_CONTEXT.get().getTenantId()));
                }
            }
        }));
        Conditional.run(CollectionUtils.isNotEmpty(insertScheduleDetailEntityList), () -> scheduleDetailRepository.batchInsert(insertScheduleDetailEntityList));
    }
}
