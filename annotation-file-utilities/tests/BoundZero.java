package annotator.tests;

import static java.lang.annotation.ElementType.TYPE_USE;
import java.lang.annotation.Target;

@Target({TYPE_USE}) @interface X {}
@Target({TYPE_USE}) @interface Y {}

class BoundZero {
  <T extends Object> void m1(T o) {}
  <T extends @Y Object & Comparable<T>> void m2(T o) {}
  <T extends @annotator.tests.Y Object & Comparable<T>> void m3(T o) {}
  <T extends java.lang.Object & Comparable<T>> void m4(T o) {}
  <T extends java.lang.@Y Object & Comparable<T>> void m5(T o) {}
  <T extends java.lang.@annotator.tests.Y Object & Comparable<T>> void m6(T o) {}
}

