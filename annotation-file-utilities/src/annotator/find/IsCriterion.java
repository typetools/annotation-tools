package annotator.find;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

/**
 * Represents the criterion that a program element has a particular type and
 * name.
 */
final class IsCriterion implements Criterion {

  private final Tree.Kind kind;
  private final String name;

  IsCriterion(Tree.Kind kind, String name) {
    this.kind = kind;
    this.name = name;
  }

  /**
   * {@inheritDoc}
   */
  public Kind getKind() {
    return Kind.HAS_KIND;
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
    Tree tree = path.getLeaf();
    if (tree.getKind() != kind)
      return false;
    if (tree.getKind() == Tree.Kind.VARIABLE) {
      if (((VariableTree)tree).getName().toString().equals(this.name))
        return true;
    } else if (tree.getKind() == Tree.Kind.METHOD) {
      if (((MethodTree)tree).getName().toString().equals(this.name))
        return true;
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    return kind.toString().toLowerCase() + " '" + name + "'";
  }

}
