package cn.xunhou.xbbcloud.rpc.schedule.server;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.AbstractScheduleServerImplBase;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerProto;
import cn.xunhou.xbbcloud.common.utils.Conditional;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.rpc.other.service.DictionaryService;
import cn.xunhou.xbbcloud.rpc.schedule.dao.ScheduleDetailRepository;
import cn.xunhou.xbbcloud.rpc.schedule.dao.ScheduleReadRecordRepository;
import cn.xunhou.xbbcloud.rpc.schedule.dao.ScheduleRepository;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleDetailEntity;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleEntity;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleReadRecordEntity;
import cn.xunhou.xbbcloud.rpc.schedule.service.ScheduleService;
import com.google.protobuf.Empty;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author litb
 * @date 2022/9/8 13:38
 * <p>
 * 排班管理对外暴露接口
 */
@GrpcService
@Slf4j
public class ScheduleServer extends AbstractScheduleServerImplBase {

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private ScheduleRepository scheduleRepository;

    @Resource
    private DictionaryService dictionaryService;

    @Resource
    private ScheduleDetailRepository scheduleDetailRepository;

    @Resource
    private ScheduleReadRecordRepository scheduleReadRecordRepository;

    @GrpcClient("ins-xhportal-platform")
    private static HrmServiceGrpc.HrmServiceBlockingStub saasServiceBlockingStub;

    @Override
    protected ScheduleServerProto.ScheduleSetting querySetting(ScheduleServerProto.QueryScheduleSettingsRequest request) {
        return scheduleService.querySetting(request);
    }

    @Override
    protected ScheduleServerProto.ScheduleSettingResponse saveSetting(ScheduleServerProto.ScheduleSetting request) {
        return scheduleService.saveSetting(request);
    }

    @Override
    protected ScheduleServerProto.ScheduleResponse querySchedule(ScheduleServerProto.ScheduleRequest request) {
        return scheduleService.querySchedule(request);
    }

    @Override
    protected Empty publish(ScheduleServerProto.ScheduleId request) {
        ScheduleEntity scheduleEntity = scheduleRepository.findOneById(request.getWorkScheduleId());
        //noinspection ConstantConditions
        IAssert.state(scheduleEntity.getPublishState() == 0, "当前排班已经发布,请勿重复发布");
        scheduleRepository.updateById(request.getWorkScheduleId(), new ScheduleEntity().setPublishState(1).setLockState(1));
        return Empty.newBuilder().build();
    }

    @Override
    protected ScheduleServerProto.ScheduleId copyPreviousSchedule(ScheduleServerProto.CopyPreviousScheduleRequest request) {
        return null;
    }

    @Override
    protected Empty unlock(ScheduleServerProto.ScheduleId request) {
        ScheduleEntity scheduleEntity = scheduleRepository.findOneById(request.getWorkScheduleId());
        //noinspection ConstantConditions
        IAssert.state(scheduleEntity.getLockState() == 1, "当前排班已经解锁,请勿重复解锁");
        scheduleRepository.updateById(request.getWorkScheduleId(), new ScheduleEntity().setLockState(0));
        return Empty.newBuilder().build();
    }

    @Override
    protected Empty lock(ScheduleServerProto.ScheduleId request) {
        ScheduleEntity scheduleEntity = scheduleRepository.findOneById(request.getWorkScheduleId());
        IAssert.notNull(scheduleEntity, "未找到排班数据");
        IAssert.state(scheduleEntity.getPublishState() == 1, "未发布的排班不能锁定");
        IAssert.state(scheduleEntity.getLockState() == 0, "排班已锁定无需再次锁定");
        scheduleRepository.updateById(request.getWorkScheduleId(), new ScheduleEntity().setLockState(1));
        return Empty.newBuilder().build();
    }

    @Override
    protected Empty loop(ScheduleServerProto.LoopScheduleRequest request) {
        return scheduleService.loop(request);
    }

    @Override
    protected ScheduleServerProto.ScheduleSaveResponse saveSchedule(ScheduleServerProto.ScheduleSaveRequest request) {
        return scheduleService.saveSchedule(request);
    }

    @Override
    protected ScheduleServerProto.DictionarySaveResponse saveDictionary(ScheduleServerProto.DictionarySaveRequest request) {
        return dictionaryService.saveDictionary(request);
    }

