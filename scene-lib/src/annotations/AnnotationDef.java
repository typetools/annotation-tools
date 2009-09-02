package annotations;

import checkers.nullness.quals.Nullable;
import checkers.javari.quals.ReadOnly;

import java.util.*;

import annotations.field.*;
import annotations.util.coll.*;

/**
 * An annotation type definition, consisting of the annotation name,
 * (optionally) its retention, and its field names and
 * types. <code>AnnotationDef</code>s are immutable.  An AnnotationDef with
 * a non-null retention policy is called a "top-level annotation definition".
 */
public final /*@ReadOnly*/ class AnnotationDef {

    /**
     * The fully qualified name of the annotation type, such as
     * "foo.Bar$Baz".
     */
    public final String name;

    /**
     * The retention policy for annotations of this type.
     * If non-null, this is called a "top-level" annotation definition.
     * It may be null for annotations that are used only as a field of other
     * annotations.
     */
    public final /*@Nullable*/ RetentionPolicy retention;

    /**
     * A map of the names of this annotation type's fields to their types. Since
     * {@link AnnotationDef}s are immutable, attempting to modify this
     * map will result in an exception.
     */
    public final /*@ReadOnly*/ Map<String, AnnotationFieldType> fieldTypes;

    /**
     * Constructs an annotation definition with the given name and field types.
     * The field type map is copied and then wrapped in an
     * {@linkplain Collections#unmodifiableMap unmodifiable map} to protect the
     * immutability of the annotation definition.
     */
    public AnnotationDef(String name, RetentionPolicy retention,
            /*@ReadOnly*/ Map<String, ? extends AnnotationFieldType> fieldTypes) {
        this.name = name;
        this.retention = retention;
        this.fieldTypes = Collections.unmodifiableMap(
                new LinkedHashMap<String, AnnotationFieldType>(fieldTypes)
                );
    }

    /**
     * This {@link AnnotationDef} equals <code>o</code> if and only if
     * <code>o</code> is another nonnull {@link AnnotationDef} and
     * <code>this</code> and <code>o</code> define annotation types of the same
     * name with the same field names and types.
     */
    @Override
    public boolean equals(/*@ReadOnly*/ Object o) /*@ReadOnly*/ {
        return o instanceof AnnotationDef
                && equals((AnnotationDef) o);
    }

    /**
     * Returns whether this {@link AnnotationDef} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link AnnotationDef}.
     */
    public boolean equals(AnnotationDef o) /*@ReadOnly*/ {
        // if (name.equals(o.name) && fieldTypes.equals(o.fieldTypes) && ! (retention == null ? retention == o.retention : retention.equals(o.retention))) {
        //     System.err.printf("Differ due to retention only: %s %s%n", this, o);
        // }
        return name.equals(o.name)
            && (retention == null ? retention == o.retention : retention.equals(o.retention))
            && fieldTypes.equals(o.fieldTypes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return name.hashCode()
            + (retention == null ? 0 : retention.hashCode())
            + fieldTypes.hashCode();
    }

    /**
     * Returns an <code>AnnotationDef</code> containing all the information
     * from both arguments, or <code>null</code> if the two arguments
     * contradict each other.  Currently this just
     * {@linkplain AnnotationFieldType#unify unifies the field types}
     * to handle arrays of unknown element type, which can arise via
     * {@link AnnotationBuilder#addEmptyArrayField}.
     */
    public static AnnotationDef unify(AnnotationDef def1,
            AnnotationDef def2) {
        if (def1.equals(def2))
            return def1;
        else if (def1.name.equals(def2.name)
                 && def1.retention.equals(def2.retention)
                 && def1.fieldTypes.keySet().equals(def2.fieldTypes.keySet())) {
            Map<String, AnnotationFieldType> newFieldTypes
                = new LinkedHashMap<String, AnnotationFieldType>();
            for (String fieldName : def1.fieldTypes.keySet()) {
                AnnotationFieldType aft1 = def1.fieldTypes.get(fieldName);
                AnnotationFieldType aft2 = def2.fieldTypes.get(fieldName);
                AnnotationFieldType uaft = AnnotationFieldType.unify(aft1, aft2);
                if (uaft == null)
                    return null;
                else newFieldTypes.put(fieldName, uaft);
            }
            return new AnnotationDef(def1.name, def1.retention, newFieldTypes);
        } else
            return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (retention != null) {
            sb.append(retention);
            sb.append(" ");
        }
        sb.append("@");
        sb.append(name);
        sb.append("(");
        boolean first = false;
        for (Map.Entry<String, AnnotationFieldType> entry : fieldTypes.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(entry.getValue().toString());
            sb.append(" ");
            sb.append(entry.getKey());
        }
        sb.append(")");
        return sb.toString();
    }
}
