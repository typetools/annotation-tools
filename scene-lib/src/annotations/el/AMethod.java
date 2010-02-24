package annotations.el;

import checkers.nullness.quals.Nullable;
import checkers.javari.quals.ReadOnly;

import annotations.*;
import annotations.util.coll.*;

import java.util.HashMap;
import java.util.Map;

/**
 * An annotated method; contains parameters, receiver, locals, typecasts, and
 * news.
 */
public final class AMethod extends AElement {
    /** The method's annotated type parameter bounds */
    public final VivifyingMap<BoundLocation, ATypeElement> bounds =
            ATypeElement.<BoundLocation>newVivifyingLHMap_ATE();

    /** The method's annotated return type */
    public final ATypeElement returnType; // initialized in constructor

    /** The method's annotated receiver */
    public final ATypeElement receiver; // initialized in constructor

    /** The method's annotated parameters; map key is parameter index */
    public final VivifyingMap<Integer, AElement> parameters =
            AElement.<Integer>newVivifyingLHMap_AET();

    // Currently we don't validate the local locations (e.g., that no two
    // distinct ranges for the same index overlap).
    /** The method's annotated local variables; map key contains local variable location numbers */
    public final VivifyingMap<LocalLocation, AElement> locals =
            AElement.<LocalLocation>newVivifyingLHMap_AET();

    /** The method's annotated typecasts; map key is the offset of the checkcast bytecode */
    public final VivifyingMap<Integer, ATypeElement> typecasts =
            ATypeElement.<Integer>newVivifyingLHMap_ATE();

    /** The method's annotated "instanceof" tests; map key is the offset of the instanceof bytecode */
    public final VivifyingMap<Integer, ATypeElement> instanceofs =
            ATypeElement.<Integer>newVivifyingLHMap_ATE();

    /** The method's annotated "new" invocations; map key is the offset of the new bytecode */
    public final VivifyingMap<Integer, ATypeElement> news =
            ATypeElement.<Integer>newVivifyingLHMap_ATE();

    public final VivifyingMap<TypeIndexLocation, ATypeElement> throwsException =
        ATypeElement.<TypeIndexLocation>newVivifyingLHMap_ATE();

    private String methodName;

    AMethod(String methodName) {
      super("method: " + methodName);
      this.methodName = methodName;
      returnType = new ATypeElement("return type of " + methodName);
      receiver = new ATypeElement("receiver of " + methodName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(/*@ReadOnly*/ AElement o) /*@ReadOnly*/ {
        return o instanceof AMethod &&
            ((/*@ReadOnly*/ AMethod) o).equalsMethod(this);
    }

    boolean equalsMethod(/*@ReadOnly*/ AMethod o) /*@ReadOnly*/ {
        parameters.prune();
        o.parameters.prune();

        return tlAnnotationsHere.equals(o.tlAnnotationsHere)
            && returnType.equalsTypeElement(o.returnType)
            && bounds.equals(o.bounds)
            && receiver.equals(o.receiver)
            && parameters.equals(o.parameters)
            && locals.equals(o.locals)
            && typecasts.equals(o.typecasts)
            && instanceofs.equals(o.instanceofs)
            && news.equals(o.news)
            && throwsException.equals(o.throwsException);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return super.hashCode() + bounds.hashCode()
                + receiver.hashCode() + parameters.hashCode()
                + locals.hashCode() + typecasts.hashCode()
                + instanceofs.hashCode() + news.hashCode()
                + throwsException.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean prune() {
        return super.prune() & bounds.prune()
            & returnType.prune()
            & receiver.prune() & parameters.prune()
            & locals.prune() & typecasts.prune()
            & instanceofs.prune() & news.prune()
            & throwsException.prune();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AMethod ");
        sb.append(methodName);
        sb.append(": (");
        sb.append(" -1:");
        sb.append(receiver.toString());
        int size = parameters.size();
        for (Map.Entry<Integer, AElement> em : parameters.entrySet()) {
            Integer i = em.getKey();
            sb.append(" ");
            sb.append(i);
            sb.append(":");
            AElement ae = em.getValue();
            sb.append(ae.toString());
            sb.append(" ");
            ATypeElement ate = ae.type;
            sb.append(ate.toString());
        }
        sb.append(" ");
        sb.append("ret:");
        sb.append(returnType.toString());
        sb.append(")");
        return sb.toString();
    }


}
