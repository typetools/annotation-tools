package annotator.find;

import scenelib.annotations.io.ASTPath;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

/**
 * Represents the criterion that a program element is not enclosed by any
 * method (i.e. it's a field, class type parameter, etc.).
 */
final class NotInMethodCriterion implements Criterion {

  /**
   * {@inheritDoc}
   */
  @Override
  public Kind getKind() {
    return Kind.NOT_IN_METHOD;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path, Tree leaf) {
    if (path == null) {
      return false;
    }
    assert path.getLeaf() == leaf;
    return isSatisfiedBy(path);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path) {
    do {
      Tree.Kind kind = path.getLeaf().getKind();
      if (kind == Tree.Kind.METHOD) {
        return false;
      }
      if (ASTPath.isClassEquiv(kind)) {
        return true;
      }
      path = path.getParentPath();
    } while (path != null && path.getLeaf() != null);

    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "not in method";
  }

}
