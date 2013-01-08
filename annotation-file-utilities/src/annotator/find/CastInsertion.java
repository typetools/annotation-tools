package annotator.find;

import type.Type;

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
  private Type type;

  /**
   * Creates a new CastInsertion.
   *
   * @param criteria where to insert the text
   * @param type the un-annotated type to cast to
   */
  public CastInsertion(Criteria criteria, Type type) {
    super(criteria, false);
    this.type = type;
  }

  /**
   * Gets the type for this insertion. It is assumed that the returned value will be modified
   * to update the type to be inserted.
   * @return the type
   */
  public Type getType() {
      return type;
  }

  /** {@inheritDoc} */
  @Override
  protected String getText(boolean comments, boolean abbreviate) {
    String result = "((" + typeToString(type, comments, abbreviate) + ") (";
    return result;
  }

  /** {@inheritDoc} */
  @Override
  protected boolean addLeadingSpace(boolean gotSeparateLine, int pos,
      char precedingChar) {
    // Don't add a leading space if this cast is on the index of an array access.
    return super.addLeadingSpace(gotSeparateLine, pos, precedingChar)
           && precedingChar != '[';
  }

  /** {@inheritDoc} */
  @Override
  protected boolean addTrailingSpace(boolean gotSeparateLine) {
    // Never add a trailing space after the first part of a cast insertion.
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public Kind getKind() {
    return Kind.CAST;
  }
}
