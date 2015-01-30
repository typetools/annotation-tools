package annotations.io;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.javari.qual.*;
*/

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.*;

import annotations.Annotation;
import annotations.el.*;
import annotations.field.*;
import annotations.util.Strings;

import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;

/**
 * IndexFileWriter provides two static methods named <code>write</code>
 * that write a given {@link AScene} to a given {@link Writer} or filename,
 * in index file format.
 */
public final class IndexFileWriter {
    final /*@ReadOnly*/ AScene scene;

    private static final String INDENT = "    ";

    void printAnnotationDefBody(AnnotationDef d) {
        for (Map. /*@ReadOnly*/ Entry<String, AnnotationFieldType> f : d.fieldTypes.entrySet()) {
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
            pw.println("package " + annotations.io.IOUtils.packagePart(d.name) + ":");
            pw.print("annotation @" + annotations.io.IOUtils.basenamePart(d.name) + ":");
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
            for (Map. /*@ReadOnly*/ Entry<String, /*@ReadOnly*/ Object> f
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
        if (e.typeargs != null) {
          for (Map.Entry<RelativeLocation, ATypeElement> entry :
              e.typeargs.entrySet()) {
            RelativeLocation loc = entry.getKey();
            printTypeElementAndInnerTypes(indentation + INDENT,
                "typearg " + loc.index, entry.getValue());
          }
        }
    }

    private void printElementAndInnerTypes(String indentation,
            String desc, /*@ReadOnly*/ AElement e) {
        if (e.type != null) {
            printElement(indentation, desc, e.type);
            if (!e.type.innerTypes.isEmpty()) {
                printInnerTypes(indentation + INDENT, e.type);
            }
        }
    }

    private void printTypeElementAndInnerTypes(String indentation,
            String desc,
            /*@ReadOnly*/ ATypeElement e) {
        if (e.tlAnnotationsHere.isEmpty() && e.innerTypes.isEmpty() && desc.equals("type")) {
            return;
        }
        printElement(indentation, desc, e);
        printInnerTypes(indentation + INDENT, e);
    }

    private void printInnerTypes(String indentation, ATypeElement e) {
      for (Map. /*@ReadOnly*/ Entry<InnerTypeLocation,
              /*@ReadOnly*/ ATypeElement> ite : e.innerTypes.entrySet()) {
          InnerTypeLocation loc = ite.getKey();
          /*@ReadOnly*/ AElement it = ite.getValue();
          pw.print(indentation + "inner-type");
          char sep = ' ';
          for (TypePathEntry l : loc.location) {
              pw.print(sep);
              pw.print(typePathEntryToString(l));
              sep = ',';
          }
          pw.print(':');
          printAnnotations(it);
          pw.println();
      }
    }

    private void printInnerTypes(String indentation, ATypeElement e,
            ASTPath path) {
        for (Map. /*@ReadOnly*/ Entry<InnerTypeLocation,
                /*@ReadOnly*/ ATypeElement> ite : e.innerTypes.entrySet()) {
            InnerTypeLocation loc = ite.getKey();
            /*@ReadOnly*/ AElement it = ite.getValue();
            pw.print(indentation + "inner-type");
            char sep = ' ';
            for (TypePathEntry l : loc.location) {
                pw.print(sep);
                pw.print(typePathEntryToString(l));
                sep = ',';
            }
            pw.print(':');
            printAnnotations(it);
            pw.println();
        }
    }

    /**
     * Converts the given {@link TypePathEntry} to a string of the form
     * {@code tag, arg}, where tag and arg are both integers.
     */
    private String typePathEntryToString(TypePathEntry t) {
        return t.tag.tag + ", " + t.arg;
    }

    private void printNumberedAmbigiousElements(String indentation,
            String desc,
            /*@ReadOnly*/ Map<Integer, ? extends /*@ReadOnly*/ AElement> nels) {
        for (Map. /*@ReadOnly*/ Entry<Integer,
        				? extends /*@ReadOnly*/ AElement> te : nels.entrySet()) {
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
        for (Map. /*@ReadOnly*/ Entry<InnerTypeLocation, /*@ReadOnly*/ ATypeElement> ite
                : e.type.innerTypes.entrySet()) {
            InnerTypeLocation loc = ite.getKey();
            /*@ReadOnly*/ AElement it = ite.getValue();
            pw.print(indentation + INDENT + INDENT + "inner-type");
            boolean first = true;
            for (TypePathEntry l : loc.location) {
                if (first)
                    pw.print(' ');
                else
                    pw.print(',');
                pw.print(typePathEntryToString(l));
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
        for (Map. /*@ReadOnly*/ Entry<RelativeLocation, /*@ReadOnly*/ ATypeElement> te
                : nels.entrySet()) {
            /*@ReadOnly*/ ATypeElement t = te.getValue();
            printTypeElementAndInnerTypes(indentation,
                    desc + " " + te.getKey().getLocationString(), t);
        }
    }

    private void printTypeArguments(String indentation,
            String desc,
            /*@ReadOnly*/ Map<RelativeLocation,
                /*@ReadOnly*/ ? extends AElement> els) {
        for (Map. /*@ReadOnly*/ Entry<RelativeLocation,
                /*@ReadOnly*/ ? extends AElement> el : els.entrySet()) {
            /*@ReadOnly*/ AElement e = el.getValue();
            //printElement(indentation, desc, x);
            if (e.typeargs != null) {
                for (Map. /*@ReadOnly*/ Entry<RelativeLocation,
                        /*@ReadOnly*/ ATypeElement> te : e.typeargs.entrySet()) {
                  /*@ReadOnly*/ ATypeElement t = te.getValue();
                  printTypeElementAndInnerTypes(indentation,
                      "typearg " + te.getKey().getLocationString(), t);
                }
            }
        }
    }

    private void printBounds(String indentation,
            /*@ReadOnly*/ Map<BoundLocation, /*@ReadOnly*/ ATypeElement> bounds) {
        for (Map. /*@ReadOnly*/ Entry<BoundLocation, /*@ReadOnly*/ ATypeElement> be
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

        for (Map. /*@ReadOnly*/ Entry<TypeIndexLocation, /*@ReadOnly*/ ATypeElement> ei
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

    private void printASTInsertions(String indentation,
            /*@ReadOnly*/ Map<ASTPath, /*@ReadOnly*/ ATypeElement>
            insertAnnotations,
            /*@ReadOnly*/ Map<ASTPath, /*@ReadOnly*/ ATypeElementWithType>
            insertTypecasts) {
        for (Map. /*@ReadOnly*/ Entry<ASTPath, /*@ReadOnly*/ ATypeElement> e :
                insertAnnotations.entrySet()) {
            ASTPath path = e.getKey();
            ATypeElement el = e.getValue();
            pw.print(indentation + "insert-annotation " + path + ":");
            printAnnotations(el);
            pw.println();
            printInnerTypes(INDENT, el, path);
        }
        for (Map. /*@ReadOnly*/ Entry<ASTPath,
                    /*@ReadOnly*/ ATypeElementWithType> e :
                insertTypecasts.entrySet()) {
            ASTPath path = e.getKey();
            ATypeElementWithType el = e.getValue();
            pw.print(indentation + "insert-typecast " + path + ":");
            printAnnotations(el);
            pw.print(" ");
            printType(el.getType());
            pw.println();
            printInnerTypes(INDENT, el, path);
        }
    }

    private void printType(type.Type type) {
        switch (type.getKind()) {
        case ARRAY:
            type.ArrayType a = (type.ArrayType) type;
            printType(a.getComponentType());
            pw.print("[]");
            break;
        case BOUNDED:
            type.BoundedType b = (type.BoundedType) type;
            printType(b.getType());
            pw.print(" ");
            pw.print(b.getBoundKind());
            pw.print(" ");
            printType(b.getBound());
            break;
        case DECLARED:
            type.DeclaredType d = (type.DeclaredType) type;
            pw.print(d.getName());
            if (!d.isWildcard()) {
                type.DeclaredType inner = d.getInnerType();
                Iterator<type.Type> iter = d.getTypeParameters().iterator();
                //for (String s : d.getAnnotations()) {
                //    pw.print(s + " ");
                //}
                if (iter.hasNext()) {
                    pw.print("<");
                    printType(iter.next());
                    while (iter.hasNext()) {
                        pw.print(", ");
                        printType(iter.next());
                    }
                    pw.print(">");
                }
                if (inner != null) {
                    pw.print(".");
                    printType(inner);
                }
            }
            break;
        }
    }

    private void write() throws DefException {
        // First the annotation definitions...
        OurDefCollector odc = new OurDefCollector();
        odc.visit();

        // Then any package annotations...
        for (Map. /*@ReadOnly*/ Entry<String, /*@ReadOnly*/ AElement> pe
                : scene.packages.entrySet()) {
            AElement elem = pe.getValue();
            if (elem != null && !elem.tlAnnotationsHere.isEmpty()) {
                pw.print("package " + pe.getKey() + ":");
                printAnnotations(elem);
                pw.println();
            }
        }

        // And then the annotated classes
        for (Map. /*@ReadOnly*/ Entry<String, /*@ReadOnly*/ AClass> ce
                : scene.classes.entrySet()) {
            String cname = ce.getKey();
            /*@ReadOnly*/ AClass c = ce.getValue();
            String pkg = annotations.io.IOUtils.packagePart(cname);
            String basename = annotations.io.IOUtils.basenamePart(cname);
            pw.println("package " + pkg + ":");
            pw.print("class " + basename + ":");
            printAnnotations(c);
            pw.println();

            printBounds(INDENT, c.bounds);
            printExtImpls(INDENT, c.extendsImplements);
            printASTInsertions(INDENT, c.insertAnnotations, c.insertTypecasts);

            for (Map. /*@ReadOnly*/ Entry<String, /*@ReadOnly*/ AField> fe
                    : c.fields.entrySet()) {
                String fname = fe.getKey();
                /*@ReadOnly*/ AField f = fe.getValue();
                pw.println();
                printElement(INDENT, "field " + fname, f);
                printTypeElementAndInnerTypes(INDENT + INDENT, "type", f.type);
                printASTInsertions(INDENT + INDENT,
                        f.insertAnnotations, f.insertTypecasts);
            }
            for (Map. /*@ReadOnly*/ Entry<String, /*@ReadOnly*/ AMethod> me
                    : c.methods.entrySet()) {
                String mkey = me.getKey();
                /*@ReadOnly*/ AMethod m = me.getValue();
                pw.println();
                printElement(INDENT, "method " + mkey, m);
                printBounds(INDENT + INDENT, m.bounds);
                printTypeElementAndInnerTypes(INDENT + INDENT, "return", m.returnType);
                if (!m.receiver.type.tlAnnotationsHere.isEmpty() || !m.receiver.type.innerTypes.isEmpty()) {
                    // Only output the receiver if there is something to say. This is a bit
                    // inconsistent with the return type, but so be it.
                    printElementAndInnerTypes(INDENT + INDENT, "receiver", m.receiver);
                }
                printNumberedAmbigiousElements(INDENT + INDENT, "parameter", m.parameters);
                for (Map. /*@ReadOnly*/ Entry<LocalLocation, /*@ReadOnly*/ AField> le
                        : m.body.locals.entrySet()) {
                    LocalLocation loc = le.getKey();
                    /*@ReadOnly*/ AElement l = le.getValue();
                    StringBuilder sb = new StringBuilder("local ");
                    sb.append(loc.varName == null
                        ? loc.index
                            + " #" + loc.scopeStart + "+" + loc.scopeLength
                        : loc.varName);
                    printElement(INDENT + INDENT, sb.toString(), l);
                    printTypeElementAndInnerTypes(INDENT + INDENT + INDENT,
                            "type", l.type);
                }
                printRelativeElements(INDENT + INDENT, "typecast",
                        m.body.typecasts);
                printRelativeElements(INDENT + INDENT, "instanceof",
                        m.body.instanceofs);
                printRelativeElements(INDENT + INDENT, "new", m.body.news);
                printTypeArguments(INDENT + INDENT, "reference", m.body.refs);
                printTypeArguments(INDENT + INDENT, "call", m.body.calls);
                for (Map. /*@ReadOnly*/ Entry<RelativeLocation, /*@ReadOnly*/ AMethod> entry
                        : m.body.funs.entrySet()) {
                    /*@ReadOnly*/ AMethod lambda = entry.getValue();
                    printBounds(INDENT + INDENT + INDENT, lambda.bounds);
                    printTypeElementAndInnerTypes(INDENT + INDENT + INDENT,
                        "return", lambda.returnType);
                    //if (!lambda.receiver.type.tlAnnotationsHere.isEmpty()
                    //        || !lambda.receiver.type.innerTypes.isEmpty()) {
                    //    printElementAndInnerTypes(INDENT + INDENT + INDENT + INDENT,
                    //            "receiver", lambda.receiver);
                    //}
                    //printNumberedAmbigiousElements(INDENT + INDENT + INDENT,
                    //        "parameter", lambda.parameters);
                    //printRelativeElements(INDENT + INDENT + INDENT,
                    //    "typecast", lambda.body.typecasts);
                    //printRelativeElements(INDENT + INDENT + INDENT,
                    //    "instanceof", lambda.body.instanceofs);
                    //printRelativeElements(INDENT + INDENT + INDENT,
                    //    "new", lambda.body.news);
                    //printRelativeElements(INDENT + INDENT + INDENT,
                    //    "typearg", lambda.body.refs);
                    //printCalls(INDENT + INDENT + INDENT,
                    //    "typearg", lambda.body.calls);
                }
                // throwsException field is not processed.  Why?
                printASTInsertions(INDENT + INDENT,
                        m.insertAnnotations, m.insertTypecasts);
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
     * {@link annotations.AnnotationBuilder#addEmptyArrayField}); in the other, bar is
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
