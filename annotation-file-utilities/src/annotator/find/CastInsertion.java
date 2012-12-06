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
   * @param separateLine whether to insert the text on its own
   * @param type the un-annotated type to cast to
   */
  public CastInsertion(Criteria criteria, boolean separateLine, Type type) {
    super(criteria, separateLine);
    this.type = type;
  }

  /** {@inheritDoc} */
  @Override
  public String getText(boolean comments, boolean abbreviate) {
    String result = "((" + typeToString(type, comments, abbreviate) + ") (";
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Kind getKind() {
    return Kind.CAST;
  }
}
