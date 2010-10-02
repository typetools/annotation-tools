package annotations.el;

import checkers.nullness.quals.Nullable;
import checkers.javari.quals.ReadOnly;

import java.util.*;

import annotations.*;
import annotations.util.coll.*;

/**
 * An <code>AElement</code> represents a Java element and the annotations it
 * carries. Some <code>AElements</code> may contain others; for example, an
 * {@link AClass} may contain {@link AMethod}s. Every <code>AElement</code>
 * usually belongs directly or indirectly to an {@link AScene}. Each subclass
 * of <code>AElement</code> represents one kind of annotatable element; its
 * name should make this clear.
 */
public class AElement {
    static <K extends /*@ReadOnly*/ Object> VivifyingMap<K, AElement> newVivifyingLHMap_AE() {
        return new VivifyingMap<K, AElement>(
                new LinkedHashMap<K, AElement>()) {
            @Override
            public AElement createValueFor(K k) /*@ReadOnly*/ {
                return new AElement(k);
            }

            @Override
            public boolean subPrune(AElement v) /*@ReadOnly*/ {
                return v.prune();
            }
        };
    }

    
    // Different than the above in that the elements are guaranteed to
    // contain a non-null "type" field.
    static <K extends /*@ReadOnly*/ Object> VivifyingMap<K, AElement> newVivifyingLHMap_AET() {
        return new VivifyingMap<K, AElement>(
                new LinkedHashMap<K, AElement>()) {
            @Override
            public AElement createValueFor(K k) /*@ReadOnly*/ {
                return new AElement(k, true);
            }

            @Override
            public boolean subPrune(AElement v) /*@ReadOnly*/ {
                return v.prune();
            }
        };
    }

    /**
     * The top-level annotations directly on this element.  Annotations on
     * subelements are in those subelements' <code>tlAnnotationsHere</code>
     * sets, not here.
     */
    public final Set<Annotation> tlAnnotationsHere;

    /** The type of a field or a method parameter */
    public final ATypeElement type; // initialized in constructor

    public Annotation lookup(String name) {
        for (Annotation anno : tlAnnotationsHere) {
            if (anno.def.name.equals(name)) {
                return anno;
            }
        }
        return null;
    }

    // general descrition of the element
    private Object description;

    AElement(Object description) {
        this(description, false);
    }

    AElement(Object description, boolean hasType) {
        tlAnnotationsHere = new LinkedHashSet<Annotation>();
        this.description = description;
        type = hasType ? new ATypeElement("type of " + description) : null;
    }

    /**
     * Returns whether this {@link AElement} equals <code>o</code> (see
     * warnings below). Generally speaking, two {@link AElement}s are equal if
     * they are of the same type, have the same {@link #tlAnnotationsHere}, and
     * have recursively equal, corresponding subelements.  Two warnings:
     *
     * <ul>
     * <li>While subelement collections usually remember the order in which
     * subelements were added and regurgitate them in this order, two
     * {@link AElement}s are equal even if order of subelements differs.
     * <li>Two {@link AElement}s are unequal if one has a subelement that the
     * other does not, even if the tree of elements rooted at the subelement
     * contains no annotations.  Thus, if you want to compare the
     * <em>annotations</em>, you should {@link #prune} both {@link AElement}s
     * first.
     * </ul>
     */
    @Override
    // Was final.  Removed that so that AnnotationDef can redefine.
    public boolean equals(/*@ReadOnly*/ Object o) /*@ReadOnly*/ {
        return o instanceof AElement &&
            equals((/*@ReadOnly*/ AElement) o);
    }

    /**
     * Returns whether this {@link AElement} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link AElement}.
     */
    /*
     * We need equals to be symmetric and operate correctly over the class
     * hierarchy.  Let x and y be objects of subclasses S and T, respectively,
     * of AElement.  x.equals((AElement) y) shall check that y is an S.  If so,
     * it shall call ((S) y).equalsS(x), which checks that x is a T and then
     * compares fields.
     */
    public boolean equals(/*@ReadOnly*/ AElement o) /*@ReadOnly*/ {
        return o.equalsElement(this);
    }

    final boolean equalsElement(/*@ReadOnly*/ AElement o) /*@ReadOnly*/ {
        return tlAnnotationsHere.equals(o.tlAnnotationsHere);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return getClass().getName().hashCode() + tlAnnotationsHere.hashCode()
            + (type == null ? 0 : type.hashCode());
    }

    /**
     * Removes empty subelements of this {@link AElement} depth-first; returns
     * whether this {@link AElement} is itself empty after pruning.
     */
    // In subclasses, we use & (not &&) because we want complete evaluation:
    // we should prune everything even if the first subelement is nonempty.
    public boolean prune() {
        return tlAnnotationsHere.isEmpty()
            & (type != null ? type.prune() : true);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("AElement: ");
      sb.append(description);
      sb.append(" : ");
      tlAnnotationsHereFormatted(sb);
      if (type!=null) {
    	  sb.append(' ');
    	  sb.append(type.toString());
      }
      return sb.toString();
    }

    public void tlAnnotationsHereFormatted(StringBuilder sb) {
        boolean first = true;
        for (Annotation aElement : tlAnnotationsHere) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(aElement.toString());
        }
    }

}
