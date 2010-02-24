package annotator.tests;

import java.util.List;

public class NewMultiple {
  void foo(Object o) {
   List var = (/* @ReadOnly*/ List) o;
   System.out.println(var);
  }
  
  void bar(Object o) {
    List var = (/* @Mutable*/ List) o;
    System.out.println(var);
  }
}
