package annotator.find;

import java.util.List;

import annotations.el.LocalLocation;
import annotator.scanner.LocalVariableScanner;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Pair;

/**
 * Criterion for being a specific local variable.
 */
public class LocalVariableCriterion implements Criterion {

  private final String fullMethodName;
  private final LocalLocation loc;

  public LocalVariableCriterion(String methodName, LocalLocation loc) {
    this.fullMethodName = methodName.substring(0, methodName.indexOf(")") + 1);
    this.loc = loc;
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

    TreePath parentPath = path.getParentPath();
    if (parentPath != null) {
      Tree parent = parentPath.getLeaf();
      if (parent != null) {
        if ((parent instanceof VariableTree)
            // Avoid matching formal parameters
            && (! (parentPath.getParentPath().getLeaf() instanceof MethodTree))) {
          VariableTree vtt = (VariableTree) parent;
          String varName = vtt.getName().toString();

          if (loc.varName!=null && loc.varName.equals(varName)) {
            int varIndex = LocalVariableScanner.indexOfVarTree(path, vtt, varName);

            if (loc.varIndex==varIndex) {
              // the location specifies a variable name and index and it matches the current variable
              // -> hurray
              return true;
            }
            return false;
          }

          Pair<String, Pair<Integer, Integer>> key =
                  Pair.of(fullMethodName, Pair.of(loc.index, loc.scopeStart));
          String potentialVarName =
                  LocalVariableScanner.getFromMethodNameIndexMap(key);
          if (potentialVarName != null) {
            if (varName.equals(potentialVarName)) {
              // now use methodNameCounter to ensure that if this is the
              // i'th variable of this name, its offset is the i'th offset
              // of all variables with this name
              List<Integer> allOffsetsWithThisName =
                      LocalVariableScanner.getFromMethodNameCounter(fullMethodName, potentialVarName);
//                methodNameCounter.get(fullMethodName).get(potentialVarName);
              Integer thisVariablesOffset =
                      allOffsetsWithThisName.indexOf(loc.scopeStart);

              // now you need to make sure that this is the
              // thisVariablesOffset'th variable tree in the entire source
              int i = LocalVariableScanner.indexOfVarTree(path, parent, potentialVarName);

              if (i == thisVariablesOffset) {
                return true;
              }
            }
          }
        } else {
          // If present leaf does not yet satisfy the local variable
          // criterion, note that it actually is the correct local variable
          // if any of its parents satisfy this local variable criterion
          // (and going all the way up past the top-level tree is taken
          // care of by the check for null above.
          //
          // For example, if you have the tree for "Integer"
          // for the local variable "List<Integer> foo;"
          // the parent of the current leaf will satisfy the local variable
          // criterion directly.  The fact that you will never return true
          // for something that is not the correct local variable comes
          // from the fact that you can't contain one local variable
          // within another.  For example, you can't have
          // List<Integer bar> foo;
          // Thus, no local variable tree can contain another local
          // variable tree.
          // Another general example:
          // List<Integer> foo = ...;
          // If the tree for ... contains one local variable, there is no fear
          // of a conflict with "List<Integer>", because "List<Integer> foo"
          // is a subtree of "List<Integer> foo = ...;", so the two
          // (possibly) conflicting local variable trees are both subtrees
          // of the same tree, and neither is an ancestor of the other.
          return this.isSatisfiedBy(parentPath);
          // To do: should stop this once it gets to method? or some other top level?
        }
      }
    }
    return false;
  }


  @Override
  public Kind getKind() {
    return Kind.LOCAL_VARIABLE;
  }

  @Override
  public String toString() {
    return "LocalVariableCriterion: in: " + fullMethodName + " loc: " + loc;
  }
}
