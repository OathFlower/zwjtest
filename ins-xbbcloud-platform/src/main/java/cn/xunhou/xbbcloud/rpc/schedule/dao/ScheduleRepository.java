
package cn.xunhou.xbbcloud.rpc.schedule.dao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.rpc.schedule.entity.ScheduleEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.*;

/**
 * @author system
 */
@Repository
public class ScheduleRepository extends XbbRepository<ScheduleEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ScheduleRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    /**
     * 根据排班id集合查找
     *
     * @param ids 结合
     * @return 结果
     */
    @NonNull
    public List<ScheduleEntity> findByIds(@NonNull Collection<Long> ids) {
        IAssert.state(CollectionUtils.isNotEmpty(ids), "排班id集合不能为空");
        @Language("sql") String sql = "select * from schedule where id in (:ids) and deleted_flag = 0";
        Map<String, Object> params = new HashMap<>(2);
        params.put("ids", ids);
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(ScheduleEntity.class))).orElse(new ArrayList<>());
    }

    /**
     * 根据排班id查找
     *
     * @param id 排班id
     * @return 结果
     */
    @Nullable
    public ScheduleEntity findOneById(@NonNull Long id) {
        @Language("sql") String sql = "select * from schedule where id = :id and deleted_flag = 0";
        Map<String, Object> params = new HashMap<>(2);
        params.put("id", id);
        return CollUtil.getFirst(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(ScheduleEntity.class)));
    }

    /**
     * 在指定周期内查询指定部门的排班
     *
     * @param orgId     部门id
     * @param startDate 排班开始日期 包含
     * @param endDate   排班结束日期 包含
     * @return 结果
     */
    @Nullable
    public ScheduleEntity findOne(@NonNull Long orgId,
                                  @NonNull Date startDate,
                                  @NonNull Date endDate) {
        @Language("sql") String sql = "select * from schedule where org_id = :orgId and start_date = :startDate and end_date = :endDate and deleted_flag = 0 ";
        Map<String, Object> params = new HashMap<>(4);
        params.put("orgId", orgId);
        params.put("startDate", DateUtil.format(startDate, "yyyy-MM-dd"));
        params.put("endDate", DateUtil.format(endDate, "yyyy-MM-dd"));
        return CollUtil.getFirst(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(ScheduleEntity.class)));
    }

    public ScheduleEntity findOne(@NonNull Long orgId, @NonNull java.util.Date currentDate) {
        @Language("sql") String sql = "select * from schedule where org_id = :orgId and start_date <= :currentDate and :currentDate <= schedule.end_date and deleted_flag = 0 ";
        Map<String, Object> params = new HashMap<>(2);
        params.put("orgId", orgId);
        params.put("currentDate", DateUtil.format(currentDate, "yyyy-MM-dd"));
        return CollUtil.getFirst(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(ScheduleEntity.class)));
    }
}

