package hibernate;

import model.Data;

public class JsonDataUserType extends JsonUserType  {

  @Override
  public Class<?> returnedClass() {
    return Data.class;
  }

}
