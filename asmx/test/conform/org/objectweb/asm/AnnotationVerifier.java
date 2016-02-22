package org.objectweb.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.TypeAnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * An <code>AnnotationVerifier</code> provides a way to check to see if two
 * versions of the same class (from two different <code>.class</code> files),
 * have the same annotations on the same elements.
 */
public class AnnotationVerifier {

  /**
   * The "correct" version of the class to verify against.
   */
  private ClassRecorder originalVisitor;

  /**
   * The uncertain version of the class to verify.
   */
  private ClassRecorder newVisitor;

  /**
   * Constructs a new <code>AnnotationVerifier</code> that does not yet have
   * any information about the class.
   */
  public AnnotationVerifier() {
    originalVisitor = new ClassRecorder();
    newVisitor = new ClassRecorder();
  }

  /**
   * Returns the <code>ClassVisitor</code> which should be made to visit the
   * version of the class known to be correct.
   *
   * @return a visitor for the good version of the class
   */
  public ClassVisitor originalVisitor() {
    return originalVisitor;
  }

  /**
   * Returns the <code>ClassVisitor</code> which should be made to visit the
   * version of the class being tested.
   *
   * @return a visitor the the experimental version of the class
   */
  public ClassVisitor newVisitor() {
    return newVisitor;
  }

  /**
   * Verifies that the visitors returned by {@link #originalVisitor()} and
   * {@link #newVisitor()} have visited the same class.  This method can only
   * be called if both visitors have already visited a class.
   *
   * @throws AnnotationMismatchException if the two visitors have not visited
   * two versions of the same class that contain idential annotations.
   */
  public void verify() {
    if(!newVisitor.name.equals(originalVisitor.name)) {
      throw new AnnotationMismatchException(
          "Cannot verify two different classes " +
          newVisitor.name + " cannot be verified against " +
          originalVisitor.name);
    }
    newVisitor.verifyAgainst(originalVisitor);
  }

  /**
   * A ClassRecorder records all the annotations that it visits, and serves
   * as a ClassVisitor, FieldVisitor and MethodVisitor.
   */
  private class ClassRecorder extends EmptyVisitor {

    private String description;

    public String name;
    private String signature;

    private Map<String, ClassRecorder> fieldRecorders;
    // key is unparameterized name

    private Map<String, ClassRecorder> methodRecorders;
    // key is complete method signature

    // general annotations
    private Map<String, AnnotationRecorder> anns;
    private Map<String, AnnotationRecorder> xanns;

    //method specific annotations
    private Set<AnnotationRecorder> danns;
    private Map<ParameterDescription, AnnotationRecorder> panns;

    public ClassRecorder() {
      this("[class: ","","");
    }

    public ClassRecorder(String internalDescription, String name, String signature) {
      this.description = internalDescription;
      this.name = name;
      this.signature = signature;

      fieldRecorders = new HashMap<String, ClassRecorder>();
      methodRecorders = new HashMap<String, ClassRecorder>();

      anns = new HashMap<String, AnnotationRecorder>();
      xanns = new HashMap<String, AnnotationRecorder>();

      danns = new HashSet<AnnotationRecorder>();
      panns = new HashMap<ParameterDescription, AnnotationRecorder>();
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      this.name = name;
      this.signature = signature;
      description = description + name;
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      AnnotationRecorder av = new AnnotationRecorder(
          description + " annotation: " + desc);
      anns.put(desc,av);
      return av;
    }

    public TypeAnnotationVisitor visitTypeAnnotation(String desc, boolean visible) {
      AnnotationRecorder av = new AnnotationRecorder(
          description + " annotation: " + desc);
      xanns.put(desc,av);
      return av;
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
      ClassRecorder fr =
        new ClassRecorder(
            description + " field: " + name,
            name, signature);
      fieldRecorders.put(name, fr);
      return fr;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      ClassRecorder mr =
        new ClassRecorder(
            description + " method: " + name + desc, name+desc, signature);
      methodRecorders.put(name+desc, mr);
      return mr;
    }

