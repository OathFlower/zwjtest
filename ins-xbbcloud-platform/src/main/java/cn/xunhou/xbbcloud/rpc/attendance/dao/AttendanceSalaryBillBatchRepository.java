
package cn.xunhou.xbbcloud.rpc.attendance.dao;

import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceSalaryBillBatchEntity;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.param.AttendanceBillBatchQueryParam;
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

/**
 * @author system
 */
@Repository
public class AttendanceSalaryBillBatchRepository extends XbbRepository<AttendanceSalaryBillBatchEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public AttendanceSalaryBillBatchRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public static boolean isValid(Long i) {
        return i != null && i > 0;
    }

    public List<AttendanceSalaryBillBatchEntity> findByQueryParam(AttendanceBillBatchQueryParam param) {
        Map<String, Object> params = new HashMap<>();
        @Language("sql") String sql = "select * from attendance_salary_bill_batch where deleted_flag = 0";
        if (param.getTenantId() != null) {
            sql = sql + " and tenant_id = :tenantId";
            params.put("tenantId", param.getTenantId());
        }
        if (param.getStatus() != null) {
            sql = sql + " and status = :status";
            params.put("status", param.getStatus());
        }

        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(AttendanceSalaryBillBatchEntity.class))).orElse(new ArrayList<>());
    }

    public void batchUpdate(List<AttendanceSalaryBillBatchEntity> salaryAttendanceBillBatchEntities) {
        for (AttendanceSalaryBillBatchEntity attendanceSalaryBillBatchEntity : salaryAttendanceBillBatchEntities) {
            updateById(attendanceSalaryBillBatchEntity.getId(), attendanceSalaryBillBatchEntity);
        }
    }
}

