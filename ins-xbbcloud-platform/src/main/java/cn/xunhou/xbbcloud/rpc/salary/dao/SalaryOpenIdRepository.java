
package cn.xunhou.xbbcloud.rpc.salary.dao;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.cloud.core.json.XbbCamelJsonUtil;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.cloud.dao.xhjdbc.XbbSqlBuilder;
import cn.xunhou.cloud.framework.util.DesPlus;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryOpenIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author system
 */
@Repository
@Slf4j
public class SalaryOpenIdRepository extends XbbRepository<SalaryOpenIdEntity> {


    public SalaryOpenIdRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }


    /**
     * 根据idCard查询openId
     *
     * @param idCard
     * @return
     */
    public SalaryOpenIdEntity queryByIdCard(String idCard) {
        log.info("根据idCard查询openId参数:" + XbbCamelJsonUtil.toJsonString(idCard));
        List<SalaryOpenIdEntity> salaryOpenIdEntityList = queryForObjects(XbbSqlBuilder.newInstance()
                .and(StringUtils.isNotBlank(idCard), "id_card_no = ?", DesPlus.getInstance().encrypt(idCard))
                .order("created_at desc"), SalaryOpenIdEntity.class);
        if (CollUtil.isEmpty(salaryOpenIdEntityList)) {
            return null;
        }
        return salaryOpenIdEntityList.get(0);
    }

}

