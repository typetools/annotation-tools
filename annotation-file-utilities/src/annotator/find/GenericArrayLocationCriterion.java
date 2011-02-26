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

  // the full location list
  private List<Integer> location;
  // the last element of the location list
  public Integer locationInParent;
  // represents all but the last element of the location list
  // TODO: this field is initialized, but never read!
  private Criterion parentCriterion;

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
      this.parentCriterion = null;
      this.locationInParent = null;
    } else {
      List<Integer> newLocation = location.subList(0, location.size() - 1);
      this.parentCriterion = new GenericArrayLocationCriterion(newLocation);
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
      return false;
    }

    // System.out.printf("GenericArrayLocationCriterion.isSatisfiedBy():%n%s%n", path.getLeaf());

    if (locationInParent == null) {
      // no inner type location, want to annotate outermost type
      // e.g.,  @Readonly List list;
      //        @Readonly List<String> list;
      //        String @Readonly [] array;
      Tree leaf = path.getLeaf();
      Tree parent = path.getParentPath().getLeaf();
      boolean result = ((leaf.getKind() == Tree.Kind.NEW_ARRAY)
                        || (leaf.getKind() == Tree.Kind.NEW_CLASS)
                        || ((is_generic_or_array(leaf)
                             // or, it might be a raw type
                             || leaf instanceof IdentifierTree
                             || leaf instanceof MethodTree
                             || leaf instanceof TypeParameterTree
                             // I don't know why a GenericArrayLocationCriterion
                             // is being created in this case, but it is.
                             || leaf instanceof PrimitiveTypeTree
                             // TODO: do we need wildcards here?
                             // || leaf instanceof WildcardTree
                             )
                            && ! is_generic_or_array(parent)));
      // System.out.printf("GenericArrayLocationCriterion.isSatisfiedBy: locationInParent==null%n  leaf=%s (%s)%n  parent=%s (%s)%n  => %s (%s %s)%n", leaf, leaf.getClass(), parent, parent.getClass(), result, is_generic_or_array(leaf), ! is_generic_or_array(parent));
      return result;
    }

    TreePath pathRemaining = path;
    List<Integer> locationRemaining = new ArrayList<Integer>(location);

    while (locationRemaining.size() != 0) {
      // annotating an inner type
      Tree leaf = pathRemaining.getLeaf();
      if ((leaf.getKind() == Tree.Kind.NEW_ARRAY)
          && (locationRemaining.size() == 1)) {
        return true;
      }
      TreePath parentPath = pathRemaining.getParentPath();
      if (parentPath == null) {
        return false;
      }
      Tree parent = parentPath.getLeaf();
      // System.out.printf("locationRemaining: %s, leaf: %s parent: %s %s%n",
      //                   locationRemaining, Main.treeToString(leaf), Main.treeToString(parent), parent.getClass());
      int loc = locationRemaining.get(locationRemaining.size()-1);
      if (parent.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
        // annotating List<@A Integer>
        // System.out.printf("parent instanceof ParameterizedTypeTree: %s loc=%d%n",
        //                   Main.treeToString(parent), loc);
        ParameterizedTypeTree ptt = (ParameterizedTypeTree) parent;
        List<? extends Tree> childTrees = ptt.getTypeArguments();
        if ((childTrees.size() > loc)
            && childTrees.get(loc).equals(leaf)) {
          locationRemaining.remove(locationRemaining.size()-1);
          pathRemaining = parentPath;
        } else {
          // System.out.printf("Generic failed for leaf %s: %d %d %s%n",
          //                   leaf, childTrees.size(), loc,
          //                   ((childTrees.size() > loc) ? childTrees.get(loc) : null));
          return false;
        }
      } else if (parent.getKind() == Tree.Kind.EXTENDS_WILDCARD) {
        // annotating List<? extends @A Integer>
        // System.out.printf("parent instanceof extends WildcardTree: %s loc=%d%n",
        //                   Main.treeToString(parent), loc);
        WildcardTree wct = (WildcardTree) parent;
        Tree boundTree = wct.getBound();
        return boundTree.equals(leaf);
      } else if (parent.getKind() == Tree.Kind.SUPER_WILDCARD) {
        System.out.println("GenericArrayLocationCriterion::isSatisfiedBy: TODO: How should SUPER_WILDCARD be handled?");
        return false;
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
          if (! (elt instanceof ArrayTypeTree)) {
            return false;
          }
          elt = ((ArrayTypeTree) elt).getType();
        }
        // System.out.printf("parent %s instanceof ArrayTypeTree: %b %s %s %d%n",
        //                   parent, elt.equals(leaf), elt, leaf, loc);
        boolean b = elt.equals(leaf);
        // System.out.printf("b=%s elt=%s leaf=%s%n", b, elt, leaf);
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
        return true;
      } else {
        // System.out.printf("unrecognized parent kind = %s%n", parent.getKind());
        return false;
      }
    }

    // no (remaining) inner type location, want to annotate outermost type
    // e.g.,  @Readonly List list;
    //        @Readonly List<String> list;
    // Tree leaf = pathRemaining.getLeaf();
    Tree parent = pathRemaining.getParentPath().getLeaf();
    // System.out.printf("No (remaining) inner type location: %s %b %b%n", Main.treeToString(leaf), is_generic_or_array(leaf), is_generic_or_array(parent));
    return ! is_generic_or_array(parent);
  }

  private boolean is_generic_or_array(Tree t) {
    return ((t.getKind() == Tree.Kind.PARAMETERIZED_TYPE)
            || (t.getKind() == Tree.Kind.ARRAY_TYPE)
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
