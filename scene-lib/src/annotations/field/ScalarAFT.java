package annotations.field;

/*>>>
import checkers.javari.quals.*;
*/

/**
 * Common superclass for non-array {@link AnnotationFieldType}s so that
 * {@link ArrayAFT} can accept only scalar element types, enforcing the Java
 * language's prohibition of multidimensional arrays as annotation field types.
 */
public abstract /*@ReadOnly*/ class ScalarAFT extends AnnotationFieldType  {
}
