package annotations.el;

import checkers.nullness.quals.Nullable;
import checkers.javari.quals.ReadOnly;

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
 * layer of abstraction in the storage of annotations was introduced.
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
 * the <code>AScene</code> <code>s</code>, creating it if it did not
 * already exist:
 *
 * <pre>
 * AMethod&lt;A&gt; m = s.classes.vivify("Foo").methods.vivify("bar");
 * </pre>
 *
 * <p>
 * Then one can add an annotation to the method:
 *
 * <pre>
 * m.annotationsHere.add(new Annotation(
 *     new AnnotationDef(readOnlyDef, RetentionPolicy.RUNTIME, true),
 *     new Annotation(readOnlyDef, Collections.emptyMap())
 * ));
 * </pre>
 */
public final class AScene {
  public static boolean debugFoundMap = false;

    /** This scene's annotated packages; map key is package name */
    public final VivifyingMap<String, AElement> packages =
            AElement.<String>newVivifyingLHMap_AE();

    /** This scene's annotated classes; map key is class name */
    public final VivifyingMap<String, AClass> classes =
            new VivifyingMap<String, AClass>(
                    new LinkedHashMap<String, AClass>()) {
                @Override
                public  AClass createValueFor(
                 String k) /*@ReadOnly*/ {
                    return new AClass(k);
                }

                @Override
                public boolean subPrune(AClass v) /*@ReadOnly*/ {
                    return v.prune();
                }
            };

    /**
     * Creates a new {@link AScene} with no classes or packages.
     */
    public AScene() {
    }

    /**
     * Returns whether this {@link AScene} equals <code>o</code>; the
     * commentary and the cautionary remarks on {@link AElement#equals(Object)}
     * also apply to {@link AScene#equals(Object)}.
     */
    @Override
    public boolean equals(/*@ReadOnly*/ Object o) /*@ReadOnly*/ {
        return o instanceof AScene
                && equals((/*@ReadOnly*/ AScene) o);
    }

    /**
     * Returns whether this {@link AScene} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link AScene}.
     */
    public boolean equals(/*@ReadOnly*/ AScene o) /*@ReadOnly*/ {
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

    /** Returns a string representation. **/
    public String unparse() {
        StringBuilder sb = new StringBuilder();
        sb.append("packages:\n");
        for (Map.Entry<String, AElement> entry : packages.entrySet()) {
            sb.append("  " + entry.getKey() + " => " + entry.getValue() + "\n");
        }
        sb.append("classes:\n");
        for (Map.Entry<String, AClass> entry : classes.entrySet()) {
            sb.append("  " + entry.getKey() + " => " + "\n");
            sb.append(entry.getValue().unparse("    "));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return unparse();
    }
}
