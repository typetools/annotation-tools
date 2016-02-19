package annotations.io.classfile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import annotations.io.DebugWriter;

/**
 * Tracks offset within a method's Code attribute as its instructions
 * are visited.  ASM really should expose this information but doesn't.
 *
 * @author dbro
 */
public class CodeOffsetAdapter extends ClassVisitor {
  static final DebugWriter debug;
  final ClassReader cr;
  final char[] buf;
  int methodStart;
  int codeStart;
  int offset;
  int previousOffset;

  static {
    debug = new DebugWriter();
    debug.setEnabled(false);
  }

  // For some reason, it is necessary to use ClassWriter to ensure that
  // all labels are visited.
  public CodeOffsetAdapter(ClassReader cr) {
    super(Opcodes.ASM5, new ClassWriter(cr, 0));
    this.cr = cr;
    // const pool size is (not lowest) upper bound of string length
    buf = new char[cr.header];
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

  @Override
  public MethodVisitor visitMethod(int access,
      String name, String desc,
      String signature, String[] exceptions) {
    return new XMethodVisitor(Opcodes.ASM5,
        super.visitMethod(access, name, desc, signature, exceptions)) {
      private int methodEnd;

      {
        String name = cr.readUTF8(methodStart + 2, buf);
        String desc = cr.readUTF8(methodStart + 4, buf);
        int attrCount = cr.readUnsignedShort(methodStart + 6);
        debug.debug("visiting %s%s (%d)%n", name, desc, methodStart);
        debug.debug("%d attributes%n", attrCount);
        methodEnd = methodStart + 8;

        // find code attribute
        codeStart = methodEnd;
        if (attrCount > 0) {
          while (--attrCount >= 0) {
            String attrName = cr.readUTF8(codeStart, buf);
            debug.debug("attribute %s%n", attrName);
            if ("Code".equals(attrName)) {
              codeStart += 6;
              offset = codeStart + cr.readInt(codeStart - 4);
              codeStart += 8;
              while (--attrCount >= 0) {
                debug.debug("attribute %s%n", cr.readUTF8(offset, buf));
                offset += 6 + cr.readInt(offset + 2);
              }
              methodEnd = offset;
              break;
            }
            codeStart += 6 + cr.readInt(codeStart + 2);
            methodEnd = codeStart;
          }
        }
        offset = 0;
        previousOffset = -1;
      }

      private int readInt(int i) {
        return cr.readInt(codeStart + i);
      }

      @Override
      public void visitLabel(Label label) {
        super.visitLabel(label);
      }

      @Override
      public void visitFieldInsn(int opcode,
          String owner, String name, String desc) {
        super.visitFieldInsn(opcode, owner, name, desc);
        debug.debug("%d visitFieldInsn(%d, %s, %s, %s)%n", offset,
            opcode, owner, name, desc);
        advance(3);
      }

      @Override
      public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
        debug.debug("%d visitIincInsn(%d, %d)%n", offset, var, increment);
        advance(3);
      }

      @Override
      public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        debug.debug("%d visitInsn(%d)%n", offset, opcode);
        advance(1);
      }

      @Override
      public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
        debug.debug("%d visitIntInsn(%d, %d)%n", offset, opcode, operand);
        advance(opcode == Opcodes.SIPUSH ? 3 : 2);
      }

      @Override
      public void visitInvokeDynamicInsn(String name, String desc,
          Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        debug.debug("%d visitInvokeDynamicInsn(%s, %s)%n", offset,
            name, desc, bsm, bsmArgs);
        advance(5);
      }

      @Override
      public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        debug.debug("%d visitJumpInsn(%d, %s)%n", offset, opcode, label);
        // account for wide instructions goto_w (200) and jsr_w (201)
        advance(cr.readByte(codeStart + offset) < 200 ? 3 : 4);
        assert offset > 0 && methodEnd > codeStart + offset;
      }

      @Override
      public void visitLdcInsn(Object cst) {
        super.visitLdcInsn(cst);
        debug.debug("%d visitLdcInsn(%s)%n", offset, cst);
        // account for wide instructions ldc_w (19) and ldc2_w (20)
        advance(cr.readByte(codeStart + offset) > 18 ? 3 : 2);
        assert offset > 0 && methodEnd > codeStart + offset;
      }

      @Override
      public void visitLookupSwitchInsn(Label dflt, int[] keys,
          Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels);
        debug.debug("%d visitLookupSwitchInsn(%s)%n", offset,
            dflt, keys, labels);
        previousOffset = offset;
        offset += 8 - (offset & 3);
        offset += 4 + 8 * readInt(offset);
        assert offset > 0 && methodEnd > codeStart + offset;
      }

      @Override
      public void visitMethodInsn(int opcode,
          String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        debug.debug("%d visitMethodInsn(%d, %s, %s, %s)%n", offset,
            opcode, owner, name, desc);
        advance(opcode == Opcodes.INVOKEINTERFACE ? 5 : 3);
      }

      @Override
      public void visitMultiANewArrayInsn(String desc, int dims) {
        super.visitMultiANewArrayInsn(desc, dims);
        debug.debug("%d visitMultiANewArrayInsn(%s, %d)%n", offset,
            desc, dims);
        advance(4);
      }

      @Override
      public void visitTableSwitchInsn(int min, int max,
          Label dflt, Label... labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels);
        debug.debug("%d visitTableSwitchInsn(%d, %d, %s)%n", offset,
            min, max, dflt, labels);
        previousOffset = offset;
        offset += 8 - (offset & 3);
        offset += 4 * (readInt(offset + 4) - readInt(offset) + 3);
        assert offset > 0 && methodEnd > codeStart + offset;
      }

      @Override
      public void visitTypeInsn(int opcode, String desc) {
        super.visitTypeInsn(opcode, desc);
        debug.debug("%d visitTypeInsn(%d, %s)%n", offset, opcode, desc);
        advance(3);
      }

      @Override
      public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
        debug.debug("%d visitVarInsn(%d, %d)%n", offset, opcode, var);
        advance(var < 4 ? 1 : 2);
      }

      @Override
      public void visitEnd() {
        super.visitEnd();
        methodStart = methodEnd;
      }
    };
  }

  public int getPreviousCodeOffset() { return previousOffset; }

  public int getMethodCodeOffset() { return offset; }

  public int getBytecodeOffset() { return codeStart + offset; }

  private void advance(int n) {
    previousOffset = offset;
    offset += n;
  }
}
