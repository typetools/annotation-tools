package annotations.el;

/*>>>
import checkers.javari.quals.ReadOnly;
*/

import java.util.Map;

import annotations.util.coll.VivifyingMap;

/**
 * ABlock has local variables in scope.
 * We currently directly use them only for static initializer blocks, which are
 * not methods, but can declare local variables.
 */
public class ABlock extends AExpression {
    // Currently we don't validate the local locations (e.g., that no two
    // distinct ranges for the same index overlap).
    /** The method's annotated local variables; map key contains local variable location numbers */
    public final VivifyingMap<LocalLocation, AElement> locals =
            AElement.<LocalLocation>newVivifyingLHMap_AET();

    ABlock(Object id) {
        super(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(/*>>> @ReadOnly ABlock this,*/ /*@ReadOnly*/ AElement o) {
        return o instanceof ABlock &&
            ((/*@ReadOnly*/ ABlock) o).equalsBlock(this);
    }

    protected boolean equalsBlock(/*>>> @ReadOnly ABlock this,*/ /*@ReadOnly*/ ABlock o) {
        return locals.equals(o.locals) && super.equalsExpression(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode(/*>>> @ReadOnly ABlock this*/) {
        return super.hashCode() + locals.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean prune() {
        return super.prune() & locals.prune();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // sb.append("ABlock ");
        // sb.append(id);
        for (Map.Entry<LocalLocation, AElement> em : locals.entrySet()) {
            LocalLocation loc = em.getKey();
            sb.append(loc);
            sb.append(": ");
            AElement ae = em.getValue();
            sb.append(ae.toString());
            sb.append(' ');
        }
        sb.append(super.toString());
        return sb.toString();
    }
}
