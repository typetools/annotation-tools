package annotator.tests;

import java.util.List;

public class ConstructorParamMultiple {
  public ConstructorParamMultiple(
      /* @ReadOnly*/ Object a,
      /* @ReadOnly*/ List</* @Mutable*/ Integer> b,
      /* @ReadOnly*/ int c) {
    
  }
}
