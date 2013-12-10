package annotator.find;

import annotator.Main;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class ReturnTypeCriterion implements Criterion {

  private final String methodName;
  private final Criterion inClassCriterion;
  private final Criterion sigMethodCriterion;

  public ReturnTypeCriterion(String className, String methodName) {
    this.methodName = methodName; // substring(0, name.indexOf(")") + 1);
    this.inClassCriterion = Criteria.inClass(className, false);
    this.sigMethodCriterion = Criteria.isSigMethod(methodName);
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
    if (path == null) { return false; }

    debug("ReturnTypeCriterion.isSatisfiedBy("
        + Main.pathToString(path) + "); this=" + this);

    do {
      if (path.getLeaf().getKind() == Tree.Kind.METHOD) {
        if (sigMethodCriterion.isSatisfiedBy(path)) {
          path = path.getParentPath();
          while (path != null && path.getLeaf() != null) {
            if (hasClassKind(path.getLeaf())) {
              if (!inClassCriterion.isSatisfiedBy(path)) { break; }
              debug("ReturnTypeCriterion.isSatisfiedBy => true");
              return true;
            }
            path = path.getParentPath();
          }
        }
        break;
      }
      path = path.getParentPath();
    } while (path != null && path.getLeaf() != null);

    debug("ReturnTypeCriterion.isSatisfiedBy => false");
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

  private static boolean hasClassKind(Tree tree) {
    Tree.Kind kind = tree.getKind();
    return kind == Tree.Kind.CLASS
            || kind == Tree.Kind.INTERFACE
            || kind == Tree.Kind.ENUM
            || kind == Tree.Kind.ANNOTATION_TYPE;
  }

  private static void debug(String s) {
    if (Criteria.debug) {
      System.out.println(s);
    }
  }
}
