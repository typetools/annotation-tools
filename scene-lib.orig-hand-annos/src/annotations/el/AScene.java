package annotations.el;

import java.util.*;

import annotations.*;
import annotations.util.coll.*;

/**
 * An <code>AScene</code> (annotated scene) represents the annotations on a
 * set of Java classes and packages along with the definitions of some or all of
 * the annotation types used.
 * 
 * <p>
 * Each client of the annotation library may wish to use its own representation
 * for certain kinds of annotations instead of a simple name-value map; thus, a
 * layer of abstraction in the storage of annotations was introduced. Any object
 * that implements {@link annotations.Annotation} can serve as an annotation in
 * a scene; the client should specify the common supertype of all the annotation
 * objects in the scene via the type parameter <code>A</code> of
 * <code>AScene</code> and all <code>AElement</code> subclasses.
 * 
 * <p>
 * A client that creates a scene must provide an
 * {@link annotations.AnnotationFactory} so that other code accessing the scene
 * can create annotations in the correct format. Clients that wish to use a
 * simple name-value map may specify A = {@link annotations.SimpleAnnotation}
 * and provide a {@link annotations.SimpleAnnotationFactory}.
 * 
 * <p>
 * <code>AScene</code>s and many {@link AElement}s can contain other
 * {@link AElement}s. When these objects are created, their collections of
 * subelements are empty. In order to associate an annotation with a particular
 * Java element in an <code>AScene</code>, one must first ensure that an
 * appropriate {@link AElement} exists in the <code>AScene</code>. To this
 * end, the maps of subelements have a <code>vivify</code> method. Calling
 * <code>vivify</code> to access a particular subelement will return the
 * subelement if it already exists; otherwise it will create and then return the
 * subelement. (Compare to vivification in Perl.) For example, the following
 * code will obtain an {@link AMethod} representing <code>Foo.bar</code> in
 * the <code>AScene</code>
 * <code>s</code>, creating it if it did not
 * already exist:
 * 
 * <pre>
 * AMethod&lt;A&gt; m = s.classes.vivify(&quot;Foo&quot;).methods.vivify(&quot;bar&quot;);
 * </pre>
 * 
 * <p>
 * Then one can add an annotation to the method:
 * 
 * <pre>
 * m.annotationsHere.add(new TLAnnotation(
 *     new TLAnnotationDef(readOnlyDef, RetentionPolicy.RUNTIME),
 *     new SimpleAnnotation(readOnlyDef, Collections.emptyMap())
 * ));
 * </pre>
 * 
 * <p>
 * {@link LinkedHashMap}s and {@link LinkedHashKeyedSet}s are now used to
 * store all subelements and annotations so that the order is more
 * deterministic.
 */
public final class AScene<A extends Annotation> {
    /**
     * An {@link AnnotationFactory} that builds annotations of the appropriate
     * type for this {@link AScene}.  Clients adding annotations to the scene
     * are recommended to use {@link #af}, but if they have another way of
     * creating annotations of the appropriate type, they may add annotations
     * created that way.
     */
    public final /*@NonNull*/ AnnotationFactory<A> af;

    /** This scene's annotated packages; map key is package name */
    public final /*@NonNull*/ VivifyingMap</*@NonNull*/ String, /*@NonNull*/ AElement<A>> packages =
            AElement.newVivifyingLHMap_AE();

    /** This scene's annotated classes; map key is class name */
    public final /*@NonNull*/ VivifyingMap</*@NonNull*/ String, /*@NonNull*/ AClass<A>> classes =
            new VivifyingMap</*@NonNull*/ String, /*@NonNull*/ AClass<A>>(
                    new LinkedHashMap</*@NonNull*/ String, /*@NonNull*/ AClass<A>>()) {
                @Override
                public /*@NonNull*/ AClass<A> createValueFor(
                /*@NonNull*/ String k) {
                    return new AClass<A>();
                }

                @Override
                public boolean subPrune(/*@NonNull*/ AClass<A> v) {
                    return v.prune();
                }
            };

    /**
     * Creates a new {@link AScene} with no classes or packages, storing
     * the given {@link AnnotationFactory} in {@link #af} for future
     * reference.
     */
    public AScene(/*@NonNull*/ AnnotationFactory<A> af) {
        this.af = af;
    }

    /**
     * Returns whether this {@link AScene} equals <code>o</code>; the
     * commentary and the cautionary remarks on {@link AElement#equals(Object)}
     * also apply to {@link AScene#equals(Object)}.
     */
    @Override
    public boolean equals(/*@ReadOnly*/ Object o) /*@ReadOnly*/ {
        return o instanceof AScene
                && equals((/*@NonNull*/ /*@ReadOnly*/ AScene<?>) o);
    }

    /**
     * Returns whether this {@link AScene} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link AScene}.
     */
    public boolean equals(/*@NonNull*/ /*@ReadOnly*/ AScene<?> o) /*@ReadOnly*/ {
        return classes.equals(o.classes) && packages.equals(o.packages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return classes.hashCode() + packages.hashCode();
    }

    /**
     * Removes empty subelements of this {@link AScene} depth-first; returns
     * whether this {@link AScene} is itself empty after pruning.
     */
    public boolean prune() {
        return classes.prune() & packages.prune();
    }
}
