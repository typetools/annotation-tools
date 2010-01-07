package annotator.find;

import annotations.el.BoundLocation;

import com.sun.source.util.TreePath;
import com.sun.source.tree.Tree;

public class MethodBoundCriterion implements Criterion {

  private String methodName;
  private BoundLocation loc;
  private Criterion sigMethodCriterion;
  private Criterion boundLocationCriterion;

  public MethodBoundCriterion(String methodName, BoundLocation loc) {
    this.methodName = methodName;
    this.loc = loc;
    this.sigMethodCriterion = Criteria.inMethod(methodName);
    this.boundLocationCriterion = Criteria.atBoundLocation(loc);
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
    return sigMethodCriterion.isSatisfiedBy(path) &&
      boundLocationCriterion.isSatisfiedBy(path);
  }

  public Kind getKind() {
    return Kind.METHOD_BOUND;
  }

  public String toString() {
    return "MethodBoundCriterion: method: " + methodName + " bound loc: " + loc;
  }
}
