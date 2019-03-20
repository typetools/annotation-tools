package experiment;

import org.objectweb.asm.*;

public class TestBigDump implements Opcodes {

  public static byte[] dump() throws Exception {

    ClassWriter classWriter = new ClassWriter(0);
    FieldVisitor fieldVisitor;
    MethodVisitor methodVisitor;
    AnnotationVisitor annotationVisitor0;

    classWriter.visit(V1_8, ACC_SUPER, "experiment/TestBig",
        "<T1:Lexperiment/Super2<Lexperiment/T3<Lexperiment/T2;>;>;>Lexperiment/Super3<Lexperiment/Super4;>;",
        "experiment/Super3", null);

    classWriter.visitSource("TestBig.java", null);

    {
      annotationVisitor0 = classWriter.visitAnnotation("Lexperiment/Annotations$A;", false);
      annotationVisitor0.visitEnd();
    }
    {
      annotationVisitor0 = classWriter.visitTypeAnnotation(285212416, null,
          "Lexperiment/Annotations$F;", false);
      annotationVisitor0.visitEnd();
    }
    {
      annotationVisitor0 = classWriter.visitTypeAnnotation(285212416, TypePath.fromString("0;"),
          "Lexperiment/Annotations$G;", false);
      annotationVisitor0.visitEnd();
    }
    {
      annotationVisitor0 = classWriter.visitTypeAnnotation(TypeReference.CLASS_TYPE_PARAMETER, null,
          "Lexperiment/Annotations$B;", false);
      annotationVisitor0.visitEnd();
    }
    {
      annotationVisitor0 = classWriter.visitTypeAnnotation(285212672, null,
          "Lexperiment/Annotations$C;", false);
      annotationVisitor0.visitEnd();
    }
    {
      annotationVisitor0 = classWriter.visitTypeAnnotation(285212672, TypePath.fromString("0;"),
          "Lexperiment/Annotations$D;", false);
      annotationVisitor0.visitEnd();
    }
    {
      annotationVisitor0 = classWriter.visitTypeAnnotation(285212672, TypePath.fromString("0;0;"),
          "Lexperiment/Annotations$E;", false);
      annotationVisitor0.visitEnd();
    }
    classWriter.visitInnerClass("experiment/Annotations$A", "experiment/Annotations", "A",
        ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$F", "experiment/Annotations", "F",
        ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$G", "experiment/Annotations", "G",
        ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$B", "experiment/Annotations", "B",
        ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$C", "experiment/Annotations", "C",
        ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$D", "experiment/Annotations", "D",
        ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$E", "experiment/Annotations", "E",
        ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    {
      methodVisitor = classWriter.visitMethod(0, "<init>", "()V", null, null);
      methodVisitor.visitCode();
      Label label0 = new Label();
      methodVisitor.visitLabel(label0);
      methodVisitor.visitLineNumber(5, label0);
      methodVisitor.visitVarInsn(ALOAD, 0);
      methodVisitor.visitMethodInsn(INVOKESPECIAL, "experiment/Super3", "<init>", "()V", false);
      methodVisitor.visitInsn(RETURN);
      methodVisitor.visitMaxs(1, 1);
      methodVisitor.visitEnd();
    }
    classWriter.visitEnd();

    return classWriter.toByteArray();
  }
}
