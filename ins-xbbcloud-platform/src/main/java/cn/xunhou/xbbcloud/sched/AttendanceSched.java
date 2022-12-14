package cn.xunhou.xbbcloud.sched;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.xunhou.cloud.framework.util.SystemUtil;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.portal.PortalServiceGrpc;
import cn.xunhou.grpc.proto.portal.PortalServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerProto;
import cn.xunhou.xbbcloud.common.enums.EnumAttendanceBillStatus;
import cn.xunhou.xbbcloud.common.enums.EnumSalaryUnit;
import cn.xunhou.xbbcloud.middleware.rocket.pojo.AttendanceBillBatchMessage;
import cn.xunhou.xbbcloud.middleware.rocket.producer.RocketMsgService;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceRecordRepository;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceSalaryBillBatchDetailRepository;
import cn.xunhou.xbbcloud.rpc.attendance.dao.AttendanceSalaryBillBatchRepository;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceRecordEntity;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceSalaryBillBatchDetailEntity;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceSalaryBillBatchEntity;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.param.AttendanceBillBatchQueryParam;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.param.AttendanceBillDetailQueryParam;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.param.AttendanceRecordQueryConditionParam;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.result.AttendanceRecordResult;
import cn.xunhou.xbbcloud.rpc.schedule.dao.ScheduleDetailRepository;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleDetailEntity;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AttendanceSched {
    @GrpcClient("ins-xhportal-platform")
    private static HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;
    @GrpcClient("ins-xhportal-platform")
    private static PortalServiceGrpc.PortalServiceBlockingStub portalServiceBlockingStub;
    @Resource
    private AttendanceRecordRepository attendanceRecordRepository;
    @Resource
    private AttendanceSalaryBillBatchRepository attendanceBillBatchRepository;
    @Resource
    private AttendanceSalaryBillBatchDetailRepository attendanceBillBatchDetailRepository;
    @Resource
    private ScheduleDetailRepository scheduleDetailRepository;
    @Autowired
    private RocketMsgService rocketMsgService;

    /**
     * ????????????7???????????????????????????
     *
     * @param param ??????????????????
     */
    @XxlJob("finishAttendanceRecord")
    public ReturnT<String> finishAttendanceRecord(String param) {
        log.info("finishAttendanceRecord ---> ????????????");
        if (CharSequenceUtil.isBlank(param)) {
            param = XxlJobHelper.getJobParam();
        }

        //????????????????????????????????????
        AttendanceRecordQueryConditionParam attendanceRecordQueryConditionParam = new AttendanceRecordQueryConditionParam();
        attendanceRecordQueryConditionParam.setPunchDate(DateUtil.yesterday().toDateStr());
        attendanceRecordQueryConditionParam.setAttendanceRecordStatus(AttendanceServerProto.AttendanceRecordStatusEnum.START_WORK_VALUE);
        attendanceRecordQueryConditionParam.setAttendanceFinishFlag(false);
        List<AttendanceRecordResult> recordList = attendanceRecordRepository.findRecordList(attendanceRecordQueryConditionParam);
        if (CollUtil.isEmpty(recordList)) {
            log.info("finishAttendanceRecord ---> ???????????????????????????");
            return ReturnT.SUCCESS;
        }
        List<AttendanceRecordEntity> updateAttendanceRecordEntities = recordList.stream().map(t -> {
            AttendanceRecordEntity attendanceRecordEntity = new AttendanceRecordEntity();
            attendanceRecordEntity.setId(t.getId());
            attendanceRecordEntity.setStatus(1);
            attendanceRecordEntity.setAttendanceFinishFlag(1);
            return attendanceRecordEntity;
        }).collect(Collectors.toList());

        attendanceRecordRepository.batchUpdate(updateAttendanceRecordEntities);
        log.info("finishAttendanceRecord ---> ????????????");
        return ReturnT.SUCCESS;
    }

    /**
     * ??????07:31:00 ?????????????????????
     *
     * @param param ????????????
     */
    @XxlJob("sendAttendanceBill")
    public ReturnT<String> sendAttendanceBill(String param) {
        log.info("sendAttendanceBill ---> ????????????");
        if (CharSequenceUtil.isBlank(param)) {
            param = XxlJobHelper.getJobParam();
        }
        //??????????????????????????????
        AttendanceBillBatchQueryParam queryParam = new AttendanceBillBatchQueryParam();
        queryParam.setStatus(0);
        List<AttendanceSalaryBillBatchEntity> attendanceSalaryBillBatchEntityList = attendanceBillBatchRepository.findByQueryParam(queryParam);
        if (CollUtil.isEmpty(attendanceSalaryBillBatchEntityList)) {
            return ReturnT.SUCCESS;
        }
        List<AttendanceBillBatchMessage> sendMsg = new ArrayList<>();

        for (AttendanceSalaryBillBatchEntity attendanceSalaryBillBatchEntity : attendanceSalaryBillBatchEntityList) {
            fillSendMsg(attendanceSalaryBillBatchEntity, sendMsg);
        }
        if (CollUtil.isEmpty(sendMsg)) {
            log.info("sendAttendanceBill ---> sendMsg????????????");
            return ReturnT.SUCCESS;
        }
        rocketMsgService.sendAttendanceBillBatchMsg(sendMsg);
        attendanceBillBatchRepository.batchUpdate(buildUpdateSalaryAttendanceBillBatchEntities(sendMsg));
        attendanceBillBatchDetailRepository.batchUpdate(buildSalaryAttendanceBillBatchDetailEntities(sendMsg));
        log.info("sendAttendanceBill ---> ????????????");
        return ReturnT.SUCCESS;
    }


    private List<AttendanceSalaryBillBatchEntity> buildUpdateSalaryAttendanceBillBatchEntities(List<AttendanceBillBatchMessage> sendMsg) {
        List<AttendanceSalaryBillBatchEntity> salaryAttendanceBillBatchEntities = new ArrayList<>(sendMsg.size());
        sendMsg.forEach(t -> {
            AttendanceSalaryBillBatchEntity attendanceSalaryBillBatchEntity = new AttendanceSalaryBillBatchEntity();
            attendanceSalaryBillBatchEntity.setId(Long.valueOf(t.getBatchNo()));
            attendanceSalaryBillBatchEntity.setStatus(EnumAttendanceBillStatus.BILL_WAIT_REVIEW.getCode());
            salaryAttendanceBillBatchEntities.add(attendanceSalaryBillBatchEntity);
        });
        return salaryAttendanceBillBatchEntities;
    }

    private List<AttendanceSalaryBillBatchDetailEntity> buildSalaryAttendanceBillBatchDetailEntities(List<AttendanceBillBatchMessage> sendMsg) {
        List<AttendanceSalaryBillBatchDetailEntity> salaryAttendanceBillBatchDetailEntities = new ArrayList<>();
        List<AttendanceBillBatchMessage.AttendanceBillBatchDetailMessage> attendanceBillBatchDetailMessageList = sendMsg
                .stream().flatMap(t -> t.getAttendanceBillBatchDetailMessages().stream()).collect(Collectors.toList());
        attendanceBillBatchDetailMessageList.forEach(t -> {
            AttendanceSalaryBillBatchDetailEntity attendanceSalaryBillBatchDetailEntity = new AttendanceSalaryBillBatchDetailEntity();
            attendanceSalaryBillBatchDetailEntity.setId(Long.valueOf(t.getDetailBatchNo()));
            attendanceSalaryBillBatchDetailEntity.setStatus(EnumAttendanceBillStatus.BILL_WAIT_REVIEW.getCode());
            salaryAttendanceBillBatchDetailEntities.add(attendanceSalaryBillBatchDetailEntity);
        });
        return salaryAttendanceBillBatchDetailEntities;
    }

    private void fillSendMsg(AttendanceSalaryBillBatchEntity attendanceSalaryBillBatchEntity, List<AttendanceBillBatchMessage> sendMsg) {
        try {
            AttendanceBillBatchMessage attendanceBillBatchMessage = new AttendanceBillBatchMessage();
            List<AttendanceBillBatchMessage.AttendanceBillBatchDetailMessage> attendanceBillBatchDetailMessages = new ArrayList<>();

            AttendanceBillDetailQueryParam param = new AttendanceBillDetailQueryParam();
            Long id = attendanceSalaryBillBatchEntity.getId();
            param.setAttendanceBillBatchIds(Collections.singletonList(id));
            List<AttendanceSalaryBillBatchDetailEntity> attendanceBillBatchDetailEntityList = attendanceBillBatchDetailRepository
                    .findByBillBatchIds(param);
            //??????????????????????????????
            List<AttendanceRecordEntity> attendanceRecordEntityList = attendanceRecordRepository
                    .findByIds(attendanceBillBatchDetailEntityList.stream().map(AttendanceSalaryBillBatchDetailEntity::getAttendanceRecordId).collect(Collectors.toList()));
            Map<Long, AttendanceRecordEntity> attendanceRecordEntityMap = attendanceRecordEntityList.stream().collect(Collectors.toMap(AttendanceRecordEntity::getId, Function.identity()));

            //??????????????????
            HrmServiceProto.EmployeePageBeRequest employeePageBeRequest = HrmServiceProto.EmployeePageBeRequest.newBuilder()
                    .addAllId(attendanceRecordEntityList.stream().map(AttendanceRecordEntity::getEmpId).collect(Collectors.toList()))
                    .setPaged(false)
                    .setType(1)
                    .build();
            HrmServiceProto.EmployeePageResponses employeePageList = hrmServiceBlockingStub.findEmployeePageList(employeePageBeRequest);
            Map<Long, HrmServiceProto.EmployeePageResponse> pageResponseMap = employeePageList.getDataList().stream().collect(Collectors.toMap(HrmServiceProto.EmployeePageResponse::getId, Function.identity()));

            //?????????????????????????????????
            Map<Long, ScheduleDetailEntity> scheduleDetailEntityMap = new HashMap<>();
            List<Long> workScheduleDetailIds = attendanceRecordEntityList.stream().map(AttendanceRecordEntity::getWorkScheduleDetailId).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(workScheduleDetailIds)) {
                List<ScheduleDetailEntity> scheduleDetailEntityList = scheduleDetailRepository.findList(null, null, null, null, null
                        , workScheduleDetailIds, null);
                if (CollUtil.isNotEmpty(scheduleDetailEntityList)) {
                    scheduleDetailEntityMap = scheduleDetailEntityList.stream().collect(Collectors.toMap(ScheduleDetailEntity::getId, Function.identity()));
                }
            }

            //??????saas???????????????????????????
            PortalServiceProto.TenantCustomerRelationQueryBeRequest customerRelationQueryBeRequest = PortalServiceProto.TenantCustomerRelationQueryBeRequest.newBuilder()
                    .addTenantId(attendanceSalaryBillBatchEntity.getTenantId())
                    .build();
            PortalServiceProto.TenantCustomerRelationListBeResponse tenantCustomerRelationListBeResponse = portalServiceBlockingStub
                    .findTenantCustomerRelationList(customerRelationQueryBeRequest);
            List<PortalServiceProto.TenantCustomerRelationBeResponse> tenantCustomerRelationBeResponses = tenantCustomerRelationListBeResponse.getDataList();
            if (CollUtil.isEmpty(tenantCustomerRelationBeResponses)) {
                log.info(attendanceSalaryBillBatchEntity.getTenantId() + "?????????????????????");
                return;
            }
            List<Long> customerIds = tenantCustomerRelationBeResponses.stream().map(PortalServiceProto.TenantCustomerRelationBeResponse::getCustomerId).collect(Collectors.toList());
            //????????????????????????
            for (AttendanceSalaryBillBatchDetailEntity t : attendanceBillBatchDetailEntityList) {
                AttendanceRecordEntity attendanceRecordEntity = attendanceRecordEntityMap.get(t.getAttendanceRecordId());
                HrmServiceProto.EmployeePageResponse employeePageResponse = pageResponseMap.get(attendanceRecordEntity.getEmpId());
                ScheduleDetailEntity scheduleDetailEntity = scheduleDetailEntityMap.get(attendanceRecordEntity.getWorkScheduleDetailId());
                LocalDate localDate = new Date(attendanceRecordEntity.getClockIn().getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                //????????????????????????
                BigDecimal requiredAttendance = BigDecimal.ZERO;
                if (null != scheduleDetailEntity) {
                    requiredAttendance = new BigDecimal((scheduleDetailEntity.getEndDatetime().getTime() - scheduleDetailEntity.getStartDatetime().getTime()) / 1000).divide(new BigDecimal(3600), 2, RoundingMode.HALF_UP);
                }

                AttendanceBillBatchMessage.AttendanceBillBatchDetailMessage attendanceBillBatchDetailMessage =
                        new AttendanceBillBatchMessage.AttendanceBillBatchDetailMessage()
                                .setDetailBatchNo(t.getId().toString())
                                .setTennatId(t.getTenantId())
                                .setAttendanceRecordId(t.getAttendanceRecordId())
                                .setIdCardNo(employeePageResponse.getIdCard())
                                .setRequiredAttendance(requiredAttendance.toString())
                                .setSalaryUnit(EnumSalaryUnit.HOUR.getMessage())//???????????????
                                .setPunchDay(localDate.format(formatter))
                                .setRealAttendance(String.valueOf(attendanceRecordEntity.getActualHour()));
                attendanceBillBatchDetailMessages.add(attendanceBillBatchDetailMessage);
            }
            attendanceBillBatchMessage.setAttendanceBillBatchDetailMessages(attendanceBillBatchDetailMessages);
            attendanceBillBatchMessage.setBatchNo(id.toString());
            attendanceBillBatchMessage.setTennatId(attendanceSalaryBillBatchEntity.getTenantId());
            attendanceBillBatchMessage.setCustomerIds(customerIds);
            sendMsg.add(attendanceBillBatchMessage);
        } catch (Exception e) {
            log.info("fillSendMsg ---> ???????????? e = " + e);
            if (SystemUtil.isOffline()) {
                throw e;
            }
        }
    }
}