    @Override
    protected ScheduleServerProto.DictionaryListResponse findDictionaryList(ScheduleServerProto.DictionaryListRequest request) {
        return dictionaryService.findDictionaryList(request);
    }

    @Override
    protected ScheduleServerProto.DictionaryResponse findDict(ScheduleServerProto.DictionaryRequest request) {
        return dictionaryService.findOne(request);
    }

    @Override
    protected ScheduleServerProto.ScheduleDetailListResponse findScheduleDetailList(ScheduleServerProto.ScheduleDetailListQueryRequest request) {
        List<ScheduleDetailEntity> scheduleDetailEntityList = scheduleDetailRepository.findList(null, null,
                Collections.singletonList(request.getEmployeeId()),
                new Date(request.getStartTime()), new Date(request.getEndTime()), null, Collections.singletonList(request.getTenantId()));
        HrmServiceProto.EmployeePageResponse employeePageResponse = CollUtil.getFirst(saasServiceBlockingStub.findEmployeePageList(HrmServiceProto.EmployeePageBeRequest.newBuilder()
                .addAllId(Collections.singletonList(request.getEmployeeId()))
                .setPaged(false)
                .setType(1)
                .build()).getDataList());

        Set<Long> allScheduleIds = scheduleDetailEntityList.stream().map(ScheduleDetailEntity::getWorkScheduleId).collect(Collectors.toSet());
        Set<Long> publishedScheduleIds = new HashSet<>();
        List<ScheduleEntity> scheduleEntityList = new ArrayList<>();
        if (CollUtil.isNotEmpty(allScheduleIds)) {
            scheduleEntityList = scheduleRepository.findByIds(allScheduleIds);
            publishedScheduleIds = scheduleEntityList.stream()
                    .filter(scheduleEntity -> scheduleEntity.getPublishState() == 1)
                    .map(XbbSnowTimeTenantEntity::getId)
                    .collect(Collectors.toSet());
        }
        Map<Long, ScheduleEntity> scheduleEntityMap = scheduleEntityList.stream().collect(Collectors.toMap(ScheduleEntity::getId, v -> v));

        ScheduleServerProto.ScheduleDetailListResponse.Builder builder = ScheduleServerProto.ScheduleDetailListResponse.newBuilder();

        Map<String, List<ScheduleDetailEntity>> scheduleDetailDateMap = scheduleDetailEntityList.stream().collect(Collectors.groupingBy(entity -> LocalDateTimeUtil.format(entity.getStartDatetime().toLocalDateTime(), "yyyy-MM-dd"), Collectors.toList()));

        LocalDate startDate = LocalDateTimeUtil.of(request.getStartTime()).toLocalDate();
        LocalDate endDate = LocalDateTimeUtil.of(request.getEndTime()).toLocalDate();

        Set<Long> currentPeriodScheduleIds = new HashSet<>();

        List<ScheduleServerProto.ScheduleDetailResponse> scheduleDetailResponseList = new ArrayList<>();

        //当前时间零点
        LocalDateTime startDayOfNow = LocalDate.now(ZoneId.of("Asia/Shanghai")).atStartOfDay();

        for (; startDate.compareTo(endDate) <= 0; startDate = startDate.plusDays(1)) {
            String currentDateStr = LocalDateTimeUtil.format(startDate, "yyyy-MM-dd");
            ScheduleServerProto.ScheduleDetailResponse.Builder detailBuilder = ScheduleServerProto.ScheduleDetailResponse.newBuilder();
            detailBuilder.setEmployeeId(request.getEmployeeId());
            detailBuilder.setDate(startDate.atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8)) * 1000);
            detailBuilder.setTenantId(request.getTenantId());

            List<ScheduleDetailEntity> currentDayScheduleList = scheduleDetailDateMap.get(currentDateStr);

