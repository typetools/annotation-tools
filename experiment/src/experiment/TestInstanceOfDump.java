package experiment;

import org.objectweb.asm.*;

public class TestInstanceOfDump implements Opcodes {

  public static byte[] dump() throws Exception {

    ClassWriter classWriter = new ClassWriter(0);
    FieldVisitor fieldVisitor;
    MethodVisitor methodVisitor;
    AnnotationVisitor annotationVisitor0;

    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "experiment/TestInstanceOf", null, "java/lang/Object", null);

    classWriter.visitSource("TestInstanceOf.java", null);

    classWriter.visitInnerClass("experiment/Annotations$B", "experiment/Annotations", "B", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$C", "experiment/Annotations", "C", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$A", "experiment/Annotations", "A", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    {
      methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      methodVisitor.visitCode();
      Label label0 = new Label();
      methodVisitor.visitLabel(label0);
      methodVisitor.visitLineNumber(5, label0);
      methodVisitor.visitVarInsn(ALOAD, 0);
      methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      methodVisitor.visitInsn(RETURN);
      methodVisitor.visitMaxs(1, 1);
      methodVisitor.visitEnd();
    }
    {
      methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "StaticMethod", "()I", null, null);
      methodVisitor.visitCode();
      Label label0 = new Label();
      methodVisitor.visitLabel(label0);
      methodVisitor.visitLineNumber(7, label0);
      methodVisitor.visitTypeInsn(NEW, "java/lang/Object");
      {
        annotationVisitor0 = methodVisitor.visitInsnAnnotation(0x44000000, null, "Lexperiment/Annotations$B;", false);
        annotationVisitor0.visitEnd();
      }
      methodVisitor.visitInsn(DUP);
      methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      methodVisitor.visitVarInsn(ASTORE, 0);
      Label label1 = new Label();
      methodVisitor.visitLabel(label1);
      methodVisitor.visitLineNumber(8, label1);
      methodVisitor.visitVarInsn(ALOAD, 0);
      {
        annotationVisitor0 = methodVisitor.visitInsnAnnotation(0x43000000, null, "Lexperiment/Annotations$C;", false);
        annotationVisitor0.visitEnd();
      }
      methodVisitor.visitTypeInsn(INSTANCEOF, "java/lang/Object");
      Label label2 = new Label();
      methodVisitor.visitJumpInsn(IFEQ, label2);
      Label label3 = new Label();
      methodVisitor.visitLabel(label3);
      methodVisitor.visitLineNumber(9, label3);
      methodVisitor.visitInsn(ICONST_1);
      methodVisitor.visitInsn(IRETURN);
      methodVisitor.visitLabel(label2);
      methodVisitor.visitLineNumber(11, label2);
      methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
      methodVisitor.visitInsn(ICONST_0);
      methodVisitor.visitInsn(IRETURN);
      Label label4 = new Label();
      methodVisitor.visitLabel(label4);
      {
        annotationVisitor0 = methodVisitor.visitLocalVariableAnnotation(0x40000000, null, new Label[]{label1}, new Label[]{label4}, new int[]{0}, "Lexperiment/Annotations$A;", false);
        annotationVisitor0.visitEnd();
      }
      methodVisitor.visitMaxs(2, 1);
      methodVisitor.visitEnd();
    }
    classWriter.visitEnd();

    return classWriter.toByteArray();
  }
}
