public class ExtImpl {
  class Top<X, Y> {}
  interface Iface<A, B> {}
  interface Iface2<C, D> {}

  class C1 extends Top<Object, String> implements Iface<Integer, String> {}

  class C2 implements Iface<String, Object>, Iface2<Object, Float> {}
}
  
