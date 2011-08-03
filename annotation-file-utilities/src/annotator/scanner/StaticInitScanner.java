package annotator.scanner;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

/**
 * StaticInitScanner scans the source tree and determines the index of a given static
 * initializer block, where the i^th index corresponds to the i^th static initializer,
 * using 0-based indexing.
 */
public class StaticInitScanner extends CommonScanner {
	public static int indexOfStaticInitTree(TreePath path) {
		// we allow to start with any path/tree within a static initializer.
		// first go to the enclosing static initializer
	    Tree tree = findEnclosingStaticInit(path).getLeaf();
	    // find the enclosing class
		path = findEnclosingClass(path);
	    if (tree==null || path == null) {
	      return -1;
	    }
	    // find the index of the current static initializer within the enclosing class
	    StaticInitScanner bts = new StaticInitScanner(tree);
		bts.scan(path, null);
		return bts.index;
	}
	
	private int index = -1;
	private boolean done = false;
	private Tree tree;

	private StaticInitScanner(Tree tree) {
		this.index = -1;
		this.done = false;
		this.tree = tree;
	}

	@Override
	public Void visitBlock(BlockTree node, Void p) {
		// TODO: is isStatic only used for static initializer blocks?
		if (!done && node.isStatic()) {
			index++;
		}
		if (tree == node) {
			done = true;
		}
		return super.visitBlock(node, p);
	}
}
