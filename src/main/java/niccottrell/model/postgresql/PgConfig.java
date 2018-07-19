package niccottrell.model.postgresql;

import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class PgConfig {

  private static final Logger logger = LoggerFactory.getLogger(PgConfig.class);

  public PgConfig() {
    logger.info("Built PgConfig");
  }

  @Bean
  public DataSource dataSource() {
    PGSimpleDataSource source = new PGSimpleDataSource();
    // source.setDriverClassName("org.postgresql.Driver");
    source.setServerName("localhost");
    source.setDatabaseName("postgres");
    return source;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(true);
    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setJpaVendorAdapter(vendorAdapter);
    factory.setPackagesToScan("niccottrell/model");
    factory.setDataSource(dataSource());
    // factory.setPersistenceUnitName("postgres");
    factory.setJpaProperties(additionalProperties());
    factory.afterPropertiesSet();
    return factory;
  }

  Properties additionalProperties() {
    Properties properties = new Properties();
    properties.setProperty("hibernate.hbm2ddl.auto", "update"); // Update the schema (don't drop)
    // properties.setProperty("hibernate.show_sql", "true"); // Show SQL in console
    // properties.setProperty("hibernate.format_sql", "true"); // Show SQL formatted
    return properties;
  }

  @Bean
  public JpaTransactionManager transactionManager() {
    JpaTransactionManager txManager = new JpaTransactionManager();
    txManager.setEntityManagerFactory(entityManagerFactory().getObject());
    return txManager;
  }

}
