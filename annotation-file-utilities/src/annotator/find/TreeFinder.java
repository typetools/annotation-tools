package annotator.find;

import annotator.Main;

import java.io.*;
import java.util.*;

import javax.tools.*;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;

/**
 * A {@code TreeScanner} that is able to locate program elements in an
 * AST based on {@code Criteria}. It is used to scan a tree and return a
 * mapping of source positions (as character offsets) to insertion text.
 */
public class TreeFinder extends TreeScanner<Void, List<Insertion>> {

  static public boolean debug = false;

  private static void debug(String message) {
    if (debug)
      System.out.println(message);
  }

  /**
   * Used to determine the insertion position for various elements. For
   * instance, variable annotations should be placed before the variable's
   * <i>type</i> rather than its name.
   */
  private static class TypePositionFinder extends TreeScanner<Integer, Void> {

    private CompilationUnitTree tree;

    public TypePositionFinder(CompilationUnitTree tree) {
      super();
      this.tree = tree;
    }

    /** et should be an expression for a type */
    private JCTree leftmostIdentifier(JCTree t) {
      while (true) {
        switch (t.getKind()) {
        case IDENTIFIER:
        case PRIMITIVE_TYPE:
          return t;
        case MEMBER_SELECT:
          t = ((JCFieldAccess) t).getExpression();
          break;
        default:
          throw new RuntimeException(String.format("Unrecognized type (kind=%s, class=%s): %s", t.getKind(), t.getClass(), t));
        }
      }
    }

    @Override
      public Integer visitVariable(VariableTree node, Void p) {
      JCTree jt = ((JCVariableDecl) node).getType();
      if (jt instanceof JCTypeApply) {
        JCTypeApply vt = (JCTypeApply) jt;
        return vt.clazz.pos;
      }
      JCExpression type = (JCExpression) (((JCVariableDecl)node).getType());
      return leftmostIdentifier(type).pos;
    }

