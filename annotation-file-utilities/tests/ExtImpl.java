package annotator.tests;

public class ExtImpl {
  class Top<X, Y> {}
  interface Iface<A, B> {}
  interface Iface2<C, D> {}
  interface I {}

  class C1 extends Top<Object, String> implements Iface<Integer, String> {}

  class C2 implements Iface<String, Object>, Iface2<Object, Float> {}

  class C3 {
    class I implements annotator.tests.ExtImpl.I {}

    /*
     * the jaif file  says that the simple name of
     * the return type in JVM format is
     * LI;
     */
    annotator.tests.ExtImpl.C3.I getI1() {
      return null;
    }

    /*
     * in this case, the jaif file uses the fully qualified name
     * for the return type
     * Lannotator.tests.ExtImpl.C3.I;
     */
    annotator.tests.ExtImpl.C3.I getI2() {
      return null;
    }

    /*
     * the jaif file uses the simple name of the return type
     * LC3$I;
     */
    I getI3() {
      return null;
    }

    /*
     * in the jaif file, the return type is I
     * (ambiguous: could be short for the interface
     * annotator.tests.ExtImpl.I)
     */
    I getI4() {
      return null;
    }
  }
}

