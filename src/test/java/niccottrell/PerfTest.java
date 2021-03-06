package niccottrell;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import niccottrell.model.ExampleDynamo;
import niccottrell.model.ExampleMongo;
import niccottrell.model.ExamplePg;
import niccottrell.model.QExampleMongo;
import niccottrell.model.dynamo.DynamoConfig;
import niccottrell.model.mongodb.MongoConfig;
import niccottrell.model.postgresql.PgConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

@SuppressWarnings("deprecation")
@EnableMongoRepositories
@EnableJpaRepositories
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoConfig.class, PgConfig.class})
public class PerfTest {

  private static final Logger logger = LoggerFactory.getLogger(PerfTest.class);

  public static final int LOOP_COUNT = 100;

  @Autowired
  private PgRepository pgRepo;

  @Autowired
  private MongodbRepository mongoRep;

  @Autowired
  private MongoClient mongoClient;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Before
  public void preparePg() {
    // TODO Populate.createPgIndexes();
    Populate.createMongoIndexes(mongoClient);
    // TODO Populate.go(pgRepo, mongoRep);
  }

  @Before
  public void prepareDynamo() throws Exception {
    DynamoConfig.cleanup();
    List<ExampleDynamo> exsDynamo = Populate.createData(ExampleDynamo.class);
    logger.info("Created {} examples for DynamoDB", exsDynamo.size());
    Populate.saveAll(DynamoConfig.getDynamoClient(), exsDynamo);
  }

  @Test
  public void testPerfPg() {
    long startTime = System.currentTimeMillis();
    for (int idx = 1; idx <= LOOP_COUNT; idx++) {
      // Query by stock
      int minStock = idx * 20;
      List<ExamplePg> byStockGreaterThan = pgRepo.findByStockGreaterThan(minStock);
      Assert.assertNotNull(byStockGreaterThan);
      Assert.assertFalse("No results for stock >" + minStock, byStockGreaterThan.isEmpty());
      for (ExamplePg ex : byStockGreaterThan) {
        Assert.assertTrue(ex.getStock() > minStock);
      }
      // Query by name
      int id = idx * 5;
      String name = "Name " + id;
      List<ExamplePg> byName = pgRepo.findByName(name);
      Assert.assertNotNull(byName);
      Assert.assertFalse("No results for name=" + name, byName.isEmpty());
      ExamplePg ex = byName.get(0);
      String refValue = "ABC" + id * 3;
      Assert.assertEquals(refValue, ex.getFeature("Ref"));
      // Query by correct and stock
      String key = "Ref";
      List<ExamplePg> byFeature = pgRepo.findByFeatures(key);
      Assert.assertFalse("No results for feature=" + key, byFeature.isEmpty());
      for (ExamplePg examplePg : byFeature) {
        String v = examplePg.getFeature(key);
        Assert.assertNotNull(v);
        Assert.assertTrue(v.startsWith("ABC"));
      }
      List<ExamplePg> byFeature2 = pgRepo.findByFeatures(key, refValue);
      Assert.assertFalse("No results for feature/value=" + key + "/" + refValue, byFeature2.isEmpty());
      for (ExamplePg examplePg : byFeature2) {
        String v = examplePg.getFeature(key);
        Assert.assertNotNull(v);
        Assert.assertEquals(refValue, v);
      }
      // Query by date range
      Date minDate = new Date(1, 1, 1); // making year=1901
      List<ExamplePg> byDate = pgRepo.findByDateAfter(minDate);
      Assert.assertNotNull(byDate);
      Assert.assertFalse(byDate.isEmpty());
      // Query by date AND feature
      List<ExamplePg> byDate2 = pgRepo.findByDateAfterAndFeatures(minDate, key, refValue);
      Assert.assertNotNull(byDate2);
      Assert.assertFalse(byDate2.isEmpty());
      for (ExamplePg exDate : byDate2) {
        Assert.assertTrue(exDate.getDate().after(minDate));
        Assert.assertEquals(refValue, exDate.getFeature(key));
      }
    }
    logger.info("Finds from Pg took " + (System.currentTimeMillis() - startTime) + " ms");
  }

