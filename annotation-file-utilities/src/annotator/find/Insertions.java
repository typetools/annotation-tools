package annotator.find;

import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.lang.model.element.Name;
import javax.lang.model.type.TypeKind;

import annotations.el.InnerTypeLocation;
import annotations.io.ASTIndex;
import annotations.io.ASTPath;
import annotations.io.ASTRecord;
import type.*;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;
import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntryKind;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.util.Pair;

/**
 * @author dbro
 *
 * An indexed collection (though not {@link java.util.Collection}, only
 * {@link java.lang.Iterable}) of {@link Insertion}s with methods to
 * select those specified for a given class or for an outer class along
 * with its local classes.  This class is especially useful when a
 * single JAIF stores annotations for many source files, as it reduces
 * the number of insertions to be considered for any AST node.
 *
 * The class now serves a second purpose, which should probably be
 * separated out (according to OO dogma, at least): It attaches
 * {@link annotations.io.ASTPath}-based inner type {@link Insertion}s to
 * a {@link TypedInsertion} on the outer type if one exists (see
 * {@link #organizeTypedInsertions(CompilationUnitTree, String, Collection)}.
 * Since getting these insertions right depends on this organization,
 * this class is now essential for correctness, not merely for
 * performance.
 */
public class Insertions implements Iterable<Insertion> {
  private static int kindLevel(Insertion i) {
    // ordered so insertion that depends on another gets inserted after other
    switch (i.getKind()) {
    case CONSTRUCTOR:
      return 3;
    case NEW:
    case RECEIVER:
      return 2;
    case CAST:
        return 1;
    //case ANNOTATION:
    //case CLOSE_PARENTHESIS:
    default:
      return 0;
    }
  }

  private static final Comparator<Insertion> byASTRecord =
      new Comparator<Insertion>() {
        @Override
        public int compare(Insertion o1, Insertion o2) {
          Criteria c1 = o1.getCriteria();
          Criteria c2 = o2.getCriteria();
          ASTPath p1 = c1.getASTPath();
          ASTPath p2 = c2.getASTPath();
          ASTRecord r1 = new ASTRecord(null,
              c1.getClassName(), c1.getMethodName(), c1.getFieldName(),
              p1 == null ? ASTPath.empty() : p1);
          ASTRecord r2 = new ASTRecord(null,
              c2.getClassName(), c2.getMethodName(), c2.getFieldName(),
              p2 == null ? ASTPath.empty() : p2);
          int c = r1.compareTo(r2);
          if (c == 0) {
            //c = o1.getKind().compareTo(o2.getKind());
            c = Integer.compare(kindLevel(o2), kindLevel(o1));  // descending
            if (c == 0) { c = o1.toString().compareTo(o2.toString()); }
          }
          return c;
        }
      };

  // store indexes insertions by (qualified) outer class name and inner
  // class path (if any)
  private Map<String, Map<String, Set<Insertion>>> store;
  private int size;

  private Pair<String, String> nameSplit(String name) {
    int i = name.indexOf('$');  // FIXME: don't split on '$' in source
    return i < 0
        ? Pair.of(name, "")
        : Pair.of(name.substring(0, i), name.substring(i));
  }

  public Insertions() {
    store = new HashMap<String, Map<String, Set<Insertion>>>();
    size = 0;
  }

  // auxiliary for following two methods
  private void forClass(CompilationUnitTree cut,
      String qualifiedClassName, Set<Insertion> result) {
    Pair<String, String> pair = nameSplit(qualifiedClassName);
    Map<String, Set<Insertion>> map = store.get(pair.fst);
    if (map != null) {
      Set<Insertion> set = new TreeSet<Insertion>(byASTRecord);
      set.addAll(map.get(pair.snd));
      if (set != null) {
        set = organizeTypedInsertions(cut, qualifiedClassName, set);
        result.addAll(set);
      }
    }
  }

  /**
   * Selects {@link Insertion}s relevant to a given class.
   *
   * @param cut the current compilation unit
   * @param qualifiedClassName the fully qualified class name
   * @return {@link java.util.Set} of {@link Insertion}s with an
   *          {@link InClassCriterion} for the given class
   */
  public Set<Insertion> forClass(CompilationUnitTree cut,
      String qualifiedClassName) {
    Set<Insertion> set = new LinkedHashSet<Insertion>();
    forClass(cut, qualifiedClassName, set);
    return set;
  }

  /**
   * Selects {@link Insertion}s relevant to a given outer class and its
   * local classes.
   *
   * @param cut the current compilation unit
   * @param qualifiedOuterClassName the fully qualified outer class name
   * @return {@link java.util.Set} of {@link Insertion}s with an
   *          {@link InClassCriterion} for the given outer class or one
   *          of its local classes
   */
  public Set<Insertion> forOuterClass(CompilationUnitTree cut,
      String qualifiedOuterClassName) {
    Map<String, Set<Insertion>> map = store.get(qualifiedOuterClassName);
    if (map == null || map.isEmpty()) {
      return Collections.<Insertion>emptySet();
    } else {
      Set<Insertion> set = new LinkedHashSet<Insertion>();
      for (String key : map.keySet()) {
        String qualifiedClassName = qualifiedOuterClassName + key;
        forClass(cut, qualifiedClassName, set);
      }
      return set;
    }
  }

  /**
   * Add an {@link Insertion} to this collection.
   */
  public void add(Insertion ins) {
    InClassCriterion icc = ins.getCriteria().getInClass();
    String k1 = "";
    String k2 = "";
    Map<String, Set<Insertion>> map;
    Set<Insertion> set;

    if (icc != null) {
      Pair<String, String> triple = nameSplit(icc.className);
      k1 = triple.fst;
      k2 = triple.snd;
    }
    map = store.get(k1);
    if (map == null) {
      map = new HashMap<String, Set<Insertion>>();
      store.put(k1, map);
    }
    set = map.get(k2);
    if (set == null) {
      set = new LinkedHashSet<Insertion>();
      map.put(k2, set);
    }

    size -= set.size();
    set.add(ins);
    size += set.size();
  }

  /**
   * Add all {@link Insertion}s in the given
   * {@link java.util.Collection} to this collection.
   */
  public void addAll(Collection<? extends Insertion> c) {
    for (Insertion ins : c) {
      add(ins);
    }
  }

  /**
   * Returns the number of {@link Insertion}s in this collection.
   */
  public int size() {
    return size;
  }

