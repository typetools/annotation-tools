package experiment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

class Annotations {
  @Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
  @interface A { }

  @Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
  @interface B { }

  @Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
  @interface C { }

  @Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
  @interface D { }

  @Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
  @interface E { }

  @Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
  @interface F { }

  @Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
  @interface G { }

  @Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
  @interface H { }
}
