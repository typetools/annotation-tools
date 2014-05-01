package annotations;

import annotations.el.*;

/**
 * A top-level annotation containing an ordinary annotation plus a retention
 * policy.  These are attached to {@link AElement}s.
 */
public final /*@Unmodifiable*/ class TLAnnotation<A extends Annotation> {

    /**
     * The top-level annotation definition, which contains the retention policy
     * and the ordinary definition.
     */
    public final /*@NonNull*/ TLAnnotationDef tldef;

    /**
     * The ordinary annotation, which contains the data and the ordinary
     * definition.
     */
    public final /*@NonNull*/ A ann;

    /**
     * Returns the ordinary annotation definition (shortcut for
     * <code>{@link #tldef}.{@link TLAnnotationDef#def}</code> or
     * <code>{@link #ann}.{@link Annotation#def()}</code>).
     */
    public /*@NonNull*/ AnnotationDef def() {
        return tldef.def;
    }

    /**
     * Wraps the given annotation in a top-level annotation using the given
     * top-level annotation definition, which provides a retention policy.
     */
    public TLAnnotation(/*@NonNull*/ TLAnnotationDef tldef, /*@NonNull*/ A ann) {
        if (!ann.def().equals(tldef.def))
            throw new IllegalArgumentException("Definitions mismatch");
        this.tldef = tldef;
        this.ann = ann;
    }

    /**
     * Wraps the given annotation in a top-level annotation with the given
     * retention policy, generating the top-level annotation definition
     * automatically for convenience.
     */
    public TLAnnotation(/*@NonNull*/ A ann1,
            /*@NonNull*/ RetentionPolicy retention) {
        this(new TLAnnotationDef(ann1.def(), retention), ann1);
    }

    /**
     * This {@link TLAnnotation} equals <code>o</code> if and only if
     * <code>o</code> is another nonnull {@link TLAnnotation}, <code>this</code>
     * and <code>o</code> contain equal ordinary annotations {@link #ann}
     * ({@link Annotation#equals(Object)}), and they have the same retention
     * policy.
     */
    @Override
    public boolean equals(/*@ReadOnly*/ Object o) {
        return o instanceof TLAnnotation
                && equals((/*@NonNull*/ TLAnnotation) o);
    }

    /**
     * Returns whether this {@link TLAnnotation} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link TLAnnotation}.
     */
    public boolean equals(/*@NonNull*/ TLAnnotation<?> o) {
        // extraEquals lets us avoid comparing the ordinary definition twice
        return ann.equals(o.ann) && tldef.extraEquals(o.tldef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return tldef.hashCode() + ann.hashCode();
    }
}
