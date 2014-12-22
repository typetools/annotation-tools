package org.objectweb.asm.tree;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A node that represents an INVOKEDYNAMIC instruction.
 */
public class InvokeDynamicInsnNode extends AbstractInsnNode {

  /**
   * Index of call site specifier in the constant pool.
   */
  public int index;

  public InvokeDynamicInsnNode(int ix1, int ix2) {
      this(((ix1 << 8) & 0xff) | (ix2 & 0xff));
  }

  public InvokeDynamicInsnNode(int index) {
      super(Opcodes.INVOKEDYNAMIC);
      this.index = index;
  }

  public void accept(final MethodVisitor mv) {
      mv.visitInvokeDynamicInsn((index >> 8) & 0xff, index & 0xff);
  }

  public int getType() {
      return MULTIANEWARRAY_INSN;
  }
}
