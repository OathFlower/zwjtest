
package cn.xunhou.xbbcloud.rpc.schedule.dao;

import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleReadRecordEntity;
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
public class ScheduleReadRecordRepository extends XbbRepository<ScheduleReadRecordEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ScheduleReadRecordRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    /**
     * 已读排班结果
     *
     * @param scheduleIds 排班id集合
     * @param employeeId  员工id
     * @return 结果
     */
    @NonNull
    public List<ScheduleReadRecordEntity> findList(@Nullable Collection<Long> scheduleIds,
                                                   @Nullable Long employeeId) {
        IAssert.hasTrue("已读排班列表查询参数不能全部为空", CollectionUtils.isNotEmpty(scheduleIds), employeeId != null);
        @Language("sql") String sql = "select * from schedule_read_record where deleted_flag = 0";
        Map<String, Object> params = new HashMap<>(4);
        if (CollectionUtils.isNotEmpty(scheduleIds)) {
            params.put("scheduleIds", scheduleIds);
            sql += " and work_schedule_id in (:scheduleIds)";
        }
        if (employeeId != null) {
            params.put("employeeId", employeeId);
            sql += " and employee_id = :employeeId";
        }
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(ScheduleReadRecordEntity.class)))
                .orElse(new ArrayList<>());
    }

}

