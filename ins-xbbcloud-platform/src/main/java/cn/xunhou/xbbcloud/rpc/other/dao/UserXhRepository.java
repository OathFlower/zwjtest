
package cn.xunhou.xbbcloud.rpc.other.dao;

import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.core.json.XbbCamelJsonUtil;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.rpc.other.entity.UserXhCEntity;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class UserXhRepository extends XbbRepository<UserXhCEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public UserXhRepository(@Qualifier(value = "userxh") NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    /**
     * 批量根据身份证查询已认证的UserXHC
     *
     * @param idCardNoList
     * @return
     */
    public List<UserXhCEntity> queryByIdCards(List<String> idCardNoList) {
        log.info("查询ServiceOrder queryServiceOrderList参数:" + XbbCamelJsonUtil.toJsonString(idCardNoList));
        @Language("sql") String sql = "select * from userxh.user_xh_c where id_card_no in (:idCardNoList) and real_name_cert_status = 1 and `status` = 0";
        Map<String, Object> params = new HashMap<>(1);
        params.put("idCardNoList", idCardNoList);
        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(UserXhCEntity.class))).orElse(new ArrayList<>());
    }

}

