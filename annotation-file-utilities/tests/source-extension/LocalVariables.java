package annotator.tests;

import org.checkerframework.checker.initialization.qual.UnderInitialization;

public class LocalVariables {
  public void foo() {
    /*Mut*/ @UnderInitialization Object a = null;
    Object b = null;
    Object c = null;
  }
}
