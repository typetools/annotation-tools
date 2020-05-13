package scenelib.annotations.el;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import scenelib.annotations.el.AField;
import scenelib.annotations.util.coll.VivifyingMap;

/**
 * An annotated method; contains bounds, return, parameters, receiver, and throws.
 */
public class AMethod extends ADeclaration {
    /** The method's annotated type parameter bounds */
    public final VivifyingMap<BoundLocation, ATypeElement> bounds =
            ATypeElement.<BoundLocation>newVivifyingLHMap_ATE();

    /** The type parameters of this method. */
    private List<? extends TypeParameterElement> typeParameters = null;

    /** The method's annotated return type */
    public final ATypeElement returnType; // initialized in constructor

    /** The return type of the method, or null if the method's return type is unknown or void. */
    private /*@Nullable*/ TypeMirror returnTypeMirror;

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
     * Sets fields from information in the methodElement.
     *
     * @param methodElt the element whose infromation to propagate into this
     */
    public void setFieldsFromMethodElement(ExecutableElement methodElt) {
        setReturnTypeMirror(methodElt.getReturnType());
        setTypeParameters(methodElt.getTypeParameters());
        vivifyAndAddTypeMirrorToParameters(methodElt);
    }

    /**
     * Returns the method's simple name.
     *
     * @return the method's simple name
     */
    public String getMethodName() {
        return methodSignature.substring(0, methodSignature.indexOf("("));
    }

    /**
     * Get the type parameters of this method.
     *
     * @return the list of type parameters
     */
    public List<? extends TypeParameterElement> getTypeParameters() {
        return typeParameters;
    }

    /**
     * Set the type parameters of this method.
     *
     * @param the list of type parameters
     */
    public void setTypeParameters(List<? extends TypeParameterElement> typeParameters) {
        if (typeParameters == null) {
            return;
        }
        if (this.typeParameters != null && !this.typeParameters.equals(typeParameters)) {
            throw new Error(String.format("setTypeParameters(%s): already is %s%n",
                                          typeParameters, this.typeParameters));
        }
        this.typeParameters = typeParameters;
    }

    /**
     * Populates the method parameter map for the method.
     * Ensures that the method parameter map always has an entry for each parameter.
     *
     * @param methodElt the method whose parameters should be vivified
     */
    private void vivifyAndAddTypeMirrorToParameters(ExecutableElement methodElt) {
        for (int i = 0; i < methodElt.getParameters().size(); i++) {
            VariableElement ve = methodElt.getParameters().get(i);
            TypeMirror type = ve.asType();
            Name name = ve.getSimpleName();
            vivifyAndAddTypeMirrorToParameter(i, type, name);
        }
    }

    /**
     * Obtain the parameter at the given index, which can be further operated on to e.g. add a type
     * annotation.
     *
     * @param i the parameter index (first parameter is zero)
     * @param type the type of the parameter
     * @param simpleName the name of the parameter
     * @return an AFieldWrapper representing the parameter
     */
    public AField vivifyAndAddTypeMirrorToParameter(int i, TypeMirror type, Name simpleName) {
        AField param = parameters.getVivify(i);
        param.setName(simpleName.toString());
        if (param.getTypeMirror() == null) {
            param.setTypeMirror(type);
        }
        return param;
    }

    /**
     * Get the return type.
     *
     * @return the return type, or null if the return type is unknown or void
     */
    public /*@Nullable*/ TypeMirror getReturnTypeMirror() {
        return returnTypeMirror;
    }

    /**
     * Set the return type.
     *
     * @param returnTypeMirror the return type
     */
    public void setReturnTypeMirror(/*@Nullable*/ TypeMirror returnTypeMirror) {
        if (returnTypeMirror == null) {
            return;
        }
        if (this.returnTypeMirror != null && this.returnTypeMirror != returnTypeMirror) {
            throw new Error(String.format("setReturnTypeMirror(%s): already is %s%n",
                                          returnTypeMirror, this.returnTypeMirror));
        }
        this.returnTypeMirror = returnTypeMirror;
    }

    /**
     * Get the parameters, as a map from parameter index (0-indexed) to representation.
     *
     * @return an immutable copy of the vivified parameters, as a map from index to representation
     */
    public Map<Integer, AField> getParameters() {
        return ImmutableMap.copyOf(parameters);
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
