package cn.xunhou.web.xbbcloud.product.sxz.dao;

import cn.xunhou.cloud.core.page.PageInfo;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.cloud.dao.xhjdbc.XbbSqlBuilder;
import cn.xunhou.web.xbbcloud.product.sxz.dto.ReceiptRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.entity.ReceiptEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
@Slf4j
public class ReceiptRepository extends XbbRepository<ReceiptEntity> {


    public ReceiptRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

    private static final String RECEIPT_RECORD = "receipt.id,u.tel as tel,(select SUM(o.payment_fee) from `order` o where o.receipt_id = receipt.id) as totalFee,receipt.customer_name as customerName,receipt.tax_no as taxNo,receipt.address as address,receipt.created_at as createdAt";

    public PagePojoList<ReceiptRecordResult> record(PageInfo param) {
        XbbSqlBuilder builder = XbbSqlBuilder.newInstanceWithTotal()
                .select(RECEIPT_RECORD)
                .join("left join user u on receipt.user_id = u.id")
                .order("receipt.id DESC");
        if (param.isPaged()) {
            builder.page(param.getCurPage(), param.getPageSize());
        }
        return super.queryForObjectPage(builder, ReceiptRecordResult.class);

    }
}
