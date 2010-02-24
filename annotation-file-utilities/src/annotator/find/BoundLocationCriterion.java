package annotator.find;

import java.util.List;

import annotations.el.BoundLocation;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.util.TreePath;

public class BoundLocationCriterion implements Criterion {

  private Criterion parentCriterion;
  private Integer boundIndex;
  private Integer paramIndex;


  public BoundLocationCriterion(BoundLocation boundLoc) {
    this(boundLoc.boundIndex, boundLoc.paramIndex);
  }

  private BoundLocationCriterion(Integer boundIndex, Integer paramIndex) {
    this.boundIndex = boundIndex;
    this.paramIndex = paramIndex;

    if (boundIndex != null) {
      this.parentCriterion = new BoundLocationCriterion(null, paramIndex);
    } else if (paramIndex != null) {
      this.parentCriterion = null;
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

    TreePath parentPath = path.getParentPath();
    if (parentPath == null) {
      return false;
    }

    Tree parent = parentPath.getLeaf();
    if (parent == null) {
      return false;
    }

    boolean returnValue = false;

    // if boundIndex is not null, need to check that this is right bound
    // in parent
    if (boundIndex != null) {
      if (parent instanceof TypeParameterTree) {
        TypeParameterTree tpt = (TypeParameterTree) parent;
        List<? extends Tree> bounds = tpt.getBounds();
        if (boundIndex < bounds.size()) {
          if (bounds.get(boundIndex) == leaf) {
            returnValue = parentCriterion.isSatisfiedBy(parentPath);
          }
        }
      }
    } else if (paramIndex != null) {
      // if paramIndex is not null, need to ensure this present
      // typeparameter tree represents the correct parameter
      if (parent instanceof MethodTree || parent instanceof ClassTree) {
        List<? extends TypeParameterTree> params = null;

        if (parent instanceof MethodTree) {
          params = ((MethodTree) parent).getTypeParameters();
        } else if (parent instanceof ClassTree) {
          params = ((ClassTree) parent).getTypeParameters();
        }

        if (paramIndex < params.size()) {
          if (params.get(paramIndex) == leaf) {
            returnValue = true;
          }
        }
      }
    }

    if (!returnValue) {
      return this.isSatisfiedBy(parentPath);
    } else {
      return true;
    }
  }

  /** {@inheritDoc} */
  public Kind getKind() {
    return Kind.BOUND_LOCATION;
  }

  /** {@inheritDoc} */
  public String toString() {
    return "BoundCriterion: at param index: " + paramIndex +
      " at bound index: " + boundIndex;
  }
}