    // MethodVisitor methods:
    public AnnotationVisitor visitAnnotationDefault() {
      AnnotationRecorder dr = new AnnotationRecorder(
          description + " default annotation");
      danns.add(dr);
      return dr;
    }

    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
      ParameterDescription pd =
        new ParameterDescription(parameter, desc, visible);
      AnnotationRecorder pr =
        new AnnotationRecorder(description +
            " parameter annotation: " + pd);
      panns.put(pd, pr);
      return pr;
    }

    public void verifyAgainst(ClassRecorder correct) {
      // first, ensure all annotations are correct
      verifyAnns(this.anns, correct.anns);
      verifyAnns(this.xanns, correct.xanns);

      // then recurse into any annotations on fields/methods
      verifyMemberAnns(this.fieldRecorders, correct.fieldRecorders);
      verifyMemberAnns(this.methodRecorders, correct.methodRecorders);
    }

    private void verifyAnns(
        Map<String, AnnotationRecorder> questionableAnns,
        Map<String, AnnotationRecorder> correctAnns) {
      Set<AnnotationRecorder> unresolvedQuestionableAnns =
        new HashSet<AnnotationRecorder>(questionableAnns.values());

      for(Map.Entry<String, AnnotationRecorder> entry :
        correctAnns.entrySet()) {
        String name = entry.getKey();
        AnnotationRecorder correctAnn = entry.getValue();
        AnnotationRecorder questionableAnn = questionableAnns.get(name);
        if(questionableAnn == null) {
          throw new AnnotationMismatchException(description +
              " does not contain expected annotation: " + correctAnn);
        }

        questionableAnn.verifyAgainst(correctAnn);

        unresolvedQuestionableAnns.remove(questionableAnn);
      }

      for(AnnotationRecorder unexpectedAnnOnThis : unresolvedQuestionableAnns) {
        throw new AnnotationMismatchException(description +
            " contains unexpected annotation : " + unexpectedAnnOnThis);
      }
    }

    private void verifyMemberAnns(
        Map<String, ClassRecorder> questionableMembers,
        Map<String, ClassRecorder> correctMembers) {
      Set<ClassRecorder> unresolvedQuestionableMembers =
        new HashSet<ClassRecorder>(questionableMembers.values());

      for(Map.Entry<String, ClassRecorder> entry :
        correctMembers.entrySet()) {
        String name = entry.getKey();
        ClassRecorder correctMember = entry.getValue();
        ClassRecorder questionableMember = questionableMembers.get(name);
        if(questionableMember == null) {
          throw new AnnotationMismatchException(description +
              " does not contain expected member: " + correctMember);
        }

        questionableMember.verifyAgainst(correctMember);

        unresolvedQuestionableMembers.remove(questionableMember);
      }

      for(ClassRecorder unexpectedMemberOnThis : unresolvedQuestionableMembers) {
        System.out.println("Going to throw exception: ");
        System.out.println("questionable: " + questionableMembers);
        System.out.println("correct: " + correctMembers);

        throw new AnnotationMismatchException(description +
            " contains unexpected member: " + unexpectedMemberOnThis);
      }
    }