    // When a method is visited, it is visited for the receiver, not the return value.
    @Override
    public Integer visitMethod(MethodTree node, Void p) {
      super.visitMethod(node, p);
      // System.out.println("node: " + node);
      // System.out.println("return: " + node.getReturnType());


      int afterParamList;

      // TODO: figure out how to better place reciever annotation!!!
      //          List<? extends VariableTree> vars = node.getParameters();
      //
      //          VariableTree vt = vars.get(0);
      //          vt.getName().length();
      JCMethodDecl jcnode = (JCMethodDecl) node;
      List<JCExpression> throwsExpressions = jcnode.thrown;
      if (throwsExpressions.isEmpty()) {
        int rightBeforeBlock = jcnode.getBody().pos;
        afterParamList = rightBeforeBlock;
      } else {
        IdentifierTree it = (IdentifierTree) leftmostIdentifier(throwsExpressions.get(0));
        afterParamList = this.visitIdentifier(it, p);
        afterParamList -= 7; // for the 'throws' clause

        JavaFileObject jfo = tree.getSourceFile();
        try {
          String s = String.valueOf(jfo.getCharContent(true));
          for (int i = afterParamList; i >= 0; i--) {
            if (s.charAt(i) == ')') {
              afterParamList = i + 1;
              break;
            }
          }
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }

      // TODO:
      //debugging: System.out.println("result: " + afterParamList);
      return afterParamList;


//       // old implementations scans characters in the source (which is error-prone).
//       if (i.getCriteria().isOnReceiver()) {
//         CharSequence s = null;
//         try {
//           s = path.getCompilationUnit().getSourceFile().getCharContent(true);
//         } catch(Exception e) {
//           throw new RuntimeException("problem with receiver: " + e.getMessage(), e);
//         }
//
//         // For a receiver, the match is on the method name.
//         // Therefore, scan forward until after the first ')' encountered
//         for (int j = pos; j < s.length(); j++) {
//           if (s.charAt(j) == ')') {
//             pos = j + 1;
//             break;
//           }
//         }
//       }


//      return ((JCMethodDecl)node).getReturnType().pos;
    }

    private int getFirstBracketAfter(int i) {
      try {
        CharSequence s = tree.getSourceFile().getCharContent(true);
        // return first [, plus 1
        for (int j=i; j < s.length(); j++) {
          if (s.charAt(j) == '[') {
            return j;
          }
        }
      } catch(Exception e) {
        throw new RuntimeException(e);
      }

      return i;
    }

    @Override
      public Integer visitIdentifier(IdentifierTree node, Void p) {
      // for arrays, need to indent inside array, not right before type
      TreePath path = TreePath.getPath(tree, node);
      Tree parent = path.getParentPath().getLeaf();
      Integer i = null;
      if (parent instanceof ArrayTypeTree) {
        debug("TypePositionFinder.visitIdentifier: recognized array");
        ArrayTypeTree att = (ArrayTypeTree) parent;
        JCIdent jcid = (JCIdent) node;
        i = jcid.pos;
      } else {
        i = ((JCIdent) node).pos;
      }

      debug("visitId: " + i);
      return i;
    }

    @Override
      public Integer visitPrimitiveType(PrimitiveTypeTree node, Void p) {
      // want exact same logistics as with visitIdentifier
      TreePath path = TreePath.getPath(tree, node);
      Tree parent = path.getParentPath().getLeaf();
      Integer i = null;
      if (parent instanceof ArrayTypeTree) {
        ArrayTypeTree att = (ArrayTypeTree) parent;
        JCTree jcid = (JCTree) node;
        i = jcid.pos;
      } else {
        i = ((JCTree) node).pos;
      }
      //          JCPrimitiveTypeTree pt = (JCPrimitiveTypeTree) node;
      JCTree jt = (JCTree) node;
      return i;
    }

    @Override
      public Integer visitParameterizedType(ParameterizedTypeTree node, Void p) {
      TreePath path = TreePath.getPath(tree, node);
      Tree parent = path.getParentPath().getLeaf();
      Integer i = null;
      if (parent instanceof ArrayTypeTree) {
        // want to annotate the first level of this array
        ArrayTypeTree att = (ArrayTypeTree) parent;
        Tree baseType = att.getType();
        i = leftmostIdentifier(((JCTypeApply) node).getType()).pos;
        debug("BASE TYPE: " + baseType.toString());
      } else {
        i = leftmostIdentifier(((JCTypeApply)node).getType()).pos;
      }
      return i;
    }

//     @Override
//       public Integer visitBlock(BlockTree node, Void p) {
//       //debugging: System.out.println("visitBlock");
//       int rightBeforeBlock = ((JCBlock) node).pos;
//       // Will be adjusted if a throws statement exists.
//       int afterParamList = -1;
//       TreePath path = TreePath.getPath(tree, node);
//       Tree methodTree = path.getParentPath().getLeaf();
//       if (!(methodTree.getKind() == Tree.Kind.METHOD)) {
//         throw new RuntimeException("BlockTree has non-method parent");
//       }
//       MethodTree mt = (MethodTree) methodTree;
//
//       // TODO: figure out how to better place reciever annotation!!!
//       //          List<? extends VariableTree> vars = mt.getParameters();
//       //
//       //          VariableTree vt = vars.get(0);
//       //          vt.getName().length();
//       List<? extends ExpressionTree> throwsExpressions = mt.getThrows();
//       if (throwsExpressions.isEmpty()) {
//         afterParamList = rightBeforeBlock;
//       } else {
//         ExpressionTree et = throwsExpressions.get(0);
//         while (afterParamList == -1) {
//           switch (et.getKind()) {
//           case IDENTIFIER:
//             afterParamList = this.visitIdentifier((IdentifierTree) et, p);
//             afterParamList -= 7; // for the 'throws' clause
//
//             JavaFileObject jfo = tree.getSourceFile();
//             try {
//               String s = String.valueOf(jfo.getCharContent(true));
//               for (int i = afterParamList; i >= 0; i--) {
//                 if (s.charAt(i) == ')') {
//                   afterParamList = i + 1;
//                   break;
//                 }
//               }
//             } catch(IOException e) {
//               throw new RuntimeException(e);
//             }
//             break;
//           case MEMBER_SELECT:
//             et = ((MemberSelectTree) et).getExpression();
//             break;
//           default:
//             throw new RuntimeException("Unrecognized throws (kind=" + et.getKind() + "): " + et);
//           }
//         }
//       }
//
//       // TODO:
//       //debugging: System.out.println("result: " + afterParamList);
//       return afterParamList;
//     }


    @Override
      public Integer visitArrayType(ArrayTypeTree node, Void p) {
      JCArrayTypeTree att = (JCArrayTypeTree) node;
      return att.getPreferredPosition();
    }

    @Override
      public Integer visitCompilationUnit(CompilationUnitTree node, Void p) {
      JCCompilationUnit cu = (JCCompilationUnit) node;
      JCTree.JCExpression pid = cu.pid;
      while (pid instanceof JCTree.JCFieldAccess) {
        pid = ((JCTree.JCFieldAccess) pid).selected;
      }
      JCTree.JCIdent firstComponent = (JCTree.JCIdent) pid;
      int result = firstComponent.getPreferredPosition();

      // Now back up over the word "package" and the preceding newline
      JavaFileObject jfo = tree.getSourceFile();
      String fileContent;
      try {
        fileContent = String.valueOf(jfo.getCharContent(true));
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
      while (java.lang.Character.isWhitespace(fileContent.charAt(result-1))) {
        result--;
      }
      result -= 7;
      String packageString = fileContent.substring(result, result+7);
      assert "package".equals(packageString) : "expected 'package', got: " + packageString;
      assert result == 0 || java.lang.Character.isWhitespace(fileContent.charAt(result-1));
      return result;
    }

    @Override
    public Integer visitClass(ClassTree node, Void p) {
      JCClassDecl cd = (JCClassDecl) node;
      if (cd.mods != null) {
        return cd.mods.getPreferredPosition();
      } else {
        return cd.getPreferredPosition();
      }
    }

  }

  /**
   * A comparator for sorting integers in reverse
   */
  private static class ReverseIntegerComparator implements Comparator<Integer> {
    public int compare(Integer o1, Integer o2) {
      return o1.compareTo(o2) * -1;
    }
  }

  private Map<Tree, TreePath> paths;
  private TypePositionFinder tpf;
  private CompilationUnitTree tree;
  private SortedMap<Integer, String> positions;
  private Set<Insertion> satisfied;

  /**
   * Creates a {@code TreeFinder} from a source tree.
   *
   * @param tree the source tree to search
   */
  public TreeFinder(CompilationUnitTree tree) {
    this.tree = tree;
    this.positions = new TreeMap<Integer, String>(new ReverseIntegerComparator());
    this.tpf = new TypePositionFinder(tree);
    this.paths = new HashMap<Tree, TreePath>();
    this.satisfied = new HashSet<Insertion>();
  }

  boolean handled(Tree node) {
    return (node instanceof CompilationUnitTree
            || node instanceof ClassTree
            || node instanceof MethodTree
            || node instanceof VariableTree
            || node instanceof IdentifierTree
            || node instanceof ParameterizedTypeTree
            || node instanceof BlockTree
            || node instanceof ArrayTypeTree
            || node instanceof PrimitiveTypeTree);
  }

  /**
   * Scans this tree, using the list of insertions to generate the source
   * position to insertion text mapping.
   */
  @Override
    public Void scan(Tree node, List<Insertion> p) {
    if (node == null) {
      return null;
    }

    if (! handled(node)) {
      // nothing to do
      return super.scan(node, p);
    }


    TreePath path;
    if (paths.containsKey(tree))
      path = paths.get(tree);
    else
      path = TreePath.getPath(tree, node);
    assert path == null || path.getLeaf() == node :
      String.format("Mismatch: '%s' '%s' '%s' '%s'%n", path, tree, paths.containsKey(tree), node);

    // To avoid annotating existing annotations right before
    // the element you wish to annotate, skip anything inside of
    // an annotation.
    if (path != null) {
      Tree parent = path.getParentPath().getLeaf();
      if (parent.getKind() == Tree.Kind.ANNOTATION) {
        return super.scan(node, p);
      }
    }

    for (Insertion i : p) {
      if (satisfied.contains(i))
        continue;

      if (debug) {
        debug("Considering insertion at tree:");
        debug("  " + i);
        debug("  " + Main.firstLine(node.toString()));
      }

      if (!i.getCriteria().isSatisfiedBy(path, node)) {
        debug("  ... not satisfied");
        continue;
      }

      // If this is a method, then it might have been selected because of
      // the receiver, or because of the return value.  Distinguish those.
      // One way would be to set a global variable here.  Another would be
      // to look for a particular different node.  I will do the latter.
      Integer pos;

      if ((node instanceof MethodTree) && ! i.getCriteria().isOnReceiver()) {
        // we must be looking for the return type
        pos = tpf.scan(((MethodTree)node).getReturnType(), null);
        assert handled(node);
        // System.out.println("return type node: " + ((JCMethodDecl)node).getReturnType().getClass());
      } else {
        pos = tpf.scan(node, null);
        assert pos != null;
      }
      assert pos >= 0 : pos;

      debug("  ... satisfied! at " + pos + " for node of type " + node.getClass() + ": " + Main.treeToString(node));

      if (pos != null && !positions.containsKey(pos)) {
        positions.put(pos, i.getText());
      }

      satisfied.add(i);
    }

    return super.scan(node, p);
  }

  /**
   * Scans the given tree with the given insertion list and returns the
   * source position to insertion text mapping.  The positions are sorted
   * in decreasing order of index, so that inserting one doesn't throw
   * off the index for a subsequent one.
   *
   * <p>
   * <i>N.B.:</i> This method calls {@code scan()} internally.
   * </p>
   *
   * @param node the tree to scan
   * @param p the list of insertion criteria
   * @return the source position to insertion text mapping
   */
  public Map<Integer, String> getPositions(Tree node, List<Insertion> p) {
    this.scan(node, p);
    // This needs to be optional, because there may be many extra
    // annotations in a .jaif file.
    if (debug) {
      // Output every insertion that was not given a position:
      for (Insertion i : p) {
        if (!satisfied.contains(i) ) { // TODO: && options.hasOption("x")) {
          System.err.println("Unable to insert: " + i);
        }
      }
    }
    return Collections.unmodifiableMap(positions);
  }
}
