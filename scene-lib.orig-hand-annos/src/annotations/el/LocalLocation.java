package annotations.el;

import annotations.util.*;

/**
 * A {@link LocalLocation} holds location information for a local
 * variable: slot index, scope start, and scope length.
 */
public final /*@Unmodifiable*/ class LocalLocation {
    /**
     * The slot index of the local variable. 
     */
    public final int index;

    /**
     * The start of the local variable's scope (or live range), as an offset
     * from the beginning of the method code in bytes. 
     */
    public final int scopeStart;

    /**
     * The length of the local variable's scope (or live range), in bytes.
     */
    public final int scopeLength;

    /**
     * Constructs a new {@link LocalLocation}; the arguments are assigned to
     * the fields of the same names.
     */
    public LocalLocation(int index, int scopeStart, int scopeLength) {
        this.index = index;
        this.scopeStart = scopeStart;
        this.scopeLength = scopeLength;
    }

    /**
     * Returns whether this {@link LocalLocation} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link LocalLocation}.
     */
    public boolean equals(/*@NonNull*/ LocalLocation l) {
        return index == l.index && scopeStart == l.scopeStart
                && scopeLength == l.scopeLength;
    }

    /**
     * This {@link LocalLocation} equals <code>o</code> if and only if
     * <code>o</code> is another nonnull {@link LocalLocation} and
     * <code>this</code> and <code>o</code> have equal {@link #index},
     * {@link #scopeStart}, and {@link #scopeLength}.
     */
    @Override
    public boolean equals(/*@ReadOnly*/ Object o) {
        return o instanceof LocalLocation
                && equals((/*@NonNull*/ LocalLocation) o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        Hasher h = new Hasher();
        h.mash(index);
        h.mash(scopeStart);
        h.mash(scopeLength);
        return h.hash;
    }
}
