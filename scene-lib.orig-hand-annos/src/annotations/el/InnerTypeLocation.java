package annotations.el;

import java.util.*;

import annotations.util.*;

/**
 * An {@link InnerTypeLocation} holds the location information for an
 * inner type (namely the location string) inside its {@link ATypeElement}.
 */
public final /*@Unmodifiable*/ class InnerTypeLocation {
    /**
     * The location numbers of the inner type as defined in the extended
     * annotation specification.  For example, the location numbers of &#064;X
     * in <code>Foo&lt;Bar&lt;Baz, &#064;X Baz&gt;&gt;</code> are
     * <code>{0, 1}</code>.
     */
    public final /*@NonNull*/ /*@ReadOnly*/ List</*@NonNull*/ Integer> location;

    /**
     * Constructs an {@link InnerTypeLocation} from the given location string,
     * which must not be zero-length.  (The "inner type" of an
     * {@link ATypeElement} with zero-length location string is the
     * {@link ATypeElement} itself.)
     */
    public InnerTypeLocation(/*@NonNull*/
    /*@ReadOnly*/ List</*@NonNull*/ Integer> location) {
        this.location = Collections.unmodifiableList(
                new ArrayList</*@NonNull*/ Integer>(location));
    }

    /**
     * Returns whether this {@link InnerTypeLocation} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link InnerTypeLocation}.
     */
    public boolean equals(/*@NonNull*/ InnerTypeLocation l) {
        return location.equals(l.location);
    }

    /**
     * This {@link InnerTypeLocation} equals <code>o</code> if and only if
     * <code>o</code> is another nonnull {@link InnerTypeLocation} and
     * <code>this</code> and <code>o</code> have equal location strings
     * {@link #location}.
     */
    @Override
    public boolean equals(/*@ReadOnly*/ Object o) {
        return o instanceof InnerTypeLocation
                && equals((/*@NonNull*/ InnerTypeLocation) o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        /*@NonNull*/ Hasher h = new Hasher();
        h.mash(location.hashCode());
        return h.hash;
    }
    
    /**
     * Returns a string representation of this {@link InnerTypeLocation}.
     * The representation looks like this:
     * 
     * <pre>InnerTypeLocation([0, 1, 2])</pre>
     */
    @Override
    public /*@NonNull*/ String toString() {
        return "InnerTypeLocation(" + location.toString() + ")";
    }
}
