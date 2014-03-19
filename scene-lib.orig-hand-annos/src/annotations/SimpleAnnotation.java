package annotations;

import java.util.*;

/**
 * A very simple annotation representation constructed with a map of field names
 * to values. See the rules for values on {@link Annotation#getFieldValue};
 * furthermore, subannotations must be {@link SimpleAnnotation}s.
 * {@link SimpleAnnotation}s are immutable.
 * 
 * <p>
 * {@link SimpleAnnotation}s can be constructed directly or through
 * {@link SimpleAnnotationFactory#saf}. Either way works, but if you construct
 * one directly, you must provide a matching {@link AnnotationDef} yourself.
 */
public final /*@Unmodifiable*/ class SimpleAnnotation extends AbstractAnnotation {
    /**
     * An unmodifiable copy of the passed map of field values.
     */
    public final /*@NonNull*/ /*@ReadOnly*/ Map</*@NonNull*/ String,
        /*@NonNull*/ /*@ReadOnly*/ Object> fieldValues;

    // TODO make sure the field values are valid?
    /**
     * Constructs a {@link SimpleAnnotation} with the given definition and
     * field values.  Make sure that the field values obey the rules given on
     * {@link Annotation#getFieldValue} and that subannotations are also
     * {@link SimpleAnnotation}s; this constructor does not validate the
     * values.
     */
    public SimpleAnnotation(/*@NonNull*/ AnnotationDef def,
            Map</*@NonNull*/ String,
                /*@NonNull*/ /*@ReadOnly*/ ? extends Object> fields) {
        super(def);
        this.fieldValues = Collections.unmodifiableMap(
                new LinkedHashMap</*@NonNull*/ String,
                /*@NonNull*/ /*@ReadOnly*/ Object>(fields)
                );
    }

    /**
     * {@inheritDoc}
     */
    public /*@ReadOnly*/ Object getFieldValue(/*@NonNull*/ String fieldName) {
        return fieldValues.get(fieldName);
    }
}
