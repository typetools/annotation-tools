package annotator.find;

import java.util.List;

import javax.lang.model.type.TypeKind;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

public class ReceiverCriterion implements Criterion {

  private String methodName; // no return type
  private List<String> params;
  private Criterion parentCriterion;

  public ReceiverCriterion(String methodName) {
    this.methodName = methodName;
    parentCriterion = Criteria.isSigMethod(methodName);
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

    return parentCriterion.isSatisfiedBy(path);
  }

  public Kind getKind() {
    return Kind.RECEIVER;
  }

  public String toString() {
    return "ReceiverCriterion for method: " + methodName;
  }


}
