package annotations.util;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.javari.qual.*;
*/

/**
 * {@link EqualByStringRepresentation} is a "mix-in" class for objects that are
 * equal if and only if their {@link #toString toString} representations are
 * equal.  {@link EqualByStringRepresentation} provides implementations of
 * {@link #equals} and {@link #hashCode} in terms of {@link #toString}.
 */
public abstract class EqualByStringRepresentation {
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String toString(/*>>> @ReadOnly EqualByStringRepresentation this*/);

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(/*>>> @ReadOnly EqualByStringRepresentation this, */ /*@ReadOnly*/ Object that) {
        return that != null && this.getClass() == that.getClass()
                && this.toString().equals(that.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode(/*>>> @ReadOnly EqualByStringRepresentation this*/) {
        return toString().hashCode();
    }
}
