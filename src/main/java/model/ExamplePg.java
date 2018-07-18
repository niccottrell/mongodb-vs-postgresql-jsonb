package model;

import hibernate.JsonDataUserType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@TypeDef(name = "JsonDataUserType", typeClass = JsonDataUserType.class)
@Table(name = "example",
        indexes = {
                @Index(columnList = "id", unique = true),
                @Index(columnList = "features"),
        }
)
public class ExamplePg implements ExampleInterface {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Type(type = "hibernate.JsonDataUserType")
  private Data data;

  @Column
  @Type(type = "hibernate.JsonDataUserType")
  private Map<String, String> features = new HashMap<String, String>();

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return data.name;
  }

  public void setName(String name) {
    this.data.name = name;
  }

  public String getDescription() {
    return data.description;
  }

  public void setDescription(String description) {
    this.data.description = description;
  }

  public int getStock() {
    return data.stock;
  }

  public void setStock(int stock) {
    this.data.stock = stock;
  }

  public float getPrice() {
    return data.price;
  }

  public void setPrice(float price) {
    this.data.price = price;
  }

  public Date getDate() {
    return data.getDate();
  }

  public void setDate(Date date) {
    this.data.setDate(date);
  }

  public boolean isCorrect() {
    return data.isCorrect();
  }

  public void setCorrect(boolean correct) {
    this.data.setCorrect(correct);
  }

  @Override
  public void addFeature(String key, String value) {
    this.features.put(key, value);
  }

  @Override
  public String getFeature(String key) {
    return this.features.get(key);
  }

}
