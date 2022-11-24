# Protocol Documentation
<a name="top"></a>

## Table of Contents

- [attendance.proto](#attendance-proto)
    - [AdjustRequest](#xbbcloud-AdjustRequest)
    - [AttendanceAddress](#xbbcloud-AttendanceAddress)
    - [AttendanceRecordDetailBeResponse](#xbbcloud-AttendanceRecordDetailBeResponse)
    - [AttendanceRecordIdBeRequest](#xbbcloud-AttendanceRecordIdBeRequest)
    - [AttendanceRecordIdBeResponse](#xbbcloud-AttendanceRecordIdBeResponse)
    - [AttendanceRecordListRequest](#xbbcloud-AttendanceRecordListRequest)
    - [AttendanceRecordListResponse](#xbbcloud-AttendanceRecordListResponse)
    - [AttendanceSetting](#xbbcloud-AttendanceSetting)
    - [BasePunchInfo](#xbbcloud-BasePunchInfo)
    - [ConfirmRequest](#xbbcloud-ConfirmRequest)
    - [EndWorkBeRequest](#xbbcloud-EndWorkBeRequest)
    - [QuerySettingsRequest](#xbbcloud-QuerySettingsRequest)
    - [RecordListBeResponse](#xbbcloud-RecordListBeResponse)
    - [RecordPageListBeResponse](#xbbcloud-RecordPageListBeResponse)
    - [RecordQueryConditionBeRequest](#xbbcloud-RecordQueryConditionBeRequest)
    - [StartWorkBeRequest](#xbbcloud-StartWorkBeRequest)
    - [UpdateAttendanceRecordBeRequest](#xbbcloud-UpdateAttendanceRecordBeRequest)
  
    - [AttendanceCalculateUnit](#xbbcloud-AttendanceCalculateUnit)
    - [AttendanceRecordStatusEnum](#xbbcloud-AttendanceRecordStatusEnum)
    - [YesOrNo](#xbbcloud-YesOrNo)
  
    - [AttendanceServer](#xbbcloud-AttendanceServer)
  
- [schedule.proto](#schedule-proto)
    - [CopyPreviousScheduleRequest](#xbbcloud-CopyPreviousScheduleRequest)
    - [EmployeeSchedule](#xbbcloud-EmployeeSchedule)
    - [LoopScheduleRequest](#xbbcloud-LoopScheduleRequest)
    - [ScheduleId](#xbbcloud-ScheduleId)
    - [ScheduleRequest](#xbbcloud-ScheduleRequest)
    - [ScheduleResponse](#xbbcloud-ScheduleResponse)
    - [ScheduleSaveRequest](#xbbcloud-ScheduleSaveRequest)
    - [ScheduleSaveResponse](#xbbcloud-ScheduleSaveResponse)
    - [ScheduleSetting](#xbbcloud-ScheduleSetting)
    - [ScheduleSettingResponse](#xbbcloud-ScheduleSettingResponse)
    - [SingleDaySchedule](#xbbcloud-SingleDaySchedule)
  
    - [ScheduleServer](#xbbcloud-ScheduleServer)
  
- [Scalar Value Types](#scalar-value-types)



<a name="attendance-proto"></a>
<p align="right"><a href="#top">Top</a></p>

## attendance.proto



<a name="xbbcloud-AdjustRequest"></a>

### AdjustRequest
调整工时入参


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| attendanceId | [int64](#int64) |  | 打卡记录id |
| actualWorkingHours | [float](#float) |  | 实际工时 |
| remark | [string](#string) | optional | 工时调整原因 |






<a name="xbbcloud-AttendanceAddress"></a>

### AttendanceAddress
打卡地点


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| locationAddress | [string](#string) |  | 定位地址 |
| longitude | [double](#double) |  | 经度 |
| latitude | [double](#double) |  | 维度 |
| offsetDistance | [int32](#int32) |  | 偏移距离/米 |
| orgId | [int64](#int64) | optional | 部门id |






<a name="xbbcloud-AttendanceRecordDetailBeResponse"></a>

### AttendanceRecordDetailBeResponse
考勤记录详细信息出参


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| attendanceRecordId | [fixed64](#fixed64) |  | 考勤记录id |
| tenantId | [fixed32](#fixed32) |  | 租户id |
| empId | [fixed32](#fixed32) |  | 企业员工id |
| orgId | [fixed32](#fixed32) |  | 部门id |
| workScheduleDetailId | [fixed32](#fixed32) |  | 排班详情id |
| attendanceConfigAddressId | [fixed32](#fixed32) |  | 考勤打卡地址配置id |
| punchInAddress | [string](#string) |  | 上班打卡地址 |
| punchOutAddress | [string](#string) |  | 下班打卡地址 |
| clockIn | [int64](#int64) |  | 上班打卡时间 |
| clockOut | [int64](#int64) |  | 下班打卡时间 |
| workHour | [double](#double) |  | 打卡工时 |
| actualHour | [double](#double) |  | 实际工时 |
| adjustWorkHourRemark | [string](#string) |  | 调整工时备注 |
| salary | [string](#string) |  | 薪资 |
| attendanceRecordStatusEnum | [AttendanceRecordStatusEnum](#xbbcloud-AttendanceRecordStatusEnum) |  | 状态 |
| attendanceFinishFlag | [YesOrNo](#xbbcloud-YesOrNo) |  | 考勤是否结束 |
| createdAt | [int64](#int64) |  | 创建时间 |
| updatedAt | [int64](#int64) |  | 更新时间 |
| createBy | [int64](#int64) |  | 创建人 |
| modifyBy | [int64](#int64) |  | 更新人 |






<a name="xbbcloud-AttendanceRecordIdBeRequest"></a>

### AttendanceRecordIdBeRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| attendanceRecordId | [int64](#int64) |  | 考勤记录id |






<a name="xbbcloud-AttendanceRecordIdBeResponse"></a>

### AttendanceRecordIdBeResponse



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| recordId | [int64](#int64) |  | 考勤记录id |






<a name="xbbcloud-AttendanceRecordListRequest"></a>

### AttendanceRecordListRequest
打卡记录列表入参


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| orgId | [int64](#int64) | optional | 部门id |
| employeeId | [int64](#int64) | optional | 员工id |
| startTime | [google.protobuf.Timestamp](#google-protobuf-Timestamp) | optional | 开始时间 |
| endTime | [google.protobuf.Timestamp](#google-protobuf-Timestamp) | optional | 结束时间 |






<a name="xbbcloud-AttendanceRecordListResponse"></a>

### AttendanceRecordListResponse
打卡记录列表响应


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| attendanceDate | [string](#string) |  | 打卡日期 |
| employeeId | [int64](#int64) |  | 员工id |
| scheduleStartTime | [google.protobuf.Timestamp](#google-protobuf-Timestamp) |  | 排班开始时间 |
| scheduleEndTime | [google.protobuf.Timestamp](#google-protobuf-Timestamp) |  | 排班结束时间 |
| workScheduleDetailId | [int64](#int64) |  | 排班详情id |
| clockIn | [google.protobuf.Timestamp](#google-protobuf-Timestamp) |  | 上班打卡时间 |
| clockOut | [google.protobuf.Timestamp](#google-protobuf-Timestamp) |  | 下班打卡时间 |
| attendanceWorkingHours | [float](#float) |  | 打卡工时 |
| actualClockWorkingHours | [float](#float) |  | 实际工时 |
| calculateUnit | [AttendanceCalculateUnit](#xbbcloud-AttendanceCalculateUnit) |  | 工时计算单位 |
| operatorId | [int64](#int64) |  | 操作人 |
| operateTime | [google.protobuf.Timestamp](#google-protobuf-Timestamp) |  | 操作时间 |
| adjustRemark | [string](#string) |  | 调整原因 |
| attendanceId | [int64](#int64) |  | 打卡记录id |






<a name="xbbcloud-AttendanceSetting"></a>

### AttendanceSetting
打卡配置


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| addressList | [AttendanceAddress](#xbbcloud-AttendanceAddress) | repeated | 打卡地点 |
| calculateUnit | [AttendanceCalculateUnit](#xbbcloud-AttendanceCalculateUnit) |  | 工时计算单位 |
| maxSettlementHour | [int32](#int32) |  | 每日最高结算工时 |
| settingsId | [int64](#int64) |  | 配置id |






<a name="xbbcloud-BasePunchInfo"></a>

### BasePunchInfo



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| attendanceConfigAddressId | [int64](#int64) |  | 考勤打卡地址配置id |
| punchAddress | [string](#string) |  | 打卡地址 |
| clock | [int64](#int64) |  | 打卡时间 |
| longitude | [double](#double) |  | 经度 |
| latitude | [double](#double) |  | 维度 |






<a name="xbbcloud-ConfirmRequest"></a>

### ConfirmRequest
确认工时


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| attendanceId | [int64](#int64) |  | 打卡记录id |






<a name="xbbcloud-EndWorkBeRequest"></a>

### EndWorkBeRequest
完工出参


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| recordId | [int64](#int64) |  | 考勤记录id |
| basePunchInfo | [BasePunchInfo](#xbbcloud-BasePunchInfo) |  | 打卡基础信息 |
| updateBy | [int64](#int64) |  | 更新人 |






<a name="xbbcloud-QuerySettingsRequest"></a>

### QuerySettingsRequest
查询配置






<a name="xbbcloud-RecordListBeResponse"></a>

### RecordListBeResponse



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| data | [AttendanceRecordDetailBeResponse](#xbbcloud-AttendanceRecordDetailBeResponse) | repeated | 数据 |






<a name="xbbcloud-RecordPageListBeResponse"></a>

### RecordPageListBeResponse
考勤打卡列表出参


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| total | [int64](#int64) |  | 总记录数 |
| data | [AttendanceRecordDetailBeResponse](#xbbcloud-AttendanceRecordDetailBeResponse) | repeated | 数据 |






<a name="xbbcloud-RecordQueryConditionBeRequest"></a>

### RecordQueryConditionBeRequest
列表查询打卡记录查询条件入参


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| paged | [bool](#bool) |  | 是否分页 |
| curPage | [int32](#int32) |  | 当前页（从0开始） |
| pageSize | [int64](#int64) |  | 页面大小 |
| orgId | [fixed32](#fixed32) |  | 部门id |
| empId | [fixed32](#fixed32) |  | 企业员工id |
| attendanceRecordStatusEnum | [AttendanceRecordStatusEnum](#xbbcloud-AttendanceRecordStatusEnum) |  | 状态 |
| dateStart | [int64](#int64) |  | 查询日期开始 |
| dateEnd | [int64](#int64) |  | 查询日期结束 |
| attendanceRecordId | [fixed64](#fixed64) |  | 打卡记录id |
| attendanceRecordIds | [fixed64](#fixed64) | repeated |  |






<a name="xbbcloud-StartWorkBeRequest"></a>

### StartWorkBeRequest
开工入参


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| empId | [int64](#int64) |  | 企业员工id |
| basePunchInfo | [BasePunchInfo](#xbbcloud-BasePunchInfo) |  | 打卡基础信息 |
| createBy | [int64](#int64) |  | 创建人 |






<a name="xbbcloud-UpdateAttendanceRecordBeRequest"></a>

### UpdateAttendanceRecordBeRequest
修改考勤记录入参


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| attendanceRecordId | [fixed64](#fixed64) |  | 考勤记录id |
| punchOutAddress | [string](#string) |  | 下班打卡地址 |
| clockOut | [int64](#int64) |  | 下班打卡时间 |
| workHour | [double](#double) |  | 打卡工时 |
| actualHour | [double](#double) |  | 实际工时 |
| adjustWorkHourRemark | [string](#string) |  | 调整工时备注 |
| salary | [string](#string) |  | 薪资 |
| attendanceRecordStatusEnum | [AttendanceRecordStatusEnum](#xbbcloud-AttendanceRecordStatusEnum) |  | 状态 |
| attendanceFinishFlag | [YesOrNo](#xbbcloud-YesOrNo) |  | 考勤是否结束 |
| modifyBy | [int64](#int64) |  | 更新人 |





 


<a name="xbbcloud-AttendanceCalculateUnit"></a>

### AttendanceCalculateUnit
打卡工时计算单位

| Name | Number | Description |
| ---- | ------ | ----------- |
| PC_MINUTES_UNIT | 0 | 分钟 |
| PC_HALF_HOUR_UNIT | 1 | 半小时 |
| PC_HOURS_UNIT | 2 | 小时 |



<a name="xbbcloud-AttendanceRecordStatusEnum"></a>

### AttendanceRecordStatusEnum
打卡状态 （0上班打卡，1下班打卡，2已审核，3已发薪）

| Name | Number | Description |
| ---- | ------ | ----------- |
| START_WORK | 0 | 0上班打卡 |
| END_WORK | 1 | 1下班打卡 |
| REVIEWED | 2 | 2已审核 |
| PAID | 3 | 3已发薪 |



<a name="xbbcloud-YesOrNo"></a>

### YesOrNo
是否（0否，1是）

| Name | Number | Description |
| ---- | ------ | ----------- |
| NO | 0 | 0否 |
| YES | 1 | 1是 |


 

 


<a name="xbbcloud-AttendanceServer"></a>

### AttendanceServer
考勤打卡相关

| Method Name | Request Type | Response Type | Description |
| ----------- | ------------ | ------------- | ------------|
| confirm | [ConfirmRequest](#xbbcloud-ConfirmRequest) | [.google.protobuf.Empty](#google-protobuf-Empty) | 确认工时 |
| adjust | [AdjustRequest](#xbbcloud-AdjustRequest) | [.google.protobuf.Empty](#google-protobuf-Empty) | 调整工时 |
| settings | [QuerySettingsRequest](#xbbcloud-QuerySettingsRequest) | [AttendanceSetting](#xbbcloud-AttendanceSetting) | 打卡配置 |
| saveSetting | [AttendanceSetting](#xbbcloud-AttendanceSetting) | [.google.protobuf.Empty](#google-protobuf-Empty) | 保存配置 |
| startWork | [StartWorkBeRequest](#xbbcloud-StartWorkBeRequest) | [AttendanceRecordIdBeResponse](#xbbcloud-AttendanceRecordIdBeResponse) | 开工 |
| endWork | [EndWorkBeRequest](#xbbcloud-EndWorkBeRequest) | [AttendanceRecordIdBeResponse](#xbbcloud-AttendanceRecordIdBeResponse) | 完工 |
| updateRecord | [UpdateAttendanceRecordBeRequest](#xbbcloud-UpdateAttendanceRecordBeRequest) | [.google.protobuf.Empty](#google-protobuf-Empty) | 更新考勤打卡记录 |
| findRecordById | [AttendanceRecordIdBeRequest](#xbbcloud-AttendanceRecordIdBeRequest) | [AttendanceRecordDetailBeResponse](#xbbcloud-AttendanceRecordDetailBeResponse) | 通过id查询打卡记录 |
| findRecordPageList | [RecordQueryConditionBeRequest](#xbbcloud-RecordQueryConditionBeRequest) | [RecordPageListBeResponse](#xbbcloud-RecordPageListBeResponse) | 打卡分页列表 |
| findRecordByCondition | [RecordQueryConditionBeRequest](#xbbcloud-RecordQueryConditionBeRequest) | [RecordListBeResponse](#xbbcloud-RecordListBeResponse) | 打卡条件查询 |

 



<a name="schedule-proto"></a>
<p align="right"><a href="#top">Top</a></p>

## schedule.proto



<a name="xbbcloud-CopyPreviousScheduleRequest"></a>

### CopyPreviousScheduleRequest
复制前一周排班入参


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| preWorkScheduleId | [int64](#int64) |  | 前一周排班id |
| currentWorkScheduleId | [int64](#int64) | optional | 当前周排班id |






<a name="xbbcloud-EmployeeSchedule"></a>

### EmployeeSchedule
个人排班信息


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| employeeId | [int64](#int64) |  | 员工id |
| restDays | [int32](#int32) |  | 休息天数 |
| workingDays | [int32](#int32) |  | 工作天数 |
| dayScheduleList | [SingleDaySchedule](#xbbcloud-SingleDaySchedule) | repeated | 每天的排班信息 |
| totalWorkingHours | [int32](#int32) |  | 周工时

TODO:其他统计信息 |






<a name="xbbcloud-LoopScheduleRequest"></a>

### LoopScheduleRequest
循环排班入参


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| workScheduleId | [int64](#int64) |  | 排班id |
| orgId | [int64](#int64) |  | 部门id |
| employeeIds | [int64](#int64) | repeated | 指定员工 |
| coverWhenExist | [bool](#bool) |  | 是否覆盖已有排班 |






<a name="xbbcloud-ScheduleId"></a>

### ScheduleId
排班id


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| workScheduleId | [int64](#int64) |  |  |






<a name="xbbcloud-ScheduleRequest"></a>

### ScheduleRequest
查看排班入参


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| orgId | [int64](#int64) |  | 部门 |
| employeeIds | [int64](#int64) | repeated | 指定员工 |
| scheduleStartTime | [google.protobuf.Timestamp](#google-protobuf-Timestamp) |  | 排班周期开始时间 |
| scheduleEndTime | [google.protobuf.Timestamp](#google-protobuf-Timestamp) |  | 排班周期结束时间 |






<a name="xbbcloud-ScheduleResponse"></a>

### ScheduleResponse
排班结果


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| workScheduleId | [int64](#int64) |  | 排班id |
| preWorkScheduleId | [int64](#int64) |  | 上一个周期的排班id |
| employeeScheduleList | [EmployeeSchedule](#xbbcloud-EmployeeSchedule) | repeated | 个人排班 |






<a name="xbbcloud-ScheduleSaveRequest"></a>

### ScheduleSaveRequest
保存排班


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| orgId | [int64](#int64) |  | 部门id |
| scheduleStartTime | [google.protobuf.Timestamp](#google-protobuf-Timestamp) |  | 排班周期开始时间 |
| scheduleEndTime | [google.protobuf.Timestamp](#google-protobuf-Timestamp) |  | 排班周期结束时间 |
| scheduleList | [SingleDaySchedule](#xbbcloud-SingleDaySchedule) | repeated | 排班列表 |






<a name="xbbcloud-ScheduleSaveResponse"></a>

### ScheduleSaveResponse







<a name="xbbcloud-ScheduleSetting"></a>

### ScheduleSetting
排班设置message


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| fullTimeTotalWeeklyHours | [float](#float) |  | 周排班总工时 全职 |
| fullTimeDailyHours | [float](#float) |  | 日排班工时 全职 |
| weeklyRestDays | [float](#float) |  | 每周休息天数 |
| continuousWorkingDays | [float](#float) |  | 连续上班天数 |
| partTimeTotalWeeklyHours | [float](#float) |  | 周排班总工时 兼职 |
| partTimeDailyHours | [float](#float) |  | 日排班总工时 兼职 |






<a name="xbbcloud-ScheduleSettingResponse"></a>

### ScheduleSettingResponse







<a name="xbbcloud-SingleDaySchedule"></a>

### SingleDaySchedule
每日的排班信息


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| workScheduleDetailId | [int64](#int64) |  | 排班详情id |
| startAt | [google.protobuf.Timestamp](#google-protobuf-Timestamp) |  | 排班开始时间 |
| endAt | [google.protobuf.Timestamp](#google-protobuf-Timestamp) |  | 排班结束时间 |
| dayOfWeek | [int32](#int32) |  | 周一-周七 1-7 |
| employeeId | [int64](#int64) |  | 员工id |





 

 

 


<a name="xbbcloud-ScheduleServer"></a>

### ScheduleServer
排班管理服务

| Method Name | Request Type | Response Type | Description |
| ----------- | ------------ | ------------- | ------------|
| querySetting | [.google.protobuf.Empty](#google-protobuf-Empty) | [ScheduleSetting](#xbbcloud-ScheduleSetting) | 查询排班设置 |
| saveSetting | [ScheduleSetting](#xbbcloud-ScheduleSetting) | [ScheduleSettingResponse](#xbbcloud-ScheduleSettingResponse) | 保存排班设置 |
| querySchedule | [ScheduleRequest](#xbbcloud-ScheduleRequest) | [ScheduleResponse](#xbbcloud-ScheduleResponse) | 查看排班 |
| publish | [ScheduleId](#xbbcloud-ScheduleId) | [.google.protobuf.Empty](#google-protobuf-Empty) | 发布 |
| copyPreviousSchedule | [CopyPreviousScheduleRequest](#xbbcloud-CopyPreviousScheduleRequest) | [ScheduleId](#xbbcloud-ScheduleId) | 复制前一周排班 |
| unlock | [ScheduleId](#xbbcloud-ScheduleId) | [.google.protobuf.Empty](#google-protobuf-Empty) | 解锁排班 |
| loop | [LoopScheduleRequest](#xbbcloud-LoopScheduleRequest) | [.google.protobuf.Empty](#google-protobuf-Empty) | 循环排班 |
| saveSchedule | [ScheduleSaveRequest](#xbbcloud-ScheduleSaveRequest) | [ScheduleSaveResponse](#xbbcloud-ScheduleSaveResponse) | 保存排班 |

 



## Scalar Value Types

| .proto Type | Notes | C++ | Java | Python | Go | C# | PHP | Ruby |
| ----------- | ----- | --- | ---- | ------ | -- | -- | --- | ---- |
| <a name="double" /> double |  | double | double | float | float64 | double | float | Float |
| <a name="float" /> float |  | float | float | float | float32 | float | float | Float |
| <a name="int32" /> int32 | Uses variable-length encoding. Inefficient for encoding negative numbers – if your field is likely to have negative values, use sint32 instead. | int32 | int | int | int32 | int | integer | Bignum or Fixnum (as required) |
| <a name="int64" /> int64 | Uses variable-length encoding. Inefficient for encoding negative numbers – if your field is likely to have negative values, use sint64 instead. | int64 | long | int/long | int64 | long | integer/string | Bignum |
| <a name="uint32" /> uint32 | Uses variable-length encoding. | uint32 | int | int/long | uint32 | uint | integer | Bignum or Fixnum (as required) |
| <a name="uint64" /> uint64 | Uses variable-length encoding. | uint64 | long | int/long | uint64 | ulong | integer/string | Bignum or Fixnum (as required) |
| <a name="sint32" /> sint32 | Uses variable-length encoding. Signed int value. These more efficiently encode negative numbers than regular int32s. | int32 | int | int | int32 | int | integer | Bignum or Fixnum (as required) |
| <a name="sint64" /> sint64 | Uses variable-length encoding. Signed int value. These more efficiently encode negative numbers than regular int64s. | int64 | long | int/long | int64 | long | integer/string | Bignum |
| <a name="fixed32" /> fixed32 | Always four bytes. More efficient than uint32 if values are often greater than 2^28. | uint32 | int | int | uint32 | uint | integer | Bignum or Fixnum (as required) |
| <a name="fixed64" /> fixed64 | Always eight bytes. More efficient than uint64 if values are often greater than 2^56. | uint64 | long | int/long | uint64 | ulong | integer/string | Bignum |
| <a name="sfixed32" /> sfixed32 | Always four bytes. | int32 | int | int | int32 | int | integer | Bignum or Fixnum (as required) |
| <a name="sfixed64" /> sfixed64 | Always eight bytes. | int64 | long | int/long | int64 | long | integer/string | Bignum |
| <a name="bool" /> bool |  | bool | boolean | boolean | bool | bool | boolean | TrueClass/FalseClass |
| <a name="string" /> string | A string must always contain UTF-8 encoded or 7-bit ASCII text. | string | String | str/unicode | string | string | string | String (UTF-8) |
| <a name="bytes" /> bytes | May contain any arbitrary sequence of bytes. | string | ByteString | str | []byte | ByteString | string | String (ASCII-8BIT) |

