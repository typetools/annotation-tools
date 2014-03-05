package annotations.el;

import java.util.Map;

import annotations.io.ASTPath;
import annotations.util.coll.VivifyingMap;

/*>>>
import checkers.nullness.quals.Nullable;
import checkers.javari.quals.ReadOnly;
*/

/**
 * A declaration, as opposed to an expression.  Base class for AClass,
 * AMethod, and AField.
 * 
 * @author dbro
 */
public abstract class ADeclaration extends AElement {
  /** The element's insert-annotation invocations; map key is the AST path to the insertion place */
  public final VivifyingMap<ASTPath, ATypeElement> insertAnnotations =
          ATypeElement.<ASTPath>newVivifyingLHMap_ATE();

  /** The element's annotated insert-typecast invocations; map key is the AST path to the insertion place */
  public final VivifyingMap<ASTPath, ATypeElementWithType> insertTypecasts =
          ATypeElementWithType.<ASTPath>newVivifyingLHMap_ATEWT();

  protected ADeclaration(Object description) {
    super(description, true);
  }

  @Override
  public boolean equals(/*>>> @ReadOnly ADeclaration this, */
      /*@ReadOnly*/ Object o) {
    return o instanceof ADeclaration
        && ((/*@ReadOnly*/ ADeclaration) o).equalsDeclaration(this);
  }

  private boolean equalsDeclaration(/*>>> @ReadOnly ADeclaration this, */
      /*@ReadOnly*/ ADeclaration o) {
    return super.equals(o)
            && insertAnnotations.equals(o.insertAnnotations)
            && insertTypecasts.equals(o.insertTypecasts);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode(/*>>> @ReadOnly ADeclaration this*/) {
    return super.hashCode()
        + (insertAnnotations == null ? 0 : insertAnnotations.hashCode())
        + (insertTypecasts == null ? 0 : insertTypecasts.hashCode());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean prune() {
    return super.prune()
        & (insertAnnotations == null || insertAnnotations.prune())
        & (insertTypecasts == null || insertTypecasts.prune());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());
    for (Map.Entry<ASTPath, ATypeElement> em :
            insertAnnotations.entrySet()) {
        sb.append("insert-annotation: ");
        ASTPath loc = em.getKey();
        sb.append(loc);
        sb.append(": ");
        ATypeElement ae = em.getValue();
        sb.append(ae.toString());
        sb.append(' ');
    }
    for (Map.Entry<ASTPath, ATypeElementWithType> em :
        insertTypecasts.entrySet()) {
      sb.append("insert-typecast: ");
      ASTPath loc = em.getKey();
      sb.append(loc);
      sb.append(": ");
      AElement ae = em.getValue();
      sb.append(ae.toString());
      sb.append(' ');
    }
    return sb.toString();
  }

  @Override
  public abstract <R, T> R accept(ElementVisitor<R, T> v, T t);
}
