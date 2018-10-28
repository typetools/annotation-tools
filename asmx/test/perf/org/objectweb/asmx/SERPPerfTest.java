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

import serp.bytecode.BCClass;
import serp.bytecode.BCMethod;
import serp.bytecode.Code;
import serp.bytecode.Project;

import java.io.InputStream;

/**
 * @author Eric Bruneton
 */
public class SERPPerfTest extends ALLPerfTest {

    private static Project p = new Project();

    private static BCClass c;

    public static void main(final String args[]) throws Exception {
        System.out.println("SERP PERFORMANCES\n");
        new SERPPerfTest().perfs(args);
    }

    ALLPerfTest newInstance() {
        return new SERPPerfTest();
    }

    byte[] nullAdaptClass(final InputStream is, final String name)
            throws Exception
    {
        if (c != null) {
            p.removeClass(c);
        }
        c = p.loadClass(is);
        c.getDeclaredFields();
        BCMethod[] methods = c.getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            Code code = methods[i].getCode(false);
            if (code != null) {
                while (code.hasNext()) {
                    code.next();
                }
                if (compute) {
                    code.calculateMaxStack();
                    code.calculateMaxLocals();
                }
            }
        }
        return c.toByteArray();
    }

    byte[] counterAdaptClass(final InputStream is, final String name)
            throws Exception
    {
        if (c != null) {
            p.removeClass(c);
        }
        c = p.loadClass(is);
        c.getDeclaredFields();
        if (!c.isInterface()) {
            c.declareField("_counter", "I");
        }
        BCMethod[] methods = c.getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            BCMethod m = methods[i];
            if (!m.getName().equals("<init>") && !m.isStatic()
                    && !m.isAbstract() && !m.isNative())
            {
                Code code = m.getCode(false);
                if (code != null) {
                    code.aload().setLocal(0);
                    code.aload().setLocal(0);
                    code.getfield().setField(name, "_counter", "I");
                    code.constant().setValue(1);
                    code.iadd();
                    code.putfield().setField(name, "_counter", "I");
                    code.setMaxStack(Math.max(code.getMaxStack(), 2));
                }
            }
        }
        return c.toByteArray();
    }
}
