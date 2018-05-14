package annotator.find;

import scenelib.annotations.el.RelativeLocation;
import annotator.scanner.MethodCallScanner;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class CallCriterion implements Criterion {
  private final String methodName;
  private final RelativeLocation loc;

  public CallCriterion(String methodName, RelativeLocation loc) {
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
      return false;
    }

    Tree leaf = path.getLeaf();

    if (leaf.getKind() == Tree.Kind.METHOD_INVOCATION) {
      int indexInSource = MethodCallScanner.indexOfMethodCallTree(path, leaf);
      boolean b;
      if (loc.isBytecodeOffset()) {
        int indexInClass =
            MethodCallScanner.getMethodCallIndex(methodName, loc.offset);
        b = (indexInSource == indexInClass);
      } else {
        b = (indexInSource == loc.index);
      }
      return b;
    } else {
      boolean b = this.isSatisfiedBy(path.getParentPath());
      return b;
    }
  }

  @Override
  public Kind getKind() {
    return Kind.METHOD_CALL;
  }

  @Override
  public String toString() {
    return "CallCriterion: in method: " + methodName + " location: " + loc;
  }
}
