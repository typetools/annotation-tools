package annotations.field;

/**
 * An {@link EnumAFT} is the type of an annotation field that can hold an
 * constant from a certain enumeration type.
 */
public final /*@Unmodifiable*/ class EnumAFT extends ScalarAFT {
    @Override
    void onlyPackageMayExtend() {
    }

    /**
     * The name of the enumeration type whose constants the annotation field
     * can hold.
     */
    public final /*@NonNull*/ String typeName;

    /**
     * Constructs an {@link EnumAFT} for an annotation field that can hold
     * constants of the enumeration type with the given name.
     */
    public EnumAFT(/*@NonNull*/ String typeName) {
        this.typeName = typeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public /*@NonNull*/ String toString() {
        return "enum " + typeName;
    }
}
