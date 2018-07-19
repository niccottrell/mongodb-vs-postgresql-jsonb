package niccottrell;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import niccottrell.model.ExampleInterface;
import niccottrell.model.ExampleMongo;
import niccottrell.model.ExamplePg;
import niccottrell.model.mongodb.MongoConfig;
import niccottrell.model.postgresql.PgConfig;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static niccottrell.model.mongodb.MongoConfig.COLLNAME;
import static niccottrell.model.mongodb.MongoConfig.DBNAME;

@ContextConfiguration(classes = {MongoConfig.class, PgConfig.class})
@EnableMongoRepositories
@EnableJpaRepositories
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class Populate {

  private static final Logger logger = LoggerFactory.getLogger(Populate.class);

  private PgRepository pgRepo;

  private MongodbRepository mongoRep;

  private MongoClient mongoClient;

  @Autowired
  public Populate(PgRepository pgRepo, MongodbRepository mongoRep, MongoClient mongoClient) {
    logger.info("Building Populate");
    this.pgRepo = pgRepo;
    this.mongoRep = mongoRep;
    this.mongoClient = mongoClient;
  }

  protected void createIndexes() throws Exception {
    createMongoIndexes(mongoClient);
    createPgIndexes();
  }

  protected static void createPgIndexes() throws SQLException {
    logger.info("Creating Pg indexes");
    // create indexes on Postgresql
    String url = "jdbc:postgresql://localhost/postgres";
    Connection conn = DriverManager.getConnection(url);
    // See: https://www.postgresql.org/docs/10/static/datatype-json.html
    // Note: Seems that the double round brackets is required
    conn.prepareCall("CREATE INDEX ON example ((features ->> 'k'));").execute();
    // conn.prepareCall("CREATE INDEX ON example ((data ->> 'stock')::INT);").execute(); // syntax error
    // conn.prepareCall("CREATE INDEX ON example USING GIN ((data ->> 'stock'));").execute(); // doesn't work on Pg 10.4
    conn.prepareCall("CREATE INDEX ON example USING GIN ((data ->> 'date'));").execute(); // doesn't work on Pg 10.4
  }

  protected static void createMongoIndexes(MongoClient mongoClient) {
    logger.info("Creating Mongo indexes");
    // create indexes on MongoDB
    MongoCollection<Document> collection = mongoClient.getDatabase(DBNAME).getCollection(COLLNAME);
    collection.createIndex(new Document("name", 1));
    collection.createIndex(new Document("date", 1));
    collection.createIndex(new Document("features.Ref", 1).append("date", 1));
  }

  protected void go() throws Exception {
    go(pgRepo, mongoRep);
  }

  protected static void go(PgRepository pgRepo, MongodbRepository mongoRep) throws Exception {
    logger.info("Dropping test data");
    mongoRep.deleteAll();
    pgRepo.deleteAll();
    logger.info("Building fresh test data");
    List<ExampleMongo> exsMongo = createData(ExampleMongo.class);
    long startTime = System.currentTimeMillis();
    mongoRep.saveAll(exsMongo);
    logger.info("Save to Mongo took " + (System.currentTimeMillis() - startTime) + " ms");
    List<ExamplePg> exsPg = createData(ExamplePg.class);
    startTime = System.currentTimeMillis();
    pgRepo.saveAll(exsPg);
    logger.info("Save to Pg took " + (System.currentTimeMillis() - startTime) + " ms");
  }

  private static <K extends ExampleInterface> List<K> createData(Class<K> cls) throws Exception {
    List<K> results = new ArrayList<K>();
    for (int i = 1; i < 10000; i++) {
      K ex = cls.getDeclaredConstructor().newInstance();
      populate(ex, i);
      if (ex.getId() == 0) throw new RuntimeException("No id set: " + ex);
      if (ex.getDescription() == null) throw new RuntimeException("No description set: " + ex);
      if (ex.getDate() == null) throw new RuntimeException("No date set: " + ex);
      results.add(ex);
    }
    return results;
  }

  @SuppressWarnings("deprecation")
  private static void populate(ExampleInterface ex, int i) {
    ex.setId(i);
    ex.setCorrect(i % 2 == 1);
    ex.setDate(new Date(i % 2000, i % 12, i % 28));
    ex.setDescription("This is a description " + i);
    ex.setName("Name " + i);
    ex.setPrice(i / 100f);
    ex.setStock(20000 - i);
    ex.addFeature("Ref", "ABC" + (i * 3));
    ex.addFeature("Code", "x" + ((int) (i / 100) + 123));
  }

}
