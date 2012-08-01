package annotations.io;

import checkers.nullness.quals.*;
import checkers.javari.quals.*;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
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
            // TODO: We would only print Retention and Target annotations
            printAnnotations(requiredMetaannotations(d.tlAnnotationsHere));
            pw.println();
            printAnnotationDefBody(d);
        }

        private Collection<Annotation> requiredMetaannotations(
                Collection<Annotation> annos) {
            Set<Annotation> results = new HashSet<Annotation>();
            for (Annotation a : annos) {
                String aName = a.def.name;
                if (aName.equals(Retention.class.getCanonicalName())
                    || aName.equals(Target.class.getCanonicalName())) {
                    results.add(a);
                }
            }
            return results;
        }
    }

    final PrintWriter pw;

    private void printValue(AnnotationFieldType aft, /*@ReadOnly*/ Object o) {
        if (aft instanceof AnnotationAFT) {
            printAnnotation((Annotation) o);
        } else if (aft instanceof ArrayAFT) {
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
        } else if (aft instanceof ClassTokenAFT) {
            pw.print(aft.format(o));
        } else if (aft instanceof BasicAFT && o instanceof String) {
            pw.print(Strings.escape((String) o));
        } else {
            pw.print(o.toString());
        }
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

    private void printAnnotations(Collection<? extends Annotation> annos) {
        for (Annotation tla : annos) {
            pw.print(' ');
            printAnnotation(tla);
        }
    }

    private void printAnnotations(/*@ReadOnly*/ AElement e) {
        printAnnotations(e.tlAnnotationsHere);
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
            /*@ReadOnly*/ AElement e) {
        printElement(indentation, desc, e);
        printTypeElementAndInnerTypes(indentation + INDENT, desc, e.type);
    }

    private void printTypeElementAndInnerTypes(String indentation,
            String desc,
            /*@ReadOnly*/ ATypeElement e) {
        if (e.tlAnnotationsHere.isEmpty() && e.innerTypes.isEmpty() && desc.equals("type")) {
            return;
        }
        printElement(indentation, desc, e);
        for (/*@ReadOnly*/ Map.Entry<InnerTypeLocation, /*@ReadOnly*/ ATypeElement> ite
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

    private void printNumberedAmbigiousElements(String indentation,
            String desc,
            /*@ReadOnly*/ Map<Integer, /*@ReadOnly*/ AElement> nels) {
        for (/*@ReadOnly*/ Map.Entry<Integer, /*@ReadOnly*/ AElement> te
                : nels.entrySet()) {
            /*@ReadOnly*/ AElement t = te.getValue();
            printAmbElementAndInnerTypes(indentation,
                    desc + " #" + te.getKey(), t);
        }
    }

    private void printAmbElementAndInnerTypes(String indentation,
            String desc,
            /*@ReadOnly*/ AElement e) {
        printElement(indentation, desc, e);
        if (e.type.tlAnnotationsHere.isEmpty() && e.type.innerTypes.isEmpty()) {
            return;
        }
        printElement(indentation + INDENT, "type", e.type);
        for (/*@ReadOnly*/ Map.Entry<InnerTypeLocation, /*@ReadOnly*/ ATypeElement> ite
                : e.type.innerTypes.entrySet()) {
            InnerTypeLocation loc = ite.getKey();
            /*@ReadOnly*/ AElement it = ite.getValue();
            pw.print(indentation + INDENT + INDENT + "inner-type");
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

    private void printRelativeElements(String indentation,
            String desc,
            /*@ReadOnly*/ Map<RelativeLocation, /*@ReadOnly*/ ATypeElement> nels) {
        for (/*@ReadOnly*/ Map.Entry<RelativeLocation, /*@ReadOnly*/ ATypeElement> te
                : nels.entrySet()) {
            /*@ReadOnly*/ ATypeElement t = te.getValue();
            printTypeElementAndInnerTypes(indentation,
                    desc + " " + te.getKey().getLocationString(), t);
        }
    }

    private void printBounds(String indentation,
            /*@ReadOnly*/ Map<BoundLocation, /*@ReadOnly*/ ATypeElement> bounds) {
        for (/*@ReadOnly*/ Map.Entry<BoundLocation, /*@ReadOnly*/ ATypeElement> be
                : bounds.entrySet()) {
            BoundLocation bl = be.getKey();
            /*@ReadOnly*/ ATypeElement b = be.getValue();
            if (bl.boundIndex == -1) {
                printTypeElementAndInnerTypes(indentation,
                                              "typeparam " + bl.paramIndex, b);
            } else {
                printTypeElementAndInnerTypes(indentation,
                           "bound " + bl.paramIndex + " &" + bl.boundIndex, b);
            }
        }
    }

    private void printExtImpls(String indentation,
            /*@ReadOnly*/ Map<TypeIndexLocation, /*@ReadOnly*/ ATypeElement> extImpls) {

        for (/*@ReadOnly*/ Map.Entry<TypeIndexLocation, /*@ReadOnly*/ ATypeElement> ei
                : extImpls.entrySet()) {
            TypeIndexLocation idx = ei.getKey();
            /*@ReadOnly*/ ATypeElement ty = ei.getValue();
            // reading from a short into an integer does not preserve sign?
            if (idx.typeIndex == -1 || idx.typeIndex == 65535) {
                printTypeElementAndInnerTypes(indentation, "extends", ty);
            } else {
                printTypeElementAndInnerTypes(indentation, "implements " + idx.typeIndex, ty);
            }
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
            printExtImpls(INDENT, c.extendsImplements);

            for (/*@ReadOnly*/ Map.Entry<String, /*@ReadOnly*/ AElement> fe
                    : c.fields.entrySet()) {
                String fname = fe.getKey();
                /*@ReadOnly*/ AElement f = fe.getValue();
                pw.println();
                printElement(INDENT, "field " + fname, f);
                printTypeElementAndInnerTypes(INDENT + INDENT, "type", f.type);
            }
            for (/*@ReadOnly*/ Map.Entry<String, /*@ReadOnly*/ AMethod> me
                    : c.methods.entrySet()) {
                String mkey = me.getKey();
                /*@ReadOnly*/ AMethod m = me.getValue();
                pw.println();
                printElement(INDENT, "method " + mkey, m);
                printBounds(INDENT + INDENT, m.bounds);
                printTypeElementAndInnerTypes(INDENT + INDENT, "return", m.returnType);
                if (!m.receiver.tlAnnotationsHere.isEmpty() || !m.receiver.innerTypes.isEmpty()) {
                    // Only output the receiver if there is something to say. This is a bit
                    // inconsistent with the return type, but so be it.
                    printTypeElementAndInnerTypes(INDENT + INDENT, "receiver", m.receiver);
                }
                printNumberedAmbigiousElements(INDENT + INDENT, "parameter", m.parameters);
                for (/*@ReadOnly*/ Map.Entry<LocalLocation, /*@ReadOnly*/ AElement> le
                        : m.locals.entrySet()) {
                    LocalLocation loc = le.getKey();
                    /*@ReadOnly*/ AElement l = le.getValue();
                    printElement(INDENT + INDENT,
                            "local " + loc.index + " #"
                            + loc.scopeStart + "+" + loc.scopeLength, l);
                    printTypeElementAndInnerTypes(INDENT + INDENT + INDENT,
                            "type", l.type);
                }
                printRelativeElements(INDENT + INDENT, "typecast", m.typecasts);
                printRelativeElements(INDENT + INDENT, "instanceof", m.instanceofs);
                printRelativeElements(INDENT + INDENT, "new", m.news);
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
