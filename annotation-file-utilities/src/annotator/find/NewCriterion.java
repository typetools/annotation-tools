package annotator.find;

import scenelib.annotations.el.RelativeLocation;
import annotator.scanner.NewScanner;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

/**
 * Criterion for being a specific object creation expression.
 */
public class NewCriterion implements Criterion {

  private final String methodName;
  private final Criterion inMethodCriterion;

  private final RelativeLocation loc;

  public NewCriterion(String methodName, RelativeLocation loc) {
    this.methodName = methodName.substring(0, methodName.lastIndexOf(")") + 1);

    if (!(methodName.startsWith("init for field") ||
            methodName.startsWith("static init number")
            || methodName.startsWith("instance init number"))) {
      // keep strings consistent with text used in IndexFileSpecification
      this.inMethodCriterion = Criteria.inMethod(methodName);
    } else {
      this.inMethodCriterion = null;
    }

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

    if (inMethodCriterion!=null && !inMethodCriterion.isSatisfiedBy(path)) {
      // If we're not in the method now, the parent path may still be in the method.
      // For example, the current leaf could be inside a method inside of an
      // anonymous inner class defined in another method.
      return this.isSatisfiedBy(path.getParentPath());
    }
    if (leaf.getKind() == Tree.Kind.NEW_CLASS
            || leaf.getKind() == Tree.Kind.NEW_ARRAY) {
      int indexInSource = NewScanner.indexOfNewTree(path, leaf);
      // System.out.printf("indexInSource=%d%n", indexInSource);
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

  @Override
  public Kind getKind() {
    return Kind.NEW;
  }

  @Override
  public String toString() {
    return "NewCriterion in method: " + methodName + " at location " + loc;
  }

}
