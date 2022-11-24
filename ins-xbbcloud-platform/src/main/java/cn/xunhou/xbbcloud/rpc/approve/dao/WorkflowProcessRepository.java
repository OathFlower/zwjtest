package cn.xunhou.xbbcloud.rpc.approve.dao;

import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowProcessEntity;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotEmpty;
import java.util.*;

/**
 * @Author: chenning
 * @Date: 2022/09/27/16:03
 * @Description:
 */
@Repository
public class WorkflowProcessRepository extends XbbRepository<WorkflowProcessEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public WorkflowProcessRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<WorkflowProcessEntity> findByInsId(@NonNull Long insId,@Nullable Integer runStatus) {
        @Language("sql") String sql = "select * from workflow_process where ins_id = :insId";
        Map<String, Object> params = new HashMap<>(1);
        params.put("insId", insId);
        if (runStatus != null){
            params.put("runStatus", runStatus);
            sql = sql + " and run_status = :runStatus";
        }
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(WorkflowProcessEntity.class)))
                .orElse(new ArrayList<>());
    }

    public List<WorkflowProcessEntity> findList(List<Long> insIds) {
        @Language("sql") String sql = "select * from workflow_process  where 1=1";
        Map<String, Object> params = new HashMap<>();
        if (!CollectionUtils.isEmpty(insIds)) {
            params.put("insIds", insIds);
            sql = sql + " and ins_id in (:insIds)";
        }

        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(WorkflowProcessEntity.class)))
                .orElse(new ArrayList<>());
    }

    public List<Long> findInsIdByNodeLevel(@NonNull Integer approveNodeLevel,@NotEmpty List<Long> flowTemplateId) {
        @Language("sql") String sql = "SELECT ins_id FROM workflow_process WHERE flow_temp_id IN (:flowTemplateId) GROUP BY ins_id,node_type HAVING node_type =1 and COUNT(node_type)= :approveNodeLevel";
        Map<String, Object> params = new HashMap<>();
        params.put("approveNodeLevel",approveNodeLevel);
        params.put("flowTemplateId",flowTemplateId);
        return new ArrayList<>(Opt.ofEmptyAble(this.jdbcTemplate.queryForList(sql, params, Long.class))
                .orElse(new ArrayList<>()));
    }
}
