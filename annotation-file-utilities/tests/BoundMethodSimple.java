package annotator.tests;

import java.util.List;

public class BoundMethodSimple {
  public <T extends Date> void foo(T t) {
    System.out.println(t);
  }
}
