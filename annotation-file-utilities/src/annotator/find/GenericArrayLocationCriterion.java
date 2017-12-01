package annotator.find;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.type.TypeKind;

import scenelib.annotations.el.InnerTypeLocation;
import annotator.Main;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;
import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntryKind;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;

/**
 * GenericArrayLocationCriterion represents the criterion specifying the location
 * of an element in the generic/array hierarchy as specified by the
 * JSR 308 proposal.
 */
public class GenericArrayLocationCriterion implements Criterion {
  private static final boolean debug = false;

  // the full location list
  private final List<TypePathEntry> location;

  // represents all but the last element of the location list
  // TODO: this field is initialized, but never read!
  // I removed it, see the version control history.
  // private Criterion parentCriterion;

  /**
   * Creates a new GenericArrayLocationCriterion specifying that the element
   * is an outer type, such as:
   *  <code>@A List&lt;Integer&gt;</code>
   * or
   *  <code>Integer @A []</code>
   */
  public GenericArrayLocationCriterion() {
    this(new ArrayList<TypePathEntry>());
  }

  /**
   * Creates a new GenericArrayLocationCriterion representing the given
   * location.
   *
   * @param innerTypeLoc the location of the element being represented
   */
  public GenericArrayLocationCriterion(InnerTypeLocation innerTypeLoc) {
    this(innerTypeLoc.location);
  }

  private GenericArrayLocationCriterion(List<TypePathEntry> location) {
    this.location = location;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path, Tree leaf) {
    if (path == null) {
      return false;
    }
    assert path.getLeaf() == leaf;
    return isSatisfiedBy(path);
  }

