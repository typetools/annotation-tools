package annotator.scanner;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

/**
 * The common base-class for all scanners that contains shared tree-traversal
 * methods.
 */
public class CommonScanner extends TreePathScanner<Void, Void> {

	/**
	 * The counting context for new, typecast, instanceof, and locals.
	 * This is a path to a method or a field/static initializer.
	 */
	public static TreePath findCountingContext(TreePath path) {
		while (path != null) {
			if (path.getLeaf().getKind() == Tree.Kind.METHOD ||
					isFieldInit(path) ||
					isStaticInit(path)) {
				return path;
			}
			path = path.getParentPath();
		}
		return path;
	}
	
	// classes
	
	public static TreePath findEnclosingClass(TreePath path) {
		while (path.getLeaf().getKind() != Tree.Kind.CLASS) {
			path = path.getParentPath();
			if (path == null) {
				return null;
			}
		}
		return path;
	}

	// methods
	
	public static TreePath findEnclosingMethod(TreePath path) {
		while (path.getLeaf().getKind() != Tree.Kind.METHOD) {
			path = path.getParentPath();
			if (path == null) {
				return null;
			}
		}
		return path;
	}

	// Field Initializers

	public static boolean isFieldInit(TreePath path) {
		return path.getLeaf().getKind() == Tree.Kind.VARIABLE
				&& path.getParentPath() != null
				&& path.getParentPath().getLeaf().getKind() == Tree.Kind.CLASS;
	}

	public static TreePath findEnclosingFieldInit(TreePath path) {
		while (!isFieldInit(path)) {
			path = path.getParentPath();
			if (path == null) {
				return null;
			}
		}
		return path;
	}

	// Static Initializers

	public static boolean isStaticInit(TreePath path) {
		return path.getParentPath() != null
				&& path.getParentPath().getLeaf().getKind() == Tree.Kind.CLASS
				&& path.getLeaf().getKind() == Tree.Kind.BLOCK;
	}

	public static TreePath findEnclosingStaticInit(TreePath path) {
		while (!isStaticInit(path)) {
			path = path.getParentPath();
			if (path == null) {
				return null;
			}
		}
		return path;
	}
}
