package annotator.find;

import com.sun.source.util.TreePath;
import com.sun.source.tree.Tree;

/**
 * A criterion for locating a program element in an AST.  A Criterion does
 * not actually give a location.  Given a location, the isSatisfiedBy
 * method indicates whether that location is a desired one.
 */
public interface Criterion {

    /**
     * Types of criterion.
     */
    public static enum Kind {
        IN_METHOD,
        IN_CLASS,
        ENCLOSED_BY,
        HAS_KIND,
        NOT_IN_METHOD,
        TYPE_PARAM,
        GENERIC_ARRAY_LOCATION,
        RECEIVER,
        RETURN_TYPE,
        SIG_METHOD,
        PARAM,
        CAST,
        LOCAL_VARIABLE,
        FIELD,
        NEW,
        INSTANCE_OF,
        BOUND_LOCATION,
        METHOD_BOUND,
        CLASS_BOUND,
        IN_PACKAGE,
        CLASS,
        PACKAGE;
    }

    /**
     * Determines if the given tree path is satisfied by this criterion.
     *
     * @param path the tree path to check against
     * @return true if this criterion is satisfied by the given path,
     * false otherwise
     */
    public boolean isSatisfiedBy(TreePath path, Tree tree);

    /**
     * Determines if the given tree path is satisfied by this criterion.
     *
     * @param path the tree path to check against
     * @return true if this criterion is satisfied by the given path,
     * false otherwise
     */
    public boolean isSatisfiedBy(TreePath path);

    /**
     * Gets the type of this criterion.
     *
     * @return this criterion's kind
     */
    public Kind getKind();

}
