package annotations.io;

import java.util.ArrayList;
import java.util.List;

import com.sun.source.tree.Tree.Kind;

/**
 * A path through the AST.
 */
public class ASTPath {

    // Constants for the various child selectors.
    public static final String ANNOTATION = "annotation";
    public static final String ARGUMENT = "argument";
    public static final String BLOCK = "block";
    public static final String BODY = "body";
    public static final String BOUND = "bound";
    public static final String CASE = "case";
    public static final String CATCH = "catch";
    public static final String CLASS_BODY = "classBody";
    public static final String CONDITION = "condition";
    public static final String DETAIL = "detail";
    public static final String DIMENSION = "dimension";
    public static final String ELSE_STATEMENT = "elseStatement";
    public static final String ENCLOSING_EXPRESSION = "enclosingExpression";
    public static final String EXPRESSION = "expression";
    public static final String FALSE_EXPRESSION = "falseExpression";
    public static final String FINALLY_BLOCK = "finallyBlock";
    public static final String IDENTIFIER = "identifier";
    public static final String INDEX = "index";
    public static final String INITIALIZER = "initializer";
    public static final String LEFT_OPERAND = "leftOperand";
    public static final String METHOD_SELECT = "methodSelect";
    public static final String PARAMETER = "parameter";
    public static final String QUALIFIER_EXPRESSION = "qualifierExpression";
    public static final String RIGHT_OPERAND = "rightOperand";
    public static final String STATEMENT = "statement";
    public static final String THEN_STATEMENT = "thenStatement";
    public static final String TRUE_EXPRESSION = "trueExpression";
    public static final String TYPE = "type";
    public static final String TYPE_ALTERNATIVE = "typeAlternative";
    public static final String TYPE_ARGUMENT = "typeArgument";
    public static final String UNDERLYING_TYPE = "underlyingType";
    public static final String UPDATE = "update";
    public static final String VARIABLE = "variable";

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
         * See {@link #ASTPath(Kind, String, int)} for an example of the parameters.
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
         * Determines if the given string is equal to this AST path entry's
         * child selector.
         *
         * @param s the string to compare to.
         * @return {@code true} if the string matches the child selector,
         *         {@code false} otherwise.
         */
        public boolean childSelectorIs(String s) {
            return childSelector.equals(s);
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

    /**
     * Stores the AST entries in this AST path.
     */
    private List<ASTEntry> path;

    /**
     * Constructs an empty AST path.
     */
    public ASTPath() {
        path = new ArrayList<ASTEntry>();
    }

    /**
     * Appends the given AST entry to the end of this path.
     */
    public void add(ASTEntry entry) {
        path.add(entry);
    }

    /**
     * @return the AST entry at the given index.
     */
    public ASTEntry get(int index) {
        return path.get(index);
    }

    /**
     * @return the number of AST entries in this AST path.
     */
    public int size() {
        return path.size();
    }

    /**
     * @return {@code true} if this AST path contains zero AST entries,
     *         {@code false} otherwise.
     */
    public boolean isEmpty() {
        return path.isEmpty();
    }
}
