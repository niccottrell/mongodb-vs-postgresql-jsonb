package niccottrell.hibernate;

import niccottrell.model.Data;
import org.hibernate.HibernateException;

public class JsonDataUserType extends JsonUserType  {

  @Override
  public Class<?> returnedClass() {
    return Data.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object deepCopy(Object originalValue) throws HibernateException {
    if (originalValue == null) {
      return null;
    } else if (originalValue instanceof Data) {
      Data result = new Data();
      Data originalData = (Data) originalValue;
      result.setCorrect(originalData.isCorrect());
      result.setDate(originalData.getDate());
      result.setDescription(originalData.getDescription());
      result.setName(originalData.getName());
      result.setPrice(originalData.getPrice());
      result.setStock(originalData.getStock());
      return result;
    } else {
      throw new RuntimeException("Unexpected value: " + originalValue);
    }
  }

}
