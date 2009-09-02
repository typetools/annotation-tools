package annotations;

import checkers.nullness.quals.*;
import checkers.javari.quals.*;

import java.util.*;

import annotations.field.*;

/**
 * This noninstantiable class provides useful static methods related to
 * annotations, following the convention of {@link java.util.Collections}.
 */
public abstract class Annotations {
    private Annotations() {}

    /**
     * Converts the given scalar annotation field value to one appropriate for
     * passing to an {@link AnnotationBuilder} created by <code>af</code>.
     * Conversion is only necessary if <code>x</code> is a subannotation, in
     * which case we rebuild it with <code>af</code> since
     * {@link AnnotationBuilder#addScalarField addScalarField} of an
     * {@link AnnotationBuilder} created by <code>af</code> only accepts
     * subannotations built by <code>af</code>.
     */
    private static /*@ReadOnly*/ Object convertAFV(ScalarAFT aft, /*@ReadOnly*/ Object x) {
        if (aft instanceof AnnotationAFT)
            return rebuild((Annotation) x);
        else
            return x;
    }

    /**
     * Rebuilds the annotation <code>a</code> using the factory
     * <code>af</code> by iterating through its fields according to its
     * definition and getting the values with {@link Annotation#getFieldValue}.
     * Returns null if the factory is not interested in <code>a</code>.
     */
    public static final Annotation rebuild(Annotation a) {
        AnnotationBuilder ab = AnnotationFactory.saf.beginAnnotation(a.def());
        if (ab != null) {
            for (/*@ReadOnly*/ Map.Entry<String, AnnotationFieldType> fieldDef
                    : a.def().fieldTypes.entrySet()) {

                String fieldName = fieldDef.getKey();
                AnnotationFieldType fieldType =
                        fieldDef.getValue();
                /*@ReadOnly*/ Object fieldValue =
                        a.getFieldValue(fieldName);

                /*@ReadOnly*/ Object nnFieldValue;
                if (fieldValue != null)
                    nnFieldValue = fieldValue;
                else throw new IllegalArgumentException(
                        "annotation has no field value");

                if (fieldType instanceof ArrayAFT) {
                    ArrayAFT aFieldType =
                            (ArrayAFT) fieldType;
                    ArrayBuilder arrb =
                            ab.beginArrayField(fieldName, aFieldType);
                    /*@ReadOnly*/ List<? extends /*@ReadOnly*/ Object> l =
                            (/*@ReadOnly*/ List<? extends /*@ReadOnly*/ Object>) fieldValue;
                    ScalarAFT nnElementType;
                    if (aFieldType.elementType != null)
                        nnElementType = aFieldType.elementType;
                    else
                        throw new IllegalArgumentException(
                                "annotation field type is missing element type");
                    for (/*@ReadOnly*/ Object o : l)
                        arrb.appendElement(convertAFV(
                                nnElementType, o));
                    arrb.finish();
                } else {
                    ScalarAFT sFieldType =
                            (ScalarAFT) fieldType;
                    ab.addScalarField(fieldName, sFieldType,
                            convertAFV(sFieldType, fieldValue));
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
    private static /*@ReadOnly*/ class FieldValuesMap extends AbstractMap<String, /*@ReadOnly*/ Object> {
        /**
         * The backing {@link Annotation}.
         */
        final Annotation a;

        FieldValuesMap(Annotation a) {
            this.a = a;
        }

        /**
         * A field name-to-value entry backed by {@link #a}.
         */
        /*@ReadOnly*/ class FVEntry implements Map.Entry<String, Object> {
            final String fieldName;

            FVEntry(String fieldName) {
                this.fieldName = fieldName;
            }

            public String getKey() {
                return fieldName;
            }

            public /*@ReadOnly*/ Object getValue() {
                return FieldValuesMap.this.get(getKey());
            }

            public /*@ReadOnly*/ Object setValue(/*@ReadOnly*/ Object value) {
                throw new UnsupportedOperationException();
            }

            public boolean equals(Object o) {
                if (! (o instanceof FVEntry)) {
                    return false;
                }
                FVEntry e1 = this;
                FVEntry e2 = (FVEntry) o;
                return (e1.getKey()==null ?
                        e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &&
                    (e1.getValue()==null ?
                     e2.getValue()==null : e1.getValue().equals(e2.getValue()));
            }

            public int hashCode() {
                return (getKey()==null   ? 0 : getKey().hashCode()) ^
                    (getValue()==null ? 0 : getValue().hashCode());
            }

        }

        /**
         * An iterator for {@link EntrySet}.
         */
        /*@ReadOnly*/ class EntryIterator implements Iterator</*@ReadOnly*/ Entry<String, /*@ReadOnly*/ Object>> {
            final /*@ReadOnly*/ Iterator<String> fieldNames =
                    keySet().iterator();

            public boolean hasNext() /*@ReadOnly*/ {
                return fieldNames.hasNext();
            }

            public /*@ReadOnly*/ Map.Entry<String, /*@ReadOnly*/ Object> next() /*@ReadOnly*/ {
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
        /*@ReadOnly*/ class EntrySet
            extends AbstractSet</*@ReadOnly*/ Entry<String, /*@ReadOnly*/ Object>> {
            @Override
            public /*@ReadOnly*/ Iterator</*@ReadOnly*/
                Map.Entry<String, /*@ReadOnly*/ Object>> iterator() {
                return new EntryIterator();
            }

            @Override
            public int size() {
                return FieldValuesMap.this.keySet().size();
            }
        }

        @Override
        public /*@ReadOnly*/ Object get(/*@ReadOnly*/ Object key) {
            if (key instanceof String)
                return a.getFieldValue((String) key);
            else
                return null;
        }

        // works because field values are never null
        @Override
        public boolean containsKey(/*@ReadOnly*/ Object key) {
            return get(key) != null;
        }

        @Override
        public /*@ReadOnly*/ Set<String> keySet() {
            return a.def().fieldTypes.keySet();
        }

        @Override
        public /*@ReadOnly*/ Set</*@ReadOnly*/ Entry<String, /*@ReadOnly*/ Object>> entrySet() {
            return new EntrySet();
        }
    }

    /**
     * Returns a map of field names to values for the given annotation. The map
     * is backed by the annotation, so this operation is fast. Attempting to
     * modify the returned map will result in an exception.
     */
    public static final /*@ReadOnly*/ Map<String, /*@ReadOnly*/ Object> fieldValuesMap(Annotation a) {
        return new FieldValuesMap(a);
    }
}
