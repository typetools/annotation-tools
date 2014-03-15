package annotations.util.coll;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.javari.qual.*;
*/

/**
 * A {@link WrapperMap} is a map all of whose methods delegate by default to
 * those of a supplied {@linkplain #back backing map}.  Subclasses can add or
 * override methods.  Compare to {@link java.io.FilterInputStream}.
 */
public class WrapperMap<K, V> implements Map<K, V> {
    /**
     * The backing map.
     */
    protected final Map<K, V> back;

    /**
     * Constructs a new {@link WrapperMap} with the given backing map.
     */
    protected /*@PolyRead*/ WrapperMap(/*@PolyRead*/ Map<K, V> back) {
        this.back = back;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        back.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(/*>>> @ReadOnly WrapperMap<K, V> this, */ /*@ReadOnly*/ Object key) {
        return back.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(/*>>> @ReadOnly WrapperMap<K, V> this, */ /*@ReadOnly*/ Object value) {
        return back.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public /*@PolyRead*/ Set<java.util.Map.Entry<K, V>>
        entrySet(/*>>> @PolyRead WrapperMap<K, V> this*/) {
        return back.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(/*>>> @ReadOnly WrapperMap<K, V> this, */ /*@ReadOnly*/ Object key) {
        return back.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty(/*>>> @ReadOnly WrapperMap<K, V> this*/) {
        return back.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public /*@PolyRead*/ Set<K> keySet(/*>>> @PolyRead WrapperMap<K, V> this*/) {
        return back.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(K key, V value) {
        return back.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(/*@ReadOnly*/ Map<? extends K, ? extends V> m) {
        back.putAll(m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V remove(/*@ReadOnly*/ Object key) {
        return back.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size(/*>>> @ReadOnly WrapperMap<K, V> this*/) {
        return back.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public /*@PolyRead*/ Collection<V> values(/*>>> @PolyRead WrapperMap<K, V> this*/) {
        return back.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(/*>>> @ReadOnly WrapperMap<K, V> this, */ /*@ReadOnly*/ Object o) {
        return back.equals(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode(/*>>> @ReadOnly WrapperMap<K, V> this*/) {
        return back.hashCode();
    }
}
