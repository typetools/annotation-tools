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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.util.TraceClassVisitor;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Super class for test suites based on a jar file.
 *
 * @author Eugene Kuleshov
 * @author Eric Bruneton
 */
public abstract class AbstractTest extends TestCase {

    protected String n;

    protected InputStream is;

    public AbstractTest() {
        super("test");
    }

    protected void init(final String n, final InputStream is) {
        this.n = n;
        this.is = is;
    }

    protected TestSuite getSuite() throws Exception {
        TestSuite suite = new TestSuite(getClass().getName());
        String files = System.getProperty("asm.test");
        String clazz = System.getProperty("asm.test.class");
        if(files==null) {
            files = System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar";
            if(clazz==null) {
                clazz = "java.lang.";
            }
        }
        files += ",";

        while (files.indexOf(',') != -1) {
            String file = files.substring(0, files.indexOf(','));
            files = files.substring(files.indexOf(',') + 1);
            File f = new File(file);
            if (f.isDirectory()) {
                File[] fs = f.listFiles();
                for (int i = 0; i < fs.length; ++i) {
                    String n = fs[i].getName();
                    if (n.endsWith(".class")) {
                        n = n.substring(0, n.length() - 6).replace('/', '.');
                        if (clazz == null || n.indexOf(clazz) != -1) {
                            InputStream is = new FileInputStream(fs[i]);
                            AbstractTest t = (AbstractTest) getClass().newInstance();
                            t.init(n, is);
                            suite.addTest(t);
                        }
                    }
                }
            } else {
                ZipFile zip = new ZipFile(file);
                Enumeration entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry e = (ZipEntry) entries.nextElement();
                    String n = e.getName();
                    if (n.endsWith(".class")) {
                        n = n.substring(0, n.length() - 6).replace('/', '.');
                        if (clazz == null || n.indexOf(clazz) != -1) {
                            InputStream is = zip.getInputStream(e);
                            AbstractTest t = (AbstractTest) getClass().newInstance();
                            t.init(n, is);
                            suite.addTest(t);
                        }
                    }
                }
            }
        }
        return suite;
    }

    public abstract void test() throws Exception;

    public void assertEquals(final ClassReader cr1, final ClassReader cr2)
            throws Exception
    {
        if (!Arrays.equals(cr1.b, cr2.b)) {
            StringWriter sw1 = new StringWriter();
            StringWriter sw2 = new StringWriter();
            ClassVisitor cv1 = new TraceClassVisitor(new PrintWriter(sw1));
            ClassVisitor cv2 = new TraceClassVisitor(new PrintWriter(sw2));
            cr1.accept(new ClassFilter(cv1), false);
            cr2.accept(new ClassFilter(cv2), false);
            String s1 = sw1.toString();
            String s2 = sw2.toString();
            assertEquals("different data", s1, s2);
        }
    }

    public String getName() {
        return super.getName() + ": " + n;
    }

    // -------------------------------------------------------------------------

    static class ClassFilter extends ClassAdapter {

        public ClassFilter(final ClassVisitor cv) {
            super(cv);
        }

        public void visitAttribute(final Attribute attr) {
            // remove unknown attributes
        }

        public FieldVisitor visitField(
            final int access,
            final String name,
            final String desc,
            final String signature,
            final Object value)
        {
            return new FieldFilter(cv.visitField(access,
                    name,
                    desc,
                    signature,
                    value));
        }

        public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String desc,
            final String signature,
            final String[] exceptions)
        {
            return new MethodFilter(cv.visitMethod(access,
                    name,
                    desc,
                    signature,
                    exceptions));
        }
    }

    static class MethodFilter extends MethodAdapter {

        public MethodFilter(final MethodVisitor mv) {
            super(mv);
        }

        public void visitAttribute(final Attribute attr) {
            // remove unknown attributes
        }
    }

    static class FieldFilter implements FieldVisitor {

        FieldVisitor fv;

        public FieldFilter(final FieldVisitor fv) {
            this.fv = fv;
        }

        public AnnotationVisitor visitAnnotation(
            final String desc,
            final boolean visible)
        {
            return fv.visitAnnotation(desc, visible);
        }

        public TypeAnnotationVisitor visitTypeAnnotation(
            final String desc,
            final boolean visible)
        {
            return fv.visitTypeAnnotation(desc, visible);
        }

        public void visitAttribute(final Attribute attr) {
            // remove unknown attributes
        }

        public void visitEnd() {
            fv.visitEnd();
        }
    }
}
