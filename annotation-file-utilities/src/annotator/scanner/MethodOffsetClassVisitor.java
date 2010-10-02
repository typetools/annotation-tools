package annotator.scanner;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sun.tools.javac.util.Pair;


/**
 * MethodOffsetClassVisitor is a class visitor that can should be passed to
 * ASM's ClassReader in order to retrieve extra information about method offsets
 * needed by all of the annotator.scanner classes.  This visitor should visit
 * every class that is to be annotated, and should be done before trying
 * to match elements in the tree to the various criterion.
 */
// Note: in order to ensure all labels are visited, this class
// needs to extend ClassWriter and not other class visitor classes.
// There is no good reason why this is the case with ASM.
public class MethodOffsetClassVisitor extends ClassWriter {

  // This field should be set by entry on a method through visitMethod,
  // and so all the visit* methods in LocalVariableMethodVisitor
  private String methodName;

  public MethodOffsetClassVisitor() {
    super(true, false);
    this.methodName = "LocalVariableVisitor: DEFAULT_METHOD";
  }

  @Override
  public MethodVisitor visitMethod(int access, String name,
        String desc, String signature, String[  ] exceptions) {
    methodName = name + desc.substring(0, desc.indexOf(")") + 1);
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

    @Override
    public void visitLocalVariable(String name, String desc,
          String signature, Label start, Label end, int index)  {
      super.visitLocalVariable(name, desc, signature, start, end, index);
      LocalVariableScanner.addToMethodNameIndexMap(
          Pair.of(methodName, Pair.of(index, start.getOffset())),
          name);
      LocalVariableScanner.addToMethodNameCounter(
          methodName, name, start.getOffset());
    }

    @Override
    public void visitLabel(Label label) {
      super.visitLabel(label);
      lastLabel = label;
    }

    @Override
    public void visitTypeInsn(int opcode,  String desc)   {
      super.visitTypeInsn(opcode, desc);
      if (opcode == Opcodes.CHECKCAST) {
        CastScanner.addCastToMethod(methodName, lastLabel.getOffset() + 1);
      }

      if (opcode == Opcodes.NEW || opcode == Opcodes.ANEWARRAY) {
        NewScanner.addNewToMethod(methodName, lastLabel.getOffset());
      }

      if (opcode == Opcodes.INSTANCEOF) {
        InstanceOfScanner.addInstanceOfToMethod(methodName,
            lastLabel.getOffset() + 1);
      }
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims)  {
      super.visitMultiANewArrayInsn(desc, dims);
      NewScanner.addNewToMethod(methodName, lastLabel.getOffset());
    }

    @Override
    public void visitIntInsn(int opcode, int operand)  {
      super.visitIntInsn(opcode, operand);
      if (opcode == Opcodes.NEWARRAY) {
        NewScanner.addNewToMethod(methodName, lastLabel.getOffset());
      }
    }
  }
}
