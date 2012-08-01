package annotations.field;

/*>>>
import checkers.nullness.quals.*;
import checkers.javari.quals.*;
*/

/**
 * A {@link ClassTokenAFT} is the type of an annotation field that holds a
 * class token (something like <code>{@link String}.class</code>).
 * Even if the field type was originally some parameterization
 * <code>{@link Class}&lt;...&gt;</code>, the annotation scene library
 * represents it as a plain {@link ClassTokenAFT}.  Use the singleton
 * {@link #ctaft}.
 */
public final /*@ReadOnly*/ class ClassTokenAFT extends ScalarAFT {

    // On 2006.07.07 we decided against parameterizations because
    // class files that use annotations don't contain them.

    // The type arguments, if any, of the field type
    // Could be "" or "<HashMap>" (stupid) or "<? extends PrettyPrinter>", etc.,
    // but not null.
    /* public final String parameterization; */

    private ClassTokenAFT() {}

    /**
     * The singleton {@link ClassTokenAFT}.
     */
    public static final ClassTokenAFT ctaft = new ClassTokenAFT();

    //public ClassTokenAFT(/* String parameterization */) {
    //    /* this.parameterization = parameterization; */
    //}

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidValue(Object o) {
        return 	o instanceof java.lang.Class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Class"/* + parameterization */;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(Object o) {
        return ((java.lang.Class)o).getName() + ".class";
    }

}
