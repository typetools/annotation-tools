package annotations.el;

import java.io.*;

import annotations.*;
import annotations.field.*;
import annotations.util.coll.*;

/**
 * A {@link DefCollector} collects all plain and top-level annotation
 * definitions used in a scene and supplies them in topological order to
 * subclass-defined methods.  It checks that all definitions of the
 * same annotation type are identical (modulo unknown array types, in which
 * case it {@linkplain AnnotationDef#unify unifies the definitions}).
 * This class exists primarily for the benefit of
 * {@link annotations.io.IndexFileWriter#write(AScene, Writer)}.
 */
public abstract class DefCollector {
    // Yay for LinkedHashSet! Preserves order and gives O(1) access!
    private final /*@NonNull*/ KeyedSet</*@NonNull*/ String, /*@NonNull*/ AnnotationDef> defs;

    // We don't care about order here--just that we can refer to top-level
    // definitions for retention policy information.
    private final /*@NonNull*/ KeyedSet</*@NonNull*/ String, /*@NonNull*/ TLAnnotationDef> tldefs;

    /**
     * Constructs a new {@link DefCollector}, which immediately collects all
     * the definitions from annotations the given scene.  Next call
     * {@link #visit} to have the definitions passed back to you in topological
     * order.  If the scene contains two irreconcilable definitions of the
     * same annotation type, a {@link DefException} is thrown.
     */
    public DefCollector(/*@NonNull*/ /*@ReadOnly*/ AScene<?> s)
            throws DefException {
        LinkedHashKeyedSet</*@NonNull*/ String, /*@NonNull*/ AnnotationDef> defs1
            = new LinkedHashKeyedSet</*@NonNull*/ String, /*@NonNull*/ AnnotationDef>(AnnotationDef.nameKeyer);
        defs = defs1;
        LinkedHashKeyedSet</*@NonNull*/ String, /*@NonNull*/ TLAnnotationDef> tldefs1
            = new LinkedHashKeyedSet</*@NonNull*/ String, /*@NonNull*/ TLAnnotationDef>(TLAnnotationDef.nameKeyer);
        tldefs = tldefs1;
        collect(s);
    }

    private void collect(/*@NonNull*/ /*@ReadOnly*/ AScene<?> s)
            throws DefException {
        for (/*@NonNull*/ AElement<?> p : s.packages.values())
            collect(p);
        for (/*@NonNull*/ AClass<?> c : s.classes.values())
            collect(c);
    }

    private void collect(/*@NonNull*/ AnnotationDef d) throws DefException {
        if (!defs.contains(d)) {
            // define the fields first
            for (/*@NonNull*/ AnnotationFieldType aft : d.fieldTypes.values())
                if (aft instanceof AnnotationAFT)
                    collect(((/*@NonNull*/ AnnotationAFT) aft).annotationDef);

            AnnotationDef oldD = defs.lookup(d.name);
            if (oldD == null)
                defs.add(d);
            else {
                AnnotationDef ud = AnnotationDef.unify(oldD, d);
                if (ud != null)
                    defs.replace(ud);
                else
                    throw new DefException(d.name);
            }
        }
    }

    private void collect(/*@NonNull*/ /*@ReadOnly*/ AElement<?> e)
            throws DefException {
        for (/*@NonNull*/ TLAnnotation<?> tla : e.tlAnnotationsHere) {
            /*@NonNull*/ TLAnnotationDef tld = tla.tldef;
            if (!tldefs.contains(tld)) {
                /*@NonNull*/ AnnotationDef d = tld.def;
                collect(d);

                TLAnnotationDef oldTld = tldefs.lookup(d.name);
                if (oldTld == null)
                    tldefs.add(tld);
                else {
                    TLAnnotationDef utld = TLAnnotationDef.unify(oldTld, tld);
                    if (utld != null)
                        tldefs.replace(utld);
                    else
                        throw new DefException(d.name);
                }
            }
        }
    }

    private void collect(/*@NonNull*/ /*@ReadOnly*/ ATypeElement<?> e)
            throws DefException {
        collect((/*@NonNull*/ /*@ReadOnly*/ AElement<?>) e);
        for (/*@NonNull*/ AElement<?> it : e.innerTypes.values())
            collect(it);
    }

    private void collect(/*@NonNull*/ /*@ReadOnly*/ AMethod<?> m)
            throws DefException {
        for (/*@NonNull*/ ATypeElement<?> b : m.bounds.values())
            collect(b);
        collect((/*@NonNull*/ /*@ReadOnly*/ ATypeElement<?>) m);
        collect(m.receiver);
        for (/*@NonNull*/ ATypeElement<?> p : m.parameters.values())
            collect(p);
        for (/*@NonNull*/ ATypeElement<?> l : m.locals.values())
            collect(l);
        for (/*@NonNull*/ ATypeElement<?> tc : m.typecasts.values())
            collect(tc);
        for (/*@NonNull*/ ATypeElement<?> i : m.instanceofs.values())
            collect(i);
        for (/*@NonNull*/ ATypeElement<?> n : m.news.values())
            collect(n);
    }

    private void collect(/*@NonNull*/ /*@ReadOnly*/ AClass<?> c)
            throws DefException {
        collect((/*@NonNull*/ /*@ReadOnly*/ AElement<?>) c);
        for (/*@NonNull*/ ATypeElement<?> b : c.bounds.values())
            collect(b);
        for (/*@NonNull*/ AMethod<?> m : c.methods.values())
            collect(m);
        for (/*@NonNull*/ ATypeElement<?> f : c.fields.values())
            collect(f);
    }

    /**
     * Override this method to perform some sort of subclass-specific
     * processing on the given {@link AnnotationDef}.
     */
    protected abstract void visitAnnotationDef(/*@NonNull*/ AnnotationDef d);

    /**
     * Override this method to perform some sort of subclass-specific
     * processing on the given {@link TLAnnotationDef}.
     */
    protected abstract void visitTLAnnotationDef(
            /*@NonNull*/ TLAnnotationDef tld);

    /**
     * Calls {@link #visitAnnotationDef} and {@link #visitTLAnnotationDef} on
     * the definitions collected from the scene that was passed to the
     * constructor.  If the definition of <code>A</code> contains a
     * subannotation of type <code>B</code>, then <code>B</code> is guaranteed
     * to be visited before <code>A</code>.
     * 
     * <p>
     * If the retention policy of an annotation type is known, a top-level
     * annotation definition is passed to {@link #visitTLAnnotationDef};
     * otherwise, an ordinary annotation definition is passed to
     * {@link #visitAnnotationDef}.  Under no circumstances are both methods
     * called for the same annotation type.
     */
    public final void visit() {
        for (/*@NonNull*/ AnnotationDef d : defs) {
            TLAnnotationDef tld = tldefs.lookup(d.name);
            if (tld == null)
                visitAnnotationDef(d);
            else
                visitTLAnnotationDef((/*@NonNull*/ TLAnnotationDef) tld);
        }
    }
}
