package annotator.scanner;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypeAnnotationVisitor;
import org.objectweb.asm.TypePath;

import scenelib.annotations.io.classfile.CodeOffsetAdapter;

import com.sun.tools.javac.util.Pair;


/**
 * MethodOffsetClassVisitor is a class visitor that should be passed to
 * ASM's ClassReader in order to retrieve extra information about method
 * offsets needed by all of the annotator.scanner classes.  This visitor
 * should visit every class that is to be annotated, and should be done
 * before trying to match elements in the tree to the various criterion.
 */
// Note: in order to ensure all labels are visited, this class
// needs to extend ClassWriter and not other class visitor classes.
// There is no good reason why this is the case with ASM.
public class MethodOffsetClassVisitor extends ClassWriter {
  CodeOffsetAdapter coa;
  MethodVisitor mcoa;

  // This field should be set by entry on a method through visitMethod,
  // and so all the visit* methods in LocalVariableMethodVisitor
  private String methodName;

  public MethodOffsetClassVisitor(ClassReader cr) {
    super(true, false);
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
  private class MethodOffsetMethodVisitor extends MethodAdapter {
    private Label lastLabel;

    public MethodOffsetMethodVisitor(MethodVisitor mv) {
      super(mv);
      lastLabel = null;
    }

    private int labelOffset() {
      try {
        return lastLabel.getOffset();
      } catch (Exception ex) {
        return 0;  // TODO: find a better default?
      }
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
      lastLabel = label;
      mcoa.visitLabel(label);
    }

    @Override
    public void visitTypeInsn(int opcode,  String desc)   {
      super.visitTypeInsn(opcode, desc);
      switch (opcode) {
      case Opcodes.CHECKCAST:
        // CastScanner.addCastToMethod(methodName, labelOffset() + 1);
        CastScanner.addCastToMethod(methodName,
            coa.getMethodCodeOffset());
        break;
      case Opcodes.NEW:
      case Opcodes.ANEWARRAY:
        NewScanner.addNewToMethod(methodName, labelOffset());
        break;
      case Opcodes.INSTANCEOF:
        InstanceOfScanner.addInstanceOfToMethod(methodName,
            labelOffset() + 1);
        break;
      }
      mcoa.visitTypeInsn(opcode, desc);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims)  {
      super.visitMultiANewArrayInsn(desc, dims);
      NewScanner.addNewToMethod(methodName, labelOffset());
      mcoa.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitIntInsn(int opcode, int operand)  {
      super.visitIntInsn(opcode, operand);
      if (opcode == Opcodes.NEWARRAY) {
        NewScanner.addNewToMethod(methodName, labelOffset());
      }
      mcoa.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
        String desc) {
      super.visitMethodInsn(opcode, owner, name, desc);
      switch (opcode) {
      case Opcodes.INVOKEINTERFACE:
      case Opcodes.INVOKESTATIC:
      case Opcodes.INVOKEVIRTUAL:
        MethodCallScanner.addMethodCallToMethod(methodName,
            labelOffset());
        break;
      default:
        break;
      }
      mcoa.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc,
        Handle bsm, Object... bsmArgs) {
      super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
      LambdaScanner.addLambdaExpressionToMethod(methodName,
          labelOffset());
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
        Label[] labels) {
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
