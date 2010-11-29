package annotations.io;

import java.io.*;
import java.util.*;

import annotations.*;
import annotations.el.*;
import annotations.field.*;
import annotations.util.*;

import static annotations.io.IOUtils.*;

/**
 * <code>IndexFileWriter</code> provides a static method that writes a given
 * {@link AScene} to a given {@link Writer} in index file format.
 */
public final class IndexFileWriter<A extends Annotation> {
    final /*@NonNull*/ /*@ReadOnly*/ AScene<A> scene;

    private static final /*@NonNull*/ String INDENT = "    ";

    void printAnnotationDefBody(AnnotationDef d) {
        for (/*@NonNull*/ Map.Entry</*@NonNull*/ String, /*@NonNull*/ AnnotationFieldType> f : d.fieldTypes
                .entrySet()) {
            pw.println(INDENT + f.getValue().toString() + " " + f.getKey());
        }
        pw.println();
    }

    private class OurDefCollector extends DefCollector {
        OurDefCollector() throws DefException {
            super(IndexFileWriter.this.scene);
        }

        @Override
        protected void visitAnnotationDef(/*@NonNull*/ AnnotationDef d) {
            pw.println("package " + packagePart(d.name) + ":");
            pw.println("annotation @" + basenamePart(d.name) + ":");
            printAnnotationDefBody(d);
        }

        @Override
        protected void visitTLAnnotationDef(/*@NonNull*/ TLAnnotationDef tld) {
            AnnotationDef d = tld.def;
            pw.println("package " + packagePart(d.name) + ":");
            pw.print("annotation ");
            pw.print(tld.retention.ifname);
            pw.println(" @" + basenamePart(d.name) + ":");
            printAnnotationDefBody(d);
        }
    }

    final /*@NonNull*/ PrintWriter pw;

    private void printValue(/*@NonNull*/ AnnotationFieldType aft, /*@NonNull*/
    /*@ReadOnly*/ Object o) {
        if (aft instanceof AnnotationAFT)
            printAnnotation((/*@NonNull*/ Annotation) o);
        else if (aft instanceof ArrayAFT) {
            /*@NonNull*/ ArrayAFT aaft = (/*@NonNull*/ ArrayAFT) aft;
            pw.print('{');
            /*@NonNull*/ /*@ReadOnly*/ List<?> l =
                (/*@NonNull*/ /*@ReadOnly*/ List<?>) o;
            // watch out--could be an empty array of unknown type
            // (see AnnotationBuilder#addEmptyArrayField)
            if (aaft.elementType == null) {
                if (l.size() != 0)
                    throw new AssertionError();
            } else {
                boolean first = true;
                for (/*@NonNull*/ /*@ReadOnly*/ Object o2 : l) {
                    if (!first)
                        pw.print(',');
                    printValue(aaft.elementType, o2);
                    first = false;
                }
            }
            pw.print('}');
        } else if (aft instanceof ClassTokenAFT)
            pw.print((/*@NonNull*/ String) o + ".class");
        else if (aft instanceof BasicAFT && o instanceof String)
            pw.print(Strings.escape((/*@NonNull*/ String) o));
        else
            pw.print(o.toString());
    }

    private void printAnnotation(/*@NonNull*/ Annotation a) {
        pw.print("@" + a.def().name);
        /*@NonNull*/ /*@ReadOnly*/ Map</*@NonNull*/ String,
            /*@NonNull*/ /*@ReadOnly*/ Object> fieldValues
            = Annotations.fieldValuesMap(a);
        if (!fieldValues.isEmpty()) {
            pw.print('(');
            boolean first = true;
            for (/*@NonNull*/ /*@ReadOnly*/ Map.Entry</*@NonNull*/ String,
                    /*@NonNull*/ /*@ReadOnly*/ Object> f
                    : fieldValues.entrySet()) {
                if (!first)
                    pw.print(',');
                pw.print(f.getKey() + "=");
                printValue(a.def().fieldTypes.get(f.getKey()), f.getValue());
                first = false;
            }
            pw.print(')');
        }
    }

