package annotator.find;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

/**
 * Represents the criterion that a program element is enclosed (directly or
 * indirect) by a program element of a certain type.
 */
final class EnclosedByCriterion implements Criterion {

  private final Tree.Kind kind;

  EnclosedByCriterion(Tree.Kind kind) {
    this.kind = kind;
  }

  /** {@inheritDoc} */
  @Override
  public Kind getKind() {
    return Kind.ENCLOSED_BY;
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

    if (path == null)
      return false;

    for (Tree tree : path) {
      if (tree.getKind() == kind)
        return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "enclosed by '" + kind + "'";
  }
}
