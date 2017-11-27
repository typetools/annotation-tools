package scenelib.annotations.field;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
*/

/**
 * An {@link EnumAFT} is the type of an annotation field that can hold an
 * constant from a certain enumeration type.
 */
public final class EnumAFT extends ScalarAFT {

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
        String fieldValue = o.toString();
        if (!fieldValue.contains(".")) {
            // If fieldValue is not qualified, prepend the typeName
            return typeName + "." + fieldValue;
        }
        return fieldValue;
    }


    @Override
    public <R, T> R accept(AFTVisitor<R, T> v, T arg) {
        return v.visitEnumAFT(this, arg);
    }
}
