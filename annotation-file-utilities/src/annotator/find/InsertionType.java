package annotator.find;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

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
     * The un-annotated, raw type.
     */
    private String type;

    /**
     * Constructs a new InsertionType.
     *
     * @param outerAnnotation An annotation to put on the outer type.
     * @param type The un-annotated, raw type.
     */
    public InsertionType(Insertion outerAnnotation, String type) {
        this.annotations = Lists.newArrayList(outerAnnotation);
        this.typeParams = new ArrayList<InsertionType>();
        this.type = type;
    }

    /**
     * Returns this type in text form, as a legal Java type.
     *
     * @param comments true if annotations should be surrounded in comments.
     * @param abbreviate true if package names should be removed from annotations.
     * @return This type as text.
     */
    public String getText(boolean comments, boolean abbreviate) {
        StringBuilder result = new StringBuilder();
        getText(result, comments, abbreviate);
        return result.toString();
    }

    /**
     * Builds up the resulting type in the given StringBuilder.
     */
    private void getText(StringBuilder result,
            boolean comments, boolean abbreviate) {
        for (Insertion anno : annotations) {
            result.append(anno.getText(comments, abbreviate));
            result.append(' ');
        }
        result.append(type);
        if (!typeParams.isEmpty()) {
            result.append('<');
            typeParams.get(0).getText(result, comments,
                    abbreviate);
            for (int i = 1; i < typeParams.size(); i++) {
                result.append(", ");
                typeParams.get(i).getText(result, comments,
                        abbreviate);
            }
            result.append('>');
        }
    }

    @Override
    public String toString() {
        return getText(false, false);
    }
}
