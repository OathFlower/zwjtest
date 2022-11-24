package cn.xunhou.xbbcloud.rpc.approve.dao;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
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
 * @Author: chenning
 * @Date: 2022/09/27/16:02
 * @Description:
 */
@Repository
public class WorkflowInstanceRepository extends XbbRepository<WorkflowInstanceEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public WorkflowInstanceRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }


    @NonNull
    public List<WorkflowInstanceEntity> findList(@Nullable Long insId,
                                               @Nullable Integer status,
                                               @Nullable Integer runStatus,
                                               @Nullable Date startDate,
                                               @Nullable Date endDate,
                                               @Nullable Long applicantId,
                                                 @Nullable Long flowTempId) {
        IAssert.hasTrue("参数不能全部为空", insId != null, status != null, runStatus != null,
                startDate != null, endDate != null, applicantId != null);
        @Language("sql") String sql = "select * from workflow_instances where deleted_flag = 0";
        Map<String, Object> params = new HashMap<>(8);
        if (insId != null) {
            params.put("id", insId);
            sql = sql + " and id = :insId";
        }
        if (status != null) {
            params.put("status", status);
            sql = sql + " and status = :status";
        }
        if (runStatus != null) {
            params.put("runStatus", runStatus);
            sql = sql + " and run_status = :runStatus";
        }
        if (startDate != null) {
            String currentDate = DateUtil.format(startDate, "yyyy-MM-dd");
            params.put("currentDateStart", currentDate + " 00:00:00");
            sql = sql + " and :currentDateStart <= start_time";
        }
        if (endDate != null) {
            String currentDate = DateUtil.format(endDate, "yyyy-MM-dd");
            params.put("currentDateEnd", currentDate + " 23:59:59");
            sql = sql + " and start_time <= :currentDateEnd";
        }
        if (applicantId != null) {
            params.put("applicantId", applicantId);
            sql = sql + " and applicant_id = :applicantId";
        }
        if (flowTempId != null) {
            params.put("flowTempId", flowTempId);
            sql = sql + " and flow_temp_id = :flowTempId";
        }
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(WorkflowInstanceEntity.class)))
                .orElse(new ArrayList<>());
    }
}
