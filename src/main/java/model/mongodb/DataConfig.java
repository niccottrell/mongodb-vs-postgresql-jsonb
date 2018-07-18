package model.mongodb;

import com.mongodb.MongoClient;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class DataConfig extends AbstractMongoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(DataConfig.class);

  public static final String DBNAME = "PgPerf";

  public DataConfig() {
    logger.info("Built Mongo config");
  }

  @Override
  protected String getDatabaseName() {
    return DBNAME;
  }

  @Override
  @Bean
  public MongoClient mongoClient() {
    return new MongoClient("localhost", 27017);
  }

  @Bean
  public MongoTemplate mongoTemplate() throws Exception {
    return new MongoTemplate(mongoClient(), getDatabaseName());
  }

  @Bean
  public DataSource dataSource() {
    PGSimpleDataSource source = new PGSimpleDataSource();
    source.setServerName("localhost");
    source.setDatabaseName("postgres");
    return source;
  }

  @Bean
  public EntityManagerFactory entityManagerFactory() {
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(true);
    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setJpaVendorAdapter(vendorAdapter);
    factory.setPackagesToScan("model");
    factory.setDataSource(dataSource());
    factory.setPersistenceUnitName("postgres");
    factory.afterPropertiesSet();
    return factory.getObject();
  }

}
