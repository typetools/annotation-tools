package annotator.find;

import annotator.scanner.CommonScanner;
import annotator.scanner.StaticInitScanner;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

/**
 * Criterion for being within a specific static initializer.
 */
public class InStaticInitCriterion implements Criterion {

  public final int blockID;
  public final Criterion notInMethodCriterion;

  public InStaticInitCriterion(int blockID) {
    this.blockID = blockID;
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
    while (path != null) {
      if (CommonScanner.isStaticInit(path)) {
        int indexInSource = StaticInitScanner.indexOfStaticInitTree(path);
        return indexInSource == blockID;
      }
      path = path.getParentPath();
    }
    return false;
  }

  @Override
  public Kind getKind() {
    return Kind.IN_METHOD;
  }

  @Override
  public String toString() {
    return "In static initializer with index " + blockID;
  }
}
