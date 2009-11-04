package annotations.io;

import checkers.nullness.quals.*;
import checkers.javari.quals.*;

import java.io.*;
import java.util.*;

import annotations.*;
import annotations.el.*;
import annotations.field.*;
import annotations.util.*;

import static annotations.io.IOUtils.*;

/**
 * IndexFileWriter provides two static methods named <code>write</code>
 * that write a given {@link AScene} to a given {@link Writer} or filename,
 * in index file format.
 */
public final class IndexFileWriter {
    final /*@ReadOnly*/ AScene scene;

    private static final String INDENT = "    ";

    void printAnnotationDefBody(AnnotationDef d) {
        for (/*@ReadOnly*/ Map.Entry<String, AnnotationFieldType> f : d.fieldTypes.entrySet()) {
            String fieldname = f.getKey();
            AnnotationFieldType fieldType = f.getValue();
            pw.println(INDENT + fieldType + " " + fieldname);
        }
        pw.println();
    }

    private class OurDefCollector extends DefCollector {
        OurDefCollector() throws DefException {
            super(IndexFileWriter.this.scene);
        }

        @Override
        protected void visitAnnotationDef(AnnotationDef d) {
            pw.println("package " + packagePart(d.name) + ":");
            pw.print("annotation @" + basenamePart(d.name) + ":");
            printAnnotations(d);
            pw.println();
            printAnnotationDefBody(d);
        }
    }

    final PrintWriter pw;

    private void printValue(AnnotationFieldType aft, /*@ReadOnly*/ Object o) {
        if (aft instanceof AnnotationAFT)
            printAnnotation((Annotation) o);
        else if (aft instanceof ArrayAFT) {
            ArrayAFT aaft = (ArrayAFT) aft;
            pw.print('{');
            if (!(o instanceof List)) {
                printValue(aaft.elementType, o);
            } else {
                /*@ReadOnly*/ List<?> l =
                    (/*@ReadOnly*/ List<?>) o;
                // watch out--could be an empty array of unknown type
                // (see AnnotationBuilder#addEmptyArrayField)
                if (aaft.elementType == null) {
                    if (l.size() != 0)
                        throw new AssertionError();
                } else {
                    boolean first = true;
                    for (/*@ReadOnly*/ Object o2 : l) {
                        if (!first)
                            pw.print(',');
                        printValue(aaft.elementType, o2);
                        first = false;
                    }
                }
            }
            pw.print('}');
        } else if (aft instanceof ClassTokenAFT)
            pw.print((String) o + ".class");
        else if (aft instanceof BasicAFT && o instanceof String)
            pw.print(Strings.escape((String) o));
        else
            pw.print(o.toString());
    }

    private void printAnnotation(Annotation a) {
        pw.print("@" + a.def().name);
        if (!a.fieldValues.isEmpty()) {
            pw.print('(');
            boolean first = true;
            for (/*@ReadOnly*/ Map.Entry<String, /*@ReadOnly*/ Object> f
                    : a.fieldValues.entrySet()) {
                if (!first)
                    pw.print(',');
                pw.print(f.getKey() + "=");
                printValue(a.def().fieldTypes.get(f.getKey()), f.getValue());
                first = false;
            }
            pw.print(')');
        }
    }

    private void printAnnotations(/*@ReadOnly*/ AElement e) {
        for (Annotation tla : e.tlAnnotationsHere) {
            pw.print(' ');
            printAnnotation(tla);
        }
    }

    private void printElement(String indentation,
            String desc,
            /*@ReadOnly*/ AElement e) {
        pw.print(indentation + desc + ":");
        printAnnotations(e);
        pw.println();
    }

