package annotator.tests;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class BoundMethodMultiple {
  public <T> void foo( Object o) {

  }

  public <T extends  Date> void foo(T o) {

  }

  public <T extends  List &  Serializable> void foo(T t) {

  }

  public <T extends  Date, U extends Map> void foo(T t, U u) {

  }
}
