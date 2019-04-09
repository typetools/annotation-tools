package experiment;

import experiment.Annotations.*;

public class TestInstanceOf {
  public static int StaticMethod() {
    @A Object a = new @B Object();
    if (a instanceof @C Object) {
      return 1;
    } else {
      return 0;
    }
  }
}