package annotations.el;

import java.util.LinkedHashMap;

import annotations.util.coll.VivifyingMap;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.javari.qual.ReadOnly;
*/

public class AField extends ADeclaration {
  static <K extends /*@ReadOnly*/ Object> VivifyingMap<K, AField>
  newVivifyingLHMap_AF() {
    return new VivifyingMap<K, AField>(
        new LinkedHashMap<K, AField>()) {
      @Override
      public AField createValueFor(K k) {
        return new AField(k);
      }

      @Override
      public boolean subPrune(AField v) {
        return v.prune();
      }
    };
  }

  protected AField(Object description) {
    super(description);
  }

  @Override
  public boolean equals(/*>>> @ReadOnly AField this, */
        /*@ReadOnly*/ Object o) {
    return o instanceof AField
        && ((/*@ReadOnly*/ AField) o).equalsField(this);
  }

  final boolean equalsField(/*>>> @ReadOnly AField this, */
      /*@ReadOnly*/ AField o) {
    return super.equals(o);
  }

  @Override  // TODO: remove?
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString());
    return sb.toString();
  }

  @Override
  public <R, T> R accept(ElementVisitor<R, T> v, T t) {
    return v.visitField(this, t);
  }
}
