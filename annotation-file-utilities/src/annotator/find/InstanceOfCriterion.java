package annotator.find;

import annotator.scanner.InstanceOfScanner;

import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class InstanceOfCriterion implements Criterion {

  private String methodName;
  private Integer offset;

  public InstanceOfCriterion(String methodName, Integer offset) {
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
      debug("return null");
      return false;
    }

    Tree leaf = path.getLeaf();

    debug("");
    debug(this.toString());
    debug("InstanceOfCriterion.isSatisfiedBy: " + leaf);
    debug("leaf: " + leaf);
    debug("kind: " + leaf.getKind());
    debug("class: " + leaf.getClass());

    TreePath parentPath = path.getParentPath();
    if (parentPath == null) {
      debug("return: parent path null");
      return false;
    }

    Tree parent = parentPath.getLeaf();
    if (parent == null) {
      debug("return: parent null");
      return false;
    }

    if (parent.getKind() == Tree.Kind.INSTANCE_OF) {
      InstanceOfTree instanceOfTree = (InstanceOfTree) parent;
      if (leaf != instanceOfTree.getType()) {
        debug("return: not type part of instanceof");
        return false;
      }

      int indexInSource = InstanceOfScanner.indexOfInstanceOfTree(path, parent);
      int indexInClass = InstanceOfScanner.getMethodInstanceOfIndex(methodName, offset);
      boolean b = (indexInSource == indexInClass);
      debug("return source: "+ indexInSource);
      debug("return class: " + indexInClass);
      debug("return new: " + b);
      return b;
    } else {
      boolean b = this.isSatisfiedBy(path.getParentPath());
      debug("return parent: " + b);
      return b;
    }
  }

  public Kind getKind() {
    return Kind.INSTANCE_OF;
  }

  private static void debug(String s) {
    if (Criteria.debug) {
      System.out.println(s);
    }
  }

  public String toString() {
    return "InstanceOfCriterion: in method: " + methodName + " offset: " + offset;
  }

}
