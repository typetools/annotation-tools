package annotations;

import checkers.nullness.quals.*;
import checkers.javari.quals.*;

import java.util.*;

import annotations.field.*;

/**
 * An {@link AnnotationBuilder} builds a single annotation object after the
 * annotation's fields have been supplied one by one.  <code>A</code> is a
 * supertype of the built annotation object.
 *
 * <p>
 * It is not possible to specify the type name or the retention policy.
 * Either the {@link AnnotationBuilder} expects a certain definition (and
 * may throw exceptions if the fields deviate from it) or it determines the
 * definition automatically from the supplied fields.
 *
 * <p>
 * Each {@link AnnotationBuilder} is mutable and single-use; the purpose of an
 * {@link AnnotationFactory} is to produce as many {@link AnnotationBuilder}s
 * as needed.
 */
public class AnnotationBuilder {

    String typeName;
    RetentionPolicy retention;

    boolean arrayInProgress = false;

    boolean active = true;

    Map<String, AnnotationFieldType> fieldTypes =
        new LinkedHashMap<String, AnnotationFieldType>();

    Map<String, /*@ReadOnly*/ Object> fieldValues =
        new LinkedHashMap<String, /*@ReadOnly*/ Object>();

    class SimpleArrayBuilder implements ArrayBuilder {
        boolean abActive = true;

        String fieldName;

        List</*@ReadOnly*/ Object> arrayElements =
            new ArrayList</*@ReadOnly*/ Object>();

        SimpleArrayBuilder(String fieldName) {
            this.fieldName = fieldName;
        }

        public void appendElement(/*@ReadOnly*/ Object x) {
            if (!abActive)
                throw new IllegalStateException("Array is finished");
            arrayElements.add(x);
        }

        public void finish() {
            if (!abActive)
                throw new IllegalStateException("Array is finished");
            fieldValues.put(fieldName, Collections
                            .<Object>unmodifiableList(arrayElements));
            arrayInProgress = false;
            abActive = false;
        }
    }

    private void checkAddField(String fieldName) {
        if (!active)
            throw new IllegalStateException("Already finished");
        if (arrayInProgress)
            throw new IllegalStateException("Array in progress");
        if (fieldTypes.containsKey(fieldName))
            throw new IllegalArgumentException("Duplicate field "
                                               + fieldName);
    }

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
    public void addScalarField(String fieldName, ScalarAFT aft, /*@ReadOnly*/ Object x) {
        checkAddField(fieldName);
        if (x instanceof Annotation && !(x instanceof Annotation))
            throw new IllegalArgumentException(
                                               "All subannotations must be Annotations");
        fieldTypes.put(fieldName, aft);
        fieldValues.put(fieldName, x);
    }

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
    public ArrayBuilder beginArrayField(String fieldName, ArrayAFT aft) {
        checkAddField(fieldName);
        fieldTypes.put(fieldName, aft);
        arrayInProgress = true;
        return new SimpleArrayBuilder(fieldName);
    }

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
    public void addEmptyArrayField(String fieldName) {
        checkAddField(fieldName);
        fieldTypes.put(fieldName, new ArrayAFT(null));
        fieldValues.put(fieldName, Collections.emptyList());
    }

    /**
     * Returns the completed annotation. This method may throw an exception if
     * the {@link AnnotationBuilder} expects a certain definition for the
     * built annotation and one or more fields in that definition were not
     * supplied.  Once this method has been called, no more method calls may be
     * made on this {@link AnnotationBuilder}.
     */
    public Annotation finish() {
        if (!active)
            throw new IllegalStateException("Already finished");
        if (arrayInProgress)
            throw new IllegalStateException("Array in progress");
        active = false;
        return new Annotation(
                              new AnnotationDef(typeName, retention, fieldTypes), fieldValues);
    }

    AnnotationBuilder(String typeName) {
        this.typeName = typeName;
        this.retention = null;
    }

    AnnotationBuilder(String typeName, RetentionPolicy retention) {
        this.typeName = typeName;
        this.retention = retention;
    }
}
