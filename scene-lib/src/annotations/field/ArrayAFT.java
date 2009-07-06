package annotations.field;

import checkers.nullness.quals.*;
import checkers.javari.quals.*;

import annotations.*;

/**
 * An {@link ArrayAFT} represents an annotation field type that is an array.
 */
public final /*@ReadOnly*/ class ArrayAFT extends AnnotationFieldType {

    /**
     * The element type of the array, or <code>null</code> if it is unknown
     * (see {@link AnnotationBuilder#addEmptyArrayField}).
     */
    public final ScalarAFT elementType;

    /**
     * Constructs a new {@link ArrayAFT} representing an array type with
     * the given element type.  <code>elementType</code> may be
     * <code>null</code> to indicate that the element type is unknown
     * (see {@link AnnotationBuilder#addEmptyArrayField}).
     */
    public ArrayAFT(ScalarAFT elementType) {
        this.elementType = elementType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public  String toString() {
        return (elementType == null ? "unknown" :
            ((ScalarAFT) elementType).toString()) + "[]";
    }
}
