package annotator.tests;

// This test ensures that no annotation is re-inserted, if it already
// existed in the original source code.
public class DuplicateAnnotation {

  @SuppressWarnings("A")
  void m1() { }

  @java.lang.SuppressWarnings("B")
  void m2() { }

  /*@SuppressWarnings("C")*/
  void m3() { }

  /*@java.lang.SuppressWarnings("D")*/
  void m4() { }

  void m5() { }
}
