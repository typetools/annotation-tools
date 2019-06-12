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

  @Override
  public Kind getKind() {
    return Kind.IN_METHOD;
  }

  @Override
  public boolean isSatisfiedBy(TreePath path, Tree leaf) {
    if (path == null) {
      return false;
    }
    assert path.getLeaf() == leaf;
    return isSatisfiedBy(path);
  }

  @Override
  public boolean isSatisfiedBy(TreePath path) {
    Criteria.dbug.debug("InMethodCriterion.isSatisfiedBy(%s); this=%s%n",
        Main.leafString(path), this.toString());

    // true if the argument is a variable declaration.
    boolean inDecl = false;
    // Ignore the value if inDecl==false.  Otherwise:
    // true if in a static variable declaration, false if in a member variable declaration.
    boolean staticDecl = false;
    do {
      Tree leaf = path.getLeaf();
      if (leaf.getKind() == Tree.Kind.METHOD) {
        boolean b = sigMethodCriterion.isSatisfiedBy(path);
        Criteria.dbug.debug("%s%n", "InMethodCriterion.isSatisfiedBy => b");
        return b;
      }
      if (leaf.getKind() == Tree.Kind.VARIABLE) { // variable declaration
        VariableTree varDecl = (VariableTree) leaf;
        inDecl = true;
        ModifiersTree mods = varDecl.getModifiers();
        staticDecl = mods.getFlags().contains(Modifier.STATIC);
      }
      path = path.getParentPath();
    } while (path != null && path.getLeaf() != null);

    // We didn't find the method.  Return true if in a varable declarator,
    // which is initialization code that will go in <init> or <clinit>.
    boolean result = inDecl && (staticDecl ? "<clinit>()V" : "<init>()V").equals(name);
    Criteria.dbug.debug("InMethodCriterion.isSatisfiedBy => %s%n", result);
    return result;
  }

  @Override
  public String toString() {
    return "in method '" + name + "'";
  }
}
