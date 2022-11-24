package cn.xunhou.web.xbbcloud.product.sxz.dao;

import cn.hutool.core.collection.CollUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.cloud.dao.xhjdbc.XbbSqlBuilder;
import cn.xunhou.cloud.framework.util.DesPlus;
import cn.xunhou.web.xbbcloud.product.sxz.dto.AccountMultipleResult;
import cn.xunhou.web.xbbcloud.product.sxz.entity.UserEntity;
import cn.xunhou.web.xbbcloud.product.sxz.param.AccountMultiplePageParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@Slf4j
public class UserRepository extends XbbRepository<UserEntity> {


    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }


    /**
     * 根据手机号返回用户
     *
     * @param tel
     * @return
     */
    public UserEntity getUserByMobile(@NonNull String tel) {
        List<UserEntity> userEntities = queryForObjects(XbbSqlBuilder.newInstance()
                .and("tel = ?", DesPlus.getInstance().encrypt(tel)), UserEntity.class);
        if (CollUtil.isEmpty(userEntities)) {
            return null;
        }
        return userEntities.get(0);
    }

    /**
     * 根据主键返回
     *
     * @param id
     * @return
     */
    public UserEntity getUserById(@NonNull Long id) {
        List<UserEntity> userEntities = queryForObjects(XbbSqlBuilder.newInstance()
                .and("id = ?", id), UserEntity.class);
        if (CollUtil.isEmpty(userEntities)) {
            return null;
        }
        return userEntities.get(0);
    }

    private static final String MULTIPLE_LIST = "user.id as id,user.tel as tel, (select SUM(v.coin) from virtual_flow v where v.flow_type = 0 and v.user_id =user.id) as rechargeCoinCount,(select SUM(o.payment_fee) from `order` o where o.wx_status = 'SUCCESS' and o.user_id =user.id) as paymentFeeCount,(select SUM(r.total_fee) from receipt r  where r.user_id =user.id) as receiptFeeCount ,user.coin as accountBalance";

    public PagePojoList<AccountMultipleResult> accountMultipleList(AccountMultiplePageParam param) {

        XbbSqlBuilder builder = XbbSqlBuilder.newInstanceWithTotal()
                .select(MULTIPLE_LIST)
                .order("user.id DESC");
        if (param.isPaged()) {
            builder.page(param.getCurPage(), param.getPageSize());
        }
        return super.queryForObjectPage(builder, AccountMultipleResult.class);

    }

}
