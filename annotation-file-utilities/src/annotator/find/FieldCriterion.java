package annotator.find;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

public class FieldCriterion implements Criterion {

  public String varName;
  public Criterion varCriterion;
  public Criterion notInMethodCriterion;

  public FieldCriterion(String varName) {
    this.varName = varName;
    this.varCriterion = Criteria.is(Tree.Kind.VARIABLE, varName);
    this.notInMethodCriterion = Criteria.notInMethod();
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
    boolean var = leaf instanceof ParameterizedTypeTree
    || leaf instanceof IdentifierTree
    || leaf instanceof ArrayTypeTree
    || leaf instanceof PrimitiveTypeTree;

    if (leaf.getKind() == Tree.Kind.VARIABLE) {
      VariableTree vt = (VariableTree) leaf;
      String s = vt.getName().toString();
      boolean b =  varName.equals(vt.getName().toString()) &&
        notInMethodCriterion.isSatisfiedBy(path);
      // System.out.println("At (leaf.getKind() == Tree.Kind.VARIABLE)");
      // Why is the below commented out?  This branch is frequently taken.
//      return b;
    }

    if (varCriterion.isSatisfiedBy(path) &&
        notInMethodCriterion.isSatisfiedBy(path)) {
      return true;
    } else {
      return this.isSatisfiedBy(path.getParentPath());
    }
  }

  public Kind getKind() {
    return Kind.FIELD;
  }

  public String toString() {
    return "FieldCriterion: " + varName;
  }
}
