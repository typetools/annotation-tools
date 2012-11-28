package annotator.find;

import java.util.ArrayList;
import java.util.List;

import annotations.io.ASTPath;
import annotator.Main;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
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
import com.sun.source.util.TreePath;

/**
 * A criterion to determine if a node matches a path through the AST.
 */
public class ASTPathCriterion implements Criterion {

    public static boolean debug = Main.debug;

    /**
     * The path through the AST to match.
     */
    private ASTPath astPath;

    /**
     * Constructs a new ASTPathCriterion to match the given AST path.
     * <p>
     * This assumes that the astPath is valid. Specifically, that all of it's
     * arguments have been previously validated.
     *
     * @param astPath
     *            The AST path to match.
     */
    public ASTPathCriterion(ASTPath astPath) {
        this.astPath = astPath;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSatisfiedBy(TreePath path, Tree leaf) {
        assert path == null || path.getLeaf() == leaf;
        return isSatisfiedBy(path);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSatisfiedBy(TreePath path) {
        if (path == null) {
            return false;
        }
        List<Tree> actualPath = new ArrayList<Tree>();
        while (path != null && path.getLeaf().getKind() != Tree.Kind.METHOD
                && path.getLeaf().getKind() != Tree.Kind.CLASS) {
            actualPath.add(0, path.getLeaf());
            path = path.getParentPath();
        }
        if (debug) {
            System.out.println("ASTPathCriterion.isSatisfiedBy");
            System.out.println("  " + astPath);
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
            Tree next = null;
            if (debug) {
                System.out.println("astNode: " + astNode);
                System.out.println("actualNode: " + actualNode.getKind());
            }
            if (!kindsMatch(astNode.getTreeKind(), actualNode.getKind())) {
                return false;
            }

            if (actualNode.getKind() == Tree.Kind.ANNOTATED_TYPE) {
                AnnotatedTypeTree annotatedType = (AnnotatedTypeTree) actualNode;
                if (astNode.getChildSelector().equals("annotation")) {
                    next = annotatedType.getAnnotations().get(
                            astNode.getArgument());
                } else {
                    next = annotatedType.getUnderlyingType();
                }
            } else if (actualNode.getKind() == Tree.Kind.ARRAY_ACCESS) {
                ArrayAccessTree arrayAccess = (ArrayAccessTree) actualNode;
                if (astNode.getChildSelector().equals("expression")) {
                    next = arrayAccess.getExpression();
                } else {
                    next = arrayAccess.getIndex();
                }
            } else if (actualNode.getKind() == Tree.Kind.ARRAY_TYPE) {
                ArrayTypeTree arrayType = (ArrayTypeTree) actualNode;
                next = arrayType.getType();
            } else if (actualNode.getKind() == Tree.Kind.ASSERT) {
                AssertTree azzert = (AssertTree) actualNode;
                if (astNode.getChildSelector().equals("condition")) {
                    next = azzert.getCondition();
                } else {
                    next = azzert.getDetail();
                }
            } else if (actualNode.getKind() == Tree.Kind.ASSIGNMENT) {
                AssignmentTree assignment = (AssignmentTree) actualNode;
                if (astNode.getChildSelector().equals("variable")) {
                    next = assignment.getVariable();
                } else {
                    next = assignment.getExpression();
                }
            } else if (isBinaryOperator(actualNode.getKind())) {
                BinaryTree binary = (BinaryTree) actualNode;
                if (astNode.getChildSelector().equals("leftOperand")) {
                    next = binary.getLeftOperand();
                } else {
                    next = binary.getRightOperand();
                }
            } else if (actualNode.getKind() == Tree.Kind.BLOCK) {
                BlockTree block = (BlockTree) actualNode;
                next = block.getStatements().get(astNode.getArgument());
            } else if (actualNode.getKind() == Tree.Kind.CASE) {
                CaseTree caze = (CaseTree) actualNode;
                if (astNode.getChildSelector().equals("expression")) {
                    next = caze.getExpression();
                } else {
                    next = caze.getStatements().get(astNode.getArgument());
                }
            } else if (actualNode.getKind() == Tree.Kind.CATCH) {
                CatchTree cach = (CatchTree) actualNode;
                if (astNode.getChildSelector().equals("parameter")) {
                    next = cach.getParameter();
                } else {
                    next = cach.getBlock();
                }
            } else if (isCompoundAssignment(actualNode.getKind())) {
                CompoundAssignmentTree compoundAssignment = (CompoundAssignmentTree) actualNode;
                if (astNode.getChildSelector().equals("variable")) {
                    next = compoundAssignment.getVariable();
                } else {
                    next = compoundAssignment.getExpression();
                }
            } else if (actualNode.getKind() == Tree.Kind.CONDITIONAL_EXPRESSION) {
                ConditionalExpressionTree conditionalExpression = (ConditionalExpressionTree) actualNode;
                if (astNode.getChildSelector().equals("condition")) {
                    next = conditionalExpression.getCondition();
                } else if (astNode.getChildSelector().equals("trueExpression")) {
                    next = conditionalExpression.getTrueExpression();
                } else {
                    next = conditionalExpression.getFalseExpression();
                }
            } else if (actualNode.getKind() == Tree.Kind.DO_WHILE_LOOP) {
                DoWhileLoopTree doWhileLoop = (DoWhileLoopTree) actualNode;
                if (astNode.getChildSelector().equals("condition")) {
                    next = doWhileLoop.getCondition();
                } else {
                    next = doWhileLoop.getStatement();
                }
            } else if (actualNode.getKind() == Tree.Kind.ENHANCED_FOR_LOOP) {
                EnhancedForLoopTree enhancedForLoop = (EnhancedForLoopTree) actualNode;
                if (astNode.getChildSelector().equals("variable")) {
                    next = enhancedForLoop.getVariable();
                } else if (astNode.getChildSelector().equals("expression")) {
                    next = enhancedForLoop.getExpression();
                } else {
                    next = enhancedForLoop.getStatement();
                }
            } else if (actualNode.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                ExpressionStatementTree expressionStatement = (ExpressionStatementTree) actualNode;
                next = expressionStatement.getExpression();
            } else if (actualNode.getKind() == Tree.Kind.FOR_LOOP) {
                ForLoopTree forLoop = (ForLoopTree) actualNode;
                if (astNode.getChildSelector().equals("initializer")) {
                    next = forLoop.getInitializer().get(astNode.getArgument());
                } else if (astNode.getChildSelector().equals("condition")) {
                    next = forLoop.getCondition();
                } else if (astNode.getChildSelector().equals("update")) {
                    next = forLoop.getUpdate().get(astNode.getArgument());
                } else {
                    next = forLoop.getStatement();
                }
            } else if (actualNode.getKind() == Tree.Kind.IF) {
                IfTree iff = (IfTree) actualNode;
                if (astNode.getChildSelector().equals("condition")) {
                    next = iff.getCondition();
                } else if (astNode.getChildSelector().equals("thenStatement")) {
                    next = iff.getThenStatement();
                } else {
                    next = iff.getElseStatement();
                }
            } else if (actualNode.getKind() == Tree.Kind.INSTANCE_OF) {
                InstanceOfTree instanceOf = (InstanceOfTree) actualNode;
                if (astNode.getChildSelector().equals("expression")) {
                    next = instanceOf.getExpression();
                } else {
                    next = instanceOf.getType();
                }
            } else if (actualNode.getKind() == Tree.Kind.LABELED_STATEMENT) {
                LabeledStatementTree labeledStatement = (LabeledStatementTree) actualNode;
                next = labeledStatement.getStatement();
            } else if (actualNode.getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
                LambdaExpressionTree lambdaExpression = (LambdaExpressionTree) actualNode;
                if (astNode.getChildSelector().equals("parameter")) {
                    next = lambdaExpression.getParameters().get(
                            astNode.getArgument());
                } else {
                    next = lambdaExpression.getBody();
                }
            } else if (actualNode.getKind() == Tree.Kind.MEMBER_REFERENCE) {
                MemberReferenceTree memberReference = (MemberReferenceTree) actualNode;
                if (astNode.getChildSelector().equals("qualifierExpression")) {
                    next = memberReference.getQualifierExpression();
                } else {
                    next = memberReference.getTypeArguments().get(
                            astNode.getArgument());
                }
            } else if (actualNode.getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree memberSelect = (MemberSelectTree) actualNode;
                next = memberSelect.getExpression();
            } else if (actualNode.getKind() == Tree.Kind.METHOD_INVOCATION) {
                MethodInvocationTree methodInvocation = (MethodInvocationTree) actualNode;
                if (astNode.getChildSelector().equals("typeArgument")) {
                    next = methodInvocation.getTypeArguments().get(
                            astNode.getArgument());
                } else if (astNode.getChildSelector().equals("methodSelect")) {
                    next = methodInvocation.getMethodSelect();
                } else {
                    next = methodInvocation.getArguments().get(
                            astNode.getArgument());
                }
            } else if (actualNode.getKind() == Tree.Kind.NEW_ARRAY) {
                NewArrayTree newArray = (NewArrayTree) actualNode;
                if (astNode.getChildSelector().equals("type")) {
                    next = newArray.getType();
                } else if (astNode.getChildSelector().equals("dimension")) {
                    next = newArray.getDimensions().get(astNode.getArgument());
                } else {
                    next = newArray.getInitializers()
                            .get(astNode.getArgument());
                }
            } else if (actualNode.getKind() == Tree.Kind.NEW_CLASS) {
                NewClassTree newClass = (NewClassTree) actualNode;
                if (astNode.getChildSelector().equals("enclosingExpression")) {
                    next = newClass.getEnclosingExpression();
                } else if (astNode.getChildSelector().equals("typeArgument")) {
                    next = newClass.getTypeArguments().get(
                            astNode.getArgument());
                } else if (astNode.getChildSelector().equals("identifier")) {
                    next = newClass.getIdentifier();
                } else if (astNode.getChildSelector().equals("argument")) {
                    next = newClass.getArguments().get(astNode.getArgument());
                } else {
                    next = newClass.getClassBody();
                }
            } else if (actualNode.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                ParameterizedTypeTree parameterizedType = (ParameterizedTypeTree) actualNode;
                if (astNode.getChildSelector().equals("type")) {
                    next = parameterizedType.getType();
                } else {
                    next = parameterizedType.getTypeArguments().get(
                            astNode.getArgument());
                }
            } else if (actualNode.getKind() == Tree.Kind.PARENTHESIZED) {
                ParenthesizedTree parenthesized = (ParenthesizedTree) actualNode;
                next = parenthesized.getExpression();
            } else if (actualNode.getKind() == Tree.Kind.RETURN) {
                ReturnTree returnn = (ReturnTree) actualNode;
                next = returnn.getExpression();
            } else if (actualNode.getKind() == Tree.Kind.SWITCH) {
                SwitchTree zwitch = (SwitchTree) actualNode;
                if (astNode.getChildSelector().equals("expression")) {
                    next = zwitch.getExpression();
                } else {
                    next = zwitch.getCases().get(astNode.getArgument());
                }
            } else if (actualNode.getKind() == Tree.Kind.SYNCHRONIZED) {
                SynchronizedTree synchronizzed = (SynchronizedTree) actualNode;
                if (astNode.getChildSelector().equals("expression")) {
                    next = synchronizzed.getExpression();
                } else {
                    next = synchronizzed.getBlock();
                }
            } else if (actualNode.getKind() == Tree.Kind.THROW) {
                ThrowTree throww = (ThrowTree) actualNode;
                next = throww.getExpression();
            } else if (actualNode.getKind() == Tree.Kind.TRY) {
                TryTree tryy = (TryTree) actualNode;
                if (astNode.getChildSelector().equals("block")) {
                    next = tryy.getBlock();
                } else if (astNode.getChildSelector().equals("catch")) {
                    next = tryy.getCatches().get(astNode.getArgument());
                } else if (astNode.getChildSelector().equals("finallyBlock")) {
                    next = tryy.getFinallyBlock();
                } else {
                    next = tryy.getResources().get(astNode.getArgument());
                }
            } else if (actualNode.getKind() == Tree.Kind.TYPE_CAST) {
                TypeCastTree typeCast = (TypeCastTree) actualNode;
                if (astNode.getChildSelector().equals("type")) {
                    next = typeCast.getType();
                } else {
                    next = typeCast.getExpression();
                }
            } else if (isUnaryOperator(actualNode.getKind())) {
                UnaryTree unary = (UnaryTree) actualNode;
                next = unary.getExpression();
            } else if (actualNode.getKind() == Tree.Kind.UNION_TYPE) {
                UnionTypeTree unionType = (UnionTypeTree) actualNode;
                next = unionType.getTypeAlternatives().get(
                        astNode.getArgument());
            } else if (actualNode.getKind() == Tree.Kind.VARIABLE) {
                VariableTree var = (VariableTree) actualNode;
                if (astNode.getChildSelector().equals("initializer")) {
                    next = var.getInitializer();
                } else if (astNode.getChildSelector().equals("type")) {
                    next = var.getType();
                }
            } else if (actualNode.getKind() == Tree.Kind.WHILE_LOOP) {
                WhileLoopTree whileLoop = (WhileLoopTree) actualNode;
                if (astNode.getChildSelector().equals("condition")) {
                    next = whileLoop.getCondition();
                } else {
                    next = whileLoop.getStatement();
                }
            } else if (isWildcard(actualNode.getKind())) {
                WildcardTree wildcard = (WildcardTree) actualNode;
                next = wildcard.getBound();
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
     *
     * @param kind1
     *            The first kind to match.
     * @param kind2
     *            The second kind to match.
     * @return true if the kinds match, as described above.
     */
    private boolean kindsMatch(Tree.Kind kind1, Tree.Kind kind2) {
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
    private boolean isCompoundAssignment(Tree.Kind kind) {
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
    private boolean isUnaryOperator(Tree.Kind kind) {
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
    private boolean isBinaryOperator(Tree.Kind kind) {
        return kind == Tree.Kind.MULTIPLY || kind == Tree.Kind.DIVIDE
                || kind == Tree.Kind.REMAINDER || kind == Tree.Kind.PLUS
                || kind == Tree.Kind.MINUS || kind == Tree.Kind.LEFT_SHIFT
                || kind == Tree.Kind.RIGHT_SHIFT
                || kind == Tree.Kind.UNSIGNED_RIGHT_SHIFT
                || kind == Tree.Kind.LESS_THAN
                || kind == Tree.Kind.GREATER_THAN
                || kind == Tree.Kind.LESS_THAN_EQUAL
                || kind == Tree.Kind.GREATER_THAN_EQUAL
                || kind == Tree.Kind.EQUAL_TO || kind == Tree.Kind.NOT_EQUAL_TO
                || kind == Tree.Kind.AND || kind == Tree.Kind.XOR
                || kind == Tree.Kind.OR || kind == Tree.Kind.CONDITIONAL_AND
                || kind == Tree.Kind.CONDITIONAL_OR;
    }

    /**
     * Determines if the given kind is a wildcard.
     *
     * @param kind
     *            The kind to test.
     * @return true if the given kind is a wildcard.
     */
    private boolean isWildcard(Tree.Kind kind) {
        return kind == Tree.Kind.UNBOUNDED_WILDCARD
                || kind == Tree.Kind.EXTENDS_WILDCARD
                || kind == Tree.Kind.SUPER_WILDCARD;
    }

    /** {@inheritDoc} */
    @Override
    public Kind getKind() {
        return Kind.AST_PATH;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ASTPathCriterion: " + astPath;
    }
}
