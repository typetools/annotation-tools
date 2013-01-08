package annotator.find;

/**
 * Specifies an insertion of an "extends @Annotation java.lang.Object" to a type
 * bound.
 */
public class TypeBoundExtendsInsertion extends AnnotationInsertion {

    /**
     * Creates a new TypeBoundExtendsInsertion.
     *
     * @param text
     *            the text to insert
     * @param criteria
     *            where to insert the text
     * @param separateLine
     *            whether to insert the text on its own
     */
    public TypeBoundExtendsInsertion(String text, Criteria criteria,
            boolean separateLine) {
        super(text, criteria, separateLine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText(boolean comments, boolean abbreviate) {
        return "extends " + super.getText(comments, abbreviate)
                + " java.lang.Object";
    }
}