            //没有排班
            if (CollectionUtils.isEmpty(currentDayScheduleList) || CollectionUtils.isEmpty(publishedScheduleIds)) {
                detailBuilder.setHasScheduled(false);
                //有排班
            } else {
                //今日及将来的排班只按照当前所在部门展示
                if (startDate.atStartOfDay().plusSeconds(1L).isAfter(startDayOfNow)) {
                    Set<Long> finalPublishedScheduleIds = publishedScheduleIds;
                    List<ScheduleDetailEntity> matchedScheduleDetailList = currentDayScheduleList.stream()
                            .filter(scheduleDetailEntity ->
                                    finalPublishedScheduleIds.contains(scheduleDetailEntity.getWorkScheduleId()) &&
                                            scheduleDetailEntity.getOrgId().equals(employeePageResponse.getOrgId())
                            ).collect(Collectors.toList());
                    if (CollUtil.isEmpty(matchedScheduleDetailList)) {
                        detailBuilder.setHasScheduled(false);
                    } else {
                        ScheduleDetailEntity currentDateSchedule = CollUtil.getFirst(matchedScheduleDetailList);
                        detailBuilder.setHasScheduled(true)
                                .setStartDateTime(currentDateSchedule.getStartDatetime().getTime())
                                .setEndDateTime(currentDateSchedule.getEndDatetime().getTime())
                                .setScheduleDetailId(currentDateSchedule.getId())
                                .setScheduleId(currentDateSchedule.getWorkScheduleId());
                    }
                } else {
                    //历史排班优先按照当前部门展示
                    Set<Long> finalPublishedScheduleIds1 = publishedScheduleIds;
                    List<ScheduleDetailEntity> matchedScheduleDetailList = currentDayScheduleList.stream()
                            .filter(scheduleDetailEntity -> finalPublishedScheduleIds1.contains(scheduleDetailEntity.getWorkScheduleId()))
                            .collect(Collectors.toList());
                    if (CollUtil.isEmpty(matchedScheduleDetailList)) {
                        detailBuilder.setHasScheduled(false);
                    } else {
                        List<ScheduleDetailEntity> matchedOrgScheduleDetailList = matchedScheduleDetailList.stream()
                                .filter(scheduleDetailEntity -> scheduleDetailEntity.getOrgId().equals(employeePageResponse.getOrgId()))
                                .collect(Collectors.toList());
                        ScheduleDetailEntity currentDateSchedule = CollUtil.isEmpty(matchedOrgScheduleDetailList) ? matchedScheduleDetailList.get(0) : matchedOrgScheduleDetailList.get(0);
                        detailBuilder.setHasScheduled(true)
                                .setStartDateTime(currentDateSchedule.getStartDatetime().getTime())
                                .setEndDateTime(currentDateSchedule.getEndDatetime().getTime())
                                .setScheduleDetailId(currentDateSchedule.getId())
                                .setScheduleId(currentDateSchedule.getWorkScheduleId());
                    }
                }
            }

            scheduleDetailResponseList.add(detailBuilder.build());
        }

        builder.addAllScheduleDetailList(scheduleDetailResponseList);

        //生成已读记录
        Conditional.run(request.getGenerateRecord() & CollectionUtils.isNotEmpty(currentPeriodScheduleIds), () -> {
            //未生成已读记录的排班
            Set<Long> alreadyGeneratedRecordScheduleIds = scheduleReadRecordRepository.findList(currentPeriodScheduleIds, request.getEmployeeId()).stream().map(ScheduleReadRecordEntity::getWorkScheduleId).collect(Collectors.toSet());
            currentPeriodScheduleIds.removeAll(alreadyGeneratedRecordScheduleIds);
            if (CollectionUtils.isNotEmpty(currentPeriodScheduleIds)) {
                List<ScheduleReadRecordEntity> recordEntityList = new ArrayList<>();
                currentPeriodScheduleIds.forEach(id -> recordEntityList.add((ScheduleReadRecordEntity) new ScheduleReadRecordEntity().setWorkScheduleId(id).setEmployeeId(request.getEmployeeId()).setTenantId(request.getTenantId())));
                scheduleReadRecordRepository.batchInsert(recordEntityList);
            }
        });

        return builder.build();
    }

    @Override
    protected ScheduleServerProto.LatestScheduleResponse hasLatestSchedule(ScheduleServerProto.LatestScheduleQueryRequest request) {
        Set<Long> scheduleIds = scheduleDetailRepository.findScheduleIds(request.getEmployeeId(), request.getTenantId());
        ScheduleServerProto.LatestScheduleResponse.Builder builder = ScheduleServerProto.LatestScheduleResponse.newBuilder();
        if (CollectionUtils.isNotEmpty(scheduleIds)) {
            List<ScheduleReadRecordEntity> list = scheduleReadRecordRepository.findList(scheduleIds, request.getEmployeeId());
            builder.setHasLatestScheduled(scheduleIds.size() == list.size());
        }
        return builder.build();
    }
}
