package annotations.field;

/*>>>
import checkers.nullness.quals.*;
import checkers.javari.quals.*;
*/

/**
 * An {@link EnumAFT} is the type of an annotation field that can hold an
 * constant from a certain enumeration type.
 */
public final /*@ReadOnly*/ class EnumAFT extends ScalarAFT {

    /**
     * The name of the enumeration type whose constants the annotation field
     * can hold.
     */
    public final String typeName;

    /**
     * Constructs an {@link EnumAFT} for an annotation field that can hold
     * constants of the enumeration type with the given name.
     */
    public EnumAFT(String typeName) {
        this.typeName = typeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidValue(Object o) {
        // return o instanceof Enum;
        return o instanceof String;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public  String toString() {
        return "enum " + typeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(Object o) {
        return typeName + "." + o.toString();
    }

}
