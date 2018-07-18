import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.ExampleInterface;
import model.ExampleMongo;
import model.ExamplePg;
import model.mongodb.SpringMongoConfig;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static model.mongodb.SpringMongoConfig.DBNAME;

@SpringBootApplication
public class Populate {

  private static final Logger logger = LoggerFactory.getLogger(SpringMongoConfig.class);

  @Autowired
  private PgRepository pgRepo;

  @Autowired
  private MongodbRepository mongoRep;

  @Autowired
  private MongoClient mongoClient;

  public Populate() {
    logger.info("Creating Populate");
  }

  public static void main(String[] args) throws Exception {
    Populate populate = new Populate();
    populate.createIndexes();
    populate.go();
  }

  private void createIndexes() throws Exception {
    // create indexes on MongoDB
    MongoCollection<Document> collection = mongoClient.getDatabase(DBNAME).getCollection("examples");
    collection.createIndex(new Document("properties", 1));
    collection.createIndex(new Document("name", 1));
    collection.createIndex(new Document("date", 1));
    // create indexes on Postgresql
    String url = "jdbc:postgresql://localhost/postgres";
    Connection conn = DriverManager.getConnection(url);
    conn.prepareCall("CREATE INDEX ON examples ((features ->> 'k'));").execute();
    conn.prepareCall("CREATE INDEX ON examples ((data ->> 'stock')::INT);").execute();
    conn.prepareCall("CREATE INDEX ON examples USING GIN ((features ->> 'k'));").execute(); // What's the diff?
  }

  private void go() throws Exception {
    List<ExampleMongo> exsMongo = createData(ExampleMongo.class);
    long startTime = System.currentTimeMillis();
    saveMongos(exsMongo);
    System.out.println("Save to Mongo took " + (System.currentTimeMillis() - startTime) + " ms");
    List<ExamplePg> exsPg = createData(ExamplePg.class);
    startTime = System.currentTimeMillis();
    save(exsPg);
    System.out.println("Save to Pg took " + (System.currentTimeMillis() - startTime) + " ms");
  }

  private void saveMongos(List<ExampleMongo> exsMongo) {
    for (ExampleMongo mongo : exsMongo) {
      mongoRep.save(mongo);
    }
  }

  private void save(List<ExamplePg> exsPg) {
    for (ExamplePg examplePg : exsPg) {
      pgRepo.save(examplePg);
    }
  }

  private <K extends ExampleInterface> List<K> createData(Class<K> cls) throws Exception {
    List<K> results = new ArrayList<K>();
    for (int i = 0; i < 10000; i++) {
      K ex = cls.getDeclaredConstructor().newInstance();
      populate(ex, i);
      results.add(ex);
    }
    return results;
  }

  @SuppressWarnings("deprecation")
  private void populate(ExampleInterface ex, int i) {
    ex.setId(i);
    ex.setCorrect(i % 2 == 1);
    ex.setDate(new Date(i % 2000, i % 12, i % 28));
    ex.setDescription("This is a description " + i);
    ex.setName("Name " + i);
    ex.setPrice(i / 100f);
    ex.setStock(20000 - i);
    ex.addFeature("Ref", "ABC" + (i * 3));
    ex.addFeature("Code", "x" + (int) (i / 100) + 123);
  }


}
