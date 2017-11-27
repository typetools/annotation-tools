package scenelib.annotations.util.coll;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
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
    protected WrapperMap(Map<K, V> back) {
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
    public boolean containsKey(Object key) {
        return back.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        return back.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<java.util.Map.Entry<K, V>>
        entrySet() {
        return back.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(Object key) {
        return back.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return back.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<K> keySet() {
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
    public void putAll(Map<? extends K, ? extends V> m) {
        back.putAll(m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V remove(Object key) {
        return back.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return back.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V> values() {
        return back.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        return back.equals(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return back.hashCode();
    }
}
