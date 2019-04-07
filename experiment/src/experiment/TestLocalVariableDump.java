package experiment;

import org.objectweb.asm.*;

public class TestLocalVariableDump implements Opcodes {

  public static byte[] dump() throws Exception {

    ClassWriter classWriter = new ClassWriter(0);
    FieldVisitor fieldVisitor;
    MethodVisitor methodVisitor;
    AnnotationVisitor annotationVisitor0;

    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "experiment/TestLocalVariable", null, "java/lang/Object", null);

    classWriter.visitSource("TestLocalVariable.java", null);

    classWriter.visitInnerClass("experiment/Annotations$B", "experiment/Annotations", "B", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$A", "experiment/Annotations", "A", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$C", "experiment/Annotations", "C", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$D", "experiment/Annotations", "D", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    {
      methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      methodVisitor.visitCode();
      Label label0 = new Label();
      methodVisitor.visitLabel(label0);
      methodVisitor.visitLineNumber(8, label0);
      methodVisitor.visitVarInsn(ALOAD, 0);
      methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      methodVisitor.visitInsn(RETURN);
      methodVisitor.visitMaxs(1, 1);
      methodVisitor.visitEnd();
    }
    {
      methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
      methodVisitor.visitCode();
      Label label0 = new Label();
      methodVisitor.visitLabel(label0);
      methodVisitor.visitLineNumber(10, label0);
      methodVisitor.visitTypeInsn(NEW, "java/lang/Object");
      {
        annotationVisitor0 = methodVisitor.visitInsnAnnotation(0x44000000, null, "Lexperiment/Annotations$B;", false);
        annotationVisitor0.visitEnd();
      }
      methodVisitor.visitInsn(DUP);
      methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      methodVisitor.visitVarInsn(ASTORE, 1);
      Label label1 = new Label();
      methodVisitor.visitLabel(label1);
      methodVisitor.visitLineNumber(11, label1);
      methodVisitor.visitIntInsn(BIPUSH, 6);
      methodVisitor.visitVarInsn(ISTORE, 2);
      Label label2 = new Label();
      methodVisitor.visitLabel(label2);
      methodVisitor.visitLineNumber(12, label2);
      methodVisitor.visitInsn(RETURN);
      Label label3 = new Label();
      methodVisitor.visitLabel(label3);
      {
        annotationVisitor0 = methodVisitor.visitLocalVariableAnnotation(0x40000000, null, new Label[]{label1}, new Label[]{label3}, new int[]{1}, "Lexperiment/Annotations$A;", false);
        annotationVisitor0.visitEnd();
      }
      {
        annotationVisitor0 = methodVisitor.visitLocalVariableAnnotation(0x40000000, null, new Label[]{label2}, new Label[]{label3}, new int[]{2}, "Lexperiment/Annotations$C;", false);
        annotationVisitor0.visitEnd();
      }
      {
        annotationVisitor0 = methodVisitor.visitLocalVariableAnnotation(0x40000000, null, new Label[]{label2}, new Label[]{label3}, new int[]{2}, "Lexperiment/Annotations$D;", false);
        annotationVisitor0.visitEnd();
      }
      methodVisitor.visitMaxs(2, 3);
      methodVisitor.visitEnd();
    }
    classWriter.visitEnd();

    return classWriter.toByteArray();
  }
}
