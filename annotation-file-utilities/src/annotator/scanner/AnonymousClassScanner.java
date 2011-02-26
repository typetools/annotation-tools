package annotator.scanner;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

/**
 * AnonymousClassScanner determine the index of a tree for an anonymous
 * class.  If the index is i, it is the ith anonymous class in the file.
 * Thus, if i = 2, it will have a name of the form NamedClass$2.
 */
public class AnonymousClassScanner extends TreePathScanner<Void, Void> {

  /**
   * Given an anonymous class, computes and returns its 1-based index in the given tree representing
   * an anonymous class.
   *
   * @param path the source path ending in the anonymous class
   * @param anonclass the anonymous class to search for
   * @return the index of the anonymous class in the source code
   */
  public static int indexOfClassTree(TreePath path, Tree anonclass) {
    // This seems like too much:  only want to move up to wherever
    // numbering starts. -MDE
    // Move all the way to the top of the source tree in order
    // to visit every class in source file.
    while (path.getParentPath() != null) {
      path = path.getParentPath();
    }
    AnonymousClassScanner lvts = new AnonymousClassScanner(anonclass);
    lvts.scan(path, null);
    if (lvts.found) {
      return lvts.index;
    } else {
      return -1;
    }
  }

  private int index;
  // top-level class doesn't count, so first index will be -1
  private boolean found;
  private Tree anonclass;

  /**
   * Creates a new AnonymousClassScanner that searches for the index of the given
   * tree, representing an anonymous class.
   *
   * @param tree the anonymous class to search for
   */
  private AnonymousClassScanner(Tree anonclass) {
    this.index = 1;             // start counting at 1
    this.found = false;
    this.anonclass = anonclass;
  }

  // Slightly tricky counting:  if the target item is a CLASS, only count
  // CLASSes.  If it is a NEW_CLASS, only count NEW_CLASSes

  @Override
  public Void visitClass(ClassTree node, Void p) {
    if (!found && anonclass.getKind() == Tree.Kind.CLASS) {
      if (anonclass == node) {
        found = true;
      } else if (node.getSimpleName().toString().trim().isEmpty()) {
        // don't count classes with given names in source
        index++;
      }
    }
    return super.visitClass(node, p);
  }

  @Override
  public Void visitNewClass(NewClassTree node, Void p) {
    if (!found && anonclass.getKind() == Tree.Kind.NEW_CLASS) {
      if (anonclass == node) {
        found = true;
      } else if (node.getClassBody() != null) {
        // Need to make sure you actually are creating anoymous inner class,
        // not just object creation.
        index++;
      }
    }
    return super.visitNewClass(node, p);
  }
}
