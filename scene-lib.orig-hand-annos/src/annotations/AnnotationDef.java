package annotations;

import java.util.*;

import annotations.field.*;
import annotations.util.coll.*;

/**
 * An annotation type definition, consisting of the annotation name and its
 * field names and types. <code>AnnotationDef</code>s are immutable.
 */
public final /*@Unmodifiable*/ class AnnotationDef {
    /**
     * A {@link Keyer} that keys {@link AnnotationDef}s by {@link #name}.
     */
    public static final /*@NonNull*/ Keyer</*@NonNull*/ String,
        /*@NonNull*/ AnnotationDef> nameKeyer =
            new Keyer</*@NonNull*/ String, /*@NonNull*/ AnnotationDef>() {
                public /*@NonNull*/ String getKeyFor(/*@NonNull*/ AnnotationDef v) {
                    return v.name;
                }
            };

    /**
     * The fully qualified name of the annotation type (like
     * <code>"foo.Bar$Baz"</code>).
     */
    public final /*@NonNull*/ String name;

    /**
     * A map of the names of this annotation type's fields to their types. Since
     * {@link AnnotationDef}s are immutable, attempting to modify this
     * map will result in an exception.
     */
    public final /*@NonNull*/ /*@ReadOnly*/ Map</*@NonNull*/ String,
        /*@NonNull*/ AnnotationFieldType> fieldTypes;

    /**
     * Constructs an annotation definition with the given name and field types.
     * The field type map is copied and then wrapped in an
     * {@linkplain Collections#unmodifiableMap unmodifiable map} to protect the
     * immutability of the annotation definition.
     */
    public AnnotationDef(/*@NonNull*/ String name,
            /*@NonNull*/ /*@ReadOnly*/ Map</*@NonNull*/ String,
            /*@NonNull*/ ? extends AnnotationFieldType> fieldTypes) {
        this.name = name;
        this.fieldTypes = Collections.unmodifiableMap(
                new LinkedHashMap</*@NonNull*/ String,
                /*@NonNull*/ AnnotationFieldType>(fieldTypes)
                );
    }

    /**
     * This {@link AnnotationDef} equals <code>o</code> if and only if
     * <code>o</code> is another nonnull {@link AnnotationDef} and
     * <code>this</code> and <code>o</code> define annotation types of the same
     * name with the same field names and types.
     */
    @Override
    public boolean equals(/*@ReadOnly*/ Object o) {
        return o instanceof AnnotationDef
                && equals((/*@NonNull*/ AnnotationDef) o);
    }

    /**
     * Returns whether this {@link AnnotationDef} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link AnnotationDef}.
     */
    public boolean equals(/*@NonNull*/ AnnotationDef o) {
        return name.equals(o.name) && fieldTypes.equals(o.fieldTypes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return name.hashCode() + fieldTypes.hashCode();
    }

    /**
     * Returns an <code>AnnotationDef</code> containing all the information
     * from both arguments, or <code>null</code> if the two arguments
     * contradict each other.  Currently this just
     * {@linkplain AnnotationFieldType#unify unifies the field types}
     * to handle arrays of unknown element type, which can arise via
     * {@link AnnotationBuilder#addEmptyArrayField}.
     */
    public static AnnotationDef unify(/*@NonNull*/ AnnotationDef def1,
            /*@NonNull*/ AnnotationDef def2) {
        if (def1.equals(def2))
            return def1;
        else if (def1.name.equals(def2.name) &&
                def1.fieldTypes.keySet().equals(def2.fieldTypes.keySet())) {
            /*@NonNull*/ Map</*@NonNull*/ String,
                /*@NonNull*/ AnnotationFieldType> newFieldTypes
                = new LinkedHashMap</*@NonNull*/ String,
                    /*@NonNull*/ AnnotationFieldType>();
            for (/*@NonNull*/ String fieldName : def1.fieldTypes.keySet()) {
                AnnotationFieldType aft1 = def1.fieldTypes.get(fieldName);
                AnnotationFieldType aft2 = def2.fieldTypes.get(fieldName);
                AnnotationFieldType uaft = AnnotationFieldType.unify(aft1, aft2);
                if (uaft == null)
                    return null;
                newFieldTypes.put(fieldName, uaft);
            }
            return new AnnotationDef(def1.name, newFieldTypes);
        } else
            return null;
    }
}
