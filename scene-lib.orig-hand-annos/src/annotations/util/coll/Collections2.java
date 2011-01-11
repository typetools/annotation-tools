package annotations.util.coll;

import java.util.*;

/**
 * {@link Collections2} has some useful collection-related static members that
 * supplement the ones in {@link Collections}.
 */
public abstract class Collections2 {

    // HMMM what do we do for readonly implementations of an interface
    // with both reading and writing methods?
    private static /*@Unmodifiable*/ class UnmodifiableKeyedSet<K, V> implements
            KeyedSet<K, V> {
        private final /*@NonNull*/ /*@ReadOnly*/ KeyedSet<K, V> back;

        private final /*@NonNull*/ /*@ReadOnly*/ Set<V> unmod1;

        UnmodifiableKeyedSet(/*@NonNull*/ /*@ReadOnly*/ KeyedSet<K, V> back) {
            this.back = back;
            this.unmod1 = Collections.unmodifiableSet(this.back);
        }

        public /*@NonNull*/ Keyer<? extends K, ? super V> getKeyer() {
            return back.getKeyer();
        }

        public boolean add(V v) /*???*/ {
            throw new UnsupportedOperationException();
        }
        
        public V replace(V v) /*???*/ {
            throw new UnsupportedOperationException();
        }

        public V add(V v, int conflictBehavior, int equalBehavior) /*???*/ {
            throw new UnsupportedOperationException();
        }

        public V lookup(K k) /*@ReadOnly*/ {
            return back.lookup(k);
        }

        public int size() /*@ReadOnly*/ {
            return unmod1.size();
        }

        public boolean isEmpty() /*@ReadOnly*/ {
            return unmod1.isEmpty();
        }

        public boolean contains(/*@ReadOnly*/ Object o) /*@ReadOnly*/ {
            return unmod1.contains(o);
        }

        // HMMM the original signature is romaybe Iterator<V> iterator() romaybe,
        // but the receiver has to be readonly; make sure the override is allowed
        public /*@NonNull*/ /*@ReadOnly*/ Iterator<V> iterator() /*@ReadOnly*/ {
            return unmod1.iterator();
        }

        public /*@ReadOnly*/ Object[/*@NonNull*/] toArray() /*@ReadOnly*/ {
            return unmod1.toArray();
        }

        public <T> T[/*@NonNull*/] toArray(T[] a) /*@ReadOnly*/ {
            return unmod1.toArray(a);
        }

        public boolean remove(/*@ReadOnly*/ Object o) /*???*/ {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(/*@NonNull*/ /*@ReadOnly*/ Collection<?> c) /*@ReadOnly*/ {
            return unmod1.containsAll(c);
        }

        public boolean addAll(
                /*@NonNull*/ /*@ReadOnly*/ Collection<? extends V> c) /*???*/ {
            // Following Collections.UnmodifiableSet, we throw an exception
            // even if the addAll call would not actually have changed the set.
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(/*@NonNull*/ /*@ReadOnly*/ Collection<?> c) /*???*/ {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(/*@NonNull*/ /*@ReadOnly*/ Collection<?> c) /*???*/ {
            throw new UnsupportedOperationException();
        }

        public void clear() /*???*/ {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns an unmodifiable view of the given {@link KeyedSet}; similar to
     * {@link Collections#unmodifiableSet}.
     */
    // V can't be covariant because of the keyer.  There's probably a way to
    // fix this but I'm not going to bother.
    public static final <K, V> /*@NonNull*/ /*@ReadOnly*/ KeyedSet<K, V>
        unmodifiableKeyedSet(/*@NonNull*/ /*@ReadOnly*/ KeyedSet<K, V> back) {
        UnmodifiableKeyedSet<K, V> uks = new UnmodifiableKeyedSet<K, V>(back);
        return uks;
    }
}
