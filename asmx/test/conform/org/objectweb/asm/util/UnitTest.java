package org.objectweb.asm.util;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.attrs.StackMapTableAttribute;
import org.objectweb.asm.commons.EmptyVisitor;

import junit.framework.TestCase;

public class UnitTest extends TestCase implements Opcodes {

    public void testTraceClassVisitor() throws Exception {
        String s = getClass().getName();
        TraceClassVisitor.main(new String[0]);
        TraceClassVisitor.main(new String[] { "-debug" });
        TraceClassVisitor.main(new String[] { s });
        TraceClassVisitor.main(new String[] { "-debug", s });
        try {
            TraceClassVisitor.main(new String[] { s + ".class" });
        } catch (Exception e) {
        }
    }

    public void testASMifierClassVisitor() throws Exception {
        String s = getClass().getName();
        ASMifierClassVisitor.main(new String[0]);
        ASMifierClassVisitor.main(new String[] { "-debug" });
        ASMifierClassVisitor.main(new String[] { s });
        ASMifierClassVisitor.main(new String[] { "-debug", s });
        try {
            ASMifierClassVisitor.main(new String[] { s + ".class" });
        } catch (Exception e) {
        }
    }

    public void testCheckClassVisitor() throws Exception {
        String s = getClass().getName();
        CheckClassAdapter.main(new String[0]);
        CheckClassAdapter.main(new String[] { s });
        try {
            CheckClassAdapter.main(new String[] { s + ".class" });
        } catch (Exception e) {
        }

        ClassVisitor cv = new CheckClassAdapter(new EmptyVisitor());        
        try {
            cv.visit(V1_1, 1 << 20, "C", null, "java/lang/Object", null);
            fail();
        } catch (Exception e) {
        }
        
        cv = new CheckClassAdapter(new EmptyVisitor());
        try {
            cv.visit(V1_1, ACC_PUBLIC, "java/lang/Object", null, "java/lang/Object", null);
            fail();
        } catch (Exception e) {
        }

        cv = new CheckClassAdapter(new EmptyVisitor());
        try {
            cv.visit(V1_1, ACC_INTERFACE, "I", null, "C", null);
            fail();
        } catch (Exception e) {
        }

        cv = new CheckClassAdapter(new EmptyVisitor());
        try {
            cv.visit(V1_1, ACC_FINAL+ACC_ABSTRACT, "C", null, "java/lang/Object", null);
            fail();
        } catch (Exception e) {
        }

        cv = new CheckClassAdapter(new EmptyVisitor());
        try {
            cv.visitSource(null, null);
            fail();
        } catch (Exception e) {
        }

        cv.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
        try {
            cv.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
            fail();
        } catch (Exception e) {
        }

        cv.visitSource(null, null);
        try {
            cv.visitSource(null, null);
            fail();
        } catch (Exception e) {
        }

        try {
            cv.visitOuterClass(null, null, null);
            fail();
        } catch (Exception e) {
        }
        try {
            cv.visitOuterClass(null, null, null);
            fail();
        } catch (Exception e) {
        }

        try {
            cv.visitField(ACC_PUBLIC+ACC_PRIVATE, "i", "I", null, null);
            fail();
        } catch (Exception e) {
        }
        
        cv.visitEnd();
        try {
            cv.visitSource(null, null);
            fail();
        } catch (Exception e) {
        }
        
        FieldVisitor fv = new CheckFieldAdapter(new EmptyVisitor());
        fv.visitEnd();
        try {
            fv.visitAttribute(new StackMapTableAttribute());
            fail();
        } catch (Exception e) {
        }

        fv = new CheckFieldAdapter(new EmptyVisitor());
        try {
            fv.visitAttribute(null);
            fail();
        } catch (Exception e) {
        }

        MethodVisitor mv = new CheckMethodAdapter(new EmptyVisitor());
        try {
            mv.visitParameterAnnotation(0, "'", true);
            fail();
        } catch (Exception e) {
        }

        mv.visitEnd();
        try {
            mv.visitAttribute(new StackMapTableAttribute());
            fail();
        } catch (Exception e) {
        }

        mv = new CheckMethodAdapter(new EmptyVisitor());
        try {
            mv.visitAttribute(null);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitInsn(NOP);
            fail();
        } catch (Exception e) {
        }

        mv.visitCode();
        try {
            mv.visitInsn(-1);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitIntInsn(BIPUSH, Integer.MAX_VALUE);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitIntInsn(SIPUSH, Integer.MAX_VALUE);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitVarInsn(ALOAD, -1);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitIntInsn(NEWARRAY, 0);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitTypeInsn(NEW, "[I");
            fail();
        } catch (Exception e) {
        }

        Label l = new Label();
        mv.visitLabel(l);
        try {
            mv.visitLabel(l);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitTableSwitchInsn(1, 0, new Label(), new Label[0]);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitTableSwitchInsn(0, 1, null, new Label[0]);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitTableSwitchInsn(0, 1, new Label(), null);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitTableSwitchInsn(0, 1, new Label(), new Label[0]);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitLookupSwitchInsn(new Label(), null, new Label[0]);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitLookupSwitchInsn(new Label(), new int[0], null);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitLookupSwitchInsn(new Label(), new int[0], new Label[1]);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitFieldInsn(GETFIELD, null, "i", "I");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitFieldInsn(GETFIELD, "-", "i", "I");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitFieldInsn(GETFIELD, "C", null, "I");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitFieldInsn(GETFIELD, "C", "-", "I");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitFieldInsn(GETFIELD, "C", "a-", "I");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitFieldInsn(GETFIELD, "C", "i", null);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitFieldInsn(GETFIELD, "C", "i", "V");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitFieldInsn(GETFIELD, "C", "i", "II");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitFieldInsn(GETFIELD, "C", "i", "[");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitFieldInsn(GETFIELD, "C", "i", "L");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitFieldInsn(GETFIELD, "C", "i", "L-;");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitMethodInsn(INVOKEVIRTUAL, "C", null, "()V");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitMethodInsn(INVOKEVIRTUAL, "C", "-", "()V");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitMethodInsn(INVOKEVIRTUAL, "C", "a-", "()V");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitMethodInsn(INVOKEVIRTUAL, "C", "m", null);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitMethodInsn(INVOKEVIRTUAL, "C", "m", "I");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitMethodInsn(INVOKEVIRTUAL, "C", "m", "(V)V");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitMethodInsn(INVOKEVIRTUAL, "C", "m", "()VV");
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitLdcInsn(new Object());
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitMultiANewArrayInsn("I", 0);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitMultiANewArrayInsn("[[I", 0);
            fail();
        } catch (Exception e) {
        }

        try {
            mv.visitMultiANewArrayInsn("[[I", 3);
            fail();
        } catch (Exception e) {
        }
        
        Label m = new Label();
        mv.visitInsn(NOP);
        mv.visitLabel(m);
        try {
            mv.visitLocalVariable("i", "I", null, m, l, 0);
            fail();
        } catch (Exception e) {
        }
        
        try {
            mv.visitLineNumber(0, new Label());
            fail();
        } catch (Exception e) {
        }

        mv.visitMaxs(0,0);
        try {
            mv.visitInsn(NOP);
            fail();
        } catch (Exception e) {
        }
    }
}
