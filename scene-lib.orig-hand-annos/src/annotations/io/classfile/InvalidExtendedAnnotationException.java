package annotations.io.classfile;

/**
 * An <code>InvalidExtendedAnnotationException </code> indicates that an
 * extended annotation was created with invalid information.  For example,
 * an extended annotation on a local variable should not contain offset
 * information.
 */
public class InvalidExtendedAnnotationException extends RuntimeException {
  static final long serialVersionUID = 20060712L; // Today's date.
  
  /**
   * Constructs a new <code> InvalidExtendedAnnotationException </code> with
   * the given error message.
   * 
   * @param msg a message describing what was wrong with the extended annotation
   */
  public InvalidExtendedAnnotationException(String msg) {
    super(msg);
  }
}
