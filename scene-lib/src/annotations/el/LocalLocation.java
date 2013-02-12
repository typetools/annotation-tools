package annotations.el;

import annotations.util.Hasher;

/*>>>
import checkers.nullness.quals.*;
import checkers.javari.quals.*;
*/

/**
 * A {@link LocalLocation} holds location information for a local
 * variable: slot index, scope start, and scope length.
 */
public final /*@ReadOnly*/ class LocalLocation {
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
        this.varName = null;
        this.varIndex = -1;
    }

    public final String varName;
    public final int varIndex;

    public LocalLocation(String varName, int varIndex) {
        this.index = -1;
        this.scopeStart = -1;
        this.scopeLength = -1;
        this.varName = varName;
        this.varIndex = varIndex;
    }


    /**
     * Returns whether this {@link LocalLocation} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link LocalLocation}.
     */
    public boolean equals(LocalLocation l) {
        return index == l.index && scopeStart == l.scopeStart
                && scopeLength == l.scopeLength &&
                (varName==null || varName.equals(l.varName)) &&
                varIndex==l.varIndex;
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
                && equals((LocalLocation) o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode(/*>>> @ReadOnly LocalLocation this*/) {
        Hasher h = new Hasher();
        if (varName==null) {
            h.mash(index);
            h.mash(scopeStart);
            h.mash(scopeLength);
        } else {
            h.mash(varName.hashCode());
            h.mash(varIndex);
        }
        return h.hash;
    }

    @Override
    public String toString() {
        if (varName==null) {
            return "LocalLocation(" + index + ", " + scopeStart + ", " + scopeLength + ")";
        } else {
            return "LocalLocation(\"" + varName + "\" #" + varIndex + ")";
        }
    }
}
