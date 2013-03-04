package annotator.find;

import annotations.el.BoundLocation;

import com.sun.source.util.TreePath;
import com.sun.source.tree.Tree;

public class ClassBoundCriterion implements Criterion {

  private final String className;
  public final BoundLocation boundLoc;
  private final Criterion notInMethodCriterion;
  private final Criterion boundLocCriterion;

  public ClassBoundCriterion(String className, BoundLocation boundLoc) {
    this.className = className;
    this.boundLoc = boundLoc;
    this.notInMethodCriterion = Criteria.notInMethod();
    this.boundLocCriterion = Criteria.atBoundLocation(boundLoc);
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

    return boundLocCriterion.isSatisfiedBy(path) &&
      notInMethodCriterion.isSatisfiedBy(path);
  }

  @Override
  public Kind getKind() {
    return Kind.CLASS_BOUND;
  }

  @Override
  public String toString() {
    return "ClassBoundCriterion: for " + className + " at " + boundLoc;
  }
}
