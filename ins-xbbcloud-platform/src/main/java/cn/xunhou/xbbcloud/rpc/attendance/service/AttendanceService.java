package cn.xunhou.xbbcloud.rpc.attendance.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.core.json.XbbProtoJsonUtil;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerProto;
import cn.xunhou.xbbcloud.common.exception.XbbCloudErrorCode;
import cn.xunhou.xbbcloud.common.exception.XbbCloudException;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceConfigAddressRepository;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceRecordRepository;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceSalaryBillBatchDetailRepository;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceSalaryBillBatchRepository;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceConfigAddressEntity;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceRecordEntity;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceSalaryBillBatchDetailEntity;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceSalaryBillBatchEntity;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.param.AttendanceBillBatchQueryParam;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.param.AttendanceRecordQueryConditionParam;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.result.AttendanceConfigAddressResult;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.result.AttendanceRecordResult;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.result.AttendanceSettingResult;
import cn.xunhou.xbbcloud.rpc.other.dao.CommonSettingRepository;
import cn.xunhou.xbbcloud.rpc.other.dao.DictionaryRepository;
import cn.xunhou.xbbcloud.rpc.other.entity.CommonSettingEntity;
import cn.xunhou.xbbcloud.rpc.other.entity.DictionaryEntity;
import cn.xunhou.xbbcloud.rpc.schedule.dao.ScheduleDetailRepository;
import cn.xunhou.xbbcloud.rpc.schedule.dao.ScheduleRepository;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleDetailEntity;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleEntity;
import com.google.common.collect.Maps;
import com.google.protobuf.Empty;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Service
@Slf4j
public class AttendanceService {
    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();
    @GrpcClient("ins-xhportal-platform")
    private static HrmServiceGrpc.HrmServiceBlockingStub saasServiceBlockingStub;
    @Resource
    private AttendanceRecordRepository attendanceRecordRepository;
    @Resource
    private AttendanceConfigAddressRepository attendanceConfigAddressRepository;
    @Resource
    private CommonSettingRepository commonSettingRepository;
    @Resource
    private ScheduleDetailRepository scheduleDetailRepository;
    @Resource
    private AttendanceSalaryBillBatchDetailRepository attendanceSalaryBillBatchDetailRepository;
    @Resource
    private AttendanceSalaryBillBatchRepository attendanceSalaryBillBatchRepository;
    @Resource
    private DictionaryRepository dictionaryRepository;

    @Resource
    private ScheduleRepository scheduleRepository;


