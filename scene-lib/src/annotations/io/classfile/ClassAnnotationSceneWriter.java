//This class is a complete ClassVisitor with many hidden classes that do
//the work of parsing an AScene and inserting them into a class file, as
//the original class file is being read.

package annotations.io.classfile;

/*>>>
import checkers.nullness.quals.*;
*/

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.annotation.RetentionPolicy;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.TypeAnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import com.sun.tools.javac.code.TargetType;

import annotations.*;
import annotations.el.*;
import annotations.field.*;

/**
 * A ClassAnnotationSceneWriter is a {@link org.objectweb.asm.ClassVisitor}
 * that can be used to write a class file that is the combination of an
 * existing class file and annotations in an {@link AScene}.  The "write"
 * in <code> ClassAnnotationSceneWriter </code> refers to a class file
 * being rewritten with information from a scene.  Also see {@link
 * ClassAnnotationSceneReader}.
 *
 * <p>
 *
 * The proper usage of this class is to construct a
 * <code>ClassAnnotationSceneWriter</code> with a {@link AScene} that
 * already contains all its annotations, pass this as a {@link
 * org.objectweb.asm.ClassVisitor} to {@link
 * org.objectweb.asm.ClassReader#accept}, and then obtain the resulting
 * class, ready to be written to a file, with {@link #toByteArray}.  </p>
 *
 * <p>
 *
 * All other methods are intended to be called only by
 * {@link org.objectweb.asm.ClassReader#accept},
 * and should not be called anywhere else, due to the order in which
 * {@link org.objectweb.asm.ClassVisitor} methods should be called.
 *
 * <p>
 *
 * Throughout this class, "scene" refers to the {@link AScene} this class is
 * merging into a class file.
 */
public class ClassAnnotationSceneWriter extends ClassAdapter {

  // Strategy for interleaving the necessary calls to visit annotations
  // from scene into the parsing done by ClassReader
  //  (the difficulty is that the entire call sequence to every data structure
  //   to visit annotations is in ClassReader, which should not be modified
  //   by this library):
  //
  // A ClassAnnotationSceneWriter is a ClassAdapter around a ClassWriter.
  //  - To visit the class' annotations in the scene, right before the code for
  //     ClassWriter.visit{InnerClass, Field, Method, End} is called,
  //     ensure that all extended annotations in the scene are visited once.
  //  - To visit every field's annotations,
  //     ClassAnnotationSceneWriter.visitField() returns a
  //     FieldAnnotationSceneWriter that in a similar fashion makes sure
  //     that each of that field's annotations is visited once on the call
  //     to visitEnd();
  //  - To visit every method's annotations,
  //     ClassAnnotationSceneWriter.visitMethod() returns a
  //     MethodAnnotationSceneWriter that visits all of that method's
  //     annotations in the scene at the first call of visit{Code, End}.
  //

  // Whether to output error messages for unsupported cases
  private static final boolean strict = false;

  // None of these classes fields should be null, except for aClass, which
  //  can't be vivified until the first visit() is called.

  /**
   * The scene from which to get additional annotations.
   */
  private AScene scene;

  /**
   * The representation of this class in the scene.
   */
  private AClass aClass;

  /**
   * A list of annotations on this class that this has already visited
   *  in the class file.
   */
  private List<String> existingClassAnnotations;

  /**
   * Whether or not this has visited the corresponding annotations in scene.
   */
  private boolean hasVisitedClassAnnotationsInScene;

  /**
   * Whether or not to overwrite existing annotations on the same element
   *  in a class file if a similar annotation is found in scene.
   */
  private boolean overwrite;

  /**
   * Constructs a new <code> ClassAnnotationSceneWriter </code> that will
   * insert all the annotations in <code> scene </code> into the class that
   * it visits.  <code> scene </code> must be an {@link AScene} over the
   * class that this will visit.
   *
   * @param scene the annotation scene containing annotations to be inserted
   * into the class this visits
   */
  public ClassAnnotationSceneWriter(AScene scene, boolean overwrite) {
    super(new ClassWriter(false));
    this.scene = scene;
    this.hasVisitedClassAnnotationsInScene = false;
    this.aClass = null;
    this.existingClassAnnotations = new ArrayList<String>();
    this.overwrite = overwrite;
  }

