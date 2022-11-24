package cn.xunhou.xbbcloud.rpc.attendance.server;


import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.grpc.proto.xbbcloud.AbstractAttendanceServerImplBase;
import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerProto;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceRecordRepository;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceSalaryBillBatchDetailRepository;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceRecordEntity;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceSalaryBillBatchDetailEntity;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.param.AttendanceBillDetailQueryParam;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.param.AttendanceRecordQueryConditionParam;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.result.AttendanceRecordResult;
import cn.xunhou.xbbcloud.rpc.attendance.service.AttendanceService;
import cn.xunhou.xbbcloud.rpc.schedule.dao.ScheduleDetailRepository;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleDetailEntity;
import cn.xunhou.xbbcloud.sched.AttendanceSched;
import cn.xunhou.xbbcloud.sched.SalarySched;
import com.google.protobuf.Empty;
import com.google.protobuf.util.Timestamps;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 考勤打卡相关
 */
@GrpcService
@Slf4j
public class AttendanceServer extends AbstractAttendanceServerImplBase {

    @Resource
    private AttendanceRecordRepository attendanceRecordRepository;
    @Resource
    private AttendanceService attendanceService;
    @Resource
    private ScheduleDetailRepository scheduleDetailRepository;
    @Resource
    private AttendanceSalaryBillBatchDetailRepository attendanceSalaryBillBatchDetailRepository;
    @Resource
    private AttendanceSched attendanceSched;

    @Resource
    private SalarySched salarySched;

    /**
     * <pre>
     * 打卡分页列表
     * </pre>
     */
    @Override
    protected AttendanceServerProto.RecordPageListBeResponse findRecordPageList(AttendanceServerProto.RecordQueryConditionBeRequest request) {
        AttendanceRecordQueryConditionParam param = AttendanceRecordQueryConditionParam.of(request);
        log.info("打卡分页列表 findRecordPageList入参:" + XbbJsonUtil.toJsonString(param));
        assert param != null;
        //分页查询
        PagePojoList<AttendanceRecordResult> recordPageList = attendanceRecordRepository.findRecordPageList(param);
        if (CollectionUtils.isEmpty(recordPageList.getData())) {
            return AttendanceServerProto.RecordPageListBeResponse.newBuilder().addAllData(Collections.emptyList()).setTotal(0L).build();
        }
        List<Long> scheduleDetailIds = recordPageList.getData().stream().map(AttendanceRecordResult::getWorkScheduleDetailId).collect(Collectors.toList());
        List<ScheduleDetailEntity> scheduleDetailEntityList = scheduleDetailRepository.findList(null, null, null, null, null, scheduleDetailIds, null);
        Map<Long, ScheduleDetailEntity> scheduleDetailEntityMap = scheduleDetailEntityList.stream().collect(Collectors.toMap(ScheduleDetailEntity::getId, Function.identity()));
        //转换:param->Response
        List<AttendanceServerProto.RecordDetailBeResponse> recordDetailBeResponseList = new ArrayList<>();
        recordPageList.getData().forEach(result -> {
            ScheduleDetailEntity scheduleDetailEntity = scheduleDetailEntityMap.get(result.getWorkScheduleDetailId());
            AttendanceServerProto.RecordDetailBeResponse.Builder builder = AttendanceServerProto.RecordDetailBeResponse.newBuilder()
                    .setAttendanceRecordId(result.getId())
                    .setActualHour(result.getActualHour())
                    .setTenantId(result.getTenantId())
                    .setEmpId(result.getEmpId())
                    .setOrgId(result.getOrgId())
                    .setWorkScheduleDetailId(result.getWorkScheduleDetailId())
                    .setAttendanceRecordStatusEnum(AttendanceServerProto.AttendanceRecordStatusEnum.forNumber(result.getStatus()))
                    .setClockIn(Timestamps.fromMillis(result.getClockIn().getTime()))

                    .setPunchInAddress(result.getPunchInAddress())
                    .setPunchOutAddress(result.getPunchOutAddress())
                    .setWorkHour(result.getWorkHour())
                    .setAttendanceCalculateUnit(AttendanceServerProto.AttendanceCalculateUnit.forNumber(result.getCalculateUnit()))
                    .setAdjustWorkHourRemark(result.getAdjustWorkHourRemark())
                    .setCreateBy(result.getCreateBy())
                    .setModifyBy(result.getModified_by())
                    .setUpdatedAt(Timestamps.fromMillis(result.getUpdatedAt().getTime()));
            if (null != result.getClockOut()) {
                builder.setClockOut(Timestamps.fromMillis(result.getClockOut().getTime()));
            }
            if (scheduleDetailEntity != null) {
                builder.setScheduleTimeStart(Timestamps.fromMillis(scheduleDetailEntity.getStartDatetime().getTime()))
                        .setScheduleTimeEnd(Timestamps.fromMillis(scheduleDetailEntity.getEndDatetime().getTime()));
            }
            recordDetailBeResponseList.add(builder.build());

        });
        return AttendanceServerProto.RecordPageListBeResponse.newBuilder()
                .setTotal(recordPageList.getTotal())
                .addAllData(recordDetailBeResponseList)
                .build();
    }

    /**
     * <pre>
     * 开工
     * </pre>
     */
    @Override
    protected AttendanceServerProto.PunchBeResponse punchIn(AttendanceServerProto.PunchInBeRequest request) {
        return attendanceService.punchIn(request);
    }

    /**
     * <pre>
     * 完工
     * </pre>
     *
     * @param request
     */
    @Override
    protected AttendanceServerProto.PunchBeResponse punchOut(AttendanceServerProto.PunchOutBeRequest request) {
        return attendanceService.punchOut(request);
    }

