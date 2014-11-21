package annotator.find;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;

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
import com.sun.source.tree.ClassTree;
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
import com.sun.source.tree.MethodTree;
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
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

/**
 * A criterion to determine if a node matches a path through the AST.
 */
public class ASTPathCriterion implements Criterion {

    public static boolean debug = Main.debug;

    /**
     * The path through the AST to match.
     */
    ASTPath astPath;

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
        Tree leaf = path.getLeaf();
        Tree.Kind kind = leaf.getKind();
        while (kind != Tree.Kind.METHOD && !ASTPath.isClassEquiv(kind)) {
            actualPath.add(0, leaf);
            path = path.getParentPath();
            if (path == null) { break; }
            leaf = path.getLeaf();
            kind = leaf.getKind();
        }

        // If astPath starts with Method.* or Class.*, include the
        // MethodTree or ClassTree on actualPath.
        if (path != null && !astPath.isEmpty()) {
            Tree.Kind entryKind = astPath.get(0).getTreeKind();
            if (entryKind == Tree.Kind.METHOD
                            && kind == Tree.Kind.METHOD
                    || entryKind == Tree.Kind.CLASS
                            && ASTPath.isClassEquiv(kind)) {
                actualPath.add(0, leaf);
            }
        }

        if (debug) {
            System.out.println("ASTPathCriterion.isSatisfiedBy");
            System.out.println("  " + astPath);
            for (Tree t : actualPath) {
                System.out.println("  " + t.getKind() + ": "
                        + t.toString().replace('\n', ' '));
            }
        }

        int astPathLen = astPath.size();
        int actualPathLen = actualPath.size();
        if (astPathLen == 0 || actualPathLen == 0) { return false; }
        //if (actualPathLen != astPathLen + (isOnNewArrayType ? 0 : 1)) {
        //    return false;
        //}

        Tree next = null;
        int i = 0;
        while (true) {
            ASTPath.ASTEntry astNode = astPath.get(i);
            Tree actualNode = actualPath.get(i);
            if (!kindsMatch(astNode.getTreeKind(), actualNode.getKind())) {
                return isBoundableWildcard(actualPath, i);
            }

            if (debug) {
                System.out.println("astNode: " + astNode);
                System.out.println("actualNode: " + actualNode.getKind());
            }

            // Based on the child selector and (optional) argument in "astNode",
            // "next" will get set to the next source node below "actualNode".
            // Then "next" will be compared with the node following "astNode"
            // in "actualPath". If it's not a match, this is not the correct
            // location. If it is a match, keep going.
            next = getNext(actualNode, astNode);
            if (next == null) {
                return checkNull(TreePath.getPath(path, actualNode), i);
            }
            if (!(next instanceof JCTree)) {
                // converted from array type, not in source AST...
                // need to extend actualPath with "artificial" node
                actualPath.add(next);
                ++actualPathLen;
            }
            if (debug) {
                System.out.println("next: " + next);
            }

            if (++i >= astPathLen || i >= actualPathLen) { break; }
            if (next != actualPath.get(i)) {
              if (debug) {
                  System.out.println("no next match");
              }
              return false;
            }
        }

