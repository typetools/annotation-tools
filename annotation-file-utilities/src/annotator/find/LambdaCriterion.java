package annotator.find;

import scenelib.annotations.el.RelativeLocation;
import annotator.scanner.LambdaScanner;

import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class LambdaCriterion implements Criterion {
  private final String methodName;
  private final RelativeLocation loc;

  public LambdaCriterion(String methodName, RelativeLocation loc) {
    this.methodName = methodName;
    this.loc = loc;
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
      Criteria.dbug.debug("return null");
      return false;
    }

    Tree leaf = path.getLeaf();

    Criteria.dbug.debug("%n%s%n", this.toString());
    Criteria.dbug.debug("LambdaCriterion.isSatisfiedBy: %s%n", leaf);
    Criteria.dbug.debug("leaf: %s%n", leaf);
    Criteria.dbug.debug("kind: %s%n", leaf.getKind());
    Criteria.dbug.debug("class: %s%n", leaf.getClass());

    TreePath parentPath = path.getParentPath();
    if (parentPath == null) {
      Criteria.dbug.debug("return: parent path null%n");
      return false;
    }

    Tree parent = parentPath.getLeaf();
    if (parent == null) {
      Criteria.dbug.debug("return: parent null%n");
      return false;
    }

    if (parent.getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
      // LambdaExpressionTree lambdaTree = (LambdaExpressionTree) parent;

      int indexInSource = LambdaScanner.indexOfLambdaExpressionTree(path, parent);
      Criteria.dbug.debug("return source: %d%n", indexInSource);
      boolean b;
      if (loc.isBytecodeOffset()) {
        int indexInClass = LambdaScanner.getMethodLambdaExpressionIndex(methodName, loc.offset);
        Criteria.dbug.debug("return class: %d%n", indexInClass);
        b = (indexInSource == indexInClass);
      } else {
        b = (indexInSource == loc.index);
        Criteria.dbug.debug("return loc.index: %d%n", loc.index);
      }
      Criteria.dbug.debug("return new: %b%n", b);
      return b;
    } else {
      boolean b = this.isSatisfiedBy(path.getParentPath());
      Criteria.dbug.debug("return parent: %b%n", b);
      return b;
    }
  }

  @Override
  public Kind getKind() {
    return Kind.LAMBDA_EXPRESSION;
  }

  @Override
  public String toString() {
    return "LambdaCriterion: at location: " + loc;
  }
}
