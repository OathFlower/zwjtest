package cn.xunhou.xbbcloud.rpc.approve.dao;

import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowNodeEntity;
import cn.xunhou.xbbcloud.rpc.approve.param.WorkflowNodeQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: chenning
 * @Date: 2022/09/27/16:05
 * @Description:
 */
@Repository
@Slf4j
public class WorkflowNodeRepository extends XbbRepository<WorkflowNodeEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public WorkflowNodeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<WorkflowNodeEntity> findByInsId(@NonNull Long insId) {
        @Language("sql") String sql = "select * from workflow_node where deleted_flag = 0 and ins_id = :insId";
        Map<String, Object> params = new HashMap<>(1);
        params.put("insId", insId);
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(WorkflowNodeEntity.class))).orElse(new ArrayList<>());
    }


    public List<WorkflowNodeEntity> findByAssigneeId(Long instanceId, @NonNull Long assigneeId, @NonNull int level) {
        @Language("sql") String sql = "select * from workflow_node where deleted_flag = 0" +
                " and ins_id = :insId and node_level = :level and assignee_id = :assigneeId";
        Map<String, Object> params = new HashMap<>(1);
        params.put("assigneeId", assigneeId);
        params.put("level", level);
        params.put("insId", instanceId);
        log.info("sql"+sql);
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(WorkflowNodeEntity.class))).orElse(new ArrayList<>());

    }


    public List<WorkflowNodeEntity> findList(WorkflowNodeQueryParam param) {
        @Language("sql") String sql = "select * from workflow_node where deleted_flag = 0";
        Map<String, Object> params = new HashMap<>(1);

        if (param.getInsId() != null) {
            sql = sql + " and ins_id = :insId";
            params.put("insId", param.getInsId());
        }
        if (param.getStatus() != null) {
            sql = sql + " and status = :status";
            params.put("status", param.getStatus());
        }
        if (param.getNodeLevel() != null) {
            sql = sql + " and node_level = :nodeLevel";
            params.put("nodeLevel", param.getNodeLevel());
        }
        if (param.getAllLevel()!= null) {
            sql = sql + " and node_level >= :allLevel";
            params.put("allLevel", param.getAllLevel());
        }
        if (param.getInsIds() != null) {
            sql = sql + " and ins_id in (:insIds)";
            params.put("insIds", param.getInsIds());
        }
        if (!CollectionUtils.isEmpty(param.getIds())){
            sql = sql + " and id in (:ids)";
            params.put("ids", param.getIds());
        }
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(WorkflowNodeEntity.class))).orElse(new ArrayList<>());
    }

    public void deleteByIds(List<Long> ids) {
        @Language("sql") String sql = "delete  from workflow_node where id in (:ids)";
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        this.jdbcTemplate.update(sql,params);
    }


}
