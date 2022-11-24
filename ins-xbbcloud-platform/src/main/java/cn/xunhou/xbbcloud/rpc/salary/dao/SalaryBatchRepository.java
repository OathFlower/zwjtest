
package cn.xunhou.xbbcloud.rpc.salary.dao;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.SqlUtil;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryBatchEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.SalaryBatchPageParam;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryBatchResult;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author system
 */
@Repository
public class SalaryBatchRepository extends XbbRepository<SalaryBatchEntity> {

    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    public SalaryBatchRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    public PagePojoList<SalaryBatchResult> findSalaryBatchPageList(SalaryBatchPageParam param) {
        @Language("sql") String where = " where   sb.deleted_flag = 0  " +
                (StringUtils.isBlank(param.getProductName()) ? " " : " and sp.name like CONCAT('%',:productName,'%') ") +
                (param.getStatus() == null ? " " : " and sb.`status` = :status ") +
                (param.getTenantId() == null ? " " : " and sb.tenant_id =:tenantId  ") +
                (param.getBatchId() == null ? " " : " and sb.id = :batchId ") +
                (param.isOperation() ? " and sb.source = 1  " : " ") +
                (CollUtil.isEmpty(param.getStatusList()) ? " " : "  and sb.`status` in (:statusList) ") +
                (CollUtil.isEmpty(param.getBatchIdList()) ? " " : "  and sb.id in (:batchIdList) ") +
                (param.getPayMethod() == null ? " " : " and sb.pay_method = :payMethod ") +
                (param.getSubjectId() == null ? " " : " and sb.subject_id = :subjectId ") +
                (param.getStartSubmitTime() == null ? " " : " and sb.created_at >= :startSubmitTime ") +
                (param.getEndSubmitTime() == null ? " " : " and sb.created_at <= :endSubmitTime ") +

                (CollUtil.isEmpty(param.getDeductionStatusList()) ? " " : " and fdt.deductionStatus in (:deductionStatus) ");
        @Language("sql") String orderBy = " ORDER BY sb.created_at DESC ";
        @Language("sql") String sql = "SELECT sb.*,sb.id as batch_id," +
                "(SELECT count(DISTINCT(sd.id_card_no)) FROM xbbcloud.salary_detail sd where sd.batch_id = sb.id and sd.`status`  not in (3,7)) as peopleCount," +
                " (SELECT SUM(sd.payable_amount)  FROM xbbcloud.salary_detail sd where sd.batch_id = sb.id and sd.`status` not in (3,7)) as payableAmount," +
                "(SELECT SUM(sd.service_amount)  FROM xbbcloud.salary_detail sd where sd.batch_id = sb.id and sd.`status` not in (3,7)) as serviceAmount," +
                "sp.name as productName ," +
                " ifnull(fdt.deductionStatus,0) deductionStatus," +
                " fdt.deductionFailureReason " +
                "from xbbcloud.salary_batch sb left join  xbbcloud.salary_product sp on sb.product_id = sp.id " +
                " left join ( " +
                " SELECT transaction_main,CASE when find_in_set('40',dispatch_status) > 0 then 2 " +
                " when find_in_set('20',dispatch_status) > 0 then 0 " +
                " when find_in_set('10',dispatch_status) > 0 then 0 " +
                " when find_in_set('11',dispatch_status) > 0 then 0 " +
                " else 1 end deductionStatus, deductionFailureReason from ( " +
                " SELECT fd.transaction_main ,GROUP_CONCAT(fd.dispatch_status) dispatch_status,GROUP_CONCAT(fd.failure_reason) deductionFailureReason   from fund_dispatching fd WHERE  fd.deleted_flag  = 0 " +
                (param.getBatchId() == null ? " " : " and fd.transaction_main = :batchId ") +
                " group by fd.transaction_main ) fdx " +
                " ) fdt on sb.id = fdt.transaction_main "
                + where + orderBy;
        return SqlUtil.pagePojoList(jdbcTemplate, sql, param, SalaryBatchResult.class, param.getPage(), param.getPageSize());
    }
}

