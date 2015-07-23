import static java.lang.annotation.ElementType.TYPE_USE;
import java.lang.annotation.Target;

@Target({TYPE_USE}) @interface X {}
@Target({TYPE_USE}) @interface Y {}

class BoundZero {
  <T> void m0(T o) {}
  <T extends Comparable<T>> void m1(T o) {}
  <T extends Object & Comparable<T>> void m2(T o) {}
  <T extends @X Object & Comparable<T>> void m3(T o) {}
  <T extends @java.lang.X Object & Comparable<T>> void m4(T o) {}
  <T extends java.lang.Object & Comparable<T>> void m5(T o) {}
  <T extends java.lang.@X Object & Comparable<T>> void m6(T o) {}
  <T extends java.lang.@java.lang.X Object & Comparable<T>> void m7(T o) {}
}

