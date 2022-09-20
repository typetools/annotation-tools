package org.checkerframework.afu.scenelib.annotations.toys;

public @interface FancyAnnotation {
  int myInt();

  String left();

  SimplerAnnotation[] friends();
}
