package cn.xunhou.web.xbbcloud.product.sxz.dao;

import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.web.xbbcloud.product.sxz.entity.WxPaymentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
@Slf4j
public class WxPaymentRepository extends XbbRepository<WxPaymentEntity> {


    public WxPaymentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }

}
