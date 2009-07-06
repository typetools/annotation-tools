package annotator.find;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class NewCriterion implements Criterion {

  private String methodName;
  private Integer offset;

  public NewCriterion(String methodName, Integer offset) {
    this.methodName = methodName.substring(0, methodName.lastIndexOf(")") + 1);
    this.offset = offset;
  }

  public boolean isSatisfiedBy(TreePath path) {
    if (path == null) {
      return false;
    }

    Tree leaf = path.getLeaf();

    if (leaf.getKind() == Tree.Kind.NEW_CLASS) {
      int indexInSource = NewScanner.indexOfNewTree(path, leaf);
      int indexInClass = NewScanner.getMethodNewIndex(methodName, offset);
      return (indexInSource == indexInClass);
    } else {
      return this.isSatisfiedBy(path.getParentPath());
    }
  }

  public Kind getKind() {
    return Kind.NEW;
  }

  public String toString() {
    return "NewCriterion: in method: " + methodName + " offset: " + offset;
  }

}
