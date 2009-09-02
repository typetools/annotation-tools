/***
 * ASM tests
 * Copyright (c) 2002-2005 France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.objectweb.asm;

import java.io.InputStream;
import java.lang.annotation.Annotation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import annotations.ValueAttrAnnotation;
import annotations.Values;
import annotations.ValuesAnnotation;
import annotations.ValuesDump;
import annotations.ValuesEnum;

import junit.framework.TestCase;

public class AnnotationTest extends TestCase {

    private ValuesAnnotation a;

    public AnnotationTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        TestClassLoader cl = new TestClassLoader("annotations.Values",
                getClass().getClassLoader());
        Class c = cl.loadClass("annotations.Values");
        Annotation[] annotations = c.getAnnotations();
        for (int i = 0; i < annotations.length; ++i) {
            if (annotations[i] instanceof ValuesAnnotation) {
                a = (ValuesAnnotation) annotations[i];
            }
        }
        if (a == null) {
            fail();
        }
    }

    public void testByteValue() {
        assertEquals(1, a.byteValue());
    }

    public void testCharValue() {
        assertEquals('A', a.charValue());
    }

    public void testBooleanValue() {
        assertEquals(true, a.booleanValue());
    }

    public void testIntValue() {
        assertEquals(1, a.intValue());
    }

    public void testShortValue() {
        assertEquals(1, a.shortValue());
    }

    public void testLongValue() {
        assertEquals(1L, a.longValue());
    }

    public void testFloatValue() {
        assertEquals(1.0f, a.floatValue(), 0.1f);
    }

    public void testDoubleValue() {
        assertEquals(1.0d, a.doubleValue(), 0.1d);
    }

    public void testStringValue() {
        assertEquals("A", a.stringValue());
    }

    public void testAnnotationValue() {
        ValueAttrAnnotation ann = a.annotationValue();
        assertEquals("annotation", ann.value());
    }

    public void testEnumValue() {
        ValuesEnum en = a.enumValue();
        assertEquals(ValuesEnum.ONE, en);
    }

    public void testClassValue() {
        Class c = a.classValue();
        assertEquals(Values.class.getName(), c.getName());
    }

    public void testByteArrayValue() {
        byte[] bs = a.byteArrayValue();
        assertEquals(1, bs[0]);
        assertEquals(-1, bs[1]);
    }

    public void testCharArrayValue() {
        char[] bs = a.charArrayValue();
        assertEquals('c', bs[0]);
        assertEquals('b', bs[1]);
        assertEquals((char) -1, bs[2]);
    }

    public void testBooleanArrayValue() {
        boolean[] bs = a.booleanArrayValue();
        assertEquals(true, bs[0]);
        assertEquals(false, bs[1]);
    }

    public void testIntArrayValue() {
        int[] bs = a.intArrayValue();
        assertEquals(1, bs[0]);
        assertEquals(-1, bs[1]);
    }

    public void testShortArrayValue() {
        short[] bs = a.shortArrayValue();
        assertEquals(1, bs[0]);
        assertEquals(-1, bs[1]);
    }

    public void testLongArrayValue() {
        long[] bs = a.longArrayValue();
        assertEquals(1L, bs[0]);
        assertEquals(-1L, bs[1]);
    }

    public void testFloatArrayValue() {
        float[] bs = a.floatArrayValue();
        assertEquals(1.0f, bs[0], 0.1f);
        assertEquals(-1.0f, bs[1], 0.1f);
    }

    public void testDoubleArrayValue() {
        double[] bs = a.doubleArrayValue();
        assertEquals(1.0d, bs[0], 0.1d);
        assertEquals(-1.0d, bs[1], 0.1d);
    }

    public void testStringArrayValue() {
        String[] s = a.stringArrayValue();
        assertEquals("aa", s[0]);
        assertEquals("bb", s[1]);
    }

    public void testAnnotationArrayValue() {
        ValueAttrAnnotation[] ann = a.annotationArrayValue();
        assertEquals("annotation1", ann[0].value());
        assertEquals("annotation2", ann[1].value());
    }

    public void testEnumArrayValue() {
        ValuesEnum[] en = a.enumArrayValue();
        assertEquals(ValuesEnum.ONE, en[0]);
        assertEquals(ValuesEnum.TWO, en[1]);
    }

    public void testClassArrayValue() {
        Class[] c = a.classArrayValue();
        assertEquals(Values.class.getName(), c[0].getName());
        assertEquals(Values.class.getName(), c[1].getName());
    }

    // issue 303711
    public void testMethodNode() throws Exception {
        InputStream is = getClass().getResourceAsStream("/"
                + annotations.Values.class.getName().replace('.', '/')
                + ".class");
        ClassReader cr = new ClassReader(is);
        ClassNode cn = new ClassNode();
        cr.accept(cn, false);
    }

    private static final class TestClassLoader extends ClassLoader {

        private final String className;

        private final ClassLoader loader;

        public TestClassLoader(String className, ClassLoader loader) {
            super();
            this.className = className;
            this.loader = loader;
        }

        public Class loadClass(String name) throws ClassNotFoundException {
            if (className.equals(name)) {
                try {
                    byte[] bytecode = ValuesDump.dump();
                    return super.defineClass(className,
                            bytecode,
                            0,
                            bytecode.length);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new ClassNotFoundException("Load error: "
                            + ex.toString(), ex);
                }
            }

            return loader.loadClass(name);
        }
    }
}
