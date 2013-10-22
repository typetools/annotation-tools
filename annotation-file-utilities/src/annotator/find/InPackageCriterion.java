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
    assert path == null || path.getLeaf() == leaf;
    return isSatisfiedBy(path);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path) {

    if (path == null)
      return false;

    if (Criteria.debug) {
      debug("InPackageCriterion.isSatisfiedBy(" + Main.pathToString(path) + "); this=" + this);
    }

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
    debug("InPackageCriterion.isSatisfiedBy => false");
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "in package '" + name + "'";
  }

  private static void debug(String s) {
    if (Criteria.debug) {
      System.out.println(s);
    }
  }

}
