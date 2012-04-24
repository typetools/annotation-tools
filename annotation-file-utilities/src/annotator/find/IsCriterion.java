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
  @Override
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
    switch (tree.getKind()) {
    case VARIABLE:
      String varName = ((VariableTree)tree).getName().toString();
      return varName.equals(name);
    case METHOD:
      String methodName = ((MethodTree)tree).getName().toString();
      return methodName.equals(name);
    case CLASS:
      return InClassCriterion.isSatisfiedBy(path, name, /*exactMatch=*/ true);
    default:
      throw new Error("unknown tree kind " + kind);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "is " + kind.toString().toLowerCase() + " '" + name + "'";
  }

}
