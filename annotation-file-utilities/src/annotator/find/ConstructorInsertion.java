package annotator.find;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import type.Type;

public class ConstructorInsertion extends TypedInsertion {
  private ReceiverInsertion receiverInsertion = null;
  private Set<Insertion> declarationInsertions = new LinkedHashSet<Insertion>();

  /**
   * Construct a ConstructorInsertion.
   * <p>
   * To insert the annotation and the constructor (for example,
   * {@code @Anno Type this}) the name should be set to the type to insert.
   * This can either be done before calling this constructor, or by modifying
   * the return value of {@link #getType()}.
   * 
   * @param type the type to use when inserting the constructor.
   * @param criteria where to insert the text.
   * @param innerTypeInsertions the inner types to go on this constructor. See
   *         {@link ReceiverInsertion} for more details.
   */
  public ConstructorInsertion(Type type, Criteria criteria,
      List<Insertion> innerTypeInsertions) {
    super(type, criteria, true, innerTypeInsertions);
  }

  /** {@inheritDoc} */
  @Override
  protected String getText(boolean comments, boolean abbreviate) {
    StringBuilder b = new StringBuilder();
    if (annotationsOnly) {
      //List<String> annotations = type.getAnnotations();
      //if (annotations.isEmpty()) { return ""; }
      //for (String a : annotations) {
      //  b.append(a);
      //  b.append(' ');
      //}
      //return new AnnotationInsertion(b.toString(), getCriteria(),
      //    getSeparateLine()).getText(comments, abbreviate);
      return "";
    } else {
      boolean commentAnnotation =
          comments && getBaseType().getName().isEmpty();
      for (Insertion i : declarationInsertions) {
        b.append(i.getText(commentAnnotation, abbreviate)).append("\n");
        if (abbreviate) {
          packageNames.addAll(i.getPackageNames());
        }
      }
      b.append("public ")
          .append(typeToString(type, commentAnnotation, true))
          .append("(").append(") { super(); }");
      return b.toString();
    }
  }

  protected ReceiverInsertion getReceiverInsertion() {
    return receiverInsertion;
  }

  public void addReceiverInsertion(ReceiverInsertion recv) {
    if (receiverInsertion == null) {
      receiverInsertion = recv;
    } else {
      receiverInsertion.getInnerTypeInsertions()
          .addAll(recv.getInnerTypeInsertions());
    }
  }

  public void addDeclarationInsertion(Insertion ins) {
    declarationInsertions.add(ins);
    ins.setInserted(true);
  }

  /** {@inheritDoc} */
  @Override
  protected boolean addLeadingSpace(boolean gotSeparateLine, int pos,
      char precedingChar) {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  protected boolean addTrailingSpace(boolean gotSeparateLine) {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public Kind getKind() {
    return Kind.CONSTRUCTOR;
  }
}
