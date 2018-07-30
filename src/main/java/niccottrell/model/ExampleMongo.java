package niccottrell.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Document(collection="example")
public class ExampleMongo implements ExampleInterface<Long> {

  @Id
  private Long id;

  @NotNull
  String name;

  String description;

  int stock;

  BigDecimal price;

  @NotNull
  private Date date;

  private boolean correct;

  private Map<String, String> features = new HashMap<String, String>();

  public float getPrice() {
    return price.floatValue();
  }

  public void setPrice(float price) {
    this.price = new BigDecimal(price);
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }

  public boolean isCorrect() {
    return correct;
  }

  public void setCorrect(boolean correct) {
    this.correct = correct;
  }

  @Override
  public void addFeature(String key, String value) {
    this.features.put(key, value);
  }

  @Override
  public String getFeature(String key) {
 return   this.features.get(key);
  }

  @Override
  public String toString() {
    return "ExampleMongo{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", stock=" + stock +
            ", price=" + price +
            ", date=" + date +
            ", correct=" + correct +
            ", features=" + features +
            '}';
  }
}

