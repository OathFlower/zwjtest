package cn.xunhou.web.xbbcloud.product.attendance.service;

import cn.hutool.core.date.DateUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.core.snow.SnowflakeIdGenerator;
import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.cloud.core.web.XbbWebStatus;
import cn.xunhou.cloud.task.bean.AddExportTaskParam;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.template.task.TemplateTaskServiceGrpc;
import cn.xunhou.grpc.proto.template.task.TemplateTaskServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerGrpc;
import cn.xunhou.grpc.proto.xbbcloud.AttendanceServerProto;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerGrpc;
import cn.xunhou.grpc.proto.xbbcloud.ScheduleServerProto;
import cn.xunhou.web.xbbcloud.common.constants.RedisConst;
import cn.xunhou.web.xbbcloud.product.attendance.dto.AttendanceAddressResult;
import cn.xunhou.web.xbbcloud.product.attendance.dto.AttendanceRecordExportData;
import cn.xunhou.web.xbbcloud.product.attendance.dto.AttendanceRecordResult;
import cn.xunhou.web.xbbcloud.product.attendance.dto.AttendanceSettingResult;
import cn.xunhou.web.xbbcloud.product.attendance.param.AttendanceAddressParam;
import cn.xunhou.web.xbbcloud.product.attendance.param.AttendanceRecordParam;
import cn.xunhou.web.xbbcloud.product.attendance.param.AttendanceSettingParam;
import cn.xunhou.web.xbbcloud.product.attendance.param.DailyConfirmAdjustParam;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工时确认service
 *
 * @author EDZ
 */
@Service
@Slf4j
public class AttendanceRecordService {

    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    @GrpcClient("ins-xbbcloud-platform")
    private AttendanceServerGrpc.AttendanceServerBlockingStub attendanceServerBlockingStub;
    @GrpcClient("ins-xbbcloud-platform")
    private ScheduleServerGrpc.ScheduleServerBlockingStub scheduleServerBlockingStub;
    @GrpcClient("ins-xhportal-platform")
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;
    @GrpcClient("ins-xhtask-platform")
    private TemplateTaskServiceGrpc.TemplateTaskServiceBlockingStub templateTaskServiceBlockingStub;
    @Autowired
    private AttendanceRecordExportService attendanceRecordExportService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    public JsonResponse<Void> adjustWorkHours(DailyConfirmAdjustParam adjustParam) {
        JsonResponse<AttendanceSettingResult> setting = getSetting();
        Integer maxSettlementHour = setting.getData().getMaxSettlementHour();
        double newWorkHours = Double.parseDouble(adjustParam.getNewWorkHours());
        if (maxSettlementHour != 0 && maxSettlementHour < newWorkHours) {
            throw new IllegalArgumentException("调整工时不可以大于设置的最高工时");
        }
        attendanceServerBlockingStub.adjust(AttendanceServerProto.AdjustRequest.newBuilder()
                .setAttendanceId(adjustParam.getId())
                .setActualWorkingHours(newWorkHours)
                .setRemark(adjustParam.getRemark() == null ? "" : adjustParam.getRemark())
                .build());
        return JsonResponse.success();
    }

    public JsonResponse<Void> confirmWorkHours(Long id) {
        attendanceServerBlockingStub.confirm(AttendanceServerProto.ConfirmRequest.newBuilder()
                .setAttendanceId(id).build());
        return JsonResponse.success();
    }

    public JsonListResponse<AttendanceRecordResult> recordList(AttendanceRecordParam param) {

        AttendanceServerProto.RecordQueryConditionBeRequest.Builder builder = AttendanceServerProto.RecordQueryConditionBeRequest.newBuilder()
                .setAttendanceRecordStatusEnum(AttendanceServerProto.AttendanceRecordStatusEnum.forNumber(param.getStatus()));
        if (param.isPaged()) {
            builder.setPaged(true)
                    .setCurPage(param.getCur_page())
                    .setPageSize(param.getPage_size());
        }
        Integer tenantId = null;
        if (param.getTenantId() != null) {
            tenantId = param.getTenantId();
        } else {
            tenantId = XBB_USER_CONTEXT.tenantId();
        }
        builder.setTenantId(tenantId);
        if (param.getKeyword() != null) {
            HrmServiceProto.EmployeePageResponses employeePageList = hrmServiceBlockingStub.findEmployeePageList(HrmServiceProto.EmployeePageBeRequest.newBuilder().setTenantId(XBB_USER_CONTEXT.tenantId()).setKeyWord1(param.getKeyword()).setType(1).build());
            if (CollectionUtils.isEmpty(employeePageList.getDataList())) {
                return JsonListResponse.success(Collections.emptyList(), 0);
            }
            builder.addAllEmpIds(employeePageList.getDataList().stream().map(HrmServiceProto.EmployeePageResponse::getId).collect(Collectors.toList()));
        }
        if (param.getOrg_id() != null) {
            builder.setOrgId(param.getOrg_id());
        }
        if (param.getDate_start() != null) {
            builder.setClockInStart(param.getDate_start());
        }
        if (param.getDate_end() != null) {
            builder.setClockInEnd(param.getDate_end());
        }
        AttendanceServerProto.RecordPageListBeResponse recordPageList = attendanceServerBlockingStub.findRecordPageList(builder.build());
        if (CollectionUtils.isEmpty(recordPageList.getDataList())) {
            return JsonListResponse.success(Collections.emptyList(), 0);
        }
        return JsonListResponse.success(convertResponse2Result(recordPageList.getDataList(), tenantId), (int) recordPageList.getTotal());
    }

