package scenelib.annotations.el;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import scenelib.annotations.Annotation;
import scenelib.annotations.io.IndexFileParser;
import scenelib.annotations.util.coll.VivifyingMap;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

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
 *     new AnnotationDef(taintedDef, RetentionPolicy.RUNTIME, true),
 *     new Annotation(taintedDef, Collections.emptyMap())
 * ));
 * </pre>
 */
public final class AScene implements Cloneable {
    private static boolean checkClones = true;
    public static boolean debugFoundMap = false;

    /** This scene's annotated packages; map key is package name */
    public final VivifyingMap<String, AElement> packages =
            AElement.<String>newVivifyingLHMap_AE();

    /**
     * Contains for each annotation type a set of imports to be added to
     *  the source if the annotation is inserted with the "abbreviate"
     *  option on.<br>
     *  <strong>Key</strong>: fully-qualified name of an annotation. e.g. for <code>@com.foo.Bar(x)</code>,
     *  the fully-qualified name is <code>com.foo.Bar</code> <br>
     *  <strong>Value</strong>: names of packages this annotation needs
     */
    public final Map<String, Set<String>> imports =
        new LinkedHashMap<String, Set<String>>();

    /** This scene's annotated classes; map key is class name */
    public final VivifyingMap<String, AClass> classes =
            new VivifyingMap<String, AClass>(
                    new LinkedHashMap<String, AClass>()) {
                @Override
                public  AClass createValueFor(
                 String k) {
                    return new AClass(k);
                }

                @Override
                public boolean subPrune(AClass v) {
                    return v.prune();
                }
            };

    /**
     * Creates a new {@link AScene} with no classes or packages.
     */
    public AScene() {
    }

    /**
     * Copy constructor for {@link AScene}.
     */
    public AScene(AScene scene) {
        for (String key : scene.packages.keySet()) {
            AElement val = scene.packages.get(key);
            packages.put(key, val.clone());
        }
        for (String key : scene.imports.keySet()) {
            // copy could in principle have different Set implementation
            Set<String> value = scene.imports.get(key);
            Set<String> copy = new LinkedHashSet<String>();
            copy.addAll(value);
            imports.put(key, copy);
        }
        for (String key : scene.classes.keySet()) {
            AClass clazz = scene.classes.get(key);
            classes.put(key, clazz.clone());
        }
        if (checkClones) {
            checkClone(this, scene);
        }
    }

    @Override
    public AScene clone() {
        return new AScene(this);
    }

