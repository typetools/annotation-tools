package annotator.find;

import java.util.ArrayList;
import java.util.List;

import type.ArrayType;
import type.BoundedType;
import type.DeclaredType;
import type.Type;

/**
 * @author dbro
 *
 */
public abstract class TypedInsertion extends Insertion {
  /**
   * The type for insertion.
   */
  protected Type type;

  /**
   * If true only the annotations from {@link type} will be inserted.
   */
  protected boolean annotationsOnly;

  /**
   * The inner types to go on this insertion. See {@link ReceiverInsertion}
   * for more details.
   */
  protected List<Insertion> innerTypeInsertions;

  public TypedInsertion(Type type, Criteria criteria, List<Insertion> innerTypeInsertions) {
      super(criteria, false);
      this.type = type;
      this.innerTypeInsertions = innerTypeInsertions;
      annotationsOnly = false;
  }

  /**
   * If {@code true} only the annotations on {@code type} will be inserted.
   * This is useful when the "new" has already been inserted.
   */
  public void setAnnotationsOnly(boolean annotationsOnly) {
      this.annotationsOnly = annotationsOnly;
  }

  /**
   * Sets the type.
   */
  public void setType(Type type) {
      this.type = type;
  }

  /**
   * Gets the type. It is assumed that the returned value will be modified
   * to update the type to be inserted.
   */
  public Type getType() {
      return type;
  }

  /**
   * Gets a copy of the inner types to go on this receiver. See
   * {@link ReceiverInsertion} for more details.
   * @return a copy of the inner types.
   */
  public List<Insertion> getInnerTypeInsertions() {
      return new ArrayList<Insertion>(innerTypeInsertions);
  }

  public DeclaredType getBaseType() {
    return getBaseType(type);
  }

  public static DeclaredType getBaseType(Type type) {
    switch (type.getKind()) {
    case DECLARED:
      return (DeclaredType) type;
    case BOUNDED:
      return getBaseType(((BoundedType) type).getType());
    case ARRAY:
      return getBaseType(((ArrayType) type).getComponentType());
    default:  // should never be reached
      return null;
    }
  }
}
