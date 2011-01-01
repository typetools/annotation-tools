package annotations.tests.classfile.foo;

public @interface F {
  int fieldA();
  @B(value="value from annotation @F") String fieldB();
}
