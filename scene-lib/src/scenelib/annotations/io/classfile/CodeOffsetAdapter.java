package scenelib.annotations.io.classfile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import scenelib.annotations.io.DebugWriter;

public class CodeOffsetAdapter extends ClassVisitor {
  static final DebugWriter debug;
  final ClassReader classReader;
  final char[] buffer;
  int methodStart;
  int codeStart;
  int offset;

  static {
    debug = new DebugWriter();
    debug.setEnabled(false);
  }

  public CodeOffsetAdapter(int api, ClassReader classReader) {
    this(api, classReader, null);
  }

  public CodeOffsetAdapter(int api, ClassReader classReader, ClassVisitor v) {
    super(api, v);
    this.classReader = classReader;
    // const pool size is (not lowest) upper bound of string length
    buffer = new char[classReader.header];
    methodStart = classReader.header + 6;
    methodStart += 4 + 2 * classReader.readUnsignedShort(methodStart);
    for (int i = classReader.readUnsignedShort(methodStart-2); i > 0; --i) {
      methodStart += 8;
      for (int j = classReader.readUnsignedShort(methodStart-2); j > 0; --j) {
        methodStart += 6 + classReader.readInt(methodStart+2);
      }
    }
    methodStart += 2;
  }

  @Override
  public MethodVisitor visitMethod(int access,
      String name, String descriptor,
      String signature, String[] exceptions) {
    MethodVisitor methodVisitor =
        super.visitMethod(access, name, descriptor, signature, exceptions);
    return new MethodVisitor(api, methodVisitor) {
      private int methodEnd;

      {
        String name = classReader.readUTF8(methodStart + 2, buffer);
        String descriptor = classReader.readUTF8(methodStart + 4, buffer);
        int attrCount = classReader.readUnsignedShort(methodStart + 6);
        debug.debug("visiting %s%s (%d)%n", name, descriptor, methodStart);
        debug.debug("%d attributes%n", attrCount);
        methodEnd = methodStart + 8;

        // find code attribute
        codeStart = methodEnd;
        if (attrCount > 0) {
          while (--attrCount >= 0) {
            String attrName = classReader.readUTF8(codeStart, buffer);
            debug.debug("attribute %s%n", attrName);
            if ("Code".equals(attrName)) {
              codeStart += 6;
              offset = codeStart + classReader.readInt(codeStart - 4);
              codeStart += 8;
              while (--attrCount >= 0) {
                debug.debug("attribute %s%n", classReader.readUTF8(offset, buffer));
                offset += 6 + classReader.readInt(offset + 2);
              }
              methodEnd = offset;
              break;
            }
            codeStart += 6 + classReader.readInt(codeStart + 2);
            methodEnd = codeStart;
          }
        }
        offset = 0;
      }

      private int readInt(int i) {
        return classReader.readInt(codeStart + i);
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, owner, name, descriptor);
        debug.debug("%d visitFieldInsn(%d, %s, %s, %s)%n", offset, opcode, owner, name, descriptor);
        offset += 3;
      }

      @Override
      public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
        debug.debug("%d visitIincInsn(%d, %d)%n", offset, var, increment);
        offset += 3;
      }

      @Override
      public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        debug.debug("%d visitInsn(%d)%n", offset, opcode);
        ++offset;
      }

      @Override
      public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
        debug.debug("%d visitIntInsn(%d, %d)%n", offset, opcode, operand);
        offset += opcode == Opcodes.SIPUSH ? 3 : 2;
      }

      @Override
      public void visitInvokeDynamicInsn(String name, String descriptor,
            Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name, descriptor, bsm, bsmArgs);
        debug.debug("%d visitInvokeDynamicInsn(%s, %s)%n", offset,
            name, descriptor, bsm, bsmArgs);
        offset += 5;
      }

      @Override
      public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        debug.debug("%d visitJumpInsn(%d, %s)%n", offset, opcode, label);
        // account for wide instructions goto_w (200) and jsr_w (201)
        offset += classReader.readByte(codeStart + offset) < 200 ? 3 : 4;
        assert offset > 0 && methodEnd > codeStart + offset;
      }

      @Override
      public void visitLdcInsn(Object cst) {
        super.visitLdcInsn(cst);
        debug.debug("%d visitLdcInsn(%s)%n", offset, cst);
        // account for wide instructions ldc_w (19) and ldc2_w (20)
        offset += classReader.readByte(codeStart + offset) > 18 ? 3 : 2;
        assert offset > 0 && methodEnd > codeStart + offset;
      }

      @Override
      public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels);
        debug.debug("%d visitLookupSwitchInsn(%s)%n", offset, dflt, keys, labels);
        offset += 8 - (offset & 3);
        offset += 4 + 8 * readInt(offset);
        assert offset > 0 && methodEnd > codeStart + offset;
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        super.visitMethodInsn(opcode, owner, name, descriptor);
        debug.debug("%d visitMethodInsn(%d, %s, %s, %s)%n", offset, opcode, owner, name, descriptor);
        offset += opcode == Opcodes.INVOKEINTERFACE ? 5 : 3;
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        debug.debug("%d visitMethodInsn(%d, %s, %s, %s, %s)%n", offset, opcode, owner, name, descriptor, isInterface);
        offset += opcode == Opcodes.INVOKEINTERFACE ? 5 : 3;
      }

      @Override
      public void visitMultiANewArrayInsn(String descriptor, int dims) {
        super.visitMultiANewArrayInsn(descriptor, dims);
        debug.debug("%d visitMultiANewArrayInsn(%s, %d)%n", offset,
            descriptor, dims);
        offset += 4;
      }

      @Override
      public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels);
        debug.debug("%d visitTableSwitchInsn(%d, %d, %s)%n", offset, min, max, dflt, labels);
        offset += 8 - (offset & 3);
        offset += 4 * (readInt(offset + 4) - readInt(offset) + 3);
        assert offset > 0 && methodEnd > codeStart + offset;
      }

      @Override
      public void visitTypeInsn(int opcode, String descriptor) {
        super.visitTypeInsn(opcode, descriptor);
        debug.debug("%d visitTypeInsn(%d, %s)%n", offset, opcode, descriptor);
        offset += 3;
      }

      @Override
      public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
        debug.debug("%d visitVarInsn(%d, %d)%n", offset, opcode, var);
        offset += var < 4 ? 1 : 2;
      }

      @Override
      public void visitEnd() {
        methodStart = methodEnd;
      }
    };
  }

  public int getMethodCodeOffset() { return offset; }

  public int getBytecodeOffset() { return codeStart + offset; }
}
