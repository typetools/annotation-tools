package scenelib.annotations.util;

import org.objectweb.asm.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodRecorder extends ClassVisitor {
  private List<String> methods;
  private List<String> annotations;
  private List<String> fields;

  public MethodRecorder(int api) {
    this(api, null);
  }

  public MethodRecorder(int api, ClassVisitor classVisitor) {
    super(api, classVisitor);
    this.methods = new ArrayList<>();
    this.annotations = new ArrayList<>();
    this.fields = new ArrayList<>();
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
    methods.add(name);
    return super.visitMethod(access, name, descriptor, signature, exceptions);
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    annotations.add(descriptor);
    return super.visitAnnotation(descriptor, visible);
  }

  public List<String> getMethods() {
    return Collections.unmodifiableList(methods);
  }

  public List<String> getAnnotations() {
    return Collections.unmodifiableList(annotations);
  }

  public List<String> getFields() {
    return Collections.unmodifiableList(fields);
  }

  @Override
  public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
    fields.add(name);
    return super.visitField(access, name, descriptor, signature, value);
  }

  public static void main(String[] args) throws IOException {
    ClassReader classReader = new ClassReader("com.google.common.annotations.GwtCompatible");
    MethodRecorder methodRecorder = new MethodRecorder(Opcodes.ASM7);
    classReader.accept(methodRecorder, 0);
    System.out.println(methodRecorder.annotations);
    System.out.println(methodRecorder.methods);
  }
}
