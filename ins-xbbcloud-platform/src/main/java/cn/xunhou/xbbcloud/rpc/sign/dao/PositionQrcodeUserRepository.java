
package cn.xunhou.xbbcloud.rpc.sign.dao;

import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.rpc.sign.entity.PositionQrcodeUserEntity;
import cn.xunhou.xbbcloud.rpc.sign.pojo.param.QueryPositionUserParam;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
@Slf4j
public class PositionQrcodeUserRepository extends XbbRepository<PositionQrcodeUserEntity> {


    public PositionQrcodeUserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    public void updateByQrCodeId(long qrcodeId) {
        @Language("sql") String sql = "update position_qrcode_user set deleted_flag = 1  where position_qrcode_id =:position_qrcode_id ";
        Map<String, Object> params = new HashMap<>();
        params.put("position_qrcode_id", qrcodeId);
        this.jdbcTemplate.update(sql, params);
    }

    public List<PositionQrcodeUserEntity> list(QueryPositionUserParam param) {
        @Language("sql") String sql = "select * from position_qrcode_user ";
        @Language("sql") String where = " where deleted_flag = 0 " +
                (param.getPositionQrcodeId() == null ? " " : " and position_qrcode_id = :positionQrcodeId");
        sql = sql + where;
        return jdbcTemplate.query(sql, new BeanPropertySqlParameterSource(param), new BeanPropertyRowMapper<>(PositionQrcodeUserEntity.class));
    }
}

