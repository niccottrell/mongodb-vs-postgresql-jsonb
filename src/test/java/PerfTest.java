import model.ExampleMongo;
import model.ExamplePg;
import model.mongodb.SpringMongoConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

//@SpringBootApplication
// @AutoConfigureDataMongo
@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringMongoConfig.class, PgRepository.class, MongodbRepository.class})
public class PerfTest {

  @Autowired
  private PgRepository pgRepo;

  @Autowired
  private MongodbRepository mongoRep;

  @Test
  public void testPerfPg() {
    for (int i = 0; i < 100; i++) {
      // Query by name
      String name = "Name " + i * 5;
      List<ExamplePg> byName = pgRepo.findByName(name);
      Assert.assertNotNull(byName);
      Assert.assertFalse(byName.isEmpty());
      // Query by correct and stock
      String key = "Ref";
      List<ExamplePg> byFeature = pgRepo.findByFeature(key);
      for (ExamplePg examplePg : byFeature) {
        String v = examplePg.getFeature(key);
        Assert.assertNotNull(v);
        Assert.assertTrue(v.startsWith("ABC"));
      }
      String value = "ABC" + (i * 3);
      List<ExamplePg> byFeature2 = pgRepo.findByFeature(key, value);
      for (ExamplePg examplePg : byFeature2) {
        String v = examplePg.getFeature(key);
        Assert.assertNotNull(v);
        Assert.assertEquals(value, v);
      }
      // Query by date range
      Date minDate = new Date(1800, 1, 1);
      List<ExamplePg> byDate = pgRepo.findByAfter(minDate);
      Assert.assertNotNull(byDate);
      Assert.assertFalse(byDate.isEmpty());
    }
  }

  @Test
  public void testPerfMongo() {
    for (int i = 0; i < 100; i++) {
      // Query by name
      String name = "Name " + i * 5;
      List<ExampleMongo> byName = mongoRep.findByName(name);
      Assert.assertNotNull(byName);
      Assert.assertFalse(byName.isEmpty());
      // Query by correct and stock
      // String key = "ABC" + (i * 3);
      String key = "Ref";
      List<ExampleMongo> byFeature = mongoRep.findByFeature(key);
      for (ExampleMongo example : byFeature) {
        String value = example.getFeature(key);
        Assert.assertNotNull(value);
        Assert.assertTrue(value.startsWith("ABC"));
      }
      String value = "ABC" + (i * 3);
      List<ExampleMongo> byFeature2 = mongoRep.findByFeature(key, value);
      for (ExampleMongo example : byFeature2) {
        String v = example.getFeature(key);
        Assert.assertNotNull(v);
        Assert.assertEquals(value, v);
      }
      // Query by date range
      Date minDate = new Date(1800, 1, 1);
      List<ExampleMongo> byDate = mongoRep.findByAfter(minDate);
      Assert.assertNotNull(byDate);
      Assert.assertFalse(byDate.isEmpty());
    }
  }
}
