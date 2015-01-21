package annotations.el;

/*>>>
import org.checkerframework.checker.javari.qual.ReadOnly;
*/

import java.util.LinkedHashMap;
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

    /** The method's annotated member references; map key is the offset of the ??? bytecode */
    public final VivifyingMap<RelativeLocation, ATypeElement> refs =
            ATypeElement.<RelativeLocation>newVivifyingLHMap_ATE();

    /** The method's annotated method invocations; map key is the offset of the invokestatic bytecode (?) */
    public final VivifyingMap<RelativeLocation, AElement> calls =
            AElement.<RelativeLocation>newVivifyingLHMap_AE();

    /** The method's annotated lambda expressions; map key is the offset of the invokedynamic bytecode */
    public final VivifyingMap<RelativeLocation, AMethod> funs =
            new VivifyingMap<RelativeLocation, AMethod>(
                    new LinkedHashMap<RelativeLocation, AMethod>()) {
        @Override
        public AMethod createValueFor(RelativeLocation k) {
            return new AMethod("" + k);  // FIXME: find generated method name
        }

        @Override
        public boolean subPrune(AMethod v) {
            return v.prune();
        }
    };

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
        return super.equals(o)
                && typecasts.equals(o.typecasts)
                && instanceofs.equals(o.instanceofs)
                && news.equals(o.news)
                && calls.equals(o.calls)
                && refs.equals(o.refs)
                && funs.equals(o.funs);
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode(/*>>> @ReadOnly AExpression this*/) {
        return super.hashCode() + typecasts.hashCode()
            + instanceofs.hashCode() + news.hashCode()
            + calls.hashCode() + refs.hashCode() + funs.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean prune() {
        return super.prune() & typecasts.prune() & instanceofs.prune()
                & news.prune() & calls.prune() & refs.prune() & funs.prune();
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
        for (Map.Entry<RelativeLocation, AElement> em : calls.entrySet()) {
            sb.append("call: ");
            RelativeLocation loc = em.getKey();
            sb.append(loc);
            sb.append(": ");
            AElement ae = em.getValue();
            sb.append(ae.toString());
            sb.append(' ');
        }
        for (Map.Entry<RelativeLocation, AMethod> em : funs.entrySet()) {
            sb.append("fun: ");
            RelativeLocation loc = em.getKey();
            sb.append(loc);
            sb.append(": ");
            AElement ae = em.getValue();
            sb.append(ae.toString());
            sb.append(' ');
        }
        return sb.toString();
    }

    @Override
    public <R, T> R accept(ElementVisitor<R, T> v, T t) {
        return v.visitExpression(this, t);
    }
}
