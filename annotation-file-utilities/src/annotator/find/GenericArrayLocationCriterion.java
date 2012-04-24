package annotator.find;

// only used for debugging
import annotator.Main;

import java.util.ArrayList;
import java.util.List;

import annotations.el.InnerTypeLocation;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

/**
 * GenericArrayLocationCriterion represents the criterion specifying the location
 * of an element in the generic/array hierarchy as specified by the
 * JSR 308 proposal.
 */
public class GenericArrayLocationCriterion implements Criterion {
  private static final boolean debug = false;

  // the full location list
  private final List<Integer> location;

  // The last element of the location list -- that is,
  // location.get(location.size() - 1), or null if (location.size() == 0).
  // 
  // Needs to be visible for TreeFinder, for a hacky access from there. 
  // TODO: make private
  // Also TODO: locationInParent is not used for any logic in this class,
  // just abused by TreeFinder. See whether you can clean this up.
  Integer locationInParent;

  // represents all but the last element of the location list
  // TODO: this field is initialized, but never read!
  // I removed it, see the version control history.
  // private Criterion parentCriterion;

  /**
   * Creates a new GenericArrayLocationCriterion specifying that the element
   * is an outer type, such as:
   *  <code>@A List<Integer></code>
   * or
   *  <code>Integer @A []</code>
   */
  public GenericArrayLocationCriterion() {
    this(new ArrayList<Integer>());
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

  private GenericArrayLocationCriterion(List<Integer> location) {
    this.location = location;
    if (location.size() == 0) {
      this.locationInParent = null;
    } else {
      this.locationInParent = location.get(location.size() - 1);
    }
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
      if (debug) {
        System.out.println("GenericArrayLocationCriterion.isSatisfiedBy() with null path gives false.");
      }
      return false;
    }

    if (debug) {
      System.out.printf("GenericArrayLocationCriterion.isSatisfiedBy():%n  leaf of path: %s%n  searched location: %s%n",
              path.getLeaf(), location);
    }

    if (locationInParent == null) {
      // no inner type location, want to annotate outermost type
      // e.g.,  @Readonly List list;
      //        @Readonly List<String> list;
      //        String @Readonly [] array;
      Tree leaf = path.getLeaf();
      Tree parent = path.getParentPath().getLeaf();

      boolean result = ((leaf.getKind() == Tree.Kind.NEW_ARRAY)
                        || (leaf.getKind() == Tree.Kind.NEW_CLASS)
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

    TreePath pathRemaining = path;
    List<Integer> locationRemaining = new ArrayList<Integer>(location);

    while (locationRemaining.size() != 0) {
      // annotating an inner type
      Tree leaf = pathRemaining.getLeaf();
      if ((leaf.getKind() == Tree.Kind.NEW_ARRAY)
          && (locationRemaining.size() == 1)) {
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

      int loc = locationRemaining.get(locationRemaining.size()-1);
      if (parent.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
        // annotating List<@A Integer>
        // System.out.printf("parent instanceof ParameterizedTypeTree: %s loc=%d%n",
        //                   Main.treeToString(parent), loc);
        ParameterizedTypeTree ptt = (ParameterizedTypeTree) parent;
        List<? extends Tree> childTrees = ptt.getTypeArguments();
        boolean found = false;
        if (childTrees.size() > loc ) {
          Tree childi = childTrees.get(loc);

          if (childi.getKind() == Tree.Kind.ANNOTATED_TYPE) {
              childi = ((AnnotatedTypeTree) childi).getUnderlyingType();
          }
          if (childi.equals(leaf)) { 
            locationRemaining.remove(locationRemaining.size()-1);
            pathRemaining = parentPath;
            found = true;
          }
        }
        if (!found) {
          if (debug) {
            System.out.printf("Generic failed for leaf: %s: nr children: %d loc: %d child: %s%n",
                             leaf, childTrees.size(), loc,
                             ((childTrees.size() > loc) ? childTrees.get(loc) : null));
          }
          return false;
        }
      } else if (parent.getKind() == Tree.Kind.EXTENDS_WILDCARD) {
        // annotating List<? extends @A Integer>
        // System.out.printf("parent instanceof extends WildcardTree: %s loc=%d%n",
        //                   Main.treeToString(parent), loc);
        WildcardTree wct = (WildcardTree) parent;
        Tree boundTree = wct.getBound();

        if (debug) {
          System.out.printf("ExtendsWildcard with bound %s gives %s%n", boundTree, boundTree.equals(leaf));
        }

        return boundTree.equals(leaf);
      } else if (parent.getKind() == Tree.Kind.SUPER_WILDCARD) {
        // annotating List<? super @A Integer>
        // System.out.printf("parent instanceof super WildcardTree: %s loc=%d%n",
        //                   Main.treeToString(parent), loc);
        WildcardTree wct = (WildcardTree) parent;
        Tree boundTree = wct.getBound();

        if (debug) {
          System.out.printf("SuperWildcard with bound %s gives %s%n", boundTree, boundTree.equals(leaf));
        }

        return boundTree.equals(leaf);
      // } else if (parent.getKind() == Tree.Kind.UNBOUNDED_WILDCARD) {
        // The parent can never be the unbounded wildcard, as it doesn't have any members.
      } else if (parent.getKind() == Tree.Kind.ARRAY_TYPE) {
        // annotating Integer @A []
        parentPath = TreeFinder.largestContainingArray(parentPath);
        parent = parentPath.getLeaf();
        // System.out.printf("parent instanceof ArrayTypeTree: %s loc=%d%n",
        //                   parent, loc);
        Tree elt = ((ArrayTypeTree) parent).getType();
        for (int i=0; i<loc; i++) {
          if (! (elt.getKind() == Tree.Kind.ARRAY_TYPE)) { // ArrayTypeTree
            if (debug) {
              System.out.printf("Element: %s is not an ArrayTypeTree and therefore false.", elt);
            }
            return false;
          }
          elt = ((ArrayTypeTree) elt).getType();
        }
        boolean b = elt.equals(leaf);
        if (debug) {
          System.out.printf("parent %s instanceof ArrayTypeTree: %b %s %s %d%n",
                           parent, elt.equals(leaf), elt, leaf, loc);
          System.out.printf("b=%s elt=%s leaf=%s%n", b, elt, leaf);
        }

        // TODO:  The parent criterion should be exact, not just "in".
        // Otherwise the criterion [1]  matches  [5 4 3 2 1].
        // This is a disadvantage of working from the inside out instead of the outside in.
        if (b) {
          locationRemaining.remove(locationRemaining.size()-1);
          pathRemaining = parentPath;
        } else {
          return false;
        }
      } else if (parent.getKind() == Tree.Kind.NEW_ARRAY) {
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
    // e.g.,  @Readonly List list;
    //        @Readonly List<String> list;
    Tree parent = pathRemaining.getParentPath().getLeaf();
    if (debug) {
      Tree leaf = pathRemaining.getLeaf();
      System.out.printf("No (remaining) inner type location:%n  leaf: %s %b%n  parent: %s %b%n  result: %s%n",
            Main.treeToString(leaf), isGenericOrArray(leaf),
            Main.treeToString(parent), isGenericOrArray(parent),
            ! isGenericOrArray(parent));
    }

    return ! isGenericOrArray(parent);
  }

  private boolean isGenericOrArray(Tree t) {
    return ((t.getKind() == Tree.Kind.PARAMETERIZED_TYPE)
            || (t.getKind() == Tree.Kind.ARRAY_TYPE)
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
    ((locationInParent == null)
     ? "outermost type"
     : ("( " + location.toString() + " )"));
  }
}
