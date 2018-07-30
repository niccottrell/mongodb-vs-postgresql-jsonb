package niccottrell.model.dynamo;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.local.server.LocalDynamoDBRequestHandler;
import com.amazonaws.services.dynamodbv2.local.server.LocalDynamoDBServerHandler;
import com.amazonaws.services.dynamodbv2.model.*;
import niccottrell.Populate;
import niccottrell.model.ExampleDynamo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static niccottrell.model.ExampleDynamo.TABLE_NAME;

public class DynamoConfig {

  private static final Logger logger = LoggerFactory.getLogger(Populate.class);

  private static DynamoDBProxyServer localDb;

  private static Integer localDbPort = 8000;

  public static AmazonDynamoDB getDynamoClient() {
    if (localDbPort != null) {
      // use Local server
      checkServer();
      try {
        // See https://github.com/aws-samples/aws-dynamodb-examples/blob/master/src/test/java/com/amazonaws/services/dynamodbv2/DynamoDBLocalFixture.java
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("http://localhost:" + localDbPort, "us-east-1"))
                .build();
      } catch (Exception e) {
        throw new RuntimeException("Error creating Dynamo client", e);
      }
    } else {
      // Real remote server
      return AmazonDynamoDBClientBuilder.standard()
              .withRegion(Regions.EU_CENTRAL_1)
              .withCredentials(new ProfileCredentialsProvider())
              .build();
    }
  }

  private static void checkServer() {
    try {
      if (localDb == null) {
        localDb = new DynamoDBProxyServer(localDbPort, new LocalDynamoDBServerHandler(
                new LocalDynamoDBRequestHandler(0, true, null, true, true), null)
        );
        localDb.start();
      }
    } catch (Exception e) {
      throw new RuntimeException("Error launch Dynamo Local server", e);
    }
  }

  public static void main(String[] args) {
    cleanup();
  }

  public static void cleanup() {
    logger.info("Starting DynamoConfig cleanup");
    DynamoDB dynamoDB = new DynamoDB(getDynamoClient());
    try {
      deleteTable(dynamoDB.getTable(TABLE_NAME));
    } catch (Exception e) {
      logger.error("Unable to delete table", e);
    }
    try {
      // recreate an empty table
      createTable(dynamoDB);
      // test that table is loadable
      Table table = dynamoDB.getTable(TABLE_NAME);
      logger.info("Table description=" + table.getDescription());
      // create indexes
      createDynamoIndexes();
    } catch (Exception e) {
      throw new RuntimeException("Unable to create table", e);
    }
  }

  private static void deleteTable(Table table) throws InterruptedException {
    logger.info("Attempting to delete table; please wait...");
    try {
      table.delete();
    } catch (ResourceNotFoundException e) {
      logger.info("No existing Dynamo table to delete");
    }
    table.waitForDelete();
    logger.info("Success deleting table.");
  }

  private static void createTable(DynamoDB dynamoDB) throws InterruptedException {
    logger.info("Attempting to create table; please wait...");
    // create table
    List<KeySchemaElement> keySchema = Arrays.asList(
            // Partition key
            new KeySchemaElement(ExampleDynamo.KEY_ID, KeyType.HASH), // partition key
            new KeySchemaElement("stock", KeyType.RANGE)); // sort key
    List<AttributeDefinition> attribute = Arrays.asList(
            new AttributeDefinition(ExampleDynamo.KEY_ID, ScalarAttributeType.N), // number
            new AttributeDefinition("stock", ScalarAttributeType.N) // number
    );
    ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput(10L, 10L);
    Table table = dynamoDB.createTable(TABLE_NAME,
            keySchema,
            attribute,
            provisionedThroughput);
    table.waitForActive();
    logger.info("Success creating table with no indexes.");
    // create global secondary indexes
  }

  public static void createDynamoIndexes() {
    logger.info("Attempting to create global secondary indexes; please wait...");

    ProjectionType projectionType = ProjectionType.ALL; // since we want to load the whole object
    ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput(20L, 20L);

    // StockIndex
    CreateGlobalSecondaryIndexAction stockIndex = new CreateGlobalSecondaryIndexAction()
            .withIndexName("StockIndex")
            .withKeySchema(
                    new KeySchemaElement().withAttributeName("stock").withKeyType(KeyType.HASH), // Partition key
                    new KeySchemaElement().withAttributeName("date").withKeyType(KeyType.RANGE)) // Sort key
            .withProvisionedThroughput(provisionedThroughput)
            .withProjection(new Projection().withProjectionType(ProjectionType.ALL));
    addGSI(stockIndex, new AttributeDefinition("stock", ScalarAttributeType.N), new AttributeDefinition("date", ScalarAttributeType.S));

    // NameIndex
    CreateGlobalSecondaryIndexAction nameIndex = new CreateGlobalSecondaryIndexAction()
            .withIndexName("NameIndex")
            .withKeySchema(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH)) // Partition key
            .withProvisionedThroughput(provisionedThroughput)
            .withProjection(new Projection().withProjectionType(projectionType));
    addGSI(nameIndex, new AttributeDefinition("name", ScalarAttributeType.S));

    // DateIndex
    CreateGlobalSecondaryIndexAction dateIndex = new CreateGlobalSecondaryIndexAction()
            .withIndexName("DateIndex")
            .withKeySchema(new KeySchemaElement().withAttributeName("date").withKeyType(KeyType.HASH)) // Partition key
            .withProvisionedThroughput(provisionedThroughput)
            .withProjection(new Projection().withProjectionType(projectionType));
    addGSI(dateIndex, new AttributeDefinition("date", ScalarAttributeType.S));

    // FeaturesIndex (TODO: Can this be used?)
    CreateGlobalSecondaryIndexAction featuresIndex = new CreateGlobalSecondaryIndexAction()
            .withIndexName("FeaturesIndex")
            .withKeySchema(new KeySchemaElement().withAttributeName("features").withKeyType(KeyType.HASH)) // Partition key
            .withProvisionedThroughput(provisionedThroughput)
            .withProjection(new Projection().withProjectionType(projectionType));
    addGSI(featuresIndex, new AttributeDefinition("features", ScalarAttributeType.S));

    logger.info("Created secondary indexes");

  }

  private static void addGSI(CreateGlobalSecondaryIndexAction create, AttributeDefinition partitionField) {
    addGSI(create, partitionField, null);
  }

  private static void addGSI(CreateGlobalSecondaryIndexAction create, AttributeDefinition partitionField, AttributeDefinition sortField) {
    if (partitionField == null) throw new RuntimeException("No attributes provided!");
    AmazonDynamoDB client = getDynamoClient();
    try {
      Index index;
      DynamoDB dynamoDB = new DynamoDB(client);
      if (sortField == null) {
        index = dynamoDB.getTable(TABLE_NAME).createGSI(create, partitionField);
      } else {
        index = dynamoDB.getTable(TABLE_NAME).createGSI(create, partitionField, sortField);
      }
      index.waitForActive();
    } catch (Exception e) {
      throw new RuntimeException("Error creating index: " + create.getIndexName(), e);
    }
  }

  public static List<ExampleDynamo> findByStockGreaterThan(DynamoDBMapper mapper, int minStock) {
    logger.info("findByStockGreaterThan:" + minStock);

    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":minStock", new AttributeValue().withS("" + minStock));

    DynamoDBScanExpression queryExpression = new DynamoDBScanExpression()
            .withFilterExpression("stock > :minStock")
            .withExpressionAttributeValues(eav)
            .withIndexName("StockIndex");

    return mapper.scan(ExampleDynamo.class, queryExpression);
  }

  public static List<ExampleDynamo> findByName(DynamoDBMapper mapper, String name) {
    logger.info("findByName:" + name);

    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":name", new AttributeValue().withS(name));

    DynamoDBScanExpression queryExpression = new DynamoDBScanExpression()
            .withFilterExpression("name == :name")
            .withExpressionAttributeValues(eav);
    // TODO .withIndexName("NameIndex");

    return mapper.scan(ExampleDynamo.class, queryExpression);
  }

  public static List<ExampleDynamo> findByFeature(DynamoDBMapper mapper, String key) {
    logger.info("findByFeature:" + key);

    DynamoDBScanExpression queryExpression = new DynamoDBScanExpression()
            .withFilterExpression("features." + key + " != null");

    return mapper.scan(ExampleDynamo.class, queryExpression);

  }

  public static List<ExampleDynamo> findByFeature(DynamoDBMapper mapper, String key, String refValue) {
    logger.info("findByFeature:" + key + "/" + refValue);

    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":val", new AttributeValue().withS(refValue));

    DynamoDBScanExpression queryExpression = new DynamoDBScanExpression()
            .withFilterExpression("features." + key + " == :val")
            .withExpressionAttributeValues(eav);

    return mapper.scan(ExampleDynamo.class, queryExpression);

  }

  public static List<ExampleDynamo> findByDateAfter(DynamoDBMapper mapper, Date minDate) {
    logger.info("findByDateAfter:" + minDate);

    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":minDate", new AttributeValue().withS("" + minDate));

    DynamoDBScanExpression queryExpression = new DynamoDBScanExpression()
            .withFilterExpression("date > :minDate")
            .withExpressionAttributeValues(eav);
    // .withIndexName("DateIndex");

    return mapper.scan(ExampleDynamo.class, queryExpression);
  }

  public static List<ExampleDynamo> findByDateAfterWithFeature(DynamoDBMapper mapper, Date minDate, String key, String refValue) {

    logger.info("findByDateAfter:" + minDate);

    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":minDate", new AttributeValue().withS("" + minDate));
    eav.put(":val", new AttributeValue().withS(refValue));

    DynamoDBScanExpression queryExpression = new DynamoDBScanExpression()
            .withFilterExpression("date > :minDate AND features." + key + " == :val")
            .withExpressionAttributeValues(eav);
    // .withIndexName("DateIndex");

    return mapper.scan(ExampleDynamo.class, queryExpression);

  }
}
