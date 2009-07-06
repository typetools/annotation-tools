package annotations.field;

import checkers.nullness.quals.*;
import checkers.javari.quals.*;

import java.util.*;

import annotations.util.coll.*;

/**
 * A <code>BasicAFT</code> represents a primitive or {@link String} annotation
 * field type. Get one using {@link #forType(Class)}.
 */
// should be an enum except they can't be generic and can't extend a class
public final /*@ReadOnly*/ class BasicAFT extends ScalarAFT {

    /**
     * The Java type backing this annotation field type.
     */
    public final Class<?> type;

    private BasicAFT(Class<?> type) {
        this.type = type;
    }

    /**
     * Returns the <code>BasicAFT</code> for <code>type</code>, which
     * should be primitive (e.g., int.class) or String.  Returns null if
     * <code>type</code> is not appropriate for a basic annotation field
     * type.
     */
    public static BasicAFT forType(Class<?> type) {
        return bafts.get(type);
    }

    /**
     * Maps from {@link #type} to <code>BasicAFT</code>.
     * Contains every BasicAFT.
     */
    // Disgusting reason for being public; need to fix.
    public static final /*@ReadOnly*/ Map<Class<?>, BasicAFT> bafts;

    static {
        Map<Class<?>, BasicAFT> tempBafts =
            new HashMap<Class<?>, BasicAFT>(9);
        tempBafts.put(byte.class, new BasicAFT(byte.class));
        tempBafts.put(short.class, new BasicAFT(short.class));
        tempBafts.put(int.class, new BasicAFT(int.class));
        tempBafts.put(long.class, new BasicAFT(long.class));
        tempBafts.put(float.class, new BasicAFT(float.class));
        tempBafts.put(double.class, new BasicAFT(double.class));
        tempBafts.put(char.class, new BasicAFT(char.class));
        tempBafts.put(boolean.class, new BasicAFT(boolean.class));
        tempBafts.put(String.class, new BasicAFT(String.class));
        // bafts = Collections2.<Class<?>, BasicAFT>unmodifiableKeyedSet(tempBafts);
        // bafts = bafts2;
        bafts = tempBafts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (type == String.class)
            return "String";
        else
            return type.getName();
    }

}
