
package cn.xunhou.xbbcloud.rpc.sign.dao;

import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.common.utils.SqlUtil;
import cn.xunhou.xbbcloud.config.JdbcConfiguration;
import cn.xunhou.xbbcloud.rpc.sign.entity.SignInfoEntity;
import cn.xunhou.xbbcloud.rpc.sign.entity.SignRelationProjectEntity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wkm
 */
@Slf4j
@Repository
public class SignInfoRepository extends XbbRepository<SignInfoEntity> {
    @Resource
    private SignRelationProjectRepository signRelationProjectRepository;

    public SignInfoRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }
    public SignInfoEntity findById(@NonNull Long id) {
        @Language("sql") String sql = "select * from sign_info where id= :id";
        return SqlUtil.findById(jdbcTemplate, sql, SignInfoEntity.class, id, "id");
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void save(SignInfoEntity entity,List<SignRelationProjectEntity> relationProjectEntityList){
        if (entity.getId() != null) {
            SignInfoEntity oldEntity = findById(entity.getId());
            if (oldEntity == null) {
                insert(entity);
            } else {
                updateById(entity.getId(), entity);
            }
        } else {
            throw GrpcException.asRuntimeException("租户id不能为空");
//            insert(entity);
        }
        signRelationProjectRepository.batchSave(entity.getId(), relationProjectEntityList);
    }
}

