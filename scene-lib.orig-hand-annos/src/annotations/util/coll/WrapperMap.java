package annotations.util.coll;

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
    protected final /*@NonNull*/ Map<K, V> back;

    /**
     * Constructs a new {@link WrapperMap} with the given backing map.
     */
    protected WrapperMap(/*@NonNull*/ /*@RoMaybe*/ Map<K, V> back) /*@RoMaybe*/ {
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
    public boolean containsKey(/*@ReadOnly*/ Object key) /*@ReadOnly*/ {
        return back.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(/*@ReadOnly*/ Object value) /*@ReadOnly*/ {
        return back.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    public /*@NonNull*/ /*@RoMaybe*/ Set</*@NonNull*/ java.util.Map.Entry<K, V>>
        entrySet() /*@RoMaybe*/ {
        return back.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    public V get(/*@ReadOnly*/ Object key) /*@ReadOnly*/ {
        return back.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() /*@ReadOnly*/ {
        return back.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public /*@NonNull*/ /*@RoMaybe*/ Set<K> keySet() /*@RoMaybe*/ {
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
    public void putAll(/*@NonNull*/ /*@ReadOnly*/ Map<? extends K, ? extends V> m) {
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
    public int size() /*@ReadOnly*/ {
        return back.size();
    }

    /**
     * {@inheritDoc}
     */
    public /*@NonNull*/ /*@RoMaybe*/ Collection<V> values() /*@RoMaybe*/ {
        return back.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(/*@ReadOnly*/ Object o) /*@ReadOnly*/ {
        return back.equals(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return back.hashCode();
    }
}
