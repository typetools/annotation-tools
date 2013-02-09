package annotator.scanner;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

/**
 * LocalClassScanner determines the index of a tree for a local
 * class. If the index is i, it is the ith local class with the class name in
 * the file. Thus, if i = 2, it will have a name of the form
 * OuterClass$2InnerClass.
 */
public class LocalClassScanner extends TreePathScanner<Void, Void> {

  /**
   * Given a local class, computes and returns its 1-based index in the given
   * tree representing a local class.
   *
   * @param path the source path ending in the local class
   * @param localClass the local class to search for
   * @return the index of the local class in the source code
   */
  public static int indexOfClassTree(TreePath path, ClassTree localClass) {
    // This seems like too much:  only want to move up to wherever
    // numbering starts. -MDE
    // Move all the way to the top of the source tree in order
    // to visit every class in source file.
    while (path.getParentPath() != null) {
      path = path.getParentPath();
    }
    LocalClassScanner lcs = new LocalClassScanner(localClass);
    lcs.scan(path, null);
    if (lcs.found) {
      return lcs.index;
    } else {
      return -1;
    }
  }

  private int index;
  private boolean found;
  private ClassTree localClass;

  /**
   * Creates a new LocalClassScanner that searches for the index of the given
   * tree, representing a local class.
   *
   * @param localClass the local class to search for
   */
  private LocalClassScanner(ClassTree localClass) {
    this.index = 1;
    this.found = false;
    this.localClass = localClass;
  }

  @Override
  public Void visitBlock(BlockTree node, Void p) {
    // Visit blocks since a local class can only be in a block. Then visit each
    // statement of the block to see if any are the correct local class.
    for (StatementTree statement : node.getStatements()) {
      if (!found && statement.getKind() == Tree.Kind.CLASS) {
        ClassTree c = (ClassTree) statement;
        if (localClass == statement) {
          found = true;
        } else if (c.getSimpleName().equals(localClass.getSimpleName())) {
          index++;
        }
      }
    }
    return super.visitBlock(node, p);
  }
}
