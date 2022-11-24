
package cn.xunhou.xbbcloud.rpc.salary.dao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.SqlUtil;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryDetailEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.SalaryDetailPageParam;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.SalaryDetailQueryListParam;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.OneTimeSalaryResult;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.PayCountByIdCardResult;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryDetailResult;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryDetailStatusCountResult;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author system
 */
@Repository
public class SalaryDetailRepository extends XbbRepository<SalaryDetailEntity> {
    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    public SalaryDetailRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    public List<OneTimeSalaryResult> getOneTimeSalary(List<String> idCardNos, Long subjectId) {
        @Language("sql") String sql = "SELECT id_card_no ,min(updated_at) as startTime " +
                "FROM xbbcloud.salary_detail WHERE deleted_flag =0 and status not in (3,7 )" +
                " and tenant_id = :tenantId " +
                " and subject_id = :subjectId " +
                " and id_card_no in(:idCardNo) " +
                " and updated_at >= CONCAT(:year,'-01-01 00:00:00') " +
                "and updated_at <= CONCAT(:year,'-12-31 23:59:59') " +
                " group by id_card_no ";
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("idCardNo", idCardNos);
        paramMap.put("tenantId", XBB_USER_CONTEXT.tenantId());
        paramMap.put("subjectId", subjectId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date date = new Date();
        paramMap.put("year", sdf.format(date));
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<>(OneTimeSalaryResult.class)))
                .orElse(new ArrayList<OneTimeSalaryResult>());
    }

    public List<SalaryDetailEntity> list(SalaryDetailQueryListParam param) {
        if (param.getBatchId() == null && param.getTenantId() == null) {
            if (XBB_USER_CONTEXT != null && XBB_USER_CONTEXT.get() != null && XBB_USER_CONTEXT.get().getTenantId() != null) {
                param.setTenantId(XBB_USER_CONTEXT.get().getTenantId().longValue());
            }

        }
        @Language("sql") String where = " where sd.deleted_flag = 0  " +

                (param.getBatchId() == null ? " " : " and sd.batch_id = :batchId ") +
                (param.getTenantId() == null ? " " : " and sd.tenant_id = :tenantId ");

        @Language("sql") String orderBy = " order by sd.created_at desc ";
        @Language("sql") String sql = "SELECT sd.*" +
                " FROM xbbcloud.salary_detail sd " + where + orderBy;
        return SqlUtil.queryList(jdbcTemplate, sql, param, SalaryDetailEntity.class);
    }

    public PagePojoList<SalaryDetailResult> findSalaryDetailPageList(SalaryDetailPageParam param) {

        @Language("sql") String where = " where sd.deleted_flag = 0  " +
                (StringUtils.isBlank(param.getProductName()) ? " " : " and sp.name like CONCAT('%',:productName,'%') ") +
                (StringUtils.isBlank(param.getStaffName()) ? " " : " and sd.name  like CONCAT('%',:staffName,'%') ") +
                (StringUtils.isBlank(param.getPhone()) ? " " : " and sd.phone = :phone ") +
                (StringUtils.isBlank(param.getIdCardNo()) ? " " : " and sd.id_card_no = :idCardNo ") +
                (CollUtil.isEmpty(param.getDetailStatus()) ? " " : "  and sd.`status` in (:detailStatus) ") +
                (CollUtil.isEmpty(param.getTenantIdList()) ? " " : "  and sd.tenant_id in (:tenantIdList) ") +
                (CollUtil.isEmpty(param.getIdCardNoList()) ? " " : "  and sd.id_card_no in (:idCardNoList) ") +
                (CollUtil.isEmpty(param.getBatchIdList()) ? " " : "  and sd.batch_id in (:batchIdList) ") +
                (CollUtil.isEmpty(param.getNotInDetailStatus()) ? " " : "  and sd.`status`  not in (:detailStatus) ") +
                (CollUtil.isEmpty(param.getIds()) ? " " : "  and sd.id in (:ids) ") +
                (param.isOperation() ? " and sb.source = 1  " : " ") +
                (param.getBatchId() == null ? " " : " and sd.batch_id = :batchId ") +
                (param.getId() == null ? " " : " and sd.id = :id ") +
                (param.getTenantId() == null ? " " : " and sb.tenant_id = :tenantId ") +
                (param.getStartSubmitTime() == null ? " " : " and sd.created_at >= :startSubmitTime ") +
                (param.getEndSubmitTime() == null ? " " : " and sd.created_at <= :endSubmitTime ") +
                (param.getUpdateTimeStart() == null ? " " : " and sd.updated_at >= :updateTimeStart ") +
                (param.getUpdateTimeEnd() == null ? " " : " and sd.updated_at <= :updateTimeEnd ");
        @Language("sql") String orderBy = " order by sd.created_at desc ";
        @Language("sql") String sql = "SELECT sp.name as productName,sd.*,sb.month as salaryMonth,sd.id as detailId " +
                ",JSON_EXTRACT( fd.ext_info,'$.asset_detail_no') assetDetailNo " +
                " FROM xbbcloud.salary_detail sd left join xbbcloud.salary_batch sb on sd.batch_id = sb.id left JOIN xbbcloud.salary_product sp on sb.product_id = sp.id " +
                " LEFT JOIN xbbcloud.fund_dispatching fd on sb.id = fd.transaction_main " +
                " and fd.dispatch_direction = 1 " +
                " and fd.capital_type = 30 " +
                " AND fd.order_value = 1 " +
                " and fd.dispatch_status = 30 "
                + where + orderBy;
        return SqlUtil.pagePojoList(jdbcTemplate, sql, param, SalaryDetailResult.class, param.getPage(), param.getPageSize());
    }

    public List<PayCountByIdCardResult> getPayCountByIdCards(List<String> idCardNos, Long subjectId) {

        @Language("sql") String sql = "SELECT t.id_card_no as idCardNo, sum(t.tax_amount) as  totalTax, sum(t.payable_amount) as  totalPaidAble from( SELECT sd.id_card_no, sd.tax_amount, sd.payable_amount, YEAR(sd.updated_at) y " +
                " FROM xbbcloud.salary_detail sd where sd.deleted_flag = 0 and sd.status not in (3,7) and sd.id_card_no in(:idCardNo) and sd.tenant_id = :tenantId and sd.subject_id = :subjectId HAVING y = :year ) t" +
                " group BY t.id_card_no";
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("idCardNo", idCardNos);
        paramMap.put("tenantId", XBB_USER_CONTEXT.tenantId());
        paramMap.put("subjectId", subjectId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date date = new Date();
        paramMap.put("year", sdf.format(date));
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<>(PayCountByIdCardResult.class)))
                .orElse(new ArrayList<PayCountByIdCardResult>());
    }
    public SalaryDetailStatusCountResult getCountStatusInfo(Long batchId) {
        @Language("sql") String sql = "SELECT count(case when status = 0 then 1 else null end) as payingNotAuthCount,\n" +
                "count(case when status = 1 then 1 else null end) as payingAlreadyHandleCount,\n" +
                "count(case when status = 2 then 1 else null end) as alreadyPaidCount,\n" +
                "count(case when status = 4 then 1 else null end) as waitWithdrawCount,\n" +
                "count(case when status = 5 then 1 else null end) as withdrawing,\n" +
                "count(case when status = 6 then 1 else null end) as canceling,\n" +
                "count(case when status = 7 then 1 else null end) as cancelledCount,\n" +
                "count(case when status = 8 then 1 else null end) as cancelFailed,\n" +
                "count(case when status = 9 then 1 else null end) as withdrawFailed,\n" +
                "count(case when status = 10 then 1 else null end) as withdrawSuccess,\n" +
                "count(case when status = 3 then 1 else null end) as payFailCount FROM xbbcloud.salary_detail WHERE batch_id = :batchId  ";
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("batchId", batchId);
        return CollUtil.getFirst(this.jdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<>(SalaryDetailStatusCountResult.class)));
    }


    public void updateByDetailIds(List<Long> ids, String openId) {


        @Language("sql") String sql = "update salary_detail set status = 1 , open_id = :openId where id in (:ids) ";
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        params.put("openId", openId);
        this.jdbcTemplate.update(sql, params);


    }

    public void updateDetailStatus(List<Long> ids, Integer status, Boolean retryCount) {
        @Language("sql") String sql = "update salary_detail set status = :status " +
                (retryCount ? " , retry_count = retry_count + 1 " : " ") +
                " where id in (:ids) ";
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        params.put("status", status);
        this.jdbcTemplate.update(sql, params);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public List<Number> batchInsert(Collection<SalaryDetailEntity> list) {
        if (list.size() > 1000) {
            throw new RuntimeException("最大长度，不能>1000");
        } else {
            List<Number> items = new ArrayList();
            list.forEach((t) -> {
                items.add(this.insert(t));
            });
            return items;
        }
    }
}

