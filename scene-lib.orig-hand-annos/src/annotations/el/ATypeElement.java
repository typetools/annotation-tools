package annotations.el;

import java.util.*;

import annotations.*;
import annotations.util.coll.*;

/**
 * An {@link AElement} that represents a type might have annotations on inner
 * types ("generic/array" annotations in the design document). All such elements
 * extend {@link ATypeElement} so that their annotated inner types can be
 * accessed in a uniform fashion. An {@link AElement} holds the annotations on
 * one inner type; {@link #innerTypes} maps locations to inner types.
 */
public class ATypeElement<A extends Annotation> extends AElement<A> {
    static <K, A extends Annotation> /*@NonNull*/ VivifyingMap<K, /*@NonNull*/ ATypeElement<A>> newVivifyingLHMap_ATE() {
        return new VivifyingMap<K, /*@NonNull*/ ATypeElement<A>>(
                new LinkedHashMap<K, /*@NonNull*/ ATypeElement<A>>()) {
            @Override
            public ATypeElement<A> createValueFor(K k) {
                return new ATypeElement<A>();
            }

            @Override
            public boolean subPrune(ATypeElement<A> v) {
                return v.prune();
            }
        };
    }
    
    /** The annotated inner types; map key is the inner type location */
    public final VivifyingMap</*@NonNull*/ InnerTypeLocation, /*@NonNull*/ AElement<A>> innerTypes =
        AElement.newVivifyingLHMap_AE();

    ATypeElement() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(/*@NonNull*/ /*@ReadOnly*/ AElement<?> o) /*@ReadOnly*/ {
        if (!(o instanceof ATypeElement))
            return false;
        /*@NonNull*/ /*@ReadOnly*/ ATypeElement<?> o2
            = (/*@NonNull*/ /*@ReadOnly*/ ATypeElement<?>) o;
        return o2.equalsTypeElement(this);
    }
    
    final boolean equalsTypeElement(/*@NonNull*/ /*@ReadOnly*/ ATypeElement<?> o) /*@ReadOnly*/ {
        return equalsElement(o) && innerTypes.equals(o.innerTypes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return super.hashCode() + innerTypes.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean prune() {
        return super.prune() & innerTypes.prune();
    }
}
