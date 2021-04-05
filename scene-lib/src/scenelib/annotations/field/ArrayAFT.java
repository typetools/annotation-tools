package scenelib.annotations.field;

import java.util.Collection;
import java.util.StringJoiner;
import org.checkerframework.checker.nullness.qual.Nullable;

/** An {@link ArrayAFT} represents an annotation field type that is an array. */
public final class ArrayAFT extends AnnotationFieldType {

  /**
   * The element type of the array, or {@code null} if it is unknown (see {@link
   * scenelib.annotations.AnnotationBuilder#addEmptyArrayField}).
   */
  public final @Nullable ScalarAFT elementType;

  /**
   * Constructs a new {@link ArrayAFT} representing an array type with the given element type.
   * <code>elementType</code> may be <code>null</code> to indicate that the element type is unknown
   * (see {@link scenelib.annotations.AnnotationBuilder#addEmptyArrayField}).
   *
   * @param elementType the element type of the array, or {@code null} if it is unknown
   */
  public ArrayAFT(@Nullable ScalarAFT elementType) {
    this.elementType = elementType;
  }

  @Override
  public boolean isValidValue(Object o) {
    if (!(o instanceof Collection)) {
      return false;
    }
    Collection<?> asCollection = (Collection<?>) o;
    if (elementType == null) {
      return (asCollection.size() == 0);
    }
    for (Object elt : asCollection) {
      if (!elementType.isValidValue(elt)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return (elementType == null ? "unknown" : elementType.toString()) + "[]";
  }

  @Override
  public String format(Object o) {
    Collection<?> asCollection = (Collection<?>) o;
    if (asCollection.size() == 1) {
      Object elt = asCollection.iterator().next();
      return elementType.format(elt);
    }
    StringJoiner result = new StringJoiner(", ", "{", "}");
    for (Object elt : asCollection) {
      result.add(elementType.format(elt));
    }
    return result.toString();
  }

  @Override
  public <R, T> R accept(AFTVisitor<R, T> v, T arg) {
    return v.visitArrayAFT(this, arg);
  }
}
