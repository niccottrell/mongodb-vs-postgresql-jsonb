package niccottrell.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@DynamoDBTable(tableName = "example")
public class ExampleDynamo implements ExampleInterface<Integer> {

  public static final String TABLE_NAME = "example";

  public static final String KEY_ID = "id";

  public static final String KEY_NAME = "name";

  private Integer id;

  @NotNull
  String name;

  String description;

  int stock;

  BigDecimal price;

  @NotNull
  private Date date;

  private boolean correct;

  private Map<String, String> features = new HashMap<String, String>();

  @DynamoDBHashKey(attributeName="id")
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  @DynamoDBAttribute
  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @DynamoDBAttribute
  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @DynamoDBAttribute
  @Override
  public int getStock() {
    return stock;
  }

  @Override
  public void setStock(int stock) {
    this.stock = stock;
  }

  @DynamoDBAttribute
  @Override
  public float getPrice() {
    return price.floatValue();
  }

  public void setPrice(float price) {
    this.price = new BigDecimal(price);
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  @DynamoDBAttribute
  @Override
  public Date getDate() {
    return date;
  }

  @Override
  public void setDate(Date date) {
    this.date = date;
  }

  @DynamoDBAttribute
  @Override
  public boolean isCorrect() {
    return correct;
  }

  @Override
  public void setCorrect(boolean correct) {
    this.correct = correct;
  }

  public Map<String, String> getFeatures() {
    return features;
  }

  public void setFeatures(Map<String, String> features) {
    this.features = features;
  }

  @Override
  public void addFeature(String key, String value) {
    this.features.put(key, value);
  }

  @Override
  public String getFeature(String key) {
    return   this.features.get(key);
  }

}
