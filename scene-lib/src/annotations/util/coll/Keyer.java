package annotations.util.coll;

/*>>>
import org.checkerframework.checker.javari.qual.ReadOnly;
*/

/**
 * A {@link Keyer} supplies keys for the elements of a {@link KeyedSet}.
 * @param <K> the key type
 * @param <V> the element type
 */
public /*@ReadOnly*/ interface Keyer<K, V> {
    /**
     * Returns the key that this keyer wishes to assign to the element
     * <code>v</code>.
     */
    public abstract K getKeyFor(V v);
}
