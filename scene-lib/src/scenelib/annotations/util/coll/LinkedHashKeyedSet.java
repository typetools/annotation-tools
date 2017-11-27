package scenelib.annotations.util.coll;

import java.util.*;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
*/

/**
 * A simple implementation of {@link KeyedSet} backed by an insertion-order
 * {@link java.util.LinkedHashMap} and its
 * {@link java.util.LinkedHashMap#values() value collection}.
 */
public class LinkedHashKeyedSet<K, V> extends AbstractSet<V> implements KeyedSet<K, V> {
    private final Keyer<? extends K, ? super V> keyer;

    private final Map<K, V> theMap = new LinkedHashMap<K, V>();

    final Collection<V> theValues = theMap.values();

    /**
     * Constructs a {@link LinkedHashKeyedSet} that uses the given
     * {@link Keyer} to obtain keys for elements.
     */
    public LinkedHashKeyedSet(Keyer<? extends K, ? super V> keyer) {
        this.keyer = keyer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return theValues.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        return theValues.contains(o);
    }

    private class KeyedSetIterator implements Iterator<V> {
        private final Iterator<V> itr = theValues.iterator();

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public V next() {
            return itr.next();
        }

        @Override
        public void remove() {
            itr.remove();
        }

        KeyedSetIterator() {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<V> iterator() {
        return new KeyedSetIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return theValues.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return theValues.toArray(a);
    }

    private boolean checkAdd(int behavior, V old) {
        switch (behavior) {
        case REPLACE:
            remove(old);
            return true;
        case IGNORE:
            return false;
        case THROW_EXCEPTION:
            throw new IllegalStateException();
        default:
            throw new IllegalArgumentException();
        }
    }

    private static boolean eq(Object x, Object y) {
        return x == y || (x != null && x.equals(y));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V add(V o, int conflictBehavior, int equalBehavior) {
        K key = keyer.getKeyFor(o);
        V old = theMap.get(key);
        if (old == null
                || (eq(o, old) ? checkAdd(equalBehavior, old) : checkAdd(
                        conflictBehavior, old)))
            theMap.put(key, o);
        return old;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(V o) {
        return add(o, THROW_EXCEPTION, IGNORE) == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        return theValues.remove(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection<? extends V> c) {
        boolean changed = false;
        for (V o : c) {
            changed |= add(o);
        }
        return changed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        theValues.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Keyer<? extends K, ? super V> getKeyer() {
        return keyer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V replace(V v) {
        return theMap.put(keyer.getKeyFor(v), v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V lookup(K k) {
        return theMap.get(k);
    }

    // Use inherited equals and hashCode algorithms because
    // those of HashMap.Values are broken!
}