    private void printAnnotations(/*@NonNull*/ /*@ReadOnly*/ AElement<A> e) {
        for (/*@NonNull*/ TLAnnotation<A> tla : e.tlAnnotationsHere) {
            pw.print(' ');
            printAnnotation(tla.ann);
        }
    }

    private void printElement(/*@NonNull*/ String indentation,
            /*@NonNull*/ String desc,
            /*@NonNull*/ /*@ReadOnly*/ AElement<A> e) {
        pw.print(indentation + desc + ":");
        printAnnotations(e);
        pw.println();
    }
    
    private void printElementAndInnerTypes(/*@NonNull*/ String indentation,
            /*@NonNull*/ String desc,
            /*@NonNull*/ /*@ReadOnly*/ ATypeElement<A> e) {
        printElement(indentation, desc, e);
        for (/*@NonNull*/ /*@ReadOnly*/ Map.Entry</*@NonNull*/ InnerTypeLocation,
                /*@NonNull*/ /*@ReadOnly*/ AElement<A>> ite
                : e.innerTypes.entrySet()) {
            /*@NonNull*/ InnerTypeLocation loc = ite.getKey();
            /*@NonNull*/ /*@ReadOnly*/ AElement<A> it = ite.getValue();
            pw.print(indentation + INDENT + "inner-type");
            boolean first = true;
            for (int l : loc.location) {
                if (first)
                    pw.print(' ');
                else
                    pw.print(',');
                pw.print(l);
                first = false;
            }
            pw.print(':');
            printAnnotations(it);
            pw.println();
        }
    }
    
    private void printNumberedElements(/*@NonNull*/ String indentation,
            /*@NonNull*/ String desc,
            /*@NonNull*/ /*@ReadOnly*/ Map</*@NonNull*/ Integer,
                /*@NonNull*/ /*@ReadOnly*/ ATypeElement<A>> nels) {
        for (/*@NonNull*/ /*@ReadOnly*/ Map.Entry</*@NonNull*/ Integer,
                /*@NonNull*/ /*@ReadOnly*/ ATypeElement<A>> te
                : nels.entrySet()) {
            /*@NonNull*/ /*@ReadOnly*/ ATypeElement<A> t = te.getValue();
            printElementAndInnerTypes(indentation,
                    desc + " #" + te.getKey(), t);
        }
    }

    private void printBounds(/*@NonNull*/ String indentation,
            /*@NonNull*/ /*@ReadOnly*/ Map</*@NonNull*/ BoundLocation,
                /*@NonNull*/ /*@ReadOnly*/ ATypeElement<A>> bounds) {
        for (/*@NonNull*/ /*@ReadOnly*/ Map.Entry</*@NonNull*/ BoundLocation,
                /*@NonNull*/ /*@ReadOnly*/ ATypeElement<A>> be
                : bounds.entrySet()) {
            /*@NonNull*/ BoundLocation bl = be.getKey();
            /*@NonNull*/ /*@ReadOnly*/ ATypeElement<A> b = be.getValue();
            printElementAndInnerTypes(indentation,
                    "bound " + bl.paramIndex + " &" + bl.boundIndex, b);
        }
    }

