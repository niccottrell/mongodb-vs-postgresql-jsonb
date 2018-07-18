package model.mongodb;

import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class SpringMongoConfig extends AbstractMongoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(SpringMongoConfig.class);

  public static final String DBNAME = "PgPerf";

  public SpringMongoConfig() {
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

}
