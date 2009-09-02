package annotations;

import checkers.nullness.quals.Nullable;
import checkers.javari.quals.ReadOnly;

import java.util.*;

/**
 * A very simple annotation representation constructed with a map of field names
 * to values. See the rules for values on {@link Annotation#getFieldValue};
 * furthermore, subannotations must be {@link Annotation}s.
 * {@link Annotation}s are immutable.
 *
 * <p>
 * {@link Annotation}s can be constructed directly or through
 * {@link AnnotationFactory#saf}. Either way works, but if you construct
 * one directly, you must provide a matching {@link AnnotationDef} yourself.
 */
public final /*@ReadOnly*/ class Annotation {

    /**
     * The annotation definition.
     */
    public final AnnotationDef def;

    /**
     * An unmodifiable copy of the passed map of field values.
     */
    public final /*@ReadOnly*/ Map<String, /*@ReadOnly*/ Object> fieldValues;

    // TODO make sure the field values are valid?
    /**
     * Constructs a {@link Annotation} with the given definition and
     * field values.  Make sure that the field values obey the rules given on
     * {@link Annotation#getFieldValue} and that subannotations are also
     * {@link Annotation}s; this constructor does not validate the
     * values.
     */
    public Annotation(AnnotationDef def,
            /*@ReadOnly*/ Map<String, ? extends /*@ReadOnly*/ Object> fields) {
        this.def = def;
        this.fieldValues = Collections.unmodifiableMap(
                new LinkedHashMap<String, /*@ReadOnly*/ Object>(fields));
    }

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
    public /*@ReadOnly*/ Object getFieldValue(String fieldName) {
        return fieldValues.get(fieldName);
    }

    /**
     * Returns the definition of the annotation type to which this annotation
     * belongs.
     */
    public final AnnotationDef def() {
        return def;
    }

    /**
     * This {@link Annotation} equals <code>o</code> if and only if
     * <code>o</code> is a nonnull {@link Annotation} and <code>this</code> and
     * <code>o</code> have recursively equal definitions and field values,
     * even if they were created by different {@link AnnotationFactory}s.
     */
    @Override
    public final boolean equals(/*@ReadOnly*/ Object o) /*@ReadOnly*/ {
        return o instanceof Annotation && equals((Annotation) o);
    }

    /**
     * A field map created in advance to make {@link #equals(Annotation)} and
     * {@link #hashCode} slightly faster.
     */
    private final /*@ReadOnly*/ Map<String, /*@ReadOnly*/ Object> myFieldMap =
            Annotations.fieldValuesMap(this);

    /**
     * Returns whether this annotation equals <code>o</code>; a slightly faster
     * variant of {@link #equals(Object)} for when the argument is statically
     * known to be another nonnull {@link Annotation}. Implemented by comparing
     * the maps from {@link Annotations#fieldValuesMap}. Subclasses may wish to
     * override this with a hard-coded "&amp;&amp;" of field comparisons to improve
     * performance.
     */
    public boolean equals(Annotation o) /*@ReadOnly*/ {
        return def.equals(o.def())
                && myFieldMap.equals(Annotations.fieldValuesMap(o));
    }

    /**
     * Returns the hash code of this annotation as defined on
     * {@link Annotation#hashCode}. Implemented by taking the hash code of the
     * map from {@link Annotations#fieldValuesMap}. Subclasses may wish to override
     * this with a hard-coded XOR/addition of fields to improve performance.
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return def.hashCode() + myFieldMap.hashCode();
    }

    /**
     * Returns a string representation of this for
     * debugging purposes.  For now, this method relies on
     * {@link AbstractMap#toString} and the {@link Object#toString toString}
     * methods of the field values, so the representation is only a first
     * approximation to how the annotation would appear in source code.
     */
    @Override
    public String toString() /*@ReadOnly*/ {
        StringBuilder sb = new StringBuilder("@");
        sb.append(def.name);
        if (!def.fieldTypes.isEmpty()) {
            sb.append('(');
            sb.append(myFieldMap.toString());
            sb.append(')');
        }
        return sb.toString();
    }

}

// package annotations;
//
// import checkers.nullness.quals.Nullable;
// import checkers.javari.quals.*;
// import checkers.javari.quals.ReadOnly;
//
// import annotations.el.*;
// import annotations.util.coll.Keyer;
//
// /**
//  * A top-level annotation containing an ordinary annotation plus a retention
//  * policy.  These are attached to {@link AElement}s.
//  */
// public final /*@ReadOnly*/ class Annotation {
//     public static final Keyer<String, Annotation> nameKeyer
//         = new Keyer<String, Annotation>() {
//         public String getKeyFor(
//                 Annotation v) /*@ReadOnly*/ {
//             return v.tldef.name;
//         }
//     };
//
//     /**
//      * The annotation definition.
//      */
//     public final AnnotationDef tldef;
//
//     /**
//      * The ordinary annotation, which contains the data and the ordinary
//      * definition.
//      */
//     public final Annotation ann;
//
//     /**
//      * Wraps the given annotation in a top-level annotation using the given
//      * top-level annotation definition, which provides a retention policy.
//      */
//     public Annotation(AnnotationDef tldef, Annotation ann) {
//         if (!ann.def().equals(tldef))
//             throw new IllegalArgumentException("Definitions mismatch");
//         this.tldef = tldef;
//         this.ann = ann;
//     }
//
//     /**
//      * Wraps the given annotation in a top-level annotation with the given
//      * retention policy, generating the top-level annotation definition
//      * automatically for convenience.
//      */
//     public Annotation(Annotation ann1,
//             RetentionPolicy retention) {
//         this(new AnnotationDef(ann1.def(), retention), ann1);
//     }
//
//     /**
//      * {@inheritDoc}
//      */
//     @Override
//     public int hashCode() {
//         return tldef.hashCode() + ann.hashCode();
//     }
//
//     @Override
//     public String toString() {
//       StringBuilder sb = new StringBuilder();
//       sb.append("tla: ");
//       sb.append(tldef.retention);
//       sb.append(":");
//       sb.append(ann.toString());
//       return sb.toString();
//     }
// }
