
package cn.xunhou.xbbcloud.rpc.salary.dao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.SqlUtil;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantFlowEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.QuerySalaryMerchantFlow;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.SalaryMerchantFlowPageParam;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryMerchantFlowResult;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author wkm
 */
@Slf4j
@Repository
public class SalaryMerchantFlowRepository extends XbbRepository<SalaryMerchantFlowEntity> {

    public SalaryMerchantFlowRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public SalaryMerchantFlowEntity findById(@NonNull Long id) {
        @Language("sql") String sql = "select * from salary_merchant_flow where id = :id";
        return SqlUtil.findById(jdbcTemplate, sql, SalaryMerchantFlowEntity.class, id, "id");
    }

    public SalaryMerchantFlowResult findByIdForResult(@NonNull Long id) {
        @Language("sql") String sql = "select t.*" +
                " ,case when t.operation_type = 1 " +
                " then 1 else t1.status end  tradingStatus" +
                " from salary_merchant_flow t" +
                " left join salary_batch t1 on t1.id = t.salary_batch_id and t1.tenant_id = t.tenant_id and t1.deleted_flag = t.deleted_flag " +
                " where t.id =  :id";
        return SqlUtil.findById(jdbcTemplate, sql, SalaryMerchantFlowResult.class, id, "id");
    }

    /**
     * 分页查询
     *
     * @param param 参数
     * @return 结果
     */
    public PagePojoList<SalaryMerchantFlowResult> findPageList(SalaryMerchantFlowPageParam param) {
        PagePojoList.PagePojoListBuilder<SalaryMerchantFlowResult> builder = PagePojoList.<SalaryMerchantFlowResult>builder();
        if (param.getTenantId() == null) {
            return builder.build();
        }
        @Language("sql") String where = " where t.deleted_flag = 0 and t.tenant_id = :tenantId " +
                (CollUtil.isEmpty(param.getFlowOperationTypes()) ? " " : " and t.operation_type in (:flowOperationTypes) ") +
                (param.getOperationAmount() == null ? " " : " and t.operation_amount = :operationAmount ") +
                (param.getOperatorId() == null ? " " : " and t.operator_id = :operatorId ") +
                (param.getStartTime() == null ? " " : " and t.created_at >= :startTime ") +
                (param.getEndTime() == null ? " " : " and t.created_at <= :endTime ") +
                (CharSequenceUtil.isBlank(param.getRemarks()) ? " " : " and t.remarks like CONCAT('%',:remarks,'%') ");

        @Language("sql") String limit = SqlUtil.buildLimit(param.getPage(), param.getPageSize());
        @Language("sql") String orderBy = " order by created_at desc ";
        @Language("sql") String sql = "select * from ( select " +
                "t.* " +
                " ,case when t.operation_type = 1 " +
                " then 1 else t1.status end  tradingStatus" +
                " from salary_merchant_flow t " +
                " left join salary_batch t1 on t1.id = t.salary_batch_id and t1.tenant_id = t.tenant_id and t1.deleted_flag = t.deleted_flag " +
                where +
                ") x " +
                (CollUtil.isEmpty(param.getTradingStatus()) ? " " : " where tradingStatus in (:tradingStatus) ");
        Map<String, Object> countMap = jdbcTemplate.queryForMap(SqlUtil.buildCount(sql), new BeanPropertySqlParameterSource(param));
        builder.total(MapUtil.getInt(countMap, "countValue"));
        sql = sql + orderBy + limit;
        log.debug("findPageList sql = {}", sql);
        List<SalaryMerchantFlowResult> resultList = jdbcTemplate.query(sql, new BeanPropertySqlParameterSource(param), new BeanPropertyRowMapper<>(SalaryMerchantFlowResult.class));
        builder.data(resultList);
        return builder.build();
    }

//    public PagePojoList<SalaryMerchantFlowResult> findPageList2(SalaryMerchantFlowPageParam param) {
//        PagePojoList.PagePojoListBuilder<SalaryMerchantFlowResult> builder = PagePojoList.<SalaryMerchantFlowResult>builder();
//        if (param.getTenantId() == null) {
//            return builder.build();
//        }
//        @Language("sql") String where = " where t.deleted_flag = 0 and t.tenant_id = :tenantId " +
//                (CollUtil.isEmpty(param.getFlowOperationTypes()) ? " " : " and t.operation_type in (:flowOperationTypes) ") +
//                (param.getOperationAmount() == null ? " " : " and t.operation_amount = :operationAmount ") +
//                (param.getOperatorId() == null ? " " : " and t.operator_id = :operatorId ") +
//                (param.getStartTime() == null ? " " : " and t.updated_at >= :startTime ") +
//                (param.getEndTime() == null ? " " : " and t.updated_at <= :endTime ") +
//                (CharSequenceUtil.isBlank(param.getRemarks()) ? " " : " and t.remarks like CONCAT('%',:remarks,'%') ");
//        @Language("sql") String orderBy = " order by updated_at desc ";
//        @Language("sql") String sql = "select * from ( select " +
//                "t.* " +
//                " ,case when t.operation_type = 1 " +
//                " then 1 else t1.status end  tradingStatus" +
//                " from salary_merchant_flow t " +
//                " left join salary_batch t1 on t1.id = t.salary_batch_id and t1.tenant_id = t.tenant_id and t1.deleted_flag = t.deleted_flag " +
//                where +
//                ") x " +
//                (CollUtil.isEmpty(param.getTradingStatus()) ? " " : " and tradingStatus in (:tradingStatus) ");
//        sql = sql + orderBy;
//        return SqlUtil.pagePojoList(jdbcTemplate, sql, param, SalaryMerchantFlowResult.class, param.getPage(), param.getPageSize());
//    }


    public List<SalaryMerchantFlowEntity> query(QuerySalaryMerchantFlow param) {
        @Language("sql") String sql = "select * from salary_merchant_flow ";
        @Language("sql") String where = " where deleted_flag = 0 " +
                ((param.getTenantId() == null || param.getTenantId() == 0) ? " " : " and tenant_id = :tenantId") +
                (CollUtil.isEmpty(param.getTenantIds()) ? " " : " and tenant_id in (:tenantIds)") +
                (CollUtil.isEmpty(param.getFlowNos()) ? " " : " and flow_no in (:flowNos)") +
                (CollUtil.isEmpty(param.getBatchIds()) ? " " : " and salary_batch_id in (:batchIds)");
        sql = sql + where;
        return jdbcTemplate.query(sql, new BeanPropertySqlParameterSource(param), new BeanPropertyRowMapper<>(SalaryMerchantFlowEntity.class));
    }


    public List<SalaryMerchantFlowEntity> queryPageList(QuerySalaryMerchantFlow param) {
        @Language("sql") String sql = "select * from salary_merchant_flow ";
        return SqlUtil.queryThreadPage(jdbcTemplate, sql, param, SalaryMerchantFlowEntity.class, 2);
    }
}

