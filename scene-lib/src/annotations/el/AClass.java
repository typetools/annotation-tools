package annotations.el;

import checkers.nullness.quals.*;
import checkers.javari.quals.*;

import java.util.*;
import static plume.UtilMDE.mapToString;

import annotations.*;
import annotations.util.coll.*;

/** An annotated class */
public final class AClass extends AElement {
    /** The class's annotated type parameter bounds */
    public final VivifyingMap<BoundLocation, ATypeElement> bounds =
            ATypeElement.<BoundLocation>newVivifyingLHMap_ATE();

    public final VivifyingMap<TypeIndexLocation, ATypeElement> extendsImplements =
        ATypeElement.<TypeIndexLocation>newVivifyingLHMap_ATE();

    private static VivifyingMap<String, AMethod> createMethodMap() {
        return new VivifyingMap<String, AMethod>(
                new LinkedHashMap<String, AMethod>()) {
            @Override
            public  AMethod createValueFor(
             String k) /*@ReadOnly*/ {
                return new AMethod(k);
            }

            @Override
            public boolean subPrune(AMethod v) /*@ReadOnly*/ {
                return v.prune();
            }
        };
    }

    /**
     * The class's annotated methods; a method's key consists of its name
     * followed by its erased signature in JVML format.
     * For example, <code>foo()V</code> or
     * <code>bar(B[I[[Ljava/lang/String;)I</code>.  The annotation scene library
     * does not validate the keys, nor does it check that annotated subelements
     * of the {@link AMethod}s exist in the signature.
     */
    public final VivifyingMap<String, AMethod> methods =
            createMethodMap();

    /** The class's annotated fields; map key is field name */
    public final VivifyingMap<String, ATypeElement> fields =
            ATypeElement.<String>newVivifyingLHMap_ATE();

    private String className;

    // debug fields to keep track of all classes created
    private static List<AClass> debugAllClasses = new ArrayList<AClass>();
    private List<AClass> allClasses;

    AClass(String className) {
      super("class: " + className);
      this.className = className;
      debugAllClasses.add(this);
      allClasses = debugAllClasses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(/*@ReadOnly*/ AElement o) /*@ReadOnly*/ {
        return o instanceof AClass &&
            ((/*@ReadOnly*/ AClass) o).equalsClass(this);
    }

    boolean equalsClass(/*@ReadOnly*/ AClass o) /*@ReadOnly*/ {
        return equalsElement(o) && bounds.equals(o.bounds)
            && methods.equals(o.methods) && fields.equals(o.fields)
            && extendsImplements.equals(o.extendsImplements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return super.hashCode() + bounds.hashCode()
            + methods.hashCode() + fields.hashCode()
            + extendsImplements.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean prune() {
        return super.prune() & bounds.prune()
            & methods.prune() & fields.prune()
            & extendsImplements.prune();
    }

    @Override
    public String toString() {
        return "AClass: " + className;
    }

    public String unparse() {
        return unparse("");
    }

    public String unparse(String linePrefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(linePrefix);
        sb.append(toString());
        sb.append("\n");
        sb.append(linePrefix);
        sb.append("Annotations:\n");
        for (Annotation a : tlAnnotationsHere) {
            sb.append(linePrefix);
            sb.append("  " + a + "\n");
        }
        sb.append(linePrefix);
        sb.append("Bounds:\n");
        mapToString(sb, bounds, linePrefix + "  ");
        sb.append(linePrefix);
        sb.append("Extends/implements:\n");
        mapToString(sb, extendsImplements, linePrefix + "  ");
        sb.append(linePrefix);
        sb.append("Fields:\n");
        mapToString(sb, fields, linePrefix + "  ");
        sb.append(linePrefix);
        sb.append("Methods:\n");
        mapToString(sb, methods, linePrefix + "  ");
        return sb.toString();
    }

}
