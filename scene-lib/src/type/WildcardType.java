package type;

/**
 * A Java wildcard type. For example:
 * <pre>
 *   ?
 *   ? extends Object
 *   ? super String
 * </pre>
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
     */
    private String typeArgumentName;

    /**
     * The bound kind of this wildcard. {@code NONE} if there is no bound.
     */
    private BoundKind kind;

    /**
     * The bound of this wildcard. {@code null} if there is none.
     */
    private Type bound;

    /**
     * Creates a new wildcard type with no bound.
     */
    public WildcardType() {
        this(BoundKind.NONE, null);
    }

    /**
     * Creates a new wildcard type.
     * @param kind the bound kind.
     * @param bound the bound.
     */
    public WildcardType(BoundKind kind, Type bound) {
        super();
        this.typeArgumentName = "?";
        this.kind = kind;
        this.bound = bound;
    }

    /**
     * Creates a new wildcard type.
     * @param typeArgumentName the name of the type argument.
     * @param kind the bound kind.
     * @param bound the bound.
     */
    public WildcardType(String typeArgumentName, BoundKind kind, Type bound) {
        super();
        this.typeArgumentName = typeArgumentName;
        this.kind = kind;
        this.bound = bound;
    }

    /**
     * Gets the name of the type argument. For example, 'K' in:
     * <pre>
     *   public class ClassName&lt;K extends Object&gt;
     * </pre>
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
    public BoundKind getKind() {
        return kind;
    }
}
