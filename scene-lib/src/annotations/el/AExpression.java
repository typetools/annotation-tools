package annotations.el;

import checkers.javari.quals.ReadOnly;

import java.util.Map;

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

    protected Object id;
    
    AExpression(Object id) {
        super(id);
        
        this.id = id;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(/*@ReadOnly*/ AElement o) /*@ReadOnly*/ {
        return o instanceof AExpression &&
            ((/*@ReadOnly*/ AExpression) o).equalsExpression(this);
    }
    
	protected boolean equalsExpression(/*@ReadOnly*/ AExpression o) /*@ReadOnly */{
		return typecasts.equals(o.typecasts)
				&& instanceofs.equals(o.instanceofs)
				&& news.equals(o.news);
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
		return super.hashCode() + typecasts.hashCode() + instanceofs.hashCode()
				+ news.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean prune() {
		return super.prune() & typecasts.prune() & instanceofs.prune()
				& news.prune();
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
        return sb.toString();
    }
}
