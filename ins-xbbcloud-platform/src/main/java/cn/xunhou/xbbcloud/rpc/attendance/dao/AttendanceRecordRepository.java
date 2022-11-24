package cn.xunhou.xbbcloud.rpc.attendance.dao;

import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.ConvertUtil;
import cn.xunhou.xbbcloud.rpc.attendance.entity.AttendanceRecordEntity;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.param.AttendanceRecordQueryConditionParam;
import cn.xunhou.xbbcloud.rpc.attendance.pojo.result.AttendanceRecordResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class AttendanceRecordRepository extends XbbRepository<AttendanceRecordEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public AttendanceRecordRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public static boolean isValid(Long i) {
        return i != null && i > 0L;
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<AttendanceRecordEntity> attendanceRecordEntities) {
        for (AttendanceRecordEntity attendanceRecordEntity : attendanceRecordEntities) {
            updateById(attendanceRecordEntity.getId(), attendanceRecordEntity);
        }
    }

    /**
     * 通过id查询打卡记录
     *
     * @param id 打卡id
     * @return 打卡记录
     */
    public AttendanceRecordEntity findById(@NonNull Long id) {
        @Language("sql") String sql = "select * from attendance_record where id = :id";
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AttendanceRecordEntity attendanceRecordEntity = this.jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(AttendanceRecordEntity.class));
        return Opt.ofNullable(attendanceRecordEntity).orElse(new AttendanceRecordEntity());
    }

    public List<AttendanceRecordEntity> findByIds(@NonNull List<Long> ids) {
        @Language("sql") String sql = "select * from attendance_record where id in (:id)";
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", ids);
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(AttendanceRecordEntity.class))).orElse(new ArrayList<>());
    }

    /**
     * 分页查询
     *
     * @param param 查询条件
     * @return 打卡记录
     */
    public PagePojoList<AttendanceRecordResult> findRecordPageList(@NonNull AttendanceRecordQueryConditionParam param) {
        List<AttendanceRecordResult> recordList = this.findRecordList(param);
        Integer recordListCount = this.findRecordListCount(param);
        PagePojoList.PagePojoListBuilder<AttendanceRecordResult> builder = PagePojoList.builder();
        builder.data(recordList).total(recordListCount);
        return builder.build();
    }

    /**
     * 条件查询打卡记录
     *
     * @param param 查询条件
     * @return 打卡记录
     */
    public List<AttendanceRecordResult> findRecordList(@NonNull AttendanceRecordQueryConditionParam param) {
        Map<String, Object> params = new HashMap<>(10);
        @Language("sql") String sql = "select * from attendance_record where deleted_flag = 0";

        if (isValid(param.getTenantId())) {
            sql = sql + " and tenant_id = :tenantId";
            params.put("tenantId", param.getTenantId());
        }
        if (isValid(param.getOrgId())) {
            sql = sql + " and org_id = :orgId";
            params.put("orgId", param.getOrgId());
        }
        if (isValid(param.getEmpId())) {
            sql = sql + " and emp_id = :empId";
            params.put("empId", param.getEmpId());
        }
        if (null != param.getAttendanceRecordStatus()) {
            sql = sql + " and status = :status";
            params.put("status", param.getAttendanceRecordStatus());
        }
        if (StringUtils.isNotBlank(param.getDateStart())) {
            sql = sql + " and clock_in >=  DATE_FORMAT(:dateStart,'%Y-%m-%d %H:%i:%S')";
            params.put("dateStart", param.getDateStart() + " 00:00:00");
        }
        if (StringUtils.isNotBlank(param.getDateEnd())) {
            sql = sql + " and clock_in <= DATE_FORMAT(:dateEnd,'%Y-%m-%d %H:%i:%S')";
            params.put("dateEnd", param.getDateEnd() + " 23:59:59");
        }
        if (StringUtils.isNotBlank(param.getPunchDate())) {
            sql = sql + " and DATE_FORMAT(clock_in,'%Y-%m-%d') = :punchDate";
            params.put("punchDate", param.getPunchDate());
        }
        if (param.getAttendanceFinishFlag() != null) {
            sql = sql + " and attendance_finish_flag = :attendance_finish_flag";
            params.put("attendance_finish_flag", ConvertUtil.boolToInt(param.getAttendanceFinishFlag()));
        }
        if (CollectionUtils.isNotEmpty(param.getEmpIds())) {
            sql = sql + " and emp_id in (:empIds)";
            params.put("empIds", param.getEmpIds());
        }
        sql = sql + " order by created_at desc";
        if (param.isPaged()) {
            //需要分页
            Integer start = param.getCurPage() * param.getPageSize();
            Integer end = param.getPageSize();
            sql = sql + " limit :start , :end ";
            params.put("start", start);
            params.put("end", end);
        }

        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(AttendanceRecordResult.class))).orElse(new ArrayList<>());
    }

    /**
     * 获取分页总数
     *
     * @param param 查询条件
     * @return 分页总数
     */
    private Integer findRecordListCount(AttendanceRecordQueryConditionParam param) {
        Map<String, Object> params = new HashMap<>();
        @Language("sql") String sql = "select count(1) from attendance_record where deleted_flag = 0";
        if (isValid(param.getTenantId())) {
            sql = sql + " and tenant_id = :tenantId";
            params.put("tenantId", param.getTenantId());
        }
        if (isValid(param.getOrgId())) {
            sql = sql + " and org_id = :orgId";
            params.put("orgId", param.getOrgId());
        }
        if (isValid(param.getEmpId())) {
            sql = sql + " and emp_id = :empId";
            params.put("empId", param.getEmpId());
        }
        if (null != param.getAttendanceRecordStatus()) {
            sql = sql + " and status = :status";
            params.put("status", param.getAttendanceRecordStatus());
        }
        if (StringUtils.isNotBlank(param.getDateStart())) {
            sql = sql + " and clock_in >=  DATE_FORMAT(:dateStart,'%Y-%m-%d %H:%i:%S')";
            params.put("dateStart", param.getDateStart() + " 00:00:00");
        }
        if (StringUtils.isNotBlank(param.getDateEnd())) {
            sql = sql + " and clock_in <= DATE_FORMAT(:dateEnd,'%Y-%m-%d %H:%i:%S')";
            params.put("dateEnd", param.getDateEnd() + " 23:59:59");
        }
        if (StringUtils.isNotBlank(param.getPunchDate())) {
            sql = sql + " and DATE_FORMAT(clock_in,'%Y-%m-%d') = :punchDate";
            params.put("punchDate", param.getPunchDate());
        }
        if (param.getAttendanceFinishFlag() != null) {
            sql = sql + " and attendance_finish_flag = :attendance_finish_flag";
            params.put("attendance_finish_flag", ConvertUtil.boolToInt(param.getAttendanceFinishFlag()));
        }
        if (CollectionUtils.isNotEmpty(param.getEmpIds())) {
            sql = sql + " and emp_id in (:empIds)";
            params.put("empIds", param.getEmpIds());
        }
        return this.jdbcTemplate.queryForObject(sql, params, Integer.class);
    }
}

