package annotations.el;

import java.util.LinkedHashMap;

import annotations.util.coll.VivifyingMap;

/**
 * An {@link ATypeElement} that also stores the (unqualified) type for a cast
 * insertion.
 */
public class AInsertTypecastTypeElement extends ATypeElement {

    static <K extends /*@ReadOnly*/ Object> VivifyingMap<K, AInsertTypecastTypeElement> newVivifyingLHMap_AITTE() {
        return new VivifyingMap<K, AInsertTypecastTypeElement>(
                new LinkedHashMap<K, AInsertTypecastTypeElement>()) {
            @Override
            public  AInsertTypecastTypeElement createValueFor(K k) /*@ReadOnly*/ {
                return new AInsertTypecastTypeElement(k);
            }

            @Override
            public boolean subPrune(AInsertTypecastTypeElement v) /*@ReadOnly*/ {
                return v.prune();
            }
        };
    }

    /**
     * The un-annotated type to cast to.
     */
    private String type;

    AInsertTypecastTypeElement(Object description) {
        super(description);
    }

    /**
     * Gets the un-annotated type to cast to.
     *
     * @return The un-annotated type to cast to.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the unqualified type to cast to.
     *
     * @param type The unqualified type to cast to.
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof AInsertTypecastTypeElement) {
          AInsertTypecastTypeElement other = (AInsertTypecastTypeElement) o;
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
