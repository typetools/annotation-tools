package annotator.find;

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

  private Criterion parentCriterion;
  private List<Integer> location;
  private Integer locationInParent;

  /**
   * Creates a new GenericArrayLocationCriterion specifying that the element
   * is an outer type, such as:
   *  <code>@A List<Integer></code>
   * or
   *  <code>@A Integer []</code>
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
      this.locationInParent = location.get(location.size() -1);
      List<Integer> newLocation = new ArrayList<Integer>(location);
      newLocation.remove(newLocation.size() -1);
      this.parentCriterion = new GenericArrayLocationCriterion(newLocation);
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
    Tree leaf = path.getLeaf();
    if (locationInParent == null) {
      // no inner type location, want to annotate outermost type
      // i.e.   @Readonly List list;
      //        @Readonly List<String> list;
      if (leaf instanceof ParameterizedTypeTree
          || leaf instanceof IdentifierTree
          || leaf instanceof ArrayTypeTree
          || leaf instanceof PrimitiveTypeTree
          || leaf instanceof MethodTree // for return value
          ) {
        return true;
      }
    } else {
      // annotating an inner type
      TreePath parentPath = path.getParentPath();
      if (parentPath != null) {
        Tree parent = parentPath.getLeaf();
        if (parent instanceof ParameterizedTypeTree) {
          // annotating List<@A Integer>
          ParameterizedTypeTree ptt = (ParameterizedTypeTree) parent;
          List<? extends Tree> childTrees = ptt.getTypeArguments();
          if (childTrees.size() > locationInParent) {
            boolean b = childTrees.get(locationInParent).equals(leaf);
            if (parentCriterion != null) {
              b = b && parentCriterion.isSatisfiedBy(parentPath);
            }
            return b;
          }
        } else if (parent instanceof ArrayTypeTree) {
          // annotating @A Integer []
          ArrayTypeTree att = (ArrayTypeTree) parent;
          Tree typeOfVtt = att.getType();
          boolean b =  typeOfVtt.equals(leaf);
          if (parentCriterion != null) {
            b = b &&
              parentCriterion.isSatisfiedBy(parentPath);
          }
          return b;
        }
      }
    }

    return false;
  }

  public Kind getKind() {
    return Criterion.Kind.GENERIC_ARRAY_LOCATION;
  }

  public String toString() {
    return "GenericArrayLocationCriterion at " +
    ((locationInParent == null)
     ? "outermost type"
     : ("( " + location.toString() + " )"));
  }
}
