package annotations.util.coll;

import checkers.nullness.quals.*;
import checkers.javari.quals.*;

import java.util.*;

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
    public boolean contains(/*@ReadOnly*/ Object o) /*@ReadOnly*/ {
        return theValues.contains(o);
    }

    private class KeyedSetIterator implements Iterator<V> {
        private Iterator<V> itr = theValues.iterator();

        public boolean hasNext() /*@ReadOnly*/ {
            return itr.hasNext();
        }

        public V next() /*@ReadOnly*/ {
            return itr.next();
        }

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
    public /*@PolyRead*/ Iterator<V> iterator() /*@PolyRead*/ {
        return new KeyedSetIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public /*@ReadOnly*/ Object[] toArray() /*@ReadOnly*/ {
        return theValues.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(T[] a) /*@ReadOnly*/ {
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

    private static boolean eq(/*@ReadOnly*/ Object x, /*@ReadOnly*/ Object y) {
        return x == y || (x != null && x.equals(y));
    }

    /**
     * {@inheritDoc}
     */
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
    public boolean remove(/*@ReadOnly*/ Object o) {
        return theValues.remove(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection<? extends V> c) {
        boolean changed = false;
        for (V o : c)
            changed |= add(o);
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
    public Keyer<? extends K, ? super V> getKeyer() /*@ReadOnly*/ {
        return keyer;
    }

    /**
     * {@inheritDoc}
     */
    public V replace(V v) {
        return theMap.put(keyer.getKeyFor(v), v);
    }

    /**
     * {@inheritDoc}
     */
    public V lookup(K k) {
        return theMap.get(k);
    }

    // Use inherited equals and hashCode algorithms because
    // those of HashMap.Values are broken!
}
