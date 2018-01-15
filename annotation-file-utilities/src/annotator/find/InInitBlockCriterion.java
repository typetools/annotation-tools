package annotator.find;

import annotator.scanner.CommonScanner;
import annotator.scanner.InitBlockScanner;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

/**
 * Criterion for being within a specific initializer.
 */
public class InInitBlockCriterion implements Criterion {
  public final int blockID;
  public final boolean isStatic;
  public final Criterion notInMethodCriterion;

  public InInitBlockCriterion(int blockID, boolean isStatic) {
    this.blockID = blockID;
    this.isStatic = isStatic;
    this.notInMethodCriterion = Criteria.notInMethod();
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
      if (CommonScanner.isInitBlock(path, isStatic)) {
        int indexInSource = InitBlockScanner.indexOfInitTree(path, isStatic);
        return indexInSource == blockID;
      }
      path = path.getParentPath();
    }
    return false;
  }

  @Override
  public Kind getKind() {
    return isStatic ? Kind.IN_STATIC_INIT : Kind.IN_INSTANCE_INIT;
  }

  @Override
  public String toString() {
    return "In " + (isStatic ? "static" : "instance")
        + " initializer with index " + blockID;
  }
}
