package scenelib.annotations;

import java.util.Map;
import java.util.Set;

import scenelib.annotations.el.AnnotationDef;

/**
 * A very simple {@link scenelib.annotations.AnnotationFactory AnnotationFactory} that
 * creates {@link Annotation}s. It is interested in all annotations and
 * determines their definitions automatically from the fields supplied. Use the
 * singleton {@link #saf}.
 */
public final class AnnotationFactory {
    private AnnotationFactory() {
    }

    /**
     * The singleton {@link AnnotationFactory}.
     */
    public static final AnnotationFactory saf = new AnnotationFactory();

    /**
     * Returns an {@link AnnotationBuilder} appropriate for building an
     * {@link Annotation} of the given type name.
     *
     * @param def the definition for the annotation to be built
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
     * @param typeName the name of the annotation being built
     * @param tlAnnotationsHere the top-level meta-annotations on the annotation being built
     */
    public AnnotationBuilder beginAnnotation(String typeName, Set<Annotation> tlAnnotationsHere) {
        assert typeName != null;
        return new AnnotationBuilder(typeName, tlAnnotationsHere);
    }
}
