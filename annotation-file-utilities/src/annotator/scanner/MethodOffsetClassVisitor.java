package annotator.scanner;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sun.tools.javac.util.Pair;

import annotations.io.classfile.CodeOffsetAdapter;


/**
 * MethodOffsetClassVisitor is a class visitor that should be passed to
 * ASM's ClassReader in order to retrieve extra information about method
 * offsets needed by all of the annotator.scanner classes.  This visitor
 * should visit every class that is to be annotated, and should be done
 * before trying to match elements in the tree to the various criterion.
 */
public class MethodOffsetClassVisitor extends ClassVisitor {
  CodeOffsetAdapter coa;
  MethodVisitor mcoa;

  // This field should be set by entry on a method through visitMethod,
  // and so all the visit* methods in LocalVariableMethodVisitor
  private String methodName;

  public MethodOffsetClassVisitor(ClassReader cr) {
    super(Opcodes.ASM5);  // COMPUTE_MAXS?
    this.methodName = "LocalVariableVisitor: DEFAULT_METHOD";
    coa = new CodeOffsetAdapter(cr);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name,
        String desc, String signature, String[  ] exceptions) {
    methodName = name + desc.substring(0, desc.indexOf(")") + 1);
    mcoa = coa.visitMethod(access, name, desc, signature, exceptions);
    return new MethodOffsetMethodVisitor(
        super.visitMethod(access, name, desc, signature, exceptions));
  }

  /**
   * MethodOffsetMethodVisitor is the method visitor that
   * MethodOffsetClassVisitor uses to visit particular methods and gather
   * all the offset information by calling the appropriate static
   * methods in annotator.scanner classes.
   */
  private class MethodOffsetMethodVisitor extends MethodVisitor {
    public MethodOffsetMethodVisitor(MethodVisitor mv) {
      super(Opcodes.ASM5, mv);
    }

    /**
     * @return the current scan position, given as an offset from the
     * beginning of the method's code attribute
     */
    public int getOffset() {
      return coa.getMethodCodeOffset();
    }

    @Override
    public void visitLocalVariable(String name, String desc,
          String signature, Label start, Label end, int index)  {
      super.visitLocalVariable(name, desc, signature, start, end, index);
      LocalVariableScanner.addToMethodNameIndexMap(
          Pair.of(methodName, Pair.of(index, start.getOffset())),
          name);
      LocalVariableScanner.addToMethodNameCounter(
          methodName, name, start.getOffset());
      mcoa.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitLabel(Label label) {
      super.visitLabel(label);
      mcoa.visitLabel(label);
    }

    @Override
    public void visitTypeInsn(int opcode,  String desc)   {
      super.visitTypeInsn(opcode, desc);
      switch (opcode) {
      case Opcodes.CHECKCAST:
        CastScanner.addCastToMethod(methodName,
            getOffset());
        break;
      case Opcodes.NEW:
      case Opcodes.ANEWARRAY:
        NewScanner.addNewToMethod(methodName, getOffset());
        break;
      case Opcodes.INSTANCEOF:
        InstanceOfScanner.addInstanceOfToMethod(methodName,
            getOffset());
        break;
      }
      mcoa.visitTypeInsn(opcode, desc);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims)  {
      super.visitMultiANewArrayInsn(desc, dims);
      NewScanner.addNewToMethod(methodName, getOffset());
      mcoa.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitIntInsn(int opcode, int operand)  {
      super.visitIntInsn(opcode, operand);
      if (opcode == Opcodes.NEWARRAY) {
        NewScanner.addNewToMethod(methodName, getOffset());
      }
      mcoa.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
        String desc, boolean itf) {
      super.visitMethodInsn(opcode, owner, name, desc, itf);
      switch (opcode) {
      case Opcodes.INVOKEINTERFACE:
      case Opcodes.INVOKESTATIC:
      case Opcodes.INVOKEVIRTUAL:
        MethodCallScanner.addMethodCallToMethod(methodName,
            getOffset());
        // fall through
      default:
        break;
      }
      mcoa.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc,
        Handle bsm, Object... bsmArgs) {
      super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
      LambdaScanner.addLambdaExpressionToMethod(methodName,
          getOffset());
      mcoa.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitCode() {
      super.visitCode();
      mcoa.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
      super.visitInsn(opcode);
      mcoa.visitInsn(opcode);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      super.visitVarInsn(opcode, var);
      mcoa.visitVarInsn(opcode, var);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name,
        String desc) {
      super.visitFieldInsn(opcode, owner, name, desc);
      mcoa.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      super.visitJumpInsn(opcode, label);
      mcoa.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object cst) {
      super.visitLdcInsn(cst);
      mcoa.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
      super.visitIincInsn(var, increment);
      mcoa.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt,
        Label... labels) {
      super.visitTableSwitchInsn(min, max, dflt, labels);
      mcoa.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      super.visitLookupSwitchInsn(dflt, keys, labels);
      mcoa.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitEnd() {
      super.visitEnd();
      mcoa.visitEnd();
    }
  }
}
