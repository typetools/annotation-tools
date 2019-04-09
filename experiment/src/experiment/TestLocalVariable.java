package experiment;

import experiment.Annotations.A;
import experiment.Annotations.B;
import experiment.Annotations.C;
import experiment.Annotations.D;
import experiment.Annotations.E;

public class TestLocalVariable {
  public static int StaticMethod() {
    @A Object a = new @B Object();
    @C Integer b = 6;
    @D String c = "";
    return 1;
  }

  public int InstanceMethod() {
    @D String c = new @E String();
    return 1;
  }
}