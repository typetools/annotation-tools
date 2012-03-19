//This is a wrapper class around an TypeAnnotationVisitor that can be used
//to verify the information it visits specifies a valid [extended] annotation.

package annotations.io.classfile;

import checkers.nullness.quals.*;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.TypeAnnotationVisitor;

import com.sun.tools.javac.code.TargetType;

/**
 * A <code>SafeTypeAnnotationVisitor</code> wraps around an
 * TypeAnnotationVisitor and delegates all calls to it.  However, it
 * maintains a record of all methods that have been called and on
 * calling {@link SafeTypeAnnotationVisitor#visitEnd},
 * performs a check to verify that all the data passed
 * to this visitor specifies a legal annotation or extended annotation.  Its
 * intended use is to wrap around an <code>TypeAnnotationWriter</code> and
 * thus ensure that no illegal class files are written, although it will work
 * more generally on any visitor.
 *
 * <p>
 * Also note that nothing special needs to be done about subannotations
 * and regular arrays, since their lengths are not passed in to this visitor.
 *
 * <p>
 * If none of the <code>visitX*</code> methods has been called, it is
 * automatically a legal annotation.  Else, if some of the <code>visitX*</code>
 * methods have been called, the check will ensure that the data passed to this
 * specifies a legal extended annotation, as defined by its target type.
 */
