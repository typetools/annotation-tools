package annotations.io;

import java.util.ArrayList;

import com.sun.source.tree.Tree.Kind;

/**
 * A path through the AST.
 */
public class ASTPath extends ArrayList<ASTPath.ASTEntry> {

    private static final long serialVersionUID = 8943256308307232336L;

    /**
     * A single entry in an AST path.
     */
    public static class ASTEntry {
        private Kind treeKind;
        private String childSelector;
        private int argument;

        /**
         * Constructs a new AST entry. For example, in the entry:
         * <pre>
         * {@code
         * Block.statement 3
         * }</pre>
         * the tree kind is "Block", the child selector is "statement", and the
         * argument is "3".
         *
         * @param treeKind The kind of this AST entry.
         * @param childSelector The child selector to this AST entry.
         * @param argument The argument.
         */
        public ASTEntry(Kind treeKind, String childSelector, int argument) {
            this.treeKind = treeKind;
            this.childSelector = childSelector;
            this.argument = argument;
        }

        /**
         * Constructs a new AST entry, without an argument.
         *
         * See {@link #ASTPath(Kind, String, int)} for an example. of the parameters.
         *
         * @param treeKind The kind of this AST entry.
         * @param childSelector The child selector to this AST entry.
         */
        public ASTEntry(Kind treeKind, String childSelector) {
            this(treeKind, childSelector, -1);
        }

        /**
         * Gets the tree node equivalent kind of this AST entry. For example, in
         * <pre>
         * {@code
         * Block.statement 3
         * }</pre>
         * "Block" is the tree kind.
         * @return the tree kind.
         */
        public Kind getTreeKind() {
            return treeKind;
        }

        /**
         * Gets the child selector of this AST entry. For example, in
         * <pre>
         * {@code
         * Block.statement 3
         * }</pre>
         * "statement" is the child selector.
         * @return the child selector
         */
        public String getChildSelector() {
            return childSelector;
        }

        /**
         * Gets the argument of this AST entry. For example, in
         * <pre>
         * {@code
         * Block.statement 3
         * }</pre>
         * "3" is the argument.
         * @return the argument.
         * @throws IllegalStateException if this AST entry does not have an argument.
         */
        public int getArgument() {
            if (argument < 0) {
                throw new IllegalStateException("Value not set.");
            }
            return argument;
        }

        @Override
        public String toString() {
            String result = treeKind + "." + childSelector;
            if (argument >= 0) {
                result += " " + argument;
            }
            return result;
        }
    }
}
