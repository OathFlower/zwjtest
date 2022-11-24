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
     * 每日早上7点定时结束隔天任务
     *
     * @param param 定时任务参数
     */
    @XxlJob("finishAttendanceRecord")
    public ReturnT<String> finishAttendanceRecord(String param) {
        log.info("finishAttendanceRecord ---> 开始执行");
        if (CharSequenceUtil.isBlank(param)) {
            param = XxlJobHelper.getJobParam();
        }

        //查询昨日未打下班卡的记录
        AttendanceRecordQueryConditionParam attendanceRecordQueryConditionParam = new AttendanceRecordQueryConditionParam();
        attendanceRecordQueryConditionParam.setPunchDate(DateUtil.yesterday().toDateStr());
        attendanceRecordQueryConditionParam.setAttendanceRecordStatus(AttendanceServerProto.AttendanceRecordStatusEnum.START_WORK_VALUE);
        attendanceRecordQueryConditionParam.setAttendanceFinishFlag(false);
        List<AttendanceRecordResult> recordList = attendanceRecordRepository.findRecordList(attendanceRecordQueryConditionParam);
        if (CollUtil.isEmpty(recordList)) {
            log.info("finishAttendanceRecord ---> 未找到相应打卡记录");
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
        log.info("finishAttendanceRecord ---> 执行结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 每日07:31:00 同步账单到结算
     *
     * @param param 定时参数
     */
    @XxlJob("sendAttendanceBill")
    public ReturnT<String> sendAttendanceBill(String param) {
        log.info("sendAttendanceBill ---> 开始执行");
        if (CharSequenceUtil.isBlank(param)) {
            param = XxlJobHelper.getJobParam();
        }
        //获取所有待发送的账单
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
            log.info("sendAttendanceBill ---> sendMsg暂无数据");
            return ReturnT.SUCCESS;
        }
        rocketMsgService.sendAttendanceBillBatchMsg(sendMsg);
        attendanceBillBatchRepository.batchUpdate(buildUpdateSalaryAttendanceBillBatchEntities(sendMsg));
        attendanceBillBatchDetailRepository.batchUpdate(buildSalaryAttendanceBillBatchDetailEntities(sendMsg));
        log.info("sendAttendanceBill ---> 执行结束");
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
            //获取所有考勤打卡记录
            List<AttendanceRecordEntity> attendanceRecordEntityList = attendanceRecordRepository
                    .findByIds(attendanceBillBatchDetailEntityList.stream().map(AttendanceSalaryBillBatchDetailEntity::getAttendanceRecordId).collect(Collectors.toList()));
            Map<Long, AttendanceRecordEntity> attendanceRecordEntityMap = attendanceRecordEntityList.stream().collect(Collectors.toMap(AttendanceRecordEntity::getId, Function.identity()));

            //获取所有员工
            HrmServiceProto.EmployeePageBeRequest employeePageBeRequest = HrmServiceProto.EmployeePageBeRequest.newBuilder()
                    .addAllId(attendanceRecordEntityList.stream().map(AttendanceRecordEntity::getEmpId).collect(Collectors.toList()))
                    .setPaged(false)
                    .setType(1)
                    .build();
            HrmServiceProto.EmployeePageResponses employeePageList = hrmServiceBlockingStub.findEmployeePageList(employeePageBeRequest);
            Map<Long, HrmServiceProto.EmployeePageResponse> pageResponseMap = employeePageList.getDataList().stream().collect(Collectors.toMap(HrmServiceProto.EmployeePageResponse::getId, Function.identity()));

            //获取考勤打卡对应的排班
            Map<Long, ScheduleDetailEntity> scheduleDetailEntityMap = new HashMap<>();
            List<Long> workScheduleDetailIds = attendanceRecordEntityList.stream().map(AttendanceRecordEntity::getWorkScheduleDetailId).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(workScheduleDetailIds)) {
                List<ScheduleDetailEntity> scheduleDetailEntityList = scheduleDetailRepository.findList(null, null, null, null, null
                        , workScheduleDetailIds, null);
                if (CollUtil.isNotEmpty(scheduleDetailEntityList)) {
                    scheduleDetailEntityMap = scheduleDetailEntityList.stream().collect(Collectors.toMap(ScheduleDetailEntity::getId, Function.identity()));
                }
            }

            //获取saas客户绑定的人力客户
            PortalServiceProto.TenantCustomerRelationQueryBeRequest customerRelationQueryBeRequest = PortalServiceProto.TenantCustomerRelationQueryBeRequest.newBuilder()
                    .addTenantId(attendanceSalaryBillBatchEntity.getTenantId())
                    .build();
            PortalServiceProto.TenantCustomerRelationListBeResponse tenantCustomerRelationListBeResponse = portalServiceBlockingStub
                    .findTenantCustomerRelationList(customerRelationQueryBeRequest);
            List<PortalServiceProto.TenantCustomerRelationBeResponse> tenantCustomerRelationBeResponses = tenantCustomerRelationListBeResponse.getDataList();
            if (CollUtil.isEmpty(tenantCustomerRelationBeResponses)) {
                log.info(attendanceSalaryBillBatchEntity.getTenantId() + "未绑定人力客户");
                return;
            }
            List<Long> customerIds = tenantCustomerRelationBeResponses.stream().map(PortalServiceProto.TenantCustomerRelationBeResponse::getCustomerId).collect(Collectors.toList());
            //构建考勤账单详情
            for (AttendanceSalaryBillBatchDetailEntity t : attendanceBillBatchDetailEntityList) {
                AttendanceRecordEntity attendanceRecordEntity = attendanceRecordEntityMap.get(t.getAttendanceRecordId());
                HrmServiceProto.EmployeePageResponse employeePageResponse = pageResponseMap.get(attendanceRecordEntity.getEmpId());
                ScheduleDetailEntity scheduleDetailEntity = scheduleDetailEntityMap.get(attendanceRecordEntity.getWorkScheduleDetailId());
                LocalDate localDate = new Date(attendanceRecordEntity.getClockIn().getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                //计算排班打卡工时
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
                                .setSalaryUnit(EnumSalaryUnit.HOUR.getMessage())//目前只有时
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
            log.info("fillSendMsg ---> 出现错误 e = " + e);
            if (SystemUtil.isOffline()) {
                throw e;
            }
        }
    }
}
