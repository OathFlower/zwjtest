
package cn.xunhou.xbbcloud.rpc.attendance.dao;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceConfigAddressEntity;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fico
 */
@Repository
public class AttendanceConfigAddressRepository extends XbbRepository<AttendanceConfigAddressEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public AttendanceConfigAddressRepository(@Qualifier(value = "xbbcloud") NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<AttendanceConfigAddressEntity> findByIds(List<Long> ids) {
        @Language("sql") String sql = "select * from attendance_config_address where id = (:id)";
        Map<String, Object> params = new HashMap<>(2);
        params.put("id", ids);

        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(AttendanceConfigAddressEntity.class))).orElse(new ArrayList<>());
    }

    public AttendanceConfigAddressEntity findById(Long id) {
        @Language("sql") String sql = "select * from attendance_config_address where id in (:id)";
        Map<String, Object> params = new HashMap<>(2);
        params.put("id", id);

        return CollUtil.getFirst(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(AttendanceConfigAddressEntity.class)));
    }

    public void delete(Long id, Long userId) {
        @Language("sql") String sql = "update attendance_config_address set deleted_flag = 1,modified_by = :userId where id = :id ";
        Map<String, Object> params = new HashMap<>(2);
        params.put("id", id);
        params.put("userId", userId);
        this.jdbcTemplate.update(sql, params);

    }

    public List<AttendanceConfigAddressEntity> findByCommonConfigId(Long commonConfigId) {
        return null; //TODO
    }

}

