package annotator.find;

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
public class ReceiverInsertion extends TypedInsertion {
    /**
     * If true a comma will be added at the end of the insertion (only if also
     * inserting the receiver).
     */
    private boolean addComma;

    /**
     * If true, {@code this} will be qualified with the name of the
     * superclass.
     */
    private boolean qualifyThis;

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
        super(type, criteria, innerTypeInsertions);
        addComma = false;
        qualifyThis = false;
    }

    /**
     * If {@code true} a comma will be added at the end of the receiver.
     * This will only happen if a receiver is inserted (see
     * {@link #ReceiverInsertion(DeclaredType, Criteria, List)} for a description of
     * when a receiver is inserted). This is useful if the method already has
     * one or more parameters.
     */
    public void setAddComma(boolean addComma) {
        this.addComma = addComma;
    }

    /**
     * If {@code true}, qualify {@code this} with the name of the superclass.
     * This will only happen if a receiver is inserted (see
     * {@link #ReceiverInsertion(DeclaredType, Criteria, List)}
     * for a description of when a receiver is inserted). This is useful
     * for inner class constructors.
     */
    public void setQualifyType(boolean qualifyThis) {
        this.qualifyThis = qualifyThis;
    }

    /** {@inheritDoc} */
    @Override
    protected String getText(boolean comments, boolean abbreviate) {
      if (annotationsOnly) {
        StringBuilder b = new StringBuilder();
        List<String> annotations = type.getAnnotations();
        if (annotations.isEmpty()) { return ""; }
        for (String a : annotations) {
            b.append(a);
            b.append(' ');
        }
        return new AnnotationInsertion(b.toString(), getCriteria(),
                getSeparateLine()).getText(comments, abbreviate);
      } else {
        DeclaredType baseType = getBaseType();
        boolean commentAnnotation = (comments && baseType.getName().isEmpty());
        String result = typeToString(type, commentAnnotation, abbreviate);
        if (!baseType.getName().isEmpty()) {
            result += " ";
            if (qualifyThis) {
                for (DeclaredType t = baseType; t != null;
                        t = t.getInnerType()) {
                    result += t.getName() + ".";
                }
            }
            result += "this";
            if (addComma) {
                result += ",";
            }
            if (comments) {
                result = "/*>>> " + result + " */";
            }
        }
        return result;
      }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean addLeadingSpace(boolean gotSeparateLine, int pos,
            char precedingChar) {
        if (precedingChar == '.' && getBaseType().getName().isEmpty()) {
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
        if (!getBaseType().getName().isEmpty() && !addComma) {
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
