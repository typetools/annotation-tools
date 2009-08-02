package annotator.scanner;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

/** LocalVariableScanner can be used to scan the source
 * tree and determine the index of a tree for an anonymous class,
 * such that if the class'es index is i, it is the i^th class to be declared.
 * Thus, if i = 2, it will have a name of the form NamedClass$2.
 */
public class ClassScanner extends TreePathScanner<Void, Void> {

  /**
   * Computes and returns the index of the given tree representing an
   * anonymous class.
   *
   * @param path the source path ending in the anonymous class
   * @param tree the anonymous class to search for
   * @return the index of the anonymous class in the source code
   */
  public static int indexOfClassTree(TreePath path, Tree tree) {
    // Move all the way to the top of the source tree in order
    // to visit every class in source file.
    while (path.getParentPath() != null) {
      path = path.getParentPath();
    }
    ClassScanner lvts = new ClassScanner(tree);
    lvts.scan(path, null);
    return lvts.index;
  }

  private int index = -2;
  // top-level class doesn't count, so first index will be -1
  private boolean done = false;
  private Tree tree;

  /**
   * Creates a new ClassScanner that searches for the index of the given
   * tree, representing an anonymous class.
   *
   * @param tree the anonymous class to search for
   */
  private ClassScanner(Tree tree) {
    this.index = -2;
    this.done = false;
    this.tree = tree;
  }

  @Override
  public   Void visitClass(
        ClassTree node,
        Void p) {
    if (!done) {
      if (!node.getSimpleName().toString().trim().isEmpty()) {
        // don't count classes with given names in source
        index++;
      }
    }
    if (tree == node) {
      done = true;
    }
    return super.visitClass(node, p);
  }

  public   Void visitNewClass(
        NewClassTree node,
        Void p) {
    if (!done) {
      if (node.getClassBody() != null) {
        // need to make sure you actuall are creating anoymous inner class,
        // not just object creation
        index++;
      }
    }
    if (tree == node) {
      done = true;
    }
    return super.visitNewClass(node, p);
  }
}
