package annotator.find;

import type.DeclaredType;

/**
 * An insertion for a method receiver. This supports inserting an annotation on
 * an existing receiver, and creating a new receiver if one is not present.
 */
public class ReceiverInsertion extends Insertion {

    /**
     * The type to use when inserting the receiver.
     */
    private DeclaredType type;

    /**
     * If true a comma will be added at the end of the insertion (only if also
     * inserting the receiver).
     */
    private boolean addComma;

    /**
     * Construct a ReceiverInsertion.
     * <p>
     * If the receiver parameter already exists in the method declaration, then
     * pass a DeclaredType thats name is the empty String. This will only insert
     * an annotation on the existing receiver.
     * <p>
     * To insert the annotation and the receiver (for example,
     * {@code @Anno Type this}) the name should be set to the type to insert.
     * This can either be done before calling this constructor, or by modifying
     * the return value of {@link #getType()}.
     * <p>
     * A comma will not be added to the end of the receiver. In the case that
     * there is a parameter following the inserted receiver pass {@code true} to
     * {@link #setAddComma(boolean)} to add a comma to the end of the receiver.
     * 
     * @param type the type to use when inserting the receiver.
     * @param criteria where to insert the text.
     */
    public ReceiverInsertion(DeclaredType type, Criteria criteria) {
        super(criteria, false);
        this.type = type;
        addComma = false;
    }

    /**
     * If {@code true} a comma will be added at the end of the receiver. This
     * will only happen if a receiver is inserted (see
     * {@link #ReceiverInsertion(DeclaredType, Criteria)} for a description of
     * when a receiver is inserted). This is useful if the method already has
     * one or more parameters.
     */
    public void setAddComma(boolean addComma) {
        this.addComma = addComma;
    }

    /**
     * Gets the type. It is assumed that the returned value will be modified
     * to update the type to be inserted.
     */
    public DeclaredType getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    protected String getText(boolean comments, boolean abbreviate) {
        boolean commentAnnotation = (comments && type.getName().isEmpty());
        String result = typeToString(type, commentAnnotation, abbreviate);
        if (!type.getName().isEmpty()) {
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
    protected boolean addLeadingSpace(boolean gotSeparateLine, int pos,
            char precedingChar) {
        if (precedingChar == '.' && type.getName().isEmpty()) {
            // If only the annotation is being inserted then don't insert a
            // space if it's immediately after a '.'
            return false;
        }
        return super.addLeadingSpace(gotSeparateLine, pos, precedingChar);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean addTrailingSpace(boolean gotSeparateLine) {
        // If the type is not already in the source and the receiver is the only
        // parameter, don't add a trailing space.
        if (!type.getName().isEmpty() && !addComma) {
            return false;
        }
        return super.addTrailingSpace(gotSeparateLine);
    }

    /** {@inheritDoc} */
    @Override
    public Kind getKind() {
        return Kind.RECEIVER;
    }
}
