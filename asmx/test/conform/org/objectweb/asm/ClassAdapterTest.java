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

import junit.framework.TestSuite;

/**
 * ClassAdapter tests.
 * 
 * @author Eric Bruneton
 */
public class ClassAdapterTest extends AbstractTest {

    private final static TestClassLoader LOADER = new TestClassLoader();

    public static TestSuite suite() throws Exception {
        return new ClassAdapterTest().getSuite();
    }

    public void test() throws Exception {
        ClassReader cr = new ClassReader(is);
        ClassWriter cw = new ClassWriter(true, true);
        cr.accept(new ClassAdapter(cw), false);
        byte[] b = cw.toByteArray();
        try {
            LOADER.defineClass(n, b);
        } catch (ClassFormatError cfe) {
            fail(cfe.getMessage());
        } catch (Throwable ignored) {
        }
    }

    // ------------------------------------------------------------------------

    static class TestClassLoader extends ClassLoader {

        public Class defineClass(final String name, final byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
}
