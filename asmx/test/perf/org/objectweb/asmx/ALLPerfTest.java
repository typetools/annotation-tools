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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author Eric Bruneton
 */
public abstract class ALLPerfTest extends ClassLoader {

    private static ZipFile zip;

    private static ZipOutputStream dst;

    private static int mode;

    private static int total;

    private static int totalSize;

    private static double[][] perfs;

    static boolean compute;

    static boolean skipDebug;

    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile(System.getProperty("java.home")
                + "/lib/rt.jar");
        List classes = new ArrayList();

        Enumeration entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = (ZipEntry) entries.nextElement();
            String s = e.getName();
            if (s.endsWith(".class")) {
                s = s.substring(0, s.length() - 6).replace('/', '.');
                InputStream is = zip.getInputStream(e);
                classes.add(readClass(is));
            }
        }

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                new ClassReader(b).accept(new EmptyVisitor(), false);
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize " + classes.size()
                    + " classes = " + t + " ms");
        }

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassWriter cw = new ClassWriter(false);
                new ClassReader(b).accept(cw, false);
                cw.toByteArray();
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes = " + t + " ms");
        }

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassReader cr = new ClassReader(b);
                ClassWriter cw = new ClassWriter(cr, false);
                cr.accept(cw, false);
                cw.toByteArray();
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes (with copyPool) = " + t + " ms");
        }
        
        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassReader cr = new ClassReader(b);
                ClassWriter cw = new ClassWriter(true);
                cr.accept(cw, false);
                cw.toByteArray();
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes (with computeMaxs) = " + t + " ms");
        }

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                new ClassReader(b).accept(new ClassNode(), false);
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize " + classes.size()
                    + " classes with tree package = " + t + " ms");
        }

        for (int i = 0; i < 10; ++i) {
            long t = System.currentTimeMillis();
            for (int j = 0; j < classes.size(); ++j) {
                byte[] b = (byte[]) classes.get(j);
                ClassWriter cw = new ClassWriter(false);
                ClassNode cn = new ClassNode();
                new ClassReader(b).accept(cn, false);
                cn.accept(cw);
                cw.toByteArray();
            }
            t = System.currentTimeMillis() - t;
            System.out.println("Time to deserialize and reserialize "
                    + classes.size() + " classes with tree package = " + t
                    + " ms");
        }

        classes = null;

        System.out.println("\nComparing ASM, BCEL, SERP and Javassist performances...");
        System.out.println("This may take 20 to 30 minutes\n");
        // measures performances
        System.out.println("ASM PERFORMANCES\n");
        new ASMPerfTest().perfs(args);
        double[][] asmPerfs = perfs;
        System.out.println("\nBCEL PERFORMANCES\n");
        new BCELPerfTest().perfs(args);
        double[][] bcelPerfs = perfs;
        System.out.println("\nSERP PERFORMANCES\n");
        new SERPPerfTest().perfs(args);
        double[][] serpPerfs = perfs;
        System.out.println("\nJavassist PERFORMANCES\n");
        new JavassistPerfTest().perfs(args);
        double[][] javassistPerfs = perfs;

        // prints results
        System.out.println("\nGLOBAL RESULTS");
        System.out.println("\nWITH DEBUG INFORMATION\n");
        for (int step = 0; step < 2; ++step) {
            for (mode = 0; mode < 4; ++mode) {
                switch (mode) {
                    case 0:
                        System.out.print("NO ADAPT:     ");
                        break;
                    case 1:
                        System.out.print("NULL ADAPT:   ");
                        break;
                    case 2:
                        System.out.print("COMPUTE MAXS: ");
                        break;
                    default:
                        System.out.print("ADD COUNTER:  ");
                        break;
                }
                System.out.print((float) asmPerfs[step][mode] + " ms");
                if (mode > 0) {
                    System.out.print(" (*");
                    System.out.print((float) (asmPerfs[step][mode] / asmPerfs[step][0]));
                    System.out.print(")");
                }
                System.out.print(" ");
                System.out.print((float) bcelPerfs[step][mode] + " ms");
                if (mode > 0) {
                    System.out.print(" (*");
                    System.out.print((float) (bcelPerfs[step][mode] / bcelPerfs[step][0]));
                    System.out.print(")");
                }
                System.out.print(" ");
                System.out.print((float) serpPerfs[step][mode] + " ms");
                if (mode > 0) {
                    System.out.print(" (*");
                    System.out.print((float) (serpPerfs[step][mode] / serpPerfs[step][0]));
                    System.out.print(")");
                }
                System.out.print(" ");
                System.out.print((float) javassistPerfs[step][mode] + " ms");
                if (mode > 0) {
                    System.out.print(" (*");
                    System.out.print((float) (javassistPerfs[step][mode] / javassistPerfs[step][0]));
                    System.out.print(")");
                }
                System.out.println();
            }
            if (step == 0) {
                System.out.println("\nWITHOUT DEBUG INFORMATION\n");
            }
        }

        System.out.println("\nRELATIVE RESULTS");
        System.out.println("\nWITH DEBUG INFORMATION\n");
        for (int step = 0; step < 2; ++step) {
            System.err.println("[MEASURE      ASM       BCEL      SERP Javassist]");
            for (mode = 1; mode < 4; ++mode) {
                int base;
                switch (mode) {
                    case 1:
                        System.out.print("NULL ADAPT:   ");
                        base = 0;
                        break;
                    case 2:
                        System.out.print("COMPUTE MAXS: ");
                        base = 1;
                        break;
                    default:
                        System.out.print("ADD COUNTER:  ");
                        base = 1;
                        break;
                }
                double ref = asmPerfs[step][mode] - asmPerfs[step][base];
                System.out.print((float) ref + " ms ");
                double f = bcelPerfs[step][mode] - bcelPerfs[step][base];
                System.out.print((float) f + " ms (*");
                System.out.print((float) (f / ref));
                System.out.print(") ");
                double g = serpPerfs[step][mode] - serpPerfs[step][base];
                System.out.print((float) g + " ms (*");
                System.out.print((float) (g / ref));
                System.out.print(")");
                double h = javassistPerfs[step][mode]
                        - javassistPerfs[step][base];
                System.out.print((float) h + " ms (*");
                System.out.print((float) (h / ref));
                System.out.print(")");
                System.out.println();
            }
            if (step == 0) {
                System.out.println("\nWITHOUT DEBUG INFORMATION\n");
            }
        }
    }

    void perfs(final String[] args) throws Exception {
        // prepares zip files, if necessary
        if (!(new File(args[0] + "classes1.zip").exists())) {
            System.out.println("Preparing zip files from " + args[1] + "...");
            for (int step = 0; step < 2; ++step) {
                dst = new ZipOutputStream(new FileOutputStream(args[0]
                        + "classes" + (step + 1) + ".zip"));
                mode = step == 0 ? 1 : 4;
                for (int i = 1; i < args.length; ++i) {
                    ALLPerfTest loader = newInstance();
                    zip = new ZipFile(args[i]);
                    Enumeration entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        String s = ((ZipEntry) entries.nextElement()).getName();
                        if (s.endsWith(".class")) {
                            s = s.substring(0, s.length() - 6)
                                    .replace('/', '.');
                            loader.loadClass(s);
                        }
                    }
                }
                dst.close();
                dst = null;
            }
            System.out.println();
        }

        // measures performances
        perfs = new double[2][4];
        System.out.println("FIRST STEP: WITH DEBUG INFORMATION");
        for (int step = 0; step < 2; ++step) {
            zip = new ZipFile(args[0] + "classes" + (step + 1) + ".zip");
            for (mode = 0; mode < 4; ++mode) {
                for (int i = 0; i < 4; ++i) {
                    ALLPerfTest loader = newInstance();
                    total = 0;
                    totalSize = 0;
                    Enumeration entries = zip.entries();
                    double t = System.currentTimeMillis();
                    while (entries.hasMoreElements()) {
                        String s = ((ZipEntry) entries.nextElement()).getName();
                        if (s.endsWith(".class")) {
                            s = s.substring(0, s.length() - 6)
                                    .replace('/', '.');
                            loader.loadClass(s);
                        }
                    }
                    t = System.currentTimeMillis() - t;
                    if (i == 0) {
                        perfs[step][mode] = t;
                    } else {
                        perfs[step][mode] = Math.min(perfs[step][mode], t);
                    }
                    switch (mode) {
                        case 0:
                            System.out.print("NO ADAPT:     ");
                            break;
                        case 1:
                            System.out.print("NULL ADAPT:   ");
                            break;
                        case 2:
                            System.out.print("COMPUTE MAXS: ");
                            break;
                        default:
                            System.out.print("ADD COUNTER:  ");
                            break;
                    }
                    System.out.print((float) t + " ms ");
                    System.out.print("(" + total + " classes");
                    System.out.println(", " + totalSize + " bytes)");
                    loader = null;
                    gc();
                }
            }
            if (step == 0) {
                System.out.println("SECOND STEP: WITHOUT DEBUG INFORMATION");
            }
        }

        // prints results
        System.out.println("\nRESULTS");
        System.out.println("\nWITH DEBUG INFORMATION\n");
        for (int step = 0; step < 2; ++step) {
            for (mode = 0; mode < 4; ++mode) {
                switch (mode) {
                    case 0:
                        System.out.print("NO ADAPT:     ");
                        break;
                    case 1:
                        System.out.print("NULL ADAPT:   ");
                        break;
                    case 2:
                        System.out.print("COMPUTE MAXS: ");
                        break;
                    default:
                        System.out.print("ADD COUNTER:  ");
                        break;
                }
                System.out.println((float) perfs[step][mode] + " ms");
            }
            if (step == 0) {
                System.out.println("\nWITHOUT DEBUG INFORMATION\n");
            }
        }
    }

    private static byte[] readClass(final InputStream is) throws IOException {
        if (is == null) {
            throw new IOException("Class not found");
        }
        byte[] b = new byte[is.available()];
        int len = 0;
        while (true) {
            int n = is.read(b, len, b.length - len);
            if (n == -1) {
                if (len < b.length) {
                    byte[] c = new byte[len];
                    System.arraycopy(b, 0, c, 0, len);
                    b = c;
                }
                return b;
            } else {
                len += n;
                if (len == b.length) {
                    byte[] c = new byte[b.length + 1000];
                    System.arraycopy(b, 0, c, 0, len);
                    b = c;
                }
            }
        }
    }

    protected Class findClass(final String name) throws ClassNotFoundException {
        try {
            byte[] b;
            String fileName = name.replace('.', '/') + ".class";
            InputStream is = zip.getInputStream(zip.getEntry(fileName));
            switch (mode) {
                case 0:
                    b = new byte[is.available()];
                    int len = 0;
                    while (true) {
                        int n = is.read(b, len, b.length - len);
                        if (n == -1) {
                            if (len < b.length) {
                                byte[] c = new byte[len];
                                System.arraycopy(b, 0, c, 0, len);
                                b = c;
                            }
                            break;
                        } else {
                            len += n;
                            if (len == b.length) {
                                byte[] c = new byte[b.length + 1000];
                                System.arraycopy(b, 0, c, 0, len);
                                b = c;
                            }
                        }
                    }
                    break;
                case 1:
                    compute = false;
                    skipDebug = false;
                    b = nullAdaptClass(is, name);
                    break;
                case 2:
                    compute = true;
                    skipDebug = false;
                    b = nullAdaptClass(is, name);
                    break;
                case 3:
                    b = counterAdaptClass(is, name);
                    break;
                // case 4:
                default:
                    compute = false;
                    skipDebug = true;
                    b = nullAdaptClass(is, name);
                    break;
            }
            if (dst != null) {
                dst.putNextEntry(new ZipEntry(fileName));
                dst.write(b, 0, b.length);
                dst.closeEntry();
            }
            total += 1;
            totalSize += b.length;
            return defineClass(name, b, 0, b.length);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException(name);
        }
    }

    private static void gc() {
        try {
            Runtime.getRuntime().gc();
            Thread.sleep(50);
            Runtime.getRuntime().gc();
            Thread.sleep(50);
            Runtime.getRuntime().gc();
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
    }

    abstract ALLPerfTest newInstance();

    abstract byte[] nullAdaptClass(final InputStream is, final String name)
            throws Exception;

    abstract byte[] counterAdaptClass(final InputStream is, final String name)
            throws Exception;
}
