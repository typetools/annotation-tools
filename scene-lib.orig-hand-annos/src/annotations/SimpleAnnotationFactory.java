package annotations;

import java.util.*;

import annotations.field.*;

/**
 * A very simple {@link annotations.AnnotationFactory AnnotationFactory} that
 * creates {@link SimpleAnnotation}s. It is interested in all annotations and
 * determines their definitions automatically from the fields supplied. Use the
 * singleton {@link #saf}.
 */
public final class SimpleAnnotationFactory implements
        AnnotationFactory<SimpleAnnotation> {
    private SimpleAnnotationFactory() {
    }

    /**
     * The singleton {@link SimpleAnnotationFactory}.
     */
    public static final /*@NonNull*/ SimpleAnnotationFactory saf =
            new SimpleAnnotationFactory();

    static class SimpleAnnotationBuilder implements
            AnnotationBuilder<SimpleAnnotation> {
        /*@NonNull*/ String typeName;

        boolean arrayInProgress = false;

        boolean active = true;

        /*@NonNull*/ Map</*@NonNull*/ String, /*@NonNull*/ AnnotationFieldType>
            fieldTypes =
                new LinkedHashMap</*@NonNull*/ String,
                /*@NonNull*/ AnnotationFieldType>();

        /*@NonNull*/ Map</*@NonNull*/ String, /*@NonNull*/ /*@ReadOnly*/ Object>
            fieldValues =
                new LinkedHashMap</*@NonNull*/ String,
                /*@NonNull*/ /*@ReadOnly*/ Object>();

        class SimpleArrayBuilder implements ArrayBuilder {
            boolean abActive = true;

            /*@NonNull*/ String fieldName;

            /*@NonNull*/ List</*@NonNull*/ /*@ReadOnly*/ Object> arrayElements =
                    new ArrayList</*@NonNull*/ /*@ReadOnly*/ Object>();

            SimpleArrayBuilder(/*@NonNull*/ String fieldName) {
                this.fieldName = fieldName;
            }

            public void appendElement(/*@NonNull*/ /*@ReadOnly*/ Object x) {
                if (!abActive)
                    throw new IllegalStateException("Array is finished");
                arrayElements.add(x);
            }

            public void finish() {
                if (!abActive)
                    throw new IllegalStateException("Array is finished");
                fieldValues.put(fieldName, Collections
                        .unmodifiableList(arrayElements));
                arrayInProgress = false;
                abActive = false;
            }
        }
        
        private void checkAddField(/*@NonNull*/ String fieldName) {
            if (!active)
                throw new IllegalStateException("Already finished");
            if (arrayInProgress)
                throw new IllegalStateException("Array in progress");
            if (fieldTypes.containsKey(fieldName))
                throw new IllegalArgumentException("Duplicate field "
                        + fieldName);
        }

        public void addScalarField(/*@NonNull*/ String fieldName, /*@NonNull*/
        ScalarAFT aft, /*@NonNull*/ /*@ReadOnly*/ Object x) {
            checkAddField(fieldName);
            if (x instanceof Annotation && !(x instanceof SimpleAnnotation))
                throw new IllegalArgumentException(
                        "All subannotations must be SimpleAnnotations");
            fieldTypes.put(fieldName, aft);
            fieldValues.put(fieldName, x);
        }

        public /*@NonNull*/ ArrayBuilder beginArrayField(
        /*@NonNull*/ String fieldName, /*@NonNull*/ ArrayAFT aft) {
            checkAddField(fieldName);
            fieldTypes.put(fieldName, aft);
            arrayInProgress = true;
            return new SimpleArrayBuilder(fieldName);
        }

        public void addEmptyArrayField(/*@NonNull*/ String fieldName) {
            checkAddField(fieldName);
            fieldTypes.put(fieldName, new ArrayAFT(null));
            fieldValues.put(fieldName, Collections.emptyList());
        }

        public /*@NonNull*/ SimpleAnnotation finish() {
            if (!active)
                throw new IllegalStateException("Already finished");
            if (arrayInProgress)
                throw new IllegalStateException("Array in progress");
            return new SimpleAnnotation(
                    new AnnotationDef(typeName, fieldTypes), fieldValues);
        }

        SimpleAnnotationBuilder(/*@NonNull*/ String typeName) {
            this.typeName = typeName;
        }
    }

    /**
     * Returns an {@link AnnotationBuilder} appropriate for building a
     * {@link SimpleAnnotation} of the given type name. A
     * {@link SimpleAnnotationFactory} is interested in all annotations
     * (meaning that
     * {@link #beginAnnotation SimpleAnnotationFactory.beginAnnotation},
     * as opposed to
     * {@link AnnotationFactory#beginAnnotation AnnotationFactory.beginAnnotation},
     * never returns <code>null</code>), and the returned builder generates an
     * {@link AnnotationDef} automatically from the fields supplied.
     */
    public /*@NonNull*/ AnnotationBuilder<SimpleAnnotation> beginAnnotation(
    /*@NonNull*/ String typeName) {
        return new SimpleAnnotationBuilder(typeName);
    }
}
