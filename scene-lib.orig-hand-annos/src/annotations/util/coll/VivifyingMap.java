package annotations.util.coll;

import java.util.*;

/**
 * A {@link VivifyingMap} is a map that can create "empty" values on demand
 * and prune "empty" values, for some definition of "empty".
 */
public abstract class VivifyingMap<K, V> extends WrapperMap<K, V> {
    /**
     * Constructs a new {@link VivifyingMap} backed by the given map.  All
     * reads and writes to this {@link VivifyingMap} go through to the backing
     * map.  However, since the {@link VivifyingMap} generally provides a
     * superset of the functionality of the backing map, it is rarely useful to
     * access the backing map directly; the parameter is given mainly so you
     * can provide a new map of your favorite class ({@link HashMap},
     * {@link LinkedHashMap}, etc.).
     */
    public VivifyingMap(/*@NonNull*/ /*@RoMaybe*/ Map<K, V> back) /*@RoMaybe*/ {
        super(back);
    }

    /**
     * Returns the value to which the specified key is mapped; if the key is
     * not currently mapped to a value, a new, empty value is created, stored,
     * and returned.
     */
    public V vivify(K k) {
        if (containsKey(k))
            return get(k);
        else {
            V v = createValueFor(k);
            put(k, v);
            return v;
        }
    }

    /**
     * Prunes this map by deleting entries with empty values (i.e., entries
     * that could be recreated by {@link #vivify} without information loss).
     * @return true if the map is now empty
     */
    public boolean prune() {
        boolean empty = true;
        for (/*@NonNull*/ Iterator</*@NonNull*/ Map.Entry<K, V>> ei
                = entrySet().iterator(); ei.hasNext(); ) {
            /*@NonNull*/ Map.Entry<K, V> e = ei.next();
            boolean subEmpty = subPrune(e.getValue());
            if (subEmpty)
                ei.remove();
            else
                empty = false;
        }
        return empty;
    }

    /**
     * Returns a new, "empty" value to which the key <code>k</code> can be
     * mapped; subclasses must implement.
     */
    protected abstract V createValueFor(K k);

    /**
     * Returns whether the given value is "empty" and thus may be discarded
     * by {@link #prune}.  Before returning, {@link #subPrune} may carry out
     * some sort of recursive pruning on the value; for example, if the value
     * is another {@link VivifyingMap}, {@link #subPrune} could prune that map.
     */
    protected abstract boolean subPrune(V v);
}
