package experiment;

import experiment.Annotations.A;
import experiment.Annotations.B;
import experiment.Annotations.C;
import experiment.Annotations.D;

public class TestLocalVariable {
  public static void main(String[] args) {
    @A Object a = new @B Object();
    @C @D int b = 6;
  }
}