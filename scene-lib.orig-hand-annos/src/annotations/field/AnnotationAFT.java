package annotations.field;

import annotations.*;

/**
 * An {@link AnnotationAFT} represents a subannotation as the type of an
 * annotation field and contains the definition of the subannotation.
 */
public final /*@Unmodifiable*/ class AnnotationAFT extends ScalarAFT {
    @Override
    void onlyPackageMayExtend() {
    }

    /**
     * The definition of the subannotation.
     */
    public final /*@NonNull*/ AnnotationDef annotationDef;

    /**
     * Constructs a new {@link AnnotationAFT} for a subannotation of the
     * given definition.
     */
    public AnnotationAFT(/*@NonNull*/ AnnotationDef annotationDef) {
        this.annotationDef = annotationDef;
    }

    /**
     * The string representation of an {@link AnnotationAFT} looks like
     * <code>&#64;Foo</code> even though the subannotation definition is
     * logically part of the {@link AnnotationAFT}.  This is because the
     * subannotation field type appears as <code>&#64;Foo</code> in an
     * index file and the subannotation definition is written separately.
     */
    @Override
    public /*@NonNull*/ String toString() {
        return "@" + annotationDef.name;
    }
}
