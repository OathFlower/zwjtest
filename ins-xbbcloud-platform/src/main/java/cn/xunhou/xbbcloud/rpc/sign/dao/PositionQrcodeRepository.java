
package cn.xunhou.xbbcloud.rpc.sign.dao;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.SqlUtil;
import cn.xunhou.xbbcloud.rpc.sign.entity.PositionQrcodeEntity;
import cn.xunhou.xbbcloud.rpc.sign.pojo.param.PositionQrcodePageParam;
import cn.xunhou.xbbcloud.rpc.sign.pojo.result.PositionQrcodeResult;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
@Slf4j
public class PositionQrcodeRepository extends XbbRepository<PositionQrcodeEntity> {


    public PositionQrcodeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    public PagePojoList<PositionQrcodeResult> positionQrcodeList(PositionQrcodePageParam param) {

        @Language("sql") String where = " where pq.deleted_flag = 0  " +
                (CollUtil.isEmpty(param.getHroPositionIds()) ? " " : "  and pq.hro_position_id in (:hroPositionIds) ") +
                (param.getTenantId() == null ? " " : " and pq.tenant_id = :tenantId ") +
                (param.getCreateDateStart() == null ? " " : " and pq.created_at >= :createDateStart ") +
                (param.getCreateDateEnd() == null ? " " : " and pq.created_at <= :createDateEnd ");

        @Language("sql") String orderBy = " order by pq.id desc ";
        @Language("sql") String sql = "SELECT pq.*" +
                " FROM xbbcloud.position_qrcode pq " + where + orderBy;
        return SqlUtil.pagePojoList(jdbcTemplate, sql, param, PositionQrcodeResult.class, param.getPage(), param.getPageSize());
    }

    public void updateNotInPositionIds(List<Long> positionIds, Long tenantId) {

        @Language("sql") String sql = "update position_qrcode set expire_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY)   where tenant_id = :tenantId  " +
                (CollUtil.isEmpty(positionIds) ? " " : "  and hro_position_id  not in (:positionIds)   ");
        Map<String, Object> params = new HashMap<>();
        params.put("positionIds", positionIds);
        params.put("tenantId", tenantId);
        this.jdbcTemplate.update(sql, params);
    }

    public PositionQrcodeEntity findById(@NonNull Long id) {
        @Language("sql") String sql = "select * from position_qrcode where id= :id and deleted_flag = 0";
        return SqlUtil.findById(jdbcTemplate, sql, PositionQrcodeEntity.class, id, "id");
    }
}

