package scenelib.annotations.util;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypeAnnotationVisitor;
import org.objectweb.asm.TypePath;

import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;

public class AbstractClassVisitor implements ClassVisitor {
  @Override
  public TypeAnnotationVisitor visitTypeAnnotation(String desc,
      boolean visible, boolean inCode) {
    return new TypeAnnotationVisitor() {
      @Override
      public void visit(String name, Object value) {}
      @Override
      public void visitEnum(String name, String desc, String value) {}
      @Override
      public AnnotationVisitor visitAnnotation(String name,
          String desc) {
        return null;
      }
      @Override
      public AnnotationVisitor visitArray(String name) {
        return null;
      }
      @Override
      public void visitEnd() {}
      @Override
      public void visitXTargetType(int target_type) {}
      @Override
      public void visitXOffset(int offset) {}
      @Override
      public void visitXLocationLength(int location_length) {}
      @Override
      public void visitXLocation(TypePathEntry location) {}
      @Override
      public void visitXNumEntries(int num_entries) {}
      @Override
      public void visitXStartPc(int start_pc) {}
      @Override
      public void visitXLength(int length) {}
      @Override
      public void visitXIndex(int index) {}
      @Override
      public void visitXParamIndex(int param_index) {}
      @Override
      public void visitXBoundIndex(int bound_index) {}
      @Override
      public void visitXTypeIndex(int type_index) {}
      @Override
      public void visitXExceptionIndex(int exception_index) {}
      @Override
      public void visitXNameAndArgsSize() {}
    };
  }

  @Override
  public void visit(int version, int access, String name,
      String signature, String superName, String[] interfaces) {
  }

  @Override
  public void visitSource(String source, String debug) {}

  @Override
  public void visitOuterClass(String owner, String name, String desc) {}

  @Override
  public AnnotationVisitor visitAnnotation(String desc,
      boolean visible) {
    return new AnnotationVisitor() {
      @Override
      public void visit(String name, Object value) {}
      @Override
      public void visitEnum(String name, String desc, String value) {}
      @Override
      public AnnotationVisitor visitAnnotation(String name,
          String desc) {
        return null;
      }
      @Override
      public AnnotationVisitor visitArray(String name) {
        return null;
      }
      @Override
      public void visitEnd() {}
    };
  }

  @Override
  public void visitAttribute(Attribute attr) {}

  @Override
  public void visitInnerClass(String name,
      String outerName, String innerName, int access) {
  }

