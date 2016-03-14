package annotations.io.classfile;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

/**
 * @author dbro
 */
public class XClassVisitor extends ClassVisitor {
  public XClassVisitor(int api) {
    super(api);
  }

  public XClassVisitor(int api, ClassVisitor cv) {
    super(api, cv);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    return new XMethodVisitor(api,
        super.visitMethod(access, name, desc, signature, exceptions));
  }

  @Override
  public FieldVisitor visitField(int access, String name, String desc,
      String signature, Object value) {
    return new XFieldVisitor(api,
        super.visitField(access, name, desc, signature, value));
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(int typeRef,
      TypePath typePath, String desc, final boolean visible) {
    return new XAnnotationVisitor(api,
        super.visitTypeAnnotation(typeRef, typePath, desc, visible));
    //return new XAnnotationVisitor(api).accept(typeRef,
    //    typePath, desc, visible, cv);
  }
}
