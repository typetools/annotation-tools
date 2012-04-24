package annotator.find;

import java.util.List;

import annotations.el.TypeIndexLocation;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

/**
 * A criterion to find a given extends or implements clause.
 */
public class ExtImplsLocationCriterion implements Criterion {

  private final String classname;
  private final Integer index;

  /**
   * @param classname The class name; for debugging purposes only, not used to constrain.
   * @param tyLoc -1 for an extends clause, >= 0 for the zero-based implements clause.
   */
  public ExtImplsLocationCriterion(String classname, TypeIndexLocation tyLoc) {
    this.classname = classname;
    this.index = tyLoc.typeIndex;
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

    // System.out.printf("ExtImplsLocationCriterion.isSatisfiedBy(%s):%n  leaf=%s (%s)%n", path, leaf, leaf.getClass());

    TreePath parentPath = path.getParentPath();
    if (parentPath == null) {
      return false;
    }

    Tree parent = parentPath.getLeaf();
    if (parent == null) {
      return false;
    }

    // System.out.printf("ExtImplsLocationCriterion.isSatisfiedBy(%s):%n  leaf=%s (%s)%n  parent=%s (%s)%n", path, leaf, leaf.getClass(), parent, parent.getClass());

    boolean returnValue = false;

    if (parent instanceof ClassTree) {
        ClassTree ct = (ClassTree) parent;

        if (index==-1) {
            Tree ext = ct.getExtendsClause();
            if (ext == leaf) {
                returnValue = true;
            }
        } else {
            List<? extends Tree> impls = ct.getImplementsClause();
            if (index < impls.size() && impls.get(index) == leaf) {
                returnValue = true;
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
  @Override
  public Kind getKind() {
    return Kind.EXTIMPLS_LOCATION;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "ExtImplsLocationCriterion: class " + classname + " at type index: " + index;
  }
}
