package annotator.tests;

class InheritedMethod {
  abstract class Base {
    public abstract int f();
    public int[] g(String[] a, int i) { return new int[0]; }
  }

  public class Super extends Base {
    public int f() { return 0; }
  }
}

