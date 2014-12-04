/**
 * Provides interfaces and classes for finding where in the existing
 * source to insert annotations etc.  {@link TreeFinder} manages the
 * control flow and discovers the positions for insertion, relying on
 * implementations of {@link Criterion} for determining whether an
 * insertion should be made at a given location and on extensions of
 * {@link Insertion} for the concrete text to be inserted.
 * <p>
 * The current flow, given a collection of insertions and an abstract
 * syntax tree (AST) representing a Java source file, consists of a
 * pre-order traversal of the AST to find insertion positions, followed
 * by the insertion of text for each positioned {@link Insertion} into
 * the source code, in reverse order by position.  At each annotatable
 * node encountered during the traversal, the program checks the
 * {@link Criteria} for each yet-unmatched {@link Insertion} against
 * the current node; when there is a match, the program finds and
 * records the appropriate source position.
 *
 * @see TreeFinder#getPositions(com.sun.tools.javac.tree.JCTree.JCCompilationUnit, java.util.List)
 */
package annotator.find;
