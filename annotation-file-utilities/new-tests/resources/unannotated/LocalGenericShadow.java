package resources.unannotated;

import java.util.List;


public class LocalGenericShadow {
  public List<String> foo = null;

  public void method() {
    List<Integer> foo = null;
    System.out.println(foo);
  }
}
