package annotator.find;

import annotator.Main;
import annotator.scanner.CommonScanner;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class ReturnTypeCriterion implements Criterion {

  private final String methodName;
  private final Criterion inClassCriterion;
  private final Criterion sigMethodCriterion;

  public ReturnTypeCriterion(String className, String methodName) {
    this.methodName = methodName;
    this.inClassCriterion = Criteria.inClass(className, false);
    this.sigMethodCriterion = methodName.isEmpty() ? null
        : Criteria.isSigMethod(methodName);
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
    if (path == null) { return false; }

    Criteria.dbug.debug("ReturnTypeCriterion.isSatisfiedBy(%s); this=%n",
        Main.leafString(path), this.toString());

    do {
      if (path.getLeaf().getKind() == Tree.Kind.METHOD) {
        if (sigMethodCriterion == null
            || sigMethodCriterion.isSatisfiedBy(path)) {
          // Method and return type verified; now check class.
          path = path.getParentPath();
          while (path != null && path.getLeaf() != null) {
            if (CommonScanner.hasClassKind(path.getLeaf())) {
              if (!inClassCriterion.isSatisfiedBy(path)) { break; }
              Criteria.dbug.debug("ReturnTypeCriterion.isSatisfiedBy => true%n");
              return true;
            }
            path = path.getParentPath();
          }
        }
        break;
      }
      path = path.getParentPath();
    } while (path != null && path.getLeaf() != null);

    Criteria.dbug.debug("ReturnTypeCriterion.isSatisfiedBy => false%n");
    return false;
  }

  @Override
  public Kind getKind() {
    return Kind.RETURN_TYPE;
  }

  @Override
  public String toString() {
    return "ReturnTypeCriterion for method: " + methodName;
  }
}