    private void printElementAndInnerTypes(String indentation,
            String desc,
            /*@ReadOnly*/ ATypeElement e) {
        printElement(indentation, desc, e);
        for (/*@ReadOnly*/ Map.Entry<InnerTypeLocation, /*@ReadOnly*/ AElement> ite
                : e.innerTypes.entrySet()) {
            InnerTypeLocation loc = ite.getKey();
            /*@ReadOnly*/ AElement it = ite.getValue();
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

    private void printNumberedElements(String indentation,
            String desc,
            /*@ReadOnly*/ Map<Integer, /*@ReadOnly*/ ATypeElement> nels) {
        for (/*@ReadOnly*/ Map.Entry<Integer, /*@ReadOnly*/ ATypeElement> te
                : nels.entrySet()) {
            /*@ReadOnly*/ ATypeElement t = te.getValue();
            printElementAndInnerTypes(indentation,
                    desc + " #" + te.getKey(), t);
        }
    }

    private void printBounds(String indentation,
            /*@ReadOnly*/ Map<BoundLocation, /*@ReadOnly*/ ATypeElement> bounds) {
        for (/*@ReadOnly*/ Map.Entry<BoundLocation, /*@ReadOnly*/ ATypeElement> be
                : bounds.entrySet()) {
            BoundLocation bl = be.getKey();
            /*@ReadOnly*/ ATypeElement b = be.getValue();
            printElementAndInnerTypes(indentation,
                    "bound ," + bl.paramIndex + " &" + bl.boundIndex, b);
        }
    }

    private void write() throws DefException {
        // First the annotation definitions...
        OurDefCollector odc = new OurDefCollector();
        odc.visit();

        // And then the annotated classes
        for (/*@ReadOnly*/ Map.Entry<String, /*@ReadOnly*/ AClass> ce
                : scene.classes.entrySet()) {
            String cname = ce.getKey();
            /*@ReadOnly*/ AClass c = ce.getValue();
            String pkg = packagePart(cname);
            String basename = basenamePart(cname);
            pw.println("package " + pkg + ":");
            pw.print("class " + basename + ":");
            printAnnotations(c);
            pw.println();
            printBounds(INDENT, c.bounds);
            for (/*@ReadOnly*/ Map.Entry<String, /*@ReadOnly*/ ATypeElement> fe
                    : c.fields.entrySet()) {
                String fname = fe.getKey();
                /*@ReadOnly*/ ATypeElement f = fe.getValue();
                pw.println();
                printElementAndInnerTypes(INDENT, "field " + fname, f);
            }
            for (/*@ReadOnly*/ Map.Entry<String, /*@ReadOnly*/ AMethod> me
                    : c.methods.entrySet()) {
                String mkey = me.getKey();
                /*@ReadOnly*/ AMethod m = me.getValue();
                pw.println();
                printElement(INDENT, "method " + mkey, m);
                printBounds(INDENT + INDENT, m.bounds);
                printElementAndInnerTypes(INDENT + INDENT, "return", m.returnType);
                if (!m.receiver.tlAnnotationsHere.isEmpty())
                    printElement(INDENT + INDENT, "receiver", m.receiver);
                printNumberedElements(INDENT + INDENT, "parameter", m.parameters);
                for (/*@ReadOnly*/ Map.Entry<LocalLocation, /*@ReadOnly*/ ATypeElement> le
                        : m.locals.entrySet()) {
                    LocalLocation loc = le.getKey();
                    /*@ReadOnly*/ ATypeElement l = le.getValue();
                    printElementAndInnerTypes(INDENT + INDENT,
                            "local " + loc.index + " #"
                            + loc.scopeStart + "+" + loc.scopeLength, l);
                }
                printNumberedElements(INDENT + INDENT, "typecast", m.typecasts);
                printNumberedElements(INDENT + INDENT, "instanceof", m.instanceofs);
                printNumberedElements(INDENT + INDENT, "new", m.news);
                // throwsException field is not processed.  Why?
            }
            pw.println();
        }
    }

    private IndexFileWriter(/*@ReadOnly*/ AScene scene,
            Writer out) throws DefException {
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
    public static void write(
            /*@ReadOnly*/ AScene scene,
            Writer out) throws DefException {
        new IndexFileWriter(scene, out);
    }

    /**
     * Writes the annotations in <code>scene</code> and their definitions to
     * the file <code>filename</code> in index file format; see
     * {@link #write(AScene, Writer)}.
     */
    public static void write(
            AScene scene,
            String filename) throws IOException, DefException {
        write(scene, new FileWriter(filename));
    }
}
