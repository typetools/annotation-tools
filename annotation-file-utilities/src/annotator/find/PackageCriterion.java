package annotator.find;

import annotator.Main;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

import javax.tools.JavaFileObject;

/**
 * Represents the criterion that a program element is in a method with a
 * certain name.
 */
final class PackageCriterion implements Criterion {

  private final String name;

  PackageCriterion(String name) {
    this.name = name;
  }

  /** {@inheritDoc} */
  @Override
  public Kind getKind() {
    return Kind.PACKAGE;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path, Tree tree) {
    assert path == null || path.getLeaf() == tree;

    if (Criteria.debug) {
      debug(String.format("PackageCriterion.isSatisfiedBy(%s, %s); this=%s", Main.pathToString(path), tree, this));
    }

    if (path != null)
      return false;

    if (tree.getKind() == Tree.Kind.COMPILATION_UNIT) {
      CompilationUnitTree cu = (CompilationUnitTree)tree;
      if (cu.getSourceFile().getName().endsWith("package-info.java")) {
        ExpressionTree pn = cu.getPackageName();
        assert ((pn instanceof IdentifierTree)
                || (pn instanceof MemberSelectTree));
        if (this.name.equals(pn.toString()))
          return true;
      }
    }
    debug("PackageCriterion.isSatisfiedBy => false");
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path) {
    debug(String.format("PackageCriterion.isSatisfiedBy(%s); this=%s", Main.pathToString(path), this));

    if (path != null)
      return false;

    // If we got here, then the criterion isn't satisfied (I think).
    // return false;

    throw new Error("This should not happen");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "package '" + name + "'";
  }

  private static void debug(String s) {
    if (Criteria.debug) {
      System.out.println(s);
    }
  }

}
