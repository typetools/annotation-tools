package annotations;

import java.util.*;

import annotations.field.*;

/**
 * This noninstantiable class provides useful static methods related to
 * annotations, following the convention of {@link java.util.Collections}.
 */
public abstract class Annotations {
    abstract void noninstantiable();

    /**
     * Converts the given scalar annotation field value to one appropriate for
     * passing to an {@link AnnotationBuilder} created by <code>af</code>.
     * Conversion is only necessary if <code>x</code> is a subannotation, in
     * which case we rebuild it with <code>af</code> since
     * {@link AnnotationBuilder#addScalarField addScalarField} of an
     * {@link AnnotationBuilder} created by <code>af</code> only accepts
     * subannotations built by <code>af</code>.
     */
    private static /*@NonNull*/ /*@ReadOnly*/ Object convertAFV(
    /*@NonNull*/ ScalarAFT aft, /*@NonNull*/ /*@ReadOnly*/ Object x,
    /*@NonNull*/ AnnotationFactory<?> af) {
        if (aft instanceof AnnotationAFT)
            return rebuild((/*@NonNull*/ Annotation) x, af);
        else
            return x;
    }

    /**
     * Rebuilds the annotation <code>a</code> using the factory
     * <code>af</code> by iterating through its fields according to its
     * definition and getting the values with {@link Annotation#getFieldValue}.
     * Returns null if the factory is not interested in <code>a</code>.
     */
    public static final <A extends Annotation> A rebuild(
    /*@NonNull*/ Annotation a,
    /*@NonNull*/ AnnotationFactory<A> af) {
        AnnotationBuilder<A> ab = af.beginAnnotation(a.def().name);
        if (ab != null) {
            for (/*@NonNull*/ /*@ReadOnly*/ Map.Entry</*@NonNull*/ String,
                    /*@NonNull*/ AnnotationFieldType> fieldDef
                    : a.def().fieldTypes.entrySet()) {

                /*@NonNull*/ String fieldName = fieldDef.getKey();
                /*@NonNull*/ AnnotationFieldType fieldType =
                        fieldDef.getValue();
                /*@NonNull*/ /*@ReadOnly*/ Object fieldValue =
                        a.getFieldValue(fieldName);

                if (fieldType instanceof ArrayAFT) {
                    /*@NonNull*/ ArrayAFT aFieldType =
                            (/*@NonNull*/ ArrayAFT) fieldType;
                    /*@NonNull*/ ArrayBuilder arrb =
                            ab.beginArrayField(fieldName, aFieldType);
                    /*@NonNull*/ /*@ReadOnly*/ List</*@NonNull*/ /*@ReadOnly*/ ?> l =
                            (/*@NonNull*/ /*@ReadOnly*/ List) fieldValue;
                    for (/*@NonNull*/ /*@ReadOnly*/ Object o : l)
                        arrb.appendElement(convertAFV(
                                aFieldType.elementType, o, af));
                    arrb.finish();
                } else {
                    /*@NonNull*/ ScalarAFT sFieldType =
                            (/*@NonNull*/ ScalarAFT) fieldType;
                    ab.addScalarField(fieldName, sFieldType,
                            convertAFV(sFieldType, fieldValue, af));
                }
            }
            return ab.finish();
        } else
            return null;
    }

    /**
     * A map of field names to values backed by an {@link Annotation};
     * {@link #fieldValuesMap(Annotation)} is the public factory.
     */
    private static /*@Unmodifiable*/ class FieldValuesMap extends
            AbstractMap</*@NonNull*/ String, /*@NonNull*/ /*@ReadOnly*/ Object> {
        /**
         * The backing {@link Annotation}.
         */
        final /*@NonNull*/ Annotation a;

        FieldValuesMap(/*@NonNull*/ Annotation a) {
            this.a = a;
        }

        /**
         * A field name-to-value entry backed by {@link #a}.
         */
        /*@Unmodifiable*/ class FVEntry implements
                Map.Entry</*@NonNull*/ String, /*@NonNull*/ Object> {
            final /*@NonNull*/ String fieldName;

            FVEntry(/*@NonNull*/ String fieldName) {
                this.fieldName = fieldName;
            }

            public /*@NonNull*/ String getKey() {
                return fieldName;
            }

            public /*@NonNull*/ /*@ReadOnly*/ Object getValue() {
                return FieldValuesMap.this.get(getKey());
            }

            public /*@NonNull*/ /*@ReadOnly*/ Object setValue(
                    /*@NonNull*/ /*@ReadOnly*/ Object value) {
                throw new UnsupportedOperationException();
            }
        }

        /**
         * An iterator for {@link EntrySet}.
         */
        // HMMM how to readonly-annotate an iterator?
        class EntryIterator
                implements
                Iterator</*@NonNull*/ /*@ReadOnly*/ Entry</*@NonNull*/ String,
                    /*@NonNull*/ /*@ReadOnly*/ Object>> {
            final /*@NonNull*/ Iterator</*@NonNull*/ String> fieldNames =
                    keySet().iterator();

            public boolean hasNext() {
                return fieldNames.hasNext();
            }

            public /*@NonNull*/ /*@ReadOnly*/ Map.Entry</*@NonNull*/ String,
                    /*@NonNull*/ /*@ReadOnly*/ Object> next() {
                return new FVEntry(fieldNames.next());
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        /**
         * The entry set of a {@link FieldValuesMap}, which contains
         * {@link FVEntry}s.
         */
        /*@Unmodifiable*/ class EntrySet
            extends AbstractSet</*@NonNull*/ /*@ReadOnly*/
                Entry</*@NonNull*/ String, /*@NonNull*/ /*@ReadOnly*/ Object>> {
            @Override
            public /*@NonNull*/ Iterator</*@NonNull*/ /*@ReadOnly*/
                Map.Entry</*@NonNull*/ String,
                    /*@NonNull*/ /*@ReadOnly*/ Object>> iterator() {
                return new EntryIterator();
            }

            @Override
            public int size() {
                return FieldValuesMap.this.keySet().size();
            }
        }

        @Override
        public /*@NonNull*/ /*@ReadOnly*/ Object get(/*@ReadOnly*/ Object key) {
            if (key instanceof String)
                return a.getFieldValue((/*@NonNull*/ String) key);
            else
                return null;
        }

        // works because field values are never null
        @Override
        public boolean containsKey(/*@ReadOnly*/ Object key) {
            return get(key) != null;
        }

        @Override
        public Set</*@NonNull*/ String> keySet() {
            return a.def().fieldTypes.keySet();
        }

        @Override
        public Set</*@NonNull*/ /*@ReadOnly*/ Entry</*@NonNull*/ String,
            /*@NonNull*/ /*@ReadOnly*/ Object>> entrySet() {
            return new EntrySet();
        }
    }

    /**
     * Returns a map of field names to values for the given annotation. The map
     * is backed by the annotation, so this operation is fast. Attempting to
     * modify the returned map will result in an exception.
     */
    public static final /*@NonNull*/ /*@ReadOnly*/ Map</*@NonNull*/ String,
        /*@NonNull*/ /*@ReadOnly*/ Object> fieldValuesMap(/*@NonNull*/ Annotation a) {
        return new FieldValuesMap(a);
    }
}
