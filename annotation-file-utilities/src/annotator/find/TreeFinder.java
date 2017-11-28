package annotator.find;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.NullType;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
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
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;
import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntryKind;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotatedType;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
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
import com.sun.tools.javac.util.Position;

import scenelib.annotations.io.ASTIndex;
import scenelib.annotations.io.ASTPath;
import scenelib.annotations.io.ASTRecord;
import scenelib.annotations.io.DebugWriter;
import annotator.Main;
import annotator.scanner.CommonScanner;
import annotator.specification.IndexFileSpecification;

import plume.Pair;

import scenelib.type.DeclaredType;
import scenelib.type.Type;

/**
 * A {@link TreeScanner} that is able to locate program elements in an
 * AST based on {@code Criteria}. {@link #getInsertionsByPosition(JCTree.JCCompilationUnit,List)}
 * scans a tree and returns a
 * mapping of source positions (as character offsets) to insertion text.
 */
public class TreeFinder extends TreeScanner<Void, List<Insertion>> {
  public static final DebugWriter dbug = new DebugWriter();
  public static final DebugWriter stak = new DebugWriter();
  public static final DebugWriter warn = new DebugWriter();

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
      "//.*$|/\\*[^*]*+\\*++(?:[^*/][^*]*+\\*++)*+/";

  /**
   * Regular expression matching a character or string literal.
   */
  private final static String literal =
      "'(?:(?:\\\\(?:'|[^']*+))|[^\\\\'])'|\"(?:\\\\.|[^\\\\\"])*\"";

  /**
   * Regular expression matching a non-commented instance of {@code /}
   * that is not part of a comment-starting delimiter.
   */
  private final static String nonDelimSlash = "/(?=[^*/])";

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
   * character at or after the given position.  (Assumes position is not
   * inside a comment.)
   *
   * @see #getNthInstanceBetween(char, int, int, int, CompilationUnitTree)
   */
  private int getFirstInstanceAfter(char c, int i) {
    return getNthInstanceInRange(c, i, Integer.MAX_VALUE, 1);
  }

  /**
   * Returns the position of the {@code n}th (non-commented, non-quoted)
   * instance of a character between the given positions, or the last
   * instance if {@code n==0}.  (Assumes position is not inside a
   * comment.)
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
      String regex = "(?:" + otherThan(c) + ")*+" + cQuoted;
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

  // Find a node's parent in the current source tree.
  private Tree parent(Tree node) {
    return getPath(node).getParentPath().getLeaf();
  }

  /**
   * An alternative to TreePath.getPath(CompilationUnitTree,Tree) that
   * caches its results.
   */
  public TreePath getPath(Tree target) {
    if (treePathCache.containsKey(target)) {
      return treePathCache.get(target);
    }
    TreePath result = TreePath.getPath(tree, target);
    treePathCache.put(target, result);
    return result;
  }

  Map<Tree, TreePath> treePathCache = new HashMap<Tree, TreePath>();

  private ASTRecord astRecord(Tree node) {
    Map<Tree, ASTRecord> index = ASTIndex.indexOf(tree);
    return index.get(node);
  }

