package experiment;

import experiment.Annotations.*;

import java.util.Comparator;

public class TestLambdaExpression {
  public void InstanceMethod() {
    @A Comparator<Integer> comparator = (@B Integer a, @C Integer b) -> a.compareTo(b);
  }
}