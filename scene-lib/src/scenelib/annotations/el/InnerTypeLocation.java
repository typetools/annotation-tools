package scenelib.annotations.el;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.TypePath;
import scenelib.annotations.util.Hasher;

import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;

/**
 * An {@link InnerTypeLocation} holds the location information for an
 * inner type (namely the location string) inside its {@link ATypeElement}.
 */
public final class InnerTypeLocation {

    /**
     * An {@link InnerTypeLocation} containing no locations.
     */
    public static final InnerTypeLocation EMPTY_INNER_TYPE_LOCATION = new InnerTypeLocation(
            Collections.<TypePathEntry> emptyList());

    /**
     * The location numbers of the inner type as defined in the extended
     * annotation specification.  For example, the location numbers of &#064;X
     * in <code>Foo&lt;Bar&lt;Baz, &#064;X Baz&gt;&gt;</code> (Foo<Bar<Baz, @X Baz>>) are
     * <code>{0, 1}</code>.
     */
    public final List<TypePathEntry> location;

    /**
     * Constructs an {@link InnerTypeLocation} from the given location string,
     * which must not be zero-length.  (The "inner type" of an
     * {@link ATypeElement} with zero-length location string is the
     * {@link ATypeElement} itself.)
     */
    public InnerTypeLocation(List<TypePathEntry> location) {
        this.location = Collections.unmodifiableList(
                new ArrayList<TypePathEntry>(location));
    }

    /**
     * Returns whether this {@link InnerTypeLocation} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link InnerTypeLocation}.
     */
    public boolean equals(InnerTypeLocation l) {
        return location.equals(l.location);
    }

    /**
     * This {@link InnerTypeLocation} equals <code>o</code> if and only if
     * <code>o</code> is another nonnull {@link InnerTypeLocation} and
     * <code>this</code> and <code>o</code> have equal location strings
     * {@link #location}.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof InnerTypeLocation
                && equals((InnerTypeLocation) o);
    }

    @Override
    public int hashCode() {
        Hasher h = new Hasher();
        h.mash(location.hashCode());
        return h.hash;
    }


    /**
     * Creates a String representation of the location according to the format expected by {@link org.objectweb.asm.TypePath#fromString(String)}.
     * @return a String representation according to {@link org.objectweb.asm.TypePath}.
     */
    public TypePath getTypePath() {
        StringBuilder stringBuilder = new StringBuilder();
        for (TypePathEntry typePathEntry : location) {
            switch (typePathEntry.tag) {
                case ARRAY:
                    stringBuilder.append("[");
                    break;
                case INNER_TYPE:
                    stringBuilder.append(".");
                    break;
                case WILDCARD:
                    stringBuilder.append("*");
                    break;
                case TYPE_ARGUMENT:
                    stringBuilder.append(typePathEntry.arg);
                    break;
            }
        }
        return TypePath.fromString(stringBuilder.toString());
    }

    /**
     * Returns a string representation of this {@link InnerTypeLocation}.
     * The representation looks like this:
     *
     * <pre>InnerTypeLocation([0, 1, 2])</pre>
     */
    @Override
    public  String toString() {
        return "InnerTypeLocation(" + location.toString() + ")";
    }
}
