package annotations.io;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.IntersectionTypeTree;
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
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;

import annotations.util.JVMNames;

/**
 * Cache of {@code ASTPath} data for the nodes of a compilation unit tree.
 *
 * @author dbro
 */
public class ASTIndex extends AbstractMap<Tree, ASTIndex.ASTRecord> {
  /**
   * Structure bundling an {@link ASTPath} with information about its
   *  starting point. Necessary because the {@link ASTPath} structure
   *  does not include the declaration from which it originates.
   *
   * @author dbro
   */
  public static class ASTRecord {
    /**
     * Name of the enclosing class declaration.
     */
    public final String className;

    /**
     * Name of the enclosing method declaration, or null if there is none.
     */
    public final String methodName;

    /**
     * Name of the enclosing variable declaration, or null if there is none.
     */
    public final String varName;

    /**
     * Kind of immediately enclosing declaration: METHOD, VARIABLE, or a
     *  class type (CLASS, INTERFACE, ENUM, or ANNOTATION_TYPE).
     */
    public final ASTPath astPath;

    // TODO: stubs to preserve checker-framework-inference compilation; REMOVE
    public final String otherName = null;
    public final Tree.Kind declKind = null;

    ASTRecord(String className, String methodName, String varName,
        ASTPath astPath) {
      this.className = className;
      this.methodName = methodName;
      this.varName = varName;
      this.astPath = astPath;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof ASTRecord && equals((ASTRecord) o);
    }

    public boolean equals(ASTRecord astRecord) {
      return className.equals(astRecord.className)
          && methodName == null ? astRecord.methodName == null
              : methodName.equals(astRecord.methodName)
          && varName == null ? astRecord.varName == null
              : varName.equals(astRecord.varName)
          && astPath.equals(astRecord.astPath);
    }

    // top-down test for confirmation
    /**
     * Indicates whether this record identifies the given {@link TreePath}.
     */
    public boolean matches(TreePath treePath) {
      String clazz = null;
      String meth = null;
      String var = null;
      boolean matchVars = false;  // members only!
      Deque<Tree> stack = new ArrayDeque<Tree>();
      for (Tree tree : treePath) { stack.push(tree); }
      while (!stack.isEmpty()) {
        Tree tree = stack.pop();
        switch (tree.getKind()) {
        case CLASS:
        case INTERFACE:
        case ENUM:
        case ANNOTATION_TYPE:
          clazz = ((ClassTree) tree).getSimpleName().toString();
          meth = null;
          var = null;
          matchVars = true;
          break;
        case METHOD:
          assert meth == null;
          meth = ((MethodTree) tree).getName().toString();
          matchVars = false;
          break;
        case VARIABLE:
          if (matchVars) {
            assert var == null;
            var = ((VariableTree) tree).getName().toString();
            matchVars = false;
          }
          break;
        default:
          matchVars = false;
          continue;
        }
      }
      return className.equals(clazz)
          && (methodName == null ? meth == null : methodName.equals(meth))
          && (varName == null ? var == null : varName.equals(var))
          && astPath.matches(treePath);
    }
  }

  private static final Map<CompilationUnitTree, Map<Tree, ASTRecord>>
      cache = new HashMap<CompilationUnitTree, Map<Tree, ASTRecord>>();

  private final CompilationUnitTree cut;
  private final Map<Tree, ASTPath.ASTEntry> astEntries;

  /**
   * Maps source trees in compilation unit to corresponding AST paths.
   * 
   * @param cut compilation unit to be indexed
   * @return map of trees in compilation unit to AST paths
   */
  public static Map<Tree, ASTRecord> indexOf(CompilationUnitTree cut) {
    Map<Tree, ASTRecord> index = cache.get(cut);
    if (index == null) {
      index = new ASTIndex(cut);
      cache.put(cut, index);
    }
    return index;
  }

