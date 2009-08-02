package annotations;

import checkers.nullness.quals.*;
import checkers.javari.quals.*;

import java.util.*;

import annotations.field.*;

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
    public static final AnnotationFactory saf =
            new AnnotationFactory();

    /**
     * Returns an {@link AnnotationBuilder} appropriate for building a
     * {@link Annotation} of the given type name.
     */
    public AnnotationBuilder beginAnnotation(AnnotationDef def) {
        return new AnnotationBuilder(def.name, def.retention);
    }

    /**
     * Returns an {@link AnnotationBuilder} appropriate for building a
     * {@link Annotation} of the given type name.
     */
    public AnnotationBuilder beginAnnotation(String typeName, RetentionPolicy retention) {
        return new AnnotationBuilder(typeName, retention);
    }
}