    private List<AttendanceRecordResult> convertResponse2Result(List<AttendanceServerProto.RecordDetailBeResponse> dataList, Integer tenantId) {
        // 查询员工名称
        List<Long> empIds = dataList.stream().map(AttendanceServerProto.RecordDetailBeResponse::getEmpId).collect(Collectors.toList());
        List<Long> accountIds = dataList.stream().map(AttendanceServerProto.RecordDetailBeResponse::getModifyBy).filter(modifyBy -> modifyBy != 0).distinct().collect(Collectors.toList());
        HrmServiceProto.EmployeePageResponses employeePageList = hrmServiceBlockingStub.findEmployeePageList(HrmServiceProto.EmployeePageBeRequest.newBuilder()
                .addAllId(empIds)
                .setType(1)
                .build());
        Map<Long, HrmServiceProto.EmployeePageResponse> employeePageResponseMap = employeePageList.getDataList().stream().collect(Collectors.toMap(HrmServiceProto.EmployeePageResponse::getId, Function.identity()));
        Map<Long, HrmServiceProto.AccountDetailBeResponse> accountDetailBeResponseMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(accountIds)) {
            HrmServiceProto.AccountBeResponses accountBeResponses = hrmServiceBlockingStub.findAccountByIds(HrmServiceProto.SnowAccountRequest.newBuilder().addAllId(accountIds).build());
            accountDetailBeResponseMap = accountBeResponses.getDataList().stream().collect(Collectors.toMap(HrmServiceProto.AccountDetailBeResponse::getId, Function.identity()));
        }
        ScheduleServerProto.DictionaryListResponse dictionaryList = scheduleServerBlockingStub.findDictionaryList(ScheduleServerProto.DictionaryListRequest.newBuilder()
                .setDictionaryType(ScheduleServerProto.DictionaryTypeEnum.DIC_EMPLOYEE_SOURCE_TYPE)
                .setTenantId(tenantId)
                .build());
        List<ScheduleServerProto.DictionaryResponse> dictionaryListList = dictionaryList.getDictionaryListList();
        ScheduleServerProto.DictionaryResponse dictionaryResponse = dictionaryListList.stream().filter(v -> v.getCode() == 1).collect(Collectors.toList()).get(0);
        return AttendanceRecordResult.convertResponse2Result(dataList, employeePageResponseMap, dictionaryResponse.getId(),accountDetailBeResponseMap);
    }

    public JsonResponse<Void> saveSetting(AttendanceSettingParam settingParam) {
        AttendanceServerProto.AttendanceSetting.Builder builder = AttendanceServerProto.AttendanceSetting.newBuilder()
                .addAllAddressList(settingParam.getAddressParam() != null ? Collections.singletonList(AttendanceAddressParam.convertParam2Request(settingParam.getAddressParam())) : Collections.emptyList());
        if (settingParam.getCommonSettingId() != null) {
            builder.setCommonSettingsId(settingParam.getCommonSettingId());
        }
        if (settingParam.getAttendanceCalculateUnit() != null) {
            builder.setCalculateUnit(AttendanceServerProto.AttendanceCalculateUnit.forNumber(settingParam.getAttendanceCalculateUnit()));
        }
        if (settingParam.getMaxSettlementHour() != null) {
            builder.setMaxSettlementHour(settingParam.getMaxSettlementHour());
        }
        attendanceServerBlockingStub.saveSetting(builder.build());
        return JsonResponse.success();
    }

    public JsonResponse<AttendanceSettingResult> getSetting() {
        AttendanceServerProto.AttendanceSetting attendanceSetting = attendanceServerBlockingStub.settings(AttendanceServerProto.QuerySettingsRequest.newBuilder().build());
        Map<Long, HrmServiceProto.OrgListResponse> orgListResponseMap = new HashMap<>();
        AttendanceSettingResult settingResult = new AttendanceSettingResult();
        settingResult.setCommonSettingId(attendanceSetting.getCommonSettingsId());
        settingResult.setAttendanceCalculateUnit(attendanceSetting.getCalculateUnit().getNumber());
        settingResult.setMaxSettlementHour(attendanceSetting.getMaxSettlementHour());
        if (CollectionUtils.isNotEmpty(attendanceSetting.getAddressListList())) {
            List<Long> orgIds = attendanceSetting.getAddressListList().stream().map(AttendanceServerProto.AttendanceAddress::getOrgId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(orgIds)) {
                HrmServiceProto.OrgListBeResponses responses = hrmServiceBlockingStub.findOrgList(HrmServiceProto.OrgListRequest.newBuilder().addAllId(orgIds).build());
                orgListResponseMap = responses.getDataList().stream().collect(Collectors.toMap(HrmServiceProto.OrgListResponse::getId, Function.identity()));
            }
        }
        settingResult.setAddressResult(AttendanceAddressResult.convertResponse2Result(attendanceSetting.getAddressListList(), orgListResponseMap));
        return JsonResponse.success(settingResult);
    }

    public JsonResponse<Void> deleteAddress(Long id) {
        attendanceServerBlockingStub.deleteAttendanceAddress(AttendanceServerProto.DeleteAttendanceAddressRequest.newBuilder().setAttendanceAddressId(id).build());
        return JsonResponse.success();
    }

    @SneakyThrows(Exception.class)
    public JsonResponse<String> export(AttendanceRecordParam param) {
        String taskCode = RedisConst.EXP_ATT + SnowflakeIdGenerator.getId();
        Integer tenantId = XBB_USER_CONTEXT.tenantId();
        param.setTenantId(tenantId);
        HrmServiceProto.TenantBeResponses tenant = hrmServiceBlockingStub.findTenant(HrmServiceProto.TenantRequest.newBuilder().addId(tenantId).build());
        String companyName = tenant.getData(0).getCompany();
        AddExportTaskParam addExportTaskParam = AddExportTaskParam.newBuilder().tenantId(tenantId).taskCode(taskCode).params(XbbJsonUtil.toJsonBytes(param)).creator(XBB_USER_CONTEXT.get().getUserId()).fileName(companyName + DateUtil.today() + "考勤表.xlsx").build();
        attendanceRecordExportService.addExportTask(addExportTaskParam);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            TemplateTaskServiceProto.TaskBeResponses responses = templateTaskServiceBlockingStub.findTasks(TemplateTaskServiceProto.QueryTaskBeRequest.newBuilder()
                    .setPage(0)
                    .setPageSize(1)
                    .setTaskCode(taskCode)
                    .build());
            TemplateTaskServiceProto.TaskBeResponse response = responses.getDataList().get(0);
            if (TemplateTaskServiceProto.TaskStatus.FAILED == response.getStatus()) {
                log.info("导出失败,taskCode=" + taskCode);
                return JsonResponse.failed(XbbWebStatus.System_Error);
            }
            if (TemplateTaskServiceProto.TaskStatus.COMPLETED == response.getStatus()) {
                return JsonResponse.success(response.getObjectKey());
            }
        }
        return JsonResponse.failed(XbbWebStatus.System_Error);
    }

    public List<AttendanceRecordExportData> getExportData(AttendanceRecordParam param) {
        log.info("导出参数" + param);
        JsonListResponse<AttendanceRecordResult> response = recordList(param);
        if (CollectionUtils.isEmpty(response.getData())) {
            return Collections.emptyList();
        }
        List<AttendanceRecordExportData> dataList = new ArrayList<>();
        response.getData().forEach(attendanceRecordResult -> {
            dataList.add(convert2ExportData(attendanceRecordResult));
        });
        log.info("查询出的数据" + XbbJsonUtil.toJsonString(dataList));
        return dataList;
    }

    public AttendanceRecordExportData convert2ExportData(AttendanceRecordResult result) {
        AttendanceRecordExportData attendanceRecordExportData = new AttendanceRecordExportData();
        attendanceRecordExportData.setStaffNo(result.getStaffNo());
        attendanceRecordExportData.setName(result.getName());
        attendanceRecordExportData.setOrgName(result.getOrgName());
        attendanceRecordExportData.setScheduleTimeStart(result.getScheduleTimeStart());
        attendanceRecordExportData.setScheduleTimeEnd(result.getScheduleTimeEnd());
        attendanceRecordExportData.setClockInTime(result.getClockInTime());
        attendanceRecordExportData.setClockOutTime(result.getClockOutTime());
        attendanceRecordExportData.setPunchInAddress(result.getPunchInAddress());
        attendanceRecordExportData.setPunchOutAddress(result.getPunchOutAddress());
        attendanceRecordExportData.setScheduleWorkHours(result.getScheduleWorkHours());
        attendanceRecordExportData.setActualHour(result.getActualHour());
        attendanceRecordExportData.setCalculateUnit(result.getCalculateUnit());
        attendanceRecordExportData.setDate(result.getDate());
        return attendanceRecordExportData;

    }
}

