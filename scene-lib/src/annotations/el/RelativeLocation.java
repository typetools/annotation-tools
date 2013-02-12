package annotations.el;

import annotations.util.Hasher;

/*>>>
import checkers.nullness.quals.*;
import checkers.javari.quals.*;
*/

/**
 * A {@link RelativeLocation} holds location information for a
 * instanceof, cast, or new: either the bytecode offset or the source code index.
 * I call instanceof, cast, or new "the construct".
 */
public final /*@ReadOnly*/ class RelativeLocation {
    /**
     * The bytecode offset of the construct.
     */
    public final int offset;

    /**
     * The source code index of the construct.
     */
    public final int index;

    /**
     * The type index used for intersection types in casts.
     */
    public final int type_index;

    /**
     * Constructs a new {@link RelativeLocation}; the arguments are assigned to
     * the fields of the same names.
     * Use -1 for the relative location that you're not using
     */
    private RelativeLocation(int offset, int index, int type_index) {
        this.offset = offset;
        this.index = index;
        this.type_index = type_index;
    }

    public static RelativeLocation createOffset(int offset, int type_index) {
        return new RelativeLocation(offset, -1, type_index);
    }

    public static RelativeLocation createIndex(int index, int type_index) {
        return new RelativeLocation(-1, index, type_index);
    }

    public boolean isBytecodeOffset() {
        return offset>-1;
    }

    public String getLocationString() {
        if (isBytecodeOffset()) {
            return "#" + offset;
        } else {
            return "*" + index;
        }
    }

    /**
     * Returns whether this {@link RelativeLocation} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link RelativeLocation}.
     */
    public boolean equals(RelativeLocation l) {
        return offset == l.offset && index == l.index && type_index == l.type_index;
    }

    /**
     * This {@link RelativeLocation} equals <code>o</code> if and only if
     * <code>o</code> is another nonnull {@link RelativeLocation} and
     * <code>this</code> and <code>o</code> have equal {@link #offset} and {@link #index}.
     */
    @Override
    public boolean equals(/*@ReadOnly*/ Object o) {
        return o instanceof RelativeLocation
                && equals((RelativeLocation) o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode(/*>>> @ReadOnly RelativeLocation this*/) {
        Hasher h = new Hasher();
        h.mash(offset);
        h.mash(index);
        h.mash(type_index);
        return h.hash;
    }

    @Override
    public String toString() {
        return "RelativeLocation(" + getLocationString() + ")";
    }
}
