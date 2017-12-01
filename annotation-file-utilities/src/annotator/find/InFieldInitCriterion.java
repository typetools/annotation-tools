package annotator.find;

import annotator.scanner.CommonScanner;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

/**
 * Criterion for being within a specific field initializer.
 */
public class InFieldInitCriterion implements Criterion {

  public final String varName;
  public final Criterion varCriterion;

  public InFieldInitCriterion(String varName) {
    this.varName = varName;
    this.varCriterion = Criteria.is(Tree.Kind.VARIABLE, varName);
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
    while (path != null) {
      if (CommonScanner.isFieldInit(path)) {
        return varCriterion.isSatisfiedBy(path);
      }
      path = path.getParentPath();
    }
    return false;
  }

  @Override
  public Kind getKind() {
    return Kind.IN_FIELD_INIT;
  }

  @Override
  public String toString() {
    return "In field initializer for field '" + varName + "'";
  }
}
