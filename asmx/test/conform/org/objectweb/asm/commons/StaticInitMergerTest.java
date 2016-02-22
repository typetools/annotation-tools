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
package org.objectweb.asm.commons;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import junit.framework.TestCase;

/**
 * @author Eric Bruneton
 */
public class StaticInitMergerTest extends TestCase implements Opcodes {

    private final static TestClassLoader LOADER = new TestClassLoader();

    public void test() throws Exception {
        ClassWriter cw = new ClassWriter(true);
        ClassVisitor cv = new StaticInitMerger("$clinit$", cw);
        cv.visit(V1_1, ACC_PUBLIC, "A", null, "java/lang/Object", null);
        cv.visitField(ACC_PUBLIC + ACC_STATIC, "counter", "I", null, null);
        for (int i = 0; i < 5; ++i) {
            MethodVisitor mv = cv.visitMethod(ACC_PUBLIC,
                    "<clinit>",
                    "()V",
                    null,
                    null);
            mv.visitFieldInsn(GETSTATIC, "A", "counter", "I");
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IADD);
            mv.visitFieldInsn(PUTSTATIC, "A", "counter", "I");
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
        }
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        cv.visitEnd();

        Class c = LOADER.defineClass("A", cw.toByteArray());
        assertEquals(c.getField("counter").getInt(c.newInstance()), 5);
    }

    // ------------------------------------------------------------------------

    static class TestClassLoader extends ClassLoader {

        public Class defineClass(final String name, final byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
}
