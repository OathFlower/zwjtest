package cn.xunhou.web.xbbcloud.product.sxz.dao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.xunhou.cloud.core.json.XbbCamelJsonUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.cloud.dao.xhjdbc.XbbSqlBuilder;
import cn.xunhou.web.xbbcloud.product.sxz.dto.VerificationRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.entity.ServiceOrderEntity;
import cn.xunhou.web.xbbcloud.product.sxz.param.VerificationRecordParam;
import cn.xunhou.web.xbbcloud.product.sxz.enums.ServiceOrderStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@Slf4j
public class ServiceOrderRepository extends XbbRepository<ServiceOrderEntity> {


    public ServiceOrderRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    /**
     * 根据主键返回
     *
     * @param id
     * @return
     */
    public ServiceOrderEntity getById(@NonNull Long id) {
        List<ServiceOrderEntity> serviceOrderEntityList = queryForObjects(XbbSqlBuilder.newInstance()
                .and("id = ?", id), ServiceOrderEntity.class);
        if (CollUtil.isEmpty(serviceOrderEntityList)) {
            return null;
        }
        return serviceOrderEntityList.get(0);
    }

    /**
     * 通过条件查询
     *
     * @param param
     * @return
     */
    public List<ServiceOrderEntity> queryServiceOrderList(@NonNull ServiceOrderEntity param) {
        log.info("查询ServiceOrder queryServiceOrderList参数:" + XbbCamelJsonUtil.toJsonString(param));
        List<ServiceOrderEntity> serviceOrderEntityList = queryForObjects(XbbSqlBuilder.newInstance()
                .and(!ObjectUtil.isEmpty(param.getUserId()) && param.getUserId() > 0, "user_id = ?", param.getUserId())
                .and(!ObjectUtil.isEmpty(param.getProductId()) && param.getProductId() > 0, "product_id = ?", param.getProductId())
                .and(!ObjectUtil.isEmpty(param.getServiceType()), "service_type = ?", param.getServiceType()).and(!ObjectUtil.isEmpty(param.getStatus()), "status = ?", param.getStatus()), ServiceOrderEntity.class);
        if (CollUtil.isEmpty(serviceOrderEntityList)) {
            return null;
        }
        return serviceOrderEntityList;
    }

    private static final String VERIFICATION_RECORD = "service_order.coin as coin,service_order.title as title,service_order.customer_name as customerName,service_order.remark as remark,service_order.updated_at as updatedAt";

    public PagePojoList<VerificationRecordResult> verificationRecord(VerificationRecordParam param) {
        XbbSqlBuilder builder = XbbSqlBuilder.newInstanceWithTotal()
                .select(VERIFICATION_RECORD)
                .and("service_order.user_id = ?", param.getUserId())
                .and("service_order.status = ?", ServiceOrderStatusEnum.USED.getCode())
                .order("service_order.updated_at DESC");
        if (param.isPaged()) {
            builder.page(param.getCurPage(), param.getPageSize());
        }
        return super.queryForObjectPage(builder, VerificationRecordResult.class);
    }
}
