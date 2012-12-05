package annotator.find;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Stores a generic type with annotations and converts it into a String.
 */
// TODO: This should also support arrays and wildcards.
public class InsertionType {

    /**
     * The annotations on this type.
     */
    private List<Insertion> annotations;

    /**
     * The ordered type parameters.
     */
    private List<InsertionType> typeParams;

    /**
     * The un-annotated, raw type. This can be set to the empty String if a type
     * is not necessary.
     */
    private String type;

    /**
     * Constructs a new InsertionType.
     *
     * @param type The un-annotated, raw type.
     */
    public InsertionType(String type) {
        this.annotations = new ArrayList<Insertion>();
        this.typeParams = new ArrayList<InsertionType>();
        this.type = type;
    }

    /**
     * Constructs a new InsertionType.
     *
     * @param outerAnnotation An annotation to put on the outer type.
     * @param type The un-annotated, raw type.
     */
    public InsertionType(Insertion outerAnnotation, String type) {
        this(type);
        this.annotations.add(outerAnnotation);
    }

    /**
     * Sets the raw, un-annotated type.
     *
     * @param type the type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the raw, un-annotated type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the type parameter at the given index.
     *
     * @param index The index.
     * @return The type parameter.
     */
    public InsertionType getTypeParameter(int index) {
        return typeParams.get(index);
    }

    /**
     * Adds the given type parameter to the next available index.
     *
     * @param child The type parameter to add.
     */
    public void addTypeParameter(InsertionType typeParam) {
        typeParams.add(typeParam);
    }

    /**
     * Returns this type in text form, as a legal Java type.
     *
     * @param comments true if annotations should be surrounded in comments.
     * @param abbreviate true if package names should be removed from annotations.
     * @param packageNames a set to store the removed package names in (only used if abbreviate is true).
     * @return This type as text.
     */
    public String getText(boolean comments, boolean abbreviate, Set<String> packageNames) {
        StringBuilder result = new StringBuilder();
        getText(result, comments, abbreviate, packageNames);
        return result.toString();
    }

    /**
     * Builds up the resulting type in the given StringBuilder.
     */
    private void getText(StringBuilder result,
            boolean comments, boolean abbreviate, Set<String> packageNames) {
        for (Insertion anno : annotations) {
            result.append(anno.getText(comments, abbreviate));
            if (abbreviate) {
                packageNames.addAll(anno.getPackageNames());
            }
            result.append(' ');
        }
        if (type.isEmpty()) {
            // If there's no type then remove the trailing space.
            result.deleteCharAt(result.length() - 1);
        } else {
            result.append(type);
            if (!typeParams.isEmpty()) {
                result.append('<');
                typeParams.get(0).getText(result, comments,
                        abbreviate, packageNames);
                for (int i = 1; i < typeParams.size(); i++) {
                    result.append(", ");
                    typeParams.get(i).getText(result, comments,
                            abbreviate, packageNames);
                }
                result.append('>');
            }
        }
    }

    @Override
    public String toString() {
        return getText(false, false, null);
    }
}
