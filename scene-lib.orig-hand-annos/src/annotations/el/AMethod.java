package annotations.el;

import annotations.*;
import annotations.util.coll.*;

/**
 * An annotated method; contains parameters, receiver, locals, typecasts, and
 * news.
 */
public final class AMethod<A extends Annotation> extends ATypeElement<A> {
    /** The method's annotated type parameter bounds */
    public final /*@NonNull*/ VivifyingMap</*@NonNull*/ BoundLocation, /*@NonNull*/ ATypeElement<A>> bounds =
            ATypeElement.newVivifyingLHMap_ATE();

    /** The method's annotated receiver */
    public final /*@NonNull*/ AElement<A> receiver = new AElement<A>();

    /** The method's annotated parameters; map key is parameter index */
    public final /*@NonNull*/ VivifyingMap</*@NonNull*/ Integer, /*@NonNull*/ ATypeElement<A>> parameters =
            ATypeElement.newVivifyingLHMap_ATE();

    // Currently we don't validate the local locations (e.g., that no two
    // distinct ranges for the same index overlap).
    /** The method's annotated local variables; map key contains local variable location numbers */
    public final /*@NonNull*/ VivifyingMap</*@NonNull*/ LocalLocation, /*@NonNull*/ ATypeElement<A>> locals =
            ATypeElement.newVivifyingLHMap_ATE();

    /** The method's annotated typecasts; map key is the offset of the checkcast bytecode */
    public final /*@NonNull*/ VivifyingMap</*@NonNull*/ Integer, /*@NonNull*/ ATypeElement<A>> typecasts =
            ATypeElement.newVivifyingLHMap_ATE();

    /** The method's annotated "instanceof" tests; map key is the offset of the instanceof bytecode */
    public final /*@NonNull*/ VivifyingMap</*@NonNull*/ Integer, /*@NonNull*/ ATypeElement<A>> instanceofs =
            ATypeElement.newVivifyingLHMap_ATE();

    /** The method's annotated "new" invocations; map key is the offset of the new bytecode */
    public final /*@NonNull*/ VivifyingMap</*@NonNull*/ Integer, /*@NonNull*/ ATypeElement<A>> news =
            ATypeElement.newVivifyingLHMap_ATE();
    
    AMethod() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(/*@NonNull*/ /*@ReadOnly*/ AElement<?> o) /*@ReadOnly*/ {
        return o instanceof AMethod &&
            ((/*@NonNull*/ /*@ReadOnly*/ AMethod<?>) o).equalsMethod(this);
    }
    
    boolean equalsMethod(/*@NonNull*/ /*@ReadOnly*/ AMethod<?> o) /*@ReadOnly*/ {
        return equalsTypeElement(o) && bounds.equals(o.bounds)
            && receiver.equals(o.receiver) && parameters.equals(o.parameters)
            && locals.equals(o.locals) && typecasts.equals(o.typecasts)
            && instanceofs.equals(o.instanceofs) && news.equals(o.news);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return super.hashCode() + bounds.hashCode()
                + receiver.hashCode() + parameters.hashCode()
                + locals.hashCode() + typecasts.hashCode()
                + instanceofs.hashCode() + news.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean prune() {
        return super.prune() & bounds.prune()
            & receiver.prune() & parameters.prune()
            & locals.prune() & typecasts.prune()
            & instanceofs.prune() & news.prune();
    }
}
