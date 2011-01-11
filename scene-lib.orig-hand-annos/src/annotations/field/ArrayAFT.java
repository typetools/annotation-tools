package annotations.field;

import annotations.*;

/**
 * An {@link ArrayAFT} represents an annotation field type that is an array.
 */
public final /*@Unmodifiable*/ class ArrayAFT extends AnnotationFieldType {
    @Override
    void onlyPackageMayExtend() {
    }

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
    public /*@NonNull*/ String toString() {
        return (elementType == null ? "unknown" :
            ((/*@NonNull*/ ScalarAFT) elementType).toString()) + "[]";
    }
}
