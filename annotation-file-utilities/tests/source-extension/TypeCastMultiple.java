package annotator.tests;

import java.util.List;
import java.util.LinkedList;

public class TypeCastMultiple {
  public void foo(Object o) {
    List myList = (List) o;
    myList = new LinkedList();
    if (myList instanceof List) {
    }
    Integer i = (Integer) o;
    System.out.println(myList);
    System.out.println(i);
  }
}
