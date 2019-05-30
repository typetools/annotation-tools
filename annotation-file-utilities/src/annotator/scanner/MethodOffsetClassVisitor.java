package annotator.scanner;

import org.objectweb.asmx.AnnotationVisitor;
import org.objectweb.asmx.Attribute;
import org.objectweb.asmx.ClassReader;
import org.objectweb.asmx.ClassWriter;
import org.objectweb.asmx.Handle;
import org.objectweb.asmx.Label;
import org.objectweb.asmx.MethodAdapter;
import org.objectweb.asmx.MethodVisitor;
import org.objectweb.asmx.Opcodes;
import org.objectweb.asmx.TypeAnnotationVisitor;
import org.objectweb.asmx.TypePath;

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
  CodeOffsetAdapter codeOffsetAdapter;
  MethodVisitor methodOffsetClassVisitor;

  // This field should be set by entry on a method through visitMethod,
  // and so all the visit* methods in LocalVariableMethodVisitor
  private String methodName;

  public MethodOffsetClassVisitor(ClassReader classReader) {
    super(true, false);
    this.methodName = "LocalVariableVisitor: DEFAULT_METHOD";
    codeOffsetAdapter = new CodeOffsetAdapter(classReader);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name,
        String descriptor, String signature, String[  ] exceptions) {
    methodName = name + descriptor.substring(0, descriptor.indexOf(")") + 1);
    methodOffsetClassVisitor = codeOffsetAdapter.visitMethod(access, name, descriptor, signature, exceptions);
    return new MethodOffsetMethodVisitor(
        super.visitMethod(access, name, descriptor, signature, exceptions));
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
    public void visitLocalVariable(String name, String descriptor,
          String signature, Label start, Label end, int index)  {
      super.visitLocalVariable(name, descriptor, signature, start, end, index);
      LocalVariableScanner.addToMethodNameIndexMap(
          Pair.of(methodName, Pair.of(index, start.getOffset())),
          name);
      LocalVariableScanner.addToMethodNameCounter(
          methodName, name, start.getOffset());
      methodOffsetClassVisitor.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public void visitLabel(Label label) {
      super.visitLabel(label);
      lastLabel = label;
      methodOffsetClassVisitor.visitLabel(label);
    }

    @Override
    public void visitTypeInsn(int opcode,  String descriptor)   {
      super.visitTypeInsn(opcode, descriptor);
      switch (opcode) {
      case Opcodes.CHECKCAST:
        // CastScanner.addCastToMethod(methodName, labelOffset() + 1);
        CastScanner.addCastToMethod(methodName,
            codeOffsetAdapter.getMethodCodeOffset());
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
      methodOffsetClassVisitor.visitTypeInsn(opcode, descriptor);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int dims)  {
      super.visitMultiANewArrayInsn(descriptor, dims);
      NewScanner.addNewToMethod(methodName, labelOffset());
      methodOffsetClassVisitor.visitMultiANewArrayInsn(descriptor, dims);
    }

    @Override
    public void visitIntInsn(int opcode, int operand)  {
      super.visitIntInsn(opcode, operand);
      if (opcode == Opcodes.NEWARRAY) {
        NewScanner.addNewToMethod(methodName, labelOffset());
      }
      methodOffsetClassVisitor.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
        String descriptor) {
      super.visitMethodInsn(opcode, owner, name, descriptor);
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
      methodOffsetClassVisitor.visitMethodInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor,
        Handle bsm, Object... bsmArgs) {
      super.visitInvokeDynamicInsn(name, descriptor, bsm, bsmArgs);
      LambdaScanner.addLambdaExpressionToMethod(methodName,
          labelOffset());
      methodOffsetClassVisitor.visitInvokeDynamicInsn(name, descriptor, bsm, bsmArgs);
    }

    @Override
    public void visitCode() {
      super.visitCode();
      methodOffsetClassVisitor.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
      super.visitInsn(opcode);
      methodOffsetClassVisitor.visitInsn(opcode);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      super.visitVarInsn(opcode, var);
      methodOffsetClassVisitor.visitVarInsn(opcode, var);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name,
        String descriptor) {
      super.visitFieldInsn(opcode, owner, name, descriptor);
      methodOffsetClassVisitor.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      super.visitJumpInsn(opcode, label);
      methodOffsetClassVisitor.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object cst) {
      super.visitLdcInsn(cst);
      methodOffsetClassVisitor.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
      super.visitIincInsn(var, increment);
      methodOffsetClassVisitor.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt,
        Label[] labels) {
      super.visitTableSwitchInsn(min, max, dflt, labels);
      methodOffsetClassVisitor.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      super.visitLookupSwitchInsn(dflt, keys, labels);
      methodOffsetClassVisitor.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitEnd() {
      super.visitEnd();
      methodOffsetClassVisitor.visitEnd();
    }
  }
}
