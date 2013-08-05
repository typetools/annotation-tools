package annotations;

/*>>>
import checkers.nullness.quals.*;
import checkers.javari.quals.*;
*/

import java.util.*;

import annotations.field.*;
import annotations.el.AnnotationDef;

/**
 * An {@link AnnotationBuilder} builds a single annotation object after the
 * annotation's fields have been supplied one by one.
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

    // Sometimes, we build the AnnotationDef at the very end, and sometimes
    // we have it before starting.
    AnnotationDef def;

    private String typeName;
    Set<Annotation> tlAnnotationsHere;

    boolean arrayInProgress = false;

    boolean active = true;

    // Generally, don't use this.  Use method fieldTypes() instead.
    private Map<String, AnnotationFieldType> fieldTypes =
        new LinkedHashMap<String, AnnotationFieldType>();

    Map<String, /*@ReadOnly*/ Object> fieldValues =
        new LinkedHashMap<String, /*@ReadOnly*/ Object>();

    public String typeName() {
        if (def != null) {
            return def.name;
        } else {
            return typeName;
        }
    }

    public Map<String, AnnotationFieldType> fieldTypes() {
        if (def != null) {
            return def.fieldTypes;
        } else {
            return fieldTypes;
        }
    }

    class SimpleArrayBuilder implements ArrayBuilder {
        boolean abActive = true;

        String fieldName;
        AnnotationFieldType aft; // the type for the elements

        List</*@ReadOnly*/ Object> arrayElements =
            new ArrayList</*@ReadOnly*/ Object>();

        SimpleArrayBuilder(String fieldName, AnnotationFieldType aft) {
            assert aft != null;
            assert fieldName != null;
            this.fieldName = fieldName;
            this.aft = aft;
        }

        public void appendElement(/*@ReadOnly*/ Object x) {
            if (!abActive)
                throw new IllegalStateException("Array is finished");
            if (!aft.isValidValue(x)) {
                throw new IllegalArgumentException(String.format("Bad array element value%n  %s (%s)%nfor field %s%n  %s (%s)",
                                                                 x, x.getClass(), fieldName, aft, aft.getClass()));
            }
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
        if (fieldValues.containsKey(fieldName))
            throw new IllegalArgumentException("Duplicate field \'"
                                               + fieldName + "\' in " + fieldValues);
    }

    /**
     * Supplies a scalar field of the given name, type, and value for inclusion
     * in the annotation returned by {@link #finish}. See the rules for values
     * on {@link Annotation#getFieldValue}.
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
            throw new IllegalArgumentException("All subannotations must be Annotations");
        if (def == null) {
            fieldTypes.put(fieldName, aft);
        }
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
        if (def == null) {
            fieldTypes.put(fieldName, aft);
        } else {
            aft = (ArrayAFT) fieldTypes().get(fieldName);
            if (aft == null) {
                throw new Error(String.format("Definition for %s lacks field %s:%n  %s",
                                              def.name, fieldName, def));
            }
            assert aft != null;
        }
        arrayInProgress = true;
        assert aft.elementType != null;
        return new SimpleArrayBuilder(fieldName, aft.elementType);
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
        if (def == null) {
            fieldTypes.put(fieldName, new ArrayAFT(null));
        }
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
            throw new IllegalStateException("Already finished: " + this);
        if (arrayInProgress)
            throw new IllegalStateException("Array in progress: " + this);
        active = false;
        if (def == null) {
            assert fieldTypes != null;
            def = new AnnotationDef(typeName, tlAnnotationsHere, fieldTypes);
        } else {
            assert typeName == null;
            assert fieldTypes.isEmpty();
        }
        return new Annotation(def, fieldValues);
    }

    AnnotationBuilder(AnnotationDef def) {
        assert def != null;
        this.def = def;
    }

    AnnotationBuilder(String typeName) {
        assert typeName != null;
        this.typeName = typeName;
    }

    AnnotationBuilder(String typeName, Set<Annotation> tlAnnotationsHere) {
        assert typeName != null;
        this.typeName = typeName;
        this.tlAnnotationsHere = tlAnnotationsHere;
    }

    public String toString() {
        if (def != null) {
            return String.format("AnnotationBuilder %s", def);
        } else
            return String.format("(AnnotationBuilder %s : %s)", typeName, tlAnnotationsHere);
    }

}
