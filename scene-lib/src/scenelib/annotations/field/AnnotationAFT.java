package scenelib.annotations.field;

import java.util.Map.Entry;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
*/

import scenelib.annotations.Annotation;
import scenelib.annotations.el.AnnotationDef;

/**
 * An {@link AnnotationAFT} represents a subannotation as the type of an
 * annotation field and contains the definition of the subannotation.
 */
public final class AnnotationAFT extends ScalarAFT {

    /**
     * The definition of the subannotation.
     */
    public final AnnotationDef annotationDef;

    /**
     * Constructs a new {@link AnnotationAFT} for a subannotation of the
     * given definition.
     */
    public AnnotationAFT(AnnotationDef annotationDef) {
        this.annotationDef = annotationDef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidValue(Object o) {
        return o instanceof Annotation;
    }

    /**
     * The string representation of an {@link AnnotationAFT} looks like
     * <code>&#64;Foo</code> even though the subannotation definition is
     * logically part of the {@link AnnotationAFT}.  This is because the
     * subannotation field type appears as <code>&#64;Foo</code> in an
     * index file and the subannotation definition is written separately.
     */
    @Override
    public  String toString() {
        return "annotation-field " + annotationDef.name;
    }

    /**
     * {@inheritDoc}
     */
    // TODO: reduce duplication with annotator.specification.IndexFileSpecification.getElementAnnotations(AElement)
    @Override
    public String format(Object o) {
        Annotation anno = (Annotation) o;
        StringBuilder result = new StringBuilder();
        // TODO: figure out how to consider abbreviated annotation names.
        // See annotator.find.AnnotationInsertion.getText(boolean, boolean)
        result.append("@" + annotationDef.name);
        if (anno.fieldValues.size() == 1 && anno.fieldValues.containsKey("value")) {
            AnnotationFieldType fieldType = annotationDef.fieldTypes.get("value");
            result.append('(');
            result.append(fieldType.format(anno.fieldValues.get(anno.fieldValues.get("value"))));
            result.append(')');
        } else if (anno.fieldValues.size() > 0) {
            result.append('(');
            boolean notfirst = false;
            for (Entry<String, Object> field : anno.fieldValues.entrySet()) {
                // parameters of the annotation
                if (notfirst) {
                    result.append(", ");
                } else {
                    notfirst = true;
                }
                result.append(field.getKey() + "=");
                AnnotationFieldType fieldType = annotationDef.fieldTypes.get(field.getKey());
                result.append(fieldType.format(field.getValue()));
            }
            result.append(')');
        }
        return result.toString();
    }

    @Override
    public <R, T> R accept(AFTVisitor<R, T> v, T arg) {
        return v.visitAnnotationAFT(this, arg);
    }
}
