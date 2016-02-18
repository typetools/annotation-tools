package annotator.scanner;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

/**
 * The common base-class for all scanners that contains shared tree-traversal
 * methods.
 */
public class CommonScanner extends TreePathScanner<Void, Void> {
    public static boolean hasClassKind(Tree tree) {
        Tree.Kind kind = tree.getKind();
        // Tree.Kind.NEW_CLASS is excluded here because 1) there is no
        // type name to be annotated on an anonymous inner class, and
        // consequently 2) NEW_CLASS insertions are handled separately.
        return kind == Tree.Kind.CLASS
                || kind == Tree.Kind.INTERFACE
                || kind == Tree.Kind.ENUM
                || kind == Tree.Kind.ANNOTATION_TYPE;
    }

    /**
     * The counting context for new, typecast, instanceof, and locals.
     * This is a path to a method or a field/instance/static initializer.
     */
    public static TreePath findCountingContext(TreePath path) {
        while (path != null) {
            if (path.getLeaf().getKind() == Tree.Kind.METHOD ||
                    isFieldInit(path) ||
                    isInitBlock(path)) {
                return path;
            }
            path = path.getParentPath();
        }
        return path;
    }

    // classes

    public static TreePath findEnclosingClass(TreePath path) {
        while (!hasClassKind(path.getLeaf())
                || path.getParentPath().getLeaf().getKind() ==
                    Tree.Kind.NEW_CLASS) {
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
                && hasClassKind(path.getParentPath().getLeaf());
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

    // initializer blocks

    public static boolean isInitBlock(TreePath path, boolean isStatic) {
      return isInitBlock(path)
              && ((BlockTree) path.getLeaf()).isStatic() == isStatic;
    }

    public static boolean isInitBlock(TreePath path) {
        return path.getParentPath() != null
                && hasClassKind(path.getParentPath().getLeaf())
                && path.getLeaf().getKind() == Tree.Kind.BLOCK;
    }

    public static TreePath findEnclosingInitBlock(TreePath path,
            boolean isStatic) {
        while (!isInitBlock(path, isStatic)) {
            path = path.getParentPath();
            if (path == null) {
                return null;
            }
        }
        return path;
    }

    public static boolean isStaticInit(TreePath path) {
        return isInitBlock(path, true);
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

    public static boolean isInstanceInit(TreePath path) {
        return isInitBlock(path, false);
    }

    public static TreePath findEnclosingInstanceInit(TreePath path) {
        while (!isInstanceInit(path)) {
            path = path.getParentPath();
            if (path == null) {
                return null;
            }
        }
        return path;
    }

    // Don't scan into any classes so that occurrences in nested classes
    // aren't counted.
    @Override
    public Void visitClass(ClassTree node, Void p) {
        return p;
    }
}
