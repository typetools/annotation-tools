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
package org.objectweb.asm.util.attrs;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.objectweb.asm.AbstractTest;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.attrs.StackMapTableAttributeTest;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.objectweb.asm.util.ASMifierTest;
import org.objectweb.asm.util.AbstractVisitor;

/**
 * StackMapTableAttributeTest
 * 
 * @author Eugene Kuleshov
 */
public class ASMStackMapTableAttributeTest extends AbstractTest {

    public ASMStackMapTableAttributeTest() {
        super();
    }

    public void test() throws Exception {
        String n = "org.objectweb.asm.attrs.StackMapTableSample";
        InputStream is = StackMapTableAttributeTest.class.getResourceAsStream(StackMapTableAttributeTest.TEST_CLASS);
        ClassReader cr = new ClassReader(is);

        StringWriter sw = new StringWriter();
        ASMifierClassVisitor cv = new ASMifierClassVisitor(new PrintWriter(sw));
        cr.accept(cv, AbstractVisitor.getDefaultAttributes(), false);

        String generated = sw.toString();

        byte[] generatorClassData;
        try {
            generatorClassData = ASMifierTest.COMPILER.compile(n, generated);
            System.err.println(generated);
        } catch (Exception ex) {
            System.err.println(generated);
            System.err.println("------------------");
            throw ex;
        }

        Class c = ASMifierTest.LOADER.defineClass("asm." + n + "Dump",
                generatorClassData);
        Method m = c.getMethod("dump", new Class[0]);
        byte[] b = (byte[]) m.invoke(null, new Object[0]);

        assertEquals(cr, new ClassReader(b));
    }

}
