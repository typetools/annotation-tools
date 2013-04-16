package annotations;

import java.util.*;

import annotations.el.*;

/**
 * All objects that represent annotations in
 * {@link annotations.el.AScene AScene}s must implement {@link Annotation}.
 * Every annotation object must be able to produce
 * {@linkplain #def() its definition} and
 * {@linkplain #getFieldValue field values} on demand. {@link Annotation}s
 * should be immutable.
 * 
 * <p>
 * To attach an {@link Annotation} to an {@link AElement}, first wrap it in
 * a {@link TLAnnotation} to specify the retention policy.
 */
public /*@Unmodifiable*/ interface Annotation {
    /**
     * Returns the definition of the annotation type to which this annotation
     * belongs.
     */
    /*@NonNull*/ AnnotationDef def();

    /**
     * Returns the value of the field whose name is given.
     * 
     * <p>
     * Everywhere in the annotation scene library, field values are to be
     * represented as follows:
     * 
     * <ul>
     * <li>Primitive value: wrapper object, such as {@link Integer}.
     * <li>{@link String}: {@link String}.
     * <li>Class token: name of the type as a {@link String}, using the source
     * code notation <code>int[]</code> for arrays.
     * <li>Enumeration constant: name of the constant as a {@link String}.
     * <li>Subannotation: <code>Annotation</code> object.
     * <li>Array: {@link List} of elements in the formats defined here.  If
     * the element type is unknown (see
     * {@link AnnotationBuilder#addEmptyArrayField}), the array must have zero
     * elements.
     * </ul>
     */
    /*@ReadOnly*/ Object getFieldValue(/*@NonNull*/ String fieldName);

    /**
     * This {@link Annotation} equals <code>o</code> if and only if 
     * <code>o</code> is a nonnull {@link Annotation} and <code>this</code> and
     * <code>o</code> have recursively equal definitions and field values,
     * even if they were created by different {@link AnnotationFactory}s.
     */
    boolean equals(/*@ReadOnly*/ Object o);

    /**
     * The hash code of an annotation shall be the hash code of its definition
     * plus the hash code of the field values map that
     * {@link Annotations#fieldValuesMap} would create (see {@link #getFieldValue} for
     * value formats and {@link Map#hashCode}).
     */
    int hashCode();
}
