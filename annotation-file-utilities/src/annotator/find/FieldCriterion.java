package annotator.find;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class FieldCriterion implements Criterion {

  public final String varName;
  public final Criterion varCriterion;
  public final Criterion notInMethodCriterion;

  public FieldCriterion(String varName) {
    this.varName = varName;
    this.varCriterion = Criteria.is(Tree.Kind.VARIABLE, varName);
    this.notInMethodCriterion = Criteria.notInMethod();
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
    if (path == null) {
      return false;
    }

    if (varCriterion.isSatisfiedBy(path) &&
        notInMethodCriterion.isSatisfiedBy(path)) {
      return true;
    } else {
      return this.isSatisfiedBy(path.getParentPath());
    }
  }

  @Override
  public Kind getKind() {
    return Kind.FIELD;
  }

  @Override
  public String toString() {
    return "FieldCriterion: " + varName;
  }
}
