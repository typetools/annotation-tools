package scenelib.annotations.io;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
*/

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scenelib.annotations.Annotation;
import scenelib.annotations.el.AClass;
import scenelib.annotations.el.AElement;
import scenelib.annotations.el.AField;
import scenelib.annotations.el.AMethod;
import scenelib.annotations.el.AScene;
import scenelib.annotations.el.ATypeElement;
import scenelib.annotations.el.ATypeElementWithType;
import scenelib.annotations.el.AnnotationDef;
import scenelib.annotations.el.BoundLocation;
import scenelib.annotations.el.DefCollector;
import scenelib.annotations.el.DefException;
import scenelib.annotations.el.InnerTypeLocation;
import scenelib.annotations.el.LocalLocation;
import scenelib.annotations.el.RelativeLocation;
import scenelib.annotations.el.TypeIndexLocation;
import scenelib.annotations.field.AnnotationAFT;
import scenelib.annotations.field.AnnotationFieldType;
import scenelib.annotations.field.ArrayAFT;
import scenelib.annotations.field.BasicAFT;
import scenelib.annotations.field.ClassTokenAFT;
import scenelib.annotations.util.Strings;

import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;

/**
 * IndexFileWriter provides two static methods named <code>write</code>
 * that write a given {@link AScene} to a given {@link Writer} or filename,
 * in index file format.
 */
public final class IndexFileWriter {
    final AScene scene;

    private static final String INDENT = "    ";

