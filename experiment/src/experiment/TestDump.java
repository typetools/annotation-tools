package experiment;

import org.objectweb.asm.*;

public class TestDump implements Opcodes {

  public static byte[] dump() throws Exception {

    ClassWriter classWriter = new ClassWriter(0);
    FieldVisitor fieldVisitor;
    MethodVisitor methodVisitor;
    AnnotationVisitor annotationVisitor0;

    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "experiment/Test", "<T1:Lexperiment/Super1;>Lexperiment/Super2<Lexperiment/T2;>;", "experiment/Super2", null);

    classWriter.visitSource("Test.java", null);

    {
      annotationVisitor0 = classWriter.visitAnnotation("Lexperiment/Annotations$A;", false);
      annotationVisitor0.visitEnd();
    }
    {
      annotationVisitor0 = classWriter.visitTypeAnnotation(0x10FFFF00, null, "Lexperiment/Annotations$C;", false);
      annotationVisitor0.visitEnd();
    }
    {
      annotationVisitor0 = classWriter.visitTypeAnnotation(0x10FFFF00, TypePath.fromString("0;"), "Lexperiment/Annotations$D;", false);
      annotationVisitor0.visitEnd();
    }
    {
      annotationVisitor0 = classWriter.visitTypeAnnotation(0, null, "Lexperiment/Annotations$B;", false);
      annotationVisitor0.visitEnd();
    }
    classWriter.visitInnerClass("experiment/Annotations$A", "experiment/Annotations", "A", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$C", "experiment/Annotations", "C", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$D", "experiment/Annotations", "D", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$B", "experiment/Annotations", "B", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    {
      methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      methodVisitor.visitCode();
      Label label0 = new Label();
      methodVisitor.visitLabel(label0);
      methodVisitor.visitLineNumber(9, label0);
      methodVisitor.visitVarInsn(ALOAD, 0);
      methodVisitor.visitMethodInsn(INVOKESPECIAL, "experiment/Super2", "<init>", "()V", false);
      methodVisitor.visitInsn(RETURN);
      methodVisitor.visitMaxs(1, 1);
      methodVisitor.visitEnd();
    }
    classWriter.visitEnd();

    return classWriter.toByteArray();
  }
}
