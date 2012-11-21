package annotations.el;

import checkers.nullness.quals.Nullable;
import checkers.javari.quals.ReadOnly;

import annotations.util.coll.*;

import java.util.Map;

/**
 * An annotated method; contains bounds, return, parameters, receiver, and throws.
 */
public final class AMethod extends ABlock {
    /** The method's annotated type parameter bounds */
    public final VivifyingMap<BoundLocation, ATypeElement> bounds =
            ATypeElement.<BoundLocation>newVivifyingLHMap_ATE();

    /** The method's annotated return type */
    public final ATypeElement returnType; // initialized in constructor

    /** The method's annotated receiver parameter type */
    public final ATypeElement receiver; // initialized in constructor

    /** The method's annotated parameters; map key is parameter index */
    public final VivifyingMap<Integer, AElement> parameters =
            AElement.<Integer>newVivifyingLHMap_AET();

    public final VivifyingMap<TypeIndexLocation, ATypeElement> throwsException =
        ATypeElement.<TypeIndexLocation>newVivifyingLHMap_ATE();

    private String methodName;

    AMethod(String methodName) {
      super("method: " + methodName);
      this.methodName = methodName;
      returnType = new ATypeElement("return type of " + methodName);
      receiver = new ATypeElement("receiver parameter type of " + methodName);
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
            && equalsBlock(o)
            && throwsException.equals(o.throwsException);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return super.hashCode() + bounds.hashCode()
                + receiver.hashCode() + parameters.hashCode()
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
        // int size = parameters.size();
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
        sb.append(") ");
        sb.append(super.toString());
        return sb.toString();
    }
}
