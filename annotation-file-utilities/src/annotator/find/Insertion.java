package annotator.find;

/**
 * Specifies something that needs to be inserted into a source file, including
 * the "what" and the "where".
 */
public class Insertion {

    private final String text;
    private final Criteria criteria;

    /**
     * Creates a new insertion.
     * 
     * @param text the text to insert
     * @param criteria where to insert the text
     */
    public Insertion(String text, Criteria criteria) {
        this.text = text;
        this.criteria = criteria;
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
     * {@inheritDoc}
     */
    public String toString() {
        return text + " @ " + criteria;
    }
}
