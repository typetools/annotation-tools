package annotator.find;

import java.util.ArrayList;
import java.util.List;

import annotations.io.ASTPath;
import annotator.Main;

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
     * This assumes that the astPath is valid. Specifically, that all of its
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

        // actualPath stores the path through the source code AST to this
        // location (specified by the "path" parameter to this method). It is
        // computed by traversing from this location up the source code AST
        // until it reaches a method node (this gets only the part of the path
        // within a method) or class node (this gets only the part of the path
        // within a field).
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
                ConditionalExpressionTree conditionalExpression = (ConditionalExpressionTree) actualNode;
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
                DoWhileLoopTree doWhileLoop = (DoWhileLoopTree) actualNode;
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
                ExpressionStatementTree expressionStatement = (ExpressionStatementTree) actualNode;
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
                LabeledStatementTree labeledStatement = (LabeledStatementTree) actualNode;
                next = labeledStatement.getStatement();
                break;
            }
            case LAMBDA_EXPRESSION: {
                LambdaExpressionTree lambdaExpression = (LambdaExpressionTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.PARAMETER)) {
                    int arg = astNode.getArgument();
                    List<? extends VariableTree> params = lambdaExpression.getParameters();
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
                    List<? extends ExpressionTree> typeArgs = memberReference.getTypeArguments();
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
                MethodInvocationTree methodInvocation = (MethodInvocationTree) actualNode;
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
                ParameterizedTypeTree parameterizedType = (ParameterizedTypeTree) actualNode;
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
                    CompoundAssignmentTree compoundAssignment = (CompoundAssignmentTree) actualNode;
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
                    next = wildcard.getBound();
                } else {
                    throw new IllegalArgumentException("Illegal kind: " + actualNode.getKind());
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
