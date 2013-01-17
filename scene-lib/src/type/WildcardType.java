package type;

import type.Type.Kind;

/**
 * A Java wildcard type. For example:
 * <pre>
 *   ?
 *   ? extends Object
 *   ? super String
 * </pre>
 * This can also be used with generic type arguments with bounds.
 */
public class WildcardType extends Type {

    /**
     * The possible wildcard bound kinds.
     */
    public enum BoundKind {
        NONE,
        EXTENDS,
        SUPER;

        /**
         * Gets this bound kind in a format that can be inserted into source
         * code (except if this bound kind is {@code NONE}).
         */
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    /**
     * The name of the type argument. For example, 'K' in:
     * <pre>
     *   public class ClassName&lt;K extends Object&gt;
     * </pre>
     * Can also be '?'.
     */
    private String typeArgumentName;

    /**
     * The bound kind of this wildcard. {@code NONE} if there is no bound.
     */
    private BoundKind boundKind;

    /**
     * The bound of this wildcard. {@code null} if there is none.
     */
    private Type bound;

    /**
     * Creates a new wildcard type with no bound.
     */
    public WildcardType() {
        this("?", BoundKind.NONE, null);
    }

    /**
     * Creates a new wildcard type.
     * @param typeArgumentName the name of the type argument.
     * @param boundKind the bound kind.
     * @param bound the bound.
     */
    public WildcardType(String typeArgumentName, BoundKind boundKind, Type bound) {
        super();
        this.typeArgumentName = typeArgumentName;
        this.boundKind = boundKind;
        this.bound = bound;
    }

    /**
     * Gets the name of the type argument. For example, 'K' in:
     * <pre>
     *   public class ClassName&lt;K extends Object&gt;
     * </pre>
     * Can also be '?'.
     * @return the name of the type argument.
     */
    public String getTypeArgumentName() {
        return typeArgumentName;
    }

    /**
     * Gets the bound of this wildcard or {@code null} if there is none.
     * @return the bound or {@code null}.
     */
    public Type getBound() {
        return bound;
    }

    /**
     * Gets the bound kind of this wildcard ({@code NONE} if there is none).
     * @return the bound kind.
     */
    public BoundKind getBoundKind() {
        return boundKind;
    }

    /** {@inheritDoc} */
    @Override
    public Kind getKind() {
        return Kind.WILDCARD;
    }
}
