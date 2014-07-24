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

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.IADD;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.Type;

import java.io.InputStream;

/**
 * @author Eric Bruneton
 */
public class BCELPerfTest extends ALLPerfTest implements Constants {

    public static void main(final String args[]) throws Exception {
        System.out.println("BCEL PERFORMANCES\n");
        new BCELPerfTest().perfs(args);
    }

    ALLPerfTest newInstance() {
        return new BCELPerfTest();
    }

    byte[] nullAdaptClass(final InputStream is, final String name)
            throws Exception
    {
        JavaClass jc = new ClassParser(is, name + ".class").parse();
        ClassGen cg = new ClassGen(jc);
        ConstantPoolGen cp = cg.getConstantPool();
        Method[] ms = cg.getMethods();
        for (int j = 0; j < ms.length; ++j) {
            MethodGen mg = new MethodGen(ms[j], cg.getClassName(), cp);
            boolean lv = ms[j].getLocalVariableTable() == null;
            boolean ln = ms[j].getLineNumberTable() == null;
            if (lv) {
                mg.removeLocalVariables();
            }
            if (ln) {
                mg.removeLineNumbers();
            }
            mg.stripAttributes(skipDebug);
            InstructionList il = mg.getInstructionList();
            if (il != null) {
                InstructionHandle ih = il.getStart();
                while (ih != null) {
                    ih = ih.getNext();
                }
                if (compute) {
                    mg.setMaxStack();
                    mg.setMaxLocals();
                }
            }
            cg.replaceMethod(ms[j], mg.getMethod());
        }
        return cg.getJavaClass().getBytes();
    }

    byte[] counterAdaptClass(final InputStream is, final String name)
            throws Exception
    {
        JavaClass jc = new ClassParser(is, name + ".class").parse();
        ClassGen cg = new ClassGen(jc);
        ConstantPoolGen cp = cg.getConstantPool();
        if (!cg.isInterface()) {
            FieldGen fg = new FieldGen(ACC_PUBLIC,
                    Type.getType("I"),
                    "_counter",
                    cp);
            cg.addField(fg.getField());
        }
        Method[] ms = cg.getMethods();
        for (int j = 0; j < ms.length; ++j) {
            MethodGen mg = new MethodGen(ms[j], cg.getClassName(), cp);
            if (!mg.getName().equals("<init>") && !mg.isStatic()
                    && !mg.isAbstract() && !mg.isNative())
            {
                if (mg.getInstructionList() != null) {
                    InstructionList il = new InstructionList();
                    il.append(new ALOAD(0));
                    il.append(new ALOAD(0));
                    il.append(new GETFIELD(cp.addFieldref(name, "_counter", "I")));
                    il.append(new ICONST(1));
                    il.append(new IADD());
                    il.append(new PUTFIELD(cp.addFieldref(name, "_counter", "I")));
                    mg.getInstructionList().insert(il);
                    mg.setMaxStack(Math.max(mg.getMaxStack(), 2));
                    boolean lv = ms[j].getLocalVariableTable() == null;
                    boolean ln = ms[j].getLineNumberTable() == null;
                    if (lv) {
                        mg.removeLocalVariables();
                    }
                    if (ln) {
                        mg.removeLineNumbers();
                    }
                    cg.replaceMethod(ms[j], mg.getMethod());
                }
            }
        }
        return cg.getJavaClass().getBytes();
    }
}
