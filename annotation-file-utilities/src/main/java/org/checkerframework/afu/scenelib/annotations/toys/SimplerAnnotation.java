package org.checkerframework.afu.scenelib.annotations.toys;

import java.util.*;

public @interface SimplerAnnotation {
  BalanceEnum be();

  int height();

  int[] wrappedHeight();

  Class<? super HashMap<String, String>> favoriteClass();
}
