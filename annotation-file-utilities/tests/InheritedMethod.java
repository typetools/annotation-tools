package annotator.tests;

class InheritedMethod {
  abstract class Base<T> {
    //public T f(T t) { return t; }  // type instantiation test (NYI)
    public int[] g(String[] a, int i) { return new int[0]; }
  }

  public class Sub extends Base<Integer> {
  }
}

