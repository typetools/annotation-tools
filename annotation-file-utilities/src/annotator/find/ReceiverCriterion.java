package annotator.find;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class ReceiverCriterion implements Criterion {

  private final String methodName; // no return type
  private final Criterion isSigMethodCriterion;

  public ReceiverCriterion(String methodName) {
    this.methodName = methodName;
    isSigMethodCriterion = Criteria.isSigMethod(methodName);
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
    // want to annotate BlockTree returned by MethodTree.getBody();
    if (path == null) {
      return false;
    }

    // TODO: have changed receiver from annotating block to annotating method,
    // and taking care of moving from method's type to after parameter list
    // in TreeFinder.
//    Tree leaf = path.getLeaf();
//    if (leaf.getKind() == Tree.Kind.BLOCK) {
//      return parentCriterion.isSatisfiedBy(path.getParentPath());
//    }

    return isSigMethodCriterion.isSatisfiedBy(path);
  }

  @Override
  public Kind getKind() {
    return Kind.RECEIVER;
  }

  @Override
  public String toString() {
    return "ReceiverCriterion for method: " + methodName;
  }
}
