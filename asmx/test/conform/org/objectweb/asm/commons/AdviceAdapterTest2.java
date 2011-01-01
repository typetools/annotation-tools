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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.AbstractTest;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Simple example of using AdviceAdapter to implement tracing callback 
 *
 * @author Eugene Kuleshov
 */
public class AdviceAdapterTest2 extends AbstractTest {

    public void test() throws Exception {
        Class c = getClass();
        String name = c.getName();
        AdvisingClassLoader cl = new AdvisingClassLoader(name+"$");
        Class cc = cl.loadClass(name+"$B");
        Method m = cc.getMethod("run", new Class[] {Integer.TYPE});
        try {
            m.invoke(null, new Object[] { new Integer(0)});
        } catch (InvocationTargetException e) {
            throw (Exception) e.getTargetException();
        }
    }

    
    private static class AdvisingClassLoader extends ClassLoader {
        private String prefix;

        public AdvisingClassLoader(String prefix) throws IOException {
            this.prefix = prefix;
        }
        
        public Class loadClass(String name) throws ClassNotFoundException {
            if(name.startsWith(prefix)) {
                try {
                    ClassWriter cw = new ClassWriter(true, true);
                    ClassReader cr = new ClassReader(getClass().getResourceAsStream( "/"+name.replace('.', '/')+".class"));
                    cr.accept(new AdviceClassAdapter(cw), false);
                    byte[] bytecode = cw.toByteArray();
                    return super.defineClass(name, bytecode, 0, bytecode.length);            
                } catch(IOException ex) {
                    throw new ClassNotFoundException( "Load error: "+ex.toString(), ex);
                }
            }
            return super.loadClass(name);
        }

    }

    
    // test callback
    private static int n = 0;
    public static void enter(String msg) {
        System.err.println(off().append("enter ").append(msg).toString());
        n++;
    }
    public static void exit(String msg) {
        n--;
        System.err.println(off().append("<").toString());
    }
    private static StringBuffer off() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++) {
            sb.append("  ");
        }
        return sb;
    }
    
    
    static class AdviceClassAdapter extends ClassAdapter implements Opcodes {
        private String cname;

        public AdviceClassAdapter(ClassVisitor cv) {
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
            this.cname = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        public MethodVisitor visitMethod(
            int access,
            final String name,
            final String desc,
            String signature,
            String[] exceptions)
        {
            MethodVisitor mv = cv.visitMethod(access,
                    name,
                    desc,
                    signature,
                    exceptions);

            if (mv == null
                    || (access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) > 0)
            {
                return mv;
            }

            return new AdviceAdapter(mv, access, name, desc) {
                    protected void onMethodEnter() {
                        mv.visitLdcInsn(cname+"."+name+desc);
                        mv.visitMethodInsn(INVOKESTATIC, 
                                "org/objectweb/asm/commons/AdviceAdapterTest2", 
                                "enter", "(Ljava/lang/String;)V");
                    }
    
                    protected void onMethodExit(int opcode) {
                        mv.visitLdcInsn(cname+"."+name+desc);
                        mv.visitMethodInsn(INVOKESTATIC, 
                                "org/objectweb/asm/commons/AdviceAdapterTest2", 
                                "exit", "(Ljava/lang/String;)V");
                    }
                    
                };
        }
    }


    // TEST CLASSES
    
    public static class A {
      final String s;

      public A(String s) {
        this.s = s;
      }
      public A(A a) {
        this.s = a.s;
      }
    }
    
    
    public static class B extends A {
      
      public B() {
        super(new B(""));
        test(this);
      }

      public B( A a) {
        super(a);
        test(this);
      }
      
      public B(String s) {
        super(s==null ? new A( "") : new A(s));
        test(this);
      }

      private static A aa;
      public B(String s, A a) {
        this(s==null ? aa = new A( s) : a);
        A aa = new A("");
        test(aa);
      }

      public B(String s, String s1) {
        super(s!=null ? new A( getA(s1).s) : new A( s) );
        test(this);
      }

      
      private void test(Object b) {
      }
      private static A getA(String s) {
        return new A(s);
      }
      
      // execute all
      public static void run(int n) {
          new B();
          new B( new A(""));
          new B( new B());
          new B( "", new A(""));
          new B( "", "");
      }
      
    }
    
}

