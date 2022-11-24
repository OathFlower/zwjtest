
package cn.xunhou.xbbcloud.rpc.other.dao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.utils.IAssert;
import cn.xunhou.xbbcloud.rpc.other.entity.DictionaryEntity;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author system
 */
@Repository
public class DictionaryRepository extends XbbRepository<DictionaryEntity> {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public DictionaryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }


    /**
     * 字典列表
     *
     * @param tenantId 租户id
     * @param type     字典类型
     * @return 结果
     */
    @NonNull
    public List<DictionaryEntity> findList(@Nullable Integer tenantId,
                                           @NonNull Integer type,
                                           @Nullable String name) {
        IAssert.notNull(type, "字典类型不能为空");

        Map<String, Object> params = new HashMap<>(4);
        List<Integer> tenantIds = new ArrayList<>();
        tenantIds.add(0);

        //如果没传租户id,则查找系统默认的字典
        if (tenantId != null) {
            tenantIds.add(tenantId);
        }
        if (StringUtils.isNotBlank(name)) {
            params.put("name", name);
        }
        params.put("tenantIds", tenantIds);
        params.put("type", type);
        @Language("sql") String sql = "select * from dictionary where tenant_id in (:tenantIds) and type = :type and deleted_flag = 0";

        return Opt.ofEmptyAble(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(DictionaryEntity.class))).orElse(new ArrayList<>());
    }

    /**
     * 根据id查找
     *
     * @param id 排班id
     * @return 结果
     */
    @Nullable
    public DictionaryEntity findOneById(@NonNull Long id) {
        @Language("sql") String sql = "select * from dictionary where id = :id and deleted_flag = 0";
        Map<String, Object> params = new HashMap<>(2);
        params.put("id", id);
        return CollUtil.getFirst(this.jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(DictionaryEntity.class)));
    }
}

