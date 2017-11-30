package annotator.find;

import annotator.Main;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

/**
 * Represents the criterion that a program element is in a package with a
 * certain name.
 */
final class InPackageCriterion implements Criterion {

  private final String name;

  InPackageCriterion(String name) {
    this.name = name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Kind getKind() {
    return Kind.IN_PACKAGE;
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
    if (path == null) {
      return false;
    }

    Criteria.dbug.debug("InPackageCriterion.isSatisfiedBy(%s); this=%s",
        Main.leafString(path), this.toString());

    do {
      Tree tree = path.getLeaf();
      if (tree.getKind() == Tree.Kind.COMPILATION_UNIT) {
        CompilationUnitTree cu = (CompilationUnitTree)tree;
        ExpressionTree pn = cu.getPackageName();
        if (pn == null) {
          return name == null || name.equals("");
        } else {
          String packageName = pn.toString();
          return name != null && (name.equals(packageName));
        }
      }
      path = path.getParentPath();
    } while (path != null && path.getLeaf() != null);

    Criteria.dbug.debug("InPackageCriterion.isSatisfiedBy => false");
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "in package '" + name + "'";
  }
}
