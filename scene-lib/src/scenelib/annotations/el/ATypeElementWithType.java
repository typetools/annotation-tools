package scenelib.annotations.el;

import java.util.LinkedHashMap;

import scenelib.type.Type;
import scenelib.annotations.io.ASTPath;
import scenelib.annotations.util.coll.VivifyingMap;

/**
 * An {@link ATypeElement} that also stores an un-annotated type. This is useful for cast
 * insertion or receiver insertion.
 */
public class ATypeElementWithType extends ATypeElement {

    /**
     * A map with {@link ATypeElementWithType}s as values. When
     * {@link VivifyingMap#vivify(Object)} method is called, a new
     * {@code ATypeElementWithType} is constructed with the parameter to
     * {@code vivify} passed to {@code ATypeElementWithType}'s constructor. This
     * parameter is also used as the key into the map. This is used to map
     * {@link ASTPath}s to their corresponding {@code ATypeElementWithType}.
     * <p>
     * {@code ATEWT} stands for {@code ATypeElementWithType}.
     */
    /*package-private*/ static <K extends Object> VivifyingMap<K, ATypeElementWithType> newVivifyingLHMap_ATEWT() {
        return new VivifyingMap<K, ATypeElementWithType>(
                new LinkedHashMap<K, ATypeElementWithType>()) {
            @Override
            public  ATypeElementWithType createValueFor(K k) {
                return new ATypeElementWithType(k);
            }

            @Override
            public boolean subPrune(ATypeElementWithType v) {
                return v.prune();
            }
        };
    }

    /**
     * The un-annotated type.
     */
    private Type type;

    ATypeElementWithType(Object description) {
        super(description);
    }

    ATypeElementWithType(ATypeElementWithType elem) {
        super(elem);
        type = elem.type;
    }

    @Override
    public ATypeElementWithType clone() {
        return new ATypeElementWithType(this);
    }

    /**
     * Gets the un-annotated type.
     *
     * @return the un-annotated type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the un-annotated type.
     *
     * @param type the un-annotated type
     */
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ATypeElementWithType
            && ((ATypeElementWithType) o).equals(this);
    }

    public boolean equalsTypeElementWithType(ATypeElementWithType o) {
        return super.equals(o) && o.type.equals(type);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + type.hashCode();
    }

    @Override
    public boolean prune() {
        boolean result = super.prune();
        if (result && tlAnnotationsHere.isEmpty()) {
            // If we're about to be pruned because we have no annotations, then
            // stop the prune to just insert a cast with no annotations.
            result = false;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AInsertTypecastTypeElement: ");
        sb.append('\t');
        sb.append(super.toString());
        sb.append("type: " + type);
        return sb.toString();
    }

    @Override
    public <R, T> R accept(ElementVisitor<R, T> v, T t) {
        return v.visitTypeElementWithType(this, t);
    }
}
