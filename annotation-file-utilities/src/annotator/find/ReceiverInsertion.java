package annotator.find;

import java.util.ArrayList;
import java.util.List;

import type.DeclaredType;

/**
 * An insertion for a method receiver. This supports inserting an annotation on
 * an existing receiver, and creating a new receiver if one is not present.
 * <p>
 * A receiver insertion also keeps track of the insertions on its inner types.
 * If the receiver already exists in source code, these inner types can be
 * inserted as with any inner types. However, if the receiver does not already
 * exist in source code, these inner types must be manually inserted into the
 * new receiver. We don't know until the end of the whole insertion process if
 * the receiver already exists or not. To remedy this, a reference to each
 * insertion on an inner type of a receiver is stored in two places: the global
 * list of all insertions and the {@code ReceiverInsertion} that is the parent
 * of the inner type insertion. The {@code ReceiverInsertion} will always be
 * inserted before its inner types. Therefore, when inserting the
 * {@code ReceiverInsertion} if the receiver does not already exist, the inner
 * type insertions are manually inserted into the new receiver and labeled as
 * "inserted" (with {@link Insertion#setInserted(boolean)}) so they are not
 * inserted as the rest of the insertions list is processed.
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
     * The inner types to go on this receiver. See {@link ReceiverInsertion} for
     * more details.
     */
    private List<Insertion> innerTypeInsertions;

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
     * @param innerTypeInsertions the inner types to go on this receiver. See
     *         {@link ReceiverInsertion} for more details.
     */
    public ReceiverInsertion(DeclaredType type, Criteria criteria, List<Insertion> innerTypeInsertions) {
        super(criteria, false);
        this.type = type;
        addComma = false;
        this.innerTypeInsertions = innerTypeInsertions;
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

    /**
     * Gets a copy of the inner types to go on this receiver. See
     * {@link ReceiverInsertion} for more details.
     * @return a copy of the inner types.
     */
    public List<Insertion> getInnerTypeInsertions() {
        return new ArrayList<Insertion>(innerTypeInsertions);
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
