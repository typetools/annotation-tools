package annotator.tests;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalMultiple {
  public void foo(Object o) {
    List myList = null;
    
    if(myList.size() != 0) {
      /* @Mutable*/ Set localVar = null;
      myList.add(localVar);
    } else {
      /* @ReadOnly*/ Set localVar = null;
      myList.add(localVar);
    }
    foo(o);
  }
}
