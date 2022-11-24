package cn.xunhou.web.xbbcloud.product.sxz.dao;

import cn.hutool.core.collection.CollectionUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.json.XbbCamelJsonUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.cloud.dao.xhjdbc.XbbSqlBuilder;
import cn.xunhou.cloud.dao.xhjdbc.XbbSqlProperty;
import cn.xunhou.web.xbbcloud.product.sxz.dto.ConsumeRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.RechargeRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.entity.VirtualFlowEntity;
import cn.xunhou.web.xbbcloud.product.sxz.param.ConsumeRecordPageParam;
import cn.xunhou.web.xbbcloud.product.sxz.param.RechargeRecordPageParam;
import cn.xunhou.web.xbbcloud.product.sxz.enums.VirtualFlowStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
@Slf4j
public class VirtualFlowRepository extends XbbRepository<VirtualFlowEntity> {


    public VirtualFlowRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }
    private static final String RECHARGE_RECORD = "virtual_flow.coin as coin,o.id as orderId,o.payment_fee as paymentFee,virtual_flow.created_at as created_at";
    private static final String CONSUME_RECORD = "virtual_flow.coin as coin,s.title as title,virtual_flow.created_at as created_at";
    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();
    /**
     * 分页查询充值列表
     *
     * @param param
     * @return
     */
    public PagePojoList<RechargeRecordResult> rechargePageList(RechargeRecordPageParam param) {
        log.info("分页查询充值列表 参数:" + XbbCamelJsonUtil.toJsonString(param));
      /*  Map<String, Object> params = new HashMap<>();
        params.put("id", param.getOrderIds());*/

        XbbSqlProperty.Builder orderIdsBuilder = new XbbSqlProperty.Builder();
        orderIdsBuilder.OfInLong(CollectionUtil.isNotEmpty(param.getOrderIds()), "o.id", param.getOrderIds());
        XbbSqlBuilder builder = XbbSqlBuilder.newInstanceWithTotal()
                .select(RECHARGE_RECORD)
                .join("left join `order` o on virtual_flow.object_id = o.id")
                .and("virtual_flow.flow_type = ?", VirtualFlowStatusEnum.RECHARGE.getCode())
                .and(orderIdsBuilder).and(CollectionUtil.isNotEmpty(param.getOrderIds()), "o.receipt_id > 0")
                .and(param.getUnReceiptFlag(), "o.receipt_id = 0")
                .and("virtual_flow.user_id = ?", XBB_USER_CONTEXT.get().getUserId())
                .order("virtual_flow.id DESC");
        if (param.isPaged()) {
            builder.page(param.getCurPage(), param.getPageSize());
        }
        return super.queryForObjectPage(builder, RechargeRecordResult.class);
    }

    public PagePojoList<ConsumeRecordResult> consumePageList(ConsumeRecordPageParam param) {


        log.info("分页查询消费列表 参数:" + XbbCamelJsonUtil.toJsonString(param));


        XbbSqlBuilder builder = XbbSqlBuilder.newInstanceWithTotal()
                .select(CONSUME_RECORD)
                .join("left join service_order s on virtual_flow.object_id = s.id")
                .and("virtual_flow.flow_type = ?", VirtualFlowStatusEnum.CONSUME.getCode())
                .and("virtual_flow.user_id = ?", XBB_USER_CONTEXT.get().getUserId())
                .order("virtual_flow.id DESC");
        if (param.isPaged()) {
            builder.page(param.getCurPage(), param.getPageSize());
        }
        return super.queryForObjectPage(builder, ConsumeRecordResult.class);
    }
}
