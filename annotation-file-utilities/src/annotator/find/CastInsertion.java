package annotator.find;

/**
 * Specifies an insertion of a cast into a source file. Stores the type of cast
 * to insert in addition to the annotation and location. Note that this
 * insertion leaves two unclosed parenthesis and requires the use of a
 * {@link CloseParenthesisInsertion} after the expression that's being casted.
 */
public class CastInsertion extends Insertion {

  /**
   * The un-annotated type to cast to.
   */
  private String type;

  /**
   * Creates a new CastInsertion.
   *
   * @param text the text to insert
   * @param criteria where to insert the text
   * @param separateLine whether to insert the text on its own
   * @param type the un-annotated type to cast to
   */
  public CastInsertion(String text, Criteria criteria,
      boolean separateLine, String type) {
    super(text, criteria, separateLine);
    this.type = type;
  }

  /** {@inheritDoc} */
  @Override
  public String getText(boolean comments, boolean abbreviate) {
    return "((" + super.getText(comments, abbreviate) + " " + getType() + ") (";
  }

  /**
   * Returns the (unqualified) type to cast to
   */
  public String getType() {
    return type;
  }
}
