package type;

import java.util.List;

/**
 * A Java bounded type. For example:
 * <pre>
 *   K extends Object
 *   E super String
 *   ? super String
 * </pre>
 * Calling {@link #addAnnotation(String)}, {@link #getAnnotation(int)}, or
 * {@link #getAnnotations()} on a {@code BoundedType} will result in an
 * {@link UnsupportedOperationException}. Annotations should be added to the
 * {@code type} and {@code bound} of this {@code BoundedType}.
 */
public class BoundedType extends Type {

    /**
     * The possible bound kinds.
     */
    public enum BoundKind {
        EXTENDS,
        SUPER;

        /**
         * Gets this bound kind in a format that can be inserted into source
         * code.
         */
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    /**
     * The base type. For example, 'K' in:
     * <pre>
     *   K extends Object
     * </pre>
     */
    private DeclaredType type;

    /**
     * The bound kind.
     */
    private BoundKind boundKind;

    /**
     * The bound of this type. For example, 'Object' in:
     * <pre>
     *   K extends Object
     * </pre>
     */
    private DeclaredType bound;

    /**
     * Creates a new bounded type.
     * @param type the name of the base type.
     * @param boundKind the bound kind.
     * @param bound the bound.
     */
    public BoundedType(DeclaredType type, BoundKind boundKind, DeclaredType bound) {
        super();
        this.type = type;
        this.boundKind = boundKind;
        this.bound = bound;
    }

    /**
     * Gets the name of the base type. For example, 'K' in:
     * <pre>
     *   K extends Object
     * </pre>
     * @return the name of the base type.
     */
    public DeclaredType getType() {
        return type;
    }

    /**
     * Gets the bound of this type.
     * @return the bound.
     */
    public Type getBound() {
        return bound;
    }

    /**
     * Gets the bound kind of this type.
     * @return the bound kind.
     */
    public BoundKind getBoundKind() {
        return boundKind;
    }

    /** {@inheritDoc} */
    @Override
    public Kind getKind() {
        return Kind.BOUNDED;
    }

    // Override Type methods and throw an exception since annotations can not be
    // put on a bounded type. Annotations should be added to the "type" and
    // "bound" of a bounded type.

    @Override
    public void addAnnotation(String annotation) {
        throw new UnsupportedOperationException(
                "BoundedType cannot have annotations.");
    }

    @Override
    public String getAnnotation(int index) {
        throw new UnsupportedOperationException(
                "BoundedType cannot have annotations.");
    }

    @Override
    public List<String> getAnnotations() {
        throw new UnsupportedOperationException(
                "BoundedType cannot have annotations.");
    }
}