    /**
     * Returns whether this {@link AScene} equals <code>o</code>; the
     * commentary and the cautionary remarks on {@link AElement#equals(Object)}
     * also apply to {@link AScene#equals(Object)}.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AScene
            && ((AScene) o).equals(this);
    }

    /**
     * Returns whether this {@link AScene} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link AScene}.
     */
    public boolean equals(AScene o) {
        return o.classes.equals(classes) && o.packages.equals(packages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return classes.hashCode() + packages.hashCode();
    }

    /**
     * Removes empty subelements of this {@link AScene} depth-first; returns
     * whether this {@link AScene} is itself empty after pruning.
     */
    public boolean prune() {
        return classes.prune() & packages.prune();
    }

    /** Returns a string representation. */
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

    /**
     * Throws exception if the arguments 1) are the same reference;
     * 2) are not equal() in both directions; or 3) contain
     * corresponding elements that meet either of the preceding two
     * conditions.
     */
    public static void checkClone(AScene s0, AScene s1) {
        if (s0 == null) {
            if (s1 != null) {
                cloneCheckFail();
            }
        } else {
            if (s1 == null) {
                cloneCheckFail();
            }
            s0.prune();
            s1.prune();
            if (s0 == s1) {
                cloneCheckFail();
            }
            checkElems(s0.packages, s1.packages);
            checkElems(s0.classes, s1.classes);
        }
    }

    public static <K, V extends AElement> void
    checkElems(VivifyingMap<K, V> m0, VivifyingMap<K, V> m1) {
        if (m0 == null) {
            if (m1 != null) {
                cloneCheckFail();
            }
        } else if (m1 == null) {
            cloneCheckFail();
        } else {
            for (K k : m0.keySet()) {
                checkElem(m0.get(k), m1.get(k));
            }
        }
    }

    /**
     * Throw exception on visit if e0 == e1 or !e0.equals(e1).
     * (See {@link #checkClone(AScene, AScene)} for explanation.)
     */
    public static void checkElem(AElement e0, AElement e1) {
        checkObject(e0, e1);
        if (e0 != null) {
            if (e0 == e1) {
                cloneCheckFail();
            }
            e0.accept(checkVisitor, e1);
        }
    }

    /**
     * Throw exception on visit if !el.equals(arg) or !arg.equals(el).
     * (See {@link #checkClone(AScene, AScene)} for explanation.)
     */
    public static void checkObject(Object o0, Object o1) {
        if (o0 == null ? o1 != null
                : !(o0.equals(o1) && o1.equals(o0))) {  // ok if ==
            throw new RuntimeException("clone check failed");
        }
    }

    /**
     * Throw exception on visit if el == arg or !el.equals(arg).
     * (See {@link checkClone(AScene, AScene)} for explanation.)
     */
    private static ElementVisitor<Void, AElement> checkVisitor =
        new ElementVisitor<Void, AElement>() {
            @Override
            public Void visitAnnotationDef(AnnotationDef el,
                    AElement arg) {
                return null;
            }

            @Override
            public Void visitBlock(ABlock el, AElement arg) {
                ABlock b = (ABlock) arg;
                checkElems(el.locals, b.locals);
                return null;
            }

            @Override
            public Void visitClass(AClass el, AElement arg) {
                AClass c = (AClass) arg;
                checkElems(el.bounds, c.bounds);
                checkElems(el.extendsImplements, c.extendsImplements);
                checkElems(el.fieldInits, c.fieldInits);
                checkElems(el.fields, c.fields);
                checkElems(el.instanceInits, c.instanceInits);
                checkElems(el.methods, c.methods);
                checkElems(el.staticInits, c.staticInits);
                return visitDeclaration(el, arg);
            }

            @Override
            public Void visitDeclaration(ADeclaration el, AElement arg) {
                ADeclaration d = (ADeclaration) arg;
                checkElems(el.insertAnnotations, d.insertAnnotations);
                checkElems(el.insertTypecasts, d.insertTypecasts);
                return visitElement(el, arg);
            }

            @Override
            public Void visitExpression(AExpression el, AElement arg) {
                AExpression e = (AExpression) arg;
                checkObject(el.id, e.id);
                checkElems(el.calls, e.calls);
                checkElems(el.funs, e.funs);
                checkElems(el.instanceofs, e.instanceofs);
                checkElems(el.news, e.news);
                checkElems(el.refs, e.refs);
                checkElems(el.typecasts, e.typecasts);
                return visitElement(el, arg);
            }

            @Override
            public Void visitField(AField el, AElement arg) {
                AField f = (AField) arg;
                checkElem(el.init, f.init);
                return visitDeclaration(el, arg);
            }

            @Override
            public Void visitMethod(AMethod el, AElement arg) {
                AMethod m = (AMethod) arg;
                checkObject(el.methodName, m.methodName);
                checkElem(el.body, m.body);
                checkElem(el.returnType, m.returnType);
                checkElems(el.bounds, m.bounds);
                checkElems(el.parameters, m.parameters);
                checkElems(el.throwsException, m.throwsException);
                return null;
            }

            @Override
            public Void visitTypeElement(ATypeElement el, AElement arg) {
                ATypeElement t = (ATypeElement) arg;
                checkObject(el.description, t.description);
                checkElems(el.innerTypes, t.innerTypes);
                return null;
            }

            @Override
            public Void visitTypeElementWithType(ATypeElementWithType el,
                    AElement arg) {
                ATypeElementWithType t = (ATypeElementWithType) arg;
                checkObject(el.getType(), t.getType());
                return visitTypeElement(el, arg);
            }

            @Override
            public Void visitElement(AElement el, AElement arg) {
                checkObject(el.description, arg.description);
                if (el.tlAnnotationsHere.size() !=
                        arg.tlAnnotationsHere.size()) {
                    cloneCheckFail();
                }
                for (Annotation a : el.tlAnnotationsHere) {
                    if (!arg.tlAnnotationsHere.contains(a)) {
                        cloneCheckFail();
                    }
                }
                checkElem(el.type, arg.type);
                return null;
            }
        };

    private static void cloneCheckFail() {
        throw new RuntimeException("clone check failed");
    }

    // temporary main for easy testing on JAIFs
    public static void main(String[] args) {
        int status = 0;
        checkClones = true;

        for (int i = 0; i < args.length; i++) {
            AScene s0 = new AScene();
            System.out.print(args[i] + ": ");
            try {
                IndexFileParser.parseFile(args[i], s0);
                s0.clone();
                System.out.println("ok");
            } catch (Throwable e) {
                status = 1;
                System.out.println("failed");
                e.printStackTrace();
            }
        }
        System.exit(status);
    }
}
