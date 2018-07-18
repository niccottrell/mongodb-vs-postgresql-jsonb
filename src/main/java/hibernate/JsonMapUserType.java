package hibernate;

import java.util.Map;

public class JsonMapUserType extends JsonUserType  {

  @Override
  public Class<?> returnedClass() {
    return Map.class;
  }


}
