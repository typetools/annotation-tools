package annotator.find;

import java.util.List;

import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

public class ParamCriterion implements Criterion {

  private final String methodName;
  private final Integer paramPos;

  public ParamCriterion(String methodName, Integer pos) {
    this.methodName = methodName.substring(0, methodName.indexOf(")") + 1);
    this.paramPos = pos;
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

    if (path == null) {
      return false;
    }

    // no inner type location, want to annotate outermost type
    // i.e.   @Nullable List list;
    //        @Nullable List<String> list;
    Tree leaf = path.getLeaf();
    if (leaf instanceof VariableTree) {
      Tree parent = path.getParentPath().getLeaf();
      List<? extends VariableTree> params;
      switch (parent.getKind()) {
      case METHOD:
        params = ((MethodTree) parent).getParameters();
        break;
      case LAMBDA_EXPRESSION:
        params = ((LambdaExpressionTree) parent).getParameters();
        break;
      default:
        params = null;
        break;
      }
      return params != null && params.size() > paramPos
          && params.get(paramPos).equals(leaf);
    }

    return this.isSatisfiedBy(path.getParentPath());
  }

  @Override
  public Kind getKind() {
    return Kind.PARAM;
  }

  @Override
  public String toString() {
    return "ParamCriterion for method: " + methodName + " at position: " +
            paramPos;
  }
}
