package org.objectweb.asm;

/** 
 * A visitor to visit a Java extended annotation.  The methods of this 
 * interface must be called in the following order:
 * (<tt>visit<tt> | 
 *  <tt>visitEnum<tt> | 
 *  <tt>visitAnnotation<tt> | 
 *  <tt>visitArray<tt>)* 
 * 
 * <tt>visitXTargetType<tt>
 * 
 * (<i>visit the fields (if any) of the <tt>reference_info</tt> structure
 *  for the given target type in the correct order using <tt>visitXOffset</tt>,
 *  <tt>visitXStartPc</tt>, <tt>visitXLength</tt>, <tt>visitXIndex</tt>,
 *  <tt>visitXParamIndex</tt>, <tt>visitXBoundIndex</tt>,
 *  <tt>visitXLocationLength</tt>, and/or <tt>visitXLocation</tt></i>)
 *  
 * <tt>visitEnd<tt>
 * 
 * @author jaimeq
 */

public interface TypeAnnotationVisitor extends AnnotationVisitor {
  /**
   * Visits the target type of the extended annotation, which defines the
   * type and structure of the reference info of the extended annotation.
   * 
   * @param target_type the target type of the extended annotation
   */
  void visitXTargetType(int target_type);

  /**
   * Visits the offset specified by the extended annotation, whose meaning
   * depends on the extended annotation's target type.
   * 
   * @param offset the offset specified by the extended annotation
   */
  void visitXOffset(int offset);

  /**
   * Visits the location_length specified by the extended annotation, whose 
   * meaning depends on the extended annotation's target type.
   * 
   * @param location_length the location_length specified by the extended 
   *  annotation
   */
  void visitXLocationLength(int location_length);

  /**
   * Visits the location specified by the extended annotation, whose meaning 
   * depends on the extended annotation's target type.
   * 
   * @param location the location specified by the extended annotation
   */
  void visitXLocation(int location);

  void visitXNumEntries(int num_entries);

  /**
   * Visits the start_pc specified by the extended annotation, whose meaning
   * depends on the extended annotation's target type.
   * 
   * @param start_pc the start_pc specified by the extended annotation
   */
  void visitXStartPc(int start_pc);

  /**
   * Visits the length specified by the extended annotation, whose meaning
   * depends on the extended annotation's target type.
   * 
   * @param length the length specified by the extended annotation
   */
  void visitXLength(int length);

  /**
   * Visits the index specified by the extended annotation, whose meaning 
   * depends on the extended annotation's target type.
   * 
   * @param index the index specified by the extended annotation
   */
  void visitXIndex(int index);

  /**
   * Visits the param_index specified by the extended annotation, whose meaning
   * depends on the extended annotation's target type.
   * 
   * @param param_index the param_index specified by the extended annotation
   */
  public void visitXParamIndex(int param_index);

  /**
   * Visits the bound_index specified by the extended annotation, whose meaning
   * depends on the extended annotation's target type.
   * 
   * @param bound_index the bound_index specified by the extended annotation
   */
  public void visitXBoundIndex(int bound_index);

  /**
   * Visits the type_index specified by the extended annotation, whose meaning
   * depends on the extended annotation's target type.
   * @param type_index
   */
  public void visitXTypeIndex(int type_index);
}
