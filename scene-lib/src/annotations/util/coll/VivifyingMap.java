package annotations.util.coll;

import checkers.nullness.quals.Nullable;
import checkers.javari.quals.*;

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
    public VivifyingMap(/*@PolyRead*/ Map<K, V> back) /*@PolyRead*/ {
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
     */
    public boolean prune() {
        boolean empty = true;
        for (Iterator<Map.Entry<K, V>> ei
                = entrySet().iterator(); ei.hasNext(); ) {
            Map.Entry<K, V> e = ei.next();
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
    protected abstract V createValueFor(K k) /*@ReadOnly*/;

    /**
     * Returns whether the given value is "empty" and thus may be discarded
     * by {@link #prune}.  Before returning, {@link #subPrune} may carry out
     * some sort of recursive pruning on the value; for example, if the value
     * is another {@link VivifyingMap}, {@link #subPrune} could prune that map.
     */
    protected abstract boolean subPrune(V v) /*@ReadOnly*/;
}
