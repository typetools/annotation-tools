//This is a wrapper class around an ExtendedAnnotationVisitor that can be used
//to verify the information it visits specifies a valid [extended] annotation.

package annotations.io.classfile;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ExtendedAnnotationVisitor;

import annotations.TargetType;

/**
 * A <code>SafeExtendedAnnotationVisitor</code> wraps around an 
 * ExtendedAnnotationVisitor and delegates all calls to it.  However, it 
 * maintains a record of all methods that have been called and on
 * calling {@link SafeExtendedAnnotationVisitor#visitEnd}, 
 * performs a check to verify that all the data passed
 * to this visitor specifies a legal annotation or extended annotation.  It's
 * intended use is to wrap around an <code>ExtendedAnnotationWriter</code> and
 * thus ensure that no illegal class files are written, although it will work
 * more generally on any visitor.
 * 
 * Also note that nothing special needs to be done about subannotations
 * and regular arrays, since their lengths are not passed in to this visitor.
 * 
 * If none of the <code>visitX*</code> methods have been called, it is 
 * automatically a legal annotation.  Else, if some of the <code>visitX*</code>
 * methods have been called, the check will ensure that the data passed to this 
 * specifies a legal extended annotation, as defined by its target type.
 */
public class SafeExtendedAnnotationVisitor 
implements ExtendedAnnotationVisitor {

  // The visitor this delegates all calls to.
  private final ExtendedAnnotationVisitor xav;

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

  /**
   * Constructs a new <code> SafeExtendedAnnotationVisitor </code> that
   *  delegates all calls to the given visitor.
   *  
   * @param xav the visitor to delegate all method calls to
   */
  public SafeExtendedAnnotationVisitor(ExtendedAnnotationVisitor xav) {
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
   * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXIndex(int)
   */
  public void visitXIndex(int index) {
    xIndexArgs.add(index);
    xav.visitXIndex(index);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXLength(int)
   */
  public void visitXLength(int length) {
    xLengthArgs.add(length);
    xav.visitXLength(length);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXLocation(int)
   */
  public void visitXLocation(int location) {
    xLocationArgs.add(location);
    xav.visitXLocation(location);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXLocationLength(int)
   */
  public void visitXLocationLength(int location_length) {
    xLocationLengthArgs.add(location_length);
    xav.visitXLocationLength(location_length);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXOffset(int)
   */
  public void visitXOffset(int offset) {
    xOffsetArgs.add(offset);
    xav.visitXOffset(offset);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXStartPc(int)
   */
  public void visitXStartPc(int start_pc) {
    xStartPcArgs.add(start_pc);
    xav.visitXStartPc(start_pc);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXTargetType(int)
   */
  public void visitXTargetType(int target_type) {
    xTargetTypeArgs.add(target_type);
    xav.visitXTargetType(target_type);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXParamIndex(int)
   */
  public void visitXParamIndex(int param_index) {
    xParamIndexArgs.add(param_index);
    xav.visitXParamIndex(param_index);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXBoundIndex(int)
   */
  public void visitXBoundIndex(int bound_index) {
    xBoundIndexArgs.add(bound_index);
    xav.visitXBoundIndex(bound_index);
  }
  
  /**
   * Visits the end of the annotation, and also performs a check
   *  to ensure that the information this has visited specifies a legal 
   *  annotation.  If the information does not specify a legal annotation,
   *  throws an exception.
   *  
   * @inheritDoc
   * @throws InvalidExtendedAnnotationException if the information this
   *  has visited does not specify a legal extended annotation
   * @see org.objectweb.asm.AnnotationVisitor#visitEnd()
   */
  public void visitEnd() {
    if(xTargetTypeArgs.size() > 0) {
      checkX();
    } else {
      // This has not visited an the target type of an extended annotation, so 
      //  must ensure that all other extended information lists are empty.
      if(xIndexArgs.size() != 0 || 
          xLengthArgs.size() != 0 || 
          xLocationArgs.size() != 0 ||
          xLocationLengthArgs.size() != 0 || 
          xOffsetArgs.size() != 0 || 
          xStartPcArgs.size() != 0) {
        throw new InvalidExtendedAnnotationException(
        "No target type was specified, yet other visitX* methods were still called.");
      }
    }
    xav.visitEnd();
  }

  /**
   * Checks that the extended information this has visited is valid.
   * 
   * @throws InvalidExtendedAnnotationException if extended information is
   *  not valid
   */
  private void checkX() {
    // First, check to see that only one target type was specified, and
    // then dispatch to checkListSize() based on that target type.
    if(xTargetTypeArgs.size() != 1) {
      throw new 
      InvalidExtendedAnnotationException("More than one target type visited.");
    }

    // Since the correct size of xLocationArgs is specified by
    // xLocationLengthArgs, this information must be looked up first.
    int c = 0;
    if(xLocationLengthArgs.size() > 0) {
      c = xLocationLengthArgs.get(0);
    }

    switch(TargetType.values()[xTargetTypeArgs.get(0)]) {
    case TYPECAST:
      checkListSize(0, 0, 0, 0, 1, 0, 0, 0, 
          "Invalid typecast annotation:");
      break;
    case TYPECAST_GENERIC_OR_ARRAY:
      checkListSize(0, 0, c, 1, 1, 0, 0, 0, 
      "Invalid typecast generic/array annotation:");
      break; 
    case INSTANCEOF:        
      checkListSize(0, 0, 0, 0, 1, 0, 0, 0, 
      "Invalid type test annotation:");
      break;
    case INSTANCEOF_GENERIC_OR_ARRAY:
      checkListSize(0, 0, c, 1, 1, 0, 0, 0, 
      "Invalid type test generic/array annotation:");
      break;
    case NEW:
      checkListSize(0, 0, 0, 0, 1, 0, 0, 0, 
      "Invalid object creation annotation:");
      break;
    case NEW_GENERIC_OR_ARRAY:
      checkListSize(0, 0, c, 1, 1, 0, 0, 0, 
      "Invalid object creation generic/array annotation:");
      break;
    case METHOD_RECEIVER:
      checkListSize(0, 0, 0, 0, 0, 0, 0, 0, 
      "Invalid method receiver annotation:");
      break;
    case LOCAL_VARIABLE:
      checkListSize(1, 1, 0, 0, 0, 1, 0, 0, 
      "Invalid local variable annotation:");
      break;
    case LOCAL_VARIABLE_GENERIC_OR_ARRAY:
      checkListSize(1, 1, c, 1, 0, 1, 0, 0, 
      "Invalid local variable generic/array annotation:");
      break; 
    case METHOD_RETURN_GENERIC_OR_ARRAY:
      checkListSize(0, 0, c, 1, 0, 0, 0, 0, 
      "Invalid method return type generic/array annotation:");
      break; 
    case METHOD_PARAMETER_GENERIC_OR_ARRAY:
      checkListSize(0, 0, c, 1, 0, 0, 0, 0, 
      "Invalid method parameter generic/array annotation:");
      break; 
    case FIELD_GENERIC_OR_ARRAY:
      checkListSize(0, 0, c, 1, 0, 0, 0, 0, 
      "Invalid field generic/array annotation:");
      break;
    case CLASS_TYPE_PARAMETER_BOUND:
      checkListSize(0, 0, 0, 0, 0, 0, 1, 1, 
      "Invalid class type parameter bound annotation:");
      break;
    case CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY:
      checkListSize(0, 0, c, 1, 0, 0, 1, 1, 
      "Invalid class type parameter bound generic/array annotation:");
      break;
    case METHOD_TYPE_PARAMETER_BOUND:
      checkListSize(0, 0, 0, 0, 0, 0, 1, 1, 
      "Invalid method type parameter bound annotation:");
      break;
    case METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY:
      checkListSize(0, 0, c, 1, 0, 0, 1, 1, 
      "Invalid method type parameter bound generic/array annotation:");
      break;
    default:
      throw new InvalidExtendedAnnotationException(
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
    if(list.size() != idealLength) {
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
   * @throws InvalidExtendedAnnotationException if the extended information 
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
      String msg) {     
    StringBuilder sb = new StringBuilder();
    appendMessage(xIndexArgs, correctLengthIndex, "visitXIndex", sb);
    appendMessage(xLengthArgs, correctLengthLength, "visitXLength",  sb);
    appendMessage(xLocationArgs, correctLengthLocation, "visitXLocation", sb);
    appendMessage(xLocationLengthArgs, correctLengthLocationLength, 
        "visitXLocationLength",  sb);
    appendMessage(xOffsetArgs, correctLengthOffset, "visitXOffset", sb);
    appendMessage(xStartPcArgs, correctLengthStartPc, "visitXStartPc", sb);
    appendMessage(xParamIndexArgs, correctLengthParamIndex, "visitXParamIndex", sb);
    appendMessage(xBoundIndexArgs, correctLengthBoundIndex, "visitXBoundIndex", sb);

    // At this point, sb will contain Strings iff there is an error
    //  in the extended annotation information.
    String s = sb.toString();
    if(s.length() > 0) {
      throw new InvalidExtendedAnnotationException(msg + s);
    }
  }
}