        if (i >= actualPathLen || next != actualPath.get(i)) {
            if (debug) {
                System.out.println("no next match");
            }
            return false;
        }
        return true;
    }

    private Tree getNext(Tree actualNode, ASTPath.ASTEntry astNode) {
        try {
            switch (actualNode.getKind()) {
            case ANNOTATED_TYPE: {
                AnnotatedTypeTree annotatedType =
                    (AnnotatedTypeTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.ANNOTATION)) {
                    int arg = astNode.getArgument();
                    List<? extends AnnotationTree> annos =
                        annotatedType.getAnnotations();
                    if (arg >= annos.size()) {
                        return null;
                    }
                    return annos.get(arg);
                } else {
                    return annotatedType.getUnderlyingType();
                }
            }
            case ARRAY_ACCESS: {
                ArrayAccessTree arrayAccess = (ArrayAccessTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
                    return arrayAccess.getExpression();
                } else {
                    return arrayAccess.getIndex();
                }
            }
            case ARRAY_TYPE: {
                ArrayTypeTree arrayType = (ArrayTypeTree) actualNode;
                return arrayType.getType();
            }
            case ASSERT: {
                AssertTree azzert = (AssertTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.CONDITION)) {
                    return azzert.getCondition();
                } else {
                    return azzert.getDetail();
                }
            }
            case ASSIGNMENT: {
                AssignmentTree assignment = (AssignmentTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.VARIABLE)) {
                    return assignment.getVariable();
                } else {
                    return assignment.getExpression();
                }
            }
            case BLOCK: {
                BlockTree block = (BlockTree) actualNode;
                int arg = astNode.getArgument();
                List<? extends StatementTree> statements = block.getStatements();
                if (arg >= block.getStatements().size()) {
                    return null;
                }
                return statements.get(arg);
            }
            case CASE: {
                CaseTree caze = (CaseTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
                    return caze.getExpression();
                } else {
                    int arg = astNode.getArgument();
                    List<? extends StatementTree> statements = caze.getStatements();
                    if (arg >= statements.size()) {
                        return null;
                    }
                    return statements.get(arg);
                }
            }
            case CATCH: {
                CatchTree cach = (CatchTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.PARAMETER)) {
                    return cach.getParameter();
                } else {
                    return cach.getBlock();
                }
            }
            case ANNOTATION:
            case CLASS:
            case ENUM:
            case INTERFACE: {
                ClassTree clazz = (ClassTree) actualNode;
                int arg = astNode.getArgument();
                if (astNode.childSelectorIs(ASTPath.TYPE_PARAMETER)) {
                    return clazz.getTypeParameters().get(arg);
                } else if (astNode.childSelectorIs(ASTPath.INITIALIZER)) {
                    int i = 0;
                    for (Tree member : clazz.getMembers()) {
                        if (member.getKind() == Tree.Kind.BLOCK && arg == i++) {
                            return member;
                        }
                    }
                } else if (astNode.childSelectorIs(ASTPath.BOUND)) {
                  return arg < 0 ? clazz.getExtendsClause()
                          : clazz.getImplementsClause().get(arg);
                } else {
                    return null;
                }
            }
            case CONDITIONAL_EXPRESSION: {
                ConditionalExpressionTree conditionalExpression = (ConditionalExpressionTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.CONDITION)) {
                    return conditionalExpression.getCondition();
                } else if (astNode.childSelectorIs(ASTPath.TRUE_EXPRESSION)) {
                    return conditionalExpression.getTrueExpression();
                } else {
                    return conditionalExpression.getFalseExpression();
                }
            }
            case DO_WHILE_LOOP: {
                DoWhileLoopTree doWhileLoop = (DoWhileLoopTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.CONDITION)) {
                    return doWhileLoop.getCondition();
                } else {
                    return doWhileLoop.getStatement();
                }
            }
            case ENHANCED_FOR_LOOP: {
                EnhancedForLoopTree enhancedForLoop = (EnhancedForLoopTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.VARIABLE)) {
                    return enhancedForLoop.getVariable();
                } else if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
                    return enhancedForLoop.getExpression();
                } else {
                    return enhancedForLoop.getStatement();
                }
            }
            case EXPRESSION_STATEMENT: {
                ExpressionStatementTree expressionStatement = (ExpressionStatementTree) actualNode;
                return expressionStatement.getExpression();
            }
            case FOR_LOOP: {
                ForLoopTree forLoop = (ForLoopTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.INITIALIZER)) {
                    int arg = astNode.getArgument();
                    List<? extends StatementTree> inits = forLoop.getInitializer();
                    if (arg >= inits.size()) {
                        return null;
                    }
                    return inits.get(arg);
                } else if (astNode.childSelectorIs(ASTPath.CONDITION)) {
                    return forLoop.getCondition();
                } else if (astNode.childSelectorIs(ASTPath.UPDATE)) {
                    int arg = astNode.getArgument();
                    List<? extends ExpressionStatementTree> updates = forLoop.getUpdate();
                    if (arg >= updates.size()) {
                        return null;
                    }
                    return updates.get(arg);
                } else {
                    return forLoop.getStatement();
                }
            }
            case IF: {
                IfTree iff = (IfTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.CONDITION)) {
                    return iff.getCondition();
                } else if (astNode.childSelectorIs(ASTPath.THEN_STATEMENT)) {
                    return iff.getThenStatement();
                } else {
                    return iff.getElseStatement();
                }
            }
            case INSTANCE_OF: {
                InstanceOfTree instanceOf = (InstanceOfTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
                    return instanceOf.getExpression();
                } else {
                    return instanceOf.getType();
                }
            }
            case LABELED_STATEMENT: {
                LabeledStatementTree labeledStatement = (LabeledStatementTree) actualNode;
                return labeledStatement.getStatement();
            }
            case LAMBDA_EXPRESSION: {
                LambdaExpressionTree lambdaExpression = (LambdaExpressionTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.PARAMETER)) {
                    int arg = astNode.getArgument();
                    List<? extends VariableTree> params = lambdaExpression.getParameters();
                    if (arg >= params.size()) {
                        return null;
                    }
                    return params.get(arg);
                } else {
                    return lambdaExpression.getBody();
                }
            }
            case MEMBER_REFERENCE: {
                MemberReferenceTree memberReference = (MemberReferenceTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.QUALIFIER_EXPRESSION)) {
                    return memberReference.getQualifierExpression();
                } else {
                    int arg = astNode.getArgument();
                    List<? extends ExpressionTree> typeArgs = memberReference.getTypeArguments();
                    if (arg >= typeArgs.size()) {
                        return null;
                    }
                    return typeArgs.get(arg);
                }
            }
            case MEMBER_SELECT: {
                MemberSelectTree memberSelect = (MemberSelectTree) actualNode;
                return memberSelect.getExpression();
            }
            case METHOD: {
                MethodTree method = (MethodTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.TYPE)) {
                    return method.getReturnType();
                } else if (astNode.childSelectorIs(ASTPath.PARAMETER)) {
                    int arg = astNode.getArgument();
                    List<? extends VariableTree> params =
                            method.getParameters();
                    return arg < 0 ? method.getReceiverParameter()
                            : arg < params.size() ? params.get(arg) : null;
                } else if (astNode.childSelectorIs(ASTPath.TYPE_PARAMETER)) {
                    int arg = astNode.getArgument();
                    return method.getTypeParameters().get(arg);
                } else {    // BODY
                    return method.getBody();
                }
            }
            case METHOD_INVOCATION: {
                MethodInvocationTree methodInvocation = (MethodInvocationTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.TYPE_ARGUMENT)) {
                    int arg = astNode.getArgument();
                    List<? extends Tree> typeArgs = methodInvocation.getTypeArguments();
                    if (arg >= typeArgs.size()) {
                        return null;
                    }
                    return typeArgs.get(arg);
                } else if (astNode.childSelectorIs(ASTPath.METHOD_SELECT)) {
                    return methodInvocation.getMethodSelect();
                } else {
                    int arg = astNode.getArgument();
                    List<? extends ExpressionTree> args = methodInvocation.getArguments();
                    if (arg >= args.size()) {
                        return null;
                    }
                    return args.get(arg);
                }
            }
            case NEW_ARRAY: {
                NewArrayTree newArray = (NewArrayTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.TYPE)) {
                    int arg = astNode.getArgument();
                    Type type = ((JCTree.JCNewArray) newArray).type;
                    Tree typeTree = Insertions.TypeTree.fromType(type);
                    while (arg > 0) {
                      if (!(typeTree instanceof ArrayTypeTree)) { return null; }
                      typeTree = ((ArrayTypeTree) typeTree).getType();
                      --arg;
                    }
                    return typeTree;
                    //Tree type = newArray.getType();
                    //return arg == 0 ? typeTree : null;
                } else if (astNode.childSelectorIs(ASTPath.DIMENSION)) {
                    int arg = astNode.getArgument();
                    List<? extends ExpressionTree> dims = newArray.getDimensions();
                    if (arg >= dims.size()) {
                        return null;
                    }
                    return dims.get(arg);
                } else {
                    int arg = astNode.getArgument();
                    List<? extends ExpressionTree> inits = newArray.getInitializers();
                    if (arg >= inits.size()) {
                        return null;
                    }
                    return inits.get(arg);
                }
            }
            case NEW_CLASS: {
                NewClassTree newClass = (NewClassTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.ENCLOSING_EXPRESSION)) {
                    return newClass.getEnclosingExpression();
                } else if (astNode.childSelectorIs(ASTPath.TYPE_ARGUMENT)) {
                    int arg = astNode.getArgument();
                    List<? extends Tree> typeArgs = newClass.getTypeArguments();
                    if (arg >= typeArgs.size()) {
                        return null;
                    }
                    return typeArgs.get(arg);
                } else if (astNode.childSelectorIs(ASTPath.IDENTIFIER)) {
                    return newClass.getIdentifier();
                } else if (astNode.childSelectorIs(ASTPath.ARGUMENT)) {
                    int arg = astNode.getArgument();
                    List<? extends ExpressionTree> args = newClass.getArguments();
                    if (arg >= args.size()) {
                        return null;
                    }
                    return args.get(arg);
                } else {
                    return newClass.getClassBody(); // For anonymous classes
                }
            }
            case PARAMETERIZED_TYPE: {
                ParameterizedTypeTree parameterizedType = (ParameterizedTypeTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.TYPE)) {
                    return parameterizedType.getType();
                } else {
                    int arg = astNode.getArgument();
                    List<? extends Tree> typeArgs = parameterizedType.getTypeArguments();
                    if (arg >= typeArgs.size()) {
                        return null;
                    }
                    return typeArgs.get(arg);
                }
            }
            case PARENTHESIZED: {
                ParenthesizedTree parenthesized = (ParenthesizedTree) actualNode;
                return parenthesized.getExpression();
            }
            case RETURN: {
                ReturnTree returnn = (ReturnTree) actualNode;
                return returnn.getExpression();
            }
            case SWITCH: {
                SwitchTree zwitch = (SwitchTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
                    return zwitch.getExpression();
                } else {
                    int arg = astNode.getArgument();
                    List<? extends CaseTree> cases = zwitch.getCases();
                    if (arg >= cases.size()) {
                        return null;
                    }
                    return cases.get(arg);
                }
            }
            case SYNCHRONIZED: {
                SynchronizedTree synchronizzed = (SynchronizedTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.EXPRESSION)) {
                    return synchronizzed.getExpression();
                } else {
                    return synchronizzed.getBlock();
                }
            }
            case THROW: {
                ThrowTree throww = (ThrowTree) actualNode;
                return throww.getExpression();
            }
            case TRY: {
                TryTree tryy = (TryTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.BLOCK)) {
                    return tryy.getBlock();
                } else if (astNode.childSelectorIs(ASTPath.CATCH)) {
                    int arg = astNode.getArgument();
                    List<? extends CatchTree> catches = tryy.getCatches();
                    if (arg >= catches.size()) {
                        return null;
                    }
                    return catches.get(arg);
                } else if (astNode.childSelectorIs(ASTPath.FINALLY_BLOCK)) {
                    return tryy.getFinallyBlock();
                } else {
                    int arg = astNode.getArgument();
                    List<? extends Tree> resources = tryy.getResources();
                    if (arg >= resources.size()) {
                        return null;
                    }
                    return resources.get(arg);
                }
            }
            case TYPE_CAST: {
                TypeCastTree typeCast = (TypeCastTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.TYPE)) {
                    return typeCast.getType();
                } else {
                    return typeCast.getExpression();
                }
            }
            case TYPE_PARAMETER: {
                TypeParameterTree typeParam = (TypeParameterTree) actualNode;
                List<? extends Tree> bounds = typeParam.getBounds();
                int arg = astNode.getArgument();
                return bounds.get(arg);
            }
            case UNION_TYPE: {
                UnionTypeTree unionType = (UnionTypeTree) actualNode;
                int arg = astNode.getArgument();
                List<? extends Tree> typeAlts = unionType.getTypeAlternatives();
                if (arg >= typeAlts.size()) {
                    return null;
                }
                return typeAlts.get(arg);
            }
            case VARIABLE: {
                // A VariableTree can have modifiers, but we only look at
                // the initializer and type because modifiers can't be
                // annotated. Any annotations on the LHS must be on the type.
                VariableTree var = (VariableTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.INITIALIZER)) {
                    return var.getInitializer();
                } else if (astNode.childSelectorIs(ASTPath.TYPE)) {
                    return var.getType();
                } else {
                    return null;
                }
            }
            case WHILE_LOOP: {
                WhileLoopTree whileLoop = (WhileLoopTree) actualNode;
                if (astNode.childSelectorIs(ASTPath.CONDITION)) {
                    return whileLoop.getCondition();
                } else {
                    return whileLoop.getStatement();
                }
            }
            default: {
                if (ASTPath.isBinaryOperator(actualNode.getKind())) {
                    BinaryTree binary = (BinaryTree) actualNode;
                    if (astNode.childSelectorIs(ASTPath.LEFT_OPERAND)) {
                        return binary.getLeftOperand();
                    } else {
                        return binary.getRightOperand();
                    }
                } else if (ASTPath.isCompoundAssignment(actualNode.getKind())) {
                    CompoundAssignmentTree compoundAssignment =
                            (CompoundAssignmentTree) actualNode;
                    if (astNode.childSelectorIs(ASTPath.VARIABLE)) {
                        return compoundAssignment.getVariable();
                    } else {
                        return compoundAssignment.getExpression();
                    }
                } else if (ASTPath.isUnaryOperator(actualNode.getKind())) {
                    UnaryTree unary = (UnaryTree) actualNode;
                    return unary.getExpression();
                } else if (isWildcard(actualNode.getKind())) {
                    WildcardTree wildcard = (WildcardTree) actualNode;
                    return wildcard.getBound();
                } else {
                    throw new IllegalArgumentException("Illegal kind: "
                            + actualNode.getKind());
                }
              }
            }
        } catch (RuntimeException ex) { return null; }
    }

    private boolean checkNull(TreePath path, int ix) {
        Tree node = path.getLeaf();
        int last = astPath.size() - 1;
        ASTPath.ASTEntry entry = astPath.get(ix);
        Tree.Kind kind = entry.getTreeKind();

        switch (kind) {
        case ANNOTATION:
        case CLASS:  // "extends" clause?
        case INTERFACE:
        case TYPE_PARAMETER:
            return ix == last && entry.getArgument() == 0
                && entry.childSelectorIs(ASTPath.BOUND);
        case METHOD:  // nullary constructor? receiver?
            MethodTree method = (MethodTree) node;
            List<? extends VariableTree> params = method.getParameters();
            if ("<init>".equals(method.getName().toString())) {
                if (ix == last) { return true; }
                ASTPath.ASTEntry next = astPath.get(++ix);
                String selector = next.getChildSelector();
                Tree typeTree =
                    ASTPath.TYPE_PARAMETER.equals(selector)
                        ? method.getTypeParameters().get(next.getArgument())
                  : ASTPath.PARAMETER.equals(selector)
                        ? params.get(next.getArgument()).getType()
                  : null;
                return typeTree != null && checkTypePath(ix, typeTree);
            } else if (entry.childSelectorIs(ASTPath.PARAMETER)
                    && entry.getArgument() == -1) {
                if (ix == last) { return true; }
                VariableTree rcvrParam = method.getReceiverParameter();
                if (rcvrParam == null) {
                  //ClassTree clazz = methodReceiverType(path);
                  //return checkReceiverType(ix,
                  //    ((JCTree.JCClassDecl) clazz).type);
                } else {
                  return checkTypePath(ix+1, rcvrParam.getType());
                }
            }
            return false;
        case NEW_ARRAY:
            if (entry.childSelectorIs(ASTPath.TYPE)) { return ix == last; }
            NewArrayTree newArray = (NewArrayTree) node;
            int arg = entry.getArgument();
            List<? extends ExpressionTree> typeTrees =
                entry.childSelectorIs(ASTPath.DIMENSION)
                    ? newArray.getDimensions()
              : entry.childSelectorIs(ASTPath.INITIALIZER)
                    ? newArray.getInitializers()
              : null;
            return typeTrees != null && checkTypePath(ix+1, typeTrees.get(arg));
        default:  // TODO: casts?
            return false;
        }
    }

    private boolean checkReceiverType(int i, Type t) {
        if (t == null) { return false; }
        while (++i < astPath.size()) {
            ASTPath.ASTEntry entry = astPath.get(i);
            switch (entry.getTreeKind()) {
            case ANNOTATED_TYPE:
              break;
            case ARRAY_TYPE:
              if (t.getKind() != TypeKind.ARRAY) { return false; }
              t = ((Type.ArrayType) t).getComponentType();
              break;
            case MEMBER_SELECT:
              // TODO
              break;
            case PARAMETERIZED_TYPE:
              if (entry.childSelectorIs(ASTPath.TYPE_PARAMETER)) {
                if (!t.isParameterized()) { return false; }
                List<Type> args = t.getTypeArguments();
                int a = entry.getArgument();
                if (a >= args.size()) { return false; }
                t = args.get(a);
              } // else TYPE -- stay?
              break;
            case TYPE_PARAMETER:
              if (t.getKind() != TypeKind.WILDCARD) { return false; }
              t = t.getLowerBound();
              break;
            case EXTENDS_WILDCARD:
              if (t.getKind() != TypeKind.WILDCARD) { return false; }
              t = ((Type.WildcardType) t).getExtendsBound();
              break;
            case SUPER_WILDCARD:
              if (t.getKind() != TypeKind.WILDCARD) { return false; }
              t = ((Type.WildcardType) t).getSuperBound();
              break;
            case UNBOUNDED_WILDCARD:
              if (t.getKind() != TypeKind.WILDCARD) { return false; }
              t = t.getLowerBound();
              break;
            default:
              return false;
            }
            if (t == null) { return false; }
        }
        return true;
    }

    public static ClassTree methodReceiverType(TreePath path) {
      Tree t = path.getLeaf();
      if (t.getKind() != Tree.Kind.METHOD) { return null; }
      JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) t;
      if ((method.mods.flags & Flags.STATIC) != 0) { return null; }

      // Find the name of the class with type parameters to create the
      // receiver. Walk up the tree and pick up class names to add to
      // the receiver type. Since we're starting from the innermost
      // class, the classes we get to at earlier iterations of the loop
      // are inside of the classes we get to at later iterations.
      TreePath parent = path.getParentPath();
      Tree leaf = parent.getLeaf();
      Tree.Kind kind = leaf.getKind();

      // For an inner class constructor, the receiver comes from the
      // superclass, so skip past the first type definition.
      boolean skip = method.getReturnType() == null;

      while (kind != Tree.Kind.COMPILATION_UNIT
          && kind != Tree.Kind.NEW_CLASS) {
        if (kind == Tree.Kind.CLASS
            || kind == Tree.Kind.INTERFACE
            || kind == Tree.Kind.ENUM
            || kind == Tree.Kind.ANNOTATION_TYPE) {
          JCTree.JCClassDecl clazz = (JCTree.JCClassDecl) leaf;
          boolean isStatic = kind == Tree.Kind.INTERFACE
              || kind == Tree.Kind.ENUM
              || clazz.getModifiers().getFlags().contains(Modifier.STATIC);
          skip &= !isStatic;
          if (!skip || isStatic) { return clazz; }
          skip = false;
        }

        parent = path.getParentPath();
        leaf = parent.getLeaf();
        kind = leaf.getKind();
      }

      throw new IllegalArgumentException("no receiver for non-inner constructor");
    }

    private boolean checkTypePath(int i, Tree typeTree) {
        try {
loop:       while (typeTree != null && i < astPath.size()) {
                ASTPath.ASTEntry entry = astPath.get(i);
                Tree.Kind kind = entry.getTreeKind();
                switch (kind) {
                case ANNOTATED_TYPE:
                    typeTree = ((AnnotatedTypeTree) typeTree)
                        .getUnderlyingType();
                    continue;
                case ARRAY_TYPE:
                    typeTree = ((ArrayTypeTree) typeTree).getType();
                    break;
                case MEMBER_SELECT:
                    typeTree = ((MemberSelectTree) typeTree).getExpression();
                    break;
                case PARAMETERIZED_TYPE:
                    if (entry.childSelectorIs(ASTPath.TYPE_ARGUMENT)) {
                        int arg = entry.getArgument();
                        typeTree = ((ParameterizedTypeTree) typeTree)
                                .getTypeArguments().get(arg);
                    } else {  // TYPE
                        typeTree = ((ParameterizedTypeTree) typeTree).getType();
                    }
                    break;
                default:
                    if (isWildcard(kind)) {
                        return ++i == astPath.size();  // ???
                    }
                    break loop;
                }
                ++i;
            }
        } catch (RuntimeException ex) {}
        return false;
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
        return kind1 == kind2 ? true
              : ASTPath.isClassEquiv(kind1)
                      ? ASTPath.isClassEquiv(kind2)
              : ASTPath.isCompoundAssignment(kind1)
                      ? ASTPath.isCompoundAssignment(kind2)
              : ASTPath.isUnaryOperator(kind1)
                      ? ASTPath.isUnaryOperator(kind2)
              : ASTPath.isBinaryOperator(kind1)
                      ? ASTPath.isBinaryOperator(kind2)
              : ASTPath.isWildcard(kind1)
                      ? ASTPath.isWildcard(kind2)
              : false;
    }

    /**
     * Determines if the given kind is a binary operator.
     *
     * @param kind
     *            The kind to test.
     * @return true if the given kind is a binary operator.
     */
    public boolean isBinaryOperator(Tree.Kind kind) {
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

    public boolean isExpression(Tree.Kind kind) {
        switch (kind) {
        case ARRAY_ACCESS:
        case ASSIGNMENT:
        case CONDITIONAL_EXPRESSION:
        case EXPRESSION_STATEMENT:
        case MEMBER_SELECT:
        case MEMBER_REFERENCE:
        case IDENTIFIER:
        case INSTANCE_OF:
        case METHOD_INVOCATION:
        case NEW_ARRAY:
        case NEW_CLASS:
        case LAMBDA_EXPRESSION:
        case PARENTHESIZED:
        case TYPE_CAST:
        case POSTFIX_INCREMENT:
        case POSTFIX_DECREMENT:
        case PREFIX_INCREMENT:
        case PREFIX_DECREMENT:
        case UNARY_PLUS:
        case UNARY_MINUS:
        case BITWISE_COMPLEMENT:
        case LOGICAL_COMPLEMENT:
        case MULTIPLY:
        case DIVIDE:
        case REMAINDER:
        case PLUS:
        case MINUS:
        case LEFT_SHIFT:
        case RIGHT_SHIFT:
        case UNSIGNED_RIGHT_SHIFT:
        case LESS_THAN:
        case GREATER_THAN:
        case LESS_THAN_EQUAL:
        case GREATER_THAN_EQUAL:
        case EQUAL_TO:
        case NOT_EQUAL_TO:
        case AND:
        case XOR:
        case OR:
        case CONDITIONAL_AND:
        case CONDITIONAL_OR:
        case MULTIPLY_ASSIGNMENT:
        case DIVIDE_ASSIGNMENT:
        case REMAINDER_ASSIGNMENT:
        case PLUS_ASSIGNMENT:
        case MINUS_ASSIGNMENT:
        case LEFT_SHIFT_ASSIGNMENT:
        case RIGHT_SHIFT_ASSIGNMENT:
        case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
        case AND_ASSIGNMENT:
        case XOR_ASSIGNMENT:
        case OR_ASSIGNMENT:
        case INT_LITERAL:
        case LONG_LITERAL:
        case FLOAT_LITERAL:
        case DOUBLE_LITERAL:
        case BOOLEAN_LITERAL:
        case CHAR_LITERAL:
        case STRING_LITERAL:
        case NULL_LITERAL:
            return true;
        default:
            return false;
        }
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

    // The following check is necessary because Oracle has decided that
    //   x instanceof Class<? extends Object>
    // will remain illegal even though it means the same thing as
    //   x instanceof Class<?>.
    private boolean isBoundableWildcard(List<Tree> actualPath, int i) {
        if (i <= 0) { return false; }
        Tree actualNode = actualPath.get(i);
        if (isWildcard(actualNode.getKind())) {
          // TODO: refactor GenericArrayLoc to use same code?
          Tree ancestor = actualPath.get(i-1);
          if (ancestor.getKind() == Tree.Kind.INSTANCE_OF) {
            System.err.println("WARNING: wildcard bounds "
                + "not allowed in 'instanceof' expression; "
                + "skipping insertion");
            return false;
          } else if (i > 1 && ancestor.getKind() ==
              Tree.Kind.PARAMETERIZED_TYPE) {
            ancestor = actualPath.get(i-2);
            if (ancestor.getKind() == Tree.Kind.ARRAY_TYPE) {
              System.err.println("WARNING: wildcard bounds "
                  + "not allowed in generic array type; "
                  + "skipping insertion");
              return false;
            }
          }
          return true;
        }
        return false;
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