  private static ASTRecord makeASTRecord(TreePath treePath,
      ASTPath astPath) {
    MethodTree method = null;
    VariableTree field = null;
    for (Tree node : treePath) {
      Tree.Kind kind = node.getKind();
      switch (kind) {
      case CLASS:
      case INTERFACE:
      case ENUM:
      case ANNOTATION_TYPE:
        return assemble(astPath, (ClassTree) node, method, field);
      case METHOD:
        method = (MethodTree) node;
        continue;
      case VARIABLE:
        field = (VariableTree) node;
        continue;
      default:
        method = null;
        field = null;
      }
    }
    return null;
  }

  private static ASTRecord assemble(ASTPath astPath,
      ClassTree classNode, MethodTree methodNode, VariableTree fieldNode) {
    String className = ((JCClassDecl) classNode).sym.flatname.toString();
    if (methodNode == null) {
      if (fieldNode != null) {
        return new ASTRecord(className, null,
            fieldNode.getName().toString(), astPath);
      }
      return new ASTRecord(className, null, null, astPath);
    }

    String methodName = JVMNames.getJVMMethodName(methodNode);
    if (fieldNode == null) {
      return new ASTRecord(className, methodName, null, astPath);
    }

    String fieldName = fieldNode.getName().toString();
    VariableTree recv = methodNode.getReceiverParameter();
    if (recv != null && recv.getName().toString().equals(fieldName)) {
      return new ASTRecord(className, methodName,
          Integer.toString(-1), astPath);
    }

    int i = 0;
    for (VariableTree param : methodNode.getParameters()) {
      if (param.getName().toString().equals(fieldName)) {
        return new ASTRecord(className, methodName, Integer.toString(i),
            astPath);
      }
      i++;
    }
    return null;
  }

