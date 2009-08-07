package annotator.find;

import java.util.List;

import annotator.scanner.CastScanner;

import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Pair;

public class CastCriterion implements Criterion {

  private String methodName;
  private Integer offset;

  public CastCriterion(String methodName, Integer offset) {
    this.methodName = methodName.substring(0, methodName.lastIndexOf(")") + 1);
    this.offset = offset;
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

    Tree leaf = path.getLeaf();

    if (leaf.getKind() == Tree.Kind.TYPE_CAST) {
      int indexInSource = CastScanner.indexOfCastTree(path, leaf);
      int indexInClass = CastScanner.getMethodCastIndex(methodName, offset);
      boolean b = (indexInSource == indexInClass);
      return b;
    } else {
      boolean b = this.isSatisfiedBy(path.getParentPath());
      return b;
    }
  }

  public Kind getKind() {
    return Kind.CAST;
  }

  public String toString() {
    return "CastCriterion: in method: " + methodName + " offset: " + offset;
  }
}
