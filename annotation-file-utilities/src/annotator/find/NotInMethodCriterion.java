package annotator.find;

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
    assert path == null || path.getLeaf() == leaf;
    return isSatisfiedBy(path);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path) {
    do {
      Tree tree = path.getLeaf();
      if (tree.getKind() == Tree.Kind.METHOD)
        return false;
      if (Criteria.isClassEquiv(tree)) {
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
