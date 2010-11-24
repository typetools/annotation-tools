package annotations.el;

import checkers.nullness.quals.*;
import checkers.javari.quals.*;

import annotations.util.*;

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
     * Constructs a new {@link RelativeLocation}; the arguments are assigned to
     * the fields of the same names.
     * Use -1 for the relative location that you're not using
     */
    private RelativeLocation(int offset, int index) {
    	this.offset = offset;
    	this.index = index;
    }
    
    public static RelativeLocation createOffset(int offset) {
    	return new RelativeLocation(offset, -1);
    }

    public static RelativeLocation createIndex(int index) {
    	return new RelativeLocation(-1, index);
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
        return offset == l.offset && index == l.index;
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
    public int hashCode() /*@ReadOnly*/ {
		Hasher h = new Hasher();
		h.mash(offset);
		h.mash(index);
		return h.hash;
    }

    @Override
    public String toString() {
    	return "RelativeLocation(" + getLocationString() + ")";
    }
}
