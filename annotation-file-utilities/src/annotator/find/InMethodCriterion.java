package annotator.find;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

/**
 * Represents the criterion that a program element is in a method with a
 * certain name.
 */
final class InMethodCriterion implements Criterion {

    private final String name;
    private final IsSigMethodCriterion sigMethodCriterion;

    InMethodCriterion(String name) {
        this.name = name;
        sigMethodCriterion = new IsSigMethodCriterion(name);
    }

    /**
     * {@inheritDoc}
     */
    public Kind getKind() {
        return Kind.IN_METHOD;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSatisfiedBy(TreePath path) {

        debug("InMethodCriterion.isSatisfiedBy(" + path.getClass() + ") for " + this);

        if (path == null)
            return false;

        do {
//            Tree tree = path.getLeaf();
//            if (tree.getKind() == Tree.Kind.METHOD) {
//                MethodTree m = (MethodTree)tree;
//                if (this.name.equals(m.getName().toString()))
//                    return true;
//            }
            if (sigMethodCriterion.isSatisfiedBy(path)) {
                debug("InMethodCriterion.isSatisfiedBy => true");
                return true;
            }
            path = path.getParentPath();
        } while (path != null && path.getLeaf() != null);
        debug("InMethodCriterion.isSatisfiedBy => false");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "in method '" + name + "'";
    }

    private static void debug(String s) {
        if (Criteria.debug) {
            System.out.println(s);
        }
    }

}
