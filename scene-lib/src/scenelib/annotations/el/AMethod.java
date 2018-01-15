package scenelib.annotations.el;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

import java.util.Map;

import scenelib.annotations.util.coll.VivifyingMap;

/**
 * An annotated method; contains bounds, return, parameters, receiver, and throws.
 */
public final class AMethod extends ADeclaration {
    /** The method's annotated type parameter bounds */
    public final VivifyingMap<BoundLocation, ATypeElement> bounds =
            ATypeElement.<BoundLocation>newVivifyingLHMap_ATE();

    /** The method's annotated return type */
    public final ATypeElement returnType; // initialized in constructor

    /** The method's annotated receiver parameter type */
    public final AField receiver; // initialized in constructor

    /** The method's annotated parameters; map key is parameter index */
    public final VivifyingMap<Integer, AField> parameters =
            AField.<Integer>newVivifyingLHMap_AF();

    public final VivifyingMap<TypeIndexLocation, ATypeElement> throwsException =
        ATypeElement.<TypeIndexLocation>newVivifyingLHMap_ATE();

    public ABlock body;
    public final String methodName;

    AMethod(String methodName) {
      super("method: " + methodName);
      this.methodName = methodName;
      this.body = new ABlock(methodName);
      returnType = new ATypeElement("return type of " + methodName);
      receiver = new AField("receiver parameter type of " + methodName);
    }

    AMethod(AMethod method) {
      super("method: " + method.methodName, method);
      methodName = method.methodName;
      body = method.body.clone();
      returnType = method.returnType.clone();
      receiver = method.receiver.clone();
      copyMapContents(method.bounds, bounds);
      copyMapContents(method.parameters, parameters);
      copyMapContents(method.throwsException, throwsException);
      copyMapContents(method.bounds, bounds);
    }

    @Override
    public AMethod clone() {
      return new AMethod(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(AElement o) {
        return o instanceof AMethod &&
            ((AMethod) o).equalsMethod(this);
    }

    boolean equalsMethod(AMethod o) {
        parameters.prune();
        o.parameters.prune();

        return super.equals(o)
            && returnType.equalsTypeElement(o.returnType)
            && bounds.equals(o.bounds)
            && receiver.equals(o.receiver)
            && parameters.equals(o.parameters)
            && body.equals(o.body)
            && methodName.equals(o.methodName)
            && throwsException.equals(o.throwsException);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode()
                + bounds.hashCode() + receiver.hashCode()
                + parameters.hashCode() + throwsException.hashCode()
                + body.hashCode() + methodName.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean prune() {
        return super.prune() & bounds.prune()
            & returnType.prune()
            & receiver.prune() & parameters.prune()
            & throwsException.prune() & body.prune();
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
        for (Map.Entry<Integer, AField> em : parameters.entrySet()) {
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
        sb.append(body.toString());
        return sb.toString();
    }

    @Override
    public <R, T> R accept(ElementVisitor<R, T> v, T t) {
        return v.visitMethod(this, t);
    }
}