  /**
   * Returns a byte array that represents the resulting class file
   * from merging all the annotations in the scene into the class file
   * this has visited.  This method may only be called once this has already
   * completely visited a class, which is done by calling
   * {@link org.objectweb.asm.ClassReader#accept}.
   *
   * @return a byte array of the merged class file
   */
  public byte[] toByteArray() {
    return ((ClassWriter) cv).toByteArray();
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public void visit(int version, int access, String name,
      String signature, String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    // class files store fully quantified class names with '/' instead of '.'
    name = name.replace('/', '.');
    aClass = scene.classes.vivify(name);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ClassAdapter#visitInnerClass(java.lang.String, java.lang.String, java.lang.String, int)
   */
  @Override
  public void visitInnerClass(String name, String outerName, String innerName, int access ) {
    ensureVisitSceneClassAnnotations();
    super.visitInnerClass(name, outerName, innerName, access);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ClassAdapter#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
   */
  @Override
  public FieldVisitor visitField(int access, String name, String desc,
      String signature, Object value) {
    ensureVisitSceneClassAnnotations();
    // FieldAnnotationSceneWriter ensures that the field visits all
    //  its annotations in the scene.
    return new FieldAnnotationSceneWriter(name,
        super.visitField(access, name, desc, signature, value));
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    ensureVisitSceneClassAnnotations();
    // MethodAnnotationSceneWriter ensures that the method visits all
    //  its annotations in the scene.
    return new MethodAnnotationSceneWriter(name, desc,
        super.visitMethod(access, name, desc, signature, exceptions));
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ClassAdapter#visitEnd()
   */
  @Override
  public void visitEnd() {
    ensureVisitSceneClassAnnotations();
    super.visitEnd();
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ClassAdapter#visitAnnotation(java.lang.String, boolean)
   */
  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    existingClassAnnotations.add(desc);
    // If annotation exists in scene, and in overwrite mode,
    //  return empty visitor, since annotation from scene will be visited later.
    if (aClass.lookup(classDescToName(desc)) != null
        && overwrite) {
      return new EmptyVisitor();
    }
    return super.visitAnnotation(desc, visible);
  }

  /**
   * @inheritDoc
   * @see org.objectweb.asm.ClassAdapter#visitTypeAnnotation(java.lang.String, boolean)
   */
  @Override
  public TypeAnnotationVisitor visitTypeAnnotation(String desc, boolean visible) {
    existingClassAnnotations.add(desc);
    // If annotation exists in scene, and in overwrite mode,
    //  return empty visitor, annotation from scene will be visited later.
    if (aClass.lookup(classDescToName(desc)) != null
       && overwrite) {
      return new EmptyVisitor();
    }
    return new SafeTypeAnnotationVisitor(
        super.visitTypeAnnotation(desc, visible));
  }

  /**
   * Have this class visit the annotations in scene if and only if it has not
   * already visited them.
   */
  private void ensureVisitSceneClassAnnotations() {
    if (!hasVisitedClassAnnotationsInScene) {
      hasVisitedClassAnnotationsInScene = true;
      for (Annotation tla : aClass.tlAnnotationsHere) {
        // If not in overwrite mode and annotation already exists in classfile,
        //  ignore tla.
        if ((!overwrite) && existingClassAnnotations.contains(name(tla)))
          continue;

        AnnotationVisitor av = visitAnnotation(tla);
        visitFields(av, tla);
        av.visitEnd();
      }

      // do type parameter bound annotations
      for (Map.Entry<BoundLocation, ATypeElement> e :
        aClass.bounds.entrySet()) {
        BoundLocation bloc = e.getKey();
        ATypeElement bound = e.getValue();

        for (Annotation tla : bound.tlAnnotationsHere) {
          TypeAnnotationVisitor xav = visitTypeAnnotation(tla);

          visitFields(xav, tla);
          if (bloc.boundIndex == -1) {
            visitTargetType(xav, TargetType.CLASS_TYPE_PARAMETER);
            visitBound(xav, bloc);
          } else {
            visitTargetType(xav, TargetType.CLASS_TYPE_PARAMETER_BOUND);
            visitBound(xav, bloc);
          }
          xav.visitEnd();
        }

        for (Map.Entry<InnerTypeLocation, ATypeElement> e2 :
          bound.innerTypes.entrySet()) {
          InnerTypeLocation itloc = e2.getKey();
          ATypeElement innerType = e2.getValue();

          for (Annotation tla : innerType.tlAnnotationsHere) {
            TypeAnnotationVisitor xav = visitTypeAnnotation(tla);

            visitFields(xav, tla);
            visitTargetType(xav, TargetType.CLASS_TYPE_PARAMETER_BOUND_COMPONENT);
            visitBound(xav, bloc);
            visitLocations(xav, itloc);
            xav.visitEnd();
          }
        }
      }

      for (Map.Entry<TypeIndexLocation, ATypeElement> e : aClass.extendsImplements.entrySet()) {
        TypeIndexLocation idx = e.getKey();
        ATypeElement aty = e.getValue();

        // TODO: How is this annotation written back out?
        if (strict) { System.err.println("ClassAnnotationSceneWriter: ignoring Extends/Implements annotation " + idx + " with type: " + aty); }
      }
      
    }
  }

  /**
   * The following methods are utility methods for accessing
   * information useful to asm from scene-library data structures.
   *
   * @return true iff tla is visible at runtime
   */
  private static boolean isRuntimeRetention(Annotation tla) {
    if (tla.def.retention() == null)
      return false; // TODO: temporary
    return tla.def.retention().equals(RetentionPolicy.RUNTIME);
  }

  /**
   * Returns the name of the annotation in the top level.
   */
  private static String name(Annotation tla) {
    return tla.def().name;
  }

  /**
   * Wraps the given class name in a class descriptor.
   */
  private static String classNameToDesc(String name) {
    return "L" + name.replace('.', '/') + ";";
  }

  /**
   * Unwraps the class name from the given class descriptor.
   */
  private static String classDescToName(String desc) {
    return desc.substring(1, desc.length() - 1).replace('/', '.');
  }

  /**
   * Returns an AnnotationVisitor over the given top-level annotation.
   */
  private AnnotationVisitor visitAnnotation(Annotation tla) {
    return super.visitAnnotation(classNameToDesc(name(tla)), isRuntimeRetention(tla));
  }

  /**
   * Returns an TypeAnnotationVisitor over the given top-level annotation.
   */
  private TypeAnnotationVisitor visitTypeAnnotation(Annotation tla) {
    return super.visitTypeAnnotation(classNameToDesc(name(tla)), isRuntimeRetention(tla));
  }

  /**
   * Has av visit the fields in the given annotation.
   * This method is necessary even with
   * visitFields(AnnotationVisitor, Annotation)
   * because a Annotation cannot be created from the Annotation
   * specified to be available from the Annotation object for subannotations.
   */
  private void visitFields(AnnotationVisitor av, Annotation a) {
    for (String fieldName : a.def().fieldTypes.keySet()) {
      Object value = a.getFieldValue(fieldName);
      if (value == null) {
          // hopefully a field with a default value
          continue;
      }
      AnnotationFieldType aft = a.def().fieldTypes.get(fieldName);
      if (value instanceof Annotation) {
        AnnotationVisitor nav = av.visitAnnotation(fieldName, classDescToName(a.def().name));
        visitFields(nav, (Annotation) a);
        nav.visitEnd();
      } else if (value instanceof List) {
        // In order to visit an array, the AnnotationVisitor returned by
        // visitArray needs to visit each element, and by specification
        // the name should be null for each element.
        AnnotationVisitor aav = av.visitArray(fieldName);
        aft = ((ArrayAFT) aft).elementType;
        for (Object o : (List<?>)value) {
          if (aft instanceof EnumAFT) {
            aav.visitEnum(null, ((EnumAFT) aft).typeName, o.toString());
          } else {
            aav.visit(null, o);
          }
        }
        aav.visitEnd();
      } else if (aft instanceof EnumAFT) {
        av.visitEnum(fieldName, ((EnumAFT) aft).typeName, value.toString());
      } else if (aft instanceof ClassTokenAFT) {
        av.visit(fieldName, org.objectweb.asm.Type.getType((Class)value));
      } else {
        // everything else is a string
        av.visit(fieldName, value);
      }
    }
  }

  /**
   * Has xav visit the given target type.
   */
  private void visitTargetType(TypeAnnotationVisitor xav, TargetType t) {
    xav.visitXTargetType(t.targetTypeValue());
  }

  /**
   * Have xav visit the location length  and all locations in loc.
   */
  private void visitLocations(TypeAnnotationVisitor xav, InnerTypeLocation loc) {
    List<Integer> location = loc.location;
    xav.visitXLocationLength(location.size());
    for (Integer l : location) {
      xav.visitXLocation(l);
    }
  }

  /**
   * Has xav visit the local varialbe information in loc.
   */
  private void visitLocalVar(TypeAnnotationVisitor xav, LocalLocation loc) {
    xav.visitXNumEntries(1);
    xav.visitXStartPc(loc.scopeStart);
    xav.visitXLength(loc.scopeLength);
    xav.visitXIndex(loc.index);
  }

  /**
   * Has xav visit the offset.
   */
  private void visitOffset(TypeAnnotationVisitor xav, int offset) {
    xav.visitXOffset(offset);
  }

  private void visitParameterIndex(TypeAnnotationVisitor xav, int index) {
	  xav.visitXParamIndex(index);
  }

  private void visitTypeIndex(TypeAnnotationVisitor xav, int index) {
	  xav.visitXTypeIndex(index);
  }

  /**
   * Has xav visit the type parameter bound information in loc.
   */
  private void visitBound(TypeAnnotationVisitor xav, BoundLocation loc) {
    xav.visitXParamIndex(loc.paramIndex);
    if (loc.boundIndex != -1)
      xav.visitXBoundIndex(loc.boundIndex);
  }

  /**
   * A FieldAnnotationSceneWriter is a wrapper class around a FieldVisitor that
   * delegates all calls to its internal FieldVisitor, and on a call to
   * visitEnd(), also has its internal FieldVisitor visit all the
   * corresponding field annotations in scene.
   */
  private class FieldAnnotationSceneWriter implements FieldVisitor {
    // After being constructed, none of these fields should be null.

    /**
     * Internal FieldVisitor all calls are delegated to.
     */
    private FieldVisitor fv;

    /**
     * List of all annotations this has already visited.
     */
    private List<String> existingFieldAnnotations;

    /**
     * The AElement that represents this field in the AScene the
     *   class is visiting.
     */
    private AElement aField;

    /**
     * Constructs a new FieldAnnotationSceneWriter with the given name that
     * wraps the given FieldVisitor.
     */
    public FieldAnnotationSceneWriter(String name, FieldVisitor fv) {
      this.fv = fv;
      this.existingFieldAnnotations = new ArrayList<String>();
      this.aField = aClass.fields.vivify(name);
    }

    /**
     * @inheritDoc
     * @see org.objectweb.asm.FieldVisitor#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      existingFieldAnnotations.add(desc);

      // If annotation exists in scene, and in overwrite mode,
      //  return empty visitor, annotation from scene will be visited later.
      if (aField.lookup(classDescToName(desc)) != null
          && overwrite)
        return new EmptyVisitor();

      return fv.visitAnnotation(desc, visible);
    }

    /**
     * @inheritDoc
     * @see org.objectweb.asm.FieldVisitor#visitTypeAnnotation(java.lang.String, boolean)
     */
    @Override
    public TypeAnnotationVisitor visitTypeAnnotation(String desc, boolean visible) {
      existingFieldAnnotations.add(desc);

      // If annotation exists in scene, and in overwrite mode,
      //  return empty visitor, annotation from scene will be visited later.
      if (aField.lookup(classDescToName(desc)) != null
         && overwrite)
        return new EmptyVisitor();

      return new SafeTypeAnnotationVisitor(
          fv.visitTypeAnnotation(desc, visible));
    }

    /** @inheritDoc
     * @see org.objectweb.asm.FieldVisitor#visitAttribute(org.objectweb.asm.Attribute)
     */
    @Override
    public void visitAttribute(Attribute attr) {
      fv.visitAttribute(attr);
    }

    /**
     * Tells this to visit the end of the field in the class file,
     * and also ensures that this visits all its annotations in the scene.
     *
     * @see org.objectweb.asm.FieldVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
      ensureVisitSceneFieldAnnotations();
      fv.visitEnd();
    }

    /**
     * Has this visit the annotations on the corresponding field in scene.
     */
    private void ensureVisitSceneFieldAnnotations() {
      // First do declaration annotations on a field.
      for (Annotation tla : aField.tlAnnotationsHere) {
        if ((!overwrite) && existingFieldAnnotations.contains(name(tla))) {
          continue;
        }
        AnnotationVisitor av = fv.visitAnnotation(classNameToDesc(name(tla)), isRuntimeRetention(tla));
        visitFields(av, tla);
        av.visitEnd();
      }

      // Then do the type annotations on the field
      for (Annotation tla : aField.type.tlAnnotationsHere) {
        if ((!overwrite) && existingFieldAnnotations.contains(name(tla))) {
          continue;
        }
        TypeAnnotationVisitor av = fv.visitTypeAnnotation(classNameToDesc(name(tla)), isRuntimeRetention(tla));
        visitFields(av, tla);
        visitTargetType(av, TargetType.FIELD);
        av.visitEnd();
      }

      // Now do field generics/arrays.
      for (Map.Entry<InnerTypeLocation, ATypeElement> fieldInnerEntry :
        aField.type.innerTypes.entrySet()) {

        for (Annotation tla : fieldInnerEntry.getValue().tlAnnotationsHere) {
          if ((!overwrite) && existingFieldAnnotations.contains(name(tla))) {
            continue;
          }
          TypeAnnotationVisitor xav =
            fv.visitTypeAnnotation(classNameToDesc(name(tla)), isRuntimeRetention(tla));
          visitFields(xav, tla);
          visitTargetType(xav, TargetType.FIELD_COMPONENT);
          visitLocations(xav, fieldInnerEntry.getKey());
          xav.visitEnd();
        }
      }
    }
  }

  /**
   * A MethodAnnotationSceneWriter is to a MethodAdapter exactly
   * what ClassAnnotationSceneWriter is to a ClassAdapter:
   * it will ensure that the MethodVisitor behind MethodAdapter
   * visits each of the extended annotations in scene in the correct
   * sequence, before any of the later data is visited.
   */
  private class MethodAnnotationSceneWriter extends MethodAdapter {
    // basic strategy:
    // ensureMethodVisitSceneAnnotation will be called, if it has not already
    // been called, at the beginning of visitCode, visitEnd

    /**
     * The AMethod that represents this method in scene.
     */
    private AMethod aMethod;

    /**
     * Whether or not this has visit the method's annotations in scene.
     */
    private boolean hasVisitedMethodAnnotations;

    /**
     * The existing annotations this method has visited.
     */
    private List<String> existingMethodAnnotations;

    /**
     * Constructs a new MethodAnnotationSceneWriter with the given name and
     * description that wraps around the given MethodVisitor.
     *
     * @param name the name of the method, as in "foo"
     * @param desc the method signature minus the name,
     *  as in "(Ljava/lang/String)V"
     * @param mv the method visitor to wrap around
     */
    MethodAnnotationSceneWriter(String name, String desc, MethodVisitor mv) {
      super(mv);
      this.hasVisitedMethodAnnotations = false;
      this.aMethod = aClass.methods.vivify(name+desc);
      this.existingMethodAnnotations = new ArrayList<String>();
    }

    /**
     * @inheritDoc
     * @see org.objectweb.asm.MethodAdapter#visitCode()
     */
    @Override
    public void visitCode() {
      ensureVisitSceneMethodAnnotations();
      super.visitCode();
    }

    /**
     * @inheritDoc
     * @see org.objectweb.asm.MethodAdapter#visitEnd()
     */
    @Override
    public void visitEnd() {
      ensureVisitSceneMethodAnnotations();
      super.visitEnd();
    }

    /**
     * @inheritDoc
     * @see org.objectweb.asm.MethodAdapter#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      existingMethodAnnotations.add(desc);
      // If annotation exists in scene, and in overwrite mode,
      //  return empty visitor, annotation from scene will be visited later.
      if (shouldSkipExisting(classDescToName(desc))) {
        return new EmptyVisitor();
      }

      return super.visitAnnotation(desc, visible);
    }

    /**
     * @inheritDoc
     * @see org.objectweb.asm.MethodAdapter#visitTypeAnnotation(java.lang.String, boolean)
     */
    @Override
    public TypeAnnotationVisitor visitTypeAnnotation(String desc, boolean visible) {

      existingMethodAnnotations.add(desc);

      // If annotation exists in scene, and in overwrite mode,
      //  return empty visitor, annotation from scene will be visited later.
      if (shouldSkipExisting(classDescToName(desc))) {
        return new EmptyVisitor();
      }

      return new SafeTypeAnnotationVisitor(
          super.visitTypeAnnotation(desc, visible));
    }

    /**
     * Returns true iff the annotation in tla should not be written because it
     *  already exists in this method's annotations.
     */
    private boolean shouldSkip(Annotation tla) {
      return ((!overwrite) && existingMethodAnnotations.contains(name(tla)));
    }

    /**
     * Returns true iff the annotation with the given name should not be written
     * because it already exists in this method's annotations.
     */
    private boolean shouldSkipExisting(String name) {
      return ((!overwrite)
              && aMethod.lookup(name) != null);
    }

    /**
     * Has this visit the annotation in tla, and returns the resulting visitor.
     */
    private AnnotationVisitor visitAnnotation(Annotation tla) {
      return super.visitAnnotation(classNameToDesc(name(tla)), isRuntimeRetention(tla));
    }

    /**
     * Has this visit the extended annotation in tla and returns the
     * resulting visitor.
     */
    private TypeAnnotationVisitor
    visitTypeAnnotation(Annotation tla) {
      return super.visitTypeAnnotation(classNameToDesc(name(tla)), isRuntimeRetention(tla));
    }

    /**
     * Has this visit the parameter annotation in tla and returns the
     * resulting visitor.
     */
    private AnnotationVisitor visitParameterAnnotation(Annotation tla, int index) {
      return super.visitParameterAnnotation(index, classNameToDesc(name(tla)), isRuntimeRetention(tla));
    }

    /**
     * Has this visit the declaration annotation and the type annotations on the return type.
     */
    private void ensureVisitMethodDeclarationAnnotations() {
      // Annotations on method declaration.
      for (Annotation tla : aMethod.tlAnnotationsHere) {
        if (shouldSkip(tla)) continue;

        AnnotationVisitor av = visitAnnotation(tla);
        visitFields(av, tla);
        av.visitEnd();
      }

    }

    /**
     * Has this visit the declaration annotations and the type annotations on the return type.
     */
    private void ensureVisitReturnTypeAnnotations() {
      // Standard annotations on return type.
      for (Annotation tla : aMethod.returnType.tlAnnotationsHere) {
        if (shouldSkip(tla)) continue;

        TypeAnnotationVisitor av = visitTypeAnnotation(tla);
        visitFields(av, tla);
        visitTargetType(av, TargetType.METHOD_RETURN);
        av.visitEnd();
      }

      // Now do generic/array information on return type
      for (Map.Entry<InnerTypeLocation, ATypeElement> e :
        aMethod.returnType.innerTypes.entrySet()) {
        InnerTypeLocation loc = e.getKey();
        ATypeElement innerType = e.getValue();

        for (Annotation tla : innerType.tlAnnotationsHere) {
          TypeAnnotationVisitor xav = visitTypeAnnotation(tla);

          visitFields(xav, tla);
          visitTargetType(xav, TargetType.METHOD_RETURN_COMPONENT);
          // information for raw type (return type)
          //  (none)
          // information for generic/array (on return type)
          visitLocations(xav, loc);
          xav.visitEnd();
        }
      }

    }

    /**
     * Has this visit the annotations on type parameter bounds.
     */
    private void ensureVisitTypeParameterBoundAnnotations() {
      for (Map.Entry<BoundLocation, ATypeElement> e :
        aMethod.bounds.entrySet()) {
        BoundLocation bloc = e.getKey();
        ATypeElement bound = e.getValue();

        for (Annotation tla : bound.tlAnnotationsHere) {
          TypeAnnotationVisitor xav = visitTypeAnnotation(tla);

          visitFields(xav, tla);
          if (bloc.boundIndex == -1) {
            visitTargetType(xav, TargetType.METHOD_TYPE_PARAMETER);
            visitBound(xav, bloc);
          } else {
            visitTargetType(xav, TargetType.METHOD_TYPE_PARAMETER_BOUND);
            visitBound(xav, bloc);
          }
          xav.visitEnd();
        }

        for (Map.Entry<InnerTypeLocation, ATypeElement> e2 :
          bound.innerTypes.entrySet()) {
          InnerTypeLocation itloc = e2.getKey();
          ATypeElement innerType = e2.getValue();

          for (Annotation tla : innerType.tlAnnotationsHere) {
            TypeAnnotationVisitor xav = visitTypeAnnotation(tla);

            visitFields(xav, tla);
            visitTargetType(xav, TargetType.METHOD_TYPE_PARAMETER_BOUND_COMPONENT);
            visitBound(xav, bloc);
            visitLocations(xav, itloc);
            xav.visitEnd();
          }
        }
      }
    }

    /**
     * Has this visit the annotations on local variables in this method.
     */
    private void ensureVisitLocalVariablesAnnotations() {
      for (Map.Entry<LocalLocation, AElement> entry :
        aMethod.locals.entrySet()) {
        LocalLocation localLocation = entry.getKey();
        AElement aLocation = entry.getValue();

        for (Annotation tla : aLocation.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          TypeAnnotationVisitor xav = visitTypeAnnotation(tla);
          visitFields(xav, tla);
          visitTargetType(xav, TargetType.LOCAL_VARIABLE);
          visitLocalVar(xav, localLocation);
          xav.visitEnd();
        }

        // now do annotations on inner type of aLocation (local variable)
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
          aLocation.type.innerTypes.entrySet()) {
          InnerTypeLocation localVariableLocation = e.getKey();
          ATypeElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            TypeAnnotationVisitor xav = visitTypeAnnotation(tla);
            visitFields(xav, tla);
            visitTargetType(xav, TargetType.LOCAL_VARIABLE_COMPONENT);
            // information for raw type (local variable)
            visitLocalVar(xav, localLocation);
            // information for generic/array (on local variable)
            visitLocations(xav, localVariableLocation);
            xav.visitEnd();
          }

        }
      }
    }

    /**
     * Has this visit the object creation (new) annotations on this method.
     */
    private void ensureVisitObjectCreationAnnotations() {
      for (Map.Entry<RelativeLocation, ATypeElement> entry :
        aMethod.news.entrySet()) {
        if(!entry.getKey().isBytecodeOffset()) {
          // if the RelativeLocation is a source index, we cannot insert it
          // into bytecode
          // TODO: output a warning or translate
          if (strict) { System.err.println("ClassAnnotationSceneWriter.ensureVisitObjectCreationAnnotation: no bytecode offset found!"); }
        }
        int offset = entry.getKey().offset;
        ATypeElement aNew = entry.getValue();
        for (Annotation tla : aNew.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          TypeAnnotationVisitor xav = visitTypeAnnotation(tla);
          visitFields(xav, tla);
          visitTargetType(xav, TargetType.NEW);
          visitOffset(xav, offset);
          xav.visitEnd();
        }

        // now do inner annotations on aNew (object creation)
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
          aNew.innerTypes.entrySet()) {
          InnerTypeLocation aNewLocation = e.getKey();
          ATypeElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            TypeAnnotationVisitor xav = visitTypeAnnotation(tla);
            visitFields(xav, tla);
            visitTargetType(xav, TargetType.NEW_COMPONENT);
            // information for raw type (object creation)
            visitOffset(xav, offset);
            // information for generic/array (on object creation)
            visitLocations(xav, aNewLocation);
            xav.visitEnd();
          }
        }
      }
    }

    /**
     * Has this visit the parameter annotations on this method.
     */
    private void ensureVisitParameterAnnotations() {
      for (Map.Entry<Integer, AElement> entry :
        aMethod.parameters.entrySet()) {
        AElement aParameter = entry.getValue();
        int index = entry.getKey();
        // First visit declaration annotations on the parameter
        for (Annotation tla : aParameter.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          AnnotationVisitor av = visitParameterAnnotation(tla, index);
          visitFields(av, tla);
          av.visitEnd();
        }

        // Then handle type annotations targeting the parameter
        for (Annotation tla : aParameter.type.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            TypeAnnotationVisitor av = visitTypeAnnotation(tla);
            visitFields(av, tla);
            visitTargetType(av, TargetType.METHOD_PARAMETER);
            visitParameterIndex(av, index);
            av.visitEnd();
        }

        // now handle inner annotations on aParameter (parameter)
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
          aParameter.type.innerTypes.entrySet()) {
          InnerTypeLocation aParameterLocation = e.getKey();
          ATypeElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            TypeAnnotationVisitor xav = visitTypeAnnotation(tla);
            visitFields(xav, tla);
            visitTargetType(xav,
                TargetType.METHOD_PARAMETER_COMPONENT);
            // information for raw type (parameter)
            //  (none)
            // information for generic/array (on parameter)
            visitParameterIndex(xav, index);
            visitLocations(xav, aParameterLocation);
            xav.visitEnd();
          }
        }
      }
    }

    /**
     * Has this visit the receiver annotations on this method.
     */
    private void ensureVisitReceiverAnnotations() {
      ATypeElement aReceiver = aMethod.receiver;
      for (Annotation tla : aReceiver.tlAnnotationsHere) {
        if (shouldSkip(tla)) continue;

        TypeAnnotationVisitor xav = visitTypeAnnotation(tla);
        visitFields(xav, tla);
        visitTargetType(xav, TargetType.METHOD_RECEIVER);
        xav.visitEnd();
      }

      // now do inner annotations of aReceiver
      for (Map.Entry<InnerTypeLocation, ATypeElement> e :
        aReceiver.innerTypes.entrySet()) {
        InnerTypeLocation aReceiverLocation = e.getKey();
        ATypeElement aInnerType = e.getValue();
        for (Annotation tla : aInnerType.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          TypeAnnotationVisitor xav = visitTypeAnnotation(tla);
          visitFields(xav, tla);
          visitTargetType(xav, TargetType.METHOD_RECEIVER_COMPONENT);
          // information for generic/array (on receiver)
          visitLocations(xav, aReceiverLocation);
          xav.visitEnd();
        }
      }

    }

    /**
     * Has this visit the typecast annotations on this method.
     */
    private void ensureVisitTypecastAnnotations() {
      for (Map.Entry<RelativeLocation, ATypeElement> entry :
        aMethod.typecasts.entrySet()) {
        if(!entry.getKey().isBytecodeOffset()) {
          // if the RelativeLocation is a source index, we cannot insert it
          // into bytecode
          // TODO: output a warning or translate
          if (strict) { System.err.println("ClassAnnotationSceneWriter.ensureVisitTypecastAnnotation: no bytecode offset found!"); }
        }
        int offset = entry.getKey().offset;
        ATypeElement aTypecast = entry.getValue();
        for (Annotation tla : aTypecast.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          TypeAnnotationVisitor xav = visitTypeAnnotation(tla);
          visitFields(xav, tla);
          visitTargetType(xav, TargetType.TYPECAST);
          visitOffset(xav, offset);
          xav.visitEnd();
        }

        // now do inner annotations of aTypecast (typecast)
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
          aTypecast.innerTypes.entrySet()) {
          InnerTypeLocation aTypecastLocation = e.getKey();
          ATypeElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            TypeAnnotationVisitor xav = visitTypeAnnotation(tla);
            visitFields(xav, tla);
            visitTargetType(xav, TargetType.TYPECAST_COMPONENT);
            // information for raw type (typecast)
            visitOffset(xav, offset);
            // information for generic/array (on typecast)
            visitLocations(xav, aTypecastLocation);
            xav.visitEnd();
          }
        }
      }
    }

    /**
     * Has this visit the typetest annotations on this method.
     */
    private void ensureVisitTypeTestAnnotations() {
      for (Map.Entry<RelativeLocation, ATypeElement> entry :
        aMethod.instanceofs.entrySet()) {
        if(!entry.getKey().isBytecodeOffset()) {
          // if the RelativeLocation is a source index, we cannot insert it
          // into bytecode
          // TODO: output a warning or translate
          if (strict) { System.err.println("ClassAnnotationSceneWriter.ensureVisitTypeTestAnnotation: no bytecode offset found!"); }
        }
        int offset = entry.getKey().offset;
        ATypeElement aTypeTest = entry.getValue();
        for (Annotation tla : aTypeTest.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          TypeAnnotationVisitor xav = visitTypeAnnotation(tla);
          visitFields(xav, tla);
          visitTargetType(xav, TargetType.INSTANCEOF);
          visitOffset(xav, offset);
          xav.visitEnd();
        }

        // now do inner annotations of aTypeTest (typetest)
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
          aTypeTest.innerTypes.entrySet()) {
          InnerTypeLocation aTypeTestLocation = e.getKey();
          AElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            TypeAnnotationVisitor xav = visitTypeAnnotation(tla);
            visitFields(xav, tla);
            visitTargetType(xav, TargetType.INSTANCEOF_COMPONENT);
            // information for raw type (typetest)
            visitOffset(xav, offset);
            // information for generic/array (on typtest)
            visitLocations(xav, aTypeTestLocation);
            xav.visitEnd();
          }
        }
      }
    }

    /**
     * Have this method visit the annotations in scene if and only if
     *  it has not visited them before.
     */
    private void ensureVisitSceneMethodAnnotations() {
      if (!hasVisitedMethodAnnotations) {
        hasVisitedMethodAnnotations = true;

        ensureVisitMethodDeclarationAnnotations();
        ensureVisitReturnTypeAnnotations();

        // Now iterate through method's locals, news, parameter, receiver,
        // and typecasts annotations, which will all be extended annotations
        ensureVisitTypeParameterBoundAnnotations();
        ensureVisitLocalVariablesAnnotations();
        ensureVisitObjectCreationAnnotations();
        ensureVisitParameterAnnotations();
        ensureVisitReceiverAnnotations();
        ensureVisitTypecastAnnotations();
        ensureVisitTypeTestAnnotations();
        // TODO: throw clauses?!
        // TODO: catch clauses!?
      }
    }
  }
}
