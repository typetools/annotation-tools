package annotator.find;

/**
 * Specifies an insertion of a cast into a source file. Stores the type of cast
 * to insert in addition to the annotation and location. Note that this
 * insertion leaves two unclosed parenthesis and requires the use of a
 * {@link CloseParenthesisInsertion} after the expression that's being casted.
 */
public class CastInsertion extends Insertion {

  /**
   * The type to cast to.
   */
  private InsertionType type;

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
    super(criteria, separateLine);
    this.type = new InsertionType(new AnnotationInsertion(text), type);
  }

  /** {@inheritDoc} */
  @Override
  public String getText(boolean comments, boolean abbreviate) {
    String result = "((" + type.getText(comments, abbreviate) + ") (";
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Kind getKind() {
    return Kind.CAST;
  }
}
