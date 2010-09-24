package annotator.scanner;

import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

/**
 * NewScanner scans the source tree and determines the index of a given new,
 * where the i^th index corresponds to the i^th new, using 0-based indexing.
 */
public class NewScanner extends TreePathScanner<Void, Void> {

	/**
	 * Computes the index of the given new tree amongst all new trees inside its
	 * method, using 0-based indexing. The tree has to be either a NewClassTree
	 * or a NewArrayTree.
	 * 
	 * @param path
	 *            the path ending in the given cast tree
	 * @param tree
	 *            the cast tree to search for
	 * @return the index of the given cast tree
	 */
	public static int indexOfNewTree(TreePath path, Tree tree) {
		// only start searching from within this method
		while (path.getLeaf().getKind() != Tree.Kind.METHOD) {
			path = path.getParentPath();
			if (path == null) {
				// Was called on something other than a local variable, so
				// return
				// -1 to ensure that it doesn't match anything.
				return -1;
			}
		}
		NewScanner lvts = new NewScanner(tree);
		lvts.scan(path, null);
		return lvts.index;
	}

	private int index = -1;
	private boolean done = false;
	private Tree tree;

	private NewScanner(Tree tree) {
		this.index = -1;
		this.done = false;
		this.tree = tree;
	}

	@Override
	public Void visitNewClass(NewClassTree node, Void p) {
		if (!done) {
			index++;
		}
		if (tree == node) {
			done = true;
		}
		return p;
	}

	@Override
	public Void visitNewArray(NewArrayTree node, Void p) {
		if (!done) {
			index++;
		}
		if (tree == node) {
			done = true;
		}
		return p;
	}
}
