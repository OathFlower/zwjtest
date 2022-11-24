
package cn.xunhou.xbbcloud.rpc.salary.dao;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.common.utils.SqlUtil;
import cn.xunhou.xbbcloud.config.JdbcConfiguration;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantInfoEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.QuerySalaryMerchantInfo;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.SalaryMerchantInfoPageParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wkm
 */
@Slf4j
@Repository
public class SalaryMerchantInfoRepository extends XbbRepository<SalaryMerchantInfoEntity> {

    public SalaryMerchantInfoRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    public SalaryMerchantInfoEntity findById(@NonNull Long id) {
        @Language("sql") String sql = "select * from salary_merchant_info where id= :id";
        return SqlUtil.findById(jdbcTemplate, sql, SalaryMerchantInfoEntity.class, id, "id");
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public SalaryMerchantInfoEntity saveById(@NonNull SalaryMerchantInfoEntity entity) {
        if (entity.getId() != null) {
            SalaryMerchantInfoEntity oldEntity = findById(entity.getId());
            if (oldEntity == null) {
                insert(entity);
            } else {
                updateById(entity.getId(), entity);
            }
        } else {
            throw GrpcException.asRuntimeException("租户id不能为空");
//            insert(entity);
        }
        SalaryMerchantInfoEntity newEntity = findById(entity.getId());
        entity.setId(newEntity.getId());
        return newEntity;
    }

    public List<SalaryMerchantInfoEntity> query(QuerySalaryMerchantInfo param) {
        @Language("sql") String sql = "select * from salary_merchant_info ";
        @Language("sql") String where = " where deleted_flag = 0 " +
                (CollUtil.isEmpty(param.getTenantTypes()) ? " " : " and tenant_type in (:tenantTypes) ") +
                (CollUtil.isEmpty(param.getPayeeSubAccountIds()) ? " " : " and payee_sub_account_id in (:payeeSubAccountIds) ") +
                (CollUtil.isEmpty(param.getIds()) ? " " : " and id in (:ids) ") +
                "";
        sql = sql + where;
        return jdbcTemplate.query(sql, new BeanPropertySqlParameterSource(param), new BeanPropertyRowMapper<>(SalaryMerchantInfoEntity.class));
    }


    public PagePojoList<SalaryMerchantInfoEntity> queryPage(SalaryMerchantInfoPageParam param) {
        @Language("sql") String sql = "select * from salary_merchant_info ";
        @Language("sql") String where = " where deleted_flag = 0 " +
                (CollUtil.isEmpty(param.getTenantTypes()) ? " " : " and tenant_type = :tenantTypes ") +
                (CollUtil.isEmpty(param.getIds()) ? " " : " and id in (:ids) ") +
                "";
        sql = sql + where + " order by id ";
        return SqlUtil.pagePojoList(jdbcTemplate, sql, param, SalaryMerchantInfoEntity.class, param.getPage(), param.getPageSize());
    }
}

