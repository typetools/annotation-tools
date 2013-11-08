package annotator.find;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.NullType;

import plume.Pair;
import type.DeclaredType;
import type.Type;
import annotator.Main;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
//import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.AnnotatedType;
import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;
import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntryKind;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotatedType;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Position;

/**
 * A {@code TreeScanner} that is able to locate program elements in an
 * AST based on {@code Criteria}. {@link #getPositions(JCCompilationUnit,List)}
 * scans a tree and returns a
 * mapping of source positions (as character offsets) to insertion text.
 */
public class TreeFinder extends TreeScanner<Void, List<Insertion>> {

  public static boolean debug = false;

  private static void debug(String message) {
    if (debug)
      System.out.println(message);
  }

  private static void debug(String message, Object... args) {
    if (debug) {
      System.out.printf(message, args);
      if (! message.endsWith("%n")) {
        System.out.println();
      }
    }
  }

  /**
   * String representation of regular expression matching a comment in
   * Java code.  The part before {@code |} matches a single-line
   * comment, and the part after matches a multi-line comment, which
   * breaks down as follows (adapted from
   * <a href="http://perldoc.perl.org/perlfaq6.html#How-do-I-use-a-regular-expression-to-strip-C-style-comments-from-a-file%3f">Perl FAQ</a>):
   * <pre>
   *          /\*         ##  Start of comment
   *          [^*]*\*+    ##  Non-* followed by 1-or-more *s
   *          (
   *              [^/*][^*]*\*+
   *          )*          ##  0 or more things which don't start with /
   *                      ##    but do end with '*'
   *          /           ##  End of comment
   * </pre>
   * Note: Care must be taken to avoid false comment matches starting
   * inside a string literal.  Ensuring that the code segment being
   * matched starts at an AST node boundary is sufficient to prevent
   * this complication.
   */
  private final static String comment =
      "//.*$|/\\*[^*]*\\*+(?:[^*/][^*]*\\*+)*/";

  /**
   * Regular expression matching a character or string literal.
   */
  private final static String literal =
      "'(?:(?:\\\\(?:'|[^']*))|[^\\\\'])'|\"(?:\\\\.|[^\\\\\"])*\"";

  /**
   * Regular expression matching a sequence consisting only of
   * whitespace and comments.
   * 
   * @see #comment
   */
  private final static String whitespace = "(?:\\s|" + comment + ")*";

  /**
   * Regular expression matching a non-commented instance of {@code /}
   * that is not part of a comment-starting delimiter.
   */
  private final static String nonDelimSlash = "/(?=[^*/])";

  /**
   * Regular expression matching a single character, a comment including
   * delimiter(s), or a character or string literal.
   */
  private final static String unit =
      "[^/'\"]|" + nonDelimSlash + "|" + literal + "|" + comment;

  /**
   * Returns regular expression matching "anything but" {@code c}: a
   * single comment, character or string literal, or non-{@code c}
   * character.
   */
  private final static String otherThan(char c) {
    String cEscaped;

    // escape if necessary for use in character class
    switch (c) {
    case '/':
    case '"':
    case '\'':
      cEscaped = ""; break;  // already present in class defn
    case '\\':
    case '[':
    case ']':
      cEscaped = "\\" + c; break;  // escape!
    default:
      cEscaped = "" + c;
    }

    return "[^/'" + cEscaped + "\"]|" + "|" + literal + "|" + comment
        + (c == '/' ? "" : nonDelimSlash);
  }

  // If this code location is not an array type, return null.  Otherwise,
  // starting at an array type, walk up the AST as long as still an array,
  // and stop at the largest containing array (with nothing but arrays in
  // between).
  public static TreePath largestContainingArray(TreePath p) {
    if (p.getLeaf().getKind() != Tree.Kind.ARRAY_TYPE) {
      return null;
    }
    while (p.getParentPath().getLeaf().getKind() == Tree.Kind.ARRAY_TYPE) {
      p = p.getParentPath();
    }
    assert p.getLeaf().getKind() == Tree.Kind.ARRAY_TYPE;
    return p;
  }

  /**
   * Returns the position of the first (non-commented) instance of a
   * character at or after the given position.
   *
   * {@see #getNthInstanceBetween(char, int, int, int, CompilationUnitTree)}
   */
  private int getFirstInstanceAfter(char c, int i) {
    return getNthInstanceInRange(c, i, Integer.MAX_VALUE, 1);
  }

