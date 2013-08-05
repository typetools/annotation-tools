package annotations;

import annotations.util.coll.*;

/**
 * A <em>top-level</em> annotation type definition, which contains an ordinary
 * {@link AnnotationDef} plus a retention policy. <code>TLAnnotationDef</code>s
 * are immutable.
 */
public final /*@Unmodifiable*/ class TLAnnotationDef {
    /**
     * A {@link Keyer} that keys {@link TLAnnotationDef}s by {@link #name()}.
     */
    public static final /*@NonNull*/ Keyer</*@NonNull*/ String, /*@NonNull*/ TLAnnotationDef> nameKeyer =
            new Keyer</*@NonNull*/ String, /*@NonNull*/ TLAnnotationDef>() {
                public /*@NonNull*/ String getKeyFor(
                        /*@NonNull*/ TLAnnotationDef v) {
                    return v.def.name;
                }
            };

    /**
     * The ordinary annotation definition, which contains the name and field
     * types.
     */
    public final /*@NonNull*/ AnnotationDef def;

    /**
     * The retention policy for annotations of this type.
     */
    public final /*@NonNull*/ RetentionPolicy retention;
    
    /**
     * Returns the name of the annotation type (shortcut for
     * <code>{@link #def}.{@link AnnotationDef#name name}</code>).
     */
    public /*@NonNull*/ String name() {
        return def.name;
    }

    /**
     * Creates a {@link TLAnnotationDef} for a top-level annotation with
     * the given ordinary definiton and the given retention policy.
     */
    public TLAnnotationDef(/*@NonNull*/ AnnotationDef def1, /*@NonNull*/
            RetentionPolicy retention) {
        this.def = def1;
        this.retention = retention;
    }

    /**
     * Compare everything but the ordinary {@link AnnotationDef}. For the
     * convenience of {@link TLAnnotation#equals(TLAnnotation)}.
     */
    boolean extraEquals(/*@NonNull*/ TLAnnotationDef o) {
        return retention.equals(o.retention);
    }

    /**
     * This {@link TLAnnotationDef} equals <code>o</code> if and only if
     * <code>o</code> is another nonnull {@link TLAnnotationDef},
     * <code>this</code> and <code>o</code> contain equal ordinary annotation
     * definitions {@link #def} ({@link AnnotationDef#equals(Object)}), and
     * they have the same retention policy {@link #retention}.
     */
    @Override
    public boolean equals(/*@ReadOnly*/ Object o) {
        return o instanceof TLAnnotationDef
                && equals((/*@NonNull*/ TLAnnotationDef) o);
    }

    /**
     * Returns whether this {@link TLAnnotationDef} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link TLAnnotationDef}.
     */
    public boolean equals(/*@NonNull*/ TLAnnotationDef o) {
        return def.equals(o.def) && extraEquals(o);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return def.hashCode() + retention.hashCode();
    }
    
    /**
     * Returns a <code>TLAnnotationDef</code> containing all the information
     * from both arguments, or <code>null</code> if the two arguments
     * contradict each other; see {@link AnnotationDef#unify}.
     */
    public static TLAnnotationDef unify(/*@NonNull*/ TLAnnotationDef tld1,
            /*@NonNull*/ TLAnnotationDef tld2) {
        if (tld1.equals(tld2))
            return tld1;
        else if (tld1.retention.equals(tld2.retention)) {
            AnnotationDef ud = AnnotationDef.unify(tld1.def, tld2.def);
            if (ud == null)
                return null;
            else
                return new TLAnnotationDef(ud, tld1.retention);
        } else
            return null;
    }
}
