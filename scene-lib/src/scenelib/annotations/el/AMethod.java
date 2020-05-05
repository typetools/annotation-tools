package scenelib.annotations.el;

import java.util.Map;

import scenelib.annotations.util.coll.VivifyingMap;

/**
 * An annotated method; contains bounds, return, parameters, receiver, and throws.
 */
public class AMethod extends ADeclaration {
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

    /** The body of the method. */
    public ABlock body;

    /**
     * The method's simple name
     * followed by its erased signature in JVML format.
     * For example, {@code foo()V} or
     * {@code bar(B[I[[Ljava/lang/String;)I}.
     */
    public final String methodSignature;

    /**
     * Create an AMethod.
     *
     * @param methodSignature the method's name, plus its erased signature
     *   in JVML format within parentheses
     */
    AMethod(String methodSignature) {
      super("method: " + methodSignature);
      this.methodSignature = methodSignature;
      this.body = new ABlock(methodSignature);
      returnType = new ATypeElement("return type of " + methodSignature);
      receiver = new AField("receiver parameter type of " + methodSignature);
    }

    /**
     * Create a copy on an AMethod.
     *
     * @param method the AMethod to copy
     */
    AMethod(AMethod method) {
      super("method: " + method.methodSignature, method);
      methodSignature = method.methodSignature;
      body = method.body.clone();
      returnType = method.returnType.clone();
      receiver = method.receiver.clone();
      copyMapContents(method.bounds, bounds);
      copyMapContents(method.parameters, parameters);
      copyMapContents(method.throwsException, throwsException);
      copyMapContents(method.bounds, bounds);
    }

    /**
     * Returns the method's simple name.
     *
     * @return the method's simple name
     */
    public String getMethodName() {
        return methodSignature.substring(0, methodSignature.indexOf("("));
    }

    @Override
    public AMethod clone() {
      return new AMethod(this);
    }

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
            && methodSignature.equals(o.methodSignature)
            && throwsException.equals(o.throwsException);
    }

    @Override
    public int hashCode() {
        return super.hashCode()
                + bounds.hashCode() + receiver.hashCode()
                + parameters.hashCode() + throwsException.hashCode()
                + body.hashCode() + methodSignature.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && bounds.isEmpty()
            && returnType.isEmpty()
            && receiver.isEmpty() && parameters.isEmpty()
            && throwsException.isEmpty() && body.isEmpty();
    }

    @Override
    public void prune() {
        super.prune();
        bounds.prune();
        returnType.prune();
        receiver.prune();
        parameters.prune();
        throwsException.prune();
        body.prune();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AMethod ");
        sb.append(methodSignature);
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
