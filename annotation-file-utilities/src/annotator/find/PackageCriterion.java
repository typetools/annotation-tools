package annotator.find;

import annotator.Main;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

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
    return isSatisfiedBy(path);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path) {
    Tree tree = path.getLeaf();
    Criteria.dbug.debug("PackageCriterion.isSatisfiedBy(%s, %s); this=%s%n",
        Main.leafString(path), tree, this.toString());

    if (tree.getKind() == Tree.Kind.COMPILATION_UNIT) {
      CompilationUnitTree cu = (CompilationUnitTree)tree;
      if (cu.getSourceFile().getName().endsWith("package-info.java")) {
        ExpressionTree pn = cu.getPackageName();
        assert ((pn instanceof IdentifierTree)
                || (pn instanceof MemberSelectTree));
        if (this.name.equals(pn.toString())) {
          return true;
        }
      }
    }
    Criteria.dbug.debug("PackageCriterion.isSatisfiedBy => false%n");
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "package '" + name + "'";
  }
}