    public AttendanceServerProto.PunchBeResponse punchIn(AttendanceServerProto.PunchInBeRequest request) {
        if (request == null) {
            return AttendanceServerProto.PunchBeResponse.newBuilder().build();
        }
        //????????????????????????
        HrmServiceProto.EmployeePageResponse employeePageResponse = getEmployee(request.getEmpId());

        //????????????
        ScheduleEntity scheduleEntity = scheduleRepository.findOne(employeePageResponse.getOrgId(),
                Date.from(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8))));
        List<ScheduleDetailEntity> scheduleDetailEntityList = null;
        if (scheduleEntity != null) {
            //???????????????????????????
            scheduleDetailEntityList = scheduleDetailRepository.findList(null, Collections.singletonList(scheduleEntity.getId()),
                    Collections.singletonList(employeePageResponse.getId())
                    , Date.from(LocalDateTime.of(LocalDate.now(), LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant())
                    , Date.from(LocalDateTime.of(LocalDate.now(), LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()), null, null);
        }

        AttendanceRecordQueryConditionParam attendanceRecordQueryConditionParam = new AttendanceRecordQueryConditionParam();
        attendanceRecordQueryConditionParam.setPaged(false);
        attendanceRecordQueryConditionParam.setEmpId(employeePageResponse.getId());
        attendanceRecordQueryConditionParam.setPunchDate(LocalDate.now().toString());
        List<AttendanceRecordResult> recordList = attendanceRecordRepository.findRecordList(attendanceRecordQueryConditionParam);
        if (CollUtil.isNotEmpty(recordList)) {
            throw new XbbCloudException("?????????????????????");
        }
        //????????????????????????
        AttendanceSettingResult attendanceSetting = commonSettingRepository.findAttendanceSetting(employeePageResponse.getTenantId());
        //????????????????????????
        AttendanceRecordEntity attendanceRecordEntity = new AttendanceRecordEntity();
        attendanceRecordEntity.setEmpId(employeePageResponse.getId());
        attendanceRecordEntity.setOrgId(employeePageResponse.getOrgId());
        attendanceRecordEntity.setWorkScheduleDetailId(CollUtil.isEmpty(scheduleDetailEntityList) ? null : CollUtil.getFirst(scheduleDetailEntityList).getId());
        attendanceRecordEntity.setClockIn(new Timestamp(request.getBasePunchInfo().getClock().getSeconds() * 1000));
        attendanceRecordEntity.setPunchInAttendanceConfigAddressId(getAttendanceConfigAddressId(attendanceSetting.getAddressList(), request.getBasePunchInfo()));
        attendanceRecordEntity.setPunchInAddress(request.getBasePunchInfo().getPunchAddress());
        attendanceRecordEntity.setCalculateUnit(attendanceSetting.getCalculateUnit());
        attendanceRecordEntity.setCreateBy(request.getCreateBy());
        attendanceRecordEntity.setTenantId(employeePageResponse.getTenantId());
        Number insert = attendanceRecordRepository.insert(attendanceRecordEntity);

        AttendanceServerProto.PunchBeResponse.Builder builder = AttendanceServerProto.PunchBeResponse.newBuilder()
                .setRecordId(insert.longValue());
        return builder.build();
    }

    public AttendanceServerProto.PunchBeResponse punchOut(AttendanceServerProto.PunchOutBeRequest request) {

        AttendanceRecordEntity attendanceRecordEntity = attendanceRecordRepository.findById(request.getRecordId());
        if (attendanceRecordEntity.getAttendanceFinishFlag() == 1) {
            throw new XbbCloudException("??????????????????????????????");
        }
        getEmployee(attendanceRecordEntity.getEmpId());
        //????????????????????????
        AttendanceSettingResult attendanceSetting = commonSettingRepository.findAttendanceSetting(attendanceRecordEntity.getTenantId());

        Timestamp clockOut = new Timestamp(request.getBasePunchInfo().getClock().getSeconds() * 1000);
        Long seconds = request.getBasePunchInfo().getClock().getSeconds() - (attendanceRecordEntity.getClockIn().getTime() / 1000);
        //??????????????????????????????????????????
        double workHour = calcActualHour(attendanceSetting.getCalculateUnit(), attendanceSetting.getMaxSettlementHour(), seconds);
        AttendanceRecordEntity updateAttendanceRecordEntity = new AttendanceRecordEntity();
        updateAttendanceRecordEntity
                .setClockOut(clockOut)
                .setPunchOutAddress(request.getBasePunchInfo().getPunchAddress())
                .setPunchOutAttendanceConfigAddressId(getAttendanceConfigAddressId(attendanceSetting.getAddressList(), request.getBasePunchInfo()))
                .setWorkHour(workHour)
                .setActualHour(workHour)
                .setStatus(AttendanceServerProto.AttendanceRecordStatusEnum.WAIT_CONFIRMED.getNumber())
                .setAttendanceFinishFlag(1)
                .setModifiedBy(request.getUpdateBy())
        ;
        attendanceRecordRepository.updateById(attendanceRecordEntity.getId(), updateAttendanceRecordEntity);

        AttendanceServerProto.PunchBeResponse.Builder builder = AttendanceServerProto.PunchBeResponse.newBuilder()
                .setRecordId(attendanceRecordEntity.getId());
        return builder.build();
    }

    private HrmServiceProto.EmployeePageResponse getEmployee(Long empId) {
        HrmServiceProto.SnowEmployeeRequest snowEmployeeRequest = HrmServiceProto.SnowEmployeeRequest.newBuilder()
                .addId(empId)
                .build();
        HrmServiceProto.EmployeePageResponses employeeDetail = saasServiceBlockingStub.findEmployeeDetail(snowEmployeeRequest);
        List<HrmServiceProto.EmployeePageResponse> dataList = employeeDetail.getDataList();
        IAssert.notEmpty(dataList, XbbCloudErrorCode.NOT_FOUND_EMP.getMessage());
        HrmServiceProto.EmployeePageResponse employeePageResponse = dataList.get(0);

        if (employeePageResponse.getCid() == 0) {
            throw new XbbCloudException("???????????????????????????,??????????????????");
        }

        if (employeePageResponse.getStatus() == 4) {
            throw new XbbCloudException(XbbCloudErrorCode.EMP_ALREADY_QUIT);
        }
        return employeePageResponse;
    }


    /**
     * ??????????????????????????????????????????
     */
    private Long getAttendanceConfigAddressId(List<AttendanceConfigAddressResult> attendanceConfigAddressResultList
            , AttendanceServerProto.BasePunchInfo basePunchInfo) {

        HashMap<Long, Double> map = Maps.newHashMap();
        // ???????????????????????????
        for (AttendanceConfigAddressResult attendanceConfigAddressResult : attendanceConfigAddressResultList) {
            Integer offsetDistance = attendanceConfigAddressResult.getOffsetDistance();
            GlobalCoordinates source = new GlobalCoordinates(attendanceConfigAddressResult.getLatitude(), attendanceConfigAddressResult.getLongitude());
            GlobalCoordinates target = new GlobalCoordinates(basePunchInfo.getLatitude(), basePunchInfo.getLongitude());
            Double d = new GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.Sphere, source, target).getEllipsoidalDistance();
            if (offsetDistance > d.intValue()) {
                map.put(attendanceConfigAddressResult.getAttendanceAddressId(), d);
            }
        }
        if (MapUtil.isEmpty(map)) {
            throw new XbbCloudException("?????????????????????");
        }
        List<Map.Entry<Long, Integer>> list = new ArrayList(map.entrySet());
        list.sort(Comparator.comparingInt(Map.Entry::getValue));
        return list.get(0).getKey();
    }

    public void saveSalaryBillBatchDetail(AttendanceRecordEntity attendanceRecordEntity) {

        log.info("saveSalaryBillBatchDetail ---> ????????????????????????");
        //???????????????????????????????????????
        HrmServiceProto.SnowEmployeeRequest snowEmployeeRequest = HrmServiceProto.SnowEmployeeRequest.newBuilder()
                .addId(attendanceRecordEntity.getEmpId())
                .build();
        HrmServiceProto.EmployeePageResponses employeeDetail = saasServiceBlockingStub.findEmployeeDetail(snowEmployeeRequest);
        HrmServiceProto.EmployeePageResponse employeePageResponse = employeeDetail.getDataList().get(0);

        //??????????????????
        long inviteType = employeePageResponse.getInviteType();
        //?????????????????? 1?????? 2?????? 3??????
        int employeeSource = employeePageResponse.getEmployeeSource();
        DictionaryEntity dictionaryEntity = dictionaryRepository.findOneById(inviteType);
        if (dictionaryEntity == null || !(dictionaryEntity.getCode().equals(1) && dictionaryEntity.getTenantId().equals(0) && dictionaryEntity.getType().equals(2) && employeeSource == 2)) {
            log.info("saveSalaryBillBatchDetail ---> dictionaryEntity = " + JSONUtil.toJsonStr(dictionaryEntity) + ",employeeDetail = " + XbbProtoJsonUtil.toJsonString(employeeDetail));
            return;
        }
        AttendanceBillBatchQueryParam attendanceBillBatchQueryParam = new AttendanceBillBatchQueryParam();
        attendanceBillBatchQueryParam.setTenantId(attendanceRecordEntity.getTenantId());
        attendanceBillBatchQueryParam.setStatus(0);
        Long batchId = null;
        //???????????????????????????
        List<AttendanceSalaryBillBatchEntity> attendanceSalaryBillBatchEntities = attendanceSalaryBillBatchRepository.findByQueryParam(attendanceBillBatchQueryParam);
        if (CollUtil.isEmpty(attendanceSalaryBillBatchEntities)) {
            //??????????????????
            AttendanceSalaryBillBatchEntity attendanceSalaryBillBatchEntity = new AttendanceSalaryBillBatchEntity();
            attendanceSalaryBillBatchEntity.setTenantId(attendanceRecordEntity.getTenantId());
            Number insert = attendanceSalaryBillBatchRepository.insert(attendanceSalaryBillBatchEntity);
            batchId = insert.longValue();
        } else {
            batchId = CollUtil.getFirst(attendanceSalaryBillBatchEntities).getId();
        }
        AttendanceSalaryBillBatchDetailEntity attendanceSalaryBillBatchDetailEntity = new AttendanceSalaryBillBatchDetailEntity();
        attendanceSalaryBillBatchDetailEntity.setSalaryAttendanceBillBatchId(batchId);
        attendanceSalaryBillBatchDetailEntity.setAttendanceRecordId(attendanceRecordEntity.getId());
        attendanceSalaryBillBatchDetailEntity.setTenantId(attendanceRecordEntity.getTenantId());
        attendanceSalaryBillBatchDetailRepository.insert(attendanceSalaryBillBatchDetailEntity);
    }

    public Empty saveSetting(AttendanceServerProto.AttendanceSetting request) {
        AttendanceSettingResult attendanceSetting = commonSettingRepository.findAttendanceSetting(XBB_USER_CONTEXT.get().getTenantId());
        if (request.hasCommonSettingsId()) {
            AttendanceSettingResult attendanceSettingResult = new AttendanceSettingResult()
                    .setCalculateUnit(attendanceSetting.getCalculateUnit())
                    .setMaxSettlementHour(attendanceSetting.getMaxSettlementHour());
            if (request.hasCalculateUnit()) {
                attendanceSettingResult.setCalculateUnit(request.getCalculateUnit().getNumber());
            }
            if (request.hasMaxSettlementHour()) {
                attendanceSettingResult.setMaxSettlementHour(request.getMaxSettlementHour());
            }
            commonSettingRepository.updateById(request.getCommonSettingsId(), (CommonSettingEntity) new CommonSettingEntity()
                    .setConfigInfo(XbbJsonUtil.toJsonString(attendanceSettingResult))
                    .setModifyBy(XBB_USER_CONTEXT.get().getUserId())
                    .setId(request.getCommonSettingsId()));
        }
        if (CollUtil.isNotEmpty(request.getAddressListList())) {
            for (AttendanceServerProto.AttendanceAddress attendanceAddress : request.getAddressListList()) {
                if (attendanceAddress.hasAttendanceAddressId()) {
                    attendanceConfigAddressRepository.delete(attendanceAddress.getAttendanceAddressId(), XBB_USER_CONTEXT.get().getUserId());
                }
                attendanceConfigAddressRepository.insert(new AttendanceConfigAddressEntity()
                        .setLatitude(attendanceAddress.getLatitude())
                        .setLongitude(attendanceAddress.getLongitude())
                        .setCreateBy(XBB_USER_CONTEXT.get().getUserId())
                        .setOrgId(attendanceAddress.getOrgId())
                        .setOffsetDistance(attendanceAddress.getOffsetDistance())
                        .setLocationAddress(attendanceAddress.getLocationAddress())
                        .setCommonConfigId(attendanceSetting.getCommonSettingId())
                        .setAddressName(attendanceAddress.getAddressName())
                        .setTenantId(XBB_USER_CONTEXT.get().getTenantId())
                );
            }
        }
        return Empty.newBuilder().build();
    }

    public AttendanceServerProto.AttendanceSetting querySetting(AttendanceServerProto.QuerySettingsRequest request) {
        Integer tenantId = null;
        if (request.hasTenantId()) {
            tenantId = request.getTenantId();
        } else {
            tenantId = XBB_USER_CONTEXT.get().getTenantId();
        }
        if (tenantId == null) {
            throw new XbbCloudException("??????????????????");
        }
        AttendanceSettingResult attendanceSetting = commonSettingRepository.findAttendanceSetting(tenantId);
        return AttendanceServerProto.AttendanceSetting.newBuilder()
                .setCommonSettingsId(attendanceSetting.getCommonSettingId())
                .setMaxSettlementHour(attendanceSetting.getMaxSettlementHour())
                .setCalculateUnit(AttendanceServerProto.AttendanceCalculateUnit.forNumber(attendanceSetting.getCalculateUnit()))
                .addAllAddressList(AttendanceConfigAddressResult.convertResult2Response(attendanceSetting.getAddressList()))
                .build();
    }

    public Empty adjust(AttendanceServerProto.AdjustRequest request) {
        AttendanceRecordEntity attendanceRecordEntity = attendanceRecordRepository.findById(request.getAttendanceId());
        if (AttendanceServerProto.AttendanceRecordStatusEnum.WAIT_CONFIRMED.getNumber() != attendanceRecordEntity.getStatus()) {
            throw new XbbCloudException("??????????????????????????????????????????");
        }
        AttendanceSettingResult attendanceSetting = commonSettingRepository.findAttendanceSetting(attendanceRecordEntity.getTenantId());
        double actualHour = calcActualHour(attendanceSetting.getCalculateUnit(), attendanceSetting.getMaxSettlementHour(), (long) (request.getActualWorkingHours() * 3600));
        attendanceRecordRepository.updateById(request.getAttendanceId(), (AttendanceRecordEntity) new AttendanceRecordEntity()
                .setAdjustWorkHourFlag(1)
                .setActualHour(actualHour)
                .setAdjustWorkHourRemark(request.getRemark())
                .setModifiedBy(XBB_USER_CONTEXT.get().getUserId())
                .setId(request.getAttendanceId()));
        return Empty.newBuilder().build();
    }

    /**
     * ??????????????????
     *
     * @param calculateUnit     ??????????????????
     * @param maxSettlementHour ??????????????????
     * @param seconds           ??????????????????????????????
     * @return
     */
    public double calcActualHour(Integer calculateUnit, Integer maxSettlementHour, Long seconds) {
        double actualHour = 0;
        switch (calculateUnit) {
            case AttendanceServerProto.AttendanceCalculateUnit.PC_MINUTES_UNIT_VALUE:
                actualHour = new BigDecimal(seconds).divide(new BigDecimal(3600), 1, RoundingMode.HALF_UP).doubleValue();
                break;
            case AttendanceServerProto.AttendanceCalculateUnit.PC_HALF_HOUR_UNIT_VALUE:
                actualHour = ((int) (seconds / 1800)) * 0.5;
                break;
            case AttendanceServerProto.AttendanceCalculateUnit.PC_HOURS_UNIT_VALUE:
                actualHour = (int) (seconds / 3600);
                break;
            default:
                break;
        }
        //???????????????????????????????????????????????????????????????????????????
        if (!maxSettlementHour.equals(0) && actualHour > maxSettlementHour) {
            actualHour = maxSettlementHour;
        }
        return new BigDecimal(actualHour).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    public Empty confirm(AttendanceServerProto.ConfirmRequest request) {
        AttendanceRecordEntity attendanceRecordEntity = attendanceRecordRepository.findById(request.getAttendanceId());
        if (AttendanceServerProto.AttendanceRecordStatusEnum.WAIT_CONFIRMED.getNumber() != attendanceRecordEntity.getStatus()) {
            throw new XbbCloudException("??????????????????????????????????????????");
        }
        Double actualHour = attendanceRecordEntity.getWorkHour();

        AttendanceRecordEntity entity = new AttendanceRecordEntity();
        if (attendanceRecordEntity.getAdjustWorkHourFlag() == 0) {
            //??????????????????????????????????????????????????????
            entity.setActualHour(attendanceRecordEntity.getWorkHour());
        } else {
            //???????????????????????????????????????
            actualHour = attendanceRecordEntity.getActualHour();
        }
        attendanceRecordRepository.updateById(request.getAttendanceId(), entity
                .setStatus(AttendanceServerProto.AttendanceRecordStatusEnum.REVIEWED.getNumber())
                .setModifiedBy(XBB_USER_CONTEXT.get().getUserId()));
        if (actualHour != 0) {
            //?????????????????????0????????????????????????
            saveSalaryBillBatchDetail(attendanceRecordEntity);
        }
        return Empty.newBuilder().build();
    }

    public void deleteAddress(long attendanceAddressId) {
        attendanceConfigAddressRepository.delete(attendanceAddressId, XBB_USER_CONTEXT.get().getUserId());
    }
}