  @Override
  public FieldVisitor visitField(int access, String name, String desc,
      String signature, Object value) {
    return new FieldVisitor() {
      @Override
      public TypeAnnotationVisitor visitTypeAnnotation(String desc,
          boolean visible, boolean inCode) {
        return new TypeAnnotationVisitor() {
          @Override
          public void visit(String name, Object value) {}
          @Override
          public void visitEnum(String name, String desc, String value) {}
          @Override
          public AnnotationVisitor visitAnnotation(String name,
              String desc) {
            return null;
          }
          @Override
          public AnnotationVisitor visitArray(String name) {
            return null;
          }
          @Override
          public void visitEnd() {}
          @Override
          public void visitXTargetType(int target_type) {}
          @Override
          public void visitXOffset(int offset) {}
          @Override
          public void visitXLocationLength(int location_length) {}
          @Override
          public void visitXLocation(TypePathEntry location) {}
          @Override
          public void visitXNumEntries(int num_entries) {}
          @Override
          public void visitXStartPc(int start_pc) {}
          @Override
          public void visitXLength(int length) {}
          @Override
          public void visitXIndex(int index) {}
          @Override
          public void visitXParamIndex(int param_index) {}
          @Override
          public void visitXBoundIndex(int bound_index) {}
          @Override
          public void visitXTypeIndex(int type_index) {}
          @Override
          public void visitXExceptionIndex(int exception_index) {}
          @Override
          public void visitXNameAndArgsSize() {}
        };
      }
      @Override
      public AnnotationVisitor visitAnnotation(String desc,
          boolean visible) {
        return null;
      }
      @Override
      public void visitAttribute(Attribute attr) {}
      @Override
      public void visitEnd() {}
    };
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    return new MethodVisitor() {
      @Override
      public TypeAnnotationVisitor visitTypeAnnotation(String desc,
          boolean visible, boolean inCode) {
        return new TypeAnnotationVisitor() {
          @Override
          public void visit(String name, Object value) {}
          @Override
          public void visitEnum(String name, String desc, String value) {}
          @Override
          public AnnotationVisitor visitAnnotation(String name,
              String desc) {
            return null;
          }
          @Override
          public AnnotationVisitor visitArray(String name) {
            return null;
          }
          @Override
          public void visitEnd() {}
          @Override
          public void visitXTargetType(int target_type) {}
          @Override
          public void visitXOffset(int offset) {}
          @Override
          public void visitXLocationLength(int location_length) {}
          @Override
          public void visitXLocation(TypePathEntry location) {}
          @Override
          public void visitXNumEntries(int num_entries) {}
          @Override
          public void visitXStartPc(int start_pc) {}
          @Override
          public void visitXLength(int length) {}
          @Override
          public void visitXIndex(int index) {}
          @Override
          public void visitXParamIndex(int param_index) {}
          @Override
          public void visitXBoundIndex(int bound_index) {}
          @Override
          public void visitXTypeIndex(int type_index) {}
          @Override
          public void visitXExceptionIndex(int exception_index) {}
          @Override
          public void visitXNameAndArgsSize() {}
        };
      }
      @Override
      public AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationVisitor() {
          @Override
          public void visit(String name, Object value) {}
          @Override
          public void visitEnum(String name, String desc, String value) {}
          @Override
          public AnnotationVisitor visitAnnotation(String name,
              String desc) {
            return null;
          }
          @Override
          public AnnotationVisitor visitArray(String name) {
            return null;
          }
          @Override
          public void visitEnd() {}
        };
      }
      @Override
      public AnnotationVisitor visitAnnotation(String desc,
          boolean visible) {
        return null;
      }
      @Override
      public AnnotationVisitor visitParameterAnnotation(int parameter,
          String desc, boolean visible) {
        return new AnnotationVisitor() {
          @Override
          public void visit(String name, Object value) {}
          @Override
          public void visitEnum(String name, String desc, String value) {}
          @Override
          public AnnotationVisitor visitAnnotation(String name,
              String desc) {
            return null;
          }
          @Override
          public AnnotationVisitor visitArray(String name) {
            return null;
          }
          @Override
          public void visitEnd() {}
        };
      }
      @Override
      public void visitAttribute(Attribute attr) {}
      @Override
      public void visitCode() {}
      @Override
      public void visitInsn(int opcode) {}
      @Override
      public void visitIntInsn(int opcode, int operand) {}
      @Override
      public void visitVarInsn(int opcode, int var) {}
      @Override
      public void visitTypeInsn(int opcode, String desc) {}
      @Override
      public void visitFieldInsn(int opcode, String owner, String name,
          String desc) {
      }
      @Override
      public void visitMethodInsn(int opcode, String owner, String name,
          String desc) {
      }
      @Override
      public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
          Object... bsmArgs) {
      }
      @Override
      public void visitJumpInsn(int opcode, Label label) {
      }
      @Override
      public void visitLabel(Label label) {}
      @Override
      public void visitLdcInsn(Object cst) {}
      @Override
      public void visitIincInsn(int var, int increment) {}
      @Override
      public void visitTableSwitchInsn(int min, int max, Label dflt,
          Label[] labels) {
      }
      @Override
      public void visitLookupSwitchInsn(Label dflt, int[] keys,
          Label[] labels) {
      }
      @Override
      public void visitMultiANewArrayInsn(String desc, int dims) {}
      @Override
      public AnnotationVisitor visitInsnAnnotation(int typeRef,
          TypePath typePath, String desc, boolean visible) {
        return new AnnotationVisitor() {
          @Override
          public void visit(String name, Object value) {}
          @Override
          public void visitEnum(String name, String desc, String value) {}
          @Override
          public AnnotationVisitor visitAnnotation(String name,
              String desc) {
            return null;
          }
          @Override
          public AnnotationVisitor visitArray(String name) {
            return null;
          }
          @Override
          public void visitEnd() {}
        };
      }
      @Override
      public void visitTryCatchBlock(Label start, Label end,
          Label handler, String type) {
      }
      @Override
      public void visitLocalVariable(String name, String desc,
          String signature, Label start, Label end, int index) {
      }
      @Override
      public void visitLineNumber(int line, Label start) {}
      @Override
      public void visitMaxs(int maxStack, int maxLocals) {}
      @Override
      public void visitEnd() {}
    };
  }

  @Override
  public void visitEnd() {}
}
