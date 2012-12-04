package annotator.find;

/**
 * This insertion adds two closing parentheses to close the unclosed parentheses
 * left by a {@link CastInsertion}. This should be inserted after the expression
 * that's being casted.
 */
public class CloseParenthesisInsertion extends Insertion {

    public CloseParenthesisInsertion(Criteria criteria,
            boolean separateLine) {
        super("", criteria, separateLine);
    }

    /** {@inheritDoc} */
    @Override
    public String getText(boolean comments, boolean abbreviate) {
        return "))";
    }
}
