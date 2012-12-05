package annotator.find;

/**
 * An insertion for a method receiver. This supports inserting an annotation on
 * an existing receiver, and creating a new receiver if one is not present.
 */
public class ReceiverInsertion extends Insertion {

    /**
     * The type to use when inserting the receiver.
     */
    private InsertionType type;

    /**
     * If true a comma will be added at the end of the insertion (only if also
     * inserting the receiver).
     */
    private boolean addComma;

    /**
     * Construct a ReceiverInsertion. Initially only the annotation and not the
     * receiver will be inserted. Call {@link #setType(String)} to add a type
     * and therefore insert a receiver. Initially a comma will not be added to
     * the end of the receiver. Pass true to {@link #setAddComma(boolean)} to
     * change this.
     *
     * @param text the annotation to insert
     * @param criteria where to insert the text
     * @param separateLine whether to insert the text on its own
     */
    public ReceiverInsertion(String text, Criteria criteria,
            boolean separateLine) {
        super(criteria, separateLine);
        type = new InsertionType(new AnnotationInsertion(text), "");
        addComma = false;
    }

    /**
     * If true a comma will be added at the end of the receiver. This will open
     * happen if a receiver is inserted (that is if {@link #setType(String)} has
     * been passed a non-null value). This is useful if the method already has
     * one or more parameters.
     */
    public void setAddComma(boolean addComma) {
        this.addComma = addComma;
    }

    /**
     * Gets the type. It is assumed that the returned value will be modified
     * to update the type to be inserted.
     */
    public InsertionType getType() {
        return type;
    }

    @Override
    public String getText(boolean comments, boolean abbreviate) {
        boolean commentAnnotation = (comments && type.getType().isEmpty());
        String result = type.getText(commentAnnotation, abbreviate);
        if (!type.getType().isEmpty()) {
            result += " this";
            if (addComma) {
                result += ",";
            }
        }
        if (comments && !commentAnnotation) {
            result = "/*>>> " + result + " */";
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Kind getKind() {
        return Kind.RECEIVER;
    }
}
