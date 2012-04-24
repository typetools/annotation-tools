package annotator.find;

import annotations.el.BoundLocation;

import com.sun.source.util.TreePath;
import com.sun.source.tree.Tree;

public class MethodBoundCriterion implements Criterion {

  private final String methodName;
  public final BoundLocation boundLoc;
  private final Criterion sigMethodCriterion;
  private final Criterion boundLocationCriterion;

  public MethodBoundCriterion(String methodName, BoundLocation boundLoc) {
    this.methodName = methodName;
    this.boundLoc = boundLoc;
    this.sigMethodCriterion = Criteria.inMethod(methodName);
    this.boundLocationCriterion = Criteria.atBoundLocation(boundLoc);
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

  @Override
  public Kind getKind() {
    return Kind.METHOD_BOUND;
  }

  @Override
  public String toString() {
    return "MethodBoundCriterion: method: " + methodName + " bound boundLoc: " + boundLoc;
  }
}
