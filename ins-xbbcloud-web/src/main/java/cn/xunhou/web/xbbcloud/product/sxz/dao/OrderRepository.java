package cn.xunhou.web.xbbcloud.product.sxz.dao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.xunhou.cloud.core.json.XbbCamelJsonUtil;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.cloud.dao.xhjdbc.XbbSqlBuilder;
import cn.xunhou.web.xbbcloud.product.sxz.entity.OrderEntity;
import cn.xunhou.web.xbbcloud.product.sxz.enums.WxTradeStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;


@Repository
@Slf4j
public class OrderRepository extends XbbRepository<OrderEntity> {


    public OrderRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    /**
     * 根据id批量更新发票id
     *
     * @param ids
     * @param receiptId
     * @return
     */
    @Transactional
    public void batchUpdateReceipt(@NonNull List<Long> ids, Long receiptId) {
        for (Long id :
                ids) {
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setReceiptId(receiptId);
            this.updateById(id, orderEntity);
        }

    }

    /**
     * 根据主键id查询订单
     *
     * @param id
     * @return
     */
    public OrderEntity queryOrderById(@NonNull Long id) {
        log.info("根据主键id查询订单参数:" + XbbCamelJsonUtil.toJsonString(id));
        List<OrderEntity> orderEntityList = queryForObjects(XbbSqlBuilder.newInstance()
                .and(!ObjectUtil.isEmpty(id) && id > 0, "id = ?", id), OrderEntity.class);
        if (CollUtil.isEmpty(orderEntityList)) {
            return null;
        }
        return orderEntityList.get(0);
    }

    /**
     * 通过条件查询
     *
     * @param param
     * @return
     */
    public List<OrderEntity> queryOrderList(@NonNull OrderEntity param) {
        log.info("查询Order queryOrderList参数:" + XbbCamelJsonUtil.toJsonString(param));
        List<OrderEntity> orderEntityList = queryForObjects(XbbSqlBuilder.newInstance()
                .and(!ObjectUtil.isEmpty(param.getUserId()) && param.getUserId() > 0, "user_id = ?", param.getUserId())
                .and(StringUtils.isNotBlank(param.getWxStatus()), "wx_status = ?", param.getWxStatus())
                .and(!ObjectUtil.isEmpty(param.getProductId()) && param.getProductId() > 0, "product_id = ?", param.getProductId()), OrderEntity.class);
        if (CollUtil.isEmpty(orderEntityList)) {
            return null;
        }
        return orderEntityList;
    }


    public List<OrderEntity> getNoPayOrderByDuration(int minutes) {

        Instant instant = Instant.now().minus(Duration.ofMinutes(minutes));
        List<OrderEntity> orderEntityList = queryForObjects(XbbSqlBuilder.newInstance()
                .and("wx_status = ?", WxTradeStateEnum.NOTPAY.getType())
                .and("createdAt <= ?", instant), OrderEntity.class);
        if (CollUtil.isEmpty(orderEntityList)) {
            return null;
        }
        return orderEntityList;
    }

}
