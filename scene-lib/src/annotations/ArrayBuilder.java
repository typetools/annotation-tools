package annotations;

/*>>>
import checkers.nullness.quals.*;
import checkers.javari.quals.*;
*/

/**
 * Builds an array that will serve as a field of an annotation; created by
 * {@link AnnotationBuilder#beginArrayField}.
 */
public interface ArrayBuilder {
    /**
     * Appends a value to the array. Call this method for each desired array
     * element, in order. See the rules for values on
     * {@link Annotation#getFieldValue}; furthermore, a subannotation must have
     * been created by the same factory as the annotation of which it is a
     * field.
     */
    void appendElement(/*@ReadOnly*/ Object x);

    /**
     * Finishes building the array. Call this method after all elements have
     * been appended.
     */
    void finish();
}