  /**
   * Determines the insertion position for type annotations on various
   * elements.  For instance, type annotations for a declaration should be
   * placed before the type rather than the variable name.
   */
  private class TypePositionFinder
  extends TreeScanner<Pair<ASTRecord, Integer>, Insertion> {
    private Pair<ASTRecord, Integer> pathAndPos(JCTree t) {
      return Pair.of(astRecord(t), t.pos);
    }

    private Pair<ASTRecord, Integer> pathAndPos(JCTree t, int i) {
      return Pair.of(astRecord(t), i);
    }

    /** @param t an expression for a type */
    private Pair<ASTRecord, Integer> getBaseTypePosition(JCTree t) {
      while (true) {
        switch (t.getKind()) {
        case IDENTIFIER:
        case PRIMITIVE_TYPE:
          return pathAndPos(t);
        case MEMBER_SELECT:
          JCTree exp = t;
          do {  // locate pkg name, if any
            JCFieldAccess jfa = (JCFieldAccess) exp;
            exp = jfa.getExpression();
            if (jfa.sym.isStatic()) {
              return pathAndPos(exp,
                  getFirstInstanceAfter('.',
                      exp.getEndPosition(tree.endPositions)) + 1);
            }
          } while (exp instanceof JCFieldAccess
              && ((JCFieldAccess) exp).sym.getKind() != ElementKind.PACKAGE);
          if (exp != null) {
            if (exp.getKind() == Tree.Kind.IDENTIFIER) {
              Symbol sym = ((JCIdent) exp).sym;
              if (!(sym.isStatic() || sym.getKind() == ElementKind.PACKAGE)) {
                return pathAndPos(t, t.getStartPosition());
              }
            }
            t = exp;
          }
          return pathAndPos(t,
              getFirstInstanceAfter('.',
                  t.getEndPosition(tree.endPositions)) + 1);
        case ARRAY_TYPE:
          t = ((JCArrayTypeTree) t).elemtype;
          break;
        case PARAMETERIZED_TYPE:
          return pathAndPos(t, t.getStartPosition());
        case EXTENDS_WILDCARD:
        case SUPER_WILDCARD:
          t = ((JCWildcard) t).inner;
          break;
        case UNBOUNDED_WILDCARD:
          // This is "?" as in "List<?>".  ((JCWildcard) t).inner is null.
          // There is nowhere to attach the annotation, so for now return
          // the "?" tree itself.
          return pathAndPos(t);
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
    public Pair<ASTRecord, Integer> visitVariable(VariableTree node, Insertion ins) {
      Name name = node.getName();
      JCVariableDecl jn = (JCVariableDecl) node;
      JCTree jt = jn.getType();
      Criteria criteria = ins.getCriteria();
      dbug.debug("visitVariable: %s %s%n", jt, jt.getClass());
      if (name != null && criteria.isOnFieldDeclaration()) {
        return Pair.of(astRecord(node), jn.getStartPosition());
      }
      if (jt instanceof JCTypeApply) {
        JCExpression type = ((JCTypeApply) jt).clazz;
        return pathAndPos(type);
      }
      return Pair.of(astRecord(node), jn.pos);
    }

    // When a method is visited, it is visited for the receiver, not the
    // return value and not the declaration itself.
    @Override
    public Pair<ASTRecord, Integer> visitMethod(MethodTree node, Insertion ins) {
      dbug.debug("TypePositionFinder.visitMethod%n");
      super.visitMethod(node, ins);

      JCMethodDecl jcnode = (JCMethodDecl) node;
      JCVariableDecl jcvar = (JCVariableDecl) node.getReceiverParameter();
      if (jcvar != null) { return pathAndPos(jcvar); }

      int pos = Position.NOPOS;
      ASTRecord astPath = astRecord(jcnode)
          .extend(Tree.Kind.METHOD, ASTPath.PARAMETER, -1);

      if (node.getParameters().isEmpty()) {
        // no parameters; find first (uncommented) '(' after method name
        pos = findMethodName(jcnode);
        if (pos >= 0) { pos = getFirstInstanceAfter('(', pos); }
        if (++pos <= 0) {
          throw new RuntimeException("Couldn't find param opening paren for: "
              + jcnode);
        }
      } else {
        pos = ((JCTree) node.getParameters().get(0)).getStartPosition();
      }
      return Pair.of(astPath, pos);
    }

    @Override
    public Pair<ASTRecord, Integer> visitIdentifier(IdentifierTree node, Insertion ins) {
      dbug.debug("TypePositionFinder.visitIdentifier(%s)%n", node);
      // for arrays, need to indent inside array, not right before type
      ASTRecord rec = ASTIndex.indexOf(tree).get(node);
      ASTPath astPath = ins.getCriteria().getASTPath();
      Tree parent = parent(node);
      Integer i = null;
      JCIdent jcnode = (JCIdent) node;

      // ASTPathEntry.type _n_ is a special case because it does not
      // correspond to a node in the AST.
      if (parent.getKind() == Tree.Kind.NEW_ARRAY) { // NewArrayTree)
        ASTPath.ASTEntry entry;
        dbug.debug("TypePositionFinder.visitIdentifier: recognized array%n");
        if (astPath == null) {
          entry = new ASTPath.ASTEntry(Tree.Kind.NEW_ARRAY, ASTPath.TYPE, 0);
          astPath = astRecord(parent).extend(entry).astPath;
        } else {
          entry = astPath.get(astPath.size() - 1);  // kind is NewArray
        }
        if (entry.childSelectorIs(ASTPath.TYPE)) {
          int n = entry.getArgument();
          i = jcnode.getStartPosition();
          if (n < getDimsSize((JCExpression) parent)) {  // else n == #dims
            i = getNthInstanceInRange('[', i,
                ((JCNewArray) parent).getEndPosition(tree.endPositions), n+1);
          }
        }
        if (i == null) {
          i = jcnode.getEndPosition(tree.endPositions);
        }
      } else if (parent.getKind() == Tree.Kind.NEW_CLASS) { // NewClassTree)
        dbug.debug("TypePositionFinder.visitIdentifier: recognized class%n");
        JCNewClass nc = (JCNewClass) parent;
        dbug.debug(
            "TypePositionFinder.visitIdentifier: clazz %s (%d) constructor %s%n",
            nc.clazz, nc.clazz.getPreferredPosition(), nc.constructor);
        i = nc.clazz.getPreferredPosition();
        if (astPath == null) {
          astPath = astRecord(node).astPath;
        }
      } else {
        ASTRecord astRecord = astRecord(node);
        astPath = astRecord.astPath;
        i = ((JCIdent) node).pos;
      }

      dbug.debug("visitIdentifier(%s) => %d where parent (%s) = %s%n",
          node, i, parent.getClass(), parent);
      return Pair.of(rec.replacePath(astPath), i);
    }

    @Override
    public Pair<ASTRecord, Integer> visitMemberSelect(MemberSelectTree node, Insertion ins) {
      dbug.debug("TypePositionFinder.visitMemberSelect(%s)%n", node);
      JCFieldAccess raw = (JCFieldAccess) node;
      return Pair.of(astRecord(node),
          raw.getEndPosition(tree.endPositions) - raw.name.length());
    }

    @Override
    public Pair<ASTRecord, Integer> visitTypeParameter(TypeParameterTree node, Insertion ins) {
      JCTypeParameter tp = (JCTypeParameter) node;
      return Pair.of(astRecord(node), tp.getStartPosition());
    }

    @Override
    public Pair<ASTRecord, Integer> visitWildcard(WildcardTree node, Insertion ins) {
      JCWildcard wc = (JCWildcard) node;
      return Pair.of(astRecord(node), wc.getStartPosition());
    }

    @Override
    public Pair<ASTRecord, Integer> visitPrimitiveType(PrimitiveTypeTree node, Insertion ins) {
      dbug.debug("TypePositionFinder.visitPrimitiveType(%s)%n", node);
      return pathAndPos((JCTree) node);
    }

    @Override
    public Pair<ASTRecord, Integer> visitParameterizedType(ParameterizedTypeTree node, Insertion ins) {
      Tree parent = parent(node);
      dbug.debug("TypePositionFinder.visitParameterizedType %s parent=%s%n",
          node, parent);
      Integer pos = getBaseTypePosition(((JCTypeApply) node).getType()).b;
      return Pair.of(astRecord(node), pos);
    }

    /**
     * Returns the number of array levels that are in the given array type tree,
     * or 0 if the given node is not an array type tree.
     */
    private int arrayLevels(com.sun.tools.javac.code.Type t) {
      return t.accept(new Types.SimpleVisitor<Integer, Integer>() {
        @Override
        public Integer visitArrayType(com.sun.tools.javac.code.Type.ArrayType t,
            Integer i) {
          return t.elemtype.accept(this, i+1);
        }
        @Override
        public Integer visitType(com.sun.tools.javac.code.Type t, Integer i) {
          return i;
        }
      }, 0);
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

    private ArrayTypeTree largestContainingArray(Tree node) {
      TreePath p = getPath(node);
      Tree result = TreeFinder.largestContainingArray(p).getLeaf();
      assert result.getKind() == Tree.Kind.ARRAY_TYPE;
      return (ArrayTypeTree) result;
    }

    @Override
    public Pair<ASTRecord, Integer> visitArrayType(ArrayTypeTree node, Insertion ins) {
      dbug.debug("TypePositionFinder.visitArrayType(%s)%n", node);
      JCArrayTypeTree att = (JCArrayTypeTree) node;
      dbug.debug("TypePositionFinder.visitArrayType(%s) preferred = %s%n",
          node, att.getPreferredPosition());
      // If the code has a type like "String[][][]", then this gets called
      // three times:  for String[][][], String[][], and String[]
      // respectively.  For each of the three, call String[][][] "largest".
      ArrayTypeTree largest = largestContainingArray(node);
      int largestLevels = arrayLevels(largest);
      int levels = arrayLevels(node);
      int start = arrayContentType(att).getPreferredPosition() + 1;
      int end = att.getEndPosition(tree.endPositions);
      int pos = arrayInsertPos(start, end);

      dbug.debug("  levels=%d largestLevels=%d%n", levels, largestLevels);
      for (int i=levels; i<largestLevels; i++) {
        pos = getFirstInstanceAfter('[', pos+1);
        dbug.debug("  pos %d at i=%d%n", pos, i);
      }
      return Pair.of(astRecord(node), pos);
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
    public Pair<ASTRecord, Integer> visitCompilationUnit(CompilationUnitTree node, Insertion ins) {
      dbug.debug("TypePositionFinder.visitCompilationUnit%n");
      JCCompilationUnit cu = (JCCompilationUnit) node;
      return Pair.of(astRecord(node), cu.getStartPosition());
    }

    @Override
    public Pair<ASTRecord, Integer> visitClass(ClassTree node, Insertion ins) {
      dbug.debug("TypePositionFinder.visitClass%n");
      JCClassDecl cd = (JCClassDecl) node;
      JCTree t = cd.mods == null ? cd : cd.mods;
      return Pair.of(astRecord(cd), t.getPreferredPosition());
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
    public Pair<ASTRecord, Integer> visitNewArray(NewArrayTree node, Insertion ins) {
      dbug.debug("TypePositionFinder.visitNewArray%n");
      JCNewArray na = (JCNewArray) node;
      GenericArrayLocationCriterion galc =
          ins.getCriteria().getGenericArrayLocation();
      ASTRecord rec = ASTIndex.indexOf(tree).get(node);
      ASTPath astPath = ins.getCriteria().getASTPath();
      String childSelector = null;
      // Invariant:  na.dims.size() == 0  or  na.elems == null  (but not both)
      // If na.dims.size() != 0, na.elemtype is non-null.
      // If na.dims.size() == 0, na.elemtype may be null or non-null.
      int dimsSize = getDimsSize(na);
      int dim = galc == null ? 0 : galc.getLocation().size();

      if (astPath == null) {
        astPath = astRecord(node).astPath.extendNewArray(dim);
        childSelector = ASTPath.TYPE;
      } else {
        ASTPath.ASTEntry lastEntry = null;
        int n = astPath.size();
        int i = n;
        // find matching node = last path entry w/kind NEW_ARRAY
        while (--i >= 0) {
          lastEntry = astPath.get(i);
          if (lastEntry.getTreeKind() == Tree.Kind.NEW_ARRAY) { break; }
        }
        assert i >= 0 : "no matching path entry (kind=NEW_ARRAY)";
        if (n > i+1) {
          // find correct node further down and visit if present
          assert dim + 1 == dimsSize;
          Tree typeTree = na.elemtype;
          int j = i + dim + 1;
          while (--dim >= 0) {
            typeTree = ((ArrayTypeTree) typeTree).getType();
          }
loop:
          while (j < n) {
            ASTPath.ASTEntry entry = astPath.get(j);
            switch (entry.getTreeKind()) {
            case ANNOTATED_TYPE:
              typeTree = ((AnnotatedTypeTree) typeTree).getUnderlyingType();
              continue;  // no increment
            case ARRAY_TYPE:
              typeTree = ((ArrayTypeTree) typeTree).getType();
              break;
            case MEMBER_SELECT:
              if (typeTree instanceof JCTree.JCFieldAccess) {
                JCTree.JCFieldAccess jfa = (JCTree.JCFieldAccess) typeTree;
                typeTree = jfa.getExpression();
                // if just a qualifier, don't increment loop counter
                if (jfa.sym.getKind() == ElementKind.PACKAGE) { continue; }
                break;
              }
              break loop;
            case PARAMETERIZED_TYPE:
              if (entry.childSelectorIs(ASTPath.TYPE_ARGUMENT)) {
                int arg = entry.getArgument();
                List<? extends Tree> typeArgs =
                    ((ParameterizedTypeTree) typeTree).getTypeArguments();
                typeTree = typeArgs.get(arg);
              } else {  // ASTPath.TYPE
                typeTree = ((ParameterizedTypeTree) typeTree).getType();
              }
              break;
            default:
              break loop;
            }
            ++j;
          }
          if (j < n) {
            // sought node is absent, so return default; insertion can
            // be applied only as an inner of some TypedInsertion anyway
            return getBaseTypePosition(na);
          }
          return typeTree.accept(this, ins);
        }

        childSelector = lastEntry.getChildSelector();
        if (dim > 0 && ASTPath.TYPE.equals(childSelector)) {
          // rebuild path with current value of dim
          ASTPath newPath = ASTPath.empty();
          int j = 0;
          dim += lastEntry.getArgument();
          while (j < i) {  // [0,i)
            newPath = newPath.extend(astPath.get(j));
            j++;
          }
          lastEntry = new ASTPath.ASTEntry(Tree.Kind.NEW_ARRAY,
              ASTPath.TYPE, dim);  // i
          newPath = newPath.extend(lastEntry);
          while (j < n) {  // [i,n)
            newPath = newPath.extend(astPath.get(j));
            j++;
          }
          astPath = newPath;
        } else {
          dim = lastEntry.getArgument();
        }
      }

      if (ASTPath.TYPE.equals(childSelector)) {
      if (na.toString().startsWith("{")) {
        if (ins.getKind() == Insertion.Kind.ANNOTATION) {
          TreePath parentPath = TreePath.getPath(tree, na).getParentPath();
          if (parentPath != null) {
            Tree parent = parentPath.getLeaf();
            if (parent.getKind() == Tree.Kind.VARIABLE) {
              AnnotationInsertion ai = (AnnotationInsertion) ins;
              JCTree typeTree = ((JCVariableDecl) parent).getType();
              ai.setType(typeTree.toString());
              return Pair.of(rec.replacePath(astPath), na.getStartPosition());
            }
          }
          System.err.println("WARNING: array initializer " + node +
              " has no explicit type; skipping insertion " + ins);
          return null;
        } else {
          return Pair.of(rec.replacePath(astPath), na.getStartPosition());
        }
      }
      if (dim == dimsSize) {
        if (na.elemtype == null) {
          System.err.println("WARNING: array initializer " + node +
              " has no explicit type; skipping insertion " + ins);
          return null;
        }
        return getBaseTypePosition(na.elemtype);
      }
      if (na.dims.size() != 0) {
        int startPos = na.getStartPosition();
        int endPos = na.getEndPosition(tree.endPositions);
        int pos = getNthInstanceInRange('[', startPos, endPos, dim + 1);
        return Pair.of(rec.replacePath(astPath), pos);
      }
      // In a situation like
      //   node=new String[][][][][]{{{}}}
      // Also see Pretty.printBrackets.
      if (dim == 0) {
        if (na.elemtype == null) {
          return Pair.of(rec.replacePath(astPath), na.getStartPosition());
        }
        // na.elemtype.getPreferredPosition(); seems to be at the end,
        //  after the brackets.
        // na.elemtype.getStartPosition(); is before the type name itself.
        int startPos = na.elemtype.getStartPosition();
        return Pair.of(rec.replacePath(astPath),
            getFirstInstanceAfter('[', startPos+1));
      } else if (dim == dimsSize) {
        return Pair.of(rec.replacePath(astPath),
            na.getType().pos().getStartPosition());
      } else {
        JCArrayTypeTree jcatt = (JCArrayTypeTree) na.elemtype;
        for (int i=1; i<dim; i++) {
          JCTree elem = jcatt.elemtype;
          if (elem.hasTag(JCTree.Tag.ANNOTATED_TYPE)) {
            elem = ((JCAnnotatedType) elem).underlyingType;
          }
          assert elem.hasTag(JCTree.Tag.TYPEARRAY);
          jcatt = (JCArrayTypeTree) elem;
        }
        return Pair.of(rec.replacePath(astPath),
            jcatt.pos().getPreferredPosition());
      }
      } else if (ASTPath.DIMENSION.equals(childSelector)) {
        List<JCExpression> inits = na.getInitializers();
        if (dim < inits.size()) {
          JCExpression expr = inits.get(dim);
          return Pair.of(astRecord(expr), expr.getStartPosition());
        }
        return null;
      } else if (ASTPath.INITIALIZER.equals(childSelector)) {
        JCExpression expr = na.getDimensions().get(dim);
        return Pair.of(astRecord(expr), expr.getStartPosition());
      } else {
        assert false : "Unexpected child selector in AST path: "
            + (childSelector == null ? "null" : "\"" + childSelector + "\"");
        return null;
      }
    }

    @Override
    public Pair<ASTRecord, Integer> visitNewClass(NewClassTree node, Insertion ins) {
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
      int result = -1;
      if (cd.mods != null
          && (cd.mods.flags != 0 || cd.mods.annotations.size() > 0)) {
        result = cd.mods.getPreferredPosition();
      }
      if (result < 0) {
        result = cd.getPreferredPosition();
      }
      assert result >= 0 || cd.name.isEmpty()
        : String.format("%d %d %d%n", cd.getStartPosition(),
                        cd.getPreferredPosition(), cd.pos);
      return result < 0 ? null : result;
    }

  }

  private final TypePositionFinder tpf;
  private final DeclarationPositionFinder dpf;
  private final JCCompilationUnit tree;
  private final SetMultimap<Pair<Integer, ASTPath>, Insertion> insertions;
  private final SetMultimap<ASTRecord, Insertion> astInsertions;

  /**
   * Creates a {@code TreeFinder} from a source tree.
   *
   * @param tree the source tree to search
   */
  public TreeFinder(JCCompilationUnit tree) {
    this.tree = tree;
    this.insertions = LinkedHashMultimap.create();
    this.astInsertions = LinkedHashMultimap.create();
    this.tpf = new TypePositionFinder();
    this.dpf = new DeclarationPositionFinder();
  }

  // which nodes are possible insertion sites
  boolean handled(Tree node) {
    switch (node.getKind()) {
    case ANNOTATION:
    case ARRAY_TYPE:
    case CLASS:
    case COMPILATION_UNIT:
    case ENUM:
    case EXPRESSION_STATEMENT:
    case EXTENDS_WILDCARD:
    case IDENTIFIER:
    case INTERFACE:
    case METHOD:
    case NEW_ARRAY:
    case NEW_CLASS:
    case PARAMETERIZED_TYPE:
    case PRIMITIVE_TYPE:
    case SUPER_WILDCARD:
    case TYPE_PARAMETER:
    case UNBOUNDED_WILDCARD:
    case VARIABLE:
      return true;
    default:
      return node instanceof ExpressionTree;
    }
  }

  /**
   * Determines if the last {@link TypePathEntry} in the given list is a
   * {@link TypePathEntryKind#WILDCARD}.
   *
   * @param location the list to check
   * @return {@code true} if the last {@link TypePathEntry} is a
   *         {@link TypePathEntryKind#WILDCARD}, {@code false} otherwise.
   */
  private boolean wildcardLast(List<TypePathEntry> location) {
    return location.get(location.size() - 1).tag == TypePathEntryKind.WILDCARD;
  }

  /**
   * Scans this tree, using the list of insertions to generate the source
   * position to insertion text mapping.  Insertions are removed from the
   * list when positions are found for them.
   *
   * @param node AST node being considered for annotation insertions
   * @param p list of insertions not yet placed
   * <p>
   * When a match is found, this routine removes the insertion from p and
   * adds it to the insertions map as a value, with a key that is a pair.
   * On return, p contains only the insertions for which no match was found.
   */
  @Override
  public Void scan(Tree node, List<Insertion> p) {
    if (node == null) {
      return null;
    }

    dbug.debug("SCANNING: %s %s (%d insertions)%n", node.getKind(), node, p.size());
    if (annotator.Main.temporaryDebug) {
      new Error("backtrace at scan()").printStackTrace();
    }
    if (! handled(node)) {
      dbug.debug("Not handled, skipping (%s): %s%n", node.getClass(), node);
      // nothing to do
      return super.scan(node, p);
    }

    TreePath path = getPath(node);
    assert path == null || path.getLeaf() == node :
      String.format("Mismatch: '%s' '%s' '%s'%n",
          path, path.getLeaf(), node);

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

    dbug.debug("Considering %d insertions.%n", p.size());
    for (Iterator<Insertion> it = p.iterator(); it.hasNext(); ) {
      Insertion i = it.next();
      dbug.debug("Considering insertion at tree:%n");
      dbug.debug("  Insertion: %s%n", i);
      dbug.debug("  First line of node: %s%n", Main.firstLine(node.toString()));
      dbug.debug("  Type of node: %s%n", node.getClass());
      if (i.isInserted()) {
        // Skip this insertion if it has already been inserted. See
        // the ReceiverInsertion class for details.
        dbug.debug("  ... already inserted%n");
        it.remove();
        continue;
      }
      if (!i.getCriteria().isSatisfiedBy(path, node)) {
        dbug.debug("  ... not satisfied%n");
        continue;
      } else {
        dbug.debug("  ... satisfied!%n");
        dbug.debug("    First line of node: %s%n", Main.firstLine(node.toString()));
        dbug.debug("    Type of node: %s%n", node.getClass());

        ASTPath astPath = i.getCriteria().getASTPath();
        Integer pos = astPath == null ? findPosition(path, i)
            : Main.convert_jaifs ? null  // already in correct form
            : findPositionByASTPath(astPath, path, i);
        if (pos != null) {
          dbug.debug("  ... satisfied! at %d for node of type %s: %s%n",
              pos, node.getClass(), Main.treeToString(node));
          insertions.put(Pair.of(pos, astPath), i);
        }
      }
      it.remove();
    }
    return super.scan(node, p);
  }

  // Find insertion position for Insertion whose criteria matched the
  // given TreePath.
  // If no position is found, report an error and return null.
  Integer findPosition(TreePath path, Insertion i) {
    Tree node = path.getLeaf();
    try {
      // As per the JSR308 specification, receiver parameters are not allowed
      // on method declarations of anonymous inner classes.
      if (i.getCriteria().isOnReceiver()
          && path.getParentPath().getParentPath().getLeaf().getKind() == Tree.Kind.NEW_CLASS) {
        warn.debug("WARNING: Cannot insert a receiver parameter "
            + "on a method declaration of an anonymous inner class.  "
            + "This insertion will be skipped.%n    Insertion: %s%n", i);
        return null;
      }

      // TODO: Find a more fine-grained replacement for the 2nd conjunct below.
      // The real issue is whether the insertion will add non-annotation code,
      // which is only sometimes the case for a TypedInsertion.
      if (alreadyPresent(path, i) && !(i instanceof TypedInsertion)) {
        // Don't insert a duplicate if this particular annotation is already
        // present at this location.
        return null;
      }

      if (i.getKind() == Insertion.Kind.CONSTRUCTOR) {
        ConstructorInsertion cons = (ConstructorInsertion) i;
        if (node.getKind() == Tree.Kind.METHOD) {
          JCMethodDecl method = (JCMethodDecl) node;
          // TODO: account for the following situation in matching phase instead
          if (method.sym.owner.isAnonymous()) { return null; }
          if ((method.mods.flags & Flags.GENERATEDCONSTR) != 0) {
            addConstructor(path, cons, method);
          } else {
            cons.setAnnotationsOnly(true);
            cons.setInserted(true);
            i = cons.getReceiverInsertion();
            if (i == null) { return null; }
          }
        } else {
          cons.setAnnotationsOnly(true);
        }
      }

      if (i.getKind() == Insertion.Kind.RECEIVER && node.getKind() == Tree.Kind.METHOD) {
        ReceiverInsertion receiver = (ReceiverInsertion) i;
        MethodTree method = (MethodTree) node;
        VariableTree rcv = method.getReceiverParameter();

        if (rcv == null) {
          addReceiverType(path, receiver, method);
        }
      }

      if (i.getKind() == Insertion.Kind.NEW && node.getKind() == Tree.Kind.NEW_ARRAY) {
        NewInsertion neu = (NewInsertion) i;
        NewArrayTree newArray = (NewArrayTree) node;

        if (newArray.toString().startsWith("{")) {
          addNewType(path, neu, newArray);
        }
      }

      // If this is a method, then it might have been selected because of
      // the receiver, or because of the return value.  Distinguish those.
      // One way would be to set a global variable here.  Another would be
      // to look for a particular different node.  I will do the latter.
      Integer pos = Position.NOPOS;

      // The insertion location is at or below the matched location
      // in the source tree.  For example, a receiver annotation
      // matches on the method and inserts on the (possibly newly
      // created) receiver.
      Map<Tree, ASTRecord> astIndex = ASTIndex.indexOf(tree);
      ASTRecord insertRecord = astIndex.get(node);
      dbug.debug("TreeFinder.scan: node=%s%n  critera=%s%n",
          node, i.getCriteria());

      if (CommonScanner.hasClassKind(node)
          && i.getCriteria().isOnTypeDeclarationExtendsClause()
          && ((ClassTree) node).getExtendsClause() == null) {
        return implicitClassBoundPosition((JCClassDecl) node, i);
      }
      if (node.getKind() == Tree.Kind.METHOD
          && i.getCriteria().isOnReturnType()) {
        JCMethodDecl jcnode = (JCMethodDecl) node;
        Tree returnType = jcnode.getReturnType();
        insertRecord = insertRecord.extend(Tree.Kind.METHOD, ASTPath.TYPE);
        if (returnType == null) {
          // find constructor name instead
          pos = findMethodName(jcnode);
          if (pos < 0) {  // skip -- inserted w/generated constructor
            return null;
          }
          dbug.debug("pos = %d at constructor name: %s%n",
              pos, jcnode.sym.toString());
        } else {
          Pair<ASTRecord, Integer> pair = tpf.scan(returnType, i);
          insertRecord = pair.a;
          pos = pair.b;
          assert handled(node);
          dbug.debug("pos = %d at return type node: %s%n",
              pos, returnType.getClass());
        }
      } else if (node.getKind() == Tree.Kind.TYPE_PARAMETER
          && i.getCriteria().onBoundZero()
          && (((TypeParameterTree) node).getBounds().isEmpty()
              || (((JCExpression) ((TypeParameterTree) node)
                      .getBounds().get(0))).type.tsym.isInterface())
          || (node instanceof WildcardTree
              && ((WildcardTree) node).getBound() == null
              && wildcardLast(i.getCriteria()
                      .getGenericArrayLocation().getLocation()))) {
        Pair<ASTRecord, Integer> pair = tpf.scan(node, i);
        insertRecord = pair.a;
        pos = pair.b;

        if (i.getKind() == Insertion.Kind.ANNOTATION) {
          if (node.getKind() == Tree.Kind.TYPE_PARAMETER
              && !((TypeParameterTree) node).getBounds().isEmpty()) {
            Tree bound = ((TypeParameterTree) node).getBounds().get(0);
            pos = ((JCExpression) bound).getStartPosition();
            ((AnnotationInsertion) i).setGenerateBound(true);
          } else {
            int limit = ((JCTree) parent(node)).getEndPosition(tree.endPositions);
            Integer nextpos1 = getNthInstanceInRange(',', pos+1, limit, 1);
            Integer nextpos2 = getNthInstanceInRange('>', pos+1, limit, 1);
            pos = (nextpos1 != Position.NOPOS && nextpos1 < nextpos2) ? nextpos1 : nextpos2;
            ((AnnotationInsertion) i).setGenerateExtends(true);
          }
        }
      } else if (i.getKind() == Insertion.Kind.CAST) {
        Type t = ((CastInsertion) i).getType();
        JCTree jcTree = (JCTree) node;
        pos = jcTree.getStartPosition();
        if (t.getKind() == Type.Kind.DECLARED) {
          DeclaredType dt = (DeclaredType) t;
          if (dt.getName().isEmpty()) {
            dt.setName(jcTree.type instanceof NullType ? "Object"
                : jcTree.type.toString());
          }
        }
      } else if (i.getKind() == Insertion.Kind.CLOSE_PARENTHESIS) {
        JCTree jcTree = (JCTree) node;
        pos = jcTree.getEndPosition(tree.endPositions);
      } else {
        boolean typeScan = true;
        if (node.getKind() == Tree.Kind.METHOD) { // MethodTree
          // looking for the receiver or the declaration
          typeScan = i.getCriteria().isOnReceiver();
        } else if (CommonScanner.hasClassKind(node)) { // ClassTree
          typeScan = ! i.isSeparateLine(); // hacky check
        }
        if (typeScan) {
          // looking for the type
          dbug.debug("Calling tpf.scan(%s: %s)%n", node.getClass(), node);
          Pair<ASTRecord, Integer> pair = tpf.scan(node, i);
          insertRecord = pair.a;
          pos = pair.b;
          assert handled(node);
          dbug.debug("pos = %d at type: %s (%s)%n", pos,
              node.toString(), node.getClass());
        } else if (node.getKind() == Tree.Kind.METHOD
            && i.getKind() == Insertion.Kind.CONSTRUCTOR
            && (((JCMethodDecl) node).mods.flags & Flags.GENERATEDCONSTR) != 0) {
          Tree parent = path.getParentPath().getLeaf();
          pos = ((JCClassDecl) parent).getEndPosition(tree.endPositions) - 1;
          insertRecord = null;  // TODO
        } else {
          // looking for the declaration
          pos = dpf.scan(node, null);
          insertRecord = astRecord(node);
          dbug.debug("pos = %s at declaration: %s%n", pos, node.getClass());
        }
      }

      if (pos != null) {
        assert pos >= 0 :
          String.format("pos: %s%nnode: %s%ninsertion: %s%n", pos, node, i);
        astInsertions.put(insertRecord, i);
      }
      return pos;
    } catch (Throwable e) {
      reportInsertionError(i, e);
      return null;
    }
  }

  // Find insertion position for Insertion whose criteria (including one
  // for the ASTPath) matched the given TreePath.
  // If no position is found, report an error and return null.
  Integer findPositionByASTPath(ASTPath astPath, TreePath path, Insertion i) {
    Tree node = path.getLeaf();
    try {
      ASTPath.ASTEntry entry = astPath.getLast();
      // As per the JSR308 specification, receiver parameters are not allowed
      // on method declarations of anonymous inner classes.
      if (entry.getTreeKind() == Tree.Kind.METHOD
          && entry.childSelectorIs(ASTPath.PARAMETER)
          && entry.getArgument() == -1
          && path.getParentPath().getParentPath().getLeaf().getKind()
          == Tree.Kind.NEW_CLASS) {
        warn.debug("WARNING: Cannot insert a receiver parameter "
            + "on a method declaration of an anonymous inner class.  "
            + "This insertion will be skipped.%n    Insertion: %s%n", i);
        return null;
      }

      if (alreadyPresent(path, i)) {
        // Don't insert a duplicate if this particular annotation is already
        // present at this location.
        return null;
      }

      if (i.getKind() == Insertion.Kind.CONSTRUCTOR) {
        ConstructorInsertion cons = (ConstructorInsertion) i;

        if (node.getKind() == Tree.Kind.METHOD) {
          JCMethodDecl method = (JCMethodDecl) node;
          if ((method.mods.flags & Flags.GENERATEDCONSTR) != 0) {
            addConstructor(path, cons, method);
          } else {
            cons.setAnnotationsOnly(true);
            cons.setInserted(true);
            i = cons.getReceiverInsertion();
            if (i == null) { return null; }
          }
        } else {
          cons.setAnnotationsOnly(true);
        }
      }

      if (i.getKind() == Insertion.Kind.RECEIVER && node.getKind() == Tree.Kind.METHOD) {
        ReceiverInsertion receiver = (ReceiverInsertion) i;
        MethodTree method = (MethodTree) node;

        if (method.getReceiverParameter() == null) {
          addReceiverType(path, receiver, method);
        }
      }

      if (i.getKind() == Insertion.Kind.NEW && node.getKind() == Tree.Kind.NEW_ARRAY) {
        NewInsertion neu = (NewInsertion) i;
        NewArrayTree newArray = (NewArrayTree) node;

        if (newArray.toString().startsWith("{")) {
          addNewType(path, neu, newArray);
        }
      }

      // If this is a method, then it might have been selected because of
      // the receiver, or because of the return value.  Distinguish those.
      // One way would be to set a global variable here.  Another would be
      // to look for a particular different node.  I will do the latter.
      Integer pos = Position.NOPOS;

      // The insertion location is at or below the matched location
      // in the source tree.  For example, a receiver annotation
      // matches on the method and inserts on the (possibly newly
      // created) receiver.
      Map<Tree, ASTRecord> astIndex = ASTIndex.indexOf(tree);
      ASTRecord insertRecord = astIndex.get(node);
      dbug.debug("TreeFinder.scan: node=%s%n  criteria=%s%n",
          node, i.getCriteria());

      if (CommonScanner.hasClassKind(node)
          && entry.childSelectorIs(ASTPath.BOUND)
          && entry.getArgument() < 0
          && ((ClassTree) node).getExtendsClause() == null) {
        return implicitClassBoundPosition((JCClassDecl) node, i);
      }
      if (node.getKind() == Tree.Kind.METHOD
          && i.getCriteria().isOnMethod("<init>()V")
          && entry.childSelectorIs(ASTPath.PARAMETER)
          && entry.getArgument() < 0) {
        if (i.getKind() != Insertion.Kind.CONSTRUCTOR) { return null; }
        Tree parent = path.getParentPath().getLeaf();
        insertRecord = insertRecord.extend(Tree.Kind.METHOD, ASTPath.PARAMETER, -1);
        pos = ((JCTree) parent).getEndPosition(tree.endPositions) - 1;
      } else if (node.getKind() == Tree.Kind.METHOD
          && entry.childSelectorIs(ASTPath.TYPE)) {
        JCMethodDecl jcnode = (JCMethodDecl) node;
        Tree returnType = jcnode.getReturnType();
        insertRecord = insertRecord.extend(Tree.Kind.METHOD, ASTPath.TYPE);
        if (returnType == null) {
          // find constructor name instead
          pos = findMethodName(jcnode);
          if (pos < 0) {  // skip -- inserted w/generated constructor
            return null;
          }
          dbug.debug("pos = %d at constructor name: %s%n",
              pos, jcnode.sym.toString());
        } else {
          Pair<ASTRecord, Integer> pair = tpf.scan(returnType, i);
          insertRecord = pair.a;
          pos = pair.b;
          assert handled(node);
          dbug.debug("pos = %d at return type node: %s%n",
              pos, returnType.getClass());
        }
      } else if (node.getKind() == Tree.Kind.TYPE_PARAMETER
          && entry.getTreeKind() == Tree.Kind.TYPE_PARAMETER  // TypeParameter.bound
          && (((TypeParameterTree) node).getBounds().isEmpty()
              || (((JCExpression) ((TypeParameterTree) node)
                      .getBounds().get(0))).type.tsym.isInterface())
          || ASTPath.isWildcard(node.getKind())
          && (entry.getTreeKind() == Tree.Kind.TYPE_PARAMETER
              || ASTPath.isWildcard(entry.getTreeKind()))
          && entry.childSelectorIs(ASTPath.BOUND)
          && (!entry.hasArgument() || entry.getArgument() == 0)) {
        Pair<ASTRecord, Integer> pair = tpf.scan(node, i);
        insertRecord = pair.a;
        pos = pair.b;

        if (i.getKind() == Insertion.Kind.ANNOTATION) {
          if (node.getKind() == Tree.Kind.TYPE_PARAMETER
              && !((TypeParameterTree) node).getBounds().isEmpty()) {
            Tree bound = ((TypeParameterTree) node).getBounds().get(0);
            pos = ((JCExpression) bound).getStartPosition();
            ((AnnotationInsertion) i).setGenerateBound(true);
          } else {
            int limit = ((JCTree) parent(node)).getEndPosition(tree.endPositions);
            Integer nextpos1 = getNthInstanceInRange(',', pos+1, limit, 1);
            Integer nextpos2 = getNthInstanceInRange('>', pos+1, limit, 1);
            pos = (nextpos1 != Position.NOPOS && nextpos1 < nextpos2) ? nextpos1 : nextpos2;
            ((AnnotationInsertion) i).setGenerateExtends(true);
          }
        }
      } else if (i.getKind() == Insertion.Kind.CAST) {
        Type t = ((CastInsertion) i).getType();
        JCTree jcTree = (JCTree) node;
        if (jcTree.getKind() == Tree.Kind.VARIABLE && !astPath.isEmpty()
            && astPath.getLast().childSelectorIs(ASTPath.INITIALIZER)) {
          node = ((JCVariableDecl) node).getInitializer();
          if (node == null) { return null; }
          jcTree = (JCTree) node;
        }
        pos = jcTree.getStartPosition();
        if (t.getKind() == Type.Kind.DECLARED) {
          DeclaredType dt = (DeclaredType) t;
          if (dt.getName().isEmpty()) {
              if (jcTree.type instanceof NullType) {
                dt.setName("Object");
              } else {
                t = Insertions.TypeTree.javacTypeToType(jcTree.type);
                t.setAnnotations(dt.getAnnotations());
                ((CastInsertion) i).setType(t);
              }
          }
        }
      } else if (i.getKind() == Insertion.Kind.CLOSE_PARENTHESIS) {
        JCTree jcTree = (JCTree) node;
        if (jcTree.getKind() == Tree.Kind.VARIABLE && !astPath.isEmpty()
            && astPath.getLast().childSelectorIs(ASTPath.INITIALIZER)) {
          node = ((JCVariableDecl) node).getInitializer();
          if (node == null) { return null; }
          jcTree = (JCTree) node;
        }
        pos = jcTree.getEndPosition(tree.endPositions);
      } else {
        boolean typeScan = true;
        if (node.getKind() == Tree.Kind.METHOD) { // MethodTree
          // looking for the receiver or the declaration
          typeScan = IndexFileSpecification.isOnReceiver(i.getCriteria());
        } else if (node.getKind() == Tree.Kind.CLASS) { // ClassTree
          typeScan = ! i.isSeparateLine(); // hacky check
        }
        if (typeScan) {
          // looking for the type
          dbug.debug("Calling tpf.scan(%s: %s)%n", node.getClass(), node);
          Pair<ASTRecord, Integer> pair = tpf.scan(node, i);
          insertRecord = pair.a;
          pos = pair.b;
          assert handled(node);
          dbug.debug("pos = %d at type: %s (%s)%n",
              pos, node.toString(), node.getClass());
        } else if (node.getKind() == Tree.Kind.METHOD
            && i.getKind() == Insertion.Kind.CONSTRUCTOR
            && (((JCMethodDecl) node).mods.flags & Flags.GENERATEDCONSTR) != 0) {
          Tree parent = path.getParentPath().getLeaf();
          pos = ((JCClassDecl) parent).getEndPosition(tree.endPositions) - 1;
          insertRecord = null;  // TODO
        } else {
          // looking for the declaration
          pos = dpf.scan(node, null);
          insertRecord = astRecord(node);
          assert pos != null;
          dbug.debug("pos = %d at declaration: %s%n", pos, node.getClass());
        }
      }

      if (pos != null) {
        assert pos >= 0 :
          String.format("pos: %s%nnode: %s%ninsertion: %s%n", pos, node, i);
        astInsertions.put(insertRecord, i);
      }
      return pos;
    } catch (Throwable e) {
      reportInsertionError(i, e);
      return null;
    }
  }

  private Integer implicitClassBoundPosition(JCClassDecl cd, Insertion i) {
    Integer pos;
    if (cd.sym == null || cd.sym.isAnonymous()
        || i.getKind() != Insertion.Kind.ANNOTATION) {
      return null;
    }
    JCModifiers mods = cd.getModifiers();
    String name = cd.getSimpleName().toString();
    if (cd.typarams == null || cd.typarams.isEmpty()) {
      int start = cd.getStartPosition();
      int offset = Math.max(start,
          mods.getEndPosition(tree.endPositions) + 1);
      String s = cd.toString().substring(offset - start);
      Pattern p = Pattern.compile("(?:\\s|" + comment
          + ")*+class(?:\\s|" + comment
          + ")++" + Pattern.quote(name) + "\\b");
      Matcher m = p.matcher(s);
      if (!m.find() || m.start() != 0) { return null; }
      pos = offset + m.end() - 1;
    } else {  // generic class
      JCTypeParameter param = cd.typarams.get(cd.typarams.length()-1);
      int start = param.getEndPosition(tree.endPositions);
      pos = getFirstInstanceAfter('>', start) + 1;
    }
    ((AnnotationInsertion) i).setGenerateExtends(true);
    return pos;
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
    JCBlock body = node.body;
    if ((mods.flags & Flags.GENERATEDCONSTR) != 0) { return Position.NOPOS; }
    int nodeStart = node.getStartPosition();
    int nodeEnd = node.getEndPosition(tree.endPositions);
    int nodeLength = nodeEnd - nodeStart;
    int modsLength = mods.getEndPosition(tree.endPositions)
        - mods.getStartPosition();  // can't trust string length!
    int bodyLength = body == null ? 1
        : body.getEndPosition(tree.endPositions) - body.getStartPosition();
    int start = nodeStart + modsLength;
    int end = nodeStart + nodeLength - bodyLength;
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
   * @param path the location in the AST to check for the annotation
   * @param ins the annotation to check for
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
          break;
        } else if (n.getKind() == Tree.Kind.ARRAY_TYPE) {
          Tree type = ((ArrayTypeTree) n).getType();
          if (type.getKind() == Tree.Kind.ANNOTATED_TYPE) {
            alreadyPresent = ((AnnotatedTypeTree) type).getAnnotations();
          }
          break;
        } else if (n.getKind() == Tree.Kind.ANNOTATED_TYPE) {
          alreadyPresent = ((AnnotatedTypeTree) n).getAnnotations();
          break;
        }
        // TODO: don't add cast insertion if it's already present.
      }
    }

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
        // strip off leading @ along w/any leading or trailing whitespace
        String text = ins.getText();
        String iann = Main.removeArgs(text).a.trim()
            .substring(text.startsWith("@") ? 1 : 0);
        String iannNoPackage = Insertion.removePackage(iann).b;
        // System.out.printf("Comparing: %s %s %s%n", ann, iann, iannNoPackage);
        if (ann.equals(iann) || ann.equals(iannNoPackage)) {
          dbug.debug("Already present, not reinserting: %s%n", ann);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Reports an error inserting an insertion to {@code System.err}.
   * @param i the insertion that caused the error
   * @param e the error. If there's a message it will be printed.
   */
  public static void reportInsertionError(Insertion i, Throwable e) {
    System.err.println("Error processing insertion:");
    System.err.println("\t" + i);
    if (e.getMessage() != null) {
      // If the message has multiple lines, indent them so it's easier to read.
      System.err.println("\tError: " + e.getMessage().replace("\n", "\n\t\t"));
    }
    if (dbug.or(stak).isEnabled()) {
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
   * @param path the location in the AST to insert the receiver
   * @param receiver details of the receiver to insert
   * @param method the method the receiver is being inserted into
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
    Tree.Kind kind = leaf.getKind();
    // This is the outermost type, currently containing only the
    // annotation to add to the receiver.
    Type outerType = receiver.getType();
    DeclaredType baseType = receiver.getBaseType();
    // This holds the inner types as they're being read in.
    DeclaredType innerTypes = null;
    DeclaredType staticType = null;
    // For an inner class constructor, the receiver comes from the
    // superclass, so skip past the first type definition.
    boolean isCon = ((MethodTree) parent.getLeaf()).getReturnType() == null;
    boolean skip = isCon;

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
          receiver.setQualifyType(true);
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
    if (isCon && innerTypes == null) {
      throw new IllegalArgumentException(
          "can't annotate (non-existent) receiver of non-inner constructor");
    }

    // Merge innerTypes into outerType: outerType only has the annotations
    // on the receiver, while innerTypes has everything else. innerTypes can
    // have the annotations if it is a static class.
    baseType.setName(innerTypes.getName());
    baseType.setTypeParameters(innerTypes.getTypeParameters());
    baseType.setInnerType(innerTypes.getInnerType());
    if (staticType != null && !innerTypes.getAnnotations().isEmpty()) {
      outerType.setAnnotations(innerTypes.getAnnotations());
    }

    Type type = (staticType == null) ? baseType : staticType;
    Insertion.decorateType(receiver.getInnerTypeInsertions(), type,
        receiver.getCriteria().getASTPath());

    // If the method doesn't have parameters, don't add a comma.
    receiver.setAddComma(method.getParameters().size() > 0);
  }

  private void addNewType(TreePath path, NewInsertion neu,
      NewArrayTree newArray) {
    DeclaredType baseType = neu.getBaseType();
    if (baseType.getName().isEmpty()) {
      List<String> annotations = neu.getType().getAnnotations();
      Type newType = Insertions.TypeTree.javacTypeToType(
          ((JCTree.JCNewArray) newArray).type);
      for (String ann : annotations) {
        newType.addAnnotation(ann);
      }
      neu.setType(newType);
    }
    Insertion.decorateType(neu.getInnerTypeInsertions(), neu.getType(),
        neu.getCriteria().getASTPath());
  }

  private void addConstructor(TreePath path, ConstructorInsertion cons,
      MethodTree method) {
    ReceiverInsertion recv = cons.getReceiverInsertion();
    MethodTree leaf = (MethodTree) path.getLeaf();
    ClassTree parent = (ClassTree) path.getParentPath().getLeaf();
    DeclaredType baseType = cons.getBaseType();
    if (baseType.getName().isEmpty()) {
      List<String> annotations = baseType.getAnnotations();
      String className = parent.getSimpleName().toString();
      Type newType = new DeclaredType(className);
      cons.setType(newType);
      for (String ann : annotations) {
        newType.addAnnotation(ann);
      }
    }
    if (recv != null) {
      Iterator<Insertion> iter = cons.getInnerTypeInsertions().iterator();
      List<Insertion> recvInner = new ArrayList<Insertion>();
      addReceiverType(path, recv, leaf);
      while (iter.hasNext()) {
        Insertion i = iter.next();
        if (i.getCriteria().isOnReceiver()) {
          recvInner.add(i);
          iter.remove();
        }
      }
      Insertion.decorateType(recvInner, recv.getType(),
          cons.getCriteria().getASTPath());
    }
    Insertion.decorateType(cons.getInnerTypeInsertions(), cons.getType(),
        cons.getCriteria().getASTPath());
  }

  public SetMultimap<ASTRecord, Insertion> getPaths() {
    return Multimaps.unmodifiableSetMultimap(astInsertions);
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
  public SetMultimap<Pair<Integer, ASTPath>, Insertion>
  getInsertionsByPosition(JCCompilationUnit node, List<Insertion> p) {
    List<Insertion> uninserted = new ArrayList<Insertion>(p);
    this.scan(node, uninserted);
    // There may be many extra annotations in a .jaif file.  For instance,
    // the .jaif file may be for an entire library, but its compilation
    // units are processed one by one.
    // However, we should warn about any insertions that were within the
    // given compilation unit but still didn't get inserted.
    List<? extends Tree> typeDecls = node.getTypeDecls();
    for (Insertion i : uninserted) {
      InClassCriterion c = i.getCriteria().getInClass();
      if (c == null) {
        continue;
      }
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
    if (dbug.isEnabled()) {
      // Output every insertion that was not given a position:
      for (Insertion i : uninserted) {
        System.err.println("Unable to insert: " + i);
      }
    }
    dbug.debug("getPositions => %d positions%n", insertions.size());
    return Multimaps.unmodifiableSetMultimap(insertions);
  }

  /**
   * Scans the given tree with the given {@link Insertions} and returns
   * the mapping from source position to insertion text.
   *
   * <p>
   * <i>N.B.:</i> This method calls {@code scan()} internally.
   * </p>
   *
   * @param node the tree to scan
   * @param insertions the insertion criteria
   * @return the source position to insertion text mapping
   */
  public SetMultimap<Pair<Integer, ASTPath>, Insertion>
  getPositions(JCCompilationUnit node, Insertions insertions) {
    List<Insertion> list = new ArrayList<Insertion>();
    treePathCache.clear();
    if (annotator.Main.temporaryDebug) {
      System.out.println("insertions size: " + insertions.size());
      System.out.println("insertions.forOuterClass(\"\") size: " + insertions.forOuterClass(node, "").size());
      System.out.println("list pre-size: " + list.size());
    }
    list.addAll(insertions.forOuterClass(node, ""));
    if (annotator.Main.temporaryDebug) {
      System.out.println("list post-size: " + list.size());
    }
    for (JCTree decl : node.getTypeDecls()) {
      if (decl.getTag() == JCTree.Tag.CLASSDEF) {
        String name = ((JCClassDecl) decl).sym.className();
        Collection<Insertion> forClass = insertions.forOuterClass(node, name);
        if (annotator.Main.temporaryDebug) {
          System.out.println("insertions size: " + insertions.size());
          System.out.println("insertions.forOuterClass("+name+") size: " + forClass.size());
          System.out.println("list pre-size: " + list.size());
        }
        list.addAll(forClass);
        if (annotator.Main.temporaryDebug) {
          System.out.println("list post-size: " + list.size());
        }
      }
    }
    return getInsertionsByPosition(node, list);
  }
}