    public String toString() {
      return description;
    }
  }

  /**
   * An AnnotationRecorder is an TypeAnnotationVisitor that records all the
   * information it visits.
   */
  private class AnnotationRecorder implements TypeAnnotationVisitor {
    private String description;

    private List<String> fieldArgs1;
    private List<Object> fieldArgs2;

    private List<String> enumArgs1;
    private List<String> enumArgs2;
    private List<String> enumArgs3;

    private List<String> innerAnnotationArgs1;
    private List<String> innerAnnotationArgs2;
    private Map<String, AnnotationRecorder> innerAnnotationMap;

    private List<String> arrayArgs;
    private Map<String, AnnotationRecorder> arrayMap;

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

    public AnnotationRecorder(String description) {
      this.description = description;
      fieldArgs1 = new ArrayList<String>();
      fieldArgs2 = new ArrayList<Object>();

      enumArgs1 = new ArrayList<String>();
      enumArgs2 = new ArrayList<String>();
      enumArgs3 = new ArrayList<String>();

      innerAnnotationArgs1 = new ArrayList<String>();
      innerAnnotationArgs2 = new ArrayList<String>();
      innerAnnotationMap = new HashMap<String, AnnotationRecorder>();

      arrayArgs = new ArrayList<String>();
      arrayMap = new HashMap<String, AnnotationRecorder>();

      xIndexArgs = new ArrayList<Integer>();
      xLengthArgs = new ArrayList<Integer>();
      xLocationArgs = new ArrayList<Integer>();
      xLocationLengthArgs = new ArrayList<Integer>();
      xOffsetArgs = new ArrayList<Integer>();
      xStartPcArgs = new ArrayList<Integer>();
      xTargetTypeArgs = new ArrayList<Integer>();
      xParamIndexArgs = new ArrayList<Integer>();
      xBoundIndexArgs = new ArrayList<Integer>();
      xTypeIndexArgs = new ArrayList<Integer>();
    }

    public void visitXIndex(int index) {
      xIndexArgs.add(index);
    }

    public void visitXLength(int length) {
      xLengthArgs.add(length);
    }

    public void visitXLocation(int location) {
      xLocationArgs.add(location);
    }

    public void visitXLocationLength(int location_length) {
     xLocationLengthArgs.add(location_length);
    }

    public void visitXOffset(int offset) {
      xOffsetArgs.add(offset);
    }

    @Override
    public void visitXNumEntries(int num_entries) { }

    public void visitXStartPc(int start_pc) {
      xStartPcArgs.add(start_pc);
    }

    public void visitXTargetType(int target_type) {
      xTargetTypeArgs.add(target_type);
    }

    public void visitXParamIndex(int param_index) {
      xParamIndexArgs.add(param_index);
    }

    public void visitXBoundIndex(int bound_index) {
      xBoundIndexArgs.add(bound_index);
    }

    public void visitXTypeIndex(int type_index) {
      xTypeIndexArgs.add(type_index);
    }

    public void visit(String name, Object value) {
      fieldArgs1.add(name);
      fieldArgs2.add(value);
    }

    public AnnotationVisitor visitAnnotation(String name, String desc) {
      innerAnnotationArgs1.add(name);
      innerAnnotationArgs2.add(name);

      AnnotationRecorder av= new AnnotationRecorder(description + name);
      innerAnnotationMap.put(name, av);
      return av;
    }

    public AnnotationVisitor visitArray(String name) {
      arrayArgs.add(name);
      AnnotationRecorder av = new AnnotationRecorder(description + name);
      arrayMap.put(name, av);
      return av;
    }

    public void visitEnd() {
    }

    public void visitEnum(String name, String desc, String value) {
      enumArgs1.add(name);
      enumArgs2.add(desc);
      enumArgs3.add(value);
    }

    public String toString() {
      return description;
    }

    /**
     * Checks that the information passed into this matches the information
     * passed into another AnnotationRecorder.  For right now, the order in
     * which information is passed in does matter.  If there is a conflict in
     * information, an exception will be thrown.
     *
     * @param ar an annotation recorder that has visited the correct information
     *  this should visit
     * @throws AnnotationMismatchException if the information visited by this
     *  does not match the information in ar
     */
    public void verifyAgainst(AnnotationRecorder ar) {
      StringBuilder sb = new StringBuilder();
      verifyList(sb, "visit()", 1, this.fieldArgs1, ar.fieldArgs1);
      verifyList(sb, "visit()", 2, this.fieldArgs2, ar.fieldArgs2);

      verifyList(sb, "visitEnum()", 1, this.enumArgs1, ar.enumArgs1);
      verifyList(sb, "visitEnum()", 2, this.enumArgs2, ar.enumArgs2);
      verifyList(sb, "visitEnum()", 3, this.enumArgs3, ar.enumArgs3);

      verifyList(sb, "visitAnnotation()", 1, this.innerAnnotationArgs1, ar.innerAnnotationArgs1);
      verifyList(sb, "visitAnnotation()", 2, this.innerAnnotationArgs2, ar.innerAnnotationArgs2);

      verifyList(sb, "visitArray()", 1, this.arrayArgs, ar.arrayArgs);

      verifyList(sb, "visitXIndexArgs()", 1, this.xIndexArgs, ar.xIndexArgs);
      verifyList(sb, "visitXLength()", 1, this.xLengthArgs, ar.xIndexArgs);
      verifyList(sb, "visitXLocation()", 1, this.xLocationArgs, ar.xLocationArgs);
      verifyList(sb, "visitXLocationLength()", 1, this.xLocationLengthArgs, ar.xLocationLengthArgs);
      verifyList(sb, "visitXOffset()", 1, this.xOffsetArgs, ar.xOffsetArgs);
      verifyList(sb, "visitXStartPc()", 1, this.xStartPcArgs, ar.xStartPcArgs);
      verifyList(sb, "visitXTargetType()", 1, this.xTargetTypeArgs, ar.xTargetTypeArgs);
      verifyList(sb, "visitXParamIndex()", 1, this.xParamIndexArgs, ar.xParamIndexArgs);
      verifyList(sb, "visitXBoundIndex()", 1, this.xBoundIndexArgs, ar.xBoundIndexArgs);
      verifyList(sb, "visitXTypeIndex()", 1, this.xTypeIndexArgs, ar.xTypeIndexArgs);

      verifyInnerAnnotationRecorder(sb, this.innerAnnotationMap, ar.innerAnnotationMap);
      verifyInnerAnnotationRecorder(sb, this.arrayMap, ar.arrayMap);

      if(sb.length() > 0) {
        throw new AnnotationMismatchException(sb.toString());
      }
    }

    private void verifyList(
        StringBuilder sb,
        String methodName,
        int parameter,
        List questionable,
        List correct) {
      if(!questionable.equals(correct)) {
        String s = "\n" + description +
        " was called with unexpected information in parameter: " + parameter +
        "\nReceived: " + questionable  +
        "\nExpected: " + correct;
      }
    }

    private void verifyInnerAnnotationRecorder(
        StringBuilder sb,
        Map<String, AnnotationRecorder> questionableAR,
        Map<String, AnnotationRecorder> correctAR) {
      // checks on arguments passed in to the methods that created these
      // AnnotationRecorders (i.e. the checks on the String keys on these maps)
      // ensure that these have identical keys
      for(Map.Entry<String, AnnotationRecorder> questionableEntry :
        questionableAR.entrySet()) {
        questionableEntry.getValue().verifyAgainst(
            correctAR.get(questionableEntry.getClass()));
      }
    }

  }

  /**
   * A ParameterDescription is a convenient class used to keep information about
   * method parameters.  Parameters are equal if they have the same index,
   * regardless of their description.
   */
  private class ParameterDescription {
    public final int parameter;
    public final String desc;
    public final boolean visible;

    public ParameterDescription(int parameter, String desc, boolean visible) {
      this.parameter = parameter;
      this.desc = desc;
      this.visible = visible;
    }

    public boolean equals(Object o) {
      if(o instanceof ParameterDescription) {
        ParameterDescription p = (ParameterDescription) o;
        return this.parameter == p.parameter;
      }
      return false;
    }

    public int hashCode() {
      return parameter * 17;
    }

    public String toString() {
      return
        "parameter index: " + parameter +
        " desc: " + desc +
        " visible: " + visible;
    }
  }

  /**
   * An AnnotationMismatchException is an Exception that indicates that
   * two versions of the same class do not have the same annotations on
   * either the class, its field, or its methods.
   */
  public class AnnotationMismatchException extends RuntimeException {
    private static final long serialVersionUID = 20060714L; // today's date

    /**
     * Constructs a new AnnotationMismatchException with the given error message.
     *
     * @param msg the error as to why the annotations do not match.
     */
    public AnnotationMismatchException(String msg) {
      super(msg);
    }
  }
}
