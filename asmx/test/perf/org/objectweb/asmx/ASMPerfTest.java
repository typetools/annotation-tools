/***
 * ASM performance test: measures the performances of asm package
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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.InputStream;

/**
 * @author Eric Bruneton
 */
public class ASMPerfTest extends ALLPerfTest {

    final static Integer ONE = new Integer(1);

    public static void main(final String args[]) throws Exception {
        System.out.println("ASM PERFORMANCES\n");
        new ASMPerfTest().perfs(args);
    }

    ALLPerfTest newInstance() {
        return new ASMPerfTest();
    }

    byte[] nullAdaptClass(final InputStream is, final String name)
            throws Exception
    {
        ClassReader cr = new ClassReader(is);
        ClassWriter cw = new ClassWriter(compute);
        ClassAdapter ca = new ClassAdapter(cw);
        cr.accept(ca, skipDebug);
        return cw.toByteArray();
    }

    byte[] counterAdaptClass(final InputStream is, final String name)
            throws Exception
    {
        ClassReader cr = new ClassReader(is);
        ClassWriter cw = new ClassWriter(false);
        ClassAdapter ca = new CounterClassAdapter(cw);
        cr.accept(ca, false);
        return cw.toByteArray();
    }

    static class CounterClassAdapter extends ClassAdapter implements Opcodes {

        private String owner;

        CounterClassAdapter(ClassVisitor cv) {
            super(cv);
        }

        public void visit(
            int version,
            int access,
            String name,
            String signature,
            String superName,
            String[] interfaces)
        {
            super.visit(version, access, name, signature, superName, interfaces);
            if ((access & ACC_INTERFACE) == 0) {
                cv.visitField(ACC_PUBLIC, "_counter", "I", null, null);
            }
            owner = name;
        }

        public MethodVisitor visitMethod(
            int access,
            String name,
            String desc,
            String signature,
            String[] exceptions)
        {
            MethodVisitor mv = super.visitMethod(access,
                    name,
                    desc,
                    signature,
                    exceptions);
            if (!name.equals("<init>")
                    && (access & (ACC_STATIC | ACC_NATIVE | ACC_ABSTRACT)) == 0)
            {
                return new CounterMethodAdapter(mv, owner);
            }
            return mv;
        }
    }

    static class CounterMethodAdapter extends MethodAdapter implements Opcodes {

        CounterMethodAdapter(MethodVisitor mv, String owner) {
            super(mv);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, owner, "_counter", "I");
            mv.visitLdcInsn(ONE);
            mv.visitInsn(IADD);
            mv.visitFieldInsn(PUTFIELD, owner, "_counter", "I");
        }

        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(Math.max(maxStack, 2), maxLocals);
        }
    }
}