public class SafeTypeAnnotationVisitor
implements TypeAnnotationVisitor {

  // The visitor this delegates all calls to.
  private final TypeAnnotationVisitor xav;

  // Each list keeps a record of what was passed in to the similarly-named
  // method, and except for xLocationArgs, should all contain at most 1 element.
  private List<Integer> xIndexArgs;
  private List<Integer> xLengthArgs;
  private List<Integer> xLocationArgs;
  private List<Integer> xLocationLengthArgs;
  private List<Integer> xOffsetArgs;
  private List<Integer> xStartPcArgs;
  private List<Integer> xTargetTypeArgs;
  private List<Integer> xParamIndexArgs;
  private List<Integer> xBoundIndexArgs;
  private List<Integer> xTypeIndexArgs;

  /**
   * Constructs a new <code> SafeTypeAnnotationVisitor </code> that
   *  delegates all calls to the given visitor.
   *
   * @param xav the visitor to delegate all method calls to
   */
  public SafeTypeAnnotationVisitor(TypeAnnotationVisitor xav) {
    this.xav = xav;
    // Start most of these with a capacity of one, since for legal annotations
    // they should not contain more than one element.
    xIndexArgs = new ArrayList<Integer>(1);
    xLengthArgs = new ArrayList<Integer>(1);
    xLocationArgs = new ArrayList<Integer>();
    xLocationLengthArgs = new ArrayList<Integer>(1);
    xOffsetArgs = new ArrayList<Integer>(1);
    xStartPcArgs = new ArrayList<Integer>(1);
    xTargetTypeArgs = new ArrayList<Integer>(1);
    xParamIndexArgs = new ArrayList<Integer>(1);
    xBoundIndexArgs = new ArrayList<Integer>(1);
    xTypeIndexArgs = new ArrayList<Integer>(1);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String, java.lang.Object)
   */
  public void visit(String name, Object value) {
    xav.visit(name, value);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
   */
  public AnnotationVisitor visitAnnotation(String name, String desc) {
    return xav.visitAnnotation(name, desc);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.AnnotationVisitor#visitArray(java.lang.String)
   */
  public AnnotationVisitor visitArray(String name) {
    return xav.visitArray(name);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.AnnotationVisitor#visitEnum(java.lang.String, java.lang.String, java.lang.String)
   */
  public void visitEnum(String name, String desc, String value) {
    xav.visitEnum(name, desc, value);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.TypeAnnotationVisitor#visitXIndex(int)
   */
  public void visitXIndex(int index) {
    xIndexArgs.add(index);
    xav.visitXIndex(index);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.TypeAnnotationVisitor#visitXLength(int)
   */
  public void visitXLength(int length) {
    xLengthArgs.add(length);
    xav.visitXLength(length);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.TypeAnnotationVisitor#visitXLocation(int)
   */
  public void visitXLocation(int location) {
    xLocationArgs.add(location);
    xav.visitXLocation(location);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.TypeAnnotationVisitor#visitXLocationLength(int)
   */
  public void visitXLocationLength(int location_length) {
    xLocationLengthArgs.add(location_length);
    xav.visitXLocationLength(location_length);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.TypeAnnotationVisitor#visitXOffset(int)
   */
  public void visitXOffset(int offset) {
    xOffsetArgs.add(offset);
    xav.visitXOffset(offset);
  }

  public void visitXNumEntries(int num_entries) {
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.TypeAnnotationVisitor#visitXStartPc(int)
   */
  public void visitXStartPc(int start_pc) {
    xStartPcArgs.add(start_pc);
    xav.visitXStartPc(start_pc);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.TypeAnnotationVisitor#visitXTargetType(int)
   */
  public void visitXTargetType(int target_type) {
    xTargetTypeArgs.add(target_type);
    xav.visitXTargetType(target_type);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.TypeAnnotationVisitor#visitXParamIndex(int)
   */
  public void visitXParamIndex(int param_index) {
    xParamIndexArgs.add(param_index);
    xav.visitXParamIndex(param_index);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.TypeAnnotationVisitor#visitXBoundIndex(int)
   */
  public void visitXBoundIndex(int bound_index) {
    if (bound_index != -1) {
      xBoundIndexArgs.add(bound_index);
      xav.visitXBoundIndex(bound_index);
    }
  }

  public void visitXTypeIndex(int type_index) {
    xTypeIndexArgs.add(type_index);
    xav.visitXTypeIndex(type_index);
  }

  /**
   * Visits the end of the annotation, and also performs a check
   *  to ensure that the information this has visited specifies a legal
   *  annotation.  If the information does not specify a legal annotation,
   *  throws an exception.
   *
   * @inheritDoc
   * @throws InvalidTypeAnnotationException if the information this
   *  has visited does not specify a legal extended annotation
   * @see org.objectweb.asm.AnnotationVisitor#visitEnd()
   */
  public void visitEnd() {
    if (xTargetTypeArgs.size() > 0) {
      checkX();
    } else {
      // This has not visited an the target type of an extended annotation, so
      //  must ensure that all other extended information lists are empty.
      if (xIndexArgs.size() != 0 ||
          xLengthArgs.size() != 0 ||
          xLocationArgs.size() != 0 ||
          xLocationLengthArgs.size() != 0 ||
          xOffsetArgs.size() != 0 ||
          xStartPcArgs.size() != 0) {
        throw new InvalidTypeAnnotationException(
        "No target type was specified, yet other visitX* methods were still called.");
      }
    }
    xav.visitEnd();
  }

  /**
   * Checks that the extended information this has visited is valid.
   *
   * @throws InvalidTypeAnnotationException if extended information is
   *  not valid
   */
  private void checkX() {
    // First, check to see that only one target type was specified, and
    // then dispatch to checkListSize() based on that target type.
    if (xTargetTypeArgs.size() != 1) {
      throw new
      InvalidTypeAnnotationException("More than one target type visited.");
    }

    // Since the correct size of xLocationArgs is specified by
    // xLocationLengthArgs, this information must be looked up first.
    int c = 0;
    if (xLocationLengthArgs.size() > 0) {
      c = xLocationLengthArgs.get(0);
    }

    switch(TargetType.fromTargetTypeValue(xTargetTypeArgs.get(0))) {
    case TYPECAST:
      checkListSize(0, 0, 0, 0, 1, 0, 0, 0, 0,
          "Invalid typecast annotation:");
      break;
    case TYPECAST_COMPONENT:
      checkListSize(0, 0, c, 1, 1, 0, 0, 0, 0,
      "Invalid typecast generic/array annotation:");
      break;
    case INSTANCEOF:
      checkListSize(0, 0, 0, 0, 1, 0, 0, 0, 0,
      "Invalid type test annotation:");
      break;
    case INSTANCEOF_COMPONENT:
      checkListSize(0, 0, c, 1, 1, 0, 0, 0, 0,
      "Invalid type test generic/array annotation:");
      break;
    case NEW:
      checkListSize(0, 0, 0, 0, 1, 0, 0, 0, 0,
      "Invalid object creation annotation:");
      break;
    case NEW_COMPONENT:
      checkListSize(0, 0, c, 1, 1, 0, 0, 0, 0,
      "Invalid object creation generic/array annotation:");
      break;
    case METHOD_RECEIVER:
      checkListSize(0, 0, 0, 0, 0, 0, 0, 0, 0,
      "Invalid method receiver annotation:");
      break;
    case METHOD_RECEIVER_COMPONENT:
      // TODO
      checkListSize(0, 0, 0, 0, 0, 0, 0, 0, 0,
      "Invalid method receiver generic/array annotation:");
      break;
    case LOCAL_VARIABLE:
      checkListSize(1, 1, 0, 0, 0, 1, 0, 0, 0,
      "Invalid local variable annotation:");
      break;
    case LOCAL_VARIABLE_COMPONENT:
      checkListSize(1, 1, c, 1, 0, 1, 0, 0, 0,
      "Invalid local variable generic/array annotation:");
      break;
    case METHOD_RETURN:
      checkListSize(0, 0, 0, 0, 0, 0, 0, 0, 0,
      "Invalid method return type annotation:");
      break;
    case METHOD_RETURN_COMPONENT:
      checkListSize(0, 0, c, 1, 0, 0, 0, 0, 0,
      "Invalid method return type generic/array annotation:");
      break;
    case METHOD_PARAMETER:
      checkListSize(0, 0, 0, 0, 0, 0, 1, 0, 0,
      "Invalid method parameter annotation:");
      break;
    case METHOD_PARAMETER_COMPONENT:
      checkListSize(0, 0, c, 1, 0, 0, 1, 0, 0,
      "Invalid method parameter generic/array annotation:");
      break;
    case FIELD:
      checkListSize(0, 0, c, 0, 0, 0, 0, 0, 0,
      "Invalid field annotation:");
      break;
    case FIELD_COMPONENT:
      checkListSize(0, 0, c, 1, 0, 0, 0, 0, 0,
      "Invalid field generic/array annotation:");
      break;
    case CLASS_TYPE_PARAMETER:
      checkListSize(0, 0, 0, 0, 0, 0, 1, 0, 0,
      "Invalid class type parameter annotation:");
      break;
    case CLASS_TYPE_PARAMETER_BOUND:
      checkListSize(0, 0, 0, 0, 0, 0, 1, 1, 0,
      "Invalid class type parameter bound annotation:");
      break;
    case CLASS_TYPE_PARAMETER_BOUND_COMPONENT:
      checkListSize(0, 0, c, 1, 0, 0, 1, 1, 0,
      "Invalid class type parameter bound generic/array annotation:");
      break;
    case METHOD_TYPE_PARAMETER:
      checkListSize(0, 0, 0, 0, 0, 0, 1, 0, 0,
      "Invalid method type parameter annotation:");
      break;
    case METHOD_TYPE_PARAMETER_BOUND:
      checkListSize(0, 0, 0, 0, 0, 0, 1, 1, 0,
      "Invalid method type parameter bound annotation:");
      break;
    case METHOD_TYPE_PARAMETER_BOUND_COMPONENT:
      checkListSize(0, 0, c, 1, 0, 0, 1, 1, 0,
      "Invalid method type parameter bound generic/array annotation:");
      break;
    case CLASS_EXTENDS:
      checkListSize(0, 0, 0, 0, 0, 0, 0, 0, 1,
      "Invalid class extends/implements annotation:");
      break;
    case CLASS_EXTENDS_COMPONENT:
      checkListSize(0, 0, 0, 0, 0, 0, 0, 0, 1,
      "Invalid class extends/implements generic/array annotation:");
      break;
    case THROWS:
      checkListSize(0, 0, 0, 0, 0, 0, 0, 0, 1,
      "Invalid exception type in throws annotation:");
      break;
    default:
      throw new InvalidTypeAnnotationException(
          "Unknown target type given: " + xTargetTypeArgs.get(0));
    }
  }

  /**
   * If list.size() != correctLength, appends a descriptive error message to sb
   *  specifying how many times methodName was supposed to be called and how
   *  many times it was actually called.
   * Else, has no effect.
   *
   * @param list the list of arguments actually visited
   * @param correctLength the correct length of list
   * @param methodName the name of the method whose arguments went into list
   * @param sb the StringBuilder to append error messages to
   */
  private void appendMessage(List<Integer> list, int idealLength,
      String methodName, StringBuilder sb) {
    if (list.size() != idealLength) {
      sb.append("\nInvalid method calls: ");
      sb.append(methodName);
      sb.append(" was called ");
      sb.append(list.size());
      sb.append(" times, but should have only been called ");
      sb.append(idealLength);
      sb.append(" times");
    }
  }

  /**
   * Checks that the seven lists containing extended annotation information are
   *  of the specified sizes.  If at least one of the lists is not of the
   *  correct length, this throws an exception with a message equal to msg, plus
   *  information describing which lists were incorrect.
   *
   * @throws InvalidTypeAnnotationException if the extended information
   *  lists are not of the correct length
   */
  private void checkListSize(
      int correctLengthIndex,
      int correctLengthLength,
      int correctLengthLocation,
      int correctLengthLocationLength,
      int correctLengthOffset,
      int correctLengthStartPc,
      int correctLengthParamIndex,
      int correctLengthBoundIndex,
      int correctLengthTypeIndex,
      String msg) {
    StringBuilder sb = new StringBuilder();
    appendMessage(xIndexArgs, correctLengthIndex, "visitXIndex", sb);
    appendMessage(xLengthArgs, correctLengthLength, "visitXLength", sb);
    appendMessage(xLocationArgs, correctLengthLocation, "visitXLocation", sb);
    appendMessage(xLocationLengthArgs, correctLengthLocationLength,
        "visitXLocationLength", sb);
    appendMessage(xOffsetArgs, correctLengthOffset, "visitXOffset", sb);
    appendMessage(xStartPcArgs, correctLengthStartPc, "visitXStartPc", sb);
    appendMessage(xParamIndexArgs, correctLengthParamIndex, "visitXParamIndex", sb);
    appendMessage(xBoundIndexArgs, correctLengthBoundIndex, "visitXBoundIndex", sb);
    appendMessage(xTypeIndexArgs, correctLengthTypeIndex, "VisitXTypeIndex", sb);

    // At this point, sb will contain Strings iff there is an error
    //  in the extended annotation information.
    String s = sb.toString();
    if (s.length() > 0) {
      throw new InvalidTypeAnnotationException(msg + s);
    }
  }
}
