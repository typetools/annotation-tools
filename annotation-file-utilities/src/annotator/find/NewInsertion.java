package annotator.find;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scenelib.type.DeclaredType;
import scenelib.type.Type;

public class NewInsertion extends TypedInsertion {
  private final static Pattern qualifiers = Pattern.compile("(?:\\w++\\.)*+");

  /**
   * If true, the type will be qualified with the name of the superclass.
   */
  protected boolean qualifyType;

  /**
   * Construct a NewInsertion.
   * <p>
   * If "new" already exists in the initializer, then pass a
   * {@link DeclaredType} thats name is the empty String. This will only
   * insert an annotation on the existing type.
   * <p>
   * To insert the annotation along with "new" and the type (for example,
   * {@code @Anno new Type[] \{...\}}), set the name to the type to insert.
   * This can be done either before calling this constructor, or by modifying
   * the return value of {@link #getType()}.
   *
   * @param type the type to use when inserting the receiver
   * @param criteria where to insert the text
   * @param innerTypeInsertions the inner types to go on this receiver
   */
  public NewInsertion(Type type, Criteria criteria,
      List<Insertion> innerTypeInsertions) {
    super(type, criteria, innerTypeInsertions);
    annotationsOnly = false;
    qualifyType = false;
  }

  /** {@inheritDoc} */
  @Override
  protected String getText(boolean comments, boolean abbreviate) {
    if (annotationsOnly || type.getKind() != Type.Kind.ARRAY) {
      StringBuilder b = new StringBuilder();
      List<String> annotations = type.getAnnotations();
      if (annotations.isEmpty()) { return ""; }
      for (String a : annotations) {
        b.append(' ').append(a);  // initial space removed below
      }
      return new AnnotationInsertion(b.substring(1), getCriteria(),
          isSeparateLine()).getText(comments, abbreviate);
    } else {
      DeclaredType baseType = getBaseType();
      boolean commentAnnotation = (comments && baseType.getName().isEmpty());
      String result = typeToString(type, commentAnnotation, abbreviate);
      if (!baseType.getName().isEmpty()) {
        // First, temporarily strip off any qualifiers.
        Matcher matcher = qualifiers.matcher(result);
        String prefix = "";
        if (matcher.find() && matcher.start() == 0) {
          prefix = result.substring(0, matcher.end());
          result = result.substring(matcher.end());
        }
        // If the variable name preceded the array brackets in the
        // source, extract it from the result.
        if (qualifyType) {
          for (DeclaredType t = baseType; t != null; t = t.getInnerType()) {
            result += t.getName() + ".";
          }
        }
        // Finally, prepend extracted qualifiers.
        result = prefix + result;
      }
      result = "new " + result;
      if (comments) {
        result = "/*>>> " + result + " */";
      }
      return result;
    }
  }

  /**
   * If {@code true}, qualify {@code type} with the name of the superclass.
   * This will only happen if a "new" is inserted.
   */
  public void setQualifyType(boolean qualifyType) {
    this.qualifyType = qualifyType;
  }

  /** {@inheritDoc} */
  @Override
  protected boolean addLeadingSpace(boolean gotSeparateLine, int pos,
      char precedingChar) {
    if ((precedingChar == '.' || precedingChar == '(')
        && getBaseType().getName().isEmpty()) {
      // If only the annotation is being inserted then don't insert a
      // space if it's immediately after a '.' or '('
      return false;
    }
    return super.addLeadingSpace(gotSeparateLine, pos, precedingChar);
  }

  /** {@inheritDoc} */
  @Override
  protected boolean addTrailingSpace(boolean gotSeparateLine) {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public Kind getKind() {
    return Kind.NEW;
  }
}
