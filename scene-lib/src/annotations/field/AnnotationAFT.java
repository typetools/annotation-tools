package annotations.field;

/*>>>
import checkers.nullness.quals.*;
import checkers.javari.quals.*;
*/

import annotations.Annotation;
import annotations.el.AnnotationDef;

/**
 * An {@link AnnotationAFT} represents a subannotation as the type of an
 * annotation field and contains the definition of the subannotation.
 */
public final /*@ReadOnly*/ class AnnotationAFT extends ScalarAFT {

    /**
     * The definition of the subannotation.
     */
    public final AnnotationDef annotationDef;

    /**
     * Constructs a new {@link AnnotationAFT} for a subannotation of the
     * given definition.
     */
    public AnnotationAFT(AnnotationDef annotationDef) {
        this.annotationDef = annotationDef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidValue(Object o) {
        return o instanceof Annotation;
    }

    /**
     * The string representation of an {@link AnnotationAFT} looks like
     * <code>&#64;Foo</code> even though the subannotation definition is
     * logically part of the {@link AnnotationAFT}.  This is because the
     * subannotation field type appears as <code>&#64;Foo</code> in an
     * index file and the subannotation definition is written separately.
     */
    @Override
    public  String toString() {
        return "annotation-field " + annotationDef.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(Object o) {
        return o.toString();
    }

}
