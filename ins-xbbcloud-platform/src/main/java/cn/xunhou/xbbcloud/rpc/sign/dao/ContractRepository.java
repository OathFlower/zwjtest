
package cn.xunhou.xbbcloud.rpc.sign.dao;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.SqlUtil;
import cn.xunhou.xbbcloud.rpc.sign.entity.ContractEntity;
import cn.xunhou.xbbcloud.rpc.sign.pojo.param.ContractPageParam;
import cn.xunhou.xbbcloud.rpc.sign.pojo.result.ContractResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
@Slf4j
public class ContractRepository extends XbbRepository<ContractEntity> {


    public ContractRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    public PagePojoList<ContractResult> contractList(ContractPageParam param) {


        @Language("sql") String where = " where c.deleted_flag = 0  " +
                (StringUtils.isBlank(param.getIdCardNo()) ? " " : " and c.id_card_no = :idCardNo ") +
                (param.getSourceBusinessId() == null ? " " : " and c.source_business_id = :sourceBusinessId ") +
                (param.getEmployeeId() == null ? " " : " and c.employee_id = :employeeId ") +
                (param.getSource() == null ? " " : " and c.source = :source ") +
                (param.getType() == null ? " " : " and c.type = :type ") +
                (CollUtil.isEmpty(param.getIds()) ? " " : "  and c.id in (:ids) ") +
                (CollUtil.isEmpty(param.getStatusList()) ? " " : "  and c.`status` in (:statusList) ") +
                (CollUtil.isEmpty(param.getEmployeeIds()) ? " " : "  and c.employee_id in (:employeeIds) ") +
                (CollUtil.isEmpty(param.getIdCardNos()) ? " " : "  and c.id_card_no in (:idCardNos) ") +
                (CollUtil.isEmpty(param.getExcludeIds()) ? " " : "  and c.id not in (:excludeIds) ") +
                (CollUtil.isEmpty(param.getTypes()) ? " " : "  and c.type in (:types) ") +
                (param.getId() == null ? " " : " and c.id = :id ") +
                (param.getTenantId() == null ? " " : " and c.tenant_id = :tenantId ") +
                (param.getStatus() == null ? " " : " and c.`status` = :status ");

        @Language("sql") String orderBy = " order by c.id desc ";
        @Language("sql") String sql = "SELECT c.*" +
                " FROM xbbcloud.contract c " + where + orderBy;
        return SqlUtil.pagePojoList(jdbcTemplate, sql, param, ContractResult.class, param.getPage(), param.getPageSize());


    }
}

