package annotations.io.classfile;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.TypePath;

/**
 * @author dbro
 */
public class XFieldVisitor extends FieldVisitor {
  public XFieldVisitor(int api) {
    super(api);
  }

  public XFieldVisitor(int api, FieldVisitor fv) {
    super(api, fv);
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(int typeRef,
      TypePath typePath, String desc, final boolean visible) {
    return new XAnnotationVisitor(api,
        super.visitTypeAnnotation(typeRef, typePath, desc, visible));
  }
}
