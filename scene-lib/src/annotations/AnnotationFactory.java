package annotations;

import checkers.nullness.quals.*;
import checkers.javari.quals.*;

import java.util.*;
import java.lang.annotation.RetentionPolicy;

import annotations.field.*;
import annotations.el.AnnotationDef;

/**
 * A very simple {@link annotations.AnnotationFactory AnnotationFactory} that
 * creates {@link Annotation}s. It is interested in all annotations and
 * determines their definitions automatically from the fields supplied. Use the
 * singleton {@link #saf}.
 */
public final /*@ReadOnly*/ class AnnotationFactory {
    private AnnotationFactory() {
    }

    /**
     * The singleton {@link AnnotationFactory}.
     */
    public static final AnnotationFactory saf = new AnnotationFactory();

    /**
     * Returns an {@link AnnotationBuilder} appropriate for building a
     * {@link Annotation} of the given type name.
     */
    public AnnotationBuilder beginAnnotation(AnnotationDef def) {
        return new AnnotationBuilder(def);
    }

    /**
     * Returns an {@link AnnotationBuilder}.
     * Tries to look up the AnnotationDef in adefs; if not found, inserts in adefs.
     */
    public AnnotationBuilder beginAnnotation(java.lang.annotation.Annotation a, Map<String, AnnotationDef> adefs) {
        AnnotationDef def = AnnotationDef.fromClass(a.getClass(), adefs);
        return new AnnotationBuilder(def);
    }

    /**
     * Returns an {@link AnnotationBuilder} appropriate for building a
     * {@link Annotation} of the given type name.
     */
    public AnnotationBuilder beginAnnotation(String typeName, Set<Annotation> tlAnnotationsHere) {
        assert typeName != null;
        return new AnnotationBuilder(typeName, tlAnnotationsHere);
    }
}
