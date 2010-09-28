package annotator.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

/**
 * NewScanner scans the source tree and determines the index of a given new,
 * where the i^th index corresponds to the i^th new, using 0-based indexing.
 */
public class NewScanner extends CommonScanner {
	private static boolean debug = false;

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
		debug("indexOfNewTree: " + path.getLeaf());
		// only start searching from within this method
	    path = findEnclosingMethod(path);
	    if (path == null) {
	      // Was called on something other than a local variable, so return
	      // -1 to ensure that it doesn't match anything.
	      return -1;
	    }

	    NewScanner lvts = new NewScanner(tree);
		lvts.scan(path, null);
		return lvts.index;
	}
	
	public static int indexOfNewTreeInFieldInit(TreePath path, Tree tree) {
		debug("indexOfNewTreeInFieldInit: " + path.getLeaf());
		// only start searching from within this field initializer
	    path = findEnclosingFieldInit(path);
	    if (path == null) {
	      return -1;
	    }

		NewScanner lvts = new NewScanner(tree);
		lvts.scan(path, null);
		return lvts.index;
	}

	public static int indexOfNewTreeInStaticInit(TreePath path, Tree tree) {
		debug("indexOfNewTreeInStaticInit: " + path.getLeaf());
		// only start searching from within this method
	    path = findEnclosingStaticInit(path);
	    if (path == null) {
	      return -1;
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
		return super.visitNewClass(node, p);
	}

	@Override
	public Void visitNewArray(NewArrayTree node, Void p) {
		if (!done) {
			index++;
		}
		if (tree == node) {
			done = true;
		}
		return super.visitNewArray(node, p);
	}

	public static void debug(String s) {
		if (debug) {
			System.out.println(s);
		}
	}

	private static Map<String, List<Integer>> methodNameToNewOffsets = new HashMap<String, List<Integer>>();

	public static void addNewToMethod(String methodName, Integer offset) {
		debug("adding new to method: " + methodName + " offset: " + offset);
		List<Integer> offsetList = methodNameToNewOffsets.get(methodName);
		if (offsetList == null) {
			offsetList = new ArrayList<Integer>();
			methodNameToNewOffsets.put(methodName, offsetList);
		}
		offsetList.add(offset);
	}

	public static Integer getMethodNewIndex(String methodName, Integer offset) {
		List<Integer> offsetList = methodNameToNewOffsets.get(methodName);
		if (offsetList == null) {
			throw new RuntimeException("NewScanner.getMethodNewIndex() : "
					+ "did not find offsets for method: " + methodName);
		}

		Integer offsetIndex = offsetList.indexOf(offset);
		if (offsetIndex < 0) {
			throw new RuntimeException("NewScanner.getMethodNewIndex() : "
					+ "in method: " + methodName + " did not find offset: "
					+ offset);
		}

		return offsetIndex;
	}
}
