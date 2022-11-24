
package cn.xunhou.xbbcloud.rpc.sign.dao;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.exception.XbbCloudException;
import cn.xunhou.xbbcloud.common.utils.SqlUtil;
import cn.xunhou.xbbcloud.config.JdbcConfiguration;
import cn.xunhou.xbbcloud.rpc.sign.entity.SignRelationProjectEntity;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wkm
 */
@Slf4j
@Repository
public class SignRelationProjectRepository extends XbbRepository<SignRelationProjectEntity> {

    public SignRelationProjectRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    /**
     * 过滤项目数据插入
     * @param relationProjectEntityList  项目关联关系
     * @param signInfoId
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void batchSave(@NonNull Long signInfoId,List<SignRelationProjectEntity> relationProjectEntityList) {
        if(CollUtil.isNotEmpty(relationProjectEntityList)){
            Map<String,SignRelationProjectEntity> relationProjectEntityMap = new HashMap<>();
            for (SignRelationProjectEntity relationProjectEntity: relationProjectEntityList){
                if (!signInfoId.equals(relationProjectEntity.getSignInfoId())) {
                    throw new XbbCloudException("只允许操作同一租户信息");
                }
                relationProjectEntityMap.put(
                        relationProjectEntity.getSignInfoId() + "-" + relationProjectEntity.getProjectId()
                ,relationProjectEntity);
            }
            batchDeleteSignInfoId(signInfoId);
            batchInsert(relationProjectEntityMap.values());
        }
    }

    /**
     * 批量删除
     * @param signInfoId 租户id ｜ 签约云id
     */
    public void batchDeleteSignInfoId(@NonNull Long signInfoId){
        @Language("sql") String sql = "delete from sign_relation_project where sign_info_id= :signInfoId ";
        Map<String,Long> param = Maps.newHashMap();
        param.put("signInfoId",signInfoId);
        jdbcTemplate.update(sql,param);
    }


    public List<SignRelationProjectEntity> findProjectInfoBySignId(Long signInfoId){
        @Language("sql") String sql = "select * from sign_relation_project where sign_info_id= :signInfoId and deleted_flag = 0";
        Map<String,Long> param = Maps.newHashMap();
        param.put("signInfoId",signInfoId);
        return SqlUtil.queryList(jdbcTemplate, sql,param, SignRelationProjectEntity.class);
    }
}

