package annotator.find;

import javax.lang.model.element.Modifier;

import annotator.Main;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

/**
 * Represents the criterion that a program element is in a method with a
 * certain name.
 */
final class InMethodCriterion implements Criterion {

  public final String name;
  private final IsSigMethodCriterion sigMethodCriterion;

  InMethodCriterion(String name) {
    this.name = name;
    sigMethodCriterion = new IsSigMethodCriterion(name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Kind getKind() {
    return Kind.IN_METHOD;
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
    Criteria.dbug.debug("InMethodCriterion.isSatisfiedBy(%s); this=%s%n",
        Main.pathToString(path), this.toString());
    boolean staticDecl = false;
    boolean result = false;

    do {
      if (path.getLeaf().getKind() == Tree.Kind.METHOD) {
        boolean b = sigMethodCriterion.isSatisfiedBy(path);
        Criteria.dbug.debug("%s%n", "InMethodCriterion.isSatisfiedBy => b");
        return b;
      }
      if (path.getLeaf().getKind() == Tree.Kind.VARIABLE) {
        ModifiersTree mods = ((VariableTree) path.getLeaf()).getModifiers();
        staticDecl = mods.getFlags().contains(Modifier.STATIC);
      }
      path = path.getParentPath();
    } while (path != null && path.getLeaf() != null);

    result = (staticDecl ? "<clinit>()V" : "<init>()V").equals(name);

    Criteria.dbug.debug("InMethodCriterion.isSatisfiedBy => %s%n", result);
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "in method '" + name + "'";
  }
}
