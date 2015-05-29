package annotations.io.classfile;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import annotations.util.AbstractClassVisitor;

public class CodeOffsetAdapter extends ClassAdapter {
  final ClassReader cr;
  final char[] buf;
  final int version;
  int methodStart;
  int codeStart;
  int offset;

  public CodeOffsetAdapter(ClassReader cr) {
    this(cr, new AbstractClassVisitor());
  }

  public CodeOffsetAdapter(ClassReader cr, ClassVisitor v) {
    super(v);
    this.cr = cr;

    // const pool size is (not lowest) upper bound of string length
    buf = new char[cr.header];
    version = cr.readUnsignedShort(6);
    methodStart = cr.header + 6;
    methodStart += 4 + 2 * cr.readUnsignedShort(methodStart);
    for (int i = cr.readUnsignedShort(methodStart-2); i > 0; --i) {
      methodStart += 8;
      for (int j = cr.readUnsignedShort(methodStart-2); j > 0; --j) {
        methodStart += 6 + cr.readInt(methodStart+2);
      }
    }
    methodStart += 2;
  }

  public ClassVisitor getDelegate() {
    return cv;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    MethodVisitor v =
        super.visitMethod(access, name, desc, signature, exceptions);
    return new MethodAdapter(v) {
      private int methodEnd = 0;

      private int readInt(int i) {
        return cr.readInt(codeStart + i);
      }

      @Override
      public void visitCode() {
        super.visitCode();
        int attrCount = cr.readUnsignedShort(methodStart + 6);

        // find code attribute
        codeStart = methodStart + 8;
        if (attrCount > 0) {
          while (--attrCount >= 0) {
            String attrName = cr.readUTF8(codeStart, buf);
            if ("Code".equals(attrName)) {
              offset = 6 + codeStart + cr.readInt(codeStart + 2);
              while (--attrCount >= 0) {
                offset += 6 + cr.readInt(offset + 2);
              }
              methodEnd = offset;
              offset = 0;
              codeStart += 14;
              return;
            }
            codeStart += 6 + cr.readInt(codeStart + 2);
          }
        }
        methodEnd = 0;
        offset = 0;
      }

      @Override
      public void visitFieldInsn(int opcode,
          String owner, String name, String desc) {
//System.err.println(offset + ": visitFieldInsn");
        super.visitFieldInsn(opcode, owner, name, desc);
        offset += 3;
      }

      @Override
      public void visitIincInsn(int var, int increment) {
//System.err.println(offset + ": visitIincInsn");
        super.visitIincInsn(var, increment);
        offset += 3;
      }

      @Override
      public void visitInsn(int opcode) {
//System.err.println(offset + ": visitInsn");
        super.visitInsn(opcode);
        ++offset;
      }

      @Override
      public void visitIntInsn(int opcode, int operand) {
//System.err.println(offset + ": visitIntInsn");
        super.visitIntInsn(opcode, operand);
        offset += opcode == Opcodes.SIPUSH ? 3 : 2;
      }

      @Override
      public void visitInvokeDynamicInsn(String name, String desc,
          Handle bsm, Object... bsmArgs) {
//System.err.println(offset + ": visitInvokeDynamicInsn");
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        offset += 5;
      }

      @Override
      public void visitJumpInsn(int opcode, Label label) {
//System.err.println(offset + ": visitJumpInsn");
        super.visitJumpInsn(opcode, label);
        offset += 3;
      }

      @Override
      public void visitLdcInsn(Object cst) {
//System.err.println(offset + ": visitLdcInsn");
        super.visitLdcInsn(cst);
        offset += 2;
      }

      @Override
      public void visitLookupSwitchInsn(Label dflt, int[] keys,
          Label[] labels) {
//System.err.println(offset + ": visitLookupSwitchInsn");
        super.visitLookupSwitchInsn(dflt, keys, labels);
        offset += 8 - (offset & 3);
        offset += 4 + 8 * readInt(offset);
      }

      @Override
      public void visitMethodInsn(int opcode,
          String owner, String name, String desc) {
//System.err.println(offset + ": visitMethodInsn");
        super.visitMethodInsn(opcode, owner, name, desc);
        offset += opcode == Opcodes.INVOKEINTERFACE ? 5 : 3;
      }

      @Override
      public void visitMultiANewArrayInsn(String desc, int dims) {
//System.err.println(offset + ": visitMultiANewArrayInsn");
        super.visitMultiANewArrayInsn(desc, dims);
        offset += 4;
      }

      @Override
      public void visitTableSwitchInsn(int min, int max,
          Label dflt, Label[] labels) {
//System.err.println(offset + ": visitTableSwitchInsn");
        super.visitTableSwitchInsn(min, max, dflt, labels);
        offset += 8 - (offset & 3);
        offset += 4 * (readInt(offset + 4) - readInt(offset) + 3);
      }

      @Override
      public void visitTypeInsn(int opcode, String desc) {
//System.err.println(offset + ": visitTypeInsn");
        super.visitTypeInsn(opcode, desc);
        offset += 3;
      }

      @Override
      public void visitVarInsn(int opcode, int var) {
//System.err.println(offset + ": visitVarInsn");
        super.visitVarInsn(opcode, var);
        offset += var < 4 ? 1 : 2;
      }

      @Override
      public void visitEnd() {
        methodStart = methodEnd > 0 ? methodEnd : methodStart + offset;
      }
    };
  }

  public int getMethodCodeOffset() { return offset; }

  public int getBytecodeOffset() { return codeStart + offset; }
}
