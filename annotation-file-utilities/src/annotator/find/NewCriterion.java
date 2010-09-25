package annotator.find;

import annotations.el.RelativeLocation;
import annotator.scanner.NewScanner;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class NewCriterion implements Criterion {

  private String methodName;
  private RelativeLocation loc;

  public NewCriterion(String methodName, RelativeLocation loc) {
    this.methodName = methodName.substring(0, methodName.lastIndexOf(")") + 1);
    this.loc = loc;
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

    if (leaf.getKind() == Tree.Kind.NEW_CLASS) {
      int indexInSource = NewScanner.indexOfNewTree(path, leaf);
      boolean b;
      if (loc.isBytecodeOffset()) {
    	  int indexInClass = NewScanner.getMethodNewIndex(methodName, loc.offset);
    	  b = (indexInSource == indexInClass);
      } else {
    	  b = (indexInSource == loc.index);
      }
      return b;
    } else {
      return this.isSatisfiedBy(path.getParentPath());
    }
  }

  public Kind getKind() {
    return Kind.NEW;
  }

  public String toString() {
    return "NewCriterion: in method: " + methodName + " location: " + loc;
  }

}
