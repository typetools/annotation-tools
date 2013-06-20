package annotations.el;

/*>>>
import checkers.javari.quals.ReadOnly;
*/

import java.util.Map;

import annotations.io.ASTPath;
import annotations.util.coll.VivifyingMap;

/**
 * Manages all annotations within expressions, that is, annotations on typecasts,
 * instanceofs, and object creations.
 * We can use this class for methods, field initializers, and static initializers.
 */
public class AExpression extends AElement {
    /** The method's annotated typecasts; map key is the offset of the checkcast bytecode */
    public final VivifyingMap<RelativeLocation, ATypeElement> typecasts =
            ATypeElement.<RelativeLocation>newVivifyingLHMap_ATE();

    /** The method's annotated "instanceof" tests; map key is the offset of the instanceof bytecode */
    public final VivifyingMap<RelativeLocation, ATypeElement> instanceofs =
            ATypeElement.<RelativeLocation>newVivifyingLHMap_ATE();

    /** The method's annotated "new" invocations; map key is the offset of the new bytecode */
    public final VivifyingMap<RelativeLocation, ATypeElement> news =
            ATypeElement.<RelativeLocation>newVivifyingLHMap_ATE();

    /** The method's annotated insert-typecast invocations; map key is the AST path to the insertion place */
    public final VivifyingMap<ASTPath, ATypeElementWithType> insertTypecasts =
            ATypeElementWithType.<ASTPath>newVivifyingLHMap_ATEWT();

    protected Object id;

    AExpression(Object id) {
        super(id);

        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(/*>>> @ReadOnly AExpression this, */ /*@ReadOnly*/ AElement o) {
        return o instanceof AExpression &&
            ((/*@ReadOnly*/ AExpression) o).equalsExpression(this);
    }

    protected boolean equalsExpression(/*>>> @ReadOnly AExpression this, */ /*@ReadOnly*/ AExpression o) {
        return typecasts.equals(o.typecasts)
                && instanceofs.equals(o.instanceofs)
                && news.equals(o.news)
                && insertTypecasts.equals(o.insertTypecasts);
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode(/*>>> @ReadOnly AExpression this*/) {
        return super.hashCode() + typecasts.hashCode() + instanceofs.hashCode()
                + news.hashCode() + insertTypecasts.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean prune() {
        return super.prune() & typecasts.prune() & instanceofs.prune()
                & news.prune() & insertTypecasts.prune();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // sb.append("AExpression ");
        // sb.append(id);
        for (Map.Entry<RelativeLocation, ATypeElement> em : typecasts.entrySet()) {
            sb.append("typecast: ");
            RelativeLocation loc = em.getKey();
            sb.append(loc);
            sb.append(": ");
            AElement ae = em.getValue();
            sb.append(ae.toString());
            sb.append(' ');
        }
        for (Map.Entry<RelativeLocation, ATypeElement> em : instanceofs.entrySet()) {
            sb.append("instanceof: ");
            RelativeLocation loc = em.getKey();
            sb.append(loc);
            sb.append(": ");
            AElement ae = em.getValue();
            sb.append(ae.toString());
            sb.append(' ');
        }
        for (Map.Entry<RelativeLocation, ATypeElement> em : news.entrySet()) {
            sb.append("new: ");
            RelativeLocation loc = em.getKey();
            sb.append(loc);
            sb.append(": ");
            AElement ae = em.getValue();
            sb.append(ae.toString());
            sb.append(' ');
        }
        for (Map.Entry<ASTPath, ATypeElementWithType> em : insertTypecasts.entrySet()) {
            sb.append("insert-typecast: ");
            ASTPath loc = em.getKey();
            sb.append(loc);
            sb.append(": ");
            AElement ae = em.getValue();
            sb.append(ae.toString());
            sb.append(' ');
        }
        return sb.toString();
    }
}
