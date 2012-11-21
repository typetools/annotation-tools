package annotations;

import annotations.field.*;

/**
 * An {@link AnnotationBuilder} builds a single annotation object after
 * the annotation's fields have been supplied one by one. <code>A</code> is a
 * supertype of the built annotation object. Either the
 * {@link AnnotationBuilder} expects a certain definition (and may throw
 * exceptions if the fields deviate from it) or it determines the definition
 * automatically from the supplied fields.
 * 
 * <p>
 * Each {@link AnnotationBuilder} is mutable and single-use; the purpose of an
 * {@link AnnotationFactory} is to produce as many {@link AnnotationBuilder}s
 * as needed.
 */
public interface AnnotationBuilder<A extends Annotation> {
    /**
     * Supplies a scalar field of the given name, type, and value for inclusion
     * in the annotation returned by {@link #finish}. See the rules for values
     * on {@link Annotation#getFieldValue}. Furthermore, a subannotation must
     * have been created by the same factory as the annotation of which it is a
     * field; in particular, it must be an instance of <code>A</code>.
     * 
     * <p>
     * Each field may be supplied only once. This method may throw an exception
     * if the {@link AnnotationBuilder} expects a certain definition for
     * the built annotation and the given field does not exist in that
     * definition or has the wrong type.
     */
    void addScalarField(/*@NonNull*/ String fieldName, /*@NonNull*/
    ScalarAFT aft, /*@NonNull*/ Object x);

    /**
     * Begins supplying an array field of the given name and type. The elements
     * of the array must be passed to the returned {@link ArrayBuilder} in
     * order, and the {@link ArrayBuilder} must be finished before any other
     * methods on this {@link AnnotationBuilder} are called.
     * <code>aft.{@link ArrayAFT#elementType elementType}</code> must be known
     * (not <code>null</code>).
     * 
     * <p>
     * Each field may be supplied only once. This method may throw an exception
     * if the {@link AnnotationBuilder} expects a certain definition for
     * the built annotation and the given field does not exist in that
     * definition or has the wrong type.
     */
    /*@NonNull*/ ArrayBuilder beginArrayField(/*@NonNull*/ String fieldName,
            /*@NonNull*/ ArrayAFT aft);
    
    /**
     * Supplies an zero-element array field whose element type is unknown.  The
     * field type of this array is represented by an {@link ArrayAFT} with
     * {@link ArrayAFT#elementType elementType} == <code>null</code>.
     * 
     * <p>
     * This can sometimes happen due to a design flaw in the format of
     * annotations in class files.  An array value does not specify an type
     * itself; instead, each element carries a type.  Thus, a zero-length array
     * carries no indication of its element type.
     */
    void addEmptyArrayField(/*@NonNull*/ String fieldName);

    /**
     * Returns the completed annotation. This method may throw an exception if
     * the {@link AnnotationBuilder} expects a certain definition for the
     * built annotation and one or more fields in that definition were not
     * supplied.  Once this method has been called, no more method calls may be
     * made on this {@link AnnotationBuilder}.
     */
    /*@NonNull*/ A finish();
}
