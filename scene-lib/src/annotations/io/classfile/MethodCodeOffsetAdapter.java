package annotations.io.classfile;

import java.util.Map;
import java.util.TreeMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class MethodCodeOffsetAdapter extends XMethodVisitor {
  private final ClassReader cr;
  private final Map<String, Integer> attributeOffsets;
  private final int codeStart;
  private int offset;
  private int previousOffset;

  public MethodCodeOffsetAdapter(ClassReader classReader,
      MethodVisitor methodVisitor, int start) {
    super(Opcodes.ASM5, methodVisitor);
    char[] buf = new char[classReader.header];
    attributeOffsets = new TreeMap<String, Integer>();
    previousOffset = -1;
    offset = 0;
    cr = classReader;
    // const pool size is (not lowest) upper bound of string length
    int ix = start;
    int attrCount = classReader.readUnsignedShort(ix + 6);

    // find code and type annotation attributes
    ix += 8;
    while (attrCount > 0) {
      String attrName = classReader.readUTF8(ix, buf);
      attributeOffsets.put(attrName, ix);
      ix += 6 + classReader.readInt(ix + 2);
      --attrCount;
    }
    codeStart = !attributeOffsets.containsKey("Code") ? 0
        : attributeOffsets.get("Code");
  }

  /**
   * 
   * @param i code offset at which to read int
   * @return int represented (big-endian) by four bytes starting at {@code i}
   */
  private int readInt(int i) {
    return cr.readInt(codeStart + i);
  }

  /**
   * Record current {@link #offset} as {@link #previousOffset} and
   *  increment by {@code i}.
   */
  private void advance(int i) {
    previousOffset = offset;
    offset += i;
  }

  /**
   * @return class offset of code attribute; 0 for abstract methods
   */
  public int getCodeStart() {
    return codeStart;
  }

  /**
   * @return offset of instruction just visited; -1 before first visit
   */
  public int getPreviousOffset() {
    return previousOffset;
  }

  /**
   * @return offset after instruction just visited; 0 before first
   *          visit, -1 if {@link #visitEnd()} has been called
   */
  public int getCurrentOffset() {
    return offset;
  }

  @Override
  public void visitFieldInsn(int opcode,
      String owner, String name, String desc) {
    super.visitFieldInsn(opcode, owner, name, desc);
    advance(3);
  }

  @Override
  public void visitIincInsn(int var, int increment) {
    super.visitIincInsn(var, increment);
    advance(3);
  }

  @Override
  public void visitInsn(int opcode) {
    super.visitInsn(opcode);
    advance(1);
  }

  @Override
  public void visitIntInsn(int opcode, int operand) {
    super.visitIntInsn(opcode, operand);
    advance(opcode == Opcodes.SIPUSH ? 3 : 2);
  }

  @Override
  public void visitInvokeDynamicInsn(String name, String desc,
      Handle bsm, Object... bsmArgs) {
    super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    advance(5);
  }

  @Override
  public void visitJumpInsn(int opcode, Label label) {
    super.visitJumpInsn(opcode, label);
    advance(3);
  }

  @Override
  public void visitLdcInsn(Object cst) {
    super.visitLdcInsn(cst);
    advance(2);
  }

  @Override
  public void visitLookupSwitchInsn(Label dflt, int[] keys,
      Label[] labels) {
    super.visitLookupSwitchInsn(dflt, keys, labels);
    previousOffset = offset;
    offset += 8 - ((offset - codeStart) & 3);
    offset += 4 + 8 * readInt(offset);
  }

  @Override
  public void visitMethodInsn(int opcode,
      String owner, String name, String desc, boolean itf) {
    super.visitMethodInsn(opcode, owner, name, desc, itf);
    advance(opcode == Opcodes.INVOKEINTERFACE ? 5 : 3);
  }

  @Override
  public void visitMultiANewArrayInsn(String desc, int dims) {
    super.visitMultiANewArrayInsn(desc, dims);
    advance(4);
  }

  @Override
  public void visitTableSwitchInsn(int min, int max,
      Label dflt, Label... labels) {
    super.visitTableSwitchInsn(min, max, dflt, labels);
    previousOffset = offset;
    offset += 8 - ((offset - codeStart) & 3);
    offset += 4 * (readInt(offset + 4) - readInt(offset) + 3);
  }

  @Override
  public void visitTypeInsn(int opcode, String desc) {
    super.visitTypeInsn(opcode, desc);
    advance(3);
  }

  @Override
  public void visitVarInsn(int opcode, int var) {
    super.visitVarInsn(opcode, var);
    advance(var < 4 ? 1 : 2);
  }

  @Override
  public void visitEnd() {
//    super.visitEnd();  // ???
    offset = -1;  // invalidated
  }
}
