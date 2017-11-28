package scenelib.annotations.el;

import java.util.Map;
import java.util.TreeMap;

import scenelib.annotations.io.ASTPath;
import scenelib.annotations.util.coll.VivifyingMap;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * A declaration, as opposed to an expression.  Base class for AClass,
 * AMethod, and AField.
 */
public abstract class ADeclaration extends AElement {
  /** The element's insert-annotation invocations; map key is the AST path to the insertion place */
  public final VivifyingMap<ASTPath, ATypeElement> insertAnnotations =
          new VivifyingMap<ASTPath, ATypeElement>(
                  new TreeMap<ASTPath, ATypeElement>()) {
      @Override
      public  ATypeElement createValueFor(ASTPath k) {
          return new ATypeElement(k);
      }

      @Override
      public boolean subPrune(ATypeElement v) {
          return v.prune();
      }
  };

  /** The element's annotated insert-typecast invocations; map key is the AST path to the insertion place */
  public final VivifyingMap<ASTPath, ATypeElementWithType> insertTypecasts =
          new VivifyingMap<ASTPath, ATypeElementWithType>(
                new TreeMap<ASTPath, ATypeElementWithType>()) {
      @Override
      public ATypeElementWithType createValueFor(ASTPath k) {
          return new ATypeElementWithType(k);
      }

      @Override
      public boolean subPrune(ATypeElementWithType v) {
          return v.prune();
      }
  };

  protected ADeclaration(Object description) {
    super(description, true);
  }

  ADeclaration(ADeclaration decl) {
    super(decl);
    copyMapContents(decl.insertAnnotations, insertAnnotations);
    copyMapContents(decl.insertTypecasts, insertTypecasts);
  }

  ADeclaration(Object description, ADeclaration decl) {
    super(decl, description);
    copyMapContents(decl.insertAnnotations, insertAnnotations);
    copyMapContents(decl.insertTypecasts, insertTypecasts);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof ADeclaration
        && ((ADeclaration) o).equalsDeclaration(this);
  }

  private boolean equalsDeclaration(ADeclaration o) {
    return super.equals(o)
            && insertAnnotations.equals(o.insertAnnotations)
            && insertTypecasts.equals(o.insertTypecasts);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
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
    StringBuilder sb = new StringBuilder();
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
