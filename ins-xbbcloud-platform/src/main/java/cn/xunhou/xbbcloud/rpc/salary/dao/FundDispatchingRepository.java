
package cn.xunhou.xbbcloud.rpc.salary.dao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.core.snow.SnowflakeIdGenerator;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.enums.EnumDispatchStatus;
import cn.xunhou.xbbcloud.common.utils.SqlUtil;
import cn.xunhou.xbbcloud.config.JdbcConfiguration;
import cn.xunhou.xbbcloud.rpc.salary.entity.FundDispatchingEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.QueryFundDispatchingParam;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wkm
 */
@Slf4j
@Repository
public class FundDispatchingRepository extends XbbRepository<FundDispatchingEntity> {

    public FundDispatchingRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<FundDispatchingEntity> query(QueryFundDispatchingParam param) {
        @Language("sql") String sql = "select * from fund_dispatching " +
                "where deleted_flag = 0 " +
                (param.getTenantId() != null ? " and tenant_id = :tenantId " : " ") +
                (CharSequenceUtil.isNotBlank(param.getTransactionMain()) ? " and transaction_main = :transactionMain " : " ") +
                (CollUtil.isNotEmpty(param.getTransactionMains()) ? " and transaction_main in (:transactionMains) " : " ") +
                (CollUtil.isNotEmpty(param.getIds()) ? " and id in (:ids) " : " ") +
                (CollUtil.isNotEmpty(param.getTransactionTypes()) ? " and transaction_type in (:transactionTypes) " : " ") +
                "";
        return SqlUtil.queryList(jdbcTemplate, sql, param, FundDispatchingEntity.class);
    }

    public FundDispatchingEntity findById(Long id) {
        @Language("sql") String sql = "select * from fund_dispatching where deleted_flag = 0 and id=:id ";
        return SqlUtil.findById(jdbcTemplate, sql, FundDispatchingEntity.class, id, "id");
    }

    public List<FundDispatchingEntity> findByIds(Collection<Long> ids) {
        @Language("sql") String sql = "select * from fund_dispatching where deleted_flag = 0 and id in (:id) ";
        return SqlUtil.findByIds(jdbcTemplate, sql, FundDispatchingEntity.class, ids, "id");
    }

    public void updateRetryStatus(Collection<Long> ids) {
        @Language("sql") String sql = "update fund_dispatching set dispatch_status = :status , retry_count = retry_count + 1 " +
                " where id in (:ids) ";
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        params.put("status", EnumDispatchStatus.INIT.getCode());
        this.jdbcTemplate.update(sql, params);
    }

    /**
     * 插入数据 无id则自动填入id
     *
     * @param fundDispatchingEntityList 待插入数据
     * @param transactionMain           穿起当前交易的键（服务费，税费，实发金额）
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void insertBatch(Collection<FundDispatchingEntity> fundDispatchingEntityList, String transactionMain) {
        if (CollUtil.isEmpty(fundDispatchingEntityList)) {
            return;
        }
        for (FundDispatchingEntity fundDispatchingEntity : fundDispatchingEntityList) {
            if (fundDispatchingEntity.getId() == null) {
                fundDispatchingEntity.setId(SnowflakeIdGenerator.getId());
            }
            fundDispatchingEntity.setTransactionMain(transactionMain);
            insert(fundDispatchingEntity);
        }
    }

    /**
     * 更新数据状态
     *
     * @param id             数据id
     * @param dispatchStatus 交易状态
     * @param failureReason  失败原因
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void updateState(Long id, EnumDispatchStatus dispatchStatus, String failureReason) {
        updateById(id, new FundDispatchingEntity()
                .setDispatchStatus(dispatchStatus.getCode())
                .setFailureReason(failureReason))
        ;
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void updateSuccessState(Long id, String assetDetailNo) {
        FundDispatchingEntity fundDispatching = findById(id);
        fundDispatching.setUpdatedAt(null);
        FundDispatchingEntity.ExtInfo extInfo;
        if (CharSequenceUtil.isNotBlank(fundDispatching.getExtInfo())) {
            extInfo = XbbJsonUtil.fromJsonString(fundDispatching.getExtInfo(), FundDispatchingEntity.ExtInfo.class);
        } else {
            extInfo = new FundDispatchingEntity.ExtInfo();
        }
        extInfo.setAssetDetailNo(assetDetailNo);
        updateById(id, new FundDispatchingEntity()
                .setDispatchStatus(EnumDispatchStatus.SUCCESSFUL.getCode())
                .setExtInfo(XbbJsonUtil.toJsonString(extInfo)));
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void updateState(Collection<Long> ids, EnumDispatchStatus dispatchStatus, String failureReason, Long assetTransactionId) {
        for (Long id : ids) {
            updateById(id, new FundDispatchingEntity()
                    .setDispatchStatus(dispatchStatus.getCode())
                    .setFailureReason(failureReason)
                    .setAssetTransactionId(assetTransactionId))
            ;
        }
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void updateState(Collection<FundDispatchingEntity> fundDispatchingEntityList) {
        for (FundDispatchingEntity fundDispatchingEntity : fundDispatchingEntityList) {
            updateById(fundDispatchingEntity.getId(), new FundDispatchingEntity()
                    .setDispatchStatus(fundDispatchingEntity.getDispatchStatus())
                    .setFailureReason(fundDispatchingEntity.getFailureReason()))
            ;
        }
    }
}

