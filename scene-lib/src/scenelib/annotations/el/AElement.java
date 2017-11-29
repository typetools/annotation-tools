package scenelib.annotations.el;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import scenelib.annotations.Annotation;
import scenelib.annotations.util.coll.VivifyingMap;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * An <code>AElement</code> represents a Java element and the annotations it
 * carries. Some <code>AElements</code> may contain others; for example, an
 * {@link AClass} may contain {@link AMethod}s. Every <code>AElement</code>
 * usually belongs directly or indirectly to an {@link AScene}. Each subclass
 * of <code>AElement</code> represents one kind of annotatable element; its
 * name should make this clear.
 */
public class AElement implements Cloneable {
    static <K extends Object> VivifyingMap<K, AElement> newVivifyingLHMap_AE() {
        return new VivifyingMap<K, AElement>(
                new LinkedHashMap<K, AElement>()) {
            @Override
            public AElement createValueFor(K k) {
                return new AElement(k);
            }

            @Override
            public boolean subPrune(AElement v) {
                return v.prune();
            }
        };
    }

    // Different from the above in that the elements are guaranteed to
    // contain a non-null "type" field.
    static <K extends Object> VivifyingMap<K, AElement> newVivifyingLHMap_AET() {
        return new VivifyingMap<K, AElement>(
                new LinkedHashMap<K, AElement>()) {
            @Override
            public AElement createValueFor(K k) {
                return new AElement(k, true);
            }

            @Override
            public boolean subPrune(AElement v) {
                return v.prune();
            }
        };
    }

    @SuppressWarnings("unchecked")
    <K, V extends AElement> void
    copyMapContents(VivifyingMap<K, V> orig, VivifyingMap<K, V> copy) {
        for (K key : orig.keySet()) {
            V val = orig.get(key);
            copy.put(key, (V) val.clone());
        }
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

    // general description of the element
    final Object description;

    AElement(Object description) {
        this(description, false);
    }

    AElement(Object description, boolean hasType) {
        this(description,
            hasType ? new ATypeElement("type of " + description) : null);
    }

    AElement(Object description, ATypeElement type) {
        tlAnnotationsHere = new LinkedHashSet<Annotation>();
        this.description = description;
        this.type = type;
    }

    AElement(AElement elem) {
        this(elem, elem.type);
    }

    AElement(AElement elem, ATypeElement type) {
        this(elem.description,
            type == null ? null : type.clone());
        tlAnnotationsHere.addAll(elem.tlAnnotationsHere);
    }

    AElement(AElement elem, Object description) {
        this(description, elem.type == null ? null : elem.type.clone());
        tlAnnotationsHere.addAll(elem.tlAnnotationsHere);
    }

    // Q: Are there any fields other than elements and maps that can't be shared?

    @Override
    public AElement clone() {
        return new AElement(this);
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
    public boolean equals(Object o) {
        return o instanceof AElement
            && ((AElement) o).equals(this);
    }

    /**
     * Returns whether this {@link AElement} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link AElement}.
     */
    // We need equals to be symmetric and operate correctly over the class
    // hierarchy.  Let x and y be objects of subclasses S and T, respectively,
    // of AElement.  x.equals((AElement) y) shall check that y is an S.  If so,
    // it shall call ((S) y).equalsS(x), which checks that x is a T and then
    // compares fields.
    public boolean equals(AElement o) {
        return o.equalsElement(this);
    }

    final boolean equalsElement(AElement o) {
        return o.tlAnnotationsHere.equals(tlAnnotationsHere)
            && (o.type == null ? type == null : o.type.equals(type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
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
            & (type == null || type.prune());
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
      // typeargs?
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

    public <R, T> R accept(ElementVisitor<R, T> v, T t) {
        return v.visitElement(this, t);
    }
}
