package annotations.el;

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
public class AElement<A extends Annotation> {
    static <K, A extends Annotation> /*@NonNull*/ VivifyingMap<K, /*@NonNull*/ AElement<A>> newVivifyingLHMap_AE() {
        return new VivifyingMap<K, /*@NonNull*/ AElement<A>>(
                new LinkedHashMap<K, /*@NonNull*/ AElement<A>>()) {
            @Override
            public AElement<A> createValueFor(K k) {
                return new AElement<A>();
            }

            @Override
            public boolean subPrune(AElement<A> v) {
                return v.prune();
            }
        };
    }
    
    /**
     * The top-level annotations directly on this element, keyed by annotation
     * type name.  Annotations on subelements are in those subelements'
     * <code>tlAnnotationsHere</code> sets, not here.
     * 
     * <p>
     * Be careful when storing annotations in this keyed set.  If you try to
     * {@link KeyedSet#add add} two different annotations of the same type
     * name, the keyed set will throw an exception.  If you want to overwrite
     * an existing annotation of the same type (if any), use
     * <code>tlAnnotationsHere.</code>{@link KeyedSet#replace replace} instead
     * of {@link KeyedSet#add add}.
     */
    public final /*@NonNull*/ KeyedSet</*@NonNull*/ String, /*@NonNull*/ TLAnnotation<A>> tlAnnotationsHere;
    
    /**
     * Looks up an ordinary annotation directly on this element by type name;
     * returns it or <code>null</code> if this element has no annotation of
     * the given type name.  Clients that don't care about the retention
     * policy may find it more convenient to use this method than to call
     * {@link #tlAnnotationsHere}.{@link KeyedSet#lookup lookup} and then
     * take the ordinary annotation out of the top-level one.
     */
    public final A lookupAnnotation(/*@NonNull*/ String annoTypeName) {
        TLAnnotation<A> tla = tlAnnotationsHere.lookup(annoTypeName);
        return (tla == null) ? null : tla.ann;
    }

    AElement() {
        /*@NonNull*/ LinkedHashKeyedSet</*@NonNull*/ String, /*@NonNull*/ TLAnnotation<A>>
            tlAnnotationsHere1 =
            new LinkedHashKeyedSet</*@NonNull*/ String, /*@NonNull*/ TLAnnotation<A>>(
                    new Keyer</*@NonNull*/ String, /*@NonNull*/ TLAnnotation<A>>() {
                        public /*@NonNull*/ String getKeyFor(
                                /*@NonNull*/ TLAnnotation<A> v) {
                            return v.tldef.def.name;
                        }
                    });
        tlAnnotationsHere = tlAnnotationsHere1;
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
    public final boolean equals(/*@ReadOnly*/ Object o) /*@ReadOnly*/ {
        return o instanceof AElement &&
            equals((/*@NonNull*/ /*@ReadOnly*/ AElement<?>) o);
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
    public boolean equals(/*@NonNull*/ /*@ReadOnly*/ AElement<?> o) /*@ReadOnly*/ {
        return o.equalsElement(this);
    }
    
    final boolean equalsElement(/*@NonNull*/ /*@ReadOnly*/ AElement<?> o) /*@ReadOnly*/ {
        return tlAnnotationsHere.equals(o.tlAnnotationsHere);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return getClass().getName().hashCode() + tlAnnotationsHere.hashCode();
    }
    
    /**
     * Removes empty subelements of this {@link AElement} depth-first; returns
     * whether this {@link AElement} is itself empty after pruning.
     */
    // In subclasses, we use & (not &&) because we want complete evaluation:
    // we should prune everything even if the first subelement is nonempty.
    public boolean prune() {
        return tlAnnotationsHere.isEmpty();
    }
}
