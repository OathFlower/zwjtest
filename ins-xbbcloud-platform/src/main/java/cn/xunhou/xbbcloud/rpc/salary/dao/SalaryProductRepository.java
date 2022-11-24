
package cn.xunhou.xbbcloud.rpc.salary.dao;

import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.json.XbbCamelJsonUtil;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryProductEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author system
 */
@Repository
@Slf4j
public class SalaryProductRepository extends XbbRepository<SalaryProductEntity> {
    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();
    public SalaryProductRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    /**
     * 条件查询项目
     *
     * @param name
     * @return
     */
    public List<SalaryProductEntity> queryByParam(String name) {
        log.info("条件查询项目参数:" + XbbCamelJsonUtil.toJsonString(name));
        @Language("sql") String sql = "select * from xbbcloud.salary_product where deleted_flag = 0 and tenant_id =:tenantId " +
                (StringUtils.isBlank(name) ? " " : " and name = :name ");
        Map<String, Object> params = new HashMap<>(1);
        params.put("name", name);
        params.put("tenantId", XBB_USER_CONTEXT.get().getTenantId());
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(SalaryProductEntity.class))).orElse(new ArrayList<>());
    }

}

