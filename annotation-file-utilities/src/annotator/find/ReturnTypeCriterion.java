package annotator.find;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class ReturnTypeCriterion implements Criterion {

  private final String methodName;
  private final Criterion inMethodCriterion;

  public ReturnTypeCriterion(String methodName) {
    this.methodName = methodName; // substring(0, name.indexOf(")") + 1);
    this.inMethodCriterion = Criteria.inMethod(methodName);
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

    if (Criteria.debug) {
      System.err.println("ReturnTypeCriterion.isSatisfiedBy deferring to inMethodCriterion");
    }
    boolean result = inMethodCriterion.isSatisfiedBy(path);
    if (Criteria.debug) {
      System.err.println("ReturnTypeCriterion.isSatisfiedBy() => " + result);
    }
    return result;
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
