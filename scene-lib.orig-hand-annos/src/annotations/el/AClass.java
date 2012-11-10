package annotations.el;

import java.util.*;

import annotations.*;
import annotations.util.coll.*;

/** An annotated class */
public final class AClass<A extends Annotation> extends AElement<A> {
    /** The class's annotated type parameter bounds */
    public final /*@NonNull*/ VivifyingMap</*@NonNull*/ BoundLocation, /*@NonNull*/ ATypeElement<A>> bounds =
            ATypeElement.newVivifyingLHMap_ATE();

    /**
     * The class's annotated methods; a method's key consists of its name
     * followed by its erased signature in JVML format.
     * For example, <code>foo()V</code> or
     * <code>bar(B[I[[Ljava/lang/String;)I</code>.  The annotation scene library
     * does not validate the keys, nor does it check that annotated subelements
     * of the {@link AMethod}s exist in the signature.
     */
    public final /*@NonNull*/ VivifyingMap</*@NonNull*/ String, /*@NonNull*/ AMethod<A>> methods =
            new VivifyingMap</*@NonNull*/ String, /*@NonNull*/ AMethod<A>>(
                    new LinkedHashMap</*@NonNull*/ String, /*@NonNull*/ AMethod<A>>()) {
                @Override
                public /*@NonNull*/ AMethod<A> createValueFor(
                /*@NonNull*/ String k) {
                    return new AMethod<A>();
                }

                @Override
                public boolean subPrune(AMethod<A> v) {
                    return v.prune();
                }
            };

    /** The class's annotated fields; map key is field name */
    public final /*@NonNull*/ VivifyingMap</*@NonNull*/ String, /*@NonNull*/ ATypeElement<A>> fields =
            ATypeElement.newVivifyingLHMap_ATE();

    AClass() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(/*@NonNull*/ /*@ReadOnly*/ AElement<?> o) /*@ReadOnly*/ {
        return o instanceof AClass &&
            ((/*@NonNull*/ /*@ReadOnly*/ AClass<?>) o).equalsClass(this);
    }
    
    boolean equalsClass(/*@NonNull*/ /*@ReadOnly*/ AClass<?> o) /*@ReadOnly*/ {
        return equalsElement(o) && bounds.equals(o.bounds)
            && methods.equals(o.methods) && fields.equals(o.fields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return super.hashCode() + bounds.hashCode()
            + methods.hashCode() + fields.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean prune() {
        return super.prune() & bounds.prune()
            & methods.prune() & fields.prune();
    }
}
