
package cn.xunhou.xbbcloud.rpc.schedule.dao;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleDetailEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author system
 */
@Repository
public class ScheduleDetailRepository extends XbbRepository<ScheduleDetailEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ScheduleDetailRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }


    /**
     * 查找排班详情列表
     *
     * @param orgId           部门id
     * @param workScheduleIds 排班id结合
     * @param employeeIds     员工id集合
     * @param startDate       开始日期 当前日期零点开始 该字段仅仅是搜索数据库中的排班开始时间
     * @param endDate         结束日期 当前日期23:59:59秒结束 该字段仅仅是搜索数据库中的排班开始时间
     * @param tenantIds       租户id集合
     * @return 结果
     */
    @NonNull
    public List<ScheduleDetailEntity> findList(@Nullable Long orgId,
                                               @Nullable List<Long> workScheduleIds,
                                               @Nullable List<Long> employeeIds,
                                               @Nullable Date startDate,
                                               @Nullable Date endDate,
                                               @Nullable List<Long> ids,
                                               @Nullable List<Integer> tenantIds) {
        IAssert.hasTrue("参数不能全部为空", orgId != null, CollectionUtils.isNotEmpty(workScheduleIds),
                CollectionUtils.isNotEmpty(employeeIds), startDate != null, endDate != null, CollectionUtils.isNotEmpty(ids), CollectionUtils.isNotEmpty(tenantIds));
        @Language("sql") String sql = "select * from schedule_detail where deleted_flag = 0";
        Map<String, Object> params = new HashMap<>(8);
        if (orgId != null && orgId != 0) {
            params.put("orgId", orgId);
            sql = sql + " and org_id = :orgId";
        }
        if (CollectionUtils.isNotEmpty(workScheduleIds)) {
            params.put("workScheduleIds", workScheduleIds);
            sql = sql + " and work_schedule_id in (:workScheduleIds)";
        }
        if (CollectionUtils.isNotEmpty(employeeIds)) {
            params.put("employeeIds", employeeIds);
            sql = sql + " and employee_id in (:employeeIds)";
        }
        if (startDate != null) {
            String currentDate = DateUtil.format(startDate, "yyyy-MM-dd");
            params.put("currentDateStart", currentDate + " 00:00:00");
            sql = sql + " and :currentDateStart <= start_datetime";
        }
        if (endDate != null) {
            String currentDate = DateUtil.format(endDate, "yyyy-MM-dd");
            params.put("currentDateEnd", currentDate + " 23:59:59");
            sql = sql + " and start_datetime <= :currentDateEnd";
        }
        if (CollectionUtils.isNotEmpty(ids)) {
            params.put("ids", ids);
            sql = sql + " and id in (:ids)";
        }
        if (CollectionUtils.isNotEmpty(tenantIds)) {
            params.put("tenantIds", tenantIds);
            sql = sql + " and tenant_id in (:tenantIds)";
        }
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(ScheduleDetailEntity.class)))
                .orElse(new ArrayList<>());
    }

    public int removeByIds(@NonNull Collection<Long> ids) {
        @Language("sql") String updateSql = "update schedule_detail set deleted_flag = 1 where id in (:ids)";
        Map<String, Object> params = new HashMap<>(2);
        params.put("ids", ids);
        return this.jdbcTemplate.update(updateSql, params);
    }


    /**
     * 查找指定员工在指定租户下所有的排班周期id
     *
     * @param employeeId 员工id
     * @param tenantId   租户id
     * @return 结果集合
     */
    public Set<Long> findScheduleIds(@NonNull Long employeeId, @Nullable Integer tenantId) {
        IAssert.notNull(employeeId, "员工id不能为空");
        @Language("sql") String sql = "select distinct (work_schedule_id) from schedule_detail where deleted_flag = 0 and employee_id = :employeeId";
        Map<String, Object> params = new HashMap<>(2);
        params.put("employeeId", employeeId);
        if (tenantId != null && tenantId != 0) {
            sql += " and tenant_id = :tenantId";
            params.put("tenantId", tenantId);
        }
        return new HashSet<>(Opt.ofEmptyAble(this.jdbcTemplate.queryForList(sql, params, Long.class))
                .orElse(new ArrayList<>()));
    }
}

