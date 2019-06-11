package annotator.find;

import annotator.scanner.CommonScanner;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;

/**
 * Represents the criterion that a program element has a particular type and
 * name.
 */
final class IsCriterion implements Criterion {

  private final Tree.Kind kind;
  private final String name;

  IsCriterion(Tree.Kind kind, String name) {
    this.kind = kind;
    this.name = name;
  }

  @Override
  public Kind getKind() {
    return Kind.HAS_KIND;
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
    if (path == null) {
      return false;
    }
    Tree tree = path.getLeaf();
    if (CommonScanner.hasClassKind(tree)) {
      return InClassCriterion.isSatisfiedBy(path, name, /*exactMatch=*/ true);
    }
    if (tree.getKind() != kind) {
      return false;
    }
    switch (tree.getKind()) {
    case VARIABLE:
      String varName = ((VariableTree)tree).getName().toString();
      return varName.equals(name);
    case METHOD:
      String methodName = ((MethodTree)tree).getName().toString();
      return methodName.equals(name);
    // case CLASS:
    //  return InClassCriterion.isSatisfiedBy(path, name, /*exactMatch=*/ true);
    default:
      throw new Error("unknown tree kind " + kind);
    }
  }

  @Override
  public String toString() {
    return "is " + kind.toString().toLowerCase() + " '" + name + "'";
  }

}
