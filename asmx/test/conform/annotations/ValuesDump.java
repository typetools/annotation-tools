package annotations;
import org.objectweb.asm.*;
public class ValuesDump implements Opcodes {

public static byte[] dump () throws Exception {

ClassWriter cw = new ClassWriter(false);
FieldVisitor fv;
MethodVisitor mv;
AnnotationVisitor av0;

cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/Values", null, "java/lang/Object", null);

cw.visitSource("Values.java", null);

{
av0 = cw.visitAnnotation("Lannotations/ValuesAnnotation;", true);
av0.visit("byteValue", new Byte((byte)1));
av0.visit("charValue", new Character((char)65));
av0.visit("booleanValue", new Boolean(true));
av0.visit("intValue", new Integer(1));
av0.visit("shortValue", new Short((short)1));
av0.visit("longValue", new Long(1L));
av0.visit("floatValue", new Float("1.0"));
av0.visit("doubleValue", new Double("1.0"));
av0.visit("stringValue", "A");
av0.visitEnum("enumValue", "Lannotations/ValuesEnum;", "ONE");
{
AnnotationVisitor av1 = av0.visitAnnotation("annotationValue", "Lannotations/ValueAttrAnnotation;");
av1.visit("value", "annotation");
av1.visitEnd();
}
av0.visit("classValue", Type.getType("Lannotations/Values;"));
av0.visit("byteArrayValue", new byte[] {1,-1});
av0.visit("charArrayValue", new char[] {(char)99,(char)98,(char)65535});
av0.visit("booleanArrayValue", new boolean[] {true,false});
av0.visit("intArrayValue", new int[] {1,-1});
av0.visit("shortArrayValue", new short[] {(short)1,(short)-1});
av0.visit("longArrayValue", new long[] {1L,-1L});
av0.visit("floatArrayValue", new float[] {1.0f,-1.0f});
av0.visit("doubleArrayValue", new double[] {1.0d,-1.0d});
{
AnnotationVisitor av1 = av0.visitArray("stringArrayValue");
av1.visit(null, "aa");
av1.visit(null, "bb");
av1.visitEnd();
}
{
AnnotationVisitor av1 = av0.visitArray("enumArrayValue");
av1.visitEnum(null, "Lannotations/ValuesEnum;", "ONE");
av1.visitEnum(null, "Lannotations/ValuesEnum;", "TWO");
av1.visitEnd();
}
{
AnnotationVisitor av1 = av0.visitArray("annotationArrayValue");
{
AnnotationVisitor av2 = av1.visitAnnotation(null, "Lannotations/ValueAttrAnnotation;");
av2.visit("value", "annotation1");
av2.visitEnd();
}
{
AnnotationVisitor av2 = av1.visitAnnotation(null, "Lannotations/ValueAttrAnnotation;");
av2.visit("value", "annotation2");
av2.visitEnd();
}
av1.visitEnd();
}
{
AnnotationVisitor av1 = av0.visitArray("classArrayValue");
av1.visit(null, Type.getType("Lannotations/Values;"));
av1.visit(null, Type.getType("Lannotations/Values;"));
av1.visitEnd();
}
av0.visitEnd();
}
{
av0 = cw.visitAnnotation("Lannotations/ValueAttrAnnotation1;", true);
av0.visit("value", "classAnnotation1");
av0.visitEnd();
}
{
av0 = cw.visitAnnotation("Lannotations/ValueAttrAnnotation2;", true);
av0.visit("value", "classAnnotation2");
av0.visitEnd();
}
{
fv = cw.visitField(ACC_PUBLIC, "testfield", "Ljava/lang/String;", null, null);
{
av0 = fv.visitAnnotation("Lannotations/ValueAttrAnnotation1;", true);
av0.visit("value", "fieldAnnotation1");
av0.visitEnd();
}
{
av0 = fv.visitAnnotation("Lannotations/ValueAttrAnnotation2;", true);
av0.visit("value", "fieldAnnotation2");
av0.visitEnd();
}
fv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
mv.visitVarInsn(ALOAD, 0);
mv.visitLdcInsn("test");
mv.visitFieldInsn(PUTFIELD, "annotations/Values", "testfield", "Ljava/lang/String;");
mv.visitInsn(RETURN);
mv.visitMaxs(2, 1);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC, "testMethod", "(Ljava/lang/String;I)V", null, null);
{
av0 = mv.visitAnnotation("Lannotations/ValueAttrAnnotation1;", true);
av0.visit("value", "methodAnnotation1");
av0.visitEnd();
}
{
av0 = mv.visitAnnotation("Lannotations/ValueAttrAnnotation2;", true);
av0.visit("value", "methodAnnotation2");
av0.visitEnd();
}
{
av0 = mv.visitParameterAnnotation(0, "Lannotations/ValueAttrAnnotation1;", true);
av0.visit("value", "param1Annotation1");
av0.visitEnd();
}
{
av0 = mv.visitParameterAnnotation(0, "Lannotations/ValueAttrAnnotation2;", true);
av0.visit("value", "param1Annotation2");
av0.visitEnd();
}
{
av0 = mv.visitParameterAnnotation(1, "Lannotations/ValueAttrAnnotation1;", true);
av0.visit("value", "param2Annotation1");
av0.visitEnd();
}
{
av0 = mv.visitParameterAnnotation(1, "Lannotations/ValueAttrAnnotation2;", true);
av0.visit("value", "param2Annotation2");
av0.visitEnd();
}
mv.visitInsn(RETURN);
mv.visitMaxs(0, 3);
mv.visitEnd();
}
cw.visitEnd();

return cw.toByteArray();
}
}
