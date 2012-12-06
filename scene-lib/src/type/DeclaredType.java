package type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Java type with optional type parameters and inner type. For example:
 * <pre>
 *   <em>type</em>
 *   <em>type</em>&lt;<em>type parameters</em>&gt;.<em>inner type</em>
 * <pre>
 */
public class DeclaredType extends Type {

    /**
     * The raw, un-annotated name of this type.
     */
    private String name;

    /**
     * The type parameters to this type. Empty if there are none.
     */
    private List<Type> typeParameters;

    /**
     * The inner type of this type. {@code null} if there is none.
     */
    private DeclaredType innerType;

    /**
     * Creates a new declared type with no type parameters or inner type.
     * @param name the raw, un-annotated name of this type.
     */
    public DeclaredType(String name) {
        super();
        this.name = name;
        this.typeParameters = new ArrayList<Type>();
        this.innerType = null;
    }

    /**
     * Creates a new declared type with an empty name and no type parameters or
     * inner type.
     */
    public DeclaredType() {
        this("");
    }

    /**
     * Sets the raw, un-annotated name of this type.
     * @param name the name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the raw, un-annotated name of this type.
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Adds the given type parameter to this type.
     * @param typeParameter the type paramter.
     */
    public void addTypeParameter(Type typeParameter) {
        typeParameters.add(typeParameter);
    }

    /**
     * Gets the type parameter at the given index.
     * @param index the index.
     * @return the type paramter.
     */
    public Type getTypeParameter(int index) {
        return typeParameters.get(index);
    }

    /**
     * Gets an unmodifiable copy of the type parameters of this type. This will
     * be empty if there are none.
     * @return the type parameters.
     */
    public List<Type> getTypeParameters() {
        return Collections.unmodifiableList(typeParameters);
    }

    /**
     * Sets the inner type.
     * @param innerType the inner type.
     */
    public void setInnerType(DeclaredType innerType) {
        this.innerType = innerType;
    }

    /**
     * Gets the inner type. This will be {@code null} if there is none.
     * @return the inner type or {@code null}.
     */
    public DeclaredType getInnerType() {
        return innerType;
    }
}
