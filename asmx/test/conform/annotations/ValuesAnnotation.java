
package annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Annotation declaration with different values
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ValuesAnnotation {
  byte    byteValue()    default (byte) 255;
  char    charValue()    default (char) 128;
  boolean booleanValue() default true;
  int     intValue()     default 10;
  short   shortValue()   default (short) 20;
  long    longValue()    default 100L;
  float   floatValue()   default 10.0F;
  double  doubleValue()  default 20.0D;
  String  stringValue()  default "defaultString";

  Class classValue()     default Values.class;
  ValuesEnum enumValue() default ValuesEnum.ONE;
  ValueAttrAnnotation annotationValue() default @ValueAttrAnnotation;

  byte[]    byteArrayValue()    default {(byte) 128, (byte) 129};
  char[]    charArrayValue()    default { '1', '2'};
  boolean[] booleanArrayValue() default { true, false};
  int[]     intArrayValue()     default { 500, 501};
  short[]   shortArrayValue()   default { (short) 20000, (short) 2001};
  long[]    longArrayValue()    default { 101L, 102L};
  float[]   floatArrayValue()   default { 11.0F, 12.0F};
  double[]  doubleArrayValue()  default { 21.0D, 22.0D};
  String[]  stringArrayValue()  default { "11", "22"};

  ValuesEnum[] enumArrayValue() default { ValuesEnum.ONE, ValuesEnum.TWO};
  ValueAttrAnnotation[] annotationArrayValue() default { @ValueAttrAnnotation(), @ValueAttrAnnotation("1")};
  Class[] classArrayValue() default { Values.class, Values.class};

}