    void printAnnotationDefBody(AnnotationDef d) {
        for (Map. Entry<String, AnnotationFieldType> f : d.fieldTypes.entrySet()) {
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
          if (!d.name.contains("+")) {
            pw.println("package " + scenelib.annotations.io.IOUtils.packagePart(d.name) + ":");
            pw.print("annotation @" + scenelib.annotations.io.IOUtils.basenamePart(d.name) + ":");
            // TODO: We would only print Retention and Target annotations
            printAnnotations(requiredMetaannotations(d.tlAnnotationsHere));
            pw.println();
            printAnnotationDefBody(d);
          }
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

    private void printValue(AnnotationFieldType aft, Object o) {
        if (aft instanceof AnnotationAFT) {
            printAnnotation((Annotation) o);
        } else if (aft instanceof ArrayAFT) {
            ArrayAFT aaft = (ArrayAFT) aft;
            pw.print('{');
            if (!(o instanceof List)) {
                printValue(aaft.elementType, o);
            } else {
                List<?> l =
                    (List<?>) o;
                // watch out--could be an empty array of unknown type
                // (see AnnotationBuilder#addEmptyArrayField)
                if (aaft.elementType == null) {
                    if (l.size() != 0) {
                        throw new AssertionError();
                    }
                } else {
                    boolean first = true;
                    for (Object o2 : l) {
                        if (!first) {
                            pw.print(',');
                        }
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
            for (Map. Entry<String, Object> f
                    : a.fieldValues.entrySet()) {
                if (!first) {
                    pw.print(',');
                }
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

    private void printAnnotations(AElement e) {
        printAnnotations(e.tlAnnotationsHere);
    }

    private void printElement(String indentation,
            String desc,
            AElement e) {
        pw.print(indentation + desc + ":");
        printAnnotations(e);
        pw.println();
    }

    private void printElementAndInnerTypes(String indentation,
            String desc, AElement e) {
        if (e.type != null) {
            printElement(indentation, desc, e.type);
            if (!e.type.innerTypes.isEmpty()) {
                printInnerTypes(indentation + INDENT, e.type);
            }
        }
    }

    private void printTypeElementAndInnerTypes(String indentation,
            String desc,
            ATypeElement e) {
        if (e.tlAnnotationsHere.isEmpty() && e.innerTypes.isEmpty() && desc.equals("type")) {
            return;
        }
        printElement(indentation, desc, e);
        printInnerTypes(indentation + INDENT, e);
    }

    private void printInnerTypes(String indentation, ATypeElement e) {
      for (Map. Entry<InnerTypeLocation,
              ATypeElement> ite : e.innerTypes.entrySet()) {
          InnerTypeLocation loc = ite.getKey();
          AElement it = ite.getValue();
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
        for (Map. Entry<InnerTypeLocation,
                ATypeElement> ite : e.innerTypes.entrySet()) {
            InnerTypeLocation loc = ite.getKey();
            AElement it = ite.getValue();
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
            Map<Integer, ? extends AElement> nels) {
        for (Map. Entry<Integer,
                ? extends AElement> te : nels.entrySet()) {
            AElement t = te.getValue();
            printAmbElementAndInnerTypes(indentation,
                    desc + " #" + te.getKey(), t);
        }
    }

    private void printAmbElementAndInnerTypes(String indentation,
            String desc,
            AElement e) {
        printElement(indentation, desc, e);
        if (e.type.tlAnnotationsHere.isEmpty() && e.type.innerTypes.isEmpty()) {
            return;
        }
        printElement(indentation + INDENT, "type", e.type);
        for (Map. Entry<InnerTypeLocation, ATypeElement> ite
                : e.type.innerTypes.entrySet()) {
            InnerTypeLocation loc = ite.getKey();
            AElement it = ite.getValue();
            pw.print(indentation + INDENT + INDENT + "inner-type");
            boolean first = true;
            for (TypePathEntry l : loc.location) {
                if (first) {
                    pw.print(' ');
                } else {
                    pw.print(',');
                }
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
            Map<RelativeLocation, ATypeElement> nels) {
        for (Map. Entry<RelativeLocation, ATypeElement> te
                : nels.entrySet()) {
            ATypeElement t = te.getValue();
            printTypeElementAndInnerTypes(indentation,
                    desc + " " + te.getKey().getLocationString(), t);
        }
    }

    private void printRelativeElements(String indentation,
            String desc1, String desc2,
            Map<RelativeLocation, ATypeElement> nels) {
        RelativeLocation prev = null;
        for (Map. Entry<RelativeLocation, ATypeElement> te
                : nels.entrySet()) {
            ATypeElement t = te.getValue();
            RelativeLocation loc = te.getKey();
            boolean isOffset = loc.index < 0;
            if (prev == null || loc.type_index < 0
                    || (isOffset ? loc.offset != prev.offset
                            : loc.index != prev.index)) {
                pw.print(indentation + desc1 + " ");
                pw.print(isOffset ? "#" + loc.offset : "*" + loc.index);
                pw.print(":");
                if (loc.type_index <= 0) { printAnnotations(t); }
                pw.println();
                printInnerTypes(indentation + INDENT, t);
            }
            if (loc.type_index > 0) {
                printTypeElementAndInnerTypes(indentation + INDENT,
                        desc2 + " " + loc.type_index, t);
            }
            prev = loc;
        }
    }

    private void printBounds(String indentation,
            Map<BoundLocation, ATypeElement> bounds) {
        for (Map. Entry<BoundLocation, ATypeElement> be
                : bounds.entrySet()) {
            BoundLocation bl = be.getKey();
            ATypeElement b = be.getValue();
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
            Map<TypeIndexLocation, ATypeElement> extImpls) {

        for (Map. Entry<TypeIndexLocation, ATypeElement> ei
                : extImpls.entrySet()) {
            TypeIndexLocation idx = ei.getKey();
            ATypeElement ty = ei.getValue();
            // reading from a short into an integer does not preserve sign?
            if (idx.typeIndex == -1 || idx.typeIndex == 65535) {
                printTypeElementAndInnerTypes(indentation, "extends", ty);
            } else {
                printTypeElementAndInnerTypes(indentation, "implements " + idx.typeIndex, ty);
            }
        }
    }

    private void printASTInsertions(String indentation,
            Map<ASTPath, ATypeElement>
            insertAnnotations,
            Map<ASTPath, ATypeElementWithType>
            insertTypecasts) {
        for (Map. Entry<ASTPath, ATypeElement> e :
                insertAnnotations.entrySet()) {
            ASTPath path = e.getKey();
            ATypeElement el = e.getValue();
            pw.print(indentation + "insert-annotation " + path + ":");
            printAnnotations(el);
            pw.println();
            printInnerTypes(INDENT, el, path);
        }
        for (Map. Entry<ASTPath,
                    ATypeElementWithType> e :
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

    private void printType(scenelib.type.Type type) {
        switch (type.getKind()) {
        case ARRAY:
            scenelib.type.ArrayType a = (scenelib.type.ArrayType) type;
            printType(a.getComponentType());
            pw.print("[]");
            break;
        case BOUNDED:
            scenelib.type.BoundedType b = (scenelib.type.BoundedType) type;
            printType(b.getName());
            pw.print(" ");
            pw.print(b.getBoundKind());
            pw.print(" ");
            printType(b.getBound());
            break;
        case DECLARED:
            scenelib.type.DeclaredType d = (scenelib.type.DeclaredType) type;
            pw.print(d.getName());
            if (!d.isWildcard()) {
                scenelib.type.DeclaredType inner = d.getInnerType();
                Iterator<scenelib.type.Type> iter = d.getTypeParameters().iterator();
                // for (String s : d.getAnnotations()) {
                //    pw.print(s + " ");
                // }
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

        // Then any package scenelib.annotations...
        for (Map. Entry<String, AElement> pe
                : scene.packages.entrySet()) {
            AElement elem = pe.getValue();
            if (elem != null && !elem.tlAnnotationsHere.isEmpty()) {
                pw.print("package " + pe.getKey() + ":");
                printAnnotations(elem);
                pw.println();
            }
        }

        // And then the annotated classes
        final String indent2 = INDENT + INDENT;
        final String indent3 = INDENT + indent2;
        for (Map. Entry<String, AClass> ce
                : scene.classes.entrySet()) {
            String cname = ce.getKey();
            AClass c = ce.getValue();
            String pkg = scenelib.annotations.io.IOUtils.packagePart(cname);
            String basename = scenelib.annotations.io.IOUtils.basenamePart(cname);
            if ("package-info".equals(basename)) {
              if (!c.tlAnnotationsHere.isEmpty()) {
                pw.print("package " + pkg + ":");
                printAnnotations(c);
                pw.println();
              }
              continue;
            } else {
              pw.println("package " + pkg + ":");
              pw.print("class " + basename + ":");
              printAnnotations(c);
              pw.println();
            }

            printBounds(INDENT, c.bounds);
            printExtImpls(INDENT, c.extendsImplements);
            printASTInsertions(INDENT, c.insertAnnotations, c.insertTypecasts);

            for (Map. Entry<String, AField> fe
                    : c.fields.entrySet()) {
                String fname = fe.getKey();
                AField f = fe.getValue();
                pw.println();
                printElement(INDENT, "field " + fname, f);
                printTypeElementAndInnerTypes(indent2, "type", f.type);
                printASTInsertions(indent2,
                        f.insertAnnotations, f.insertTypecasts);
            }
            for (Map. Entry<String, AMethod> me
                    : c.methods.entrySet()) {
                String mkey = me.getKey();
                AMethod m = me.getValue();
                pw.println();
                printElement(INDENT, "method " + mkey, m);
                printBounds(indent2, m.bounds);
                printTypeElementAndInnerTypes(indent2, "return", m.returnType);
                if (!m.receiver.type.tlAnnotationsHere.isEmpty()
                        || !m.receiver.type.innerTypes.isEmpty()) {
                    // Only output the receiver if there is something to
                    // say.  This is a bit inconsistent with the return
                    // type, but so be it.
                    printElementAndInnerTypes(indent2, "receiver", m.receiver);
                }
                printNumberedAmbigiousElements(indent2,
                        "parameter", m.parameters);
                for (Map. Entry<LocalLocation,
                        AField> le : m.body.locals.entrySet()) {
                    LocalLocation loc = le.getKey();
                    AElement l = le.getValue();
                    StringBuilder sb = new StringBuilder("local ");
                    sb.append(loc.varName == null
                        ? loc.index
                            + " #" + loc.scopeStart + "+" + loc.scopeLength
                        : loc.varName);
                    printElement(indent2, sb.toString(), l);
                    printTypeElementAndInnerTypes(indent3,
                            "type", l.type);
                }
                printRelativeElements(indent2, "typecast",
                        m.body.typecasts);
                printRelativeElements(indent2, "instanceof",
                        m.body.instanceofs);
                printRelativeElements(indent2, "new", m.body.news);
                printRelativeElements(indent2, "reference", "typearg", m.body.refs);
                printRelativeElements(indent2, "call", "typearg", m.body.calls);
                for (Map. Entry<RelativeLocation,
                        AMethod> entry : m.body.funs.entrySet()) {
                    AMethod lambda = entry.getValue();
                    RelativeLocation loc = entry.getKey();
                    pw.print("lambda " + loc.getLocationString() + ":\n");
                    printBounds(indent3, lambda.bounds);
                    printTypeElementAndInnerTypes(indent3,
                        "return", lambda.returnType);
                }
                // throwsException field is not processed.  Why?
                printASTInsertions(indent2,
                        m.insertAnnotations, m.insertTypecasts);
            }
            pw.println();
        }
    }

    private IndexFileWriter(AScene scene,
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
     * {@link scenelib.annotations.AnnotationBuilder#addEmptyArrayField}); in the other, bar is
     * of known element type.  This method will
     * {@linkplain AnnotationDef#unify unify} the two definitions of Foo by
     * writing a single definition with known element type.  When the index
     * file is read into a new scene, the definitions of both annotations
     * will have known element type, whereas in the original scene, one had
     * unknown element type.
     * </ul>
     */
    public static void write(
            AScene scene,
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
