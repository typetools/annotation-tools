package scenelib.annotations.el;

import java.util.LinkedHashMap;

import scenelib.annotations.Annotation;
import scenelib.annotations.util.coll.VivifyingMap;

import org.plumelib.util.CollectionsPlume;

// TODO: Add a method to indicate whether this class in an enum, and one to get the enum fields.
/** An annotated class. */
public final class AClass extends ADeclaration {
    /** The class's annotated type parameter bounds */
    public final VivifyingMap<BoundLocation, ATypeElement> bounds =
            ATypeElement.<BoundLocation>newVivifyingLHMap_ATE();

    // -1 maps to superclass, non-negative integers map to implemented interfaces
    public final VivifyingMap<TypeIndexLocation, ATypeElement> extendsImplements =
        ATypeElement.<TypeIndexLocation>newVivifyingLHMap_ATE();

    /**
     * The class's annotated methods; a method's key consists of its name
     * followed by its erased signature in JVML format.
     * For example, {@code foo()V} or
     * {@code bar(B[I[[Ljava/lang/String;)I}.  The annotation scene library
     * does not validate the keys, nor does it check that annotated subelements
     * of the {@link AMethod}s exist in the signature.
     */
    public final VivifyingMap<String, AMethod> methods =
        createMethodMap();

    public final VivifyingMap<Integer, ABlock> staticInits =
        createInitBlockMap();

    public final VivifyingMap<Integer, ABlock> instanceInits =
        createInitBlockMap();

    /** The class's annotated fields; map key is field name */
    public final VivifyingMap<String, AField> fields =
        AField.<String>newVivifyingLHMap_AF();

    public final VivifyingMap<String, AExpression> fieldInits =
        createFieldInitMap();

    /** The fully-qualified name of the annotated class. */
    public final String className;

    // debug fields to keep track of all classes created
    // private static List<AClass> debugAllClasses = new ArrayList<>();
    // private final List<AClass> allClasses;

    /**
     * Create a new AClass.
     *
     * @param className the fully-qualified name of the annotated class
     */
    AClass(String className) {
      super("class: " + className);
      this.className = className;
      // debugAllClasses.add(this);
      // allClasses = debugAllClasses;
    }

    /**
     * A copy constructor for AClass.
     *
     * @param clazz the AClass to copy
     */
    AClass(AClass clazz) {
      super(clazz);
      className = clazz.className;
      copyMapContents(clazz.bounds, bounds);
      copyMapContents(clazz.extendsImplements, extendsImplements);
      copyMapContents(clazz.fieldInits, fieldInits);
      copyMapContents(clazz.fields, fields);
      copyMapContents(clazz.instanceInits, instanceInits);
      copyMapContents(clazz.methods, methods);
      copyMapContents(clazz.staticInits, staticInits);
    }

    @Override
    public AClass clone() {
      return new AClass(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AClass
            && ((AClass) o).equalsClass(this);
    }

    final boolean equalsClass(AClass o) {
        return super.equals(o)
            && className.equals(o.className)
            && bounds.equals(o.bounds)
            && methods.equals(o.methods)
            && fields.equals(o.fields)
            && extendsImplements.equals(o.extendsImplements);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + bounds.hashCode()
            + methods.hashCode() + fields.hashCode()
            + staticInits.hashCode() + instanceInits.hashCode()
            + extendsImplements.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && bounds.isEmpty()
            && methods.isEmpty() && fields.isEmpty()
            && staticInits.isEmpty() && instanceInits.isEmpty()
            && extendsImplements.isEmpty();
    }

    @Override
    public void prune() {
        super.prune();
        bounds.prune();
        methods.prune();
        fields.prune();
        staticInits.prune();
        instanceInits.prune();
        extendsImplements.prune();
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
        CollectionsPlume.mapToString(sb, bounds, linePrefix + "  ");
        sb.append(linePrefix);
        sb.append("Extends/implements:\n");
        CollectionsPlume.mapToString(sb, extendsImplements, linePrefix + "  ");
        sb.append(linePrefix);
        sb.append("Fields:\n");
        CollectionsPlume.mapToString(sb, fields, linePrefix + "  ");
        sb.append(linePrefix);
        sb.append("Field Initializers:\n");
        CollectionsPlume.mapToString(sb, fieldInits, linePrefix + "  ");
        sb.append(linePrefix);
        sb.append("Static Initializers:\n");
        CollectionsPlume.mapToString(sb, staticInits, linePrefix + "  ");
        sb.append(linePrefix);
        sb.append("Instance Initializers:\n");
        CollectionsPlume.mapToString(sb, instanceInits, linePrefix + "  ");
        sb.append(linePrefix);
        sb.append("AST Typecasts:\n");
        CollectionsPlume.mapToString(sb, insertTypecasts, linePrefix + "  ");
        sb.append(linePrefix);
        sb.append("AST Annotations:\n");
        CollectionsPlume.mapToString(sb, insertAnnotations, linePrefix + "  ");
        sb.append(linePrefix);
        sb.append("Methods:\n");
        CollectionsPlume.mapToString(sb, methods, linePrefix + "  ");
        return sb.toString();
    }

    @Override
    public <R, T> R accept(ElementVisitor<R, T> v, T t) {
        return v.visitClass(this, t);
    }

    // Static methods

    private static VivifyingMap<String, AMethod> createMethodMap() {
        return new VivifyingMap<String, AMethod>(new LinkedHashMap<>()) {
            @Override
            public  AMethod createValueFor(String k) {
                return new AMethod(k);
            }

            @Override
            public boolean isEmptyValue(AMethod v) {
                return v.isEmpty();
            }
        };
    }

    private static VivifyingMap<Integer, ABlock> createInitBlockMap() {
        return new VivifyingMap<Integer, ABlock>(new LinkedHashMap<>()) {
            @Override
            public  ABlock createValueFor(Integer k) {
                return new ABlock(k);
            }

            @Override
            public boolean isEmptyValue(ABlock v) {
                return v.isEmpty();
            }
        };
    }

    private static VivifyingMap<String, AExpression> createFieldInitMap() {
        return new VivifyingMap<String, AExpression>(new LinkedHashMap<>()) {
            @Override
            public  AExpression createValueFor(String k) {
                return new AExpression(k);
            }

            @Override
            public boolean isEmptyValue(AExpression v) {
                return v.isEmpty();
            }
        };
    }

}
