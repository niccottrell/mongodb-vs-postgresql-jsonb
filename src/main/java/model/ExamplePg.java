package model;

import hibernate.JsonDataUserType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@TypeDef(name = "JsonDataUserType", typeClass = JsonDataUserType.class)
@Table(name = "example",
        indexes = {
                @Index(columnList = "name", unique = true),
                @Index(columnList = "name"),
                @Index(columnList = "features"),
        }
)
public class ExamplePg implements ExampleInterface {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Type(type = "JsonDataUserType")
  private Data data;

  @Type(type = "JsonDataUserType")
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
    return data.date;
  }

  public void setDate(Date date) {
    this.data.date = date;
  }

  public boolean isCorrect() {
    return data.correct;
  }

  public void setCorrect(boolean correct) {
    this.data.correct = correct;
  }

  @Override
  public void addFeature(String key, String value) {
    this.features.put(key, value);
  }

  @Override
  public String getFeature(String key) {
    return this.features.get(key);
  }

  private class Data {


    @NotNull
    String name;

    String description;

    int stock;

    @Column(precision = 11, scale = 2)
    float price;

    @NotNull
    private Date date;

    private boolean correct;


  }
}
