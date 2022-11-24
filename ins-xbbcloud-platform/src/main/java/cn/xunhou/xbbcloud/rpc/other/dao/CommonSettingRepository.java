
package cn.xunhou.xbbcloud.rpc.other.dao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.enums.EnumCommonSettingType;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceConfigAddressEntity;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.result.AttendanceConfigAddressResult;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.result.AttendanceSettingResult;
import cn.xunhou.xbbcloud.rpc.other.entity.CommonSettingEntity;
import cn.xunhou.xbbcloud.rpc.schedule.pojo.result.ScheduleSettingResult;
import lombok.NonNull;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author system
 */
@Repository
public class CommonSettingRepository extends XbbRepository<CommonSettingEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    public CommonSettingRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    /**
     * 查询打卡设置
     *
     * @param tenantId
     * @return
     */
    public AttendanceSettingResult findAttendanceSetting(@NonNull Integer tenantId) {
        CommonSettingEntity commonSettingEntity = findOne(tenantId, EnumCommonSettingType.CLOCK_IN);
        if (commonSettingEntity == null) {
            this.insert((CommonSettingEntity) new CommonSettingEntity()
                    .setConfigInfo(XbbJsonUtil.toJsonString(new AttendanceSettingResult()))
                    .setType(EnumCommonSettingType.CLOCK_IN.getCode())
                    .setCreateBy(XBB_USER_CONTEXT.get() == null ? 0 : XBB_USER_CONTEXT.get().getUserId())
                    .setTenantId(tenantId));
            commonSettingEntity = findOne(tenantId, EnumCommonSettingType.CLOCK_IN);
        }
        AttendanceSettingResult attendanceSettingResult = XbbJsonUtil.fromJsonString(commonSettingEntity.getConfigInfo(), AttendanceSettingResult.class);
        attendanceSettingResult.setCommonSettingId(commonSettingEntity.getId()).setCreateBy(commonSettingEntity.getCreateBy());
        //查询打卡地址
        @Language("sql") String sql = "select * from attendance_config_address where tenant_id = :tenantId and deleted_flag = 0";
        Map<String, Object> params = new HashMap<>(2);
        params.put("tenantId", tenantId);
        List<AttendanceConfigAddressEntity> configAddressEntities = this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(AttendanceConfigAddressEntity.class));
        attendanceSettingResult.setAddressList(AttendanceConfigAddressResult.convertEntity2Result(configAddressEntities));
        return attendanceSettingResult;
    }

    /**
     * 查询排班预警设置
     *
     * @param tenantId 租户id
     * @return
     */
    public ScheduleSettingResult findScheduleSetting(@NonNull Integer tenantId) {
        CommonSettingEntity commonSettingEntity = findOne(tenantId, EnumCommonSettingType.SCHEDULE_WORN);
        if (commonSettingEntity == null) {
            this.insert((CommonSettingEntity) new CommonSettingEntity()
                    .setConfigInfo(XbbJsonUtil.toJsonString(new ScheduleSettingResult()))
                    .setType(EnumCommonSettingType.SCHEDULE_WORN.getCode())
                    .setCreateBy(XBB_USER_CONTEXT.get() == null ? 0 : XBB_USER_CONTEXT.get().getUserId())
                    .setTenantId(tenantId));
            commonSettingEntity = findOne(tenantId, EnumCommonSettingType.SCHEDULE_WORN);
        }
        ScheduleSettingResult settingResult = XbbJsonUtil.fromJsonString(commonSettingEntity.getConfigInfo(), ScheduleSettingResult.class);
        settingResult.setCommonSettingId(commonSettingEntity.getId()).setCreateBy(commonSettingEntity.getCreateBy());
        return settingResult;
    }

    public CommonSettingEntity findOne(@NonNull Integer tenantId,
                                       @NonNull EnumCommonSettingType type) {
        @Language("sql") String sql = "select * from common_setting where tenant_id = :tenantId and type = :type";
        Map<String, Object> params = new HashMap<>(2);
        params.put("tenantId", tenantId);
        params.put("type", type.getCode());
        return CollUtil.getFirst(Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(CommonSettingEntity.class))).orElse(new ArrayList<>()));
    }

    public List<CommonSettingEntity> findList(@NonNull List<Integer> tenantId,
                                              @NonNull List<EnumCommonSettingType> type) {
        @Language("sql") String sql = "select * from common_setting where tenant_id in (:tenantId) and type in (:type)";
        Map<String, Object> params = new HashMap<>(2);
        params.put("tenantId", tenantId);
        params.put("type", type.stream().map(EnumCommonSettingType::getCode).distinct().collect(Collectors.toList()));
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(CommonSettingEntity.class))).orElse(new ArrayList<>());
    }


}

