package cn.xunhou.xbbcloud.config;

import cn.xunhou.cloud.core.datasource.IXbbDataSourceProperties;
import cn.xunhou.cloud.dao.configuration.JdbcTemplateProperties;
import cn.xunhou.cloud.dao.configuration.XbbJdbcTemplateFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author litb
 * @date 2022/9/7 14:00
 * <p>
 * jdbc配置
 */
@Configuration
@EnableTransactionManagement
public class JdbcConfiguration {

    public final static String XBB_CLOUD_DATA_SOURCE_JDBC = "xbbcloud";
    public final static String XBB_USER_XH_DATA_SOURCE_JDBC = "userxh";

    public final static String XBBCLOUD_TRANSACTION_MANAGER = "xbbcloud_transaction";

    public final static String USER_XH_TRANSACTION_MANAGER = "userxh_transaction";

    @Bean(name = XBB_CLOUD_DATA_SOURCE_JDBC)
    @Primary
    public NamedParameterJdbcTemplate jdbcTemplate(IXbbDataSourceProperties xbbDataSourceProperties, JdbcTemplateProperties jdbcTemplateProperties) {
        return new NamedParameterJdbcTemplate(new XbbJdbcTemplateFactory().buildPrimaryMysqlDataSource("xbbcloud", xbbDataSourceProperties, jdbcTemplateProperties));
    }

    @Bean(name = XBB_USER_XH_DATA_SOURCE_JDBC)
    public NamedParameterJdbcTemplate userXhJdbcTemplate(IXbbDataSourceProperties xbbDataSourceProperties, JdbcTemplateProperties jdbcTemplateProperties) {
        return new NamedParameterJdbcTemplate(new XbbJdbcTemplateFactory().buildPrimaryMysqlDataSource("userxh", xbbDataSourceProperties, jdbcTemplateProperties));
    }

//    @Bean(name = "")
//    @Primary
//    public DataSourceTransactionManager transactionManager(NamedParameterJdbcTemplate jdbcTemplate) {
//        //noinspection ConstantConditions
//        return new DataSourceTransactionManager(jdbcTemplate.getJdbcTemplate().getDataSource());
//    }


    @Bean(name = XBBCLOUD_TRANSACTION_MANAGER)
    @Primary
    public PlatformTransactionManager xbbcloudTransactionalManager(@Qualifier(XBB_CLOUD_DATA_SOURCE_JDBC) NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new DataSourceTransactionManager(namedParameterJdbcTemplate.getJdbcTemplate().getDataSource());
    }


    @Bean(name = USER_XH_TRANSACTION_MANAGER)
    public PlatformTransactionManager userxhTransactionalManager(@Qualifier(XBB_USER_XH_DATA_SOURCE_JDBC) NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new DataSourceTransactionManager(namedParameterJdbcTemplate.getJdbcTemplate().getDataSource());
    }
}