  @Override
  public Iterator<Insertion> iterator() {
    return new Iterator<Insertion>() {
      private Iterator<Map<String, Set<Insertion>>> miter =
          store.values().iterator();
      private Iterator<Set<Insertion>> siter =
          Collections.<Set<Insertion>>emptySet().iterator();
      private Iterator<Insertion> iiter =
          Collections.<Insertion>emptySet().iterator();

      @Override
      public boolean hasNext() {
        if (iiter.hasNext()) { return true; }
        while (siter.hasNext()) {
          iiter = siter.next().iterator();
          if (iiter.hasNext()) { return true; }
        }
        while (miter.hasNext()) {
          siter = miter.next().values().iterator();
          while (siter.hasNext()) {
            iiter = siter.next().iterator();
            if (iiter.hasNext()) { return true; }
          }
        }
        return false;
      }

      @Override
      public Insertion next() {
        if (hasNext()) { return iiter.next(); }
        throw new NoSuchElementException();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Returns a {@link java.util.List} containing all {@link Insertion}s
   * in this collection.
   */
  public List<Insertion> toList() {
    List<Insertion> list = new ArrayList<Insertion>(size);
    for (Insertion ins : this) { list.add(ins); }
    return null;
  }

  /*
   * This method detects inner type relationships among ASTPath-based
   * insertion specifications and organizes the insertions accordingly.
   * This step is necessary because 1) insertion proceeds from the end to
   * the beginning of the source and 2) the insertion location does not
   * always exist prior to the top-level type insertion.
   */
  private Set<Insertion> organizeTypedInsertions(CompilationUnitTree cut,
      String className, Collection<Insertion> insertions) {
    ASTRecordMap<TypedInsertion> map = new ASTRecordMap<TypedInsertion>();
    Set<Insertion> organized = new LinkedHashSet<Insertion>();
    Set<Insertion> unorganized = new LinkedHashSet<Insertion>();
    List<Insertion> list = new ArrayList<Insertion>();

    // First divide the insertions into three buckets: TypedInsertions
    // on outer types (map), ASTPath-based insertions on local types
    // (unorganized -- built as list and then sorted, since building as
    // a set spuriously removes "duplicates" according to the
    // comparator), and everything else (organized -- where all
    // eventually land).
    for (Insertion ins : insertions) {
      if (ins.getInserted()) { continue; }
      Criteria criteria = ins.getCriteria();
      GenericArrayLocationCriterion galc =
          criteria.getGenericArrayLocation();
      ASTPath p = criteria.getASTPath();
      if (p == null || p.isEmpty()
          || galc != null && !galc.getLocation().isEmpty()
          || ins instanceof CastInsertion
          || ins instanceof CloseParenthesisInsertion) {
        organized.add(ins);
      } else {
        ASTRecord rec = new ASTRecord(cut, criteria.getClassName(),
            criteria.getMethodName(), criteria.getFieldName(), p);
        ASTPath.ASTEntry entry = rec.astPath.get(-1);
        Tree node;

        if (entry.getTreeKind() == Tree.Kind.NEW_ARRAY
            && entry.childSelectorIs(ASTPath.TYPE)
            && entry.getArgument() == 0) {
          ASTPath temp = rec.astPath.getParentPath();
          node = ASTIndex.getNode(cut, rec.replacePath(temp));
          node = node instanceof JCTree.JCNewArray
              ? TypeTree.fromType(((JCTree.JCNewArray) node).type)
              : null;
        } else {
          node = ASTIndex.getNode(cut, rec);
        }

        if (ins instanceof TypedInsertion) {
          TypedInsertion tins = map.get(rec);
          if (ins instanceof NewInsertion) {
            NewInsertion nins = (NewInsertion) ins;
            if (entry.getTreeKind() == Tree.Kind.NEW_ARRAY
                && entry.childSelectorIs(ASTPath.TYPE)) {
              int a = entry.getArgument();
              List<TypePathEntry> loc0 = new ArrayList<TypePathEntry>(a);
              ASTRecord rec0 = null;
              if (a == 0) {
                rec0 = rec.replacePath(p.getParentPath());
                Tree t = ASTIndex.getNode(cut, rec0);
                if (t == null || t.toString().startsWith("{")) {
                  rec0 = null;
                } else {
                  rec = rec0;
                  rec0 = rec.extend(Tree.Kind.NEW_ARRAY,
                      ASTPath.TYPE, 0);
                }
              } else if (node != null
                  && !nins.getInnerTypeInsertions().isEmpty()) {
                if (node.getKind() == Tree.Kind.IDENTIFIER) {
                  node = ASTIndex.getNode(cut,
                      rec.replacePath(p.getParentPath()));
                }
                if ((node.getKind() == Tree.Kind.NEW_ARRAY
                    || node.getKind() == Tree.Kind.ARRAY_TYPE)
                    && !node.toString().startsWith("{")) {
                  rec = rec.replacePath(p.getParentPath());

                  Collections.fill(loc0, TypePathEntry.ARRAY);
                  //irec = rec;
                  //if (node.getKind() == Tree.Kind.NEW_ARRAY) {
                  rec0 = rec.extend(Tree.Kind.NEW_ARRAY,
                      ASTPath.TYPE, 0);
                  //}
                }
              }

              if (rec0 != null) {
                for (Insertion inner : nins.getInnerTypeInsertions()) {
                  Criteria icriteria = inner.getCriteria();
                  GenericArrayLocationCriterion igalc =
                      icriteria.getGenericArrayLocation();
                  if (igalc != null) {
                    ASTRecord rec1;
                    int b = igalc.getLocation().size();
                    List<TypePathEntry> loc =
                        new ArrayList<TypePathEntry>(a + b);
                    loc.addAll(loc0);
                    loc.addAll(igalc.getLocation());
                    rec1 = extendToInnerType(rec0, loc, node);
                    icriteria.add(new GenericArrayLocationCriterion());
                    icriteria.add(new ASTPathCriterion(rec1.astPath));
                    inner.setInserted(false);
                    organized.add(inner);
                  }
                }
                nins.getInnerTypeInsertions().clear();
              }
            }
          }
          if (tins == null) {
            map.put(rec, (TypedInsertion) ins);
          } else if (tins.getType().equals(((TypedInsertion) ins).getType())) {
            mergeTypedInsertions(tins, (TypedInsertion) ins);
          }
        } else {
          int d = newArrayInnerTypeDepth(p);
          if (d > 0) {
            ASTPath temp = p;
            while (!temp.isEmpty()
                && (node == null || node.getKind() != Tree.Kind.NEW_ARRAY)) {
              // TODO: avoid repeating work of newArrayInnerTypeDepth()
              temp = temp.getParentPath();
              node = ASTIndex.getNode(cut, rec.replacePath(temp));
            }
            if (node == null) {
              // TODO: ???
            }
            temp = temp.extend(
                new ASTPath.ASTEntry(Tree.Kind.NEW_ARRAY, ASTPath.TYPE, 0));
            if (node.toString().startsWith("{")) {
              TypedInsertion tins = map.get(rec.replacePath(temp));
              if (tins == null) {
                // TODO
              } else {
                tins.getInnerTypeInsertions().add(ins);
                ins.setInserted(true);
              }
            } else {
              List<? extends ExpressionTree> dims =
                  ((NewArrayTree) node).getDimensions();
              ASTRecord irec = rec.replacePath(p.getParentPath())
                  .extend(Tree.Kind.NEW_ARRAY, ASTPath.TYPE, 0);
              GenericArrayLocationCriterion igalc =
                  criteria.getGenericArrayLocation();
              for (int i = 0 ; i < d; i++) {
                irec = irec.extend(Tree.Kind.ARRAY_TYPE, ASTPath.TYPE);
              }
              if (igalc != null) {
                List<TypePathEntry> loc = igalc.getLocation();
                if (!loc.isEmpty()) {
                  try {
                    Tree dim = dims.get(d-1);
                    irec = extendToInnerType(irec, loc, dim);
                    criteria.add(new ASTPathCriterion(irec.astPath));
                    criteria.add(new GenericArrayLocationCriterion());
                  } catch (RuntimeException e) {}
                }
              }
            }
          }
          list.add(ins);
        }
      }
    }
    //if (map.isEmpty()) {
    //  organized.addAll(unorganized);
    //  return organized;
    //}
    Collections.sort(list, byASTRecord);
    unorganized.addAll(list);

    // Each Insertion in unorganized gets attached to a TypedInsertion
    // in map if possible; otherwise, it gets dumped into organized.
    for (Insertion ins : unorganized) {
      Criteria criteria = ins.getCriteria();
      String methodName = criteria.getMethodName();
      String fieldName = criteria.getFieldName();
      ASTPath ap1 = criteria.getASTPath();
      List<TypePathEntry> tpes = new ArrayList<TypePathEntry>();
      if (ap1 == null) {
          // || methodName == null && fieldName == null)
        organized.add(ins);
        continue;
      }

      // First find the relevant "top-level" insertion, if any.
      // ap0: path to top-level type; ap1: path to local type
      ASTRecord rec;
      Tree.Kind kind;
      Deque<ASTPath> astack = new ArrayDeque<ASTPath>(ap1.size());
      ASTPath ap0 = ap1;
      do {
        astack.push(ap0);
        ap0 = ap0.getParentPath();
      } while (!ap0.isEmpty());
      do {
        ap0 = astack.pop();
        kind = ap0.get(-1).getTreeKind();
        rec = new ASTRecord(cut, className, methodName, fieldName, ap0);
      } while (!(astack.isEmpty() || map.containsKey(rec)));

      TypedInsertion tins = map.get(rec);
      TreePath path = ASTIndex.getTreePath(cut, rec);
      Tree node = path == null ? null : path.getLeaf();
      if (node == null && ap0.isEmpty()) {
        organized.add(ins);
        continue;
      }

      // Try to create a top-level insertion if none exists (e.g., if
      // there is an insertion for NewArray.type 1 but not for 0).
      if (tins == null) {
        GenericArrayLocationCriterion galc =
            criteria.getGenericArrayLocation();
        if (node == null) {
          // TODO: figure out from rec?
          organized.add(ins);
          continue;
        } else {
          Tree t = path.getLeaf();
          switch (t.getKind()) {
          case NEW_ARRAY:
            int d = 0;
            ASTPath.ASTEntry e = ap1.get(-1);
            List<TypePathEntry> loc = null;
            List<Insertion> inners = new ArrayList<Insertion>();
            Type type = TypeTree.conv(((JCTree.JCNewArray) t).type);
            if (e.getTreeKind() == Tree.Kind.NEW_ARRAY) {
              d += e.getArgument();
            }
            if (galc != null) {
              loc = galc.getLocation();
              int n = loc.size();
              while (--n >= 0 && loc.get(n).tag == TypePathEntryKind.ARRAY) {
                ++d;
              }
              loc = n < 0 ? null : loc.subList(0, ++n);
            }
            criteria.add(new ASTPathCriterion(
                rec.astPath.getParentPath().extendNewArray(d)));
            criteria.add(loc == null || loc.isEmpty()
                ? new GenericArrayLocationCriterion()
                : new GenericArrayLocationCriterion(
                    new InnerTypeLocation(loc)));
            inners.add(ins);
            tins = new NewInsertion(type, criteria, inners);
            tins.setInserted(true);
            map.put(rec, tins);
            break;
          default:
            break;
          }
          path = path.getParentPath();
        }
      }

      // The sought node may or may not be found in the tree; if not, it
      // may need to be created later.  Use whatever part of the path
      // exists already to distinguish MEMBER_SELECT nodes that indicate
      // qualifiers from those that indicate local types.  Assume any
      // MEMBER_SELECTs in the AST path that don't correspond to
      // existing nodes are part of a type use.
      if (node == null) {
        ASTPath ap = ap0;
        if (!ap.isEmpty()) {
          do {
            ap = ap.getParentPath();
            node = ASTIndex.getNode(cut, rec.replacePath(ap));
          } while (node == null && !ap.isEmpty());
        }
        if (node == null) {
          organized.add(ins);
          continue;
        }

        // find actual type
        ClassSymbol csym = null;
        switch (tins.getKind()) {
        case CONSTRUCTOR:
          if (node instanceof JCTree.JCMethodDecl) {
            MethodSymbol msym = ((JCTree.JCMethodDecl) node).sym;
            csym = (ClassSymbol) msym.owner;
            node = TypeTree.fromType(csym.type);
            break;
          } else if (node instanceof JCTree.JCClassDecl) {
            csym = ((JCTree.JCClassDecl) node).sym;
            if (csym.owner instanceof ClassSymbol) {
              csym = (ClassSymbol) csym.owner;
              node = TypeTree.fromType(csym.type);
              break;
            }
          }
          throw new RuntimeException();
        case NEW:
          if (node instanceof JCTree.JCNewArray) {
            if (node.toString().startsWith("{")) {
              node = TypeTree.fromType(((JCTree.JCNewArray) node).type);
              break;
            } else {
              organized.add(ins);
              continue;
            }
          }
          throw new RuntimeException();
        case RECEIVER:
          if (node instanceof JCTree.JCMethodDecl) {
            JCTree.JCMethodDecl jmd = (JCTree.JCMethodDecl) node;
            csym = (ClassSymbol) jmd.sym.owner;
            if ("<init>".equals(jmd.name.toString())) {
              csym = (ClassSymbol) csym.owner;
            }
          } else if (node instanceof JCTree.JCClassDecl) {
            csym = ((JCTree.JCClassDecl) node).sym;
          }
          if (csym != null) {
            node = TypeTree.fromType(csym.type);
            break;
          }
          throw new RuntimeException();
        default:
          throw new RuntimeException();
        }
      }

      /*
       * Inner types require special consideration due to the
       * structural differences between an AST that represents a type
       * (subclass of com.sun.source.Tree) and the type's logical
       * representation (subclass of type.Type).  The differences are
       * most prominent in the case of a type with a parameterized
       * local type.  For example, the AST for A.B.C<D> looks like
       * this:
       *
       *                     ParameterizedType
       *                    /                 \
       *               MemberSelect       Identifier
       *              /            \           |
       *        MemberSelect      (Name)       D
       *         /      \           |
       *  Identifier   (Name)       C
       *        |        |
       *        A        B
       *
       * (Technically, the Names are not AST nodes but rather
       * attributes of their parent MemberSelect nodes.)  The logical
       * representation seems more intuitive:
       *
       *       DeclaredType
       *      /     |      \
       *    Name  Params  Inner
       *     |      |       |
       *     A      -  DeclaredType
       *              /     |      \
       *            Name  Params  Inner
       *             |      |       |
       *             B      -  DeclaredType
       *                      /     |      \
       *                    Name  Params  Inner
       *                     |      |       |
       *                     C      D       -
       *
       * The opposing "chirality" of local type nesting means that the
       * usual recursive descent strategy doesn't work for finding a
       * logical type path in an AST; in effect, local types have to
       * be "turned inside-out".
       *
       * Worse yet, the actual tree structure may not exist in the tree!
       * It is possible to recover the actual type from the symbol
       * table, but the methods to create AST nodes are not visible
       * here.  Hence, the conversion relies on custom implementations
       * of the interfaces in com.sun.source.tree.Tree, which are
       * defined in the local class TypeTree.
       */
      int i = ap0.size();
      int n = ap1.size();
      int actualDepth = 0;  // inner type levels seen
      int expectedDepth = 0;  // inner type levels anticipated

      // skip any declaration nodes
      while (i < n) {
        ASTPath.ASTEntry entry = ap1.get(i);
        kind = entry.getTreeKind();
        if (kind != Tree.Kind.METHOD && kind != Tree.Kind.VARIABLE) {
          break;
        }
        ++i;
      }

      // now build up the type path in JVM's format
      while (i < n) {
        ASTPath.ASTEntry entry = ap1.get(i);
        rec = rec.extend(entry);
        kind = entry.getTreeKind();

        while (node.getKind() == Tree.Kind.ANNOTATED_TYPE) {  // skip
          node = ((AnnotatedTypeTree) node).getUnderlyingType();
        }
        if (expectedDepth == 0) {
          expectedDepth = localDepth(node);
        }

        switch (kind) {
        case ARRAY_TYPE:
          if (expectedDepth == 0 && node.getKind() == kind) {
            node = ((ArrayTypeTree) node).getType();
            while (--actualDepth >= 0) {
              tpes.add(TypePathEntry.INNER_TYPE);
            }
            tpes.add(TypePathEntry.ARRAY);
            break;
          }
          throw new RuntimeException();

        case MEMBER_SELECT:
          if (--expectedDepth >= 0) {  // otherwise, shouldn't have MEMBER_SELECT
            node = ((MemberSelectTree) node).getExpression();
            ++actualDepth;
            break;
          }
          throw new RuntimeException();

        case NEW_ARRAY:
          assert tpes.isEmpty();
          ap0 = ap0.add(new ASTPath.ASTEntry(Tree.Kind.NEW_ARRAY,
              ASTPath.TYPE, 0));
          if (expectedDepth == 0 && node.getKind() == kind) {
            if (node instanceof JCTree.JCNewArray) {
              int arg = entry.getArgument();
              if (arg > 0) {
                node = ((JCTree.JCNewArray) node).elemtype;
                tpes.add(TypePathEntry.ARRAY);
                while (--arg > 0 && node instanceof JCTree.JCArrayTypeTree) {
                  node = ((JCTree.JCArrayTypeTree) node).elemtype;
                  tpes.add(TypePathEntry.ARRAY);
                }
                if (arg > 0) { throw new RuntimeException(); }
              } else {
                node = TypeTree.fromType(((JCTree.JCNewArray) node).type);
              }
            } else {
              throw new RuntimeException("NYI");  // TODO
            }
            break;
          }
          throw new RuntimeException();

        case PARAMETERIZED_TYPE:
          if (node.getKind() == kind) {
            ParameterizedTypeTree ptt = (ParameterizedTypeTree) node;
            if (entry.childSelectorIs(ASTPath.TYPE)) {
              node = ptt.getType();
              break;  // ParameterizedType.type is "transparent" wrt type path
            } else if (expectedDepth == 0
                && entry.childSelectorIs(ASTPath.TYPE_ARGUMENT)) {
              List<? extends Tree> typeArgs = ptt.getTypeArguments();
              int j = entry.getArgument();
              if (j >= 0 && j < typeArgs.size()) {
                // make sure any inner types are accounted for
                actualDepth = 0;
                expectedDepth = localDepth(ptt.getType());
                while (--expectedDepth >= 0) {
                  tpes.add(TypePathEntry.INNER_TYPE);
                }
                node = typeArgs.get(j);
                tpes.add(
                    new TypePathEntry(TypePathEntryKind.TYPE_ARGUMENT, j));
                break;
              }
            }
          }
          throw new RuntimeException();

        case UNBOUNDED_WILDCARD:
          if (ASTPath.isWildcard(node.getKind())) {
            if (expectedDepth == 0
                && (i < 1
                    || ap1.get(i-1).getTreeKind() != Tree.Kind.INSTANCE_OF)
                && (i < 2
                    || ap1.get(i-2).getTreeKind() != Tree.Kind.ARRAY_TYPE)) {
              while (--actualDepth >= 0) {
                tpes.add(TypePathEntry.INNER_TYPE);
              }
              tpes.add(TypePathEntry.WILDCARD);
              break;
            }
          }
          throw new RuntimeException();

        default:
          node = ASTIndex.getNode(cut, rec);
          break;
        }

        ++i;
      }

      while (--actualDepth >= 0) {
        tpes.add(TypePathEntry.INNER_TYPE);
      }

      organized.add(ins);
      if (tpes.isEmpty()) {
        //assert ap1.equals(ap0) && !map.containsKey(ap0);
//        organized.add(ins);
        //map.put(rec, (TypedInsertion) ins);
      } else {
        criteria.add(new ASTPathCriterion(ap0));
        criteria.add(new GenericArrayLocationCriterion(
            new InnerTypeLocation(tpes)));
        tins.getInnerTypeInsertions().add(ins);
      }
    }
    organized.addAll(map.values());
    return organized;
  }

  private int newArrayInnerTypeDepth(ASTPath path) {
    int d = 0;
    if (path != null) {
      while (!path.isEmpty()) {
        ASTPath.ASTEntry entry = path.get(-1);
        switch (entry.getTreeKind()) {
        case ANNOTATED_TYPE:
        case MEMBER_SELECT:
        case PARAMETERIZED_TYPE:
        case UNBOUNDED_WILDCARD:
          d = 0;
          break;
        case ARRAY_TYPE:
          ++d;
          break;
        case NEW_ARRAY:
          if (entry.childSelectorIs(ASTPath.TYPE) && entry.hasArgument()) {
            d += entry.getArgument();
          }
          return d;
        default:
          return 0;
        }
        path = path.getParentPath();
      }
    }
    return 0;
  }

  /**
   * Find an {@link ASTRecord} for the tree corresponding to a nested
   * type of the type (use) to which the given record corresponds.
   *
   * @param rec record of (outer) type AST to be annotated
   * @param loc inner type path
   * @return record that locates the (nested) type in the source
   */
  private ASTRecord extendToInnerType(ASTRecord rec, List<TypePathEntry> loc) {
    ASTRecord r = rec;
    Iterator<TypePathEntry> iter = loc.iterator();
    int depth = 0;

    while (iter.hasNext()) {
      TypePathEntry tpe = iter.next();
      switch (tpe.tag) {
      case ARRAY:
        while (depth-- > 0) {
          r = r.extend(Tree.Kind.MEMBER_SELECT, ASTPath.EXPRESSION);
        }
        r = r.extend(Tree.Kind.ARRAY_TYPE, ASTPath.TYPE);
        break;
      case INNER_TYPE:
        ++depth;
        break;
      case TYPE_ARGUMENT:
        depth = 0;
        r = r.extend(Tree.Kind.PARAMETERIZED_TYPE, ASTPath.TYPE_ARGUMENT,
            tpe.arg);
        break;
      case WILDCARD:
        while (depth-- > 0) {
          r = r.extend(Tree.Kind.MEMBER_SELECT, ASTPath.EXPRESSION);
        }
        r = r.extend(Tree.Kind.UNBOUNDED_WILDCARD, ASTPath.BOUND);
        break;
      default:
        throw new RuntimeException();
      }
    }
    while (depth-- > 0) {
      r = r.extend(Tree.Kind.MEMBER_SELECT, ASTPath.EXPRESSION);
    }
    return r;
  }

  /**
   * Find an {@link ASTRecord} for the tree corresponding to a nested
   * type of the type (use) to which the given tree and record
   * correspond.
   *
   * @param rec record that locates {@code node} in the source
   * @param loc inner type path
   * @param node starting point for inner type path
   * @return record that locates the nested type in the source
   */
  private ASTRecord extendToInnerType(ASTRecord rec,
      List<TypePathEntry> loc, Tree node) {
    ASTRecord r = rec;
    Tree t = node;
    Iterator<TypePathEntry> iter = loc.iterator();
    TypePathEntry tpe = iter.next();

outer:
    while (true) {
      int d = localDepth(node);

      switch (t.getKind()) {
      case ANNOTATED_TYPE:
        r = r.extend(Tree.Kind.ANNOTATED_TYPE, ASTPath.TYPE);
        t = ((JCTree.JCAnnotatedType) t).getUnderlyingType();
        break;

      case ARRAY_TYPE:
        if (d == 0 && tpe.tag == TypePathEntryKind.ARRAY) {
          int a = 0;
          if (!r.astPath.isEmpty()) {
            ASTPath.ASTEntry e = r.astPath.get(-1);
            if (e.getTreeKind() == Tree.Kind.NEW_ARRAY
                && e.childSelectorIs(ASTPath.TYPE)) {
              a = 1 + e.getArgument();
            }
          }
          r = a > 0
              ? r.replacePath(r.astPath.getParentPath())
                  .extend(Tree.Kind.NEW_ARRAY, ASTPath.TYPE, a)
              : r.extend(Tree.Kind.ARRAY_TYPE, ASTPath.TYPE);
          t = ((ArrayTypeTree) t).getType();
          break;
        }
        throw new RuntimeException();

      case MEMBER_SELECT:
        if (d > 0 && tpe.tag == TypePathEntryKind.INNER_TYPE) {
          Tree temp = t;
          do {
            temp = ((JCTree.JCFieldAccess) temp).getExpression();
            if (!iter.hasNext()) {
              do {
                r = r.extend(Tree.Kind.MEMBER_SELECT, ASTPath.EXPRESSION);
              } while (--d > 0);
              return r;
            }
            tpe = iter.next();
            if (--d == 0) {
              continue outer;  // avoid next() at end of loop
            }
          } while (tpe.tag == TypePathEntryKind.INNER_TYPE);
        }
        throw new RuntimeException();

      case NEW_ARRAY:
        if (d == 0) {
          if (!r.astPath.isEmpty()) {
            ASTPath.ASTEntry e = r.astPath.get(-1);
            if (e.getTreeKind() == Tree.Kind.NEW_ARRAY) {
              int a = 0;
              while (tpe.tag == TypePathEntryKind.ARRAY) {
                ++a;
                if (!iter.hasNext()) { break; }
                tpe = iter.next();
              }
              r = r.replacePath(r.astPath.getParentPath())
                  .extend(Tree.Kind.NEW_ARRAY, ASTPath.TYPE, a);
              break;
            }
          }
          r = r.extend(Tree.Kind.ARRAY_TYPE, ASTPath.TYPE);
          t = ((JCTree.JCArrayTypeTree) t).getType();
          break;
        }
        throw new RuntimeException();

      case PARAMETERIZED_TYPE:
        if (d == 0 && tpe.tag == TypePathEntryKind.TYPE_ARGUMENT) {
          r = r.extend(Tree.Kind.PARAMETERIZED_TYPE,
              ASTPath.TYPE_ARGUMENT, tpe.arg);
          t = ((JCTree.JCTypeApply) t).getTypeArguments().get(tpe.arg);
          break;
        } else if (d > 0 && tpe.tag == TypePathEntryKind.INNER_TYPE) {
          Tree temp = ((JCTree.JCTypeApply) t).getType();
          r = r.extend(Tree.Kind.PARAMETERIZED_TYPE, ASTPath.TYPE);
          t = temp;
          do {
            temp = ((JCTree.JCFieldAccess) temp).getExpression();
            if (!iter.hasNext()) {
              do {
                r = r.extend(Tree.Kind.MEMBER_SELECT, ASTPath.EXPRESSION);
              } while (--d > 0);
              return r;
            }
            tpe = iter.next();
            if (--d == 0) {
              continue outer;  // avoid next() at end of loop
            }
          } while (tpe.tag == TypePathEntryKind.INNER_TYPE);
        }
        throw new RuntimeException();

      case EXTENDS_WILDCARD:
      case SUPER_WILDCARD:
      case UNBOUNDED_WILDCARD:
        if (tpe.tag == TypePathEntryKind.WILDCARD) {
          t = ((JCTree.JCWildcard) t).getBound();
          break;
        }
        throw new RuntimeException();

      default:
        if (iter.hasNext()) {
          throw new RuntimeException();
        }
      }

      if (!iter.hasNext()) { return r; }
      tpe = iter.next();
    }
  }

  // merge annotations, assuming types are structurally identical
  private void mergeTypedInsertions(TypedInsertion ins0, TypedInsertion ins1) {
    mergeTypes(ins0.getType(), ins1.getType());
  }

  private void mergeTypes(Type t0, Type t1) {
    if (t0 == t1) { return; }
    switch (t0.getKind()) {
    case ARRAY:
      {
        ArrayType at0 = (ArrayType) t0;
        ArrayType at1 = (ArrayType) t1;
        mergeTypes(at0.getComponentType(), at1.getComponentType());
        return;
      }
    case BOUNDED:
      {
        BoundedType bt0 = (BoundedType) t0;
        BoundedType bt1 = (BoundedType) t1;
        if (bt0.getBoundKind() != bt1.getBoundKind()) { break; }
        mergeTypes(bt0.getBound(), bt1.getBound());
        mergeTypes(bt0.getType(), bt1.getType());
        return;
      }
    case DECLARED:
      {
        DeclaredType dt0 = (DeclaredType) t0;
        DeclaredType dt1 = (DeclaredType) t1;
        List<Type> tps0 = dt0.getTypeParameters();
        List<Type> tps1 = dt1.getTypeParameters();
        int n = tps0.size();
        if (tps1.size() != n) { break; }
        mergeTypes(dt0.getInnerType(), dt1.getInnerType());
        for (String anno : dt1.getAnnotations()) {
          if (!dt0.getAnnotations().contains(anno)) {
            dt0.addAnnotation(anno);
          }
        }
        for (int i = 0; i < n; i++) {
          mergeTypes(tps0.get(i), tps1.get(i));
        }
        return;
      }
    }
    throw new RuntimeException();
  }

  // Returns the depth of the innermost local type of a type AST.
  private int localDepth(Tree node) {
    Tree t = node;
    int n = 0;
loop:
    while (t != null) {
      switch (t.getKind()) {
      case ANNOTATED_TYPE:
        t = ((AnnotatedTypeTree) t).getUnderlyingType();
        break;
      case MEMBER_SELECT:
        if (t instanceof JCTree.JCFieldAccess) {
          JCTree.JCFieldAccess jfa = (JCTree.JCFieldAccess) t;
          if (jfa.sym.kind == Kinds.PCK) {
            t = jfa.getExpression();
            continue;
          }
        }
        t = ((MemberSelectTree) t).getExpression();
        ++n;
        break;
      default:
        break loop;
      }
    }
    return n;
  }

  // Provides an additional level of indexing.
  class ASTRecordMap<E> implements Map<ASTRecord, E> {
    Map<ASTRecord, SortedMap<ASTPath, E>> back;

    ASTRecordMap() {
      back = new HashMap<ASTRecord, SortedMap<ASTPath, E>>();
    }

    private SortedMap<ASTPath, E> getMap(ASTRecord rec) {
      ASTRecord key = rec.replacePath(ASTPath.empty());
      SortedMap<ASTPath, E> map = back.get(key);
      if (map == null) {
        map = new TreeMap<ASTPath, E>();
        back.put(key, map);
      }
      return map;
    }

    @Override
    public int size() {
      int n = 0;
      for (SortedMap<ASTPath, E> map : back.values()) {
        n += map.size();
      }
      return n;
    }

    @Override
    public boolean isEmpty() {
      return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
      ASTRecord rec = (ASTRecord) key;
      SortedMap<ASTPath, E> m = getMap(rec);
      return m != null && m.containsKey(rec.astPath);
    }

    @Override
    public boolean containsValue(Object value) {
      @SuppressWarnings("unchecked")
      E e = (E) value;
      for (SortedMap<ASTPath, E> map : back.values()) {
        if (map.containsValue(e)) { return true; }
      }
      return false;
    }

    @Override
    public E get(Object key) {
      ASTRecord rec = (ASTRecord) key;
      SortedMap<ASTPath, E> map = getMap(rec);
      return map == null ? null : map.get(rec.astPath);
    }

    @Override
    public E put(ASTRecord key, E value) {
      ASTRecord rec = key;
      SortedMap<ASTPath, E> map = getMap(rec);
      return map == null ? null : map.put(rec.astPath, value);
    }

    @Override
    public E remove(Object key) {
      ASTRecord rec = (ASTRecord) key;
      SortedMap<ASTPath, E> map = getMap(rec);
      return map == null ? null : map.remove(rec.astPath);
    }

    @Override
    public void putAll(Map<? extends ASTRecord, ? extends E> m) {
      for (Map.Entry<? extends ASTRecord, ? extends E> entry : m.entrySet()) {
        put(entry.getKey(), entry.getValue());
      }
    }

    @Override
    public void clear() {
      back.clear();
    }

    @Override
    public Set<ASTRecord> keySet() {
      return back.keySet();
    }

    @Override
    public Collection<E> values() {
      Set<E> ret = new LinkedHashSet<E>();
      for (SortedMap<ASTPath, E> m : back.values()) {
        ret.addAll(m.values());
      }
      return ret;
    }

    @Override
    public Set<Map.Entry<ASTRecord, E>> entrySet() {
      final int size = size();
      return new AbstractSet<Map.Entry<ASTRecord, E>>() {
        @Override
        public Iterator<Map.Entry<ASTRecord, E>> iterator() {
          return new Iterator<Map.Entry<ASTRecord, E>>() {
            Iterator<Map.Entry<ASTRecord, SortedMap<ASTPath, E>>> iter0 =
                back.entrySet().iterator();
            Iterator<Map.Entry<ASTPath, E>> iter1 =
                Collections.<Map.Entry<ASTPath, E>>emptyIterator();
            ASTRecord rec = null;

            @Override
            public boolean hasNext() {
              if (iter1.hasNext()) { return true; }
              while (iter0.hasNext()) {
                Map.Entry<ASTRecord, SortedMap<ASTPath, E>> entry =
                    iter0.next();
                rec = entry.getKey();
                iter1 = entry.getValue().entrySet().iterator();
                if (iter1.hasNext()) { return true; }
              }
              iter1 = Collections.<Map.Entry<ASTPath, E>>emptyIterator();
              return false;
            }

            @Override
            public Map.Entry<ASTRecord, E> next() {
              if (!hasNext()) { throw new NoSuchElementException(); }
              final Map.Entry<ASTPath, E> e0 = iter1.next();
              return new Map.Entry<ASTRecord, E>() {
                final ASTRecord key = rec.replacePath(e0.getKey());
                final E val = e0.getValue();
                @Override public ASTRecord getKey() { return key; }
                @Override public E getValue() { return val; }
                @Override public E setValue(E value) {
                  throw new UnsupportedOperationException();
                }
              };
            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }
          };
        }

        @Override
        public int size() { return size; }
      };
    }
  }

  // Simple AST implementation used only in determining type paths.
  static abstract class TypeTree implements ExpressionTree {
    private static Map<String, TypeTag> primTags =
        new HashMap<String, TypeTag>();
    {
      primTags.put("byte", TypeTag.BYTE);
      primTags.put("char", TypeTag.CHAR);
      primTags.put("short", TypeTag.SHORT);
      primTags.put("long", TypeTag.LONG);
      primTags.put("float", TypeTag.FLOAT);
      primTags.put("int", TypeTag.INT);
      primTags.put("double", TypeTag.DOUBLE);
      primTags.put("boolean", TypeTag.BOOLEAN);
    }

    static TypeTree fromJCTree(JCTree jt) {
      if (jt != null) {
        Kind kind = jt.getKind();
        switch (kind) {
        case ANNOTATED_TYPE:
          return fromJCTree(
              ((JCTree.JCAnnotatedType) jt).getUnderlyingType());
        case IDENTIFIER:
          return new IdenT(
              ((JCTree.JCIdent) jt).sym.getSimpleName().toString());
        case ARRAY_TYPE:
          return new ArrT(
              fromJCTree(((JCTree.JCArrayTypeTree) jt).getType()));
        case MEMBER_SELECT:
          return new LocT(
              fromJCTree(((JCTree.JCFieldAccess) jt).getExpression()),
              ((JCTree.JCFieldAccess) jt).getIdentifier());
        case EXTENDS_WILDCARD:
        case SUPER_WILDCARD:
          return new WildT(kind,
              fromJCTree(((JCTree.JCWildcard) jt).getBound()));
        case UNBOUNDED_WILDCARD:
          return new WildT();
        case PARAMETERIZED_TYPE:
          com.sun.tools.javac.util.List<JCExpression> typeArgs =
            ((JCTree.JCTypeApply) jt).getTypeArguments();
          List<Tree> args = new ArrayList<Tree>(typeArgs.size());
          for (JCTree.JCExpression typeArg : typeArgs) {
            args.add(fromJCTree(typeArg));
          }
          return new ParT(
              fromJCTree(((JCTree.JCTypeApply) jt).getType()),
              args);
        default:
          break;
        }
      }
      return null;
    }

    static TypeTree fromType(final Type type) {
      switch (type.getKind()) {
      case ARRAY:
        final ArrayType atype = (ArrayType) type;
        final TypeTree componentType = fromType(atype.getComponentType());
        return new ArrT(componentType);
      case BOUNDED:
        final BoundedType btype = (BoundedType) type;
        final BoundedType.BoundKind bk = btype.getBoundKind();
        final String bname = btype.getType().getName();
        final TypeTree bound = fromType(btype.getBound());
        return new Param(bname, bk, bound);
      case DECLARED:
        final DeclaredType dtype = (DeclaredType) type;
        if (dtype.isWildcard()) {
          return new WildT();
        } else {
          final String dname = dtype.getName();
          TypeTag typeTag = primTags.get(dname);
          if (typeTag == null) {
            final TypeTree base = new IdenT(dname);
            TypeTree ret = base;
            List<Type> params = dtype.getTypeParameters();
            DeclaredType inner = dtype.getInnerType();
            if (!params.isEmpty()) {
              final List<Tree> typeArgs = new ArrayList<Tree>(params.size());
              for (Type t : params) { typeArgs.add(fromType(t)); }
              ret = new ParT(base, typeArgs);
            }
            return inner == null ? ret : meld(fromType(inner), ret);
          } else {
            final TypeKind typeKind = typeTag.getPrimitiveTypeKind();
            return new PrimT(typeKind);
          }
        }
      default:
        throw new RuntimeException("unknown type kind " + type.getKind());
      }
    }

    static TypeTree fromType(final com.sun.tools.javac.code.Type type) {
      return fromType(conv(type));
    }

    /**
     * @param jtype
     * @return
     */
    static Type conv(final com.sun.tools.javac.code.Type jtype) {
      Type type = null;
      DeclaredType d;
      com.sun.tools.javac.code.Type t;
      switch (jtype.getKind()) {
      case ARRAY:
        t = ((com.sun.tools.javac.code.Type.ArrayType) jtype).elemtype;
        type = new ArrayType(conv(t));
        break;
      case DECLARED:
        t = jtype;
        d = null;
        do {
          DeclaredType d0 = d;
          com.sun.tools.javac.code.Type.ClassType ct =
              (com.sun.tools.javac.code.Type.ClassType) t;
          d = new DeclaredType(ct.tsym.name.toString());
          d.setInnerType(d0);
          d0 = d;
          for (com.sun.tools.javac.code.Type a : ct.getTypeArguments()) {
            d.addTypeParameter(conv(a));
          }
          t = ct.getEnclosingType();
        } while (t.getKind() == TypeKind.DECLARED);
        type = d;
        break;
      case WILDCARD:
        BoundedType.BoundKind k;
        t = ((com.sun.tools.javac.code.Type.WildcardType) jtype).bound;
        switch (((com.sun.tools.javac.code.Type.WildcardType) jtype).kind) {
        case EXTENDS:
          k = BoundedType.BoundKind.EXTENDS;
          break;
        case SUPER:
          k = BoundedType.BoundKind.SUPER;
          break;
        case UNBOUND:
          k = null;
          type = new DeclaredType("?");
          break;
        default:
          throw new RuntimeException();
        }
        if (k != null) {
          d = new DeclaredType(jtype.tsym.name.toString());
          type = new BoundedType(d, k, (DeclaredType) conv(t));
        }
        break;
      case TYPEVAR:
        t = ((com.sun.tools.javac.code.Type.TypeVar) jtype).getUpperBound();
        type = conv(t);
        if (type.getKind() == Type.Kind.DECLARED) {
          type = new BoundedType(new DeclaredType(jtype.tsym.name.toString()),
              BoundedType.BoundKind.EXTENDS, (DeclaredType) type);
        }  // otherwise previous conv should have been here already
        break;
      case INTERSECTION:
        t = jtype.tsym.erasure_field;  // ???
        type = new DeclaredType(t.tsym.name.toString());
        break;
      case UNION:
        // TODO
        break;
      case BOOLEAN:
      case BYTE:
      case CHAR:
      case DOUBLE:
      case LONG:
      case SHORT:
      case FLOAT:
      case INT:
        type = new DeclaredType(jtype.tsym.name.toString());
        break;
      //case ERROR:
      //case EXECUTABLE:
      //case NONE:
      //case NULL:
      //case OTHER:
      //case PACKAGE:
      //case VOID:
      default:
        break;
      }
      return type;
    }

    private static TypeTree meld(final TypeTree t0, final TypeTree t1) {
      switch (t0.getKind()) {
      case IDENTIFIER:
        IdenT it = (IdenT) t0;
        return new LocT(t1, it.getName());
      case MEMBER_SELECT:
        LocT lt = (LocT) t0;
        return new LocT(meld(lt.getExpression(), t1), lt.getIdentifier());
      case PARAMETERIZED_TYPE:
        ParT pt = (ParT) t0;
        return new ParT(meld(pt.getType(), t1), pt.getTypeArguments());
      default:
        throw new IllegalArgumentException("unexpected type " + t0);
      }
    }

    static final class ArrT extends TypeTree implements ArrayTypeTree {
      private final TypeTree componentType;

      ArrT(TypeTree componentType) {
        this.componentType = componentType;
      }

      @Override
      public Kind getKind() { return Kind.ARRAY_TYPE; }

      @Override
      public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
        return visitor.visitArrayType(this, data);
      }

      @Override
      public TypeTree getType() { return componentType; }

      @Override
      public String toString() { return componentType + "[]"; }
    }

    static final class LocT extends TypeTree implements MemberSelectTree {
      private final TypeTree expr;
      private final Name name;

      LocT(TypeTree expr, Name name) {
        this.expr = expr;
        this.name = name;
      }

      @Override
      public Kind getKind() { return Kind.MEMBER_SELECT; }

      @Override
      public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
        return visitor.visitMemberSelect(this, data);
      }

      @Override
      public TypeTree getExpression() { return expr; }

      @Override
      public Name getIdentifier() { return name; }

      @Override
      public String toString() { return expr + "." + name; }
    }

    static final class ParT extends TypeTree implements ParameterizedTypeTree {
      private final TypeTree base;
      private final List<? extends Tree> typeArgs;

      ParT(TypeTree base, List<? extends Tree> typeArgs) {
        this.base = base;
        this.typeArgs = typeArgs;
      }

      @Override
      public Kind getKind() { return Kind.PARAMETERIZED_TYPE; }

      @Override
      public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
        return visitor.visitParameterizedType(this, data);
      }

      @Override
      public TypeTree getType() { return base; }

      @Override
      public List<? extends Tree> getTypeArguments() {
        return typeArgs;
      }

      @Override
      public String toString() {
        StringBuilder sb = new StringBuilder(base.toString());
        String s = "<";
        for (Tree t : typeArgs) {
          sb.append(s);
          sb.append(t.toString());
          s = ", ";
        }
        sb.append('>');
        return sb.toString();
      }
    }

    static final class PrimT extends TypeTree implements PrimitiveTypeTree {
      private final TypeKind typeKind;

      PrimT(TypeKind typeKind) {
        this.typeKind = typeKind;
      }

      @Override
      public Kind getKind() { return Kind.PRIMITIVE_TYPE; }

      @Override
      public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
        return visitor.visitPrimitiveType(this, data);
      }

      @Override
      public TypeKind getPrimitiveTypeKind() { return typeKind; }

      @Override
      public String toString() {
        switch (typeKind) {
        case BOOLEAN: return "boolean";
        case BYTE: return "byte";
        case CHAR: return "char";
        case DOUBLE: return "double";
        case FLOAT: return "float";
        case INT: return "int";
        case LONG: return "long";
        case SHORT: return "short";
        //case VOID: return "void";
        //case WILDCARD: return "?";
        default:
          throw new IllegalArgumentException("unexpected type kind "
              + typeKind);
        }
      }
    }