  /**
   * Determines if the given list holds only {@link TypePathEntry}s with the tag
   * {@link TypePathEntryKind#ARRAY}.
   *
   * @param location the list to check
   * @return {@code true} if the list only contains
   *         {@link TypePathEntryKind#ARRAY}, {@code false} otherwise.
   */
  private boolean containsOnlyArray(List<TypePathEntry> location) {
    for (TypePathEntry tpe : location) {
      if (tpe.tag != TypePathEntryKind.ARRAY) {
        return false;
      }
    }
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path) {
    if (path == null || path.getParentPath() == null) {
      if (debug) {
        System.out.println("GenericArrayLocationCriterion.isSatisfiedBy() with null path gives false.");
      }
      return false;
    }

    if (debug) {
      System.out.printf("GenericArrayLocationCriterion.isSatisfiedBy():%n  leaf of path: %s%n  searched location: %s%n",
              path.getLeaf(), location);
    }

    TreePath pathRemaining = path;
    Tree leaf = path.getLeaf();

    // Don't allow annotations directly on these tree kinds if the child type is
    // a MEMBER_SELECT. This way we'll continue to traverse deeper in the tree
    // to find the correct MEMBER_SELECT.
    Tree child = null;
    if (leaf.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
      child = ((ParameterizedTypeTree) leaf).getType();
    } else if (leaf.getKind() == Tree.Kind.VARIABLE) {
      child = ((VariableTree) leaf).getType();
    } else if (leaf.getKind() == Tree.Kind.NEW_CLASS) {
      child = ((NewClassTree) leaf).getIdentifier();
    } else if (leaf.getKind() == Tree.Kind.NEW_ARRAY && !location.isEmpty()) {
      child = ((NewArrayTree) leaf).getType();
    }
    if (child != null && child.getKind() == Tree.Kind.MEMBER_SELECT) {
      JCExpression exp = ((JCFieldAccess) child).getExpression();
      if (exp.type != null && exp.type.getKind() == TypeKind.PACKAGE
          || location.isEmpty()
          || (location.get(location.size()-1)).tag
              != TypePathEntryKind.INNER_TYPE) {
          return false;
      }
    }

    if (leaf.getKind() == Tree.Kind.MEMBER_SELECT) {
      JCFieldAccess fieldAccess = (JCFieldAccess) leaf;
      if (isStatic(fieldAccess)) {
        // If this MEMBER_SELECT is for a static class...
        if (location.isEmpty()) {
          // ...and it does not go on a compound type, this is the right place.
          return true;
        } else if (isGenericOrArray(path.getParentPath().getLeaf())
            && isGenericOrArray(path.getParentPath().getParentPath().getLeaf())) {
          // If the two parents above this are compound types, then skip
          // the compound type directly above. For example, to get to Outer.Inner
          // of Outer.Inner<K, V> we had to get through the PARAMETERIZED_TYPE
          // node. But we didn't go deeper in the compound type in the way the
          // type path defines, we just went deeper to get to the outer type. So
          // skip this node later when checking to make sure that we're in the
          // correct part of the compound type.
          pathRemaining = path.getParentPath();
        }
      } else {
        JCExpression exp = fieldAccess.getExpression();
        if (exp.getKind() == Tree.Kind.MEMBER_SELECT && exp.type != null
            && exp.type.getKind() == TypeKind.PACKAGE) {
          if (location.isEmpty()) {
            return true;
          } // else, keep going to make sure we're in the right part of the
            // compound type
        } else {
          if (!location.isEmpty()
              && location.get(location.size()-1).tag
                  != TypePathEntryKind.INNER_TYPE) {
            return false;
          }
        }
      }
    }

    if (location.isEmpty()) {
      // no inner type location, want to annotate outermost type
      // e.g.,  @Nullable List list;
      //        @Nullable List<String> list;
      //        String @Nullable [] array;
      leaf = path.getLeaf();
      Tree parent = path.getParentPath().getLeaf();

      boolean result = ((leaf.getKind() == Tree.Kind.NEW_ARRAY)
                        || (leaf.getKind() == Tree.Kind.NEW_CLASS)
                        || (leaf.getKind() == Tree.Kind.ANNOTATED_TYPE
                            && isSatisfiedBy(TreePath.getPath(path,
                              ((AnnotatedTypeTree) leaf).getUnderlyingType())))
                        || ((isGenericOrArray(leaf)
                             // or, it might be a raw type
                             || (leaf.getKind() == Tree.Kind.IDENTIFIER) // IdentifierTree
                             || (leaf.getKind() == Tree.Kind.METHOD) // MethodTree
                             || (leaf.getKind() == Tree.Kind.TYPE_PARAMETER) // TypeParameterTree
                             // I don't know why a GenericArrayLocationCriterion
                             // is being created in this case, but it is.
                             || (leaf.getKind() == Tree.Kind.PRIMITIVE_TYPE) // PrimitiveTypeTree
                             // TODO: do we need wildcards here?
                             // || leaf instanceof WildcardTree
                             )
                            && ! isGenericOrArray(parent)));
      if (debug) {
        System.out.printf("GenericArrayLocationCriterion.isSatisfiedBy: locationInParent==null%n  leaf=%s (%s)%n  parent=%s (%s)%n  => %s (%s %s)%n",
                  leaf, leaf.getClass(), parent, parent.getClass(), result, isGenericOrArray(leaf), ! isGenericOrArray(parent));
      }

      return result;
    }

    // If we've made it this far then we've determined that *if* this is the right
    // place to insert the annotation this is the MEMBER_SELECT it should be
    // inserted on. So remove the rest of the MEMBER_SELECTs to get down to the
    // compound type and make sure the compound type location matches.
    while (pathRemaining.getParentPath().getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
      pathRemaining = pathRemaining.getParentPath();
    }

    List<TypePathEntry> locationRemaining = new ArrayList<TypePathEntry>(location);

    while (locationRemaining.size() != 0) {
      // annotating an inner type
      leaf = pathRemaining.getLeaf();
      if ((leaf.getKind() == Tree.Kind.NEW_ARRAY)
          && containsOnlyArray(locationRemaining)) {
        if (debug) {
          System.out.println("Found a matching NEW_ARRAY");
        }
        return true;
      }
      TreePath parentPath = pathRemaining.getParentPath();
      if (parentPath == null) {
        if (debug) {
          System.out.println("Parent path is null and therefore false.");
        }
        return false;
      }
      Tree parent = parentPath.getLeaf();

      if (parent.getKind() == Tree.Kind.ANNOTATED_TYPE) {
          // If the parent is an annotated type, we did not really go up a level.
          // Therefore, skip up one more level.
          parentPath = parentPath.getParentPath();
          parent = parentPath.getLeaf();
      }

      if (debug) {
        System.out.printf("locationRemaining: %s, leaf: %s parent: %s %s%n",
                         locationRemaining, Main.treeToString(leaf), Main.treeToString(parent), parent.getClass());
      }

      TypePathEntry loc = locationRemaining.get(locationRemaining.size()-1);
      if (loc.tag == TypePathEntryKind.INNER_TYPE) {
        if (leaf.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
          leaf = parent;
          parentPath = parentPath.getParentPath();
          parent = parentPath.getLeaf();
        }
        if (leaf.getKind() != Tree.Kind.MEMBER_SELECT) { return false; }

        JCFieldAccess fieldAccess = (JCFieldAccess) leaf;
        if (isStatic(fieldAccess)) {
          return false;
        }
        locationRemaining.remove(locationRemaining.size()-1);
        leaf = fieldAccess.selected;
        pathRemaining = parentPath;
            // TreePath.getPath(pathRemaining.getCompilationUnit(), leaf);
      } else if (loc.tag == TypePathEntryKind.WILDCARD
          && leaf.getKind() == Tree.Kind.UNBOUNDED_WILDCARD) {
        // Check if the leaf is an unbounded wildcard instead of the parent, since unbounded
        // wildcard has no members so it can't be the parent of anything.
        if (locationRemaining.size() == 0) {
          return false;
        }

        // The following check is necessary because Oracle has decided that
        //   x instanceof Class<? extends Object>
        // will remain illegal even though it means the same thing as
        //   x instanceof Class<?>.
        TreePath gpath = parentPath.getParentPath();
        if (gpath != null) {  // TODO: skip over existing annotations?
          Tree gparent = gpath.getLeaf();
          if (gparent.getKind() == Tree.Kind.INSTANCE_OF) {
            TreeFinder.warn.debug("WARNING: wildcard bounds not allowed "
                + "in 'instanceof' expression; skipping insertion%n");
            return false;
          } else if (gparent.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
            gpath = gpath.getParentPath();
            if (gpath != null
                && gpath.getLeaf().getKind() == Tree.Kind.ARRAY_TYPE) {
              TreeFinder.warn.debug("WARNING: wildcard bounds not allowed "
                  + "in generic array type; skipping insertion%n");
              return false;
            }
          }
        }
        locationRemaining.remove(locationRemaining.size() - 1);
      } else if (parent.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
        if (loc.tag != TypePathEntryKind.TYPE_ARGUMENT) {
          return false;
        }

        // Find the outermost type in the AST; if it has parameters,
        // pop the stack once for each inner type on the end of the type
        // path and check the parameter.
        Tree inner = ((ParameterizedTypeTree) parent).getType();
        int i = locationRemaining.size() - 1;  // last valid type path index
        locationRemaining.remove(i--);
        while (inner.getKind() == Tree.Kind.MEMBER_SELECT
            && !isStatic((JCFieldAccess) inner)) {
          // fieldAccess.type != null && fieldAccess.type.getKind() == TypeKind.DECLARED
          // && fieldAccess.type.tsym.isStatic()
          // TODO: check whether MEMBER_SELECT indicates inner or qualifier?
          if (i < 0) { break; }
          if (locationRemaining.get(i).tag != TypePathEntryKind.INNER_TYPE) {
            return false;
          }
          locationRemaining.remove(i--);
          inner = ((MemberSelectTree) inner).getExpression();
          if (inner.getKind() == Tree.Kind.ANNOTATED_TYPE) {
            inner = ((AnnotatedTypeTree) inner).getUnderlyingType();
          }
          if (inner.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
            inner = ((ParameterizedTypeTree) inner).getType();
          }
        }
        if (i >= 0 && locationRemaining.get(i).tag ==
            TypePathEntryKind.INNER_TYPE) {
          return false;
        }

        // annotating List<@A Integer>
        // System.out.printf("parent instanceof ParameterizedTypeTree: %s loc=%d%n",
        //                   Main.treeToString(parent), loc);
        List<? extends Tree> childTrees =
            ((ParameterizedTypeTree) parent).getTypeArguments();
        boolean found = false;
        if (childTrees.size() > loc.arg) {
          Tree childi = childTrees.get(loc.arg);
          if (childi.getKind() == Tree.Kind.ANNOTATED_TYPE) {
            childi = ((AnnotatedTypeTree) childi).getUnderlyingType();
          }
          if (childi == leaf) {
            for (TreePath outerPath = parentPath.getParentPath();
                outerPath.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT
                    && !isStatic((JCFieldAccess) outerPath.getLeaf());
                outerPath = outerPath.getParentPath()) {
              outerPath = outerPath.getParentPath();
              if (outerPath.getLeaf().getKind() == Tree.Kind.ANNOTATED_TYPE) {
                outerPath = outerPath.getParentPath();
              }
              if (outerPath.getLeaf().getKind() != Tree.Kind.PARAMETERIZED_TYPE) {
                break;
              }
              parentPath = outerPath;
            }
            pathRemaining = parentPath;
            found = true;
          }
        }
        if (!found) {
          if (debug) {
            System.out.printf("Generic failed for leaf: %s: nr children: %d loc: %s child: %s%n",
                             leaf, childTrees.size(), loc,
                             ((childTrees.size() > loc.arg) ? childTrees.get(loc.arg) : null));
          }
          return false;
        }
      } else if (parent.getKind() == Tree.Kind.EXTENDS_WILDCARD
                 || parent.getKind() == Tree.Kind.SUPER_WILDCARD) {
        if (loc.tag != TypePathEntryKind.WILDCARD || locationRemaining.size() == 1) {
          // If there's only one location left, this can't be a match since a wildcard
          // needs to be in another kind of compound type.
          return false;
        }
        locationRemaining.remove(locationRemaining.size() - 1);
        // annotating List<? extends @A Integer>
        // System.out.printf("parent instanceof extends WildcardTree: %s loc=%d%n",
        //                   Main.treeToString(parent), loc);
        WildcardTree wct = (WildcardTree) parent;
        Tree boundTree = wct.getBound();

        if (debug) {
          String wildcardType;
          if (parent.getKind() == Tree.Kind.EXTENDS_WILDCARD) {
            wildcardType = "ExtendsWildcard";
          } else {
            wildcardType = "SuperWildcard";
          }
          System.out.printf("%s with bound %s gives %s%n", wildcardType, boundTree, boundTree.equals(leaf));
        }

        if (boundTree.equals(leaf)) {
          if (locationRemaining.isEmpty()) {
            return true;
          } else {
            pathRemaining = parentPath;
          }
        } else {
          return false;
        }
      } else if (parent.getKind() == Tree.Kind.ARRAY_TYPE) {
        if (loc.tag != TypePathEntryKind.ARRAY) {
          return false;
        }
        locationRemaining.remove(locationRemaining.size() - 1);
        // annotating Integer @A []
        parentPath = TreeFinder.largestContainingArray(parentPath);
        parent = parentPath.getLeaf();
        // System.out.printf("parent instanceof ArrayTypeTree: %s loc=%d%n",
        //                   parent, loc);
        Tree elt = ((ArrayTypeTree) parent).getType();
        while (locationRemaining.size() > 0
                && locationRemaining.get(locationRemaining.size() - 1).tag == TypePathEntryKind.ARRAY) {
          if (elt.getKind() != Tree.Kind.ARRAY_TYPE) { // ArrayTypeTree
            if (debug) {
              System.out.printf("Element: %s is not an ArrayTypeTree and therefore false.\n", elt);
            }
            return false;
          }
          elt = ((ArrayTypeTree) elt).getType();
          locationRemaining.remove(locationRemaining.size() - 1);
        }

        boolean b = elt.equals(leaf);
        if (debug) {
          System.out.printf("parent %s instanceof ArrayTypeTree: %b %s %s %s%n",
                           parent, elt.equals(leaf), elt, leaf, loc);
          System.out.printf("b=%s elt=%s leaf=%s%n", b, elt, leaf);
        }

        // TODO:  The parent criterion should be exact, not just "in".
        // Otherwise the criterion [1]  matches  [5 4 3 2 1].
        // This is a disadvantage of working from the inside out instead of the outside in.
        if (b) {
          pathRemaining = parentPath;
        } else {
          return false;
        }
      } else if (parent.getKind() == Tree.Kind.NEW_ARRAY) {
        if (loc.tag != TypePathEntryKind.ARRAY) {
          return false;
        }
        if (debug) {
          System.out.println("Parent is a NEW_ARRAY and always gives true.");
        }
        return true;
      } else {
        if (debug) {
          System.out.printf("unrecognized parent kind = %s%n", parent.getKind());
        }
        return false;
      }
    }

    // no (remaining) inner type location, want to annotate outermost type
    // e.g.,  @Nullable List list;
    //        @Nullable List<String> list;
    TreePath parentPath = pathRemaining.getParentPath();
    if (parentPath == null) {
      if (debug) {
        System.out.println("Parent path is null and therefore false.");
      }
      return false;
    }
    Tree parent = pathRemaining.getParentPath().getLeaf();
    if (debug) {
      leaf = pathRemaining.getLeaf();
      System.out.printf("No (remaining) inner type location:%n  leaf: %s %b%n  parent: %s %b%n  result: %s%n",
            Main.treeToString(leaf), isGenericOrArray(leaf),
            Main.treeToString(parent), isGenericOrArray(parent),
            ! isGenericOrArray(parent));
    }

    return ! isGenericOrArray(parent);
  }

