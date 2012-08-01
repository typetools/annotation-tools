package annotations.el;

/*>>>
import checkers.nullness.quals.Nullable;
import checkers.javari.quals.ReadOnly;
*/

import java.io.File;
import java.util.*;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import annotations.el.AElement;
import annotations.AnnotationBuilder;
import annotations.field.*;
import annotations.Annotation;
import annotations.Annotations;

/**
 * An annotation type definition, consisting of the annotation name,
 * its meta-annotations, and its field names and
 * types. <code>AnnotationDef</code>s are immutable.  An AnnotationDef with
 * a non-null retention policy is called a "top-level annotation definition".
 */
public final /*@ReadOnly*/ class AnnotationDef extends AElement {

    /**
     * The binary name of the annotation type, such as
     * "foo.Bar$Baz" for inner class Baz in class Bar in package foo.
     */
    public final String name;

    /**
     * A map of the names of this annotation type's fields to their types. Since
     * {@link AnnotationDef}s are immutable, attempting to modify this
     * map will result in an exception.
     */
    public /*@ReadOnly*/ Map<String, AnnotationFieldType> fieldTypes;

    /**
     * Constructs an annotation definition with the given name.
     * You MUST call setFieldTypes afterward, even if with an empty map.  (Yuck.)
     */
    public AnnotationDef(String name) {
        super("annotation: " + name);
        assert name != null;
        this.name = name;
    }

    // Problem:  I am not sure how to handle circularities (annotations meta-annotated with themselves)
    /**
     * Look up an AnnotationDefs in adefs.
     * If not found, read from a class and insert in adefs.
     */
    public static AnnotationDef fromClass(Class<? extends java.lang.annotation.Annotation> annoType, Map<String,AnnotationDef> adefs) {
        String name = annoType.getName();
        assert name != null;

        if (adefs.containsKey(name)) {
            return adefs.get(name);
        }

        Map<String,AnnotationFieldType> fieldTypes = new LinkedHashMap<String,AnnotationFieldType>();
        for (Method m : annoType.getDeclaredMethods()) {
            AnnotationFieldType aft = AnnotationFieldType.fromClass(m.getReturnType(), adefs);
            fieldTypes.put(m.getName(), aft);
        }

        AnnotationDef result = new AnnotationDef(name, Annotations.noAnnotations, fieldTypes);
        adefs.put(name, result);

        // An annotation can be meta-annotated with itself, so add
        // meta-annotations after putting the annotation in the map.
        java.lang.annotation.Annotation[] jannos;
        try {
            jannos = annoType.getDeclaredAnnotations();
        } catch (Exception e) {
            printClasspath();
            throw new Error("Exception in anno.getDeclaredAnnotations() for anno = " + annoType, e);
        }
        for (java.lang.annotation.Annotation ja : jannos) {
            result.tlAnnotationsHere.add(new Annotation(ja, adefs));
        }

        return result;
    }

    public AnnotationDef(String name, Set<Annotation> tlAnnotationsHere) {
        super("annotation: " + name);
        assert name != null;
        this.name = name;
        if (tlAnnotationsHere != null) {
            this.tlAnnotationsHere.addAll(tlAnnotationsHere);
        }
    }

    public AnnotationDef(String name, Set<Annotation> tlAnnotationsHere, /*@ReadOnly*/ Map<String, ? extends AnnotationFieldType> fieldTypes) {
        this(name, tlAnnotationsHere);
        setFieldTypes(fieldTypes);
    }

    /**
     * Constructs an annotation definition with the given name and field types.
     * The field type map is copied and then wrapped in an
     * {@linkplain Collections#unmodifiableMap unmodifiable map} to protect the
     * immutability of the annotation definition.
     * You MUST call setFieldTypes afterward, even if with an empty map.  (Yuck.)
     */
    public void setFieldTypes(/*@ReadOnly*/ Map<String, ? extends AnnotationFieldType> fieldTypes) {
        this.fieldTypes = Collections.unmodifiableMap(
                new LinkedHashMap<String, AnnotationFieldType>(fieldTypes)
                );
    }


    /**
     * The retention policy for annotations of this type.
     * If non-null, this is called a "top-level" annotation definition.
     * It may be null for annotations that are used only as a field of other
     * annotations.
     */
    public /*@Nullable*/ RetentionPolicy retention() {
        if (tlAnnotationsHere.contains(Annotations.aRetentionClass)) {
            return RetentionPolicy.CLASS;
        } else if (tlAnnotationsHere.contains(Annotations.aRetentionRuntime)) {
            return RetentionPolicy.RUNTIME;
        } else if (tlAnnotationsHere.contains(Annotations.aRetentionSource)) {
            return RetentionPolicy.SOURCE;
        } else {
            return null;
        }
    }

    /**
     * True if this is a type annotation (was meta-annotated
     * with @Target(ElementType.TYPE_USE) or @TypeQualifier).
     */
    public boolean isTypeAnnotation() {
        return (tlAnnotationsHere.contains(Annotations.aTargetTypeUse)
                || tlAnnotationsHere.contains(Annotations.aTypeQualifier));
    }


    /**
     * This {@link AnnotationDef} equals <code>o</code> if and only if
     * <code>o</code> is another nonnull {@link AnnotationDef} and
     * <code>this</code> and <code>o</code> define annotation types of the same
     * name with the same field names and types.
     */
    @Override
    public boolean equals(/*@ReadOnly*/ Object o) /*@ReadOnly*/ {
        return o instanceof AnnotationDef
                && equals((AnnotationDef) o);
    }

    /**
     * Returns whether this {@link AnnotationDef} equals <code>o</code>; a
     * slightly faster variant of {@link #equals(Object)} for when the argument
     * is statically known to be another nonnull {@link AnnotationDef}.
     */
    public boolean equals(AnnotationDef o) /*@ReadOnly*/ {
        boolean sameName = name.equals(o.name);
        boolean sameMetaAnnotations = equalsElement(o);
        boolean sameFieldTypes = fieldTypes.equals(o.fieldTypes);
        // Can be useful for debugging
        if (false && sameName && (! (sameMetaAnnotations
                            && sameFieldTypes))) {
            String message = String.format("Warning: incompatible definitions of annotation %s%n  %s%n  %s%n",
                                           name, this, o);
            new Exception(message).printStackTrace(System.out);
        }
        return sameName
            && sameMetaAnnotations
            && sameFieldTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() /*@ReadOnly*/ {
        return name.hashCode()
            // Omit tlAnnotationsHere, becase it should be unique and, more
            // importantly, including it causes an infinite loop.
            // + tlAnnotationsHere.hashCode()
            + fieldTypes.hashCode();
    }

    /**
     * Returns an <code>AnnotationDef</code> containing all the information
     * from both arguments, or <code>null</code> if the two arguments
     * contradict each other.  Currently this just
     * {@linkplain AnnotationFieldType#unify unifies the field types}
     * to handle arrays of unknown element type, which can arise via
     * {@link AnnotationBuilder#addEmptyArrayField}.
     */
    public static AnnotationDef unify(AnnotationDef def1,
            AnnotationDef def2) {
        // if (def1.name.equals(def2.name)
        //     && def1.fieldTypes.keySet().equals(def2.fieldTypes.keySet())
        //     && ! def1.equalsElement(def2)) {
        //     throw new Error(String.format("Unifiable except for meta-annotations:%n  %s%n  %s%n",
        //                                   def1, def2));
        // }
        if (def1.equals(def2)) {
            return def1;
        } else if (def1.name.equals(def2.name)
                 && def1.equalsElement(def2)
                 && def1.fieldTypes.keySet().equals(def2.fieldTypes.keySet())) {
            Map<String, AnnotationFieldType> newFieldTypes
                = new LinkedHashMap<String, AnnotationFieldType>();
            for (String fieldName : def1.fieldTypes.keySet()) {
                AnnotationFieldType aft1 = def1.fieldTypes.get(fieldName);
                AnnotationFieldType aft2 = def2.fieldTypes.get(fieldName);
                AnnotationFieldType uaft = AnnotationFieldType.unify(aft1, aft2);
                if (uaft == null) {
                    return null;
                }
                else newFieldTypes.put(fieldName, uaft);
            }
            return new AnnotationDef(def1.name, def1.tlAnnotationsHere, newFieldTypes);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        // Not: sb.append(((AElement) this).toString());
        // because it causes an infinite loop.
        boolean first;
        first = true;
        for (Annotation a : tlAnnotationsHere) {
            if (!first) {
                sb.append(" ");
            } else {
                first=false;
            }
            sb.append(a);
        }
        sb.append("] ");
        sb.append("@");
        sb.append(name);
        sb.append("(");
        first = true;
        for (Map.Entry<String, AnnotationFieldType> entry : fieldTypes.entrySet()) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }
            sb.append(entry.getValue().toString());
            sb.append(" ");
            sb.append(entry.getKey());
        }
        sb.append(")");
        return sb.toString();
    }

    public static void printClasspath() {
        System.out.println();
        System.out.println("Classpath:");
        StringTokenizer tokenizer =
            new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            String cpelt = tokenizer.nextToken();
            boolean exists = new File(cpelt).exists();
            if (! exists) {
                System.out.print(" non-existent:");
            }
            System.out.println("  " + cpelt);
        }
    }

}
