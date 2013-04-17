package org.objectweb.asm;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.TestCase;

public class UnitTest extends TestCase {

    public void testReader() throws IOException {
        try {
            new ClassReader((InputStream) null);
            fail();
        } catch (IOException e) {
        }

        ClassReader cr = new ClassReader(getClass().getName());
        int item = cr.getItem(1);
        assertTrue(item >= 10);
        assertTrue(item < cr.header);
    }

    public void testWriter() {
        ClassWriter cw = new ClassWriter(false);
        cw.newConst(new Byte((byte) 0));
        cw.newConst(new Character('0'));
        cw.newConst(new Short((short) 0));
        cw.newConst(new Boolean(false));
        try {
            cw.newConst(new Object());
            fail();
        } catch (RuntimeException e) {
        }
        cw.newMethod("A", "m", "()V", false);
    }
    
    public void testLabel() {
        new Label().toString();
        try {
            new Label().getOffset();
            fail();
        } catch (RuntimeException e) {
        }
    }
    
    public void testType() {
        assertEquals(Type.getType(Integer.TYPE), Type.INT_TYPE);
        assertEquals(Type.getType(Void.TYPE), Type.VOID_TYPE);
        assertEquals(Type.getType(Boolean.TYPE), Type.BOOLEAN_TYPE);
        assertEquals(Type.getType(Byte.TYPE), Type.BYTE_TYPE);
        assertEquals(Type.getType(Character.TYPE), Type.CHAR_TYPE);
        assertEquals(Type.getType(Short.TYPE), Type.SHORT_TYPE);
        assertEquals(Type.getType(Double.TYPE), Type.DOUBLE_TYPE);
        assertEquals(Type.getType(Float.TYPE), Type.FLOAT_TYPE);
        assertEquals(Type.getType(Long.TYPE), Type.LONG_TYPE);
        String s1 = Type.getType(UnitTest.class).getInternalName();
        String s2 = Type.getInternalName(UnitTest.class);
        assertEquals(s1, s2);
        for (int i = 0; i < Arrays.class.getMethods().length; ++i) {
            Method m = Arrays.class.getMethods()[i];
            Type[] args = Type.getArgumentTypes(m);
            Type r = Type.getReturnType(m);
            String d1 = Type.getMethodDescriptor(r, args);
            String d2 = Type.getMethodDescriptor(m);
            assertEquals(d1, d2);
        }
        Type.INT_TYPE.hashCode();
    }
}