  /**
   * @param fieldAccess
   * @return
   */
  private boolean isStatic(JCFieldAccess fieldAccess) {
    return fieldAccess.type != null
        && fieldAccess.type.getKind() == TypeKind.DECLARED
        && fieldAccess.type.tsym.isStatic();
  }

  private boolean isGenericOrArray(Tree t) {
    return ((t.getKind() == Tree.Kind.PARAMETERIZED_TYPE)
            || (t.getKind() == Tree.Kind.ARRAY_TYPE)
            || (t.getKind() == Tree.Kind.EXTENDS_WILDCARD)
            || (t.getKind() == Tree.Kind.SUPER_WILDCARD)
            || (t.getKind() == Tree.Kind.ANNOTATED_TYPE &&
                    isGenericOrArray(((AnnotatedTypeTree)t).getUnderlyingType()))
            // Monolithic:  one node for entire "new".  So, handle specially.
            // || (t.getKind() == Tree.Kind.NEW_ARRAY)
            );
  }

  @Override
  public Kind getKind() {
    return Criterion.Kind.GENERIC_ARRAY_LOCATION;
  }

  @Override
  public String toString() {
    return "GenericArrayLocationCriterion at " +
    ((location.isEmpty())
     ? "outermost type"
     : ("( " + location.toString() + " )"));
  }

  /**
   * Gets the type path location of this criterion.
   *
   * @return an unmodifiable list of {@link TypePathEntry}s
   */
  public List<TypePathEntry> getLocation() {
    return Collections.unmodifiableList(location);
  }
}
