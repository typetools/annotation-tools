package annotator.find;

import annotator.Main;
import annotator.scanner.CommonScanner;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.checkerframework.checker.signature.qual.ClassGetName;

/** Matches a return type. */
public class ReturnTypeCriterion implements Criterion {

  /** The method name. */
  private final String methodName;
  /** Matches the containing class. */
  private final Criterion inClassCriterion;
  /** Matches the method's signature. */
  private final Criterion sigMethodCriterion;

  /**
   * Creates a new ReturnTypeCriterion.
   *
   * @param methodName the method name
   * @param inClassCriterion matches the containing class
   * @param sigMethodCriterion matches the method's signature
   */
  public ReturnTypeCriterion(@ClassGetName String className, String methodName) {
    this.methodName = methodName;
    this.inClassCriterion = Criteria.inClass(className, false);
    this.sigMethodCriterion = methodName.isEmpty() ? null : Criteria.isSigMethod(methodName);
  }

  @Override
  public boolean isSatisfiedBy(TreePath path, Tree leaf) {
    if (path == null) {
      return false;
    }
    assert path.getLeaf() == leaf;
    return isSatisfiedBy(path);
  }

  @Override
  public boolean isSatisfiedBy(TreePath path) {
    if (path == null) {
      return false;
    }

    Criteria.dbug.debug(
        "ReturnTypeCriterion.isSatisfiedBy(%s); this=%n", Main.leafString(path), this.toString());

    do {
      if (path.getLeaf().getKind() == Tree.Kind.METHOD) {
        if (sigMethodCriterion == null || sigMethodCriterion.isSatisfiedBy(path)) {
          // Method and return type verified; now check class.
          path = path.getParentPath();
          while (path != null && path.getLeaf() != null) {
            if (CommonScanner.hasClassKind(path.getLeaf())) {
              if (!inClassCriterion.isSatisfiedBy(path)) {
                break;
              }
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
