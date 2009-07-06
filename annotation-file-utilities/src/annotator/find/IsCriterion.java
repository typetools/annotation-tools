package annotator.find;

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

    /**
     * {@inheritDoc}
     */
    public Kind getKind() {
        return Kind.HAS_KIND;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSatisfiedBy(TreePath path) {
        if (path == null)
            return false;
        if (path.getLeaf().getKind() != kind)
          return false;
        if (path.getLeaf().getKind() == Tree.Kind.VARIABLE) {
            if (((VariableTree)path.getLeaf()).getName().toString().equals(this.name))
                return true;
        } else if (path.getLeaf().getKind() == Tree.Kind.METHOD) {
            if (((MethodTree)path.getLeaf()).getName().toString().equals(this.name))
              return true;
        }
        
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return kind.toString().toLowerCase() + " '" + name + "'";
    }

}
