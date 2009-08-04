package annotator.tests;

import java.util.List;
import java.util.Date;
import java.io.PrintStream;

public class BoundMethodSimple {
  public <T extends Date> void foo(T t) {
    System.out.println(t);
  }
}
