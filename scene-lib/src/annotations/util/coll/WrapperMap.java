package annotations.util.coll;

import checkers.nullness.quals.Nullable;
import checkers.javari.quals.*;

import java.util.*;

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
    protected WrapperMap(/*>>> @PolyRead WrapperMap<K, V> this, */ /*@PolyRead*/ Map<K, V> back) {
        this.back = back;
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        back.clear();
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(/*>>> @ReadOnly WrapperMap<K, V> this, */ /*@ReadOnly*/ Object key) {
        return back.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(/*>>> @ReadOnly WrapperMap<K, V> this, */ /*@ReadOnly*/ Object value) {
        return back.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    public /*@PolyRead*/ Set<java.util.Map.Entry<K, V>>
        entrySet(/*>>> @PolyRead WrapperMap<K, V> this*/) {
        return back.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    public V get(/*>>> @ReadOnly WrapperMap<K, V> this, */ /*@ReadOnly*/ Object key) {
        return back.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty(/*>>> @ReadOnly WrapperMap<K, V> this*/) {
        return back.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public /*@PolyRead*/ Set<K> keySet(/*>>> @PolyRead WrapperMap<K, V> this*/) {
        return back.keySet();
    }

    /**
     * {@inheritDoc}
     */
    public V put(K key, V value) {
        return back.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    public void putAll(/*@ReadOnly*/ Map<? extends K, ? extends V> m) {
        back.putAll(m);
    }

    /**
     * {@inheritDoc}
     */
    public V remove(/*@ReadOnly*/ Object key) {
        return back.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    public int size(/*>>> @ReadOnly WrapperMap<K, V> this*/) {
        return back.size();
    }

    /**
     * {@inheritDoc}
     */
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
