package scenelib.annotations.util;

import org.objectweb.asmx.AnnotationVisitor;
import org.objectweb.asmx.Attribute;
import org.objectweb.asmx.ClassVisitor;
import org.objectweb.asmx.FieldVisitor;
import org.objectweb.asmx.Handle;
import org.objectweb.asmx.Label;
import org.objectweb.asmx.MethodVisitor;
import org.objectweb.asmx.TypeAnnotationVisitor;
import org.objectweb.asmx.TypePath;

import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;

public class AbstractClassVisitor implements ClassVisitor {
  @Override
  public TypeAnnotationVisitor visitTypeAnnotation(String descriptor,
      boolean visible, boolean inCode) {
    return new TypeAnnotationVisitor() {
      @Override
      public void visit(String name, Object value) {}
      @Override
      public void visitEnum(String name, String descriptor, String value) {}
      @Override
      public AnnotationVisitor visitAnnotation(String name,
          String descriptor) {
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
  public void visitOuterClass(String owner, String name, String descriptor) {}

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor,
      boolean visible) {
    return new AnnotationVisitor() {
      @Override
      public void visit(String name, Object value) {}
      @Override
      public void visitEnum(String name, String descriptor, String value) {}
      @Override
      public AnnotationVisitor visitAnnotation(String name,
          String descriptor) {
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
  public FieldVisitor visitField(int access, String name, String descriptor,
      String signature, Object value) {
    return new FieldVisitor() {
      @Override
      public TypeAnnotationVisitor visitTypeAnnotation(String descriptor,
          boolean visible, boolean inCode) {
        return new TypeAnnotationVisitor() {
          @Override
          public void visit(String name, Object value) {}
          @Override
          public void visitEnum(String name, String descriptor, String value) {}
          @Override
          public AnnotationVisitor visitAnnotation(String name,
              String descriptor) {
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
      public AnnotationVisitor visitAnnotation(String descriptor,
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
  public MethodVisitor visitMethod(int access, String name, String descriptor,
      String signature, String[] exceptions) {
    return new MethodVisitor() {
      @Override
      public TypeAnnotationVisitor visitTypeAnnotation(String descriptor,
          boolean visible, boolean inCode) {
        return new TypeAnnotationVisitor() {
          @Override
          public void visit(String name, Object value) {}
          @Override
          public void visitEnum(String name, String descriptor, String value) {}
          @Override
          public AnnotationVisitor visitAnnotation(String name,
              String descriptor) {
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
          public void visitEnum(String name, String descriptor, String value) {}
          @Override
          public AnnotationVisitor visitAnnotation(String name,
              String descriptor) {
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
      public AnnotationVisitor visitAnnotation(String descriptor,
          boolean visible) {
        return null;
      }
      @Override
      public AnnotationVisitor visitParameterAnnotation(int parameter,
          String descriptor, boolean visible) {
        return new AnnotationVisitor() {
          @Override
          public void visit(String name, Object value) {}
          @Override
          public void visitEnum(String name, String descriptor, String value) {}
          @Override
          public AnnotationVisitor visitAnnotation(String name,
              String descriptor) {
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
      public void visitTypeInsn(int opcode, String descriptor) {}
      @Override
      public void visitFieldInsn(int opcode, String owner, String name,
          String descriptor) {
      }
      @Override
      public void visitMethodInsn(int opcode, String owner, String name,
          String descriptor) {
      }
      @Override
      public void visitInvokeDynamicInsn(String name, String descriptor, Handle bsm,
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
      public void visitMultiANewArrayInsn(String descriptor, int dims) {}
      @Override
      public AnnotationVisitor visitInsnAnnotation(int typeRef,
          TypePath typePath, String descriptor, boolean visible) {
        return new AnnotationVisitor() {
          @Override
          public void visit(String name, Object value) {}
          @Override
          public void visitEnum(String name, String descriptor, String value) {}
          @Override
          public AnnotationVisitor visitAnnotation(String name,
              String descriptor) {
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
      public void visitLocalVariable(String name, String descriptor,
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
