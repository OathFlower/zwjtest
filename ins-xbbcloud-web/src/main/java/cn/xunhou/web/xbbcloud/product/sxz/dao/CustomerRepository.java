package cn.xunhou.web.xbbcloud.product.sxz.dao;

import cn.xunhou.cloud.core.json.XbbCamelJsonUtil;
import cn.xunhou.cloud.dao.xhjdbc.XbbRepository;
import cn.xunhou.web.xbbcloud.product.sxz.entity.CustomerEntity;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
@Slf4j
public class CustomerRepository extends XbbRepository<CustomerEntity> {


    public CustomerRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);

    }


    /**
     * 查重复
     *
     * @param param
     * @return
     */
    public List<CustomerEntity> queryRepList(@NonNull CustomerEntity param) {
        log.info("CustomerRepository查重复参数:" + XbbCamelJsonUtil.toJsonString(param));
        String sql = "SELECT * FROM xbbcloud.customer WHERE (tax_no = :taxNo or  customer_name = :customerName) and id != :id";
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("taxNo", param.getTaxNo());
        paramMap.put("customerName", param.getCustomerName());
        paramMap.put("id", param.getId());
        return jdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<CustomerEntity>(CustomerEntity.class));
    }

}
