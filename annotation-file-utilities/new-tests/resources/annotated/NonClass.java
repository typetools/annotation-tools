public class NonClass {
  @X
  interface I extends @X Comparable<@X I> {}
  interface A { void m(@X Object p); }
  enum B { ONE; void m() { @X Object l; } }
  @interface C { @X String value() default "Ha!"; }
  enum E {
    @V(3)
    @V(2) D((@V(1) String) new String());
    E(String s) { a = new @V(0) A() { @Override public void m(Object p) {} }; }
    final @X A a;
  }
}
