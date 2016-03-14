package annotations.io.classfile;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

/**
 * {@link MethodVisitor} with type annotation visitor overrides that
 *  call ASMx legacy methods.
 *
 * @author dbro
 */
public class XMethodVisitor extends MethodVisitor {
  public XMethodVisitor(int api) {
    this(api, null);
  }

  public XMethodVisitor(int api, MethodVisitor mv) {
    super(api, mv);
  }

  @Override
  public AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
      TypePath typePath, Label[] start, Label[] end, int[] index,
      String desc, boolean visible) {
    XAnnotationVisitor v = new XAnnotationVisitor(api,
        super.visitLocalVariableAnnotation(typeRef,
            typePath, start, end, index, desc, visible));
    return v;  //.accept(typeRef, typePath, start, end, index, desc, visible, mv);
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(int typeRef,
      TypePath typePath, String desc, boolean visible) {
    XAnnotationVisitor v = new XAnnotationVisitor(api,
        super.visitTypeAnnotation(typeRef, typePath, desc, visible));
    return v; //.accept(typeRef, typePath, desc, visible, mv);
  }

  @Override
  public AnnotationVisitor visitInsnAnnotation(int typeRef,
      TypePath typePath, String desc, boolean visible) {
    XAnnotationVisitor v = new XAnnotationVisitor(api,
        super.visitInsnAnnotation(typeRef, typePath, desc, visible));
    return v; //.accept(typeRef, typePath, desc, visible, mv);
  }
}
