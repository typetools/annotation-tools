package annotations.io;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import plume.ArraysMDE;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;

/**
 * A path through the AST.
 */
public final class ASTPath implements Iterable<ASTPath.ASTEntry> {

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
    public static final String RESOURCE = "resource";
    public static final String RIGHT_OPERAND = "rightOperand";
    public static final String STATEMENT = "statement";
    public static final String THEN_STATEMENT = "thenStatement";
    public static final String TRUE_EXPRESSION = "trueExpression";
    public static final String TYPE = "type";
    public static final String TYPE_ALTERNATIVE = "typeAlternative";
    public static final String TYPE_ARGUMENT = "typeArgument";
    public static final String TYPE_PARAMETER = "typeParameter";
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
         * See {@link #ASTEntry(Kind, String, int)} for an example of the parameters.
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

        /**
         * Checks that this Entry has an argument.
         *
         * @return if this entry has an argument
         */
        public boolean hasArgument() {
            return argument >= 0;
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

    @Override
    public Iterator<ASTEntry> iterator() {
      return path.iterator();
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

    @Override
    public boolean equals(Object o) {
        return o instanceof ASTPath && this.equals((ASTPath) o);
    }

    public boolean equals(ASTPath astPath) {
        if (size() != astPath.size()) { return false; }
        int i = 0;
        for (ASTEntry entry : path) {
            if (!entry.equals(astPath.get(i++))) { return false; }
        }
        return true;
    }

    @Override
    public String toString() {
        return path.toString();
    }

    /**
     * Create a new {@code ASTPath} from a formatted string description.
     * 
     * @param s formatted string as in JAIF {@code insert-\{cast,annotation\}}
     * @return the corresponding {@code ASTPath}
     * @throws ParseException
     */
    public static ASTPath parse(final String s) throws ParseException {
        return new Parser(s).parseASTPath();
    }

    /**
     * Determine whether this {@code ASTPath} matches a given {@code TreePath}.
     */
    public boolean matches(TreePath treePath) {
        CompilationUnitTree cut = treePath.getCompilationUnit();
        Map<Tree, ASTIndex.ASTRecord> index = ASTIndex.indexOf(cut);
        Tree leaf = treePath.getLeaf();
        ASTPath astPath = index.get(leaf).astPath;
        return this.equals(astPath);
    }

    static class Parser {
      // adapted from annotations.io.IndexFileParser
      // TODO: refactor IndexFileParser to use this class

      StreamTokenizer st;

      Parser(String s) {
        st = new StreamTokenizer(new StringReader(s));
      }

      private void getTok() {
        try {
          st.nextToken();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      private boolean gotType(int t) {
        return st.ttype == t;
      }

      private int natVal() throws ParseException {
        if (gotType(StreamTokenizer.TT_NUMBER)) {
          int n = (int) st.nval;
          if (n >= 0 && n == st.nval) {
            return n;
          }
        }
        throw new ParseException("expected non-negative integer, got " + st);
      }

      private String strVal() throws ParseException {
        if (gotType(StreamTokenizer.TT_WORD)) {
          return st.sval;
        }
        throw new ParseException("expected string, got " + st);
      }

      /**
       * Parses an AST path.
       * @return the AST path.
       */
      ASTPath parseASTPath() throws ParseException {
        ASTPath astPath = new ASTPath();
        astPath.add(parseASTEntry());
        while (gotType(',')) {
          getTok();
          astPath.add(parseASTEntry());
        }
        return astPath;
      }

      /**
       * Parses and returns the next AST entry.
       * @return A new AST entry.
       * @throws ParseException if the next entry type is invalid.
       */
      ASTEntry parseASTEntry() throws ParseException {
        String s = strVal();

        if (s.equals("AnnotatedType")) {
          return newASTEntry(Kind.ANNOTATED_TYPE,
              new String[] {ASTPath.ANNOTATION, ASTPath.UNDERLYING_TYPE},
              new String[] {ASTPath.ANNOTATION});
        } else if (s.equals("ArrayAccess")) {
          return newASTEntry(Kind.ARRAY_ACCESS,
              new String[] {ASTPath.EXPRESSION, ASTPath.INDEX});
        } else if (s.equals("ArrayType")) {
          return newASTEntry(Kind.ARRAY_TYPE,
              new String[] {ASTPath.TYPE});
        } else if (s.equals("Assert")) {
          return newASTEntry(Kind.ASSERT,
              new String[] {ASTPath.CONDITION, ASTPath.DETAIL});
        } else if (s.equals("Assignment")) {
          return newASTEntry(Kind.ASSIGNMENT,
              new String[] {ASTPath.VARIABLE, ASTPath.EXPRESSION});
        } else if (s.equals("Binary")) {
          // Always use Kind.PLUS for Binary
          return newASTEntry(Kind.PLUS,
              new String[] {ASTPath.LEFT_OPERAND, ASTPath.RIGHT_OPERAND});
        } else if (s.equals("Block")) {
          return newASTEntry(Kind.BLOCK,
              new String[] {ASTPath.STATEMENT},
              new String[] {ASTPath.STATEMENT});
        } else if (s.equals("Case")) {
          return newASTEntry(Kind.CASE,
              new String[] {ASTPath.EXPRESSION, ASTPath.STATEMENT},
              new String[] {ASTPath.STATEMENT});
        } else if (s.equals("Catch")) {
          return newASTEntry(Kind.CATCH,
              new String[] {ASTPath.PARAMETER, ASTPath.BLOCK});
        } else if (s.equals("CompoundAssignment")) {
          // Always use Kind.PLUS_ASSIGNMENT for CompoundAssignment
          return newASTEntry(Kind.PLUS_ASSIGNMENT,
              new String[] {ASTPath.VARIABLE, ASTPath.EXPRESSION});
        } else if (s.equals("ConditionalExpression")) {
          return newASTEntry(Kind.CONDITIONAL_EXPRESSION,
              new String[] {ASTPath.CONDITION,
              ASTPath.TRUE_EXPRESSION,
              ASTPath.FALSE_EXPRESSION});
        } else if (s.equals("DoWhileLoop")) {
          return newASTEntry(Kind.DO_WHILE_LOOP,
              new String[] {ASTPath.CONDITION, ASTPath.STATEMENT});
        } else if (s.equals("EnhancedForLoop")) {
          return newASTEntry(Kind.ENHANCED_FOR_LOOP,
              new String[] {ASTPath.VARIABLE,
              ASTPath.EXPRESSION,
              ASTPath.STATEMENT});
        } else if (s.equals("ExpressionStatement")) {
          return newASTEntry(Kind.EXPRESSION_STATEMENT,
              new String[] {ASTPath.EXPRESSION});
        } else if (s.equals("ForLoop")) {
          return newASTEntry(Kind.FOR_LOOP,
              new String[] {ASTPath.INITIALIZER, ASTPath.CONDITION,
              ASTPath.UPDATE, ASTPath.STATEMENT},
              new String[] {ASTPath.INITIALIZER, ASTPath.UPDATE});
        } else if (s.equals("If")) {
          return newASTEntry(Kind.IF,
              new String[] {ASTPath.CONDITION,
              ASTPath.THEN_STATEMENT, ASTPath.ELSE_STATEMENT});
        } else if (s.equals("InstanceOf")) {
          return newASTEntry(Kind.INSTANCE_OF,
              new String[] {ASTPath.EXPRESSION, ASTPath.TYPE});
        } else if (s.equals("LabeledStatement")) {
          return newASTEntry(Kind.LABELED_STATEMENT,
              new String[] {ASTPath.STATEMENT});
        } else if (s.equals("LambdaExpression")) {
          return newASTEntry(Kind.LAMBDA_EXPRESSION,
              new String[] {ASTPath.PARAMETER, ASTPath.BODY},
              new String[] {ASTPath.PARAMETER});
        } else if (s.equals("MemberReference")) {
          return newASTEntry(Kind.MEMBER_REFERENCE,
              new String[] {ASTPath.QUALIFIER_EXPRESSION, ASTPath.TYPE_ARGUMENT},
              new String[] {ASTPath.TYPE_ARGUMENT});
        } else if (s.equals("MemberSelect")) {
          return newASTEntry(Kind.MEMBER_SELECT, new String[] {ASTPath.EXPRESSION});
        } else if (s.equals("MethodInvocation")) {
          return newASTEntry(Kind.METHOD_INVOCATION,
              new String[] {ASTPath.TYPE_ARGUMENT,
              ASTPath.METHOD_SELECT, ASTPath.ARGUMENT},
              new String[] {ASTPath.TYPE_ARGUMENT, ASTPath.ARGUMENT});
        } else if (s.equals("NewArray")) {
          return newASTEntry(Kind.NEW_ARRAY,
              new String[] {ASTPath.TYPE, ASTPath.DIMENSION, ASTPath.INITIALIZER},
              new String[] {ASTPath.DIMENSION, ASTPath.INITIALIZER});
        } else if (s.equals("NewClass")) {
          return newASTEntry(Kind.NEW_CLASS,
              new String[] {ASTPath.ENCLOSING_EXPRESSION,
              ASTPath.TYPE_ARGUMENT, ASTPath.IDENTIFIER,
              ASTPath.ARGUMENT, ASTPath.CLASS_BODY},
              new String[] {ASTPath.TYPE_ARGUMENT, ASTPath.ARGUMENT});
        } else if (s.equals("ParameterizedType")) {
          return newASTEntry(Kind.PARAMETERIZED_TYPE,
              new String[] {ASTPath.TYPE, ASTPath.TYPE_ARGUMENT},
              new String[] {ASTPath.TYPE_ARGUMENT});
        } else if (s.equals("Parenthesized")) {
          return newASTEntry(Kind.PARENTHESIZED,
              new String[] {ASTPath.EXPRESSION});
        } else if (s.equals("Return")) {
          return newASTEntry(Kind.RETURN,
              new String[] {ASTPath.EXPRESSION});
        } else if (s.equals("Switch")) {
          return newASTEntry(Kind.SWITCH,
              new String[] {ASTPath.EXPRESSION, ASTPath.CASE},
              new String[] {ASTPath.CASE});
        } else if (s.equals("Synchronized")) {
          return newASTEntry(Kind.SYNCHRONIZED,
              new String[] {ASTPath.EXPRESSION, ASTPath.BLOCK});
        } else if (s.equals("Throw")) {
          return newASTEntry(Kind.THROW,
              new String[] {ASTPath.EXPRESSION});
        } else if (s.equals("Try")) {
          return newASTEntry(Kind.TRY,
              new String[] {ASTPath.BLOCK, ASTPath.CATCH, ASTPath.FINALLY_BLOCK},
              new String[] {ASTPath.CATCH});
        } else if (s.equals("TypeCast")) {
          return newASTEntry(Kind.TYPE_CAST,
              new String[] {ASTPath.TYPE, ASTPath.EXPRESSION});
        } else if (s.equals("Unary")) {
          // Always use Kind.UNARY_PLUS for Unary
          return newASTEntry(Kind.UNARY_PLUS,
              new String[] {ASTPath.EXPRESSION});
        } else if (s.equals("UnionType")) {
          return newASTEntry(Kind.UNION_TYPE,
              new String[] {ASTPath.TYPE_ALTERNATIVE},
              new String[] {ASTPath.TYPE_ALTERNATIVE});
        } else if (s.equals("Variable")) {
          return newASTEntry(Kind.VARIABLE,
              new String[] {ASTPath.TYPE, ASTPath.INITIALIZER});
        } else if (s.equals("WhileLoop")) {
          return newASTEntry(Kind.WHILE_LOOP,
              new String[] {ASTPath.CONDITION, ASTPath.STATEMENT});
        } else if (s.equals("Wildcard")) {
          // Always use Kind.UNBOUNDED_WILDCARD for Wildcard
          return newASTEntry(Kind.UNBOUNDED_WILDCARD,
              new String[] {ASTPath.BOUND});
        }

        throw new ParseException("Invalid AST path type: " + s);
      }

      /**
       * Parses and constructs a new AST entry, where none of the child selections require
       * arguments. For example, the call:
       *
       * <pre>
       * {@code newASTEntry(Kind.WHILE_LOOP, new String[] {"condition", "statement"});</pre>
       *
       * constructs a while loop AST entry, where the valid child selectors are "condition" or
       * "statement".
       *
       * @param kind The kind of this AST entry.
       * @param legalChildSelectors A list of the legal child selectors for this AST entry.
       * @return A new {@link ASTEntry}.
       * @throws ParseException if an illegal argument is found.
       */
      private ASTEntry newASTEntry(Kind kind, String[] legalChildSelectors)
          throws ParseException {
        return newASTEntry(kind, legalChildSelectors, null);
      }

      /**
       * Parses and constructs a new AST entry. For example, the call:
       *
       * <pre>
       * {@code newASTEntry(Kind.CASE, new String[] {"expression", "statement"}, new String[] {"statement"});
       * </pre>
       *
       * constructs a case AST entry, where the valid child selectors are
       * "expression" or "statement" and the "statement" child selector requires
       * an argument.
       *
       * @param kind The kind of this AST entry.
       * @param legalChildSelectors A list of the legal child selectors for this AST entry.
       * @param argumentChildSelectors A list of the child selectors that also require an argument.
       *                               Entries here should also be in the legalChildSelectors list.
       * @return A new {@link ASTEntry}.
       * @throws ParseException if an illegal argument is found.
       */
      private ASTEntry newASTEntry(Kind kind, String[] legalChildSelectors,
          String[] argumentChildSelectors) throws ParseException {
        if (gotType('.')) {
          getTok();
        } else {
          throw new ParseException("expected '.', got " + st);
        }

        String s = strVal();
        for (String arg : legalChildSelectors) {
          if (s.equals(arg)) {
            if (argumentChildSelectors != null
                && ArraysMDE.indexOf(argumentChildSelectors, arg) >= 0) {
              getTok();
              return new ASTEntry(kind, arg, natVal());
            } else {
              return new ASTEntry(kind, arg);
            }
          }
        }

        throw new ParseException("Invalid argument for " + kind
            + " (legal arguments - " + Arrays.toString(legalChildSelectors)
            + "): " + s);
      }
    }

    static class Matcher {
      // adapted from IndexFileParser.parseASTPath et al.
      // TODO: refactor switch statement into TreeVisitor?
      public static boolean debug = false;
      private ASTPath astPath;

      Matcher(ASTPath astPath) {
        this.astPath = astPath;
      }

      private boolean nonDecl(TreePath path) {
        switch (path.getLeaf().getKind()) {
        case CLASS:
        case METHOD:
          return false;
        case VARIABLE:
          TreePath parentPath = path.getParentPath();
          return parentPath != null
              && parentPath.getLeaf().getKind() != Kind.CLASS;
        default:
          return true;
        }
      }

      private static boolean isDecl(TreePath path) {
        switch (path.getLeaf().getKind()) {
        case CLASS:
        case METHOD:
          return true;
        case VARIABLE:
          TreePath parentPath = path.getParentPath();
          return parentPath == null
              || parentPath.getLeaf().getKind() == Kind.CLASS;
        default:
          return false;
        }
      }

      public boolean matches(TreePath path) {
        if (path == null) {
          return false;
        }

        // actualPath stores the path through the source code AST to this
        // location (specified by the "path" parameter to this method). It is
        // computed by traversing from this location up the source code AST
        // until it reaches a method node (this gets only the part of the path
        // within a method) or class node (this gets only the part of the path
        // within a field).
        List<Tree> actualPath = new ArrayList<Tree>();
        while (path != null && !isDecl(path)) {
          actualPath.add(0, path.getLeaf());
          path = path.getParentPath();
        }

        if (debug) {
          System.out.println("AST [" + astPath + "]");
          for (Tree t : actualPath) {
            System.out.println("  " + t.getKind() + ": "
                + t.toString().replace('\n', ' '));
          }
        }

        if (astPath.isEmpty() || actualPath.isEmpty()
            || actualPath.size() != astPath.size() + 1) {
          return false;
        }

        for (int i = 0; i < astPath.size() && i < actualPath.size(); i++) {
          ASTPath.ASTEntry astNode = astPath.get(i);
          Tree actualNode = actualPath.get(i);

          // Based on the child selector and (optional) argument in "astNode",
          // "next" will get set to the next source node below "actualNode".
          // Then "next" will be compared with the node following "astNode"
          // in "actualPath". If it's not a match, this is not the correct
          // location. If it is a match, keep going.
          Tree next = null;
          if (debug) {
            System.out.println("astNode: " + astNode);
            System.out.println("actualNode: " + actualNode.getKind());
          }
          if (!kindsMatch(astNode.getTreeKind(), actualNode.getKind())) {
            return false;
          }

          switch (actualNode.getKind()) {
          case ANNOTATED_TYPE: {
            AnnotatedTypeTree annotatedType = (AnnotatedTypeTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.ANNOTATION)) {
              int arg = astNode.getArgument();
              List<? extends AnnotationTree> annos = annotatedType.getAnnotations();
              if (arg >= annos.size()) {
                return false;
              }
              next = annos.get(arg);
            } else {
              next = annotatedType.getUnderlyingType();
            }
            break;
          }
          case ARRAY_ACCESS: {
            ArrayAccessTree arrayAccess = (ArrayAccessTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
              next = arrayAccess.getExpression();
            } else {
              next = arrayAccess.getIndex();
            }
            break;
          }
          case ARRAY_TYPE: {
            ArrayTypeTree arrayType = (ArrayTypeTree) actualNode;
            next = arrayType.getType();
            break;
          }
          case ASSERT: {
            AssertTree azzert = (AssertTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.CONDITION)) {
              next = azzert.getCondition();
            } else {
              next = azzert.getDetail();
            }
            break;
          }
          case ASSIGNMENT: {
            AssignmentTree assignment = (AssignmentTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.VARIABLE)) {
              next = assignment.getVariable();
            } else {
              next = assignment.getExpression();
            }
            break;
          }
          case BLOCK: {
            BlockTree block = (BlockTree) actualNode;
            int arg = astNode.getArgument();
            List<? extends StatementTree> statements = block.getStatements();
            if (arg >= block.getStatements().size()) {
              return false;
            }
            next = statements.get(arg);
            break;
          }
          case CASE: {
            CaseTree caze = (CaseTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
              next = caze.getExpression();
            } else {
              int arg = astNode.getArgument();
              List<? extends StatementTree> statements = caze.getStatements();
              if (arg >= statements.size()) {
                return false;
              }
              next = statements.get(arg);
            }
            break;
          }
          case CATCH: {
            CatchTree cach = (CatchTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.PARAMETER)) {
              next = cach.getParameter();
            } else {
              next = cach.getBlock();
            }
            break;
          }
          case CONDITIONAL_EXPRESSION: {
            ConditionalExpressionTree conditionalExpression =
                (ConditionalExpressionTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.CONDITION)) {
              next = conditionalExpression.getCondition();
            } else if (astNode.childSelectorIs(ASTPath.TRUE_EXPRESSION)) {
              next = conditionalExpression.getTrueExpression();
            } else {
              next = conditionalExpression.getFalseExpression();
            }
            break;
          }
          case DO_WHILE_LOOP: {
            DoWhileLoopTree doWhileLoop =(DoWhileLoopTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.CONDITION)) {
              next = doWhileLoop.getCondition();
            } else {
              next = doWhileLoop.getStatement();
            }
            break;
          }
          case ENHANCED_FOR_LOOP: {
            EnhancedForLoopTree enhancedForLoop = (EnhancedForLoopTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.VARIABLE)) {
              next = enhancedForLoop.getVariable();
            } else if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
              next = enhancedForLoop.getExpression();
            } else {
              next = enhancedForLoop.getStatement();
            }
            break;
          }
          case EXPRESSION_STATEMENT: {
            ExpressionStatementTree expressionStatement =
                (ExpressionStatementTree) actualNode;
            next = expressionStatement.getExpression();
            break;
          }
          case FOR_LOOP: {
            ForLoopTree forLoop = (ForLoopTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.INITIALIZER)) {
              int arg = astNode.getArgument();
              List<? extends StatementTree> inits = forLoop.getInitializer();
              if (arg >= inits.size()) {
                return false;
              }
              next = inits.get(arg);
            } else if (astNode.childSelectorIs(ASTPath.CONDITION)) {
              next = forLoop.getCondition();
            } else if (astNode.childSelectorIs(ASTPath.UPDATE)) {
              int arg = astNode.getArgument();
              List<? extends ExpressionStatementTree> updates = forLoop.getUpdate();
              if (arg >= updates.size()) {
                return false;
              }
              next = updates.get(arg);
            } else {
              next = forLoop.getStatement();
            }
            break;
          }
          case IF: {
            IfTree iff = (IfTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.CONDITION)) {
              next = iff.getCondition();
            } else if (astNode.childSelectorIs(ASTPath.THEN_STATEMENT)) {
              next = iff.getThenStatement();
            } else {
              next = iff.getElseStatement();
            }
            break;
          }
          case INSTANCE_OF: {
            InstanceOfTree instanceOf = (InstanceOfTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
              next = instanceOf.getExpression();
            } else {
              next = instanceOf.getType();
            }
            break;
          }
          case LABELED_STATEMENT: {
            LabeledStatementTree labeledStatement =
                (LabeledStatementTree) actualNode;
            next = labeledStatement.getStatement();
            break;
          }
          case LAMBDA_EXPRESSION: {
            LambdaExpressionTree lambdaExpression =
                (LambdaExpressionTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.PARAMETER)) {
              int arg = astNode.getArgument();
              List<? extends VariableTree> params =
                  lambdaExpression.getParameters();
              if (arg >= params.size()) {
                return false;
              }
              next = params.get(arg);
            } else {
              next = lambdaExpression.getBody();
            }
            break;
          }
          case MEMBER_REFERENCE: {
            MemberReferenceTree memberReference = (MemberReferenceTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.QUALIFIER_EXPRESSION)) {
              next = memberReference.getQualifierExpression();
            } else {
              int arg = astNode.getArgument();
              List<? extends ExpressionTree> typeArgs =
                  memberReference.getTypeArguments();
              if (arg >= typeArgs.size()) {
                return false;
              }
              next = typeArgs.get(arg);
            }
            break;
          }
          case MEMBER_SELECT: {
            MemberSelectTree memberSelect = (MemberSelectTree) actualNode;
            next = memberSelect.getExpression();
            break;
          }
          case METHOD_INVOCATION: {
            MethodInvocationTree methodInvocation =
                (MethodInvocationTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.TYPE_ARGUMENT)) {
              int arg = astNode.getArgument();
              List<? extends Tree> typeArgs = methodInvocation.getTypeArguments();
              if (arg >= typeArgs.size()) {
                return false;
              }
              next = typeArgs.get(arg);
            } else if (astNode.childSelectorIs(ASTPath.METHOD_SELECT)) {
              next = methodInvocation.getMethodSelect();
            } else {
              int arg = astNode.getArgument();
              List<? extends ExpressionTree> args = methodInvocation.getArguments();
              if (arg >= args.size()) {
                return false;
              }
              next = args.get(arg);
            }
            break;
          }
          case NEW_ARRAY: {
            NewArrayTree newArray = (NewArrayTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.TYPE)) {
              next = newArray.getType();
            } else if (astNode.childSelectorIs(ASTPath.DIMENSION)) {
              int arg = astNode.getArgument();
              List<? extends ExpressionTree> dims = newArray.getDimensions();
              if (arg >= dims.size()) {
                return false;
              }
              next = dims.get(arg);
            } else {
              int arg = astNode.getArgument();
              List<? extends ExpressionTree> inits = newArray.getInitializers();
              if (arg >= inits.size()) {
                return false;
              }
              next = inits.get(arg);
            }
            break;
          }
          case NEW_CLASS: {
            NewClassTree newClass = (NewClassTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.ENCLOSING_EXPRESSION)) {
              next = newClass.getEnclosingExpression();
            } else if (astNode.childSelectorIs(ASTPath.TYPE_ARGUMENT)) {
              int arg = astNode.getArgument();
              List<? extends Tree> typeArgs = newClass.getTypeArguments();
              if (arg >= typeArgs.size()) {
                return false;
              }
              next = typeArgs.get(arg);
            } else if (astNode.childSelectorIs(ASTPath.IDENTIFIER)) {
              next = newClass.getIdentifier();
            } else if (astNode.childSelectorIs(ASTPath.ARGUMENT)) {
              int arg = astNode.getArgument();
              List<? extends ExpressionTree> args = newClass.getArguments();
              if (arg >= args.size()) {
                return false;
              }
              next = args.get(arg);
            } else {
              next = newClass.getClassBody();
            }
            break;
          }
          case PARAMETERIZED_TYPE: {
            ParameterizedTypeTree parameterizedType =
                (ParameterizedTypeTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.TYPE)) {
              next = parameterizedType.getType();
            } else {
              int arg = astNode.getArgument();
              List<? extends Tree> typeArgs = parameterizedType.getTypeArguments();
              if (arg >= typeArgs.size()) {
                return false;
              }
              next = typeArgs.get(arg);
            }
            break;
          }
          case PARENTHESIZED: {
            ParenthesizedTree parenthesized = (ParenthesizedTree) actualNode;
            next = parenthesized.getExpression();
            break;
          }
          case RETURN: {
            ReturnTree returnn = (ReturnTree) actualNode;
            next = returnn.getExpression();
            break;
          }
          case SWITCH: {
            SwitchTree zwitch = (SwitchTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
              next = zwitch.getExpression();
            } else {
              int arg = astNode.getArgument();
              List<? extends CaseTree> cases = zwitch.getCases();
              if (arg >= cases.size()) {
                return false;
              }
              next = cases.get(arg);
            }
            break;
          }
          case SYNCHRONIZED: {
            SynchronizedTree synchronizzed = (SynchronizedTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
              next = synchronizzed.getExpression();
            } else {
              next = synchronizzed.getBlock();
            }
            break;
          }
          case THROW: {
            ThrowTree throww = (ThrowTree) actualNode;
            next = throww.getExpression();
            break;
          }
          case TRY: {
            TryTree tryy = (TryTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.BLOCK)) {
              next = tryy.getBlock();
            } else if (astNode.childSelectorIs(ASTPath.CATCH)) {
              int arg = astNode.getArgument();
              List<? extends CatchTree> catches = tryy.getCatches();
              if (arg >= catches.size()) {
                return false;
              }
              next = catches.get(arg);
            } else if (astNode.childSelectorIs(ASTPath.FINALLY_BLOCK)) {
              next = tryy.getFinallyBlock();
            } else {
              int arg = astNode.getArgument();
              List<? extends Tree> resources = tryy.getResources();
              if (arg >= resources.size()) {
                return false;
              }
              next = resources.get(arg);
            }
            break;
          }
          case TYPE_CAST: {
            TypeCastTree typeCast = (TypeCastTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.TYPE)) {
              next = typeCast.getType();
            } else {
              next = typeCast.getExpression();
            }
            break;
          }
          case UNION_TYPE: {
            UnionTypeTree unionType = (UnionTypeTree) actualNode;
            int arg = astNode.getArgument();
            List<? extends Tree> typeAlts = unionType.getTypeAlternatives();
            if (arg >= typeAlts.size()) {
              return false;
            }
            next = typeAlts.get(arg);
            break;
          }
          case VARIABLE: {
            VariableTree var = (VariableTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.INITIALIZER)) {
              next = var.getInitializer();
            } else {
              next = var.getType();
            }
            break;
          }
          case WHILE_LOOP: {
            WhileLoopTree whileLoop = (WhileLoopTree) actualNode;
            if (astNode.childSelectorIs(ASTPath.CONDITION)) {
              next = whileLoop.getCondition();
            } else {
              next = whileLoop.getStatement();
            }
            break;
          }
          default: {
            if (isBinaryOperator(actualNode.getKind())) {
              BinaryTree binary = (BinaryTree) actualNode;
              if (astNode.childSelectorIs(ASTPath.LEFT_OPERAND)) {
                next = binary.getLeftOperand();
              } else {
                next = binary.getRightOperand();
              }
            } else if (isCompoundAssignment(actualNode.getKind())) {
              CompoundAssignmentTree compoundAssignment =
                  (CompoundAssignmentTree) actualNode;
              if (astNode.childSelectorIs(ASTPath.VARIABLE)) {
                next = compoundAssignment.getVariable();
              } else {
                next = compoundAssignment.getExpression();
              }
            } else if (isUnaryOperator(actualNode.getKind())) {
              UnaryTree unary = (UnaryTree) actualNode;
              next = unary.getExpression();
            } else if (isWildcard(actualNode.getKind())) {
              WildcardTree wildcard = (WildcardTree) actualNode;
              // The following check is necessary because Oracle has decided that
              //   x instanceof Class<? extends Object>
              // will remain illegal even though it means the same thing as
              //   x instanceof Class<?>.
              if (i > 0) {  // TODO: refactor GenericArrayLoc to use same code?
                  Tree ancestor = actualPath.get(i-1);
              if (ancestor.getKind() == Tree.Kind.INSTANCE_OF) {
                System.err.println("WARNING: wildcard bounds not allowed"
                    + " in 'instanceof' expression; skipping insertion");
                return false;
              } else if (i > 1 && ancestor.getKind() ==
                  Tree.Kind.PARAMETERIZED_TYPE) {
                ancestor = actualPath.get(i-2);
                if (ancestor.getKind() == Tree.Kind.ARRAY_TYPE) {
                  System.err.println("WARNING: wildcard bounds not allowed"
                      + " in generic array type; skipping insertion");
                  return false;
                }
              }
              }
              next = wildcard.getBound();
            } else {
              throw new IllegalArgumentException("Illegal kind: "
                  + actualNode.getKind());
            }
            break;
          }
          }

          if (debug) {
            System.out.println("next: " + next);
          }
          if (next != actualPath.get(i + 1)) {
            if (debug) {
              System.out.println("no next match");
            }
            return false;
          }
        }
        return true;
      }

      /**
       * Determines if the given kinds match, false otherwise. Two kinds match if
       * they're exactly the same or if the two kinds are both compound
       * assignments, unary operators, binary operators or wildcards.
       * <p>
       * This is necessary because in the JAIF file these kinds are represented by
       * their general types (i.e. BinaryOperator, CompoundOperator, etc.) rather
       * than their kind (i.e. PLUS, MINUS, PLUS_ASSIGNMENT, XOR_ASSIGNMENT,
       * etc.). Internally, a single kind is used to represent each general type
       * (i.e. PLUS is used for BinaryOperator, PLUS_ASSIGNMENT is used for
       * CompoundAssignment, etc.). Yet, the actual source nodes have the correct
       * kind. So if an AST path entry has a PLUS kind, that really means it could
       * be any BinaryOperator, resulting in PLUS matching any other
       * BinaryOperator.
       *
       * @param kind1
       *            The first kind to match.
       * @param kind2
       *            The second kind to match.
       * @return {@code true} if the kinds match as described above, {@code false}
       *         otherwise.
       */
      private static boolean kindsMatch(Tree.Kind kind1, Tree.Kind kind2) {
        return kind1 == kind2
            || (isCompoundAssignment(kind1) && isCompoundAssignment(kind2))
            || (isUnaryOperator(kind1) && isUnaryOperator(kind2))
            || (isBinaryOperator(kind1) && isBinaryOperator(kind2))
            || (isWildcard(kind1) && isWildcard(kind2));
      }

      /**
       * Determines if the given kind is a compound assignment.
       *
       * @param kind
       *            The kind to test.
       * @return true if the given kind is a compound assignment.
       */
      private static boolean isCompoundAssignment(Tree.Kind kind) {
        return kind == Tree.Kind.PLUS_ASSIGNMENT
            || kind == Tree.Kind.MINUS_ASSIGNMENT
            || kind == Tree.Kind.MULTIPLY_ASSIGNMENT
            || kind == Tree.Kind.DIVIDE_ASSIGNMENT
            || kind == Tree.Kind.OR_ASSIGNMENT
            || kind == Tree.Kind.AND_ASSIGNMENT
            || kind == Tree.Kind.REMAINDER_ASSIGNMENT
            || kind == Tree.Kind.LEFT_SHIFT_ASSIGNMENT
            || kind == Tree.Kind.RIGHT_SHIFT
            || kind == Tree.Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT
            || kind == Tree.Kind.XOR_ASSIGNMENT;
      }

      /**
       * Determines if the given kind is a unary operator.
       *
       * @param kind
       *            The kind to test.
       * @return true if the given kind is a unary operator.
       */
      private static boolean isUnaryOperator(Tree.Kind kind) {
        return kind == Tree.Kind.POSTFIX_INCREMENT
            || kind == Tree.Kind.POSTFIX_DECREMENT
            || kind == Tree.Kind.PREFIX_INCREMENT
            || kind == Tree.Kind.PREFIX_DECREMENT
            || kind == Tree.Kind.UNARY_PLUS
            || kind == Tree.Kind.UNARY_MINUS
            || kind == Tree.Kind.BITWISE_COMPLEMENT
            || kind == Tree.Kind.LOGICAL_COMPLEMENT;
      }

      /**
       * Determines if the given kind is a binary operator.
       *
       * @param kind
       *            The kind to test.
       * @return true if the given kind is a binary operator.
       */
      private static boolean isBinaryOperator(Tree.Kind kind) {
        return kind == Tree.Kind.MULTIPLY
            || kind == Tree.Kind.DIVIDE
            || kind == Tree.Kind.REMAINDER
            || kind == Tree.Kind.PLUS
            || kind == Tree.Kind.MINUS
            || kind == Tree.Kind.LEFT_SHIFT
            || kind == Tree.Kind.RIGHT_SHIFT
            || kind == Tree.Kind.UNSIGNED_RIGHT_SHIFT
            || kind == Tree.Kind.LESS_THAN
            || kind == Tree.Kind.GREATER_THAN
            || kind == Tree.Kind.LESS_THAN_EQUAL
            || kind == Tree.Kind.GREATER_THAN_EQUAL
            || kind == Tree.Kind.EQUAL_TO
            || kind == Tree.Kind.NOT_EQUAL_TO
            || kind == Tree.Kind.AND
            || kind == Tree.Kind.XOR
            || kind == Tree.Kind.OR
            || kind == Tree.Kind.CONDITIONAL_AND
            || kind == Tree.Kind.CONDITIONAL_OR;
      }

      /**
       * Determines if the given kind is a wildcard.
       *
       * @param kind
       *            The kind to test.
       * @return true if the given kind is a wildcard.
       */
      private static boolean isWildcard(Tree.Kind kind) {
        return kind == Tree.Kind.UNBOUNDED_WILDCARD
            || kind == Tree.Kind.EXTENDS_WILDCARD
            || kind == Tree.Kind.SUPER_WILDCARD;
      }
    }

    /**
     * Determines if the given kind is a declaration.
     *
     * @param kind
     *            The kind to test.
     * @return true if the given kind is a declaration.
     */
    static boolean isDeclaration(Tree.Kind kind) {
      return kind == Tree.Kind.ANNOTATION
          || kind == Tree.Kind.CLASS
          || kind == Tree.Kind.METHOD
          || kind == Tree.Kind.VARIABLE;
    }

    /**
     * Determines whether an {@code ASTPath} can identify nodes of the
     *  given kind.
     *
     * @param kind
     *            The kind to test.
     * @return true if the given kind can be identified by an {@code ASTPath}.
     */
    static boolean isHandled(Kind kind) {
        return !(isDeclaration(kind)
            || kind == Kind.BREAK
            || kind == Kind.COMPILATION_UNIT
            || kind == Kind.CONTINUE
            || kind == Kind.IMPORT
            || kind == Kind.LAMBDA_EXPRESSION  // TODO
            || kind == Kind.MODIFIERS);
    }  // TODO: need "isType"?
}
