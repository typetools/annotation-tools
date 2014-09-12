package annotations.util;

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
    public abstract /*@NonNull*/ String toString() /*@ReadOnly*/;

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(/*@ReadOnly*/ Object that) /*@ReadOnly*/ {
        return that != null && this.getClass() == that.getClass()
                && this.toString().equals(that.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() /*@ReadOnly*/ {
        return toString().hashCode();
    }
}
