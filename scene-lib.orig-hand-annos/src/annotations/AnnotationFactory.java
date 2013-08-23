package annotations;

/**
 * An {@link AnnotationFactory} provides {@link AnnotationBuilder}s with
 * which one may create {@link Annotation}s in a specific format.
 * <code>A</code> is the common supertype of the objects the factory produces
 * to represent annotations.
 * 
 * <p>
 * {@link AnnotationFactory}s can place restrictions on the annotations
 * they will build. Specifically, they can announce that they are uninterested
 * in annotations of certain types by returning null from
 * {@link #beginAnnotation}, and they may have "expected definitions" for
 * certain annotation types, meaning that an {@link AnnotationBuilder} may throw
 * an exception if the fields with which it is supplied do not match what it
 * expects given the type of the annotation it is building.
 * 
 * <p>
 * Singletons may be appropriate for subclasses of
 * {@link AnnotationFactory} that do not store any per-object information
 * (such as {@link SimpleAnnotationFactory}). {@link AnnotationFactory}s
 * usually should be immutable.
 */
public /*@Unmodifiable*/ interface AnnotationFactory<A extends Annotation> {
    /**
     * Returns an {@link AnnotationBuilder} appropriate for building an
     * annotation of the given type name. If this <code>AnnotationFactory</code>
     * is not interested in annotations of the given type, it may return null.
     * An <code>AnnotationFactory</code> that is interested in one annotation
     * type must also be interested in any annotation types that appear in its
     * fields.
     */
    AnnotationBuilder<A> beginAnnotation(/*@NonNull*/ String typeName);
}
