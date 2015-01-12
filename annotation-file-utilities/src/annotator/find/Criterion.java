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
        /*
         * Used for classes, interfaces, enums, annotation types.
         * What would be a better name?
         * Also see Criteria.isClassEquiv
         */
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
        METHOD_CALL,
        LAMBDA_EXPRESSION,
        BOUND_LOCATION,
        EXTIMPLS_LOCATION,
        METHOD_BOUND,
        CLASS_BOUND,
        IN_PACKAGE,
        AST_PATH,
        IN_STATIC_INIT,
        IN_INSTANCE_INIT,
        IN_FIELD_INIT,
        /*
         * This constant is never used. What is the difference to IN_CLASS?
         * Is one for anywhere within a class and this one only for the
         * class declaration itself?
         */
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
