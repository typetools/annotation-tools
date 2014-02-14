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

/**
 * @author dbro
 *
 */
public class ASTIndex extends AbstractMap<Tree, ASTPath> {
  private static final Map<CompilationUnitTree, Map<Tree, ASTPath>>
      cache = new HashMap<CompilationUnitTree, Map<Tree, ASTPath>>();

  private final CompilationUnitTree cut;
  private final Map<Tree, ASTPath.ASTEntry> astEntries;

  public static ASTPath.ASTEntry makeEntry(Tree.Kind kind, String selector) {
    return makeEntry(kind, selector, -1);
  }

  public static ASTPath.ASTEntry makeEntry(Tree.Kind kind,
      String selector, int arg) {
    return new ASTPath.ASTEntry(kind, selector, arg);
  }

  public static ASTPath makePath() {
    return new ASTPath();
  }

  public static ASTPath makePath(CompilationUnitTree cut, Tree node) {
    return getIndex(cut).get(node);
  }

  /**
   * Maps source trees in compilation unit to corresponding AST paths.
   * 
   * @param cut compilation unit to be indexed
   * @return map of trees in compilation unit to AST paths
   */
  public static Map<Tree, ASTPath> getIndex(CompilationUnitTree cut) {
    Map<Tree, ASTPath> index = cache.get(cut);
    if (index == null) {
      index = new ASTIndex(cut);
      cache.put(cut, index);
    }
    return index;
  }

  public ASTIndex(CompilationUnitTree cut) {
    this.cut = cut;
    this.astEntries = new HashMap<Tree, ASTPath.ASTEntry>();
    if (cut == null) { return; }

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
            save(node, kind, sel, ++i);
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

      //@Override
      //public Void visitBreak(BreakTree node, ASTPath.ASTEntry entry) {
      //  return defaultAction(node, entry);
      //}

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
        save(node.getExtendsClause(), kind, ASTPath.BOUND);
        saveAll(node.getImplementsClause(), kind, ASTPath.BOUND);
        saveAll(node.getTypeParameters(), kind, ASTPath.TYPE_ARGUMENT);
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

      //@Override
      //public Void visitContinue(ContinueTree node, ASTPath.ASTEntry entry) {
      //  return defaultAction(node, entry);
      //}

      @Override
      public Void visitDoWhileLoop(DoWhileLoopTree node,
          ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getCondition(), kind, ASTPath.CONDITION);
        save(node.getStatement(), kind, ASTPath.STATEMENT);
        return defaultAction(node, entry);
      }

      //@Override
      //public Void visitErroneous(ErroneousTree node, ASTPath.ASTEntry entry) {
      //  return defaultAction(node, entry);
      //}

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

      //@Override
      //public Void visitIdentifier(IdentifierTree node,
      //    ASTPath.ASTEntry entry) {
      //  return defaultAction(node, entry);
      //}

