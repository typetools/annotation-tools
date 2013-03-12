package annotations.el;

/*>>>
import checkers.javari.quals.ReadOnly;
*/

import java.util.LinkedHashMap;

import type.Type;
import annotations.io.ASTPath;
import annotations.util.coll.VivifyingMap;

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
    /*package-private*/ static <K extends /*@ReadOnly*/ Object> VivifyingMap<K, ATypeElementWithType> newVivifyingLHMap_ATEWT() {
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

    /**
     * Gets the un-annotated type.
     *
     * @return The un-annotated type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the un-annotated type.
     *
     * @param type The un-annotated type.
     */
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof ATypeElementWithType) {
          ATypeElementWithType other = (ATypeElementWithType) o;
        return super.equals(other) && this.type.equals(other.type);
      } else {
        return false;
      }
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
}
