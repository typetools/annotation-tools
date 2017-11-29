package annotator.scanner;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.tree.JCTree.JCBlock;

/**
 * InitScanner scans the source tree and determines the index of a given
 * initializer block, where index {@code i} corresponds to the (0-based)
 * i^th initializer of the indicated kind (static or instance),
 */
public class InitBlockScanner extends TreePathScanner<Void, Boolean> {
    public static int indexOfInitTree(TreePath path, boolean isStatic) {
        // we allow to start with any path/tree within an initializer.
        // first go to the enclosing initializer
        Tree tree =
            CommonScanner.findEnclosingInitBlock(path, isStatic).getLeaf();
        // find the enclosing class
        path = CommonScanner.findEnclosingClass(path);
        if (tree==null || path == null) {
            return -1;
        }
        // find the index of the current initializer within the
        // enclosing class
        InitBlockScanner bts = new InitBlockScanner(tree);
        bts.scan(path, isStatic);
        return bts.index;
    }

    private int index = -1;
    private boolean done = false;
    private final Tree tree;

    private InitBlockScanner(Tree tree) {
        this.index = -1;
        this.done = false;
        this.tree = tree;
    }

    @Override
    public Void visitBlock(BlockTree node, Boolean isStatic) {
        // TODO: is isStatic only used for static initializer blocks?
        if (!done && isStatic == node.isStatic()
                && ((JCBlock) node).endpos >= 0) {
            index++;
        }
        if (tree == node) {
            done = true;
        }
        return super.visitBlock(node, isStatic);
    }
}