  /**
   * Returns the position of the {@code n}th (non-commented, non-quoted)
   * instance of a character between the given positions, or the last
   * instance if {@code n==0}.
   * 
   * @param c the character being sought
   * @param start position at which the search starts (inclusive)
   * @param end position at which the search ends (exclusive)
   * @param n number of repetitions, or 0 for last occurrence
   * @return position of match in {@code tree}, or
   *          {@link Position.NOPOS} if match not found
   */
  private int getNthInstanceInRange(char c, int start, int end, int n) {
    if (end < 0) {
      throw new IllegalArgumentException("negative end position");
    }
    if (n < 0) {
      throw new IllegalArgumentException("negative count");
    }

    try {
      CharSequence s = tree.getSourceFile().getCharContent(true);
      int count = n;
      int pos = Position.NOPOS;
      int stop = Math.min(end, s.length());
      String cQuoted = c == '/' ? nonDelimSlash : Pattern.quote("" + c);
      String regex = "(?:" + otherThan(c) + ")*" + cQuoted;
      Pattern p = Pattern.compile(regex, Pattern.MULTILINE);
      Matcher m = p.matcher(s).region(start, stop);

      // using n==0 for "last" ensures that {@code (--n == 0)} is always
      // false, (reasonably) assuming no underflow
      while (m.find()) {
        pos = m.end() - 1;
        if (--count == 0) { break; }
      }
      // positive count means search halted before nth instance was found
      return count > 0 ? Position.NOPOS : pos;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Determines the insertion position for type annotations on various
   * elements.  For instance, type annotations for a declaration should be
   * placed before the type rather than the variable name.
   */
  private class TypePositionFinder extends TreeScanner<Integer, Insertion> {

    /** @param t an expression for a type */
    private int getBaseTypePosition(JCTree t) {
      while (true) {
        switch (t.getKind()) {
        case IDENTIFIER:
        case PRIMITIVE_TYPE:
          return t.pos;
        case MEMBER_SELECT:
          t = ((JCFieldAccess) t).getExpression();  // pkg name
          return getFirstInstanceAfter('.',
              t.getEndPosition(tree.endPositions)) + 1;
        case ARRAY_TYPE:
          t = ((JCArrayTypeTree) t).elemtype;
          break;
        case PARAMETERIZED_TYPE:
          t = ((JCTypeApply) t).getTypeArguments().get(0);
          break;
        case EXTENDS_WILDCARD:
        case SUPER_WILDCARD:
          t = ((JCWildcard) t).inner;
          break;
        case UNBOUNDED_WILDCARD:
          // This is "?" as in "List<?>".  ((JCWildcard) t).inner is null.
          // There is nowhere to attach the annotation, so for now return
          // the "?" tree itself.
          return t.pos;
        case ANNOTATED_TYPE:
          // If this type already has annotations on it, get the underlying
          // type, without annotations.
          t = ((JCAnnotatedType) t).underlyingType;
          break;
        default:
          throw new RuntimeException(String.format("Unrecognized type (kind=%s, class=%s): %s", t.getKind(), t.getClass(), t));
        }
      }
    }

    @Override
    public Integer visitVariable(VariableTree node, Insertion ins) {
      JCTree jt = ((JCVariableDecl) node).getType();
      debug("visitVariable: %s %s%n", jt, jt.getClass());
      if (jt instanceof JCTypeApply) {
        JCTypeApply vt = (JCTypeApply) jt;
        return vt.clazz.pos;
      }
      JCExpression type = (JCExpression) jt;
      return getBaseTypePosition(type);
    }

    // When a method is visited, it is visited for the receiver, not the
    // return value and not the declaration itself.
    @Override
    public Integer visitMethod(MethodTree node, Insertion ins) {
      debug("TypePositionFinder.visitMethod");
      super.visitMethod(node, ins);
      // System.out.println("node: " + node);
      // System.out.println("return: " + node.getReturnType());

      JCMethodDecl jcnode = (JCMethodDecl) node;

      if (node.getReceiverParameter() != null) {
        return ((JCTree) node.getReceiverParameter()).getStartPosition();
      } else if (!node.getParameters().isEmpty()) {
        return ((JCTree) node.getParameters().get(0)).getStartPosition();
      } else {
        // no parameters; find first (uncommented) '(' after method name
        int pos = findMethodName(jcnode);
        if (pos >= 0) {
          pos = getFirstInstanceAfter('(', pos);
          if (pos >= 0) { return pos + 1; }
        }
      }

      throw new RuntimeException("Couldn't find param opening paren for: "
          + jcnode);
    }

    Map<Pair<CompilationUnitTree,Tree>,TreePath> getPathCache1 =
        new HashMap<Pair<CompilationUnitTree,Tree>,TreePath>();

    /**
     * An alternative to TreePath.getPath(CompilationUnitTree,Tree) that
     * caches its results.
     */
    public TreePath getPath(CompilationUnitTree unit, Tree target) {
      Pair<CompilationUnitTree,Tree> args = Pair.of(unit, target);
      if (getPathCache1.containsKey(args)) {
        return getPathCache1.get(args);
      }
      TreePath result = TreePath.getPath(unit, target);
      getPathCache1.put(args, result);
      return result;
    }

    // private static Map<Pair<TreePath,Tree>,TreePath> getPathCache2 = new HashMap<Pair<TreePath,Tree>,TreePath>();

    /**
     * An alternative to TreePath.getPath(TreePath,Tree) that
     * caches its results.
     */
    /*
    public static TreePath getPath(TreePath path, Tree target) {
      Pair<TreePath,Tree> args = Pair.of(path, target);
      if (getPathCache2.containsKey(args)) {
        return getPathCache2.get(args);
      }
      TreePath result = TreePath.getPath(path, target);
      getPathCache2.put(args, result);
      return result;
    }
    */

    private Tree parent(Tree node) {
      return getPath(tree, node).getParentPath().getLeaf();
    }

    @Override
    public Integer visitIdentifier(IdentifierTree node, Insertion ins) {
      debug("TypePositionFinder.visitIdentifier(%s)", node);
      // for arrays, need to indent inside array, not right before type
      Tree parent = parent(node);
      Integer i = null;
      if (parent.getKind() == Tree.Kind.NEW_ARRAY) { // NewArrayTree)
        debug("TypePositionFinder.visitIdentifier: recognized array");
        i = ((JCIdent) node).getEndPosition(tree.endPositions);
      } else if (parent.getKind() == Tree.Kind.NEW_CLASS) { // NewClassTree)
        debug("TypePositionFinder.visitIdentifier: recognized class");
        JCNewClass nc = (JCNewClass) parent;
        debug("TypePositionFinder.visitIdentifier: clazz %s (%d) constructor %s",
              nc.clazz,
              nc.clazz.getPreferredPosition(),
              nc.constructor);
        i = nc.clazz.getPreferredPosition();
      } else {
        i = ((JCIdent) node).pos;
      }

      debug("visitIdentifier(%s) => %d where parent (%s) = %s%n", node, i, parent.getClass(), parent);
      return i;
    }

    @Override
    public Integer visitMemberSelect(MemberSelectTree node, Insertion ins) {
        debug("TypePositionFinder.visitMemberSelect(%s)", node);
        JCFieldAccess raw = (JCFieldAccess) node;
        return raw.getEndPosition(tree.endPositions) - raw.name.length();
    }

    @Override
    public Integer visitTypeParameter(TypeParameterTree node, Insertion ins) {
      JCTypeParameter tp = (JCTypeParameter) node;
      return tp.getStartPosition();
    }

    @Override
    public Integer visitWildcard(WildcardTree node, Insertion ins) {
      JCWildcard wc = (JCWildcard) node;
      return wc.getStartPosition();
    }

    @Override
    public Integer visitPrimitiveType(PrimitiveTypeTree node, Insertion ins) {
      debug("TypePositionFinder.visitPrimitiveType(%s)", node);
      return ((JCTree) node).pos;
    }

    @Override
    public Integer visitParameterizedType(ParameterizedTypeTree node, Insertion ins) {
      Tree parent = parent(node);
      debug("TypePositionFinder.visitParameterizedType %s parent=%s%n", node, parent);
      return getBaseTypePosition(((JCTypeApply) node).getType());
    }

//     @Override
//       public Integer visitBlock(BlockTree node, Void p) {
//       //debugging: System.out.println("visitBlock");
//       int rightBeforeBlock = ((JCBlock) node).pos;
//       // Will be adjusted if a throws statement exists.
//       int afterParamList = -1;
//       Tree parent = parent(node);
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

    private com.sun.tools.javac.code.Type.Visitor<Integer, Integer>
    arrayTypeVisitor = new Types.SimpleVisitor<Integer, Integer>() {
      @Override
      public Integer visitArrayType(ArrayType t, Integer i) {
        return t.elemtype.accept(this, i+1);
      }
      @Override
      public Integer visitAnnotatedType(AnnotatedType t, Integer i) {
        return t.unannotatedType().accept(this, i);
      }
      @Override
      public Integer visitType(com.sun.tools.javac.code.Type t, Integer i) {
        return i;
      }
    };

    /**
     * Returns the number of array levels that are in the given array type tree,
     * or 0 if the given node is not an array type tree.
     */
    private int arrayLevels(com.sun.tools.javac.code.Type t) {
      return t.accept(arrayTypeVisitor, 0);
    }

    private int arrayLevels(Tree node) {
      int result = 0;
      while (node.getKind() == Tree.Kind.ARRAY_TYPE) {
        result++;
        node = ((ArrayTypeTree) node).getType();
      }
      return result;
    }

    private JCTree arrayContentType(JCArrayTypeTree att) {
      JCTree node = att;
      do {
        node = ((JCArrayTypeTree) node).getType();
      } while (node.getKind() == Tree.Kind.ARRAY_TYPE);
      return node;
    }

    public ArrayTypeTree largestContainingArray(Tree node) {
      TreePath p = getPath(tree, node);
      Tree result = TreeFinder.largestContainingArray(p).getLeaf();
      assert result.getKind() == Tree.Kind.ARRAY_TYPE;
      return (ArrayTypeTree) result;
    }

    @Override
    public Integer visitArrayType(ArrayTypeTree node, Insertion ins) {
      debug("TypePositionFinder.visitArrayType(%s)", node);
      JCArrayTypeTree att = (JCArrayTypeTree) node;
      debug("TypePositionFinder.visitArrayType(%s) preferred = %s%n", node, att.getPreferredPosition());
      // If the code has a type like "String[][][]", then this gets called
      // three times:  for String[][][], String[][], and String[]
      // respectively.  For each of the three, call String[][][] "largest".
      ArrayTypeTree largest = largestContainingArray(node);
      int largestLevels = arrayLevels(largest);
      int levels = arrayLevels(node);
      int start = arrayContentType(att).getPreferredPosition() + 1;
      int end = att.getEndPosition(tree.endPositions);
      int pos = arrayInsertPos(start, end);

      debug("  levels=%d largestLevels=%d%n", levels, largestLevels);
      for (int i=levels; i<largestLevels; i++) {
        pos = getFirstInstanceAfter('[', pos+1);
        debug("  pos %d at i=%d%n", pos, i);
      }
      return pos;
    }

    /**
     * Find position in source code where annotation is to be inserted.
     * 
     * @param start beginning of range to be matched
     * @param end end of range to be matched
     * 
     * @return position for annotation insertion
     */
    private int arrayInsertPos(int start, int end) {
      try {
        CharSequence s = tree.getSourceFile().getCharContent(true);
        int pos = getNthInstanceInRange('[', start, end, 1);

        if (pos < 0) {
          // no "[", so check for "..."
          String nonDot = otherThan('.');
          String regex = "(?:(?:\\.\\.?)?" + nonDot + ")*(\\.\\.\\.)";
          Pattern p = Pattern.compile(regex, Pattern.MULTILINE);
          Matcher m = p.matcher(s).region(start, end);

          if (m.find()) {
            pos = m.start(1);
          }
          if (pos < 0) {  // should never happen
            throw new RuntimeException("no \"[\" or \"...\" in array type");
          }
        }
        return pos;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Integer visitCompilationUnit(CompilationUnitTree node, Insertion ins) {
      debug("TypePositionFinder.visitCompilationUnit");
      JCCompilationUnit cu = (JCCompilationUnit) node;
      return cu.getStartPosition();
    }

    @Override
    public Integer visitClass(ClassTree node, Insertion ins) {
      debug("TypePositionFinder.visitClass");
      JCClassDecl cd = (JCClassDecl) node;
      if (cd.mods != null) {
        return cd.mods.getPreferredPosition();
      } else {
        return cd.getPreferredPosition();
      }
    }


    // There are three types of array initializers:
    //   /*style 1*/ String[] names1 = new String[12];
    //   /*style 2*/ String[] names2 = { "Alice", "Bob" };
    //   /*style 3*/ String[] names3 = new String[] { "Alice", "Bob" };
    // (Can the styles be combined?)
    //
    // For style 1, we can just find the location of the
    // dimensionality expression, and then locate the bracket before it.
    // For style 2, annotations are impossible.
    // For style 3, we need to count the brackets and get to the right one.
    //
    // The AST depth of the initializer is correct unless all arrays are
    // empty, in which case it is arbitary.  This is legal:
    // String[][][][][] names4 = new String[][][][][] { { { } } };
    //
    // Array initializers can also be multi-dimensional, but this is not
    // relevant to us:
    //   int[][] pascalsTriangle = { { 1 }, { 1,1 }, { 1,2,1 } };
    //   int[][] pascalsTriangle = new int[][] { { 1 }, { 1,1 }, { 1,2,1 } };


    // structure stolen from javac's Pretty.java
    private int getDimsSize(JCExpression tree) {
      if (tree instanceof JCNewArray) {
        JCNewArray na = (JCNewArray) tree;
        if (na.dims.size() != 0) {
          // when not all dims are given, na.dims.size() gives wrong answer
          return arrayLevels(na.type);
        }
        if (na.elemtype != null) {
          return getDimsSize(na.elemtype) + 1;
        }
        assert na.elems != null;
        int maxDimsSize = 0;
        for (JCExpression elem : na.elems) {
          if (elem instanceof JCNewArray) {
            int elemDimsSize = getDimsSize((JCNewArray)elem);
            maxDimsSize = Math.max(maxDimsSize, elemDimsSize);
          } else if (elem instanceof JCArrayTypeTree) {
            // Does this ever happen?  javac's Pretty.java handles it.
            System.out.printf("JCArrayTypeTree: %s%n", elem);
          }
        }
        return maxDimsSize + 1;
      } else if (tree instanceof JCAnnotatedType) {
        return getDimsSize(((JCAnnotatedType) tree).underlyingType);
      } else if (tree instanceof JCArrayTypeTree) {
        return 1 + getDimsSize(((JCArrayTypeTree) tree).elemtype);
      } else {
        return 0;
      }
    }


    // Visit an expression of one of these forms:
    //   new int[5][10]
    //   new int[][] {...}
    //   { ... }            -- as in: String[] names2 = { "Alice", "Bob" };
    @Override
    public Integer visitNewArray(NewArrayTree node, Insertion ins) {
      debug("TypePositionFinder.visitNewArray");
      JCNewArray na = (JCNewArray) node;
      int dim = ins.getCriteria().getGenericArrayLocation().getLocation().size();
      // Invariant:  na.dims.size() == 0  or  na.elems == null  (but not both)
      // If na.dims.size() != 0, na.elemtype is non-null.
      // If na.dims.size() == 0, na.elemtype may be null or non-null.
      int dimsSize = getDimsSize(na);

      // System.out.printf("visitNewArray: dim=%d (arrayLocationInParent=%s), node=%s, elemtype=%s%s, dimsSize=%d, dims=%s (size=%d), elems=%s, annotations=%s (size=%d), dimAnnotations=%s (size=%d)%n", dim, arrayLocationInParent, na, na.elemtype, (na.elemtype == null ? "" : String.format(" (class: %s)", na.elemtype.getClass())), dimsSize, na.dims, na.dims.size(), na.elems, na.annotations, na.annotations.size(), na.dimAnnotations, na.dimAnnotations.size());
      if (na.toString().startsWith("{")) {
        if (ins.getKind() == Insertion.Kind.ANNOTATION) {
          TreePath parentPath = TreePath.getPath(tree, na).getParentPath();
          if (parentPath != null) {
            Tree parent = parentPath.getLeaf();
            if (parent.getKind() == Tree.Kind.VARIABLE) {
              try {
                CharSequence s = tree.getSourceFile().getCharContent(true);
                AnnotationInsertion ai = (AnnotationInsertion) ins;
                JCTree typeTree = ((JCVariableDecl) parent).getType();
                int start = typeTree.getStartPosition();
                int end = typeTree.getEndPosition(tree.endPositions);
                ai.setType(s.subSequence(start, end).toString());
                return na.getStartPosition();
              } catch (IOException e) {}
            }
          }
        }
      }
      if (dim == dimsSize) {
        if (na.elemtype == null) {
          System.err.println("WARNING: array initializer " + node +
              " has no explicit type; skipping insertion " + ins);
          return null;
        }
        // return na.elemtype.getPreferredPosition();
        return na.elemtype.getStartPosition();
      }
      if (na.dims.size() != 0) {
        int startPos = na.getStartPosition();
        int endPos = na.getEndPosition(tree.endPositions);
        return getNthInstanceInRange('[', startPos, endPos, dim + 1);
      }
      // In a situation like
      //   node=new String[][][][][]{{{}}}
      // Also see Pretty.printBrackets.
      if (dim == 0) {
        if (na.elemtype == null) {
          return na.getStartPosition();
        }
        // na.elemtype.getPreferredPosition(); seems to be at the end, after the brackets.
        // na.elemtype.getStartPosition(); is before the type name itself
        int startPos = na.elemtype.getStartPosition();
        return getFirstInstanceAfter('[', startPos+1);
      }
      JCArrayTypeTree jcatt = (JCArrayTypeTree) na.elemtype;
      for (int i=1; i<dim; i++) {
        JCTree elem = jcatt.elemtype;
        if (elem.hasTag(JCTree.Tag.ANNOTATED_TYPE)) {
          elem = ((JCAnnotatedType) elem).underlyingType;
        }
        if (!elem.hasTag(JCTree.Tag.TYPEARRAY)) {
          throw new Error();
        }
        jcatt = (JCArrayTypeTree) elem;
      }
      return jcatt.pos().getPreferredPosition();
    }

    @Override
    public Integer visitNewClass(NewClassTree node, Insertion ins) {
      JCNewClass na = (JCNewClass) node;
      JCExpression className = na.clazz;
      // System.out.printf("classname %s (%s)%n", className, className.getClass());
      while (! (className.getKind() == Tree.Kind.IDENTIFIER)) { // IdentifierTree
        if (className instanceof JCAnnotatedType) {
          className = ((JCAnnotatedType) className).underlyingType;
        } else if (className instanceof JCTypeApply) {
          className = ((JCTypeApply) className).clazz;
        } else if (className instanceof JCFieldAccess) {
          // This occurs for fully qualified names, e.g. "new java.lang.Object()".
          // I'm not quite sure why the field "selected" is taken, but "name" would
          // be a type mismatch. It seems to work, see NewPackage test case.
          className = ((JCFieldAccess) className).selected;
        } else {
          throw new Error(String.format("unrecognized JCNewClass.clazz (%s): %s%n" +
                  "   surrounding new class tree: %s%n", className.getClass(), className, node));
        }
        // System.out.printf("classname %s (%s)%n", className, className.getClass());
      }

      return visitIdentifier((IdentifierTree) className, ins);
    }

  }

  /**
   * Determine the insertion position for declaration annotations on
   * various elements.  For instance, method declaration annotations should
   * be placed before all the other modifiers and annotations.
   */
  private class DeclarationPositionFinder extends TreeScanner<Integer, Void> {

    // When a method is visited, it is visited for the declaration itself.
    @Override
    public Integer visitMethod(MethodTree node, Void p) {
      super.visitMethod(node, p);

      // System.out.printf("DeclarationPositionFinder.visitMethod()%n");

      ModifiersTree mt = node.getModifiers();

      // actually List<JCAnnotation>.
      List<? extends AnnotationTree> annos = mt.getAnnotations();
      // Set<Modifier> flags = mt.getFlags();

      JCTree before;
      if (annos.size() > 1) {
        before = (JCAnnotation) annos.get(0);
      } else if (node.getReturnType() != null) {
        before = (JCTree) node.getReturnType();
      } else {
        // if we're a constructor, we have null return type, so we use the constructor's position
        // rather than the return type's position
        before = (JCTree) node;
      }
      int declPos = before.getStartPosition();

      // There is no source code location information for Modifiers, so
      // cannot iterate through the modifiers.  But we don't have to.
      int modsPos = ((JCModifiers)mt).pos().getStartPosition();
      if (modsPos != Position.NOPOS) {
        declPos = Math.min(declPos, modsPos);
      }

      return declPos;
    }

    @Override
    public Integer visitCompilationUnit(CompilationUnitTree node, Void p) {
      JCCompilationUnit cu = (JCCompilationUnit) node;
      return cu.getStartPosition();
    }

    @Override
    public Integer visitClass(ClassTree node, Void p) {
      JCClassDecl cd = (JCClassDecl) node;
      int result;
      if (cd.mods != null
          && (cd.mods.flags != 0 || cd.mods.annotations.size() > 0)) {
        result = cd.mods.getPreferredPosition();
      } else {
        result = cd.getPreferredPosition();
      }
      assert result >= 0 || cd.name.isEmpty()
        : String.format("%d %d %d%n", cd.getStartPosition(),
                        cd.getPreferredPosition(), cd.pos);
      return result;
    }

  }

  private final Map<Tree, TreePath> paths;
  private final TypePositionFinder tpf;
  private final DeclarationPositionFinder dpf;
  private final JCCompilationUnit tree;
  private final SetMultimap<Integer, Insertion> positions;

  /**
   * Creates a {@code TreeFinder} from a source tree.
   *
   * @param tree the source tree to search
   */
  public TreeFinder(JCCompilationUnit tree) {
    this.tree = tree;
    this.positions = LinkedHashMultimap.create();
    this.tpf = new TypePositionFinder();
    this.dpf = new DeclarationPositionFinder();
    this.paths = new HashMap<Tree, TreePath>();
  }

  boolean handled(Tree node) {
    // FIXME: shouldn't use instanceof
    boolean res = (node instanceof CompilationUnitTree
            || node instanceof ClassTree
            || node instanceof MethodTree
            || node instanceof VariableTree
            || node instanceof IdentifierTree
            || node instanceof NewArrayTree
            || node instanceof NewClassTree
            || node instanceof ParameterizedTypeTree
            || node instanceof ArrayTypeTree
            || node instanceof PrimitiveTypeTree
            // For the case with implicit upper bound
            || node instanceof TypeParameterTree
            || node instanceof ExpressionTree
            || node instanceof ExpressionStatementTree
            || node instanceof WildcardTree
            );
    if (res) return true;

    return false;
  }

  /**
   * Determines if the last {@link TypePathEntry} in the given list is a
   * {@link TypePathEntryKind#WILDCARD}.
   *
   * @param location the list to check.
   * @return {@code true} if the last {@link TypePathEntry} is a
   *         {@link TypePathEntryKind#WILDCARD}, {@code false} otherwise.
   */
  private boolean wildcardLast(List<TypePathEntry> location) {
    return location.get(location.size() - 1).tag == TypePathEntryKind.WILDCARD;
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

    debug("SCANNING: %s %s%n", node.getKind(), node);
    if (! handled(node)) {
      debug("Not handled, skipping (%s): %s%n", node.getClass(), node);
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
      for (Tree t : path) {
        if (t.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
            // We started with something within a parameterized type and
            // should not look for any further annotations.
            // TODO: does this work on multiple nested levels?
            break;
        }
        if (t.getKind() == Tree.Kind.ANNOTATION) {
          return super.scan(node, p);
        }
      }
    }

    for (Iterator<Insertion> it = p.iterator(); it.hasNext(); ) {
      Insertion i = it.next();
      if (i.getInserted()) {
        // Skip this insertion if it has already been inserted. See
        // the ReceiverInsertion class for details.
        it.remove();
        continue;
      }
      try {
        if (debug) {
          debug("Considering insertion at tree:");
          debug("  Insertion: " + i);
          debug("  First line of node: " + Main.firstLine(node.toString()));
          debug("  Type of node: " + node.getClass());
        }

        if (!i.getCriteria().isSatisfiedBy(path, node)) {
          debug("  ... not satisfied");
          continue;
        } else {
          debug("  ... satisfied!");
          debug("    First line of node: " + Main.firstLine(node.toString()));
          debug("    Type of node: " + node.getClass());
        }

        // As per the JSR308 specification, receiver parameters are not allowed
        // on method declarations of anonymous inner classes.
        if (i.getCriteria().isOnReceiver()
                && path.getParentPath().getParentPath().getLeaf().getKind() == Tree.Kind.NEW_CLASS) {
          System.err.println("WARNING: Cannot insert a receiver parameter on a method "
              + "declaration of an anonymous inner class. This insertion will be skipped.\n"
              + "    Insertion: " + i);
          continue;
        }

        if (alreadyPresent(path, i)) {
          // Don't insert a duplicate if this particular annotation is already
          // present at this location.
          it.remove();
          continue;
        }

        if (i.getKind() == Insertion.Kind.RECEIVER && node.getKind() == Tree.Kind.METHOD) {
          ReceiverInsertion receiver = (ReceiverInsertion) i;
          MethodTree method = (MethodTree) node;

          if (method.getReceiverParameter() == null) {
            addReceiverType(path, receiver, method);
          }
        }

        // If this is a method, then it might have been selected because of
        // the receiver, or because of the return value.  Distinguish those.
        // One way would be to set a global variable here.  Another would be
        // to look for a particular different node.  I will do the latter.
        Integer pos;

        debug("TreeFinder.scan: node=%s%n  critera=%s%n", node, i.getCriteria());

        if ((node.getKind() == Tree.Kind.METHOD) && (i.getCriteria().isOnReturnType())) {
          JCMethodDecl jcnode = (JCMethodDecl) node;
          Tree returnType = jcnode.getReturnType();
          if (returnType == null) {
            // find constructor name instead
            pos = findMethodName(jcnode);
            debug("pos = %d at constructor name: %s%n", pos,
                jcnode.sym.toString());
          } else {
            pos = tpf.scan(returnType, i);
            assert handled(node);
            debug("pos = %d at return type node: %s%n", pos,
                returnType.getClass());
          }
        } else if (((node.getKind() == Tree.Kind.TYPE_PARAMETER)
                    && (i.getCriteria().onBoundZero())
                    && ((TypeParameterTree) node).getBounds().isEmpty())
                || ((node instanceof WildcardTree)
                    && ((WildcardTree) node).getBound() == null
                    && wildcardLast(i.getCriteria().getGenericArrayLocation().getLocation()))) {
            pos = tpf.scan(node, i);

            Integer nextpos1 = getFirstInstanceAfter(',', pos+1);
            Integer nextpos2 = getFirstInstanceAfter('>', pos+1);
            pos = (nextpos1 != Position.NOPOS && nextpos1 < nextpos2) ? nextpos1 : nextpos2;

            i = new TypeBoundExtendsInsertion(i.getText(), i.getCriteria(), i.getSeparateLine());
        } else if (i.getKind() == Insertion.Kind.CAST) {
            Type t = ((CastInsertion) i).getType();
            JCTree jcTree = (JCTree) node;
            pos = jcTree.getStartPosition();
            if (t.getKind() == Type.Kind.DECLARED) {
                DeclaredType dt = (DeclaredType) t;
                if (dt.getName().isEmpty()) {
                    dt.setName(jcTree.type instanceof NullType
                            ? "Object"
                            : jcTree.type.toString());
                }
            }
        } else if (i.getKind() == Insertion.Kind.CLOSE_PARENTHESIS) {
            JCTree jcTree = (JCTree) node;
            pos = jcTree.getStartPosition() + jcTree.toString().length();
        } else {
          boolean typeScan = true;
          if (node.getKind() == Tree.Kind.METHOD) { // MethodTree
            // looking for the receiver or the declaration
            typeScan = i.getCriteria().isOnReceiver();
          } else if (node.getKind() == Tree.Kind.CLASS) { // ClassTree
            typeScan = ! i.getSeparateLine(); // hacky check
          }
          if (typeScan) {
            // looking for the type
            debug("Calling tpf.scan(%s: %s)%n", node.getClass(), node);
            pos = tpf.scan(node, i);
            assert handled(node);
            debug("pos = %d at type: %s (%s)%n", pos, node.toString(), node.getClass());
          } else {
            // looking for the declaration
            pos = dpf.scan(node, null);
            assert pos != null;
            debug("pos = %d at declaration: %s%n", pos, node.getClass());
          }
        }

        debug("  ... satisfied! at %d for node of type %s: %s", pos, node.getClass(), Main.treeToString(node));

        if (pos != null) {
          assert pos >= 0 : String.format("pos: %s%nnode: %s%ninsertion: %s%n", pos, node, i);
          positions.put(pos, i);
        }

        it.remove();
      } catch (Throwable e) {
        reportInsertionError(i, e);
      }
    }

    return super.scan(node, p);
  }

  /**
   * Returns the start position of the method's name.  In particular,
   * works properly for constructors, for which the name field in the
   * AST is always "<init>" instead of the name from the source.
   * 
   * @param node AST node of method declaration
   * @return position of method name (from {@link JCMethodDecl#sym}) in source
   */
  private int findMethodName(JCMethodDecl node) {
    String sym = node.sym.toString();
    String name = sym.substring(0, sym.indexOf('('));
    JCModifiers mods = node.getModifiers();
    int start = mods.getStartPosition() < 0
        ? node.getStartPosition()
        : mods.getEndPosition(tree.endPositions);
    int end = node.body == null
        ? node.getEndPosition(tree.endPositions) - 1
        : node.body.getStartPosition();
    int angle = name.lastIndexOf('>');  // check for type params
    if (angle >= 0) { name = name.substring(angle + 1); }

    try {
      CharSequence s = tree.getSourceFile().getCharContent(true);
      String regex = "\\b" + Pattern.quote(name) + "\\b";  // sufficient?
      Pattern pat = Pattern.compile(regex, Pattern.MULTILINE);
      Matcher mat = pat.matcher(s).region(start, end);
      return mat.find() ? mat.start() : Position.NOPOS;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Determines if the annotation in the given insertion is already present
   * at the given location in the AST.
   *
   * @param path The location in the AST to check for the annotation.
   * @param ins The annotation to check for.
   * @return {@code true} if the given annotation is already at the given
   *         location in the AST, {@code false} otherwise.
   */
  private boolean alreadyPresent(TreePath path, Insertion ins) {
    List<? extends AnnotationTree> alreadyPresent = null;
    if (path != null) {
      for (Tree n : path) {
        if (n.getKind() == Tree.Kind.CLASS) {
          alreadyPresent = ((ClassTree) n).getModifiers().getAnnotations();
          break;
        } else if (n.getKind() == Tree.Kind.METHOD) {
          alreadyPresent = ((MethodTree) n).getModifiers().getAnnotations();
          break;
        } else if (n.getKind() == Tree.Kind.VARIABLE) {
          alreadyPresent = ((VariableTree) n).getModifiers().getAnnotations();
          break;
        } else if (n.getKind() == Tree.Kind.TYPE_CAST) {
          Tree type = ((TypeCastTree) n).getType();
          if (type.getKind() == Tree.Kind.ANNOTATED_TYPE) {
            alreadyPresent = ((AnnotatedTypeTree) type).getAnnotations();
          }
          break;
        } else if (n.getKind() == Tree.Kind.INSTANCE_OF) {
          Tree type = ((InstanceOfTree) n).getType();
          if (type.getKind() == Tree.Kind.ANNOTATED_TYPE) {
            alreadyPresent = ((AnnotatedTypeTree) type).getAnnotations();
          }
          break;
        } else if (n.getKind() == Tree.Kind.NEW_CLASS) {
          JCNewClass nc = (JCNewClass) n;
          if (nc.clazz.getKind() == Tree.Kind.ANNOTATED_TYPE) {
            alreadyPresent = ((AnnotatedTypeTree) nc.clazz).getAnnotations();
          }
          break;
        } else if (n.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
          // If we pass through a parameterized type, stop, otherwise we
          // mix up annotations on the outer type.
          // TODO: is something similar needed for Arrays?
          break;
        }
        // TODO: don't add cast insertion if it's already present.
      }
    }
    // System.out.printf("alreadyPresent = %s for %s of kind %s%n", alreadyPresent, node, node.getKind());
    // printPath(path);
    if (alreadyPresent != null) {
      for (AnnotationTree at : alreadyPresent) {
        // Compare the to-be-inserted annotation to the existing
        // annotation, ignoring its arguments (duplicate annotations are
        // never allowed even if they differ in arguments).  If we did
        // have to compare our arguments, we'd have to deal with enum
        // arguments potentially being fully qualified or not:
        // @Retention(java.lang.annotation.RetentionPolicy.CLASS) vs
        // @Retention(RetentionPolicy.CLASS)
        String ann = at.getAnnotationType().toString();
        String iann = Main.removeArgs(ins.getText()).a.substring(1); // strip off the leading @
        String iannNoPackage = Insertion.removePackage(iann).b;
        // System.out.printf("Comparing: %s %s %s%n", ann, iann, iannNoPackage);
        if (ann.equals(iann) || ann.equals(iannNoPackage)) {
          debug("Already present, not reinserting: %s%n", ann);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Reports an error inserting an insertion to {@code System.err}.
   * @param i The insertion that caused the error.
   * @param e The error. If there's a message it will be printed.
   */
  public static void reportInsertionError(Insertion i, Throwable e) {
    System.err.println("Error processing insertion:");
    System.err.println("\t" + i);
    if (e.getMessage() != null) {
      // If the message has multiple lines, indent them so it's easier to read.
      System.err.println("\tError: " + e.getMessage().replace("\n", "\n\t\t"));
    }
    if (debug || Main.print_error_stack) {
      e.printStackTrace();
    } else {
      System.err.println("\tRun with --print_error_stack to see the stack trace.");
    }
    System.err.println("\tThis insertion will be skipped.");
  }

  /**
   * Modifies the given receiver insertion so that it contains the type
   * information necessary to insert a full method declaration receiver
   * parameter. This is for receiver insertions where a receiver does not
   * already exist in the source code. This will also add the annotations to be
   * inserted to the correct part of the receiver type.
   *
   * @param path The location in the AST to insert the receiver.
   * @param receiver Details of the receiver to insert.
   * @param method The method the receiver is being inserted into.
   */
  private void addReceiverType(TreePath path, ReceiverInsertion receiver,
      MethodTree method) {
    // Find the name of the class
    // with type parameters to create the receiver. Walk up the tree and
    // pick up class names to add to the receiver type. Since we're
    // starting from the innermost class, the classes we get to at earlier
    // iterations of the loop are inside of the classes we get to at later
    // iterations.
    TreePath parent = path;
    Tree leaf = parent.getLeaf();
    Kind kind = leaf.getKind();
    // This is the outermost type, currently containing only the
    // annotation to add to the receiver.
    DeclaredType outerType = receiver.getType();
    // This holds the inner types as they're being read in.
    DeclaredType innerTypes = null;
    DeclaredType staticType = null;
    // For an inner class constructor, the receiver comes from the
    // superclass, so skip past the first type definition.
    boolean skip = ((MethodTree) parent.getLeaf()).getReturnType() == null;
    while (kind != Tree.Kind.COMPILATION_UNIT
        && kind != Tree.Kind.NEW_CLASS) {
      if (kind == Tree.Kind.CLASS
          || kind == Tree.Kind.INTERFACE
          || kind == Tree.Kind.ENUM
          || kind == Tree.Kind.ANNOTATION_TYPE) {
        ClassTree clazz = (ClassTree) leaf;
        String className = clazz.getSimpleName().toString();
        boolean isStatic = kind == Tree.Kind.INTERFACE
            || kind == Tree.Kind.ENUM
            || clazz.getModifiers().getFlags().contains(Modifier.STATIC);
        skip &= !isStatic;
        if (skip) {
          skip = false;
          receiver.setQualifyThis(true);
        } else if (!className.isEmpty()) {
          // className will be empty for the CLASS node directly inside an
          // anonymous inner class NEW_CLASS node.
          DeclaredType inner = new DeclaredType(className);
          if (staticType == null) {
            // Only include type parameters on the classes to the right of and
            // including the rightmost static class.
            for (TypeParameterTree tree : clazz.getTypeParameters()) {
              inner.addTypeParameter(new DeclaredType(tree.getName().toString()));
            }
          }
          if (staticType == null && isStatic) {
            // If this is the first static class then move the annotations here.
            inner.setAnnotations(outerType.getAnnotations());
            outerType.clearAnnotations();
            staticType = inner;
          }
          if (innerTypes == null) {
            // This is the first type we've read in, so set it as the
            // innermost type.
            innerTypes = inner;
          } else {
            // inner (the type just read in this iteration) is outside of
            // innerTypes (the types already read in previous iterations).
            inner.setInnerType(innerTypes);
            innerTypes = inner;
          }
        }
      }
      parent = parent.getParentPath();
      leaf = parent.getLeaf();
      kind = leaf.getKind();
    }

    // Merge innerTypes into outerType: outerType only has the annotations
    // on the receiver, while innerTypes has everything else. innerTypes can
    // have the annotations if it is a static class.
    outerType.setName(innerTypes.getName());
    outerType.setTypeParameters(innerTypes.getTypeParameters());
    outerType.setInnerType(innerTypes.getInnerType());
    if (staticType != null && !innerTypes.getAnnotations().isEmpty()) {
      outerType.setAnnotations(innerTypes.getAnnotations());
    }

    Type type = (staticType == null) ? outerType : staticType;
    Insertion.decorateType(receiver.getInnerTypeInsertions(), type);

    // If the method doesn't have parameters, don't add a comma.
    receiver.setAddComma(method.getParameters().size() > 0);
  }

  /**
   * Scans the given tree with the given insertion list and returns the
   * mapping from source position to insertion text.  The positions are sorted
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
  public SetMultimap<Integer, Insertion> getPositions(JCCompilationUnit node, List<Insertion> p) {
    List<Insertion> uninserted = new LinkedList<Insertion>(p);
    this.scan(node, uninserted);
    // There may be many extra annotations in a .jaif file.  For instance,
    // the .jaif file may be for an entire library, but its compilation
    // units are processed one by one.
    // However, we should warn about any insertions that were within the
    // given compilation unit but still didn't get inserted.
    List<? extends Tree> typeDecls = node.getTypeDecls();
    for (Insertion i : uninserted) {
      InClassCriterion c = i.getCriteria().getInClass();
      if (c == null) continue;
      for (Tree t : typeDecls) {
        if (c.isSatisfiedBy(TreePath.getPath(node, t))) {
          // Avoid warnings about synthetic generated methods.
          // This test is too coarse, but is good enough for now.
          // There are also synthetic local variables; maybe suppress
          // warnings about them, too.
          if (! (i.getCriteria().isOnMethod("<init>()V")
                 || i.getCriteria().isOnLocalVariable())) {
            // Should be made more user-friendly
            System.err.printf("Found class %s, but unable to insert %s:%n  %s%n", c.className, i.getText(), i);
          }
        }
      }
    }
    if (debug) {
      // Output every insertion that was not given a position:
      for (Insertion i : uninserted) {
        System.err.println("Unable to insert: " + i);
      }
    }
    debug("getPositions => %d positions%n", positions.size());
    return Multimaps.unmodifiableSetMultimap(positions);
  }

  public SetMultimap<Integer, Insertion> getPositions(JCCompilationUnit node,
      Insertions insertions) {
    List<Insertion> list = new ArrayList<Insertion>();
    list.addAll(insertions.forClass(""));
    for (JCTree decl : node.getTypeDecls()) {
      if (decl.getTag() == JCTree.Tag.CLASSDEF) {
        Name name = ((JCClassDecl) decl).getSimpleName();
        list.addAll(insertions.forClass(name.toString()));
      }
    }
    return getPositions(node, list);
  }

  /*
  private static void printPath(TreePath path) {
    System.out.printf("-----path:%n");
    if (path != null) {
      for (Tree t : path) {
        System.out.printf("%s %s%n", t.getKind(), t);
      }
    }
    System.out.printf("-----end path.%n");
  }
  */
}
