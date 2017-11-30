package annotator.find;

import scenelib.annotations.el.RelativeLocation;
import annotator.scanner.MemberReferenceScanner;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public class MemberReferenceCriterion implements Criterion {
  private final String methodName;
  private final RelativeLocation loc;

  public MemberReferenceCriterion(String methodName, RelativeLocation loc) {
    this.methodName = methodName;
    this.loc = loc;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path, Tree leaf) {
    if (path == null) {
      return false;
    }
    assert path.getLeaf() == leaf;
    return isSatisfiedBy(path);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path) {
    if (path == null) {
      return false;
    }

    Tree leaf = path.getLeaf();

    if (leaf.getKind() == Tree.Kind.MEMBER_REFERENCE) {
      int indexInSource =
          MemberReferenceScanner.indexOfMemberReferenceTree(path, leaf);
      boolean b;
      if (loc.isBytecodeOffset()) {
        int indexInClass =
            MemberReferenceScanner.getMemberReferenceIndex(methodName,
                loc.offset);
        b = (indexInSource == indexInClass);
      } else {
        b = (indexInSource == loc.index);
      }
      return b;
    } else {
      boolean b = this.isSatisfiedBy(path.getParentPath());
      return b;
    }
  }

  @Override
  public Kind getKind() {
    return Kind.METHOD_REFERENCE;
  }

  @Override
  public String toString() {
    return "MemberReferenceCriterion: in method: " + methodName + " location: " + loc;
  }
}