    private void write() throws DefException {
        // First the annotation definitions...
        OurDefCollector odc = new OurDefCollector();
        odc.visit();

        // And then the annotated classes
        for (/*@NonNull*/ /*@ReadOnly*/ Map.Entry</*@NonNull*/ String,
                /*@NonNull*/ /*@ReadOnly*/ AClass<A>> ce
                : scene.classes.entrySet()) {
            /*@NonNull*/ String cname = ce.getKey();
            /*@NonNull*/ /*@ReadOnly*/ AClass<A> c = ce.getValue();
            /*@NonNull*/ String pkg = packagePart(cname),
                basename = basenamePart(cname);
            pw.println("package " + pkg + ":");
            pw.print("class " + basename + ":");
            printAnnotations(c);
            pw.println();
            printBounds(INDENT, c.bounds);
            for (/*@NonNull*/ /*@ReadOnly*/ Map.Entry</*@NonNull*/ String,
                    /*@NonNull*/ /*@ReadOnly*/ ATypeElement<A>> fe
                    : c.fields.entrySet()) {
                /*@NonNull*/ String fname = fe.getKey();
                /*@NonNull*/ /*@ReadOnly*/ ATypeElement<A> f = fe.getValue();
                pw.println();
                printElementAndInnerTypes(INDENT, "field " + fname, f);
            }
            for (/*@NonNull*/ /*@ReadOnly*/ Map.Entry</*@NonNull*/ String,
                    /*@NonNull*/ /*@ReadOnly*/ AMethod<A>> me
                    : c.methods.entrySet()) {
                /*@NonNull*/ String mkey = me.getKey();
                /*@NonNull*/ /*@ReadOnly*/ AMethod<A> m = me.getValue();
                pw.println();
                printElementAndInnerTypes(INDENT, "method " + mkey, m);
                printBounds(INDENT + INDENT, m.bounds);
                printNumberedElements(INDENT + INDENT, "parameter", m.parameters);
                if (!m.receiver.tlAnnotationsHere.isEmpty())
                    printElement(INDENT + INDENT, "receiver", m.receiver);
                for (/*@NonNull*/ /*@ReadOnly*/ Map.Entry<
                        /*@NonNull*/ LocalLocation,
                        /*@NonNull*/ /*@ReadOnly*/ ATypeElement<A>> le
                        : m.locals.entrySet()) {
                    /*@NonNull*/ LocalLocation loc = le.getKey();
                    /*@NonNull*/ /*@ReadOnly*/ ATypeElement<A> l = le.getValue();
                    printElementAndInnerTypes(INDENT + INDENT,
                            "local " + loc.index + " #"
                            + loc.scopeStart + "+" + loc.scopeLength, l);
                }
                printNumberedElements(INDENT + INDENT, "typecast", m.typecasts);
                printNumberedElements(INDENT + INDENT, "instanceof", m.instanceofs);
                printNumberedElements(INDENT + INDENT, "new", m.news);
            }
            pw.println();
        }
    }

    private IndexFileWriter(/*@NonNull*/ /*@ReadOnly*/ AScene<A> scene,
            /*@NonNull*/ Writer out) throws DefException {
        this.scene = scene;
        pw = new PrintWriter(out);
        write();
        pw.flush();
    }

    /**
     * Writes the annotations in <code>scene</code> and their definitions to
     * <code>out</code> in index file format.
     *
     * <p>
     * An {@link AScene} can contain several annotations of the same type but
     * different definitions, while an index file can accommodate only a single
     * definition for each annotation type.  This has two consequences:
     * 
     * <ul>
     * <li>Before writing anything, this method uses a {@link DefCollector} to
     * ensure that all definitions of each annotation type are identical
     * (modulo unknown array types).  If not, a {@link DefException} is thrown.
     * <li>There is one case in which, even if a scene is written successfully,
     * reading it back in produces a different scene.  Consider a scene
     * containing two annotations of type Foo, each with an array field bar.
     * In one annotation, bar is empty and of unknown element type (see
     * {@link AnnotationBuilder#addEmptyArrayField}); in the other, bar is
     * of known element type.  This method will
     * {@linkplain AnnotationDef#unify unify} the two definitions of Foo by
     * writing a single definition with known element type.  When the index
     * file is read into a new scene, the definitions of both annotations
     * will have known element type, whereas in the original scene, one had
     * unknown element type.
     * </ul>
     */
    public static <A extends Annotation> void write(
            /*@NonNull*/ /*@ReadOnly*/ AScene<A> scene,
            /*@NonNull*/ Writer out) throws DefException {
        new IndexFileWriter<A>(scene, out);
    }

    /**
     * Writes the annotations in <code>scene</code> and their definitions to
     * the file <code>filename</code> in index file format; see
     * {@link #write(AScene, Writer)}.
     */
    public static <A extends Annotation> void write(
            /*@NonNull*/ AScene<A> scene,
            /*@NonNull*/ String filename) throws IOException, DefException {
        write(scene, new FileWriter(filename));
    }
}
