package annotations.util.coll;

import java.util.*;

/**
 * A simple implementation of {@link KeyedSet} backed by an insertion-order
 * {@link java.util.LinkedHashMap} and its
 * {@link java.util.LinkedHashMap#values() value collection}.
 */
public class LinkedHashKeyedSet<K, V> extends AbstractSet<V> implements
        KeyedSet<K, V> {
    private final /*@NonNull*/ Keyer<? extends K, ? super V> keyer;

    private final /*@NonNull*/ Map<K, V> theMap = new LinkedHashMap<K, V>();

    final /*@NonNull*/ Collection<V> theValues = theMap.values();

    /**
     * Constructs a {@link LinkedHashKeyedSet} that uses the given
     * {@link Keyer} to obtain keys for elements.
     */
    public LinkedHashKeyedSet(/*@NonNull*/ Keyer<? extends K, ? super V> keyer) {
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
    public boolean contains(/*@ReadOnly*/ Object o) {
        return theValues.contains(o);
    }

    private class KeyedSetIterator implements Iterator<V> {
        private /*@NonNull*/ Iterator<V> itr = theValues.iterator();

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
    public /*@NonNull*/ Iterator<V> iterator() {
        return new KeyedSetIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[/*@NonNull*/] toArray() {
        return theValues.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[/*@NonNull*/] toArray(T[] a) {
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
    public boolean addAll(/*@NonNull*/ Collection<? extends V> c) {
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
    public /*@NonNull*/ Keyer<? extends K, ? super V> getKeyer() {
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