      @Override
      public Void visitIf(IfTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getCondition(), kind, ASTPath.CONDITION);
        save(node.getThenStatement(), kind, ASTPath.THEN_STATEMENT);
        save(node.getElseStatement(), kind, ASTPath.ELSE_STATEMENT);
        return defaultAction(node, entry);
      }

      //@Override
      //public Void visitImport(ImportTree node, ASTPath.ASTEntry entry) {
      //  return defaultAction(node, entry);
      //}

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

      //@Override
      //public Void visitLiteral(LiteralTree node, ASTPath.ASTEntry entry) {
      //  return defaultAction(node, entry);
      //}

      @Override
      public Void visitMethod(MethodTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getReturnType(), kind, ASTPath.TYPE);
        saveAll(node.getTypeParameters(), kind, ASTPath.TYPE_ARGUMENT);
        save(node.getReceiverParameter(), kind, ASTPath.PARAMETER);
        saveAll(node.getParameters(), kind, ASTPath.PARAMETER);
        save(node.getBody(), kind, ASTPath.BODY);
        return null;
      }

      //@Override
      //public Void visitModifiers(ModifiersTree node, ASTPath.ASTEntry entry) {
      //  return defaultAction(node, entry);
      //}

      @Override
      public Void visitNewArray(NewArrayTree node, ASTPath.ASTEntry entry) {
        Kind kind = node.getKind();
        save(node.getType(), kind, ASTPath.TYPE);
        saveAll(node.getDimensions(), kind, ASTPath.DIMENSION);
        saveAll(node.getInitializers(), kind, ASTPath.INITIALIZER);
        return defaultAction(node, entry);
      }

      /*
        Tree getType();
        List<? extends ExpressionTree> getDimensions();
        List<? extends ExpressionTree> getInitializers();
        List<? extends AnnotationTree> getAnnotations();
        List<? extends List<? extends AnnotationTree>> getDimAnnotations();
       */

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

      //@Override
      //public Void visitEmptyStatement(EmptyStatementTree node,
      //    ASTPath.ASTEntry entry) {
      //  return defaultAction(node, entry);
      //}

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

      //@Override
      //public Void visitPrimitiveType(PrimitiveTypeTree node,
      //    ASTPath.ASTEntry entry) {
      //  return defaultAction(node, entry);
      //}

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

      //@Override
      //public Void visitOther(Tree node, ASTPath.ASTEntry entry) {
      //  return defaultAction(node, entry);
      //}
    }, null);
  }

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
  public ASTPath get(Object key) {
    Tree node = (Tree) key;
    if (!ASTPath.isHandled(node.getKind())) { return null; }
    ASTPath path = makePath();
    Deque<Tree> deque = new ArrayDeque<Tree>();
    for (Tree tree : TreePath.getPath(cut, node)) {
      if (ASTPath.isHandled(tree.getKind())) { deque.push(tree); }
    }
    while (!deque.isEmpty()) {
      ASTPath.ASTEntry entry = astEntries.get(deque.pop());
      if (entry != null) { path.add(entry); }
    }
    return path;
  }

  @Override
  public Set<Tree> keySet() {
    return astEntries.keySet();
  }

  @Override
  public Collection<ASTPath> values() {
    return new AbstractCollection<ASTPath>() {
      @Override
      public int size() { return astEntries.size(); }

      @Override
      public Iterator<ASTPath> iterator() {
        return new Iterator<ASTPath>() {
          Iterator<Tree> iter = astEntries.keySet().iterator();

          @Override
          public boolean hasNext() {
            return iter.hasNext();
          }

          @Override
          public ASTPath next() {
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
  public Set<Map.Entry<Tree, ASTPath>> entrySet() {
    return new AbstractSet<Map.Entry<Tree, ASTPath>>() {
      @Override
      public int size() { return astEntries.size(); }

      @Override
      public Iterator<Map.Entry<Tree, ASTPath>> iterator() {
        return new Iterator<Map.Entry<Tree, ASTPath>>() {
          Iterator<Tree> iter = astEntries.keySet().iterator();

          @Override
          public boolean hasNext() {
            return iter.hasNext();
          }

          @Override
          public Map.Entry<Tree, ASTPath> next() {
            Tree node = iter.next();
            return new AbstractMap.SimpleImmutableEntry<Tree, ASTPath>(node,
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

/*
abstract class DynamicValueMap<K, I, V> extends AbstractMap<K, V> {
  private Map<K, I> backingMap;

  public abstract V valueFor(I backingValue);

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new AbstractSet<Map.Entry<K, V>>() {
      @Override
      public int size() { return backingMap.size(); }

      @Override
      public Iterator<Map.Entry<K, V>> iterator() {
        return new Iterator<Map.Entry<K, V>>() {
          Iterator<K> iter = backingMap.keySet().iterator();

          @Override
          public boolean hasNext() {
            return iter.hasNext();
          }

          @Override
          public Map.Entry<K, V> next() {
            K key = iter.next();
            I ival = backingMap.get(key);
            return new AbstractMap.SimpleImmutableEntry<K, V>(key,
                valueFor(ival));
          }
        };
      }
    };
  }
}
*/
