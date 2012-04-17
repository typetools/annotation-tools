package annotations;
import org.objectweb.asm.*;
public class ExtendedValuesDump implements Opcodes {

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

    public static byte[] dumpClassEmpty () throws Exception {
      
      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestClassEmpty", null, "java/lang/Object", null);

      cw.visitSource("TestClassEmpty.java", null);

      {
        av0 = cw.visitAnnotation("Lannotations/tests/classfile/foo/A;", true);
        av0.visitEnd();
      }
      {
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(3, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestClassEmpty;", null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
    }

    public static byte[] dumpClassNonEmpty () throws Exception {

      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestClassNonEmpty", null, "java/lang/Object", null);

      cw.visitSource("TestClassNonEmpty.java", null);

      {
      av0 = cw.visitAnnotation("Lannotations/tests/classfile/foo/A;", true);
      av0.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "i", "I", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PRIVATE, "a", "Ljava/lang/String;", null, null);
      fv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(7, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(8, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitInsn(ICONST_0);
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestClassNonEmpty", "i", "I");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(9, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestClassNonEmpty;", null, l0, l3, 0);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PROTECTED, "<init>", "(Ljava/lang/String;)V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(11, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(12, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestClassNonEmpty", "a", "Ljava/lang/String;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(13, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestClassNonEmpty;", null, l0, l3, 0);
      mv.visitLocalVariable("s", "Ljava/lang/String;", null, l0, l3, 1);
      mv.visitMaxs(2, 2);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "i", "()I", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(16, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestClassNonEmpty", "i", "I");
      mv.visitInsn(IRETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestClassNonEmpty;", null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "a", "()Ljava/lang/String;", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(20, l0);
      mv.visitTypeInsn(NEW, "java/lang/String");
      mv.visitInsn(DUP);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestClassNonEmpty", "a", "Ljava/lang/String;");
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "(Ljava/lang/String;)V");
      mv.visitVarInsn(ASTORE, 1);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(21, l1);
      mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
      mv.visitInsn(DUP);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
      mv.visitVarInsn(ALOAD, 1);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
      mv.visitVarInsn(ASTORE, 1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(22, l2);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitInsn(ARETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestClassNonEmpty;", null, l0, l3, 0);
      mv.visitLocalVariable("s", "Ljava/lang/String;", null, l1, l3, 1);
      mv.visitMaxs(3, 2);
      mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
      }
    public static byte[] dumpFieldSimple () throws Exception {

      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestFieldSimple", null, "java/lang/Object", null);

      cw.visitSource("TestFieldSimple.java", null);

      {
      av0 = cw.visitAnnotation("Lannotations/tests/classfile/foo/A;", true);
      av0.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "i", "I", null, null);
      {
      av0 = fv.visitAnnotation("Lannotations/tests/classfile/foo/A;", true);
      av0.visitEnd();
      }
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PRIVATE, "j", "I", null, null);
      {
      av0 = fv.visitAnnotation("Lannotations/tests/classfile/foo/B;", true);
      av0.visit("value", "Hello");
      av0.visitEnd();
      }
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PROTECTED, "o", "Ljava/lang/Object;", null, null);
      {
      av0 = fv.visitAnnotation("Lannotations/tests/classfile/foo/A;", true);
      av0.visitEnd();
      }
      {
      av0 = fv.visitAnnotation("Lannotations/tests/classfile/foo/B;", true);
      av0.visit("value", "H");
      av0.visitEnd();
      }
      fv.visitEnd();
      }
      {
      fv = cw.visitField(0, "s", "Ljava/lang/String;", null, null);
      {
      av0 = fv.visitAnnotation("Lannotations/tests/classfile/foo/B;", true);
      av0.visit("value", "E");
      av0.visitEnd();
      }
      fv.visitEnd();
      }
      {
      fv = cw.visitField(0, "f", "Lannotations/tests/classfile/cases/TestFieldSimple;", null, null);
      fv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(3, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(7, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitInsn(ACONST_NULL);
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestFieldSimple", "s", "Ljava/lang/String;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(8, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitInsn(ACONST_NULL);
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestFieldSimple", "f", "Lannotations/tests/classfile/cases/TestFieldSimple;");
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(3, l3);
      mv.visitInsn(RETURN);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestFieldSimple;", null, l0, l4, 0);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
      }
    
    public static byte[] dumpFieldGeneric () throws Exception {

      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestFieldGeneric", "<T:Ljava/lang/Object;>Ljava/lang/Object;", "java/lang/Object", null);

      cw.visitSource("TestFieldGeneric.java", null);

      {
      av0 = cw.visitAnnotation("Lannotations/tests/classfile/foo/A;", true);
      av0.visitEnd();
      }
      {
      fv = cw.visitField(0, "s", "Ljava/lang/String;", null, null);
      {
      av0 = fv.visitAnnotation("Lannotations/tests/classfile/foo/F;", true);
      av0.visit("fieldA", new Integer(1));
      av0.visit("fieldB", "fi");
      av0.visitEnd();
      }
      fv.visitEnd();
      }
      {
      fv = cw.visitField(0, "list", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/String;>;", null);
      {
      av0 = fv.visitAnnotation("Lannotations/tests/classfile/foo/G;", true);
      av0.visit("fieldA", new Integer(3));
      av0.visit("fieldB", "three");
      av0.visit("fieldC", new boolean[] {true,false});
      av0.visit("fieldD", new Integer(2));
      av0.visit("fieldE", new Integer(4));
      av0.visitEnd();
      }
      fv.visitEnd();
      }
      {
      fv = cw.visitField(0, "set", "Ljava/util/Set;", "Ljava/util/Set<Lannotations/tests/classfile/cases/TestFieldGeneric;>;", null);
      {
      av0 = fv.visitAnnotation("Lannotations/tests/classfile/foo/E;", true);
      av0.visit("fieldA", new Integer(2));
      av0.visit("fieldB", "rh");
      av0.visitEnd();
      }
      {
      av0 = fv.visitAnnotation("Lannotations/tests/classfile/foo/F;", true);
      av0.visit("fieldA", new Integer(1));
      av0.visit("fieldB", "if");
      av0.visitEnd();
      }
      fv.visitEnd();
      }
      {
      fv = cw.visitField(0, "testFieldGeneric", "Lannotations/tests/classfile/cases/TestFieldGeneric;", "Lannotations/tests/classfile/cases/TestFieldGeneric<TT;>;", null);
      {
      xav0 = fv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(11));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      fv.visitEnd();
      }
      {
      fv = cw.visitField(0, "otherSet", "Ljava/util/Set;", "Ljava/util/Set<Ljava/lang/String;>;", null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(0, "nestedSet", "Ljava/util/Set;", "Ljava/util/Set<Lannotations/tests/classfile/cases/TestFieldGeneric<Ljava/util/Set<Lannotations/tests/classfile/cases/TestFieldGeneric;>;>;>;", null);
      {
      av0 = fv.visitAnnotation("Lannotations/tests/classfile/foo/B;", true);
      av0.visit("value", "nested");
      av0.visitEnd();
      }
      {
      xav0 = fv.visitTypeAnnotation("Lannotations/tests/classfile/foo/F;", true);
      xav0.visit("fieldA", new Integer(1));
      xav0.visit("fieldB", "n");
      xav0.visitXTargetType(new Integer(11));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = fv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(11));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = fv.visitTypeAnnotation("Lannotations/tests/classfile/foo/C;", true);
      xav0.visit("fieldA", new Integer(-2));
      xav0.visit("fieldB", "nl");
      xav0.visitXTargetType(new Integer(11));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = fv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "nil");
      xav0.visitXTargetType(new Integer(11));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = fv.visitTypeAnnotation("Lannotations/tests/classfile/foo/D;", true);
      xav0.visit("fieldA", new Integer(-1));
      xav0.visit("fieldB", "hello");
      xav0.visit("fieldC", new int[] {3,2,4});
      xav0.visitXTargetType(new Integer(11));
      xav0.visitXLocationLength(new Integer(3));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      fv.visitEnd();
      }
      {
      fv = cw.visitField(0, "nestedMap", "Ljava/util/Map;", "Ljava/util/Map<Ljava/util/Set<Lannotations/tests/classfile/cases/TestFieldGeneric;>;Lannotations/tests/classfile/cases/TestFieldGeneric<TT;>;>;", null);
      {
      av0 = fv.visitAnnotation("Lannotations/tests/classfile/foo/C;", true);
      av0.visit("fieldA", new Integer(1));
      av0.visit("fieldB", "nested");
      av0.visitEnd();
      }
      {
      xav0 = fv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(11));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = fv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "inner most T");
      xav0.visitXTargetType(new Integer(11));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = fv.visitTypeAnnotation("Lannotations/tests/classfile/foo/C;", true);
      xav0.visit("fieldA", new Integer(256));
      xav0.visit("fieldB", "hello");
      xav0.visitXTargetType(new Integer(11));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = fv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "inner most F");
      xav0.visitXTargetType(new Integer(11));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      fv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(13, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(11, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "annotations/tests/classfile/cases/TestFieldGeneric");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "annotations/tests/classfile/cases/TestFieldGeneric", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestFieldGeneric", "testFieldGeneric", "Lannotations/tests/classfile/cases/TestFieldGeneric;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(15, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestFieldGeneric;", "Lannotations/tests/classfile/cases/TestFieldGeneric<TT;>;", l0, l3, 0);
      mv.visitMaxs(3, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(20, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestFieldGeneric", "s", "Ljava/lang/String;");
      mv.visitInsn(ARETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestFieldGeneric;", "Lannotations/tests/classfile/cases/TestFieldGeneric<TT;>;", l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
      }
    public static byte[] dumpLocalVariable () throws Exception {

      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestLocalVariable", "<T:Ljava/lang/Object;>Ljava/lang/Object;", "java/lang/Object", null);

      cw.visitSource("TestLocalVariable.java", null);

      {
      av0 = cw.visitAnnotation("Lannotations/tests/classfile/foo/A;", true);
      av0.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "i", "I", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "s", "Ljava/util/Set;", "Ljava/util/Set<Ljava/util/Set;>;", null);
      fv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/C;", true);
      xav0.visit("fieldA", new Integer(166));
      xav0.visit("fieldB", "good");
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(6));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(10, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(11, l1);
      mv.visitInsn(ICONST_0);
      mv.visitVarInsn(ISTORE, 1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(12, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitInsn(ICONST_0);
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestLocalVariable", "i", "I");
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(13, l3);
      mv.visitInsn(RETURN);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestLocalVariable;", "Lannotations/tests/classfile/cases/TestLocalVariable<TT;>;", l0, l4, 0);
      mv.visitLocalVariable("t", "I", null, l2, l4, 1);
      mv.visitMaxs(2, 2);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(I)V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(0));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(15, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(16, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ILOAD, 1);
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestLocalVariable", "i", "I");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(17, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestLocalVariable;", "Lannotations/tests/classfile/cases/TestLocalVariable<TT;>;", l0, l3, 0);
      mv.visitLocalVariable("i", "I", null, l0, l3, 1);
      mv.visitMaxs(2, 2);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Integer;)V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(19, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(20, l1);
      mv.visitInsn(ICONST_1);
      mv.visitVarInsn(ISTORE, 2);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(21, l2);
      mv.visitIincInsn(2, 1);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(22, l3);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestLocalVariable", "i", "I");
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLineNumber(23, l4);
      mv.visitIincInsn(2, -1);
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(24, l5);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ILOAD, 2);
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestLocalVariable", "i", "I");
      Label l6 = new Label();
      mv.visitLabel(l6);
      mv.visitLineNumber(25, l6);
      mv.visitInsn(RETURN);
      Label l7 = new Label();
      mv.visitLabel(l7);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestLocalVariable;", "Lannotations/tests/classfile/cases/TestLocalVariable<TT;>;", l0, l7, 0);
      mv.visitLocalVariable("j", "Ljava/lang/Integer;", null, l0, l7, 1);
      mv.visitLocalVariable("k", "I", null, l2, l7, 2);
      mv.visitMaxs(2, 3);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "i", "()I", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(28, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestLocalVariable", "i", "I");
      mv.visitInsn(IRETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestLocalVariable;", "Lannotations/tests/classfile/cases/TestLocalVariable<TT;>;", l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "j", "()I", null, null);
      {
      av0 = mv.visitAnnotation("Lannotations/tests/classfile/foo/A;", true);
      av0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "hello");
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(2));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(32, l0);
      mv.visitInsn(ICONST_1);
      mv.visitVarInsn(ISTORE, 1);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(33, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKEVIRTUAL, "annotations/tests/classfile/cases/TestLocalVariable", "j", "()I");
      mv.visitInsn(IRETURN);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestLocalVariable;", "Lannotations/tests/classfile/cases/TestLocalVariable<TT;>;", l0, l2, 0);
      mv.visitLocalVariable("temp", "I", null, l1, l2, 1);
      mv.visitMaxs(1, 2);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "someMethod", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(37, l0);
      mv.visitTypeInsn(NEW, "annotations/tests/classfile/cases/TestLocalVariable");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "annotations/tests/classfile/cases/TestLocalVariable", "<init>", "()V");
      mv.visitVarInsn(ASTORE, 0);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(38, l1);
      mv.visitTypeInsn(NEW, "java/lang/String");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "()V");
      mv.visitVarInsn(ASTORE, 1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(39, l2);
      mv.visitLdcInsn(new Double("2.0"));
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
      mv.visitVarInsn(ASTORE, 2);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(40, l3);
      mv.visitInsn(RETURN);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLocalVariable("t", "Lannotations/tests/classfile/cases/TestLocalVariable;", null, l1, l4, 0);
      mv.visitLocalVariable("s", "Ljava/lang/String;", null, l2, l4, 1);
      mv.visitLocalVariable("d", "Ljava/lang/Double;", null, l3, l4, 2);
      mv.visitMaxs(2, 3);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(43, l0);
      mv.visitInsn(ICONST_1);
      mv.visitVarInsn(ISTORE, 1);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(44, l1);
      mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
      mv.visitVarInsn(ISTORE, 2);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(45, l2);
      mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
      mv.visitVarInsn(ISTORE, 3);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(46, l3);
      mv.visitVarInsn(ILOAD, 2);
      Label l4 = new Label();
      mv.visitJumpInsn(IFEQ, l4);
      mv.visitVarInsn(ILOAD, 3);
      mv.visitJumpInsn(IFEQ, l4);
      mv.visitInsn(ICONST_1);
      Label l5 = new Label();
      mv.visitJumpInsn(GOTO, l5);
      mv.visitLabel(l4);
      mv.visitInsn(ICONST_0);
      mv.visitLabel(l5);
      mv.visitVarInsn(ISTORE, 1);
      Label l6 = new Label();
      mv.visitLabel(l6);
      mv.visitLineNumber(47, l6);
      mv.visitVarInsn(ILOAD, 1);
      Label l7 = new Label();
      mv.visitJumpInsn(IFNE, l7);
      mv.visitVarInsn(ILOAD, 3);
      Label l8 = new Label();
      mv.visitJumpInsn(IFEQ, l8);
      mv.visitLabel(l7);
      mv.visitLineNumber(48, l7);
      mv.visitVarInsn(ILOAD, 1);
      mv.visitVarInsn(ISTORE, 2);
      mv.visitLabel(l8);
      mv.visitLineNumber(50, l8);
      mv.visitVarInsn(ILOAD, 2);
      Label l9 = new Label();
      mv.visitJumpInsn(IFEQ, l9);
      Label l10 = new Label();
      mv.visitLabel(l10);
      mv.visitLineNumber(51, l10);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitLdcInsn("Message");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
      mv.visitLabel(l9);
      mv.visitLineNumber(53, l9);
      mv.visitInsn(RETURN);
      Label l11 = new Label();
      mv.visitLabel(l11);
      mv.visitLocalVariable("args", "[Ljava/lang/String;", null, l0, l11, 0);
      mv.visitLocalVariable("b", "Z", null, l1, l11, 1);
      mv.visitLocalVariable("b1", "Z", null, l2, l11, 2);
      mv.visitLocalVariable("b2", "Z", null, l3, l11, 3);
      mv.visitMaxs(2, 4);
      mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
      }
    
    public static byte[] dumpLocalVariableGenericArray () throws Exception {

      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestLocalVariableGenericArray", null, "java/lang/Object", null);

      cw.visitSource("TestLocalVariableGenericArray.java", null);

      {
      fv = cw.visitField(0, "i", "Ljava/lang/Integer;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(0, "map1", "Ljava/util/Map;", "Ljava/util/Map<Ljava/lang/String;Ljava/util/Set<Ljava/lang/String;>;>;", null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(0, "map2", "Ljava/util/Map;", "Ljava/util/Map<Ljava/lang/String;Ljava/util/ArrayList<Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;>;>;", null);
      fv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "good");
      xav0.visitXTargetType(new Integer(8));
      xav0.visitXStartPc(new Integer(37));
      xav0.visitXLength(new Integer(55));
      xav0.visitXIndex(new Integer(2));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "first param");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(37));
      xav0.visitXLength(new Integer(55));
      xav0.visitXIndex(new Integer(2));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "second param");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(37));
      xav0.visitXLength(new Integer(55));
      xav0.visitXIndex(new Integer(2));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(17, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(18, l1);
      mv.visitInsn(ICONST_1);
      mv.visitVarInsn(ISTORE, 1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(19, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestLocalVariableGenericArray", "map2", "Ljava/util/Map;");
      mv.visitLdcInsn("4gf");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/ArrayList");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "iterator", "()Ljava/util/Iterator;");
      mv.visitVarInsn(ASTORE, 3);
      Label l3 = new Label();
      mv.visitJumpInsn(GOTO, l3);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitVarInsn(ALOAD, 3);
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Map");
      mv.visitVarInsn(ASTORE, 2);
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(20, l5);
      mv.visitVarInsn(ILOAD, 1);
      mv.visitInsn(ICONST_5);
      Label l6 = new Label();
      mv.visitJumpInsn(IF_ICMPGE, l6);
      Label l7 = new Label();
      mv.visitLabel(l7);
      mv.visitLineNumber(21, l7);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestLocalVariableGenericArray", "map2", "Ljava/util/Map;");
      mv.visitLdcInsn("");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/ArrayList");
      mv.visitTypeInsn(NEW, "java/util/ArrayList");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "indexOf", "(Ljava/lang/Object;)I");
      mv.visitVarInsn(ISTORE, 1);
      Label l8 = new Label();
      mv.visitJumpInsn(GOTO, l8);
      mv.visitLabel(l6);
      mv.visitLineNumber(23, l6);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestLocalVariableGenericArray", "i", "Ljava/lang/Integer;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
      mv.visitInsn(ICONST_5);
      mv.visitInsn(IADD);
      mv.visitVarInsn(ISTORE, 1);
      mv.visitLabel(l8);
      mv.visitLineNumber(25, l8);
      mv.visitIincInsn(1, 1);
      mv.visitLabel(l3);
      mv.visitLineNumber(19, l3);
      mv.visitVarInsn(ALOAD, 3);
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
      mv.visitJumpInsn(IFNE, l4);
      Label l9 = new Label();
      mv.visitLabel(l9);
      mv.visitLineNumber(27, l9);
      mv.visitInsn(RETURN);
      Label l10 = new Label();
      mv.visitLabel(l10);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestLocalVariableGenericArray;", null, l0, l10, 0);
      mv.visitLocalVariable("k", "I", null, l2, l10, 1);
      mv.visitLocalVariable("e", "Ljava/util/Map;", "Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;", l5, l9, 2);
      mv.visitMaxs(3, 4);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "someMethod", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "bad");
      xav0.visitXTargetType(new Integer(8));
      xav0.visitXStartPc(new Integer(8));
      xav0.visitXLength(new Integer(26));
      xav0.visitXIndex(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/C;", true);
      xav0.visit("fieldA", new Integer(0));
      xav0.visit("fieldB", "String");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(8));
      xav0.visitXLength(new Integer(26));
      xav0.visitXIndex(new Integer(1));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(30, l0);
      mv.visitTypeInsn(NEW, "java/util/HashSet");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V");
      mv.visitVarInsn(ASTORE, 1);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(31, l1);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitTypeInsn(NEW, "java/lang/String");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "()V");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z");
      mv.visitInsn(POP);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(32, l2);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z");
      mv.visitInsn(POP);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(33, l3);
      mv.visitInsn(RETURN);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestLocalVariableGenericArray;", null, l0, l4, 0);
      mv.visitLocalVariable("s", "Ljava/util/Set;", "Ljava/util/Set<Ljava/lang/String;>;", l1, l4, 1);
      mv.visitMaxs(3, 2);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PRIVATE, "someMethod2", "(I)I", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/C;", true);
      xav0.visit("fieldA", new Integer(0));
      xav0.visit("fieldB", "Boolean");
      xav0.visitXTargetType(new Integer(8));
      xav0.visitXStartPc(new Integer(8));
      xav0.visitXLength(new Integer(66));
      xav0.visitXIndex(new Integer(2));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(8));
      xav0.visitXLength(new Integer(66));
      xav0.visitXIndex(new Integer(2));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(8));
      xav0.visitXStartPc(new Integer(16));
      xav0.visitXLength(new Integer(58));
      xav0.visitXIndex(new Integer(3));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "inner-type");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(16));
      xav0.visitXLength(new Integer(58));
      xav0.visitXIndex(new Integer(3));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(36, l0);
      mv.visitTypeInsn(NEW, "java/util/HashSet");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V");
      mv.visitVarInsn(ASTORE, 2);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(37, l1);
      mv.visitTypeInsn(NEW, "java/util/HashSet");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V");
      mv.visitVarInsn(ASTORE, 3);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(38, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "annotations/tests/classfile/cases/TestLocalVariableGenericArray", "someMethod3", "()Z");
      mv.visitVarInsn(ISTORE, 4);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(39, l3);
      mv.visitVarInsn(ALOAD, 2);
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
      mv.visitVarInsn(ILOAD, 4);
      mv.visitInsn(IAND);
      Label l4 = new Label();
      mv.visitJumpInsn(IFEQ, l4);
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(40, l5);
      mv.visitVarInsn(ILOAD, 4);
      Label l6 = new Label();
      mv.visitJumpInsn(IFEQ, l6);
      mv.visitVarInsn(ILOAD, 1);
      Label l7 = new Label();
      mv.visitJumpInsn(GOTO, l7);
      mv.visitLabel(l6);
      mv.visitVarInsn(ALOAD, 3);
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
      mv.visitLabel(l7);
      mv.visitInsn(IRETURN);
      mv.visitLabel(l4);
      mv.visitLineNumber(42, l4);
      mv.visitVarInsn(ILOAD, 1);
      mv.visitInsn(IRETURN);
      Label l8 = new Label();
      mv.visitLabel(l8);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestLocalVariableGenericArray;", null, l0, l8, 0);
      mv.visitLocalVariable("i", "I", null, l0, l8, 1);
      mv.visitLocalVariable("s", "Ljava/util/Set;", "Ljava/util/Set<Ljava/lang/Boolean;>;", l1, l8, 2);
      mv.visitLocalVariable("ints", "Ljava/util/Set;", "Ljava/util/Set<Ljava/lang/Integer;>;", l2, l8, 3);
      mv.visitLocalVariable("b", "Z", null, l3, l8, 4);
      mv.visitMaxs(2, 5);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PRIVATE, "someMethod3", "()Z", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "t");
      xav0.visitXTargetType(new Integer(8));
      xav0.visitXStartPc(new Integer(8));
      xav0.visitXLength(new Integer(70));
      xav0.visitXIndex(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "map key string");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(8));
      xav0.visitXLength(new Integer(70));
      xav0.visitXIndex(new Integer(1));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "map value set");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(8));
      xav0.visitXLength(new Integer(70));
      xav0.visitXIndex(new Integer(1));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(8));
      xav0.visitXLength(new Integer(70));
      xav0.visitXIndex(new Integer(1));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(10));
      xav0.visitXLength(new Integer(68));
      xav0.visitXIndex(new Integer(2));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/C;", true);
      xav0.visit("fieldA", new Integer(1));
      xav0.visit("fieldB", "set of maps");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(10));
      xav0.visitXLength(new Integer(68));
      xav0.visitXIndex(new Integer(2));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "maps");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(10));
      xav0.visitXLength(new Integer(68));
      xav0.visitXIndex(new Integer(2));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "map key is integer");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(10));
      xav0.visitXLength(new Integer(68));
      xav0.visitXIndex(new Integer(2));
      xav0.visitXLocationLength(new Integer(3));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "map value is 2-d array");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(10));
      xav0.visitXLength(new Integer(68));
      xav0.visitXIndex(new Integer(2));
      xav0.visitXLocationLength(new Integer(3));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "first dimension");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(10));
      xav0.visitXLength(new Integer(68));
      xav0.visitXIndex(new Integer(2));
      xav0.visitXLocationLength(new Integer(4));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "second dimension");
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(10));
      xav0.visitXLength(new Integer(68));
      xav0.visitXIndex(new Integer(2));
      xav0.visitXLocationLength(new Integer(4));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(46, l0);
      mv.visitTypeInsn(NEW, "java/util/HashMap");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
      mv.visitVarInsn(ASTORE, 1);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(47, l1);
      mv.visitInsn(ACONST_NULL);
      mv.visitVarInsn(ASTORE, 2);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(49, l2);
      mv.visitVarInsn(ALOAD, 2);
      mv.visitLdcInsn("3");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Set");
      mv.visitTypeInsn(NEW, "java/util/HashMap");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z");
      mv.visitInsn(POP);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(51, l3);
      mv.visitVarInsn(ALOAD, 2);
      mv.visitLdcInsn("4");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Set");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Map");
      mv.visitInsn(ICONST_3);
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "[[Ljava/lang/String;");
      mv.visitInsn(ICONST_2);
      mv.visitInsn(AALOAD);
      mv.visitInsn(ICONST_4);
      mv.visitLdcInsn("Hello");
      mv.visitInsn(AASTORE);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLineNumber(53, l4);
      mv.visitInsn(ICONST_1);
      mv.visitInsn(IRETURN);
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestLocalVariableGenericArray;", null, l0, l5, 0);
      mv.visitLocalVariable("t", "Ljava/util/Map;", "Ljava/util/Map<Ljava/lang/String;Ljava/util/Set<Ljava/lang/String;>;>;", l1, l5, 1);
      mv.visitLocalVariable("s", "Ljava/util/Map;", "Ljava/util/Map<Ljava/lang/String;Ljava/util/Set<Ljava/util/Map<Ljava/lang/Integer;[[Ljava/lang/String;>;>;>;", l2, l5, 2);
      mv.visitMaxs(3, 3);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PROTECTED, "someMethod4", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(9));
      xav0.visitXStartPc(new Integer(8));
      xav0.visitXLength(new Integer(10));
      xav0.visitXIndex(new Integer(1));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      Label l1 = new Label();
      mv.visitTryCatchBlock(l0, l1, l1, "java/lang/Exception");
      mv.visitLabel(l0);
      mv.visitLineNumber(58, l0);
      mv.visitTypeInsn(NEW, "java/util/HashSet");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V");
      mv.visitVarInsn(ASTORE, 1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(60, l2);
      mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
      mv.visitInsn(DUP);
      mv.visitLdcInsn("Hello");
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
      mv.visitInsn(ATHROW);
      mv.visitLabel(l1);
      mv.visitLineNumber(61, l1);
      mv.visitVarInsn(ASTORE, 1);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(62, l3);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestLocalVariableGenericArray", "i", "Ljava/lang/Integer;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLineNumber(64, l4);
      mv.visitInsn(RETURN);
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestLocalVariableGenericArray;", null, l0, l5, 0);
      mv.visitLocalVariable("s", "Ljava/util/Set;", "Ljava/util/Set<Ljava/lang/String;>;", l2, l1, 1);
      mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, l3, l4, 1);
      mv.visitMaxs(3, 2);
      mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
      }
    public static byte[] dumpMethodReceiver () throws Exception {

      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestMethodReceiver", null, "java/lang/Object", null);

      cw.visitSource("TestMethodReceiver.java", null);

      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(9, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitInsn(RETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReceiver;", null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(6));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "first method");
      xav0.visitXTargetType(new Integer(6));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(12, l0);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitLdcInsn("test()");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(13, l1);
      mv.visitInsn(RETURN);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReceiver;", null, l0, l2, 0);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PRIVATE, "test2", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/C;", true);
      xav0.visit("fieldA", new Integer(2));
      xav0.visit("fieldB", "rec");
      xav0.visitXTargetType(new Integer(6));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(16, l0);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitLdcInsn("test2()");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(17, l1);
      mv.visitInsn(RETURN);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReceiver;", null, l0, l2, 0);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PROTECTED, "test3", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(6));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(20, l0);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitLdcInsn("test3()");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(21, l1);
      mv.visitInsn(RETURN);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReceiver;", null, l0, l2, 0);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(0, "test4", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "last method");
      xav0.visitXTargetType(new Integer(6));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(24, l0);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitLdcInsn("test4()");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(25, l1);
      mv.visitInsn(RETURN);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReceiver;", null, l0, l2, 0);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
      }

    public static byte[] dumpMethodReturnTypeGenericArray () throws Exception {

    ClassWriter cw = new ClassWriter(false);
    FieldVisitor fv;
    MethodVisitor mv;
    AnnotationVisitor av0;
    TypeAnnotationVisitor xav0;

    cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestMethodReturnTypeGenericArray", null, "java/lang/Object", null);

    cw.visitSource("TestMethodReturnTypeGenericArray.java", null);

    {
    mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(7, l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    mv.visitInsn(RETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReturnTypeGenericArray;", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
    }
    {
    mv = cw.visitMethod(ACC_PUBLIC, "test", "()Ljava/util/List;", null, null);
    {
    av0 = mv.visitAnnotation("Lannotations/tests/classfile/foo/A;", true);
    av0.visitEnd();
    }
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(10, l0);
    mv.visitInsn(ACONST_NULL);
    mv.visitInsn(ARETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReturnTypeGenericArray;", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
    }
    {
    mv = cw.visitMethod(ACC_PUBLIC, "test2", "()Ljava/util/List;", "()Ljava/util/List<Ljava/lang/String;>;", null);
    {
    av0 = mv.visitAnnotation("Lannotations/tests/classfile/foo/B;", true);
    av0.visit("value", "single-depth");
    av0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(1));
    xav0.visitXLocation(new Integer(0));
    xav0.visitEnd();
    }
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(14, l0);
    mv.visitInsn(ACONST_NULL);
    mv.visitInsn(ARETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReturnTypeGenericArray;", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
    }
    {
    mv = cw.visitMethod(ACC_PUBLIC, "test3", "()[Ljava/lang/String;", null, null);
    {
    av0 = mv.visitAnnotation("Lannotations/tests/classfile/foo/A;", true);
    av0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
    xav0.visit("value", "on array element");
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(1));
    xav0.visitXLocation(new Integer(0));
    xav0.visitEnd();
    }
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(18, l0);
    mv.visitInsn(ACONST_NULL);
    mv.visitInsn(ARETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReturnTypeGenericArray;", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
    }
    {
    mv = cw.visitMethod(ACC_PUBLIC, "test4", "()[[Ljava/lang/String;", null, null);
    {
    av0 = mv.visitAnnotation("Lannotations/tests/classfile/foo/A;", true);
    av0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
    xav0.visit("value", "on");
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(1));
    xav0.visitXLocation(new Integer(0));
    xav0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
    xav0.visit("value", "in");
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(1));
    xav0.visitXLocation(new Integer(1));
    xav0.visitEnd();
    }
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(22, l0);
    mv.visitInsn(ACONST_NULL);
    mv.visitInsn(ARETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReturnTypeGenericArray;", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
    }
    {
    mv = cw.visitMethod(ACC_PUBLIC, "test5", "()Ljava/util/Set;", "()Ljava/util/Set<[Ljava/lang/String;>;", null);
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(1));
    xav0.visitXLocation(new Integer(0));
    xav0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
    xav0.visit("value", "two-deep");
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(2));
    xav0.visitXLocation(new Integer(0));
    xav0.visitXLocation(new Integer(0));
    xav0.visitEnd();
    }
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(26, l0);
    mv.visitInsn(ACONST_NULL);
    mv.visitInsn(ARETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReturnTypeGenericArray;", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
    }
    {
    mv = cw.visitMethod(ACC_PUBLIC, "test6", "()Ljava/util/Map;", "()Ljava/util/Map<Ljava/util/Map<[Ljava/lang/String;Ljava/util/Set<Ljava/lang/String;>;>;Ljava/util/Set<[Ljava/lang/String;>;>;", null);
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
    xav0.visit("value", "map as key");
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(1));
    xav0.visitXLocation(new Integer(0));
    xav0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
    xav0.visit("value", "array of value");
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(2));
    xav0.visitXLocation(new Integer(1));
    xav0.visitXLocation(new Integer(0));
    xav0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
    xav0.visit("value", "inner-most value");
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(3));
    xav0.visitXLocation(new Integer(1));
    xav0.visitXLocation(new Integer(0));
    xav0.visitXLocation(new Integer(0));
    xav0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
    xav0.visit("value", "set as value");
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(1));
    xav0.visitXLocation(new Integer(1));
    xav0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
    xav0.visit("value", "innermost key or key");
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(3));
    xav0.visitXLocation(new Integer(0));
    xav0.visitXLocation(new Integer(0));
    xav0.visitXLocation(new Integer(0));
    xav0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(2));
    xav0.visitXLocation(new Integer(0));
    xav0.visitXLocation(new Integer(0));
    xav0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/C;", true);
    xav0.visit("fieldA", new Integer(1));
    xav0.visit("fieldB", "value of key");
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(2));
    xav0.visitXLocation(new Integer(0));
    xav0.visitXLocation(new Integer(1));
    xav0.visitEnd();
    }
    {
    xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
    xav0.visitXTargetType(new Integer(11));
    xav0.visitXLocationLength(new Integer(3));
    xav0.visitXLocation(new Integer(0));
    xav0.visitXLocation(new Integer(1));
    xav0.visitXLocation(new Integer(0));
    xav0.visitEnd();
    }
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(30, l0);
    mv.visitInsn(ACONST_NULL);
    mv.visitInsn(ARETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestMethodReturnTypeGenericArray;", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
    }
    cw.visitEnd();

    return cw.toByteArray();
    }
    
    public static byte[] dumpObjectCreation () throws Exception {

      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestObjectCreation", null, "java/lang/Object", null);

      cw.visitSource("TestObjectCreation.java", null);

      {
      fv = cw.visitField(ACC_PUBLIC, "o", "Ljava/lang/Object;", null, null);
      fv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(7, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitInsn(RETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestObjectCreation;", null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "first new");
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "a string");
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(12));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(23));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(11, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/lang/Object");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreation", "o", "Ljava/lang/Object;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(12, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/lang/String");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreation", "o", "Ljava/lang/Object;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(13, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/lang/String");
      mv.visitInsn(DUP);
      mv.visitLdcInsn("");
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "(Ljava/lang/String;)V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreation", "o", "Ljava/lang/Object;");
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(14, l3);
      mv.visitInsn(RETURN);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestObjectCreation;", null, l0, l4, 0);
      mv.visitMaxs(4, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test2", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(7));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(14));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(17, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitLdcInsn("str");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreation", "o", "Ljava/lang/Object;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(18, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/util/ArrayList");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreation", "o", "Ljava/lang/Object;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(19, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestObjectCreation;", null, l0, l3, 0);
      mv.visitMaxs(3, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test3", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "new");
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(12));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(22, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/util/HashSet");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreation", "o", "Ljava/lang/Object;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(23, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/util/HashMap");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreation", "o", "Ljava/lang/Object;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(24, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestObjectCreation;", null, l0, l3, 0);
      mv.visitMaxs(3, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test4", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "self test");
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(13));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(27, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/lang/Integer");
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_2);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreation", "o", "Ljava/lang/Object;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(28, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "annotations/tests/classfile/cases/TestObjectCreation");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "annotations/tests/classfile/cases/TestObjectCreation", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreation", "o", "Ljava/lang/Object;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(29, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestObjectCreation;", null, l0, l3, 0);
      mv.visitMaxs(4, 1);
      mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
      }
    
    public static byte[] dumpObjectCreationGenericArray () throws Exception {

      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestObjectCreationGenericArray", null, "java/lang/Object", null);

      cw.visitSource("TestObjectCreationGenericArray.java", null);

      {
      fv = cw.visitField(ACC_PUBLIC, "o", "Ljava/lang/Object;", null, null);
      fv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(9, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitInsn(RETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestObjectCreationGenericArray;", null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "first new");
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(3));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(3));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(13, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitIntInsn(BIPUSH, 10);
      mv.visitIntInsn(NEWARRAY, T_INT);
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreationGenericArray", "o", "Ljava/lang/Object;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(14, l1);
      mv.visitInsn(RETURN);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestObjectCreationGenericArray;", null, l0, l2, 0);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test2", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(23));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "str");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(23));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(17, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitLdcInsn("str");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreationGenericArray", "o", "Ljava/lang/Object;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(18, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/util/ArrayList");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreationGenericArray", "o", "Ljava/lang/Object;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(19, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestObjectCreationGenericArray;", null, l0, l3, 0);
      mv.visitMaxs(3, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test3", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "new");
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "map");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(1));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(1));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "map key string");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(1));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(12));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "first level");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(12));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "value");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(12));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "on the array");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(12));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "on array elements");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(12));
      xav0.visitXLocationLength(new Integer(3));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(22, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/util/HashSet");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreationGenericArray", "o", "Ljava/lang/Object;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(23, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/util/HashMap");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreationGenericArray", "o", "Ljava/lang/Object;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(24, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestObjectCreationGenericArray;", null, l0, l3, 0);
      mv.visitMaxs(3, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test4", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(4));
      xav0.visitXOffset(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "key");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(1));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "value");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(1));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "key element");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(1));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "value array");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(1));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "value array element");
      xav0.visitXTargetType(new Integer(5));
      xav0.visitXOffset(new Integer(1));
      xav0.visitXLocationLength(new Integer(3));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(27, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/util/HashMap");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestObjectCreationGenericArray", "o", "Ljava/lang/Object;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(28, l1);
      mv.visitInsn(RETURN);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestObjectCreationGenericArray;", null, l0, l2, 0);
      mv.visitMaxs(3, 1);
      mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
      }
    
    public static byte[] dumpTypecast () throws Exception {

      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestTypecast", null, "java/lang/Object", null);

      cw.visitSource("TestTypecast.java", null);

      {
      fv = cw.visitField(ACC_PUBLIC, "o", "Ljava/lang/Object;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "s", "Ljava/lang/String;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "i", "Ljava/lang/Integer;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "b", "Ljava/lang/Boolean;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "set", "Ljava/util/Set;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "hset", "Ljava/util/HashSet;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "map", "Ljava/util/Map;", null, null);
      fv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(7, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitInsn(RETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypecast;", null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(0));
      xav0.visitXOffset(new Integer(21));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "second cast");
      xav0.visitXTargetType(new Integer(0));
      xav0.visitXOffset(new Integer(32));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/C;", true);
      xav0.visit("fieldA", new Integer(3));
      xav0.visit("fieldB", "cast");
      xav0.visitXTargetType(new Integer(0));
      xav0.visitXOffset(new Integer(59));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/C;", true);
      xav0.visit("fieldA", new Integer(4));
      xav0.visit("fieldB", "cast");
      xav0.visitXTargetType(new Integer(0));
      xav0.visitXOffset(new Integer(70));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(17, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecast", "o", "Ljava/lang/Object;");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecast", "o", "Ljava/lang/Object;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(18, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecast", "s", "Ljava/lang/String;");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecast", "o", "Ljava/lang/Object;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(19, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecast", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/lang/String");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecast", "s", "Ljava/lang/String;");
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(20, l3);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecast", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecast", "i", "Ljava/lang/Integer;");
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLineNumber(21, l4);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecast", "b", "Ljava/lang/Boolean;");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecast", "b", "Ljava/lang/Boolean;");
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(22, l5);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecast", "hset", "Ljava/util/HashSet;");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecast", "set", "Ljava/util/Set;");
      Label l6 = new Label();
      mv.visitLabel(l6);
      mv.visitLineNumber(23, l6);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecast", "set", "Ljava/util/Set;");
      mv.visitTypeInsn(CHECKCAST, "java/util/HashSet");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecast", "hset", "Ljava/util/HashSet;");
      Label l7 = new Label();
      mv.visitLabel(l7);
      mv.visitLineNumber(24, l7);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecast", "hset", "Ljava/util/HashSet;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Map");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecast", "map", "Ljava/util/Map;");
      Label l8 = new Label();
      mv.visitLabel(l8);
      mv.visitLineNumber(25, l8);
      mv.visitInsn(ICONST_0);
      mv.visitVarInsn(ISTORE, 1);
      Label l9 = new Label();
      mv.visitLabel(l9);
      mv.visitLineNumber(26, l9);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ILOAD, 1);
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecast", "i", "Ljava/lang/Integer;");
      Label l10 = new Label();
      mv.visitLabel(l10);
      mv.visitLineNumber(27, l10);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ILOAD, 1);
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecast", "o", "Ljava/lang/Object;");
      Label l11 = new Label();
      mv.visitLabel(l11);
      mv.visitLineNumber(28, l11);
      mv.visitInsn(RETURN);
      Label l12 = new Label();
      mv.visitLabel(l12);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypecast;", null, l0, l12, 0);
      mv.visitLocalVariable("pi", "I", null, l9, l12, 1);
      mv.visitMaxs(2, 2);
      mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
      }
    public static byte[] dumpTypecastGenericArray () throws Exception {

      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestTypecastGenericArray", null, "java/lang/Object", null);

      cw.visitSource("TestTypecastGenericArray.java", null);

      {
      fv = cw.visitField(ACC_PUBLIC, "o", "Ljava/lang/Object;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "s", "Ljava/lang/String;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "i", "Ljava/lang/Integer;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "b", "Ljava/lang/Boolean;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "set", "Ljava/util/Set;", "Ljava/util/Set<Ljava/lang/String;>;", null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "hset", "Ljava/util/HashSet;", "Ljava/util/HashSet<Ljava/util/Set<Ljava/lang/String;>;>;", null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_PUBLIC, "map", "Ljava/util/Map;", "Ljava/util/Map<Ljava/util/Set<Ljava/lang/String;>;Ljava/util/Set<Ljava/util/Map<Ljava/lang/String;Ljava/util/Set<Ljava/lang/String;>;>;>;>;", null);
      fv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(8, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitInsn(RETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypecastGenericArray;", null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(0));
      xav0.visitXOffset(new Integer(21));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "second");
      xav0.visitXTargetType(new Integer(0));
      xav0.visitXOffset(new Integer(32));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(18, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "o", "Ljava/lang/Object;");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "o", "Ljava/lang/Object;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(19, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "s", "Ljava/lang/String;");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "o", "Ljava/lang/Object;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(20, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/lang/String");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "s", "Ljava/lang/String;");
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(21, l3);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "i", "Ljava/lang/Integer;");
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLineNumber(22, l4);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "b", "Ljava/lang/Boolean;");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "b", "Ljava/lang/Boolean;");
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(23, l5);
      mv.visitInsn(RETURN);
      Label l6 = new Label();
      mv.visitLabel(l6);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypecastGenericArray;", null, l0, l6, 0);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test2", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(5));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "B");
      xav0.visitXTargetType(new Integer(0));
      xav0.visitXOffset(new Integer(16));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/C;", true);
      xav0.visit("fieldA", new Integer(2));
      xav0.visit("fieldB", "");
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(16));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(27, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/HashSet");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "set", "Ljava/util/Set;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(28, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Set");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "set", "Ljava/util/Set;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(29, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypecastGenericArray;", null, l0, l3, 0);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test3", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(20));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "v");
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(23));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(33, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "map", "Ljava/util/Map;");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "keySet", "()Ljava/util/Set;");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/HashSet");
      mv.visitTypeInsn(CHECKCAST, "java/util/HashSet");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "set", "Ljava/util/Set;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(34, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/HashSet");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "hset", "Ljava/util/HashSet;");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(35, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypecastGenericArray;", null, l0, l3, 0);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test4", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(5));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "second");
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(5));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(0));
      xav0.visitXOffset(new Integer(15));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(15));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "set");
      xav0.visitXTargetType(new Integer(0));
      xav0.visitXOffset(new Integer(30));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(43));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "on the set");
      xav0.visitXTargetType(new Integer(0));
      xav0.visitXOffset(new Integer(53));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "on value");
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(53));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(39, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Map");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "map", "Ljava/util/Map;");
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(40, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Set");
      mv.visitVarInsn(ASTORE, 1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(41, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "map", "Ljava/util/Map;");
      mv.visitInsn(ACONST_NULL);
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Set");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Map");
      mv.visitLdcInsn("");
      mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Set");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "set", "Ljava/util/Set;");
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(42, l3);
      mv.visitInsn(RETURN);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypecastGenericArray;", null, l0, l4, 0);
      mv.visitLocalVariable("t", "Ljava/util/Set;", "Ljava/util/Set<Ljava/util/Map<Ljava/lang/String;Ljava/util/Set<Ljava/lang/String;>;>;>;", l2, l4, 1);
      mv.visitMaxs(3, 2);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test5", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "string is key");
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(4));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "2d-array is value");
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(4));
      xav0.visitXLocationLength(new Integer(1));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "first dimension");
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(4));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(0));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "second dimension");
      xav0.visitXTargetType(new Integer(1));
      xav0.visitXOffset(new Integer(4));
      xav0.visitXLocationLength(new Integer(2));
      xav0.visitXLocation(new Integer(1));
      xav0.visitXLocation(new Integer(1));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(47, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypecastGenericArray", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(CHECKCAST, "java/util/Map");
      mv.visitVarInsn(ASTORE, 1);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(48, l1);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitVarInsn(ALOAD, 1);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(49, l2);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypecastGenericArray;", null, l0, l3, 0);
      mv.visitLocalVariable("m", "Ljava/util/Map;", "Ljava/util/Map<Ljava/lang/String;[[Ljava/lang/String;>;", l1, l3, 1);
      mv.visitMaxs(2, 2);
      mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
      }
    
    public static byte[] dumpTypeTest () throws Exception {

      ClassWriter cw = new ClassWriter(false);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;
      TypeAnnotationVisitor xav0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "annotations/tests/classfile/cases/TestTypeTest", null, "java/lang/Object", null);

      cw.visitSource("TestTypeTest.java", null);

      {
      fv = cw.visitField(ACC_PUBLIC, "o", "Ljava/lang/Object;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_STATIC + ACC_SYNTHETIC, "class$0", "Ljava/lang/Class;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_STATIC + ACC_SYNTHETIC, "class$1", "Ljava/lang/Class;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_STATIC + ACC_SYNTHETIC, "class$2", "Ljava/lang/Class;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_STATIC + ACC_SYNTHETIC, "class$3", "Ljava/lang/Class;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_STATIC + ACC_SYNTHETIC, "class$4", "Ljava/lang/Class;", null, null);
      fv.visitEnd();
      }
      {
      fv = cw.visitField(ACC_STATIC + ACC_SYNTHETIC, "class$5", "Ljava/lang/Class;", null, null);
      fv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(8, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitInsn(RETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypeTest;", null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "ismap");
      xav0.visitXTargetType(new Integer(2));
      xav0.visitXOffset(new Integer(4));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(2));
      xav0.visitXOffset(new Integer(14));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "islist");
      xav0.visitXTargetType(new Integer(2));
      xav0.visitXOffset(new Integer(24));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(12, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(INSTANCEOF, "java/util/Map");
      Label l1 = new Label();
      mv.visitJumpInsn(IFEQ, l1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(13, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(INSTANCEOF, "java/util/Set");
      mv.visitJumpInsn(IFEQ, l1);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(14, l3);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(INSTANCEOF, "java/util/List");
      mv.visitJumpInsn(IFEQ, l1);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLineNumber(15, l4);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/lang/Object");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitLabel(l1);
      mv.visitLineNumber(19, l1);
      mv.visitInsn(RETURN);
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypeTest;", null, l0, l5, 0);
      mv.visitMaxs(3, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test2", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(2));
      xav0.visitXOffset(new Integer(4));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(2));
      xav0.visitXOffset(new Integer(14));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(22, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(INSTANCEOF, "java/util/List");
      Label l1 = new Label();
      mv.visitJumpInsn(IFEQ, l1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(23, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(INSTANCEOF, "java/util/ArrayList");
      mv.visitJumpInsn(IFEQ, l1);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(24, l3);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/lang/Object");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitLabel(l1);
      mv.visitLineNumber(27, l1);
      mv.visitInsn(RETURN);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypeTest;", null, l0, l4, 0);
      mv.visitMaxs(3, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test3", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "instanceof object");
      xav0.visitXTargetType(new Integer(2));
      xav0.visitXOffset(new Integer(4));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(30, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(INSTANCEOF, "java/lang/Object");
      Label l1 = new Label();
      mv.visitJumpInsn(IFNE, l1);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLineNumber(31, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/lang/Object");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitFieldInsn(PUTFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitLabel(l1);
      mv.visitLineNumber(33, l1);
      mv.visitInsn(RETURN);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypeTest;", null, l0, l3, 0);
      mv.visitMaxs(3, 1);
      mv.visitEnd();
      }
      {
      mv = cw.visitMethod(ACC_PUBLIC, "test4", "()V", null, null);
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(2));
      xav0.visitXOffset(new Integer(12));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "second");
      xav0.visitXTargetType(new Integer(2));
      xav0.visitXOffset(new Integer(28));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(2));
      xav0.visitXOffset(new Integer(44));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/B;", true);
      xav0.visit("value", "fourth");
      xav0.visitXTargetType(new Integer(2));
      xav0.visitXOffset(new Integer(60));
      xav0.visitEnd();
      }
      {
      xav0 = mv.visitTypeAnnotation("Lannotations/tests/classfile/foo/A;", true);
      xav0.visitXTargetType(new Integer(2));
      xav0.visitXOffset(new Integer(76));
      xav0.visitEnd();
      }
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(36, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
      mv.visitVarInsn(ASTORE, 1);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(37, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(INSTANCEOF, "java/lang/Boolean");
      Label l2 = new Label();
      mv.visitJumpInsn(IFEQ, l2);
      Label l3 = new Label();
      mv.visitLabel(l3);
      mv.visitLineNumber(38, l3);
      mv.visitLdcInsn(Type.getType("Ljava/lang/Boolean;"));
      mv.visitVarInsn(ASTORE, 1);
      Label l4 = new Label();
      mv.visitJumpInsn(GOTO, l4);
      mv.visitLabel(l2);
      mv.visitLineNumber(39, l2);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(INSTANCEOF, "java/lang/Integer");
      Label l5 = new Label();
      mv.visitJumpInsn(IFEQ, l5);
      Label l6 = new Label();
      mv.visitLabel(l6);
      mv.visitLineNumber(40, l6);
      mv.visitLdcInsn(Type.getType("Ljava/lang/Integer;"));
      mv.visitVarInsn(ASTORE, 1);
      mv.visitJumpInsn(GOTO, l4);
      mv.visitLabel(l5);
      mv.visitLineNumber(41, l5);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(INSTANCEOF, "java/lang/Character");
      Label l7 = new Label();
      mv.visitJumpInsn(IFEQ, l7);
      Label l8 = new Label();
      mv.visitLabel(l8);
      mv.visitLineNumber(42, l8);
      mv.visitLdcInsn(Type.getType("Ljava/lang/Character;"));
      mv.visitVarInsn(ASTORE, 1);
      mv.visitJumpInsn(GOTO, l4);
      mv.visitLabel(l7);
      mv.visitLineNumber(43, l7);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
      Label l9 = new Label();
      mv.visitJumpInsn(IFEQ, l9);
      Label l10 = new Label();
      mv.visitLabel(l10);
      mv.visitLineNumber(44, l10);
      mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
      mv.visitVarInsn(ASTORE, 1);
      mv.visitJumpInsn(GOTO, l4);
      mv.visitLabel(l9);
      mv.visitLineNumber(45, l9);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "annotations/tests/classfile/cases/TestTypeTest", "o", "Ljava/lang/Object;");
      mv.visitTypeInsn(INSTANCEOF, "java/util/List");
      Label l11 = new Label();
      mv.visitJumpInsn(IFEQ, l11);
      Label l12 = new Label();
      mv.visitLabel(l12);
      mv.visitLineNumber(46, l12);
      mv.visitLdcInsn(Type.getType("Ljava/util/List;"));
      mv.visitVarInsn(ASTORE, 1);
      mv.visitJumpInsn(GOTO, l4);
      mv.visitLabel(l11);
      mv.visitLineNumber(48, l11);
      mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
      mv.visitVarInsn(ASTORE, 1);
      mv.visitLabel(l4);
      mv.visitLineNumber(50, l4);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitVarInsn(ALOAD, 1);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
      Label l13 = new Label();
      mv.visitLabel(l13);
      mv.visitLineNumber(51, l13);
      mv.visitInsn(RETURN);
      Label l14 = new Label();
      mv.visitLabel(l14);
      mv.visitLocalVariable("this", "Lannotations/tests/classfile/cases/TestTypeTest;", null, l0, l14, 0);
      mv.visitLocalVariable("c", "Ljava/lang/Class;", null, l1, l14, 1);
      mv.visitMaxs(2, 2);
      mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
      }
}