  // The constructor walks the entire tree and creates an ASTEntry for
  // each node of a valid type (i.e., excluding declarations and
  // terminal nodes that cannot be annotated), so ASTPaths can be
  // assembled from these entries on demand.  Thus the traversal
  // happens only once, and the space requirement is kept relatively
  // low.
  private ASTIndex(CompilationUnitTree cut) {
    this.cut = cut;
    this.astEntries = new HashMap<Tree, ASTPath.ASTEntry>();
    if (cut == null) { return; }

    // The visitor implementation is slightly complicated by the
    // inclusion of information from both parent and child nodes in each
    // ASTEntry.  The pattern for most node types is to call save() and
    // saveAll() as needed to handle the node's descendants and finally
    // to invoke defaultAction() to save the entry for the current node.
    // (If the JVM could take advantage of tail recursion, it would be
    // better to save the current node's entry first, at a small cost to
    // the clarity of the code.)
    cut.accept(new SimpleTreeVisitor<Void, ASTPath.ASTEntry>() {
      private void save(Tree node, Kind kind, String sel) {
        if (node != null) {
          node.accept(this, new ASTPath.ASTEntry(kind, sel));
        }
      }

      private void save(Tree node, Kind kind, String sel, int arg) {
        if (node != null) {
          node.accept(this, new ASTPath.ASTEntry(kind, sel, arg));
        }
      }

      private void saveAll(Iterable<? extends Tree> nodes,
          Kind kind, String sel) {
        int i = 0;
        if (nodes != null) {
          for (Tree node : nodes) {
            save(node, kind, sel, i++);
          }
        }
      }

      @Override
      public Void defaultAction(Tree node, ASTPath.ASTEntry entry) {
        if (entry != null) { astEntries.put(node, entry); }
        return null;
      }

      @Override
      public Void visitAnnotatedType(AnnotatedTypeTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        saveAll(node.getAnnotations(), kind, ASTPath.ANNOTATION);
        save(node.getUnderlyingType(), kind, ASTPath.UNDERLYING_TYPE);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitAnnotation(AnnotationTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getAnnotationType(), kind, ASTPath.TYPE);
        saveAll(node.getArguments(), kind, ASTPath.ARGUMENT);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitMethodInvocation(MethodInvocationTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        saveAll(node.getTypeArguments(), kind, ASTPath.TYPE_ARGUMENT);
        save(node.getMethodSelect(), kind, ASTPath.METHOD_SELECT);
        saveAll(node.getArguments(), kind, ASTPath.ARGUMENT);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitAssert(AssertTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getCondition(), kind, ASTPath.CONDITION);
        save(node.getDetail(), kind, ASTPath.DETAIL);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitAssignment(AssignmentTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getExpression(), kind, ASTPath.EXPRESSION);
        save(node.getVariable(), kind, ASTPath.VARIABLE);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitCompoundAssignment(CompoundAssignmentTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getExpression(), kind, ASTPath.EXPRESSION);
        save(node.getVariable(), kind, ASTPath.VARIABLE);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitBinary(BinaryTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getLeftOperand(), kind, ASTPath.LEFT_OPERAND);
        save(node.getRightOperand(), kind, ASTPath.RIGHT_OPERAND);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitBlock(BlockTree node, ASTPath.ASTEntry entry) {
        saveAll(node.getStatements(), node.getKind(), ASTPath.STATEMENT);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitCase(CaseTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getExpression(), kind, ASTPath.EXPRESSION);
        saveAll(node.getStatements(), kind, ASTPath.STATEMENT);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitCatch(CatchTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getBlock(), kind, ASTPath.BLOCK);
        save(node.getParameter(), kind, ASTPath.PARAMETER);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitClass(ClassTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getExtendsClause(), kind, ASTPath.BOUND, -1);
        saveAll(node.getImplementsClause(), kind, ASTPath.BOUND);
        saveAll(node.getTypeParameters(), kind, ASTPath.TYPE_PARAMETER);
        return visit(node.getMembers(), null);
      }

      @Override
      public Void visitConditionalExpression(ConditionalExpressionTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getCondition(), kind, ASTPath.CONDITION);
        save(node.getFalseExpression(), kind, ASTPath.FALSE_EXPRESSION);
        save(node.getTrueExpression(), kind, ASTPath.TRUE_EXPRESSION);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitDoWhileLoop(DoWhileLoopTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getCondition(), kind, ASTPath.CONDITION);
        save(node.getStatement(), kind, ASTPath.STATEMENT);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitExpressionStatement(ExpressionStatementTree node,
          ASTPath.ASTEntry entry) {
        save(node.getExpression(), node.getKind(), ASTPath.EXPRESSION);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitEnhancedForLoop(EnhancedForLoopTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getVariable(), kind, ASTPath.VARIABLE);
        save(node.getExpression(), kind, ASTPath.EXPRESSION);
        save(node.getStatement(), kind, ASTPath.STATEMENT);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitForLoop(ForLoopTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        saveAll(node.getInitializer(), kind, ASTPath.INITIALIZER);
        save(node.getCondition(), kind, ASTPath.CONDITION);
        save(node.getStatement(), kind, ASTPath.STATEMENT);
        saveAll(node.getUpdate(), kind, ASTPath.UPDATE);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitIf(IfTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getCondition(), kind, ASTPath.CONDITION);
        save(node.getThenStatement(), kind, ASTPath.THEN_STATEMENT);
        save(node.getElseStatement(), kind, ASTPath.ELSE_STATEMENT);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitArrayAccess(ArrayAccessTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getExpression(), kind, ASTPath.EXPRESSION);
        save(node.getIndex(), kind, ASTPath.INDEX);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitLabeledStatement(LabeledStatementTree node,
          ASTPath.ASTEntry entry) {
        save(node.getStatement(), node.getKind(), ASTPath.STATEMENT);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitMethod(MethodTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getReturnType(), kind, ASTPath.TYPE);
        saveAll(node.getTypeParameters(), kind, ASTPath.TYPE_PARAMETER);
        save(node.getReceiverParameter(), kind, ASTPath.PARAMETER);
        saveAll(node.getParameters(), kind, ASTPath.PARAMETER);
        save(node.getBody(), kind, ASTPath.BODY);
        return null;
      }

      @Override
      public Void visitNewArray(NewArrayTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getType(), kind, ASTPath.TYPE);
        saveAll(node.getDimensions(), kind, ASTPath.DIMENSION);
        saveAll(node.getInitializers(), kind, ASTPath.INITIALIZER);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitNewClass(NewClassTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getEnclosingExpression(), kind,
            ASTPath.ENCLOSING_EXPRESSION);
        saveAll(node.getTypeArguments(), kind, ASTPath.TYPE_ARGUMENT);
        save(node.getIdentifier(), kind, ASTPath.IDENTIFIER);
        saveAll(node.getArguments(), kind, ASTPath.ARGUMENT);
        save(node.getClassBody(), kind, ASTPath.CLASS_BODY);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitLambdaExpression(LambdaExpressionTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        saveAll(node.getParameters(), kind, ASTPath.PARAMETER);
        save(node.getBody(), kind, ASTPath.BODY);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitParenthesized(ParenthesizedTree node,
          ASTPath.ASTEntry entry) {
        save(node.getExpression(), node.getKind(), ASTPath.EXPRESSION);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitReturn(ReturnTree node, ASTPath.ASTEntry entry) {
        save(node.getExpression(), node.getKind(), ASTPath.EXPRESSION);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitMemberSelect(MemberSelectTree node,
          ASTPath.ASTEntry entry) {
        save(node.getExpression(), node.getKind(), ASTPath.EXPRESSION);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitMemberReference(MemberReferenceTree node,
          ASTPath.ASTEntry entry) {
        save(node.getQualifierExpression(), node.getKind(),
            ASTPath.QUALIFIER_EXPRESSION);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitSwitch(SwitchTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getExpression(), kind, ASTPath.EXPRESSION);
        saveAll(node.getCases(), kind, ASTPath.CASE);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitSynchronized(SynchronizedTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getExpression(), kind, ASTPath.EXPRESSION);
        save(node.getBlock(), kind, ASTPath.BLOCK);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitThrow(ThrowTree node, ASTPath.ASTEntry entry) {
        save(node.getExpression(), node.getKind(), ASTPath.EXPRESSION);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitCompilationUnit(CompilationUnitTree node,
          ASTPath.ASTEntry entry) {
        for (Tree tree : node.getTypeDecls()) {
          tree.accept(this, null);
        }
        return null;
      }

      @Override
      public Void visitTry(TryTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        saveAll(node.getResources(), kind, ASTPath.RESOURCE);
        save(node.getBlock(), kind, ASTPath.BLOCK);
        saveAll(node.getCatches(), kind, ASTPath.CATCH);
        save(node.getFinallyBlock(), kind, ASTPath.FINALLY_BLOCK);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitParameterizedType(ParameterizedTypeTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getType(), kind, ASTPath.TYPE);
        saveAll(node.getTypeArguments(), kind, ASTPath.TYPE_ARGUMENT);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitUnionType(UnionTypeTree node, ASTPath.ASTEntry entry) {
        saveAll(node.getTypeAlternatives(), node.getKind(),
            ASTPath.TYPE_ALTERNATIVE);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitIntersectionType(IntersectionTypeTree node,
          ASTPath.ASTEntry entry) {
        saveAll(node.getBounds(), node.getKind(), ASTPath.BOUND);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitArrayType(ArrayTypeTree node, ASTPath.ASTEntry entry) {
        save(node.getType(), node.getKind(), ASTPath.TYPE);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitTypeCast(TypeCastTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getType(), kind, ASTPath.TYPE);
        save(node.getExpression(), kind, ASTPath.EXPRESSION);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitTypeParameter(TypeParameterTree node,
          ASTPath.ASTEntry entry) {
        saveAll(node.getBounds(), node.getKind(), ASTPath.BOUND);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitInstanceOf(InstanceOfTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getExpression(), kind, ASTPath.EXPRESSION);
        save(node.getType(), kind, ASTPath.TYPE);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitUnary(UnaryTree node, ASTPath.ASTEntry entry) {
        save(node.getExpression(), node.getKind(), ASTPath.EXPRESSION);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitVariable(VariableTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getType(), kind, ASTPath.TYPE);
        save(node.getInitializer(), kind, ASTPath.INITIALIZER);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitWhileLoop(WhileLoopTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getCondition(), kind, ASTPath.CONDITION);
        save(node.getStatement(), kind, ASTPath.STATEMENT);
        return defaultAction(node, entry);
      }

      @Override
      public Void visitWildcard(WildcardTree node, ASTPath.ASTEntry entry) {
        save(node.getBound(), node.getKind(), ASTPath.BOUND);
        return defaultAction(node, entry);
      }
    }, null);
  }

  public ASTRecord getPath(CompilationUnitTree cut, Tree node) {
    return indexOf(cut).get(node);
  }

  // Map instance methods

  @Override
  public int size() {
    return astEntries.size();
  }

  @Override
  public boolean isEmpty() {
    return astEntries.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return astEntries.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return values().contains(value);
  }

  @Override
  public ASTRecord get(Object key) {
    // build ASTPath on demand from cached ASTEntries
    Tree node = (Tree) key;
    if (!ASTPath.isHandled(node.getKind())) { return null; }
    TreePath treePath = TreePath.getPath(cut, node);
    ASTPath astPath = new ASTPath();
    Deque<Tree> deque = new ArrayDeque<Tree>();
    Deque<Tree> backup = null;
loop:
    for (Tree tree : treePath) {
      Kind kind = tree.getKind();
      switch (kind) {
      case VARIABLE:
        backup = deque;  // keep going iff local; not yet known, so save state
        deque.push(tree);
        break;
      case METHOD:
        deque.push(tree);
        // fall through
      case ANNOTATION:
      case CLASS:
      case ENUM:
      case INTERFACE:
        if (backup != null) { deque = backup; }
        break loop;
      case BREAK:
      case COMPILATION_UNIT:
      case CONTINUE:
      case IMPORT:
      case LAMBDA_EXPRESSION: // TODO
      case MODIFIERS:
        break;
      case BLOCK:
        backup = null;  // any previously seen var was local
        // fall through
      default:
        deque.push(tree);
      }
    }
    while (!deque.isEmpty()) {
      ASTPath.ASTEntry entry = astEntries.get(deque.pop());
      if (entry != null) { astPath.add(entry); }
    }
    return makeASTRecord(treePath, astPath);
  }

  @Override
  public Set<Tree> keySet() {
    return astEntries.keySet();
  }

  @Override
  public Collection<ASTRecord> values() {
    return new AbstractCollection<ASTRecord>() {
      @Override
      public int size() { return astEntries.size(); }

      @Override
      public Iterator<ASTRecord> iterator() {
        return new Iterator<ASTRecord>() {
          Iterator<Tree> iter = astEntries.keySet().iterator();

          @Override
          public boolean hasNext() {
            return iter.hasNext();
          }

          @Override
          public ASTRecord next() {
            return ASTIndex.this.get(iter.next());
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  @Override
  public Set<Map.Entry<Tree, ASTRecord>> entrySet() {
    return new AbstractSet<Map.Entry<Tree, ASTRecord>>() {
      @Override
      public int size() { return astEntries.size(); }

      @Override
      public Iterator<Map.Entry<Tree, ASTRecord>> iterator() {
        return new Iterator<Map.Entry<Tree, ASTRecord>>() {
          Iterator<Tree> iter = astEntries.keySet().iterator();

          @Override
          public boolean hasNext() {
            return iter.hasNext();
          }

          @Override
          public Map.Entry<Tree, ASTRecord> next() {
            Tree node = iter.next();
            return new AbstractMap.SimpleImmutableEntry<Tree, ASTRecord>(node,
                ASTIndex.this.get(node));
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}
