package cn.xunhou.xbbcloud.rpc.approve.dao;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.ConvertUtil;
import cn.xunhou.xbbcloud.common.utils.SqlUtil;
import cn.xunhou.xbbcloud.rpc.approve.bean.WorkflowCountResult;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowFormEntity;
import cn.xunhou.xbbcloud.rpc.approve.entity.WorkflowInstanceEntity;
import cn.xunhou.xbbcloud.rpc.approve.param.WorkflowFormQueryParam;
import cn.xunhou.xbbcloud.rpc.approve.bean.WorkflowPageResult;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: chenning
 * @Date: 2022/09/27/16:04
 * @Description:
 */
@Repository
public class WorkflowFormRepository extends XbbRepository<WorkflowFormEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public WorkflowFormRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }


    public WorkflowFormEntity findByInsId(@NonNull Long insId) {
        @Language("sql") String sql = "select * from workflow_form_template where ins_id = :insId";
        Map<String, Object> params = new HashMap<>(1);
        params.put("insId", insId);

        return this.jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(WorkflowFormEntity.class));
    }

    public PagePojoList<WorkflowPageResult> findList(@NonNull WorkflowFormQueryParam param) {
        @Language("sql") String sql = "SELECT wft.v0,wft.v1,wft.v2,wft.v3,wft.v4,wft.v5," +
                "wft.v6,wft.v7,wft.v8,wft.v9,wft.ext,wi.applicant_id,wi.status,wi.approve_time,wft.form_json,wi.updated_at," +
                "wi.apply_time,wi.assignee_id,wi.reason,wi.id,wi.flow_temp_id,wi.edit_target_id,wi.tenant_id,wi.start_time" +
                " FROM `workflow_form_template`  wft LEFT JOIN workflow_instances wi \n" +
                "ON wft.ins_id = wi.id where wft.deleted_flag = 0";
        Map<String, Object> params = new HashMap<>(1);
        if (param.getInsId() != null) {
            sql = sql + " and wi.id = :insId";
            params.put("insId", param.getInsId());
        }
        if (!CollectionUtils.isEmpty(param.getFlowTemplateId())){
            sql = sql + " and wi.flow_temp_id in (:flowTemplateId)";
            params.put("flowTemplateId", param.getFlowTemplateId());
        }
        if (!CollectionUtils.isEmpty(param.getTenantIds())){
            sql = sql + " and wi.tenant_id in (:tenantIds)";
            params.put("tenantIds", param.getTenantIds());
        }
        if (!CollectionUtils.isEmpty(param.getEditTargetIds())) {
            sql = sql + " and wi.edit_target_id in (:editTargetIds)";
            params.put("editTargetIds", param.getEditTargetIds());
        }
        if (param.getStatus() != null) {
            sql = sql + " and wi.status = :status";
            params.put("status", param.getStatus());
        }
        if (!CollectionUtils.isEmpty(param.getStatuses())) {
            sql = sql + " and wi.status in (:status)";
            params.put("statuses", param.getStatuses());
        }
        if (param.getStartTime() != null) {
            sql = sql + " and wi.apply_time >= :startTime ";
            params.put("startTime", param.getStartTime());
        }
        if (param.getEndTime() != null) {
            sql = sql + " and wi.apply_time <= :endTime";
            params.put("endTime", param.getEndTime());
        }
        if (!CollectionUtils.isEmpty(param.getApplicantIds())) {
            sql = sql + " and wi.applicant_id in (:applicantIds)";
            params.put("applicantIds", param.getApplicantIds());
        }
        if (!CollectionUtils.isEmpty(param.getExcludeApplicantIds())) {
            sql = sql + " and wi.applicant_id not in (:excludeApplicantIds)";
            params.put("excludeApplicantIds", param.getExcludeApplicantIds());
        }
        if (param.getFlowStartTime() != null){
            sql = sql + " and wi.start_time <= :flowStartTime";
            params.put("flowStartTime", param.getFlowStartTime());
        }
        if (StringUtils.isNotBlank(param.getV0())) {
            if (ConvertUtil.isIndistinct(param.getV0())) {
                sql = sql + " and wft.v0 like CONCAT('%',:v0,'%')";
            }else {
                sql = sql + " and wft.v0 = :v0";
            }
            params.put("v0", param.getV0());
        }
        if (StringUtils.isNotBlank(param.getV1())) {
            if (ConvertUtil.isIndistinct(param.getV1())) {
                sql = sql + " and wft.v1 like CONCAT('%',:v1,'%')";
            }else {
                sql = sql + " and wft.v1 = :v1";
            }
            params.put("v1", param.getV1());
        }
        if (StringUtils.isNotBlank(param.getV2())) {
            if (ConvertUtil.isIndistinct(param.getV2())) {
                sql = sql + " and wft.v2 like CONCAT('%',:v2,'%')";
            }else {
                sql = sql + " and wft.v2 = :v2";
            }
            params.put("v2", param.getV2());
        }
        if (StringUtils.isNotBlank(param.getV3())) {
            if (ConvertUtil.isIndistinct(param.getV3())) {
                sql = sql + " and wft.v3 like CONCAT('%',:v3,'%')";
            }else {
                sql = sql + " and wft.v3 = :v3";
            }
            params.put("v3", param.getV3());
        }
        if (StringUtils.isNotBlank(param.getV4())) {
            if (ConvertUtil.isIndistinct(param.getV4())) {
                sql = sql + " and wft.v4 like CONCAT('%',:v4,'%')";
            }else {
                sql = sql + " and wft.v4 = :v4";
            }
            params.put("v4", param.getV4());
        }
        if (StringUtils.isNotBlank(param.getV5())) {
            if (ConvertUtil.isIndistinct(param.getV5())) {
                sql = sql + " and wft.v5 like CONCAT('%',:v5,'%')";
            }else {
                sql = sql + " and wft.v5 = :v5";
            }
            params.put("v5", param.getV5());
        }
        if (StringUtils.isNotBlank(param.getV6())) {
            if (ConvertUtil.isIndistinct(param.getV6())) {
                sql = sql + " and wft.v6 like CONCAT('%',:v6,'%')";
            }else {
                sql = sql + " and wft.v6 = :v6";
            }
            params.put("v5", param.getV5());
        }
        if (StringUtils.isNotBlank(param.getV7())) {
            if (ConvertUtil.isIndistinct(param.getV7())) {
                sql = sql + " and wft.v7 like CONCAT('%',:v7,'%')";
            }else {
                sql = sql + " and wft.v7 = :v7";
            }
            params.put("v7", param.getV7());
        }
        if (StringUtils.isNotBlank(param.getV8())) {
            if (ConvertUtil.isIndistinct(param.getV9())) {
                sql = sql + " and wft.v8 like CONCAT('%',:v9,'%')";
            }else {
                sql = sql + " and wft.v8 = :v8";
            }
            params.put("v8", param.getV8());
        }
        if (StringUtils.isNotBlank(param.getV9())) {
            if (ConvertUtil.isIndistinct(param.getV9())) {
                sql = sql + " and wft.v9 like CONCAT('%',:v9,'%')";
            }else if (param.getV9().contains(",")){
                String[] split = param.getV9().split(",");
                param.setValues(Arrays.asList(split));
                sql = sql + " and wft.v9 in (:values)";
            }else {
                sql = sql + " and wft.v9 = :v9";
            }
            params.put("v9", param.getV9());
        }
        if (StringUtils.isNotBlank(param.getExt())) {

            JSONObject jsonObject = JSON.parseObject(param.getExt());
            String jsonKey = "";
            Object jsonValue = "";
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                jsonKey = "$."+ entry.getKey();
                jsonValue = entry.getValue();
                sql = sql + " and wft.ext -> :jsonKey = :jsonValue";
            }
            param.setJsonKey(jsonKey);
            param.setJsonValue(jsonValue);
        }
        if (!CollectionUtils.isEmpty(param.getFilterInsIds())){
            sql = sql + " and wi.id in (:filterInsIds)";
            params.put("filterInsIds", param.getFilterInsIds());
        }
        if (StringUtils.isNotBlank(param.getSortField())){
            String sortField = param.getSortField();
            if ("createAtAsc".equals(sortField)){
                sql = sql + " order by wi.created_at ASC";
            }
            if ("createAtDesc".equals(sortField)){
                sql = sql + " order by wi.created_at DESC";
            }
            if ("approveTimeDesc".equals(sortField)){
                sql = sql + " order by wi.approve_time DESC";
            }
            if ("approveTimeAsc".equals(sortField)){
                sql = sql + " order by wi.approve_time ASC";
            }
        }else {
            sql = sql + " order by wi.created_at ASC";
        }
        return SqlUtil.pagePojoList(jdbcTemplate, sql, param, WorkflowPageResult.class, param.getCurPage(), param.getPageSize());
    }



    public List<WorkflowCountResult> findCount(@NonNull WorkflowFormQueryParam param) {
        @Language("sql") String sql = "SELECT wi.status AS status,count(*) AS count" +
                " FROM `workflow_form_template`  wft LEFT JOIN workflow_instances wi \n" +
                "ON wft.ins_id = wi.id where wft.deleted_flag = 0";
        Map<String, Object> params = new HashMap<>(1);
        if (param.getInsId() != null) {
            sql = sql + " and wi.id = :insId";
            params.put("insId", param.getInsId());
        }
        if (!CollectionUtils.isEmpty(param.getFlowTemplateId())){
            sql = sql + " and wi.flow_temp_id in (:flowTemplateId)";
            params.put("flowTemplateId", param.getFlowTemplateId());
        }
        if (!CollectionUtils.isEmpty(param.getTenantIds())){
            sql = sql + " and wi.tenant_id in (:tenantIds)";
            params.put("tenantIds", param.getTenantIds());
        }
        if (!CollectionUtils.isEmpty(param.getEditTargetIds())) {
            sql = sql + " and wi.edit_target_id in (:editTargetIds)";
            params.put("editTargetIds", param.getEditTargetIds());
        }
//        if (param.getStatus() != null) {
//            sql = sql + " and wi.status = :status";
//            params.put("status", param.getStatus());
//        }
        if (param.getStartTime() != null) {
            sql = sql + " and wi.approve_time >= :startTime ";
            params.put("startTime", param.getStartTime());
        }
        if (param.getEndTime() != null) {
            sql = sql + " and wi.approve_time <= :endTime";
            params.put("endTime", param.getEndTime());
        }
        if (!CollectionUtils.isEmpty(param.getApplicantIds())) {
            sql = sql + " and wi.applicant_id in (:applicantIds)";
            params.put("applicantIds", param.getApplicantIds());
        }
        if (!CollectionUtils.isEmpty(param.getExcludeApplicantIds())) {
            sql = sql + " and wi.applicant_id not in (:excludeApplicantIds)";
            params.put("excludeApplicantIds", param.getExcludeApplicantIds());
        }
        if (StringUtils.isNotBlank(param.getV0())) {
            sql = sql + " and wft.v0 like CONCAT('%',:v0,'%')";
            params.put("v0", param.getV0());
        }
        if (StringUtils.isNotBlank(param.getV1())) {
            sql = sql + " and wft.v1 = :v1";
            params.put("v1", param.getV1());
        }
        if (StringUtils.isNotBlank(param.getV2())) {
            sql = sql + " and wft.v2 = :v2";
            params.put("v2", param.getV2());
        }
        if (StringUtils.isNotBlank(param.getV3())) {
            sql = sql + " and wft.v3 = :v3";
            params.put("v3", param.getV3());
        }
        if (StringUtils.isNotBlank(param.getV4())) {
            sql = sql + " and wft.v4 = :v4";
            params.put("v4", param.getV4());
        }
        if (StringUtils.isNotBlank(param.getV5())) {
            sql = sql + " and wft.v5 = :v5";
            params.put("v5", param.getV5());
        }
        if (StringUtils.isNotBlank(param.getV6())) {
            sql = sql + " and wft.v6 = :v6";
            params.put("v6", param.getV6());
        }
        if (StringUtils.isNotBlank(param.getV7())) {
            sql = sql + " and wft.v7 = :v7";
            params.put("v7", param.getV7());
        }
        if (StringUtils.isNotBlank(param.getV8())) {
            sql = sql + " and wft.v8 = :v8";
            params.put("v8", param.getV8());
        }
        if (StringUtils.isNotBlank(param.getV9())) {
            sql = sql + " and wft.v9 = :v9";
            params.put("v9", param.getV9());
        }
        if (StringUtils.isNotBlank(param.getExt())) {
            sql = sql + " and wft.ext -> '$.v10' = :v10";
            JSONObject jsonObject = JSON.parseObject(param.getExt());
            params.put("v10", jsonObject.get("v10"));
        }
        if (!CollectionUtils.isEmpty(param.getFilterInsIds())){
            params.put("filterInsIds",param.getFilterInsIds());
            sql = sql + " and wi.id in (:filterInsIds)";
        }
        sql = sql + " GROUP BY wi.status";
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(WorkflowCountResult.class)))
                .orElse(new ArrayList<>());
    }
}
