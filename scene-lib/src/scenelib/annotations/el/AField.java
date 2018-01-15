package scenelib.annotations.el;

import java.util.LinkedHashMap;

import scenelib.annotations.util.coll.VivifyingMap;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

public class AField extends ADeclaration {
  static <K extends Object> VivifyingMap<K, AField>
  newVivifyingLHMap_AF() {
    return new VivifyingMap<K, AField>(
        new LinkedHashMap<K, AField>()) {
      @Override
      public AField createValueFor(K k) {
        return new AField("" + k);
      }

      @Override
      public boolean subPrune(AField v) {
        return v.prune();
      }
    };
  }

  public AExpression init;
  private final String fieldName;

  AField(String fieldName) {
    super(fieldName);
    this.fieldName = fieldName;
  }

  AField(AField field) {
    super(field.fieldName, field);
    fieldName = field.fieldName;
    init = field.init == null ? null : field.init.clone();
  }

  @Override
  public AField clone() {
    return new AField(this);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof AField
        && ((AField) o).equalsField(this);
  }

  final boolean equalsField(AField o) {
    return super.equals(o);
  }

  @Override  // TODO: remove?
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("AField ");
    sb.append(fieldName);
    sb.append(super.toString());
    return sb.toString();
  }

  @Override
  public <R, T> R accept(ElementVisitor<R, T> v, T t) {
    return v.visitField(this, t);
  }
}
