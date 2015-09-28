package annotator.tests;

import static java.lang.annotation.ElementType.TYPE_USE;
import java.lang.annotation.Target;

//@skip-test
@Target({TYPE_USE}) @interface A {}
@Target({TYPE_USE}) @interface B {}
@Target({TYPE_USE}) @interface C {}
@Target({TYPE_USE}) @interface X {}
@Target({TYPE_USE}) @interface Y {}

class BoundZero<U extends @annotator.tests.A @annotator.tests.B @annotator.tests.C Object> {
  <T extends @annotator.tests.X @annotator.tests.Y Object> void m1(T o) {}
  <T extends @Y @annotator.tests.X Object & Comparable<T>> void m2(T o) {}
  <T extends @annotator.tests.Y @annotator.tests.X Object & Comparable<T>> void m3(T o) {}
  <T extends java.lang.@annotator.tests.X @annotator.tests.Y Object & Comparable<T>> void m4(T o) {}
  <T extends java.lang.@Y @annotator.tests.X Object & Comparable<T>> void m5(T o) {}
  <T extends java.lang.@annotator.tests.Y @annotator.tests.X Object & Comparable<T>> void m6(T o) {}
  <T extends @annotator.tests.X Object & @annotator.tests.Y Comparable<T>> void m7(T o) {}
  <T extends @annotator.tests.X Object & @Y Comparable<T>> void m8(T o) {}
  <T extends @annotator.tests.X Object & @annotator.tests.Y Comparable<T>> void m9(T o) {}
  class Inner<@X V extends @Y @annotator.tests.A @annotator.tests.B @annotator.tests.C Object> {}
}

class Outer<S extends @annotator.tests.A @annotator.tests.B @annotator.tests.C Object & java.io.Serializable> {
}

