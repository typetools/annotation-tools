package type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A representation of a Java type. Handles type parameters, wildcards, arrays
 * and inner types.
 */
public abstract class Type {

    /**
     * The annotations on the outer type. Empty if there are none.
     */
    private List<String> annotations;

    /**
     * Constructs a new type with no outer annotations.
     */
    public Type() {
        annotations = new ArrayList<String>();
    }

    /**
     * Adds an outer annotation to this type.
     * @param annotation the annotation to add.
     */
    public void addAnnotation(String annotation) {
        annotations.add(annotation);
    }

    /**
     * Gets an outer annotation on this type at the given index.
     * @param index the index.
     * @return the annotation.
     */
    public String getAnnotation(int index) {
        return annotations.get(index);
    }

    /**
     * Gets an unmodifiable copy of the outer annotations on this type. This
     * will be empty if there are none.
     * @return the annotations.
     */
    public List<String> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }
}
