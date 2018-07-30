package niccottrell.model;

import java.util.Date;

public interface ExampleInterface<K> {

  void setDate(Date date);

  K getId();

  void setId(K id);

  String getName();

  void setName(String name);

  String getDescription();

  void setDescription(String description);

  int getStock();

  float getPrice();

  void setPrice(float price);

  Date getDate();

  void setStock(int stock);

  boolean isCorrect();

  void setCorrect(boolean correct);

  void addFeature(String key, String value);

  String getFeature(String key);

}
