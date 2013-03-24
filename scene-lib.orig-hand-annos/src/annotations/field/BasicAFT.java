package annotations.field;

import annotations.util.coll.*;

/**
 * A <code>BasicAFT</code> represents a primitive or {@link String} annotation
 * field type. Get one using {@link #forType(Class)}.
 */
// should be an enum except they can't be generic and can't extend a class
public final /*@Unmodifiable*/ class BasicAFT extends ScalarAFT {
    @Override
    void onlyPackageMayExtend() {
    }

    private static final Keyer</*@NonNull*/ Class<?>, /*@NonNull*/ BasicAFT> typeKeyer =
            new Keyer</*@NonNull*/ Class<?>, /*@NonNull*/ BasicAFT>() {
                public /*@NonNull*/ Class<?> getKeyFor(
                /*@NonNull*/ BasicAFT baft) {
                    return baft.type;
                }
            };

    /**
     * A set of all <code>BasicAFT</code>s, keyed by {@link #type}.
     */
    public static final /*@NonNull*/ /*@ReadOnly*/ KeyedSet</*@NonNull*/ Class<?>, /*@NonNull*/ BasicAFT> bafts;

    // if correlated type arguments were supported I could do something like
    // <X> KeyedSet<Class<X>, BasicAFT<X>>; alas no, so unchecked warning

    /**
     * Returns the <code>BasicAFT</code> for <code>type</code>. Do not pass
     * <code>Integer.class</code> for <code>type</code>; instead pass
     * <code>int.class</code>. Returns null if <code>type</code> is not
     * appropriate for a basic annotation field type. Equivalent to a
     * {@link KeyedSet#lookup lookup} on {@link #bafts} but incurs the unchecked
     * warning for you.
     */
    public static BasicAFT forType(/*@NonNull*/ Class<?> type) {
        return bafts.lookup(type);
    }

    static {
        /*@NonNull*/ LinkedHashKeyedSet</*@NonNull*/ Class<?>, /*@NonNull*/ BasicAFT> tempBafts =
                new LinkedHashKeyedSet</*@NonNull*/ Class<?>, /*@NonNull*/ BasicAFT>(
                        typeKeyer);
        tempBafts.add(new BasicAFT(byte.class));
        tempBafts.add(new BasicAFT(short.class));
        tempBafts.add(new BasicAFT(int.class));
        tempBafts.add(new BasicAFT(long.class));
        tempBafts.add(new BasicAFT(float.class));
        tempBafts.add(new BasicAFT(double.class));
        tempBafts.add(new BasicAFT(char.class));
        tempBafts.add(new BasicAFT(boolean.class));
        tempBafts.add(new BasicAFT(String.class));
        bafts = Collections2.unmodifiableKeyedSet(tempBafts);
    }

    /**
     * The Java type backing this annotation field type.
     */
    public final /*@NonNull*/ Class<?> type;

    private BasicAFT(/*@NonNull*/ Class<?> type) {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public /*@NonNull*/ String toString() {
        if (type == String.class)
            return "String";
        else
            return type.getName();
    }
}
