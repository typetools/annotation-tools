package annotations.el;

import java.util.LinkedHashMap;

import type.Type;
import annotations.util.coll.VivifyingMap;

/**
 * An {@link ATypeElement} that also stores an un-annotated type. This is useful for cast
 * insertion or receiver insertion.
 */
public class ATypeElementWithType extends ATypeElement {

    static <K extends /*@ReadOnly*/ Object> VivifyingMap<K, ATypeElementWithType> newVivifyingLHMap_AITTE() {
        return new VivifyingMap<K, ATypeElementWithType>(
                new LinkedHashMap<K, ATypeElementWithType>()) {
            @Override
            public  ATypeElementWithType createValueFor(K k) /*@ReadOnly*/ {
                return new ATypeElementWithType(k);
            }

            @Override
            public boolean subPrune(ATypeElementWithType v) /*@ReadOnly*/ {
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AInsertTypecastTypeElement: ");
        sb.append('\t');
        sb.append(super.toString());
        sb.append("type: " + type);
        return sb.toString();
    }
}
