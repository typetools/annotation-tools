package annotator.find;

/**
 * Specifies something that needs to be inserted into a source file, including
 * the "what" and the "where".
 */
public class Insertion {

    private final String text;
    private final Criteria criteria;
    // If non-null, then try to put annotation on its own line,
    // horizontally aligned with the location.
    private final boolean separateLine;

    /**
     * Creates a new insertion.
     *
     * @param text the text to insert
     * @param criteria where to insert the text
     * @param separateLine whether to insert the text on its own
     */
    public Insertion(String text, Criteria criteria, boolean separateLine) {
        this.text = text;
        this.criteria = criteria;
        this.separateLine = separateLine;
    }

    /**
     * Gets the insertion criteria.
     *
     * @return the criteria
     */
    public Criteria getCriteria() {
        return criteria;
    }

    /**
     * Gets the insertion text.
     *
     * @return the text to insert
     */
    public String getText() {
        return text;
    }

    /**
     * Gets whether the insertion goes on a separate line.
     *
     * @return whether the insertion goes on a separate line
     */
    public boolean getSeparateLine() {
        return separateLine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s (nl=%b) @ %s", text, separateLine, criteria);
    }
}
