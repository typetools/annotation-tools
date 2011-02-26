package annotator.tests;

import java.util.List;

public class TypeCastMultiple {
  public void foo(Object o) {
    List myList = (List) o;
    Integer i = (Integer) o;
    System.out.println(myList);
    System.out.println(i);
  }
}
