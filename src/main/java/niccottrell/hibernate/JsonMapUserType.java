package niccottrell.hibernate;

import org.hibernate.HibernateException;

import java.util.HashMap;
import java.util.Map;

public class JsonMapUserType extends JsonUserType  {

  @Override
  public Class<?> returnedClass() {
    return Map.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object deepCopy(Object originalValue) throws HibernateException {
    if (originalValue == null) {
      return null;
    } else if (originalValue instanceof Map) {
      Map resultMap = new HashMap<>();
      Map<?, ?> tempMap = (Map<?, ?>) originalValue;
      tempMap.forEach((key, value) -> resultMap.put((String) key, (String) value));
      return resultMap;
    } else {
      throw new RuntimeException("Unexpected value: " + originalValue);
    }
  }

}
