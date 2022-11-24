
package cn.xunhou.xbbcloud.rpc.attendance.dao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceSalaryBillBatchDetailEntity;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.param.AttendanceBillDetailQueryParam;
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
public class AttendanceSalaryBillBatchDetailRepository extends XbbRepository<AttendanceSalaryBillBatchDetailEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public AttendanceSalaryBillBatchDetailRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<AttendanceSalaryBillBatchDetailEntity> findByBillBatchIds(AttendanceBillDetailQueryParam param) {
        Map<String, Object> params = new HashMap<>();
        @Language("sql") String sql = "select * from attendance_salary_bill_batch_detail where deleted_flag = 0 ";
        if (CollUtil.isNotEmpty(param.getAttendanceBillBatchIds())) {
            sql = sql + "and salary_attendance_bill_batch_id in (:salary_attendance_bill_batch_id)";
            params.put("salary_attendance_bill_batch_id", param.getAttendanceBillBatchIds());
        }
        if (CollUtil.isNotEmpty(param.getAttendanceRecordIds())) {
            sql = sql + "and attendance_record_id in (:attendance_record_id)";
            params.put("attendance_record_id", param.getAttendanceRecordIds());
        }
        if (CollUtil.isNotEmpty(param.getTenantIds())) {
            sql = sql + "and tenant_id in (:tenant_id)";
            params.put("tenant_id", param.getTenantIds());
        }
        if (param.getStatus() != null) {
            sql = sql + "and status in (:status)";
            params.put("status", param.getStatus());
        }
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(AttendanceSalaryBillBatchDetailEntity.class))).orElse(new ArrayList<>());
    }

    public void batchUpdate(List<AttendanceSalaryBillBatchDetailEntity> salaryAttendanceBillBatchEntities) {
        for (AttendanceSalaryBillBatchDetailEntity attendanceSalaryBillBatchDetailEntity : salaryAttendanceBillBatchEntities) {
            updateById(attendanceSalaryBillBatchDetailEntity.getId(), attendanceSalaryBillBatchDetailEntity);
        }
    }
}