    /**
     * <pre>
     * 调整工时
     * </pre>
     *
     * @param request
     */
    @Override
    protected Empty adjust(AttendanceServerProto.AdjustRequest request) {
        return attendanceService.adjust(request);
    }

    /**
     * <pre>
     * 确认工时
     * </pre>
     *
     * @param request
     */
    @Override
    protected Empty confirm(AttendanceServerProto.ConfirmRequest request) {
        return attendanceService.confirm(request);
    }


    /**
     * <pre>
     * 打卡配置
     * </pre>
     *
     * @param request
     */
    @Override
    protected AttendanceServerProto.AttendanceSetting settings(AttendanceServerProto.QuerySettingsRequest request) {
        return attendanceService.querySetting(request);
    }

    /**
     * <pre>
     * 保存配置
     * </pre>
     *
     * @param request
     */
    @Override
    protected Empty saveSetting(AttendanceServerProto.AttendanceSetting request) {
        return attendanceService.saveSetting(request);
    }


    /**
     * <pre>
     * 更新考勤打卡记录
     * </pre>
     *
     * @param request
     */
    @Override
    protected Empty updateRecord(AttendanceServerProto.UpdateAttendanceRecordBeRequest request) {
        return null;
    }

    /**
     * <pre>
     * 通过id查询打卡记录
     * </pre>
     *
     * @param request
     */
    @Override
    protected AttendanceServerProto.RecordDetailBeResponse findRecordById(AttendanceServerProto.AttendanceRecordIdBeRequest request) {
        AttendanceRecordEntity attendanceRecordEntity = attendanceRecordRepository.findById(request.getAttendanceRecordId());
        return AttendanceRecordEntity.toRecordDetailBeResponse(attendanceRecordEntity);
    }


    /**
     * <pre>
     * 打卡条件查询
     * </pre>
     *
     * @param request
     */
    @Override
    protected AttendanceServerProto.RecordListBeResponse findRecordByCondition(AttendanceServerProto.RecordQueryConditionBeRequest request) {
        AttendanceRecordQueryConditionParam param = AttendanceRecordQueryConditionParam.of(request);
        List<AttendanceRecordResult> recordList = attendanceRecordRepository.findRecordList(param);
        List<AttendanceServerProto.RecordDetailBeResponse> recordDetailBeResponses = AttendanceRecordResult.toRecordDetailBeResponseList(recordList);
        return AttendanceServerProto.RecordListBeResponse.newBuilder().addAllData(recordDetailBeResponses).build();
    }

    @Override
    protected AttendanceServerProto.BillBatchDetailListBeResponse findBillBatchDetailList(AttendanceServerProto.BillBatchDetailQueryConditionBeRequest request) {

        AttendanceBillDetailQueryParam param = new AttendanceBillDetailQueryParam();
        param.setAttendanceRecordIds(request.getAttendanceRecordIdList());
        param.setTenantIds(request.getTenantIdList());
        if (request.hasBillStatusEnum()) {
            param.setStatus(request.getBillStatusEnum().getNumber());
        }

        AttendanceServerProto.BillBatchDetailListBeResponse.Builder builder = AttendanceServerProto.BillBatchDetailListBeResponse.newBuilder();
        List<AttendanceSalaryBillBatchDetailEntity> billBatchDetailEntityList = attendanceSalaryBillBatchDetailRepository
                .findByBillBatchIds(param);
        if (CollectionUtils.isEmpty(billBatchDetailEntityList)) {
            return builder.build();
        }
        List<AttendanceServerProto.BillBatchDetailBeResponse> billBatchDetailBeResponses = new ArrayList<>();
        billBatchDetailEntityList.forEach(t -> {
            AttendanceServerProto.BillBatchDetailBeResponse detailBeResponse = AttendanceServerProto.BillBatchDetailBeResponse.newBuilder()
                    .setAttendanceRecordId(t.getAttendanceRecordId())
                    .setTenantId(t.getTenantId())
                    .setMoney(t.getMoney().toString())
                    .setBillPayType(AttendanceServerProto.BillPayType.forNumber(t.getPayType()))
                    .setBankName(t.getBankName())
                    .setBillStatusEnum(AttendanceServerProto.BillStatusEnum.forNumber(t.getStatus()))
                    .setBillSubStatusEnum(AttendanceServerProto.BillSubStatusEnum.forNumber(t.getSub_status()))
                    .build();
            billBatchDetailBeResponses.add(detailBeResponse);
        });
        builder.addAllDataList(billBatchDetailBeResponses);
        return builder.build();
    }

    @Override
    protected Empty runXxlJob(AttendanceServerProto.RunXxlJobRequest request) {
        String jobHandler = request.getJobHandler();
        String param = request.getParam();
        if ("sendAttendanceBill".equals(jobHandler)) {
            attendanceSched.sendAttendanceBill(param);
        } else if ("finishAttendanceRecord".equals(jobHandler)) {
            attendanceSched.finishAttendanceRecord(param);
        } else if ("salaryOvertime".equals(jobHandler)) {
            salarySched.salaryOvertime(param);
        } else if ("salaryRechargeFlow".equals(jobHandler)) {
            salarySched.salaryRechargeFlow(param);
        }
        return Empty.newBuilder().build();
    }

    @Override
    protected Empty deleteAttendanceAddress(AttendanceServerProto.DeleteAttendanceAddressRequest request) {
        attendanceService.deleteAddress(request.getAttendanceAddressId());
        return Empty.newBuilder().build();
    }
}
