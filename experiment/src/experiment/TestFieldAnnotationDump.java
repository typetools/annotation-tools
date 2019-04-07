package experiment;

import org.objectweb.asm.*;

public class TestFieldAnnotationDump implements Opcodes {

  public static byte[] dump() throws Exception {

    ClassWriter classWriter = new ClassWriter(0);
    FieldVisitor fieldVisitor;
    MethodVisitor methodVisitor;
    AnnotationVisitor annotationVisitor0;

    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "experiment/TestFieldAnnotation", null, "java/lang/Object", null);

    classWriter.visitSource("TestFieldAnnotation.java", null);

    classWriter.visitInnerClass("experiment/Annotations$B", "experiment/Annotations", "B", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$A", "experiment/Annotations", "A", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$D", "experiment/Annotations", "D", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    classWriter.visitInnerClass("experiment/Annotations$C", "experiment/Annotations", "C", ACC_STATIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE);

    {
      fieldVisitor = classWriter.visitField(0, "a", "[Ljava/lang/Integer;", null, null);
      {
        annotationVisitor0 = fieldVisitor.visitTypeAnnotation(0x13000000, null, "Lexperiment/Annotations$B;", false);
        annotationVisitor0.visitEnd();
      }
      {
        annotationVisitor0 = fieldVisitor.visitTypeAnnotation(0x13000000, TypePath.fromString("["), "Lexperiment/Annotations$A;", false);
        annotationVisitor0.visitEnd();
      }
      fieldVisitor.visitEnd();
    }
    {
      methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      methodVisitor.visitCode();
      Label label0 = new Label();
      methodVisitor.visitLabel(label0);
      methodVisitor.visitLineNumber(8, label0);
      methodVisitor.visitVarInsn(ALOAD, 0);
      methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      Label label1 = new Label();
      methodVisitor.visitLabel(label1);
      methodVisitor.visitLineNumber(10, label1);
      methodVisitor.visitVarInsn(ALOAD, 0);
      methodVisitor.visitIntInsn(BIPUSH, 10);
      {
        annotationVisitor0 = methodVisitor.visitInsnAnnotation(0x44000000, null, "Lexperiment/Annotations$D;", false);
        annotationVisitor0.visitEnd();
      }
      {
        annotationVisitor0 = methodVisitor.visitInsnAnnotation(0x44000000, TypePath.fromString("["), "Lexperiment/Annotations$C;", false);
        annotationVisitor0.visitEnd();
      }
      methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Integer");
      methodVisitor.visitFieldInsn(PUTFIELD, "experiment/TestFieldAnnotation", "a", "[Ljava/lang/Integer;");
      methodVisitor.visitInsn(RETURN);
      methodVisitor.visitMaxs(2, 1);
      methodVisitor.visitEnd();
    }
    classWriter.visitEnd();

    return classWriter.toByteArray();
  }
}
