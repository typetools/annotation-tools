package annotations;

import java.util.*;

/**
 * Extending {@link AbstractAnnotation} makes it a bit easier to write an
 * {@link Annotation} implementation. {@link AbstractAnnotation} stores
 * the {@link AnnotationDef} and provides general-purpose implementations of
 * {@link #equals(Object)} and {@link #hashCode}.
 */
public abstract /*@Unmodifiable*/ class AbstractAnnotation implements Annotation {
    private final /*@NonNull*/ AnnotationDef def;

    /**
     * {@inheritDoc}
     */
    public final /*@NonNull*/ AnnotationDef def() {
        return def;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(/*@ReadOnly*/ Object o) {
        return o instanceof Annotation && equals((/*@NonNull*/ Annotation) o);
    }

    /**
     * A field map created in advance to make {@link #equals(Annotation)} and
     * {@link #hashCode} slightly faster.
     */
    private final /*@NonNull*/ /*@ReadOnly*/ Map</*@NonNull*/ String,
        /*@NonNull*/ /*@ReadOnly*/ Object> myFieldMap =
            Annotations.fieldValuesMap(this);

    /**
     * Returns whether this annotation equals <code>o</code>; a slightly faster
     * variant of {@link #equals(Object)} for when the argument is statically
     * known to be another nonnull {@link Annotation}. Implemented by comparing
     * the maps from {@link Annotations#fieldValuesMap}. Subclasses may wish to
     * override this with a hard-coded "&&" of field comparisons to improve
     * performance.
     */
    public boolean equals(/*@NonNull*/ Annotation o) {
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
    public int hashCode() {
        return def.hashCode() + myFieldMap.hashCode();
    }
    
    /**
     * Returns a string representation of this {@link AbstractAnnotation} for
     * debugging purposes.  For now, this method relies on
     * {@link AbstractMap#toString} and the {@link Object#toString toString}
     * methods of the field values, so the representation is only a first
     * approximation to how the annotation would appear in source code.
     */
    @Override
    public /*@NonNull*/ String toString() {
        /*@NonNull*/ StringBuilder sb = new StringBuilder("@");
        sb.append(def.name);
        if (!def.fieldTypes.isEmpty()) {
            sb.append('(');
            sb.append(myFieldMap.toString());
            sb.append(')');
        }
        return sb.toString();
    }

    /**
     * Constructs an {@link AbstractAnnotation}, storing the given
     * definition for regurgitation when {@link #def()} is called.
     */
    protected AbstractAnnotation(/*@NonNull*/ AnnotationDef def) {
        this.def = def;
    }
}
