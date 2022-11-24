package cn.xunhou.web.xbbcloud.config;

import cn.xunhou.cloud.core.datasource.IXbbDataSourceProperties;
import cn.xunhou.cloud.dao.configuration.JdbcTemplateProperties;
import cn.xunhou.cloud.dao.configuration.XbbJdbcTemplateFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author sha.li asdasd
 * @since 2022-6-21
 */
@Configuration
@EnableTransactionManagement
@Slf4j
public class JdbcConfiguration {
    public final static String DATA_SOURCE_JDBC = "xbbcloud";

    @Bean(name = DATA_SOURCE_JDBC)
    public NamedParameterJdbcTemplate jdbcTemplate(IXbbDataSourceProperties xbbDataSourceProperties, JdbcTemplateProperties jdbcTemplateProperties) {
        return new NamedParameterJdbcTemplate(new XbbJdbcTemplateFactory().buildPrimaryMysqlDataSource("xbbcloud", xbbDataSourceProperties, jdbcTemplateProperties));
    }

    @Bean
    public DataSourceTransactionManager transactionManager(NamedParameterJdbcTemplate jdbcTemplate) {
        //noinspection ConstantConditions
        return new DataSourceTransactionManager(jdbcTemplate.getJdbcTemplate().getDataSource());
    }
}

