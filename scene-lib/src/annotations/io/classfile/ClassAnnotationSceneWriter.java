//This class is a complete ClassVisitor with many hidden classes that do
//the work of parsing an AScene and inserting them into a class file, as
//the original class file is being read.

package annotations.io.classfile;

/*>>>
import org.checkerframework.checker.nullness.qual.*;
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.lang.annotation.RetentionPolicy;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

import com.sun.tools.javac.code.TargetType;
import com.sun.tools.javac.code.TypeAnnotationPosition;
//import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;

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
public class ClassAnnotationSceneWriter extends CodeOffsetAdapter {
  private static final AnnotationVisitor EMPTY_ANNOTATION_VISITOR =
      new AnnotationVisitor(Opcodes.ASM5) {};

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
  private final AScene scene;

  /**
   * The representation of this class in the scene.
   */
  private AClass aClass;

  /**
   * A list of annotations on this class that this has already visited
   *  in the class file.
   */
  private final List<String> existingClassAnnotations;

  /**
   * Whether or not this has visited the corresponding annotations in scene.
   */
  private boolean hasVisitedClassAnnotationsInScene;

  /**
   * Whether or not to overwrite existing annotations on the same element
   *  in a class file if a similar annotation is found in scene.
   */
  private final boolean overwrite;

  private final Map<String, Set<Integer>> dynamicConstructors;
  private final Map<String, Set<Integer>> lambdaExpressions;

  private ClassReader cr = null;

  /**
   * Constructs a new <code> ClassAnnotationSceneWriter </code> that will
   * insert all the annotations in <code> scene </code> into the class that
   * it visits.  <code> scene </code> must be an {@link AScene} over the
   * class that this will visit.
   *
   * @param cr the reader for the class being modified
   * @param scene the annotation scene containing annotations to be inserted
   * into the class this visits
   */
  public ClassAnnotationSceneWriter(ClassReader cr, AScene scene, boolean overwrite) {
    super(cr);
    this.scene = scene;
    this.hasVisitedClassAnnotationsInScene = false;
    this.aClass = null;
    this.existingClassAnnotations = new ArrayList<String>();
    this.overwrite = overwrite;
    this.dynamicConstructors = new HashMap<String, Set<Integer>>();
    this.lambdaExpressions = new HashMap<String, Set<Integer>>();
    this.cr = cr;
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
   * {@inheritDoc}
   * @see org.objectweb.asm.ClassVisitor#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public void visit(int version, int access, String name,
      String signature, String superName, String[] interfaces) {
    cr.accept(new MethodCodeIndexer(), 0);
    super.visit(version, access, name, signature, superName, interfaces);
    // class files store fully quantified class names with '/' instead of '.'
    name = name.replace('/', '.');
    aClass = scene.classes.vivify(name);
  }

  /**
   * {@inheritDoc}
   * @see org.objectweb.asm.ClassVisitor#visitInnerClass(java.lang.String, java.lang.String, java.lang.String, int)
   */
  @Override
  public void visitInnerClass(String name, String outerName, String innerName, int access ) {
    ensureVisitSceneClassAnnotations();
    super.visitInnerClass(name, outerName, innerName, access);
  }

  /**
   * {@inheritDoc}
   * @see org.objectweb.asm.ClassVisitor#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
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
   * {@inheritDoc}
   * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    AMethod aMethod = aClass.methods.vivify(name+desc);
    ensureVisitSceneClassAnnotations();
    // MethodAnnotationSceneWriter ensures that the method visits all
    //  its annotations in the scene.
    return new MethodAnnotationSceneWriter(aMethod,
        super.visitMethod(access, name, desc, signature, exceptions)) {};
  }

  /**
   * {@inheritDoc}
   * @see org.objectweb.asm.ClassVisitor#visitEnd()
   */
  @Override
  public void visitEnd() {
    ensureVisitSceneClassAnnotations();
    super.visitEnd();
  }

  /**
   * {@inheritDoc}
   * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String, boolean)
   */
  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    existingClassAnnotations.add(desc);
    // If annotation exists in scene, and in overwrite mode,
    //  return empty visitor, since annotation from scene will be visited later.
    if (aClass.lookup(classDescToName(desc)) != null
        && overwrite) {
      return EMPTY_ANNOTATION_VISITOR;
    }
    return super.visitAnnotation(desc, visible);
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(int typeRef,
      TypePath typePath, String desc, boolean visible) {
    existingClassAnnotations.add(desc);
    // If annotation exists in scene, and in overwrite mode,
    //  return empty visitor, annotation from scene will be visited later.
    if (aClass.lookup(classDescToName(desc)) != null
       && overwrite) {
      return EMPTY_ANNOTATION_VISITOR;
    }
    return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
  }

  /**
   * Returns an AnnotationVisitor over the given top-level type annotation.
   */
  private AnnotationVisitor visitTypeAnnotation(int typeRef,
      TypePath typePath, Annotation tla) {
    return super.visitTypeAnnotation(typeRef, typePath,
        classNameToDesc(name(tla)), isRuntimeRetention(tla));
  }

  /**
   * Returns an AnnotationVisitor over the given type annotation.
   */
  private AnnotationVisitor visitTypeAnnotation(TypeReference typeReference,
      InnerTypeLocation loc, Annotation tla) {
    int typeRef = typeReference.getValue();
    TypePath typePath = loc == null || loc.location.isEmpty() ? null
        : innerTypePath(loc);
    return visitTypeAnnotation(typeRef, typePath, tla);
  }

  /**
   * Converts InnerTypeLocation to TypePath.
   */
  static TypePath innerTypePath(InnerTypeLocation loc) {
    StringBuffer b = new StringBuffer();
    for (TypeAnnotationPosition.TypePathEntry tpe : loc.location) {
      switch (tpe.tag) {
      case ARRAY:
        b.append('['); break;
      case INNER_TYPE:
        b.append('.'); break;
      case TYPE_ARGUMENT:
        b.append(Integer.toString(tpe.arg));
        b.append(';'); break;
      case WILDCARD:
        b.append('*'); break;
      }
    }
    return TypePath.fromString(b.toString());
  }

  /**
   * Converts InnerTypeLocation to TypePath.
   */
  static InnerTypeLocation innerTypeLocation(TypePath typePath) {
    if (typePath != null) {
      // 2*typePath.getLength() upper bound on path length
      List<Integer> intsRep = new ArrayList<Integer>(2*typePath.getLength());
      String stringRep = typePath.toString();
      char[] charsRep = stringRep.toCharArray();
      int i = 0;
      while (i < charsRep.length) {
        switch (charsRep[i]) {
        case '[':
          intsRep.add(0);
          intsRep.add(0);
          break;
        case '.':
          intsRep.add(1);
          intsRep.add(0);
          break;
        case '*':
          intsRep.add(2);
          intsRep.add(0);
          break;
        default:
          if (Character.isDigit(charsRep[i])) {
            int j = stringRep.indexOf(';', i);
            if (j > i) {
              String s = stringRep.substring(i, j);
              int n = Integer.parseInt(s);
              intsRep.add(3);
              intsRep.add(n);
              i = j;
              break;
            }
          }
          throw new IllegalArgumentException();
        }
        ++i;
      }
      return new InnerTypeLocation(
          TypeAnnotationPosition.getTypePathFromBinary(intsRep));
    }
    return InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION;
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
        TypeReference typeReference =
            TypeReference.newTypeParameterBoundReference(
                TypeReference.METHOD_TYPE_PARAMETER,
                bloc.paramIndex, bloc.boundIndex);

        for (Annotation tla : bound.tlAnnotationsHere) {
          AnnotationVisitor xav =
              visitTypeAnnotation(typeReference, null, tla);

          if (bloc.boundIndex == -1) {
            visitTargetType(xav, TargetType.CLASS_TYPE_PARAMETER);
            visitBound(xav, bloc);
          } else {
            visitTargetType(xav, TargetType.CLASS_TYPE_PARAMETER_BOUND);
            visitBound(xav, bloc);
          }
          visitLocations(xav, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
          visitFields(xav, tla);
          xav.visitEnd();
        }

        for (Map.Entry<InnerTypeLocation, ATypeElement> e2 :
          bound.innerTypes.entrySet()) {
          InnerTypeLocation itloc = e2.getKey();
          ATypeElement innerType = e2.getValue();

          for (Annotation tla : innerType.tlAnnotationsHere) {
            AnnotationVisitor xav =
                visitTypeAnnotation(typeReference, itloc, tla);

            visitTargetType(xav, TargetType.CLASS_TYPE_PARAMETER_BOUND);
            visitBound(xav, bloc);
            visitLocations(xav, itloc);
            visitFields(xav, tla);
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
  static boolean isRuntimeRetention(Annotation tla) {
    if (tla.def.retention() == null)
      return false; // TODO: temporary
    return tla.def.retention().equals(RetentionPolicy.RUNTIME);
  }

  /**
   * Returns the name of the annotation in the top level.
   */
  static String name(Annotation tla) {
    return tla.def().name;
  }

  /**
   * Wraps the given class name in a class descriptor.
   */
  static String classNameToDesc(String name) {
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
   * Has av visit the fields in the given annotation.
   * This method is necessary even with
   * visitFields(AnnotationVisitor, Annotation)
   * because a Annotation cannot be created from the Annotation
   * specified to be available from the Annotation object for subannotations.
   */
  private void visitFields(AnnotationVisitor av, Annotation a) {
    if (av instanceof XAnnotationVisitor) {
      ((XAnnotationVisitor) av).visitXNameAndArgsSize();
    }
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
        av.visit(fieldName, org.objectweb.asm.Type.getType((Class<?>)value));
      } else {
        // everything else is a string
        av.visit(fieldName, value);
      }
    }
  }

  /**
   * Has xav visit the given target type.
   */
  private void visitTargetType(AnnotationVisitor xav, TargetType t) {
    if (xav instanceof XAnnotationVisitor) {
      ((XAnnotationVisitor) xav).visitXTargetType(t.targetTypeValue());
    }
  }

  /**
   * Have xav visit the location length  and all locations in loc.
   */
  private void visitLocations(AnnotationVisitor xav, InnerTypeLocation loc) {
    if (loc == null || loc.location == null) {
      ((XAnnotationVisitor) xav).visitXLocationLength(0);
    } else {
      List<TypeAnnotationPosition.TypePathEntry> location = loc.location;
      if (xav instanceof XAnnotationVisitor) {
        ((XAnnotationVisitor) xav).visitXLocationLength(location.size());
        for (TypeAnnotationPosition.TypePathEntry l : location) {
          ((XAnnotationVisitor) xav).visitXLocation(l);
        }
      }
    }
  }

  /**
   * Has xav visit the local varialbe information in loc.
   */
  private void visitLocalVar(AnnotationVisitor xav, LocalLocation loc) {
    if (xav instanceof XAnnotationVisitor) {
      ((XAnnotationVisitor) xav).visitXNumEntries(1);
      ((XAnnotationVisitor) xav).visitXStartPc(loc.scopeStart);
      ((XAnnotationVisitor) xav).visitXLength(loc.scopeLength);
      ((XAnnotationVisitor) xav).visitXIndex(loc.index);
    }
  }

  /**
   * Has xav visit the offset.
   */
  private void visitOffset(AnnotationVisitor xav, int offset) {
    if (xav instanceof XAnnotationVisitor) {
      ((XAnnotationVisitor) xav).visitXOffset(offset);
    }
  }

  private void visitParameterIndex(AnnotationVisitor xav, int index) {
    if (xav instanceof XAnnotationVisitor) {
      ((XAnnotationVisitor) xav).visitXParamIndex(index);
    }
  }

  private void visitTypeIndex(AnnotationVisitor xav, int index) {
    if (xav instanceof XAnnotationVisitor) {
      ((XAnnotationVisitor) xav).visitXTypeIndex(index);
    }
  }

  /**
   * Has xav visit the type parameter bound information in loc.
   */
  private void visitBound(AnnotationVisitor xav, BoundLocation loc) {
    if (xav instanceof XAnnotationVisitor) {
      ((XAnnotationVisitor) xav).visitXParamIndex(loc.paramIndex);
      if (loc.boundIndex != -1) {
        ((XAnnotationVisitor) xav).visitXBoundIndex(loc.boundIndex);
      }
    }
  }

  /**
   * A FieldAnnotationSceneWriter is a wrapper class around a FieldVisitor that
   * delegates all calls to its internal FieldVisitor, and on a call to
   * visitEnd(), also has its internal FieldVisitor visit all the
   * corresponding field annotations in scene.
   */
  private class FieldAnnotationSceneWriter extends FieldVisitor {
    // After being constructed, none of these fields should be null.

    /**
     * Internal FieldVisitor all calls are delegated to.
     */
    private final FieldVisitor fv;

    /**
     * List of all annotations this has already visited.
     */
    private final List<String> existingFieldAnnotations;

    /**
     * The AElement that represents this field in the AScene the
     *   class is visiting.
     */
    private final AElement aField;

    /**
     * Constructs a new FieldAnnotationSceneWriter with the given name that
     * wraps the given FieldVisitor.
     */
    public FieldAnnotationSceneWriter(String name, FieldVisitor fv) {
      super(Opcodes.ASM5);
      this.fv = fv;
      this.existingFieldAnnotations = new ArrayList<String>();
      this.aField = aClass.fields.vivify(name);
    }

    /**
     * {@inheritDoc}
     * @see org.objectweb.asm.FieldVisitor#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      existingFieldAnnotations.add(desc);

      // If annotation exists in scene, and in overwrite mode,
      //  return empty visitor, annotation from scene will be visited later.
      if (aField.lookup(classDescToName(desc)) != null
          && overwrite)
        return EMPTY_ANNOTATION_VISITOR;

      return fv.visitAnnotation(desc, visible);
    }

    /**
     * {@inheritDoc}
     * @see org.objectweb.asm.FieldVisitor#visitTypeAnnotation(int, org.objectweb.asm.TypePath, java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef,
        TypePath typePath, String desc, boolean visible) {
      existingFieldAnnotations.add(desc);

      // If annotation exists in scene, and in overwrite mode,
      //  return empty visitor, annotation from scene will be visited later.
      if (aField.lookup(classDescToName(desc)) != null
         && overwrite)
        return EMPTY_ANNOTATION_VISITOR;

      return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
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
      TypeReference typeReference =
          TypeReference.newTypeReference(TypeReference.FIELD);
      int typeRef = typeReference.getValue();

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
        AnnotationVisitor av =
            fv.visitTypeAnnotation(typeRef, null,
                classNameToDesc(name(tla)), isRuntimeRetention(tla));
        visitTargetType(av, TargetType.FIELD);
        visitLocations(av, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
        visitFields(av, tla);
        av.visitEnd();
      }

      // Now do field generics/arrays.
      for (Map.Entry<InnerTypeLocation, ATypeElement> fieldInnerEntry :
        aField.type.innerTypes.entrySet()) {
        InnerTypeLocation loc = fieldInnerEntry.getKey();
        TypePath typePath = innerTypePath(loc);

        for (Annotation tla : fieldInnerEntry.getValue().tlAnnotationsHere) {
          if ((!overwrite) && existingFieldAnnotations.contains(name(tla))) {
            continue;
          }
          AnnotationVisitor xav =
              fv.visitTypeAnnotation(typeRef, typePath,
                  classNameToDesc(name(tla)), isRuntimeRetention(tla));
          visitTargetType(xav, TargetType.FIELD);
          visitLocations(xav, loc);
          visitFields(xav, tla);
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
  private class MethodAnnotationSceneWriter extends XMethodVisitor {
    // basic strategy:
    // ensureMethodVisitSceneAnnotation will be called, if it has not already
    // been called, at the beginning of visitCode, visitEnd

    /**
     * The AMethod that represents this method in scene.
     */
    private final AMethod aMethod;

    /**
     * Whether or not this has visit the method's annotations in scene.
     */
    private boolean hasVisitedMethodAnnotations;

    /**
     * Whether or not this has visit the method's code attribute's
     * annotations in scene.
     */
    private boolean hasVisitedCodeAnnotations;

    /**
     * The existing annotations this method has visited.
     */
    private final List<String> existingMethodAnnotations;

    private final LocalVarTable localVarTable;
    private final Map<Integer, Label> labels;

    /**
     * Constructs a new MethodAnnotationSceneWriter with the given name and
     * description that wraps around the given MethodVisitor.
     *
     * @param name the name of the method, as in "foo"
     * @param desc the method signature minus the name,
     *  as in "(Ljava/lang/String)V"
     * @param mv the method visitor to wrap around
     */
    MethodAnnotationSceneWriter(AMethod aMethod, MethodVisitor mv) {
      super(ClassAnnotationSceneWriter.this.api, mv);
      this.hasVisitedMethodAnnotations = false;
      this.aMethod = aMethod;
      this.existingMethodAnnotations = new ArrayList<String>();
      this.localVarTable = new LocalVarTable();
      this.labels = new TreeMap<Integer, Label>();
    }

    /**
     * {@inheritDoc}
     * @see org.objectweb.asm.MethodVisitor#visitCode()
     */
    @Override
    public void visitCode() {
      super.visitCode();
    }

    @Override
    public void visitLabel(Label label) {
      super.visitLabel(label);
      labels.put(label.getOffset(), label);
    }

    @Override
    public void visitFieldInsn(int opcode,
        String owner, String name, String desc) {
      super.visitFieldInsn(opcode, owner, name, desc);
      track();
    }

    @Override
    public void visitIincInsn(int var, int increment) {
      super.visitIincInsn(var, increment);
      track();
    }

    @Override
    public void visitInsn(int opcode) {
      super.visitInsn(opcode);
      track();
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
      super.visitIntInsn(opcode, operand);
      track();
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc,
        Handle bsm, Object... bsmArgs) {
      super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
      track();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      super.visitJumpInsn(opcode, label);
      track();
    }

    @Override
    public void visitLdcInsn(Object cst) {
      super.visitLdcInsn(cst);
      track();
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys,
        Label[] labels) {
      super.visitLookupSwitchInsn(dflt, keys, labels);
      track();
    }

    @Override
    public void visitMethodInsn(int opcode,
        String owner, String name, String desc, boolean itf) {
      super.visitMethodInsn(opcode, owner, name, desc, itf);
      track();
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
      super.visitMultiANewArrayInsn(desc, dims);
      track();
    }

    @Override
    public void visitTableSwitchInsn(int min, int max,
        Label dflt, Label... labels) {
      super.visitTableSwitchInsn(min, max, dflt, labels);
      track();
    }

    @Override
    public void visitTypeInsn(int opcode, String desc) {
      super.visitTypeInsn(opcode, desc);
      track();
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      super.visitVarInsn(opcode, var);
      track();
    }

    @Override
    public void visitLocalVariable(String name, String desc,
        String signature, Label start, Label end, int index) {
      super.visitLocalVariable(name, desc, signature, start, end, index);
      localVarTable.put(name, desc, signature, start, end, index);
    }

    /**
     * {@inheritDoc}
     * @see org.objectweb.asm.MethodVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
//      super.visitLabel(new Label());
      ensureVisitSceneMethodAnnotations();
      ensureVisitCodeAnnotations();
      super.visitEnd();
    }

    /**
     * {@inheritDoc}
     * @see org.objectweb.asm.MethodVisitor#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      existingMethodAnnotations.add(desc);
      // If annotation exists in scene, and in overwrite mode,
      //  return empty visitor, annotation from scene will be visited later.
      if (shouldSkipExisting(classDescToName(desc))) {
        return EMPTY_ANNOTATION_VISITOR;
      }

      return super.visitAnnotation(desc, visible);
    }

    /**
     * {@inheritDoc}
     * @see org.objectweb.asm.MethodVisitor#visitTypeAnnotation(int, org.objectweb.asm.TypePath, java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef,
        TypePath typePath, String desc, boolean visible) {

      existingMethodAnnotations.add(desc);

      // If annotation exists in scene, and in overwrite mode,
      //  return empty visitor, annotation from scene will be visited later.
      if (shouldSkipExisting(classDescToName(desc))) {
        return EMPTY_ANNOTATION_VISITOR;
      }

      return new XAnnotationVisitor(api,
          super.visitTypeAnnotation(typeRef, typePath, desc, visible));
    }

    /**
     * Has this visit the extended annotation in tla and returns the
     * resulting visitor.
     */
    private AnnotationVisitor visitTypeAnnotation(int typeRef,
        TypePath typePath, Annotation tla) {
      return visitTypeAnnotation(typeRef, typePath,
          classNameToDesc(name(tla)), isRuntimeRetention(tla));
    }

    private AnnotationVisitor
    visitTypeAnnotation(TypeReference typeReference,
        InnerTypeLocation loc, Annotation tla) {
      int typeRef = typeReference.getValue();
      TypePath typePath = loc == null || loc.location.isEmpty() ? null
          : innerTypePath(loc);
      return visitTypeAnnotation(typeRef, typePath, tla);
    }

    private AnnotationVisitor
    visitTypeAnnotation(TypeReference typeReference, Annotation tla) {
      return visitTypeAnnotation(typeReference, null, tla);
    }

    private AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
        LocalLocation localLocation, Annotation tla) {
      return visitLocalVariableAnnotation(typeRef,
          null, localLocation, tla);
    }

    private AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
        InnerTypeLocation innerLocation, LocalLocation localLocation,
        Annotation tla) {
      TypePath typePath = innerLocation == null ? null
          : innerTypePath(innerLocation);
      Label start = labels.get(localLocation.scopeStart);
      if (start == null) {
        return EMPTY_ANNOTATION_VISITOR;
      }
      Label end = labels.get(localLocation.scopeStart
          + localLocation.scopeLength);
      if (end == null) {
        visitLabel(new Label());
        end = labels.get(localLocation.scopeStart
            + localLocation.scopeLength);
      }

      return new XAnnotationVisitor(api,
          super.visitLocalVariableAnnotation(typeRef, typePath,
              new Label[] { start }, new Label[] { end },
              new int[] { localLocation.index },
              classNameToDesc(name(tla)), isRuntimeRetention(tla)));
    }

    private AnnotationVisitor visitInsnAnnotation(int typeSort,
        int typeIndex, Annotation tla) {
      return visitInsnAnnotation(typeSort, typeIndex, tla, null);
    }

    private AnnotationVisitor visitInsnAnnotation(int typeSort,
        int typeIndex, Annotation tla, InnerTypeLocation loc) {
      TypeReference typeReference;
      TypePath typePath = loc == null ? null : innerTypePath(loc);
      String desc = classNameToDesc(name(tla));
      boolean visible = isRuntimeRetention(tla);

      switch (typeSort) {
      case TypeReference.INSTANCEOF:
      {
        typeReference =
            TypeReference.newTypeReference(typeSort);
        break;
      }

      case TypeReference.NEW:
      {
        typeReference =
            TypeReference.newTypeReference(typeSort);
        break;
      }

      case TypeReference.CAST:
      {
        typeReference =
            TypeReference.newTypeArgumentReference(typeSort, typeIndex);
        break;
      }
      default:
        throw new IllegalArgumentException();
      }

      return super.visitInsnAnnotation(typeReference.getValue(),
          typePath, desc, visible);
    }

    private void track() {
      track(TypeReference.INSTANCEOF, 0, aMethod.body.instanceofs);
      track(TypeReference.NEW, 0, aMethod.body.news);
      for (Map.Entry<RelativeLocation, ATypeElement> entry :
          aMethod.body.typecasts.entrySet()) {
        RelativeLocation loc = entry.getKey();
        if (loc.isBytecodeOffset() && loc.offset == getPreviousCodeOffset()) {
          track(TypeReference.CAST, loc.type_index, aMethod.body.typecasts);
        }
      }
    }

    private void track(int typeSort, int typeIndex,
        Map<RelativeLocation, ATypeElement> map) {
      RelativeLocation loc =
          RelativeLocation.createOffset(getPreviousCodeOffset(), typeIndex);
      ATypeElement elem = map.get(loc);

      if (elem != null) {
        for (Annotation tla : elem.tlAnnotationsHere) {
          visitInsnAnnotation(typeSort, typeIndex, tla);
        }

        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
            elem.innerTypes.entrySet()) {
          InnerTypeLocation innerLoc = e.getKey();
          ATypeElement inner = e.getValue();

          for (Annotation tla : inner.tlAnnotationsHere) {
            visitInsnAnnotation(typeSort, typeIndex, tla, innerLoc);
          }
        }
      }
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
      TypeReference typeReference =
          TypeReference.newTypeReference(TypeReference.METHOD_RETURN);
      // Standard annotations on return type.
      for (Annotation tla : aMethod.returnType.tlAnnotationsHere) {
        if (shouldSkip(tla)) continue;

        AnnotationVisitor av = visitTypeAnnotation(typeReference, tla);
        visitTargetType(av, TargetType.METHOD_RETURN);
        visitLocations(av, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
        visitFields(av, tla);
        av.visitEnd();
      }

      // Now do generic/array information on return type
      for (Map.Entry<InnerTypeLocation, ATypeElement> e :
        aMethod.returnType.innerTypes.entrySet()) {
        InnerTypeLocation loc = e.getKey();
        ATypeElement innerType = e.getValue();

        for (Annotation tla : innerType.tlAnnotationsHere) {
          AnnotationVisitor xav = visitTypeAnnotation(typeReference, loc, tla);

          visitTargetType(xav, TargetType.METHOD_RETURN);
          // information for raw type (return type)
          //  (none)
          // information for generic/array (on return type)
          visitLocations(xav, loc);
          visitFields(xav, tla);
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
        TypeReference typeReference =
            TypeReference.newTypeParameterBoundReference(
                TypeReference.METHOD_TYPE_PARAMETER,
                bloc.paramIndex, bloc.boundIndex);

        for (Annotation tla : bound.tlAnnotationsHere) {
          AnnotationVisitor xav = visitTypeAnnotation(typeReference, tla);

          if (bloc.boundIndex == -1) {
            visitTargetType(xav, TargetType.METHOD_TYPE_PARAMETER);
            visitBound(xav, bloc);
          } else {
            visitTargetType(xav, TargetType.METHOD_TYPE_PARAMETER_BOUND);
            visitBound(xav, bloc);
          }
          visitLocations(xav, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
          visitFields(xav, tla);
          xav.visitEnd();
        }

        for (Map.Entry<InnerTypeLocation, ATypeElement> e2 :
          bound.innerTypes.entrySet()) {
          InnerTypeLocation itloc = e2.getKey();
          ATypeElement innerType = e2.getValue();

          for (Annotation tla : innerType.tlAnnotationsHere) {
            AnnotationVisitor xav = visitTypeAnnotation(typeReference, itloc, tla);

            visitTargetType(xav, TargetType.METHOD_TYPE_PARAMETER_BOUND);
            visitBound(xav, bloc);
            visitLocations(xav, itloc);
            visitFields(xav, tla);
            xav.visitEnd();
          }
        }
      }
    }

    /**
     * Has this visit the annotations on local variables in this method.
     */
    private void ensureVisitLocalVariablesAnnotations() {
      for (Map.Entry<LocalLocation, AField> entry :
          aMethod.body.locals.entrySet()) {
        LocalLocation localLocation = entry.getKey();
        AElement aLocation = entry.getValue();
        TypeReference typeReference =
            TypeReference.newTypeReference(TypeReference.LOCAL_VARIABLE);
        int typeRef = typeReference.getValue();

        for (Annotation tla : aLocation.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          AnnotationVisitor xav =
              visitLocalVariableAnnotation(typeRef, localLocation, tla);
          visitTargetType(xav, TargetType.LOCAL_VARIABLE);
          visitLocalVar(xav, localLocation);
          visitLocations(xav, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
          visitFields(xav, tla);
          xav.visitEnd();
        }

        // now do annotations on inner type of aLocation (local variable)
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
          aLocation.type.innerTypes.entrySet()) {
          InnerTypeLocation localVariableLocation = e.getKey();
          ATypeElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            AnnotationVisitor xav = visitLocalVariableAnnotation(typeRef,
                localVariableLocation, localLocation, tla);
            visitTargetType(xav, TargetType.LOCAL_VARIABLE);
            // information for raw type (local variable)
            visitLocalVar(xav, localLocation);
            // information for generic/array (on local variable)
            visitLocations(xav, localVariableLocation);
            visitFields(xav, tla);
            xav.visitEnd();
          }

        }
      }
    }

    /**
     * Has this visit the object creation (new) annotations on this method.
     */
    private void ensureVisitObjectCreationAnnotations() {
      TypeReference typeReference =
          TypeReference.newTypeReference(TypeReference.NEW);
      for (Map.Entry<RelativeLocation, ATypeElement> entry :
          aMethod.body.news.entrySet()) {
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

          AnnotationVisitor xav = visitTypeAnnotation(typeReference, tla);
          visitTargetType(xav, TargetType.NEW);
          visitOffset(xav, offset);
          visitLocations(xav, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
          visitFields(xav, tla);
          xav.visitEnd();
        }

        // now do inner annotations on aNew (object creation)
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
          aNew.innerTypes.entrySet()) {
          InnerTypeLocation aNewLocation = e.getKey();
          ATypeElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            AnnotationVisitor xav = visitTypeAnnotation(typeReference,
                aNewLocation, tla);
            visitTargetType(xav, TargetType.NEW);
            // information for raw type (object creation)
            visitOffset(xav, offset);
            // information for generic/array (on object creation)
            visitLocations(xav, aNewLocation);
            visitFields(xav, tla);
            xav.visitEnd();
          }
        }
      }
    }

    /**
     * Has this visit the parameter annotations on this method.
     */
    private void ensureVisitParameterAnnotations() {
      for (Map.Entry<Integer, AField> entry :
          aMethod.parameters.entrySet()) {
        AField aParameter = entry.getValue();
        int index = entry.getKey();
        TypeReference typeReference =
            TypeReference.newFormalParameterReference(index);

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

            AnnotationVisitor av = visitTypeAnnotation(typeReference, tla);
            visitTargetType(av, TargetType.METHOD_FORMAL_PARAMETER);
            visitParameterIndex(av, index);
            visitLocations(av, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
            visitFields(av, tla);
            av.visitEnd();
        }

        // now handle inner annotations on aParameter (parameter)
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
          aParameter.type.innerTypes.entrySet()) {
          InnerTypeLocation aParameterLocation = e.getKey();
          ATypeElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            AnnotationVisitor xav = visitTypeAnnotation(typeReference,
                aParameterLocation, tla);
            visitTargetType(xav,
                TargetType.METHOD_FORMAL_PARAMETER);
            // information for raw type (parameter)
            //  (none)
            // information for generic/array (on parameter)
            visitParameterIndex(xav, index);
            visitLocations(xav, aParameterLocation);
            visitFields(xav, tla);
            xav.visitEnd();
          }
        }
      }
    }

    /**
     * Has this visit the receiver annotations on this method.
     */
    private void ensureVisitReceiverAnnotations() {
      TypeReference typeReference =
          TypeReference.newTypeReference(TypeReference.METHOD_RECEIVER);
      AField aReceiver = aMethod.receiver;

      //for (Annotation tla : aReceiver.tlAnnotationsHere) {
      //  if (shouldSkip(tla)) continue;
      //
      //  AnnotationVisitor av = visitTypeAnnotation(tla, false);  // FIXME
      //  visitTargetType(av, TargetType.METHOD_RECEIVER);
      //  visitLocations(av, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
      //  visitFields(av, tla);
      //  av.visitEnd();
      //}

      for (Annotation tla : aReceiver.type.tlAnnotationsHere) {
        if (shouldSkip(tla)) continue;

        AnnotationVisitor xav = visitTypeAnnotation(typeReference, tla);
        visitTargetType(xav, TargetType.METHOD_RECEIVER);
        visitLocations(xav, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
        visitFields(xav, tla);
        xav.visitEnd();
      }

      // now do inner annotations of aReceiver
      for (Map.Entry<InnerTypeLocation, ATypeElement> e :
          aReceiver.type.innerTypes.entrySet()) {
        InnerTypeLocation aReceiverLocation = e.getKey();
        ATypeElement aInnerType = e.getValue();
        for (Annotation tla : aInnerType.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          AnnotationVisitor xav = visitTypeAnnotation(typeReference,
              aReceiverLocation, tla);
          visitTargetType(xav, TargetType.METHOD_RECEIVER);
          // information for generic/array (on receiver)
          visitLocations(xav, aReceiverLocation);
          visitFields(xav, tla);
          xav.visitEnd();
        }
      }

    }

    /**
     * Has this visit the typecast annotations on this method.
     */
    private void ensureVisitTypecastAnnotations() {
      for (Map.Entry<RelativeLocation, ATypeElement> entry :
          aMethod.body.typecasts.entrySet()) {
        if(!entry.getKey().isBytecodeOffset()) {
          // if the RelativeLocation is a source index, we cannot insert it
          // into bytecode
          // TODO: output a warning or translate
          if (strict) { System.err.println("ClassAnnotationSceneWriter.ensureVisitTypecastAnnotation: no bytecode offset found!"); }
        }
        int offset = entry.getKey().offset;
        int typeIndex = entry.getKey().type_index;
        ATypeElement aTypecast = entry.getValue();
        TypeReference typeReference =
            TypeReference.newTypeArgumentReference(TypeReference.CAST,
                typeIndex);

        for (Annotation tla : aTypecast.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          AnnotationVisitor xav = visitTypeAnnotation(typeReference, tla);
          visitTargetType(xav, TargetType.CAST);
          visitOffset(xav, offset);
          visitTypeIndex(xav, typeIndex);
          visitLocations(xav, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
          visitFields(xav, tla);
          xav.visitEnd();
        }

        // now do inner annotations of aTypecast (typecast)
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
          aTypecast.innerTypes.entrySet()) {
          InnerTypeLocation aTypecastLocation = e.getKey();
          ATypeElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            AnnotationVisitor xav = visitTypeAnnotation(typeReference,
                aTypecastLocation, tla);
            visitTargetType(xav, TargetType.CAST);
            // information for raw type (typecast)
            visitOffset(xav, offset);
            visitTypeIndex(xav, typeIndex);
            // information for generic/array (on typecast)
            visitLocations(xav, aTypecastLocation);
            visitFields(xav, tla);
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
        aMethod.body.instanceofs.entrySet()) {
        if (!entry.getKey().isBytecodeOffset()) {
          // if the RelativeLocation is a source index, we cannot insert it
          // into bytecode
          // TODO: output a warning or translate
          if (strict) { System.err.println("ClassAnnotationSceneWriter.ensureVisitTypeTestAnnotation: no bytecode offset found!"); }
        }
        int offset = entry.getKey().offset;
        ATypeElement aTypeTest = entry.getValue();
        TypeReference typeReference =
            TypeReference.newTypeReference(TypeReference.INSTANCEOF);

        for (Annotation tla : aTypeTest.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          AnnotationVisitor xav = visitTypeAnnotation(typeReference, tla);
          visitTargetType(xav, TargetType.INSTANCEOF);
          visitOffset(xav, offset);
          visitLocations(xav, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
          visitFields(xav, tla);
          xav.visitEnd();
        }

        // now do inner annotations of aTypeTest (typetest)
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
          aTypeTest.innerTypes.entrySet()) {
          InnerTypeLocation aTypeTestLocation = e.getKey();
          AElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            AnnotationVisitor xav = visitTypeAnnotation(typeReference,
                aTypeTestLocation, tla);
            visitTargetType(xav, TargetType.INSTANCEOF);
            // information for raw type (typetest)
            visitOffset(xav, offset);
            // information for generic/array (on typetest)
            visitLocations(xav, aTypeTestLocation);
            visitFields(xav, tla);
            xav.visitEnd();
          }
        }
      }
    }

    private void ensureVisitLambdaExpressionAnnotations() {
      for (Map.Entry<RelativeLocation, AMethod> entry :
          aMethod.body.funs.entrySet()) {
        if (!entry.getKey().isBytecodeOffset()) {
          // if the RelativeLocation is a source index, we cannot insert it
          // into bytecode
          // TODO: output a warning or translate
          if (strict) { System.err.println("ClassAnnotationSceneWriter.ensureMemberReferenceAnnotations: no bytecode offset found!"); }
          continue;
        }
        //int offset = entry.getKey().offset;
        //int typeIndex = entry.getKey().type_index;
        AMethod aLambda = entry.getValue();

        for (Map.Entry<Integer, AField> e0 : aLambda.parameters.entrySet()) {
          AField aParameter = e0.getValue();
          int index = e0.getKey();
          TypeReference typeReference =
              TypeReference.newFormalParameterReference(index);

          for (Annotation tla : aParameter.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            AnnotationVisitor av = visitParameterAnnotation(tla, index);
            visitFields(av, tla);
            av.visitEnd();
          }

          for (Annotation tla : aParameter.type.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            AnnotationVisitor xav = visitTypeAnnotation(typeReference, tla);
            visitTargetType(xav, TargetType.METHOD_FORMAL_PARAMETER);
            //visitOffset(xav, offset);
            //visitTypeIndex(xav, typeIndex);
            visitParameterIndex(xav, index);
            visitLocations(xav, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
            visitFields(xav, tla);
            xav.visitEnd();
          }

          for (Map.Entry<InnerTypeLocation, ATypeElement> e1 :
              aParameter.type.innerTypes.entrySet()) {
            InnerTypeLocation aParameterLocation = e1.getKey();
            ATypeElement aInnerType = e1.getValue();
            for (Annotation tla : aInnerType.tlAnnotationsHere) {
              if (shouldSkip(tla)) continue;

              AnnotationVisitor xav = visitTypeAnnotation(typeReference,
                  aParameterLocation, tla);
              visitTargetType(xav, TargetType.METHOD_FORMAL_PARAMETER);
              //visitOffset(xav, offset);
              //visitTypeIndex(xav, typeIndex);
              visitParameterIndex(xav, index);
              visitLocations(xav, aParameterLocation);
              visitFields(xav, tla);
              xav.visitEnd();
            }
          }
        }
      }
    }

    private void ensureVisitMemberReferenceAnnotations() {
      for (Map.Entry<RelativeLocation, ATypeElement> entry :
          aMethod.body.refs.entrySet()) {
        if (!entry.getKey().isBytecodeOffset()) {
          // if the RelativeLocation is a source index, we cannot insert it
          // into bytecode
          // TODO: output a warning or translate
          if (strict) { System.err.println("ClassAnnotationSceneWriter.ensureMemberReferenceAnnotations: no bytecode offset found!"); }
          continue;
        }
        int offset = entry.getKey().offset;
        int typeIndex = entry.getKey().type_index;
        ATypeElement aTypeArg = entry.getValue();
        Set<Integer> lset = lambdaExpressions.get(aMethod.methodName);
        if (lset.contains(offset)) { continue; }  // something's wrong
        Set<Integer> cset = dynamicConstructors.get(aMethod.methodName);
        TargetType tt = cset != null && cset.contains(offset)
                ? TargetType.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT
                : TargetType.METHOD_REFERENCE_TYPE_ARGUMENT;
        int sort = cset != null && cset.contains(offset)
            ? TypeReference.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT
            : TypeReference.METHOD_REFERENCE_TYPE_ARGUMENT;
        TypeReference typeReference =
            TypeReference.newTypeArgumentReference(sort, offset);

        for (Annotation tla : aTypeArg.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          AnnotationVisitor xav = visitTypeAnnotation(typeReference, tla);
          visitTargetType(xav, tt);
          visitOffset(xav, offset);
          visitTypeIndex(xav, typeIndex);
          visitLocations(xav, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
          visitFields(xav, tla);
          xav.visitEnd();
        }

        // now do inner annotations of member reference
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
            aTypeArg.innerTypes.entrySet()) {
          InnerTypeLocation aTypeArgLocation = e.getKey();
          AElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            AnnotationVisitor xav = visitTypeAnnotation(typeReference,
                aTypeArgLocation, tla);
            visitTargetType(xav, tt);
            visitOffset(xav, offset);
            visitTypeIndex(xav, typeIndex);
            visitLocations(xav, aTypeArgLocation);
            visitFields(xav, tla);
            xav.visitEnd();
          }
        }
      }
    }

    private void ensureVisitMethodInvocationAnnotations() {
      for (Map.Entry<RelativeLocation, ATypeElement>
          entry : aMethod.body.calls.entrySet()) {
        if (!entry.getKey().isBytecodeOffset()) {
          // if the RelativeLocation is a source index, we cannot insert it
          // into bytecode
          // TODO: output a warning or translate
          if (strict) { System.err.println("ClassAnnotationSceneWriter.ensureVisitMethodInvocationAnnotations: no bytecode offset found!"); }
        }
        int offset = entry.getKey().offset;
        int typeIndex = entry.getKey().type_index;
        ATypeElement aCall = entry.getValue();
        Set<Integer> cset = dynamicConstructors.get(aMethod.methodName);
        TargetType tt = cset != null && cset.contains(offset)
                ? TargetType.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
                : TargetType.METHOD_INVOCATION_TYPE_ARGUMENT;
        TypeReference typeReference = TypeReference.newTypeReference(
            tt.targetTypeValue());

        for (Annotation tla : aCall.tlAnnotationsHere) {
          if (shouldSkip(tla)) continue;

          AnnotationVisitor xav = visitTypeAnnotation(typeReference, tla);
          visitTargetType(xav, tt);
          visitOffset(xav, offset);
          visitTypeIndex(xav, typeIndex);
          visitLocations(xav, InnerTypeLocation.EMPTY_INNER_TYPE_LOCATION);
          visitFields(xav, tla);
          xav.visitEnd();
        }

        // now do inner annotations of call
        for (Map.Entry<InnerTypeLocation, ATypeElement> e :
            aCall.innerTypes.entrySet()) {
          InnerTypeLocation aCallLocation = e.getKey();
          AElement aInnerType = e.getValue();
          for (Annotation tla : aInnerType.tlAnnotationsHere) {
            if (shouldSkip(tla)) continue;

            AnnotationVisitor xav =
                visitTypeAnnotation(typeReference, aCallLocation, tla);
            visitTargetType(xav, TargetType.INSTANCEOF);
            visitOffset(xav, offset);
            visitTypeIndex(xav, typeIndex);
            visitLocations(xav, aCallLocation);
            visitFields(xav, tla);
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
        // typecasts, and type argument annotations, which will all be
        // extended annotations
        ensureVisitTypeParameterBoundAnnotations();
        ensureVisitParameterAnnotations();
        ensureVisitReceiverAnnotations();
        //ensureVisitObjectCreationAnnotations();
        //ensureVisitTypecastAnnotations();
        //ensureVisitTypeTestAnnotations();
        ensureVisitLambdaExpressionAnnotations();
        ensureVisitMemberReferenceAnnotations();
        ensureVisitMethodInvocationAnnotations();
        // TODO: throw clauses?!
      }
    }

    private void ensureVisitCodeAnnotations() {
      if (!hasVisitedCodeAnnotations) {
        hasVisitedCodeAnnotations = true;

        ensureVisitLocalVariablesAnnotations();
        // TODO: catch clauses!?
      }
    }
  }

  class MethodCodeIndexer extends /*X*/ClassVisitor {
    private int codeStart = 0;
    Set<Integer> constrs;  // distinguishes constructors from methods
    Set<Integer> lambdas;  // distinguishes lambda exprs from member refs

    MethodCodeIndexer() {
      super(Opcodes.ASM5);
      int fieldCount;
      // const pool size is (not lowest) upper bound of string length
      codeStart = cr.header + 6;
      codeStart += 2 + 2 * cr.readUnsignedShort(codeStart);
      fieldCount = cr.readUnsignedShort(codeStart);
      codeStart += 2;
      while (--fieldCount >= 0) {
        int attrCount = cr.readUnsignedShort(codeStart + 6);
        codeStart += 8;
        while (--attrCount >= 0) {
          codeStart += 6 + cr.readInt(codeStart + 2);
        }
      }
      codeStart += 2;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
        String superName, String[] interfaces) {
    }

    @Override
    public void visitSource(String source, String debug) {}

    @Override
    public void visitOuterClass(String owner, String name, String desc) {}

    @Override
    public void visitInnerClass(String name, String outerName,
        String innerName, int access) {
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc,
        String signature, Object value) {
      return null;
    }

    @Override
    public MethodVisitor visitMethod(int access,
        String name, String desc, String signature, String[] exceptions) {
      String methodDescription = name + desc;
      constrs = dynamicConstructors.get(methodDescription);
      if (constrs == null) {
        constrs = new TreeSet<Integer>();
        dynamicConstructors.put(methodDescription, constrs);
      }
      lambdas = lambdaExpressions.get(methodDescription);
      if (lambdas == null) {
        lambdas = new TreeSet<Integer>();
        lambdaExpressions.put(methodDescription, lambdas);
      }

      return new XMethodVisitor(Opcodes.ASM5,
          new MethodCodeOffsetAdapter(cr, new MethodVisitor(Opcodes.ASM5) {}, codeStart) {
//              private int lastLabelOffset;
//              @Override
//              public void visitLabel(Label label) {
//                lastLabelOffset = getCurrentOffset();
//              }
//              @Override
//              public void visitLocalVariable(String name, String desc,
//                  String signature, Label start, Label end, int index) {
//                super.visitLocalVariable(name, desc,
//                    signature, start, end, index);
//              }
              @Override
              public void visitInvokeDynamicInsn(String name,
                  String desc, Handle bsm, Object... bsmArgs) {
                String methodName = ((Handle) bsmArgs[1]).getName();
                int off = getCurrentOffset();
                if ("<init>".equals(methodName)) {
                  constrs.add(off);
                } else {
                  int ix = methodName.lastIndexOf('.');
                  if (ix >= 0) {
                    methodName = methodName.substring(ix+1);
                  }
                  if (methodName.startsWith("lambda$")) {
                    lambdas.add(off);
                  }
                }
                super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
              }
//              @Override
//              public void visitEnd() {
//                if (lastLabelOffset != getCurrentOffset()) {
//                  visitLabel(new Label());
//                }
//                super.visitEnd();
//              }
          });
    }
  }
}
