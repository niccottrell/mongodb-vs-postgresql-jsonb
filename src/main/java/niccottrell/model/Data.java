package niccottrell.model;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class Data {

  @NotNull
  String name;

  String description;

  int stock;

  @Column(precision = 11, scale = 2)
  float price;

  @NotNull
  private Date date;

  private boolean correct;

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

  public float getPrice() {
    return price;
  }

  public void setPrice(float price) {
    this.price = price;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public boolean isCorrect() {
    return correct;
  }

  public void setCorrect(boolean correct) {
    this.correct = correct;
  }
}