  @Test
  public void testPerfMongo() {
    long startTime = System.currentTimeMillis();
    for (int idx = 1; idx <= LOOP_COUNT; idx++) {
      // Query by stock
      int minStock = idx * 20;
      List<ExampleMongo> byStockGreaterThan = mongoRep.findByStockGreaterThan(minStock, new PageRequest(0, 100));
      Assert.assertNotNull(byStockGreaterThan);
      Assert.assertFalse("No results for stock > " + idx, byStockGreaterThan.isEmpty());
      for (ExampleMongo ex : byStockGreaterThan) {
        Assert.assertTrue(ex.getStock() > minStock);
      }
      // Query by name
      int id = idx * 5;
      String name = "Name " + id;
      List<ExampleMongo> byName = mongoRep.findByName(name);
      Assert.assertNotNull(byName);
      Assert.assertFalse("No results for name=" + name, byName.isEmpty());
      ExampleMongo ex = byName.get(0);
      String refValue = "ABC" + id * 3;
      Assert.assertEquals(refValue, ex.getFeature("Ref"));
      // Query by correct and stock
      // String key = "ABC" + (i * 3);
      String key = "Ref";
      QExampleMongo pred1 = new QExampleMongo("pred1");
      List<ExampleMongo> byFeature = Lists.newArrayList(mongoRep.findAll(pred1.features.containsKey(key)));
      Assert.assertFalse("No results for feature=" + key, byFeature.isEmpty());
      for (ExampleMongo example : byFeature) {
        String value = example.getFeature(key);
        Assert.assertNotNull(value);
        Assert.assertTrue(value.startsWith("ABC"));
      }
      QExampleMongo pred2 = new QExampleMongo("pred2");
      List<ExampleMongo> byFeature2 = Lists.newArrayList(mongoRep.findAll(pred2.features.contains(key, refValue)));
      Assert.assertFalse("No results for feature/value=" + key + "/" + refValue, byFeature2.isEmpty());
      for (ExampleMongo example : byFeature2) {
        String v = example.getFeature(key);
        Assert.assertNotNull(v);
        Assert.assertEquals(refValue, v);
      }
      // Query by date range
      Date minDate = new Date(1, 1, 1); // making year=1901
      List<ExampleMongo> byDate = mongoRep.findByDateAfter(minDate);
      Assert.assertNotNull(byDate);
      Assert.assertFalse(byDate.isEmpty());
      for (ExampleMongo exDate : byDate) {
        Assert.assertTrue(exDate.getDate().after(minDate));
      }
      // Query by date AND feature
      QExampleMongo pred3 = new QExampleMongo("pred3");
      List<ExampleMongo> byDate2 = Lists.newArrayList(
              mongoRep.findAll(pred3.features.contains(key, refValue).and(pred3.date.after(minDate))));
      Assert.assertNotNull(byDate2);
      Assert.assertFalse(byDate2.isEmpty());
      for (ExampleMongo exDate : byDate2) {
        Assert.assertTrue(exDate.getDate().after(minDate));
        Assert.assertEquals(refValue, exDate.getFeature(key));
      }
    }
    logger.info("Finds from Mongo took " + (System.currentTimeMillis() - startTime) + " ms");
  }

  @Test
  public void testPerfDynamo() {
    AmazonDynamoDB client = DynamoConfig.getDynamoClient();
    DynamoDB dynamo = new DynamoDB(client);
    DynamoDBMapper mapper = new DynamoDBMapper(client);
    Table table = dynamo.getTable(ExampleDynamo.TABLE_NAME);
    // Simple (raw) lookup of a document
    Item documentItem =
            table.getItem(new GetItemSpec()
                    .withPrimaryKey(ExampleDynamo.KEY_ID, 123));
    System.out.println("document=" + documentItem.toString());
    // Launch comparable queries to MongoDB/Postgres
    long startTime = System.currentTimeMillis();
    for (int idx = 1; idx <= LOOP_COUNT; idx++) {
      // Query by stock
      int minStock = idx * 20;
      List<ExampleDynamo> byStockGreaterThan = DynamoConfig.findByStockGreaterThan(mapper, minStock);
      Assert.assertNotNull(byStockGreaterThan);
      Assert.assertFalse("No results for stock > " + idx, byStockGreaterThan.isEmpty());
      for (ExampleDynamo ex : byStockGreaterThan) {
        Assert.assertTrue(ex.getStock() > minStock);
      }
      // Query by name
      int id = idx * 5;
      String name = "Name " + id;
      List<ExampleDynamo> byName = DynamoConfig.findByName(mapper, name);
      Assert.assertNotNull(byName);
      Assert.assertFalse("No results for name=" + name, byName.isEmpty());
      ExampleDynamo ex = byName.get(0);
      String refValue = "ABC" + id * 3;
      Assert.assertEquals(refValue, ex.getFeature("Ref"));
      // Query by correct and stock
      // String key = "ABC" + (i * 3);
      String key = "Ref";
      List<ExampleDynamo> byFeature = DynamoConfig.findByFeature(mapper, key);
      Assert.assertFalse("No results for feature=" + key, byFeature.isEmpty());
      for (ExampleDynamo example : byFeature) {
        String value = example.getFeature(key);
        Assert.assertNotNull(value);
        Assert.assertTrue(value.startsWith("ABC"));
      }
      List<ExampleDynamo> byFeature2 = DynamoConfig.findByFeature(mapper, key, refValue);
      Assert.assertFalse("No results for feature/value=" + key + "/" + refValue, byFeature2.isEmpty());
      for (ExampleDynamo example : byFeature2) {
        String v = example.getFeature(key);
        Assert.assertNotNull(v);
        Assert.assertEquals(refValue, v);
      }
      // Query by date range
      Date minDate = new Date(1, 1, 1); // making year=1901
      List<ExampleDynamo> byDate = DynamoConfig.findByDateAfter(mapper, minDate);
      Assert.assertNotNull(byDate);
      Assert.assertFalse(byDate.isEmpty());
      for (ExampleDynamo exDate : byDate) {
        Assert.assertTrue(exDate.getDate().after(minDate));
      }
      // Query by date AND feature
      List<ExampleDynamo> byDate2 = DynamoConfig.findByDateAfterWithFeature(mapper, minDate, key, refValue);
      Assert.assertNotNull(byDate2);
      Assert.assertFalse(byDate2.isEmpty());
      for (ExampleDynamo exDate : byDate2) {
        Assert.assertTrue(exDate.getDate().after(minDate));
        Assert.assertEquals(refValue, exDate.getFeature(key));
      }
    }
    logger.info("Finds from DynamoDB took " + (System.currentTimeMillis() - startTime) + " ms");
  }


}
