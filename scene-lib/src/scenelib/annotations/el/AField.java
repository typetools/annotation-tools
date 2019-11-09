package scenelib.annotations.el;

import java.util.LinkedHashMap;

import scenelib.annotations.util.coll.VivifyingMap;

public class AField extends ADeclaration {

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

  static <K extends Object> VivifyingMap<K, AField>
  newVivifyingLHMap_AF() {
    return new VivifyingMap<K, AField>(new LinkedHashMap<>()) {
      @Override
      public AField createValueFor(K k) {
        return new AField("" + k);
      }

      @Override
      public boolean isEmptyValue(AField v) {
        return v.isEmpty();
      }
    };
  }

}