    static final class IdenT extends TypeTree implements IdentifierTree {
      private final String name;

      IdenT(String dname) {
        this.name = dname;
      }

      @Override
      public Kind getKind() { return Kind.IDENTIFIER; }

      @Override
      public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
        return visitor.visitIdentifier(this, data);
      }

      @Override
      public Name getName() { return new TypeName(name); }

      @Override
      public String toString() { return name; }
    }

    static final class WildT extends TypeTree implements WildcardTree {
      private final TypeTree bound;
      private final Kind kind;

      WildT() {
        this(Kind.UNBOUNDED_WILDCARD, null);
      }

      WildT(TypeTree bound, BoundedType.BoundKind bk) {
        this(bk == BoundedType.BoundKind.SUPER
                ? Kind.SUPER_WILDCARD
                : Kind.EXTENDS_WILDCARD,
            bound);
      }

      WildT(Kind kind, TypeTree bound) {
        this.kind = kind;
        this.bound = bound;
      }

      @Override
      public Kind getKind() { return kind; }

      @Override
      public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
        return visitor.visitWildcard(this, data);
      }

      @Override
      public Tree getBound() { return bound; }

      @Override
      public String toString() { return "?"; }
    }

    static final class Param extends TypeTree implements TypeParameterTree {
      private final String bname;
      private final BoundedType.BoundKind bk;
      private final Tree bound;

      Param(String bname, BoundedType.BoundKind bk, TypeTree bound) {
        this.bname = bname;
        this.bk = bk;
        this.bound = bound;
      }

      @Override
      public Kind getKind() { return Kind.TYPE_PARAMETER; }

      @Override
      public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
        return visitor.visitTypeParameter(this, data);
      }

      @Override
      public Name getName() { return new TypeName(bname); }

      @Override
      public List<? extends Tree> getBounds() {
        return Collections.singletonList(bound);
      }

      @Override
      public List<? extends AnnotationTree> getAnnotations() {
        return Collections.emptyList();
      }

      @Override
      public String toString() {
        return bname + " " + bk.toString() + " " + bound.toString();
      }
    }

    static final class TypeName implements Name {
      private final String str;

      TypeName(String str) {
        this.str = str;
      }

      @Override
      public int length() { return str.length(); }

      @Override
      public char charAt(int index) { return str.charAt(index); }

      @Override
      public CharSequence subSequence(int start, int end) {
        return str.subSequence(start, end);
      }

      @Override
      public boolean contentEquals(CharSequence cs) {
        if (cs != null) {
          int n = length();
          if (cs.length() == n) {
            for (int i = 0; i < n; i++) {
              if (charAt(i) != cs.charAt(i)) { return false; }
            }
            return true;
          }
        }
        return false;
      }

      @Override
      public String toString() { return str; }
    }
  }
}
