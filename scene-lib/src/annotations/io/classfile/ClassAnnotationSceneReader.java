// This class is a complete ClassVisitor with many hidden classes that do
// the work of reading annotations from a class file and inserting them into
// an AScene.
package annotations.io.classfile;

import checkers.nullness.quals.*;

import java.util.*;
import java.io.*;
import java.lang.annotation.RetentionPolicy;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.TypeAnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import com.sun.tools.javac.code.TargetType;

import annotations.*;
import annotations.el.*;
import annotations.field.*;

/**
 * A <code> ClassAnnotationSceneReader </code> is a
 * {@link org.objectweb.asm.ClassVisitor} that will insert all annotations it
 * encounters while visiting a class into a given {@link AScene}.
 *
 * The "read" in <code> ClassAnnotationSceneReader </code> refers to a class
 * file being read into a scene.  Also see {@link ClassAnnotationSceneWriter}.
 *
 * <p>
 *
 * The proper usage of this class is to construct a
 * <code>ClassAnnotationSceneReader}</code> with an {@link AScene} into which
 * annotations should be inserted, then pass this as a
 * {@link org.objectweb.asm.ClassVisitor} to
 * {@link org.objectweb.asm.ClassReader#accept}
 *
 * <p>
 *
 * All other methods are intended to be called only by
 * {@link org.objectweb.asm.ClassReader#accept},
 * and should not be called anywhere else, due to the order in which
 * {@link org.objectweb.asm.ClassVisitor} methods should be called.
 */
public class ClassAnnotationSceneReader
extends EmptyVisitor {
  // general strategy:
  // -only "Runtime[In]visible[Type]Annotations" are supported
  // -use an empty visitor for everything besides annotations, fields and
  //  methods; for those three, use a special visitor that does all the work
  //  and inserts the annotations correctly into the specified AElement

  // Whether to output tracing information
  private static final boolean trace = false;

  // Whether to output error messages for unsupported cases
  private static final boolean strict = false;

  // The scene into which this class will insert annotations.
  private AScene scene;

  // The AClass that represents this class in scene.
  private AClass aClass;

  /**
   * Holds definitions we've seen so far.  Maps from annotation name to
   * the definition itself.  Maps from both the qualified name and the
   * unqualified name.  If the unqualified name is not unique, it maps
   * to null and the qualified name should be used instead. */
  private final Map<String, AnnotationDef> adefs = initAdefs();
  private static Map<String,AnnotationDef> initAdefs() {
    Map<String,AnnotationDef> result = new HashMap<String,AnnotationDef>();
    for (AnnotationDef ad : Annotations.standardDefs) {
      result.put(ad.name, ad);
    }
    return result;
  }


  /**
   * constructs a new <code> ClassAnnotationSceneReader </code> that will
   * insert all the annotations in the class that it visits into
   * <code> scene </code>
   *
   * @param scene the annotation scene into which annotations this visits
   *  will be inserted
   */
  public ClassAnnotationSceneReader(AScene scene) {
    this.scene = scene;
  }

  /**
   * @see org.objectweb.asm.commons.EmptyVisitor#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    aClass = scene.classes.vivify(name.replace('/', '.'));
  }

  /**
   * @see org.objectweb.asm.commons.EmptyVisitor#visitAnnotation(java.lang.String, boolean)
   */
  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    if (trace) { System.out.printf("visitAnnotation(%s, %s) in %s (%s)%n", desc, visible, this, this.getClass()); }
    return visitTypeAnnotation(desc, visible);
  }

  /**
   * @see org.objectweb.asm.commons.EmptyVisitor#visitTypeAnnotation(java.lang.String, boolean)
   */
  @Override
  public TypeAnnotationVisitor visitTypeAnnotation(String desc, boolean visible) {
    if (trace) { System.out.printf("visitTypeAnnotation(%s, %s); aClass=%s in %s (%s)%n", desc, visible, aClass, this, this.getClass()); }
    return new AnnotationSceneReader(desc, visible, aClass);
  }

  /**
   * @see org.objectweb.asm.commons.EmptyVisitor#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
   */
  @Override
  public FieldVisitor visitField(
      int access,
      String name,
      String desc,
      String signature,
      Object value  ) {
    if (trace) { System.out.printf("visitField(%s, %s, %s, %s, %s) in %s (%s)%n", access, name, desc, signature, value, this, this.getClass()); }
    AElement aField = aClass.fields.vivify(name);
    return new FieldAnnotationSceneReader(name, desc, signature, value, aField);
  }

  /**
   * @see org.objectweb.asm.commons.EmptyVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public MethodVisitor visitMethod(
      int access,
      String name,
      String desc,
      String signature,
      String[] exceptions) {
    if (trace) { System.out.printf("visitMethod(%s, %s, %s, %s, %s) in %s (%s)%n", access, name, desc, signature, exceptions, this, this.getClass()); }
    AMethod aMethod = aClass.methods.vivify(name+desc);
    return new MethodAnnotationSceneReader(name, desc, signature, aMethod);
  }

  // converts JVML format to Java format
  private static String classDescToName(String desc) {
    return desc.substring(1, desc.length() - 1).replace('/', '.');
  }


  ///////////////////////////////////////////////////////////////////////////
  /// Inner classes
  ///

  // Hackish workaround for odd subclassing.
  @SuppressWarnings("signature")
  String dummyDesc = "dummy";

  /*
   * Most of the complexity behind reading annotations from a class file into
   * a scene is in AnnotationSceneReader, which fully implements the
   * TypeAnnotationVisitor interface (and therefore also implements the
   * AnnotationVisitor interface).  It keeps an AElement of the
   * element into which this should insert the annotations it visits in
   * a class file.  Thus, constructing an AnnotationSceneReader with an
   * AElement of the right type is sufficient for writing out annotations
   * to that element, which will be done once visitEnd() is called.  Note that
   * for when inner annotations are expected, the aElement passed in must be
   * of the correct form (ATypeElement, or AMethod depending on the
   * target type of the extended annotation).
   */
  private class AnnotationSceneReader implements TypeAnnotationVisitor {
    // Implementation strategy:
    // For field values and enums, simply pass the information
    //  onto annotationBuilder.
    // For arrays, use an ArrayAnnotationBuilder that will
    //  properly call the right annotationBuilder methods on its visitEnd().
    // For nested annotations, use a NestedAnnotationSceneReader that will
    //  properly call the right annotationBuilder methods on its visitEnd().
    // For extended information, store all arguments passed in and on
    //  this.visitEnd(), handle all the information based on target type.


    // The AElement into which the annotation visited should be inserted.
    protected AElement aElement;

    // Whether or not this annotation is visible at runtime.
    protected boolean visible;

    // The AnnotationBuilder used to create this annotation.
    private AnnotationBuilder annotationBuilder;

    // since AnnotationSceneReader will work for both normal
    // and extended annotations, all of the following information
    // may or may not be present, so use a list to store
    // information as it is received from visitX* methods, and
    // correctly interpret the information in visitEnd()
    // note that all of these should contain 0 or 1 elements,
    // except for xLocations, which is actually a list
    private List<Integer> xTargetTypeArgs;
    private List<Integer> xIndexArgs;
    private List<Integer> xLengthArgs;
    private List<Integer> xLocationsArgs;
    private List<Integer> xLocationLengthArgs;
    private List<Integer> xOffsetArgs;
    private List<Integer> xStartPcArgs;
    private List<Integer> xParamIndexArgs;
    private List<Integer> xBoundIndexArgs;
    private List<Integer> xTypeIndexArgs;

    // private AnnotationDef getAnnotationDef(Object o) {
    //   if (o instanceof AnnotationDef) {
    //     return (AnnotationDef) o;
    //   } else if (o instanceof String) {
    //     return getAnnotationDef((String) o);
    //   } else {
    //     throw new Error(String.format("bad type %s : %s", o.getClass(), o));
    //   }
    // }

    @SuppressWarnings("unchecked")
    private AnnotationDef getAnnotationDef(String jvmlClassName) {
      String annoTypeName = classDescToName(jvmlClassName);
      // It would be better to not require the .class file to be on the
      // classpath, but to search for it on a path that is passed to this
      // program.  Worry about that later.
      Class<? extends java.lang.annotation.Annotation> annoClass;
      try {
        annoClass = (Class<? extends java.lang.annotation.Annotation>) Class.forName(annoTypeName);
      } catch (ClassNotFoundException e) {
        System.out.printf("Could not find class: %s%n", e.getMessage());
        printClasspath();
        throw new Error(e);
      }

      AnnotationDef ad = AnnotationDef.fromClass(annoClass, adefs);

      return ad;
    }


    /*
     * Constructs a new AnnotationScene reader with the given description and
     * visibility.  Calling visitEnd() will ensure that this writes out the
     * annotation it visits into aElement.
     * @param desc JVML format for the field being read, or ClassAnnotationSceneReader.dummyDesc
     */
    public AnnotationSceneReader(String desc, boolean visible, AElement aElement) {
      if (trace) { System.out.printf("AnnotationSceneReader(%s, %s, %s)%n", desc, visible, aElement); }
      this.visible = visible;
      this.aElement = aElement;
      if (desc != dummyDesc) {    // interned
        AnnotationDef ad = getAnnotationDef(desc);

        AnnotationBuilder ab = AnnotationFactory.saf.beginAnnotation(ad);
        if (ab == null)
          throw new IllegalArgumentException("bad description: " + desc);
        else
          this.annotationBuilder = ab;
      }

      // For legal annotations, and except for xLocationsArgs, these should
      //  contain at most one element.
      this.xTargetTypeArgs = new ArrayList<Integer>(1);
      this.xIndexArgs = new ArrayList<Integer>(1);
      this.xLengthArgs = new ArrayList<Integer>(1);
      this.xLocationLengthArgs = new ArrayList<Integer>(1);
      this.xOffsetArgs = new ArrayList<Integer>(1);
      this.xStartPcArgs = new ArrayList<Integer>(1);
      this.xLocationsArgs = new ArrayList<Integer>();
      this.xParamIndexArgs = new ArrayList<Integer>(1);
      this.xBoundIndexArgs = new ArrayList<Integer>(1);
      this.xTypeIndexArgs = new ArrayList<Integer>(1);
    }

    /*
     * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String, java.lang.Object)
     */
    public void visit(String name, Object value) {
      if (trace) { System.out.printf("visit(%s, %s) on %s%n", name, value, this); }
      // BasicAFT.forType(Class) expects int.class instead of Integer.class,
      // and so on for all primitives.  String.class is ok, since it has no
      // primitive type.
      Class<?> c = value.getClass();
      if (c.equals(Boolean.class)) {
        c = boolean.class;
      } else if (c.equals(Byte.class)) {
        c = byte.class;
      } else if (c.equals(Character.class)) {
        c = char.class;
      } else if (c.equals(Short.class)) {
        c = short.class;
      } else if (c.equals(Integer.class)) {
        c = int.class;
      } else if (c.equals(Long.class)) {
        c = long.class;
      } else if (c.equals(Float.class)) {
        c = float.class;
      } else if (c.equals(Double.class)) {
        c = double.class;
      } else if (c.equals(Type.class)) {
        try {
          annotationBuilder.addScalarField(name, ClassTokenAFT.ctaft, Class.forName(((Type)value).getClassName()));
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Could not load Class for Type: " + value, e);
        }
        // Return here, otherwise the annotationBuilder would be called
        // twice for the same value.
        return;
      } else if (!c.equals(String.class)) {
        // Only possible type for value is String, in which case c is already
        // String.class, or array of primitive
        c = c.getComponentType();
        ArrayBuilder arrayBuilder = annotationBuilder.beginArrayField(
            name, new ArrayAFT(BasicAFT.forType(c)));
        // value is of type c[], now add in all the elements of the array
        for (Object o : asList(value)) {
          arrayBuilder.appendElement(o);
        }
        arrayBuilder.finish();
        return;
      }

      // handle everything but arrays
      annotationBuilder.addScalarField(name, BasicAFT.forType(c),value);

    }

    /*
     * Method that accepts an Object whose actual type is c[], where c is a
     * primitive, and returns an equivalent List<Object> that contains
     * the same elements as in hiddenArray.
     */
    private List<Object> asList(Object hiddenArray) {
      List<Object> objects = new ArrayList<Object>();
      Class<?> c = hiddenArray.getClass().getComponentType();
      if (c.equals(boolean.class)) {
        for (boolean o : (boolean[]) hiddenArray) {
          objects.add(o);
        }
      } else if (c.equals(byte.class)) {
        for (byte o : (byte[]) hiddenArray) {
          objects.add(o);
        }
      } else if (c.equals(char.class)) {
        for (char o : (char[]) hiddenArray) {
          objects.add(o);
        }
      } else if (c.equals(short.class)) {
        for (short o : (short[]) hiddenArray) {
          objects.add(o);
        }
      } else if (c.equals(int.class)) {
        for (int o : (int[]) hiddenArray) {
          objects.add(o);
        }
      } else if (c.equals(long.class)) {
        for (long o : (long[]) hiddenArray) {
          objects.add(o);
        }
      } else if (c.equals(float.class)) {
        for (float o : (float[]) hiddenArray) {
          objects.add(o);
        }
      } else if (c.equals(double.class)) {
        for (double o : (double[]) hiddenArray) {
          objects.add(o);
        }
      } else {
        throw new RuntimeException("Array has unknown type: " + hiddenArray);
      }
      return objects;
    }

    /*
     * @see org.objectweb.asm.AnnotationVisitor#visitEnum(java.lang.String, java.lang.String, java.lang.String)
     */
    public void visitEnum(String name, String desc, String value) {
      if (trace) { System.out.printf("visitEnum(%s, %s) in %s (%s)%n", name, desc, this, this.getClass()); }
      annotationBuilder.addScalarField(name, new EnumAFT(desc), value);
    }

    /*
     * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
     */
    public AnnotationVisitor visitAnnotation(String name, String desc) {
      if (trace) { System.out.printf("visitAnnotation(%s, %s) in %s (%s)%n", name, desc, this, this.getClass()); }
      return new NestedAnnotationSceneReader(this, name, desc);
    }

    /*
     * @see org.objectweb.asm.AnnotationVisitor#visitArray(java.lang.String)
     */
    public AnnotationVisitor visitArray(String name) {
      if (trace) { System.out.printf("visitArray(%s) in %s (%s)%n", name, this, this.getClass()); }
      ArrayAFT aaft = (ArrayAFT) annotationBuilder.fieldTypes().get(name);
      ScalarAFT aft = aaft.elementType;
      return new ArrayAnnotationSceneReader(this, name, aft);
    }

    /*
     * @see org.objectweb.asm.TypeAnnotationVisitor#visitXTargetType(int)
     */
    public void visitXTargetType(int target_type) {
      xTargetTypeArgs.add(target_type);
    }

    /*
     * @see org.objectweb.asm.TypeAnnotationVisitor#visitXIndex(int)
     */
    public void visitXIndex(int index) {
      xIndexArgs.add(index);
    }

    /*
     * @see org.objectweb.asm.TypeAnnotationVisitor#visitXLength(int)
     */
    public void visitXLength(int length) {
      xLengthArgs.add(length);
    }

    /*
     * @see org.objectweb.asm.TypeAnnotationVisitor#visitXLocation(int)
     */
    public void visitXLocation(int location) {
      xLocationsArgs.add(location);
    }

    /*
     * @see org.objectweb.asm.TypeAnnotationVisitor#visitXLocationLength(int)
     */
    public void visitXLocationLength(int location_length) {
      xLocationLengthArgs.add(location_length);
    }

    /*
     * @see org.objectweb.asm.TypeAnnotationVisitor#visitXOffset(int)
     */
    public void visitXOffset(int offset) {
      xOffsetArgs.add(offset);
    }

    public void visitXNumEntries(int num_entries) {
    }

    /*
     * @see org.objectweb.asm.TypeAnnotationVisitor#visitXStartPc(int)
     */
    public void visitXStartPc(int start_pc) {
      xStartPcArgs.add(start_pc);
    }

    /*
     * @see org.objectweb.asm.TypeAnnotationVisitor#visitXBoundIndex(int)
     */
    public void visitXParamIndex(int param_index) {
      xParamIndexArgs.add(param_index);
    }

    /*
     * @see org.objectweb.asm.TypeAnnotationVisitor#visitXBoundIndex(int)
     */
    public void visitXBoundIndex(int bound_index) {
      xBoundIndexArgs.add(bound_index);
    }

    public void visitXTypeIndex(int type_index) {
      xTypeIndexArgs.add(type_index);
    }

    /*
     * Visits the end of the annotation, and actually writes out the
     *  annotation into aElement.
     *
     * @see org.objectweb.asm.TypeAnnotationVisitor#visitEnd()
     */
    public void visitEnd() {
      if (trace) { System.out.printf("visitEnd on %s (%s)%n", this, this.getClass()); }
      if (xTargetTypeArgs.size() >= 1) {
        TargetType target = TargetType.fromTargetTypeValue(xTargetTypeArgs.get(0));
        // TEMP
        // If the expression used to initialize a field contains annotations
        // on instanceofs, typecasts, or news, the extended compiler enters
        // those annotations on the field.  If we get such an annotation and
        // aElement is a field, skip the annotation for now to avoid crashing.
        switch(target) {
        case FIELD:
            // TODO: resolve issue once classfile format is finalized
            if (aElement instanceof AClass) {
              //handleFieldOnClass((AClass) aElement);
              if (strict) { System.err.println("Unhandled FIELD annotation for " + aElement); }
            } else if (aElement instanceof ATypeElement) {
              handleField((ATypeElement) aElement);
            } else {
              throw new RuntimeException("Unknown FIELD aElement: " + aElement);
            }
            break;
        case FIELD_COMPONENT:
          // TODO: resolve issue once classfile format is finalized
          if (aElement instanceof AClass) {
            //handleFieldGenericArrayOnClass((AClass) aElement);
            if (strict) { System.err.println("Unhandled FIELD_COMPONENT annotation for " + aElement); }
          } else if (aElement instanceof ATypeElement) {
            handleFieldGenericArray((ATypeElement) aElement);
          } else {
            throw new RuntimeException("Unknown FIELD_COMPONENT: " + aElement);
          }
          break;
        case LOCAL_VARIABLE:
          handleMethodLocalVariable((AMethod) aElement);
          break;
        case LOCAL_VARIABLE_COMPONENT:
          handleMethodLocalVariableGenericArray((AMethod) aElement);
          break;
        case NEW:
          if (aElement instanceof AMethod) {
            handleMethodObjectCreation((AMethod) aElement);
          } else {
            // TODO: in field initializers
            if (strict) { System.err.println("Unhandled NEW annotation for " + aElement); }
          }
          break;
        case NEW_COMPONENT:
          if (aElement instanceof AMethod) {
            handleMethodObjectCreationGenericArray((AMethod) aElement);
          } else {
              // TODO: in field initializers
              if (strict) { System.err.println("Unhandled NEW_COMPONENT annotation for " + aElement); }
          }
          break;
        case METHOD_PARAMETER:
          handleMethodParameterType((AMethod) aElement);
          break;
        case METHOD_PARAMETER_COMPONENT:
          handleMethodParameterTypeGenericArray((AMethod) aElement);
          break;
        case METHOD_RECEIVER:
            handleMethodReceiver((AMethod) aElement);
            break;
        case METHOD_RECEIVER_COMPONENT:
            handleMethodReceiverGenericArray((AMethod) aElement);
            break;
        case TYPECAST:
          if (aElement instanceof AMethod) {
            handleMethodTypecast((AMethod) aElement);
          } else {
              // TODO: in field initializers
              if (strict) { System.err.println("Unhandled TYPECAST annotation for " + aElement); }
          }
          break;
        case TYPECAST_COMPONENT:
          if (aElement instanceof AMethod) {
            handleMethodTypecastGenericArray((AMethod) aElement);
          } else {
              // TODO: in field initializers
              if (strict) { System.err.println("Unhandled TYPECAST_COMPONENT annotation for " + aElement); }
          }
          break;
        case METHOD_RETURN:
          handleMethodReturnType((AMethod) aElement);
          break;
        case METHOD_RETURN_COMPONENT:
          handleMethodReturnTypeGenericArray((AMethod) aElement);
          break;
        case INSTANCEOF:
          if (aElement instanceof AMethod) {
            handleMethodInstanceOf((AMethod) aElement);
          } else {
              // TODO: in field initializers
              if (strict) { System.err.println("Unhandled INSTANCEOF annotation for " + aElement); }
          }
          break;
        case INSTANCEOF_COMPONENT:
          if (aElement instanceof AMethod) {
            handleMethodInstanceOfGenericArray((AMethod) aElement);
          } else {
              // TODO: in field initializers
              if (strict) { System.err.println("Unhandled INSTANCEOF_COMPONENT annotation for " + aElement); }
          }
          break;
        case CLASS_TYPE_PARAMETER_BOUND:
          handleClassTypeParameterBound((AClass) aElement);
          break;
        case CLASS_TYPE_PARAMETER_BOUND_COMPONENT:
          handleClassTypeParameterBoundGenericArray((AClass) aElement);
          break;
        case METHOD_TYPE_PARAMETER_BOUND:
          handleMethodTypeParameterBound((AMethod) aElement);
          break;
        case METHOD_TYPE_PARAMETER_BOUND_COMPONENT:
          handleMethodTypeParameterBoundGenericArray((AMethod) aElement);
        case CLASS_EXTENDS:
          handleClassExtends((AClass) aElement);
          break;
        case CLASS_EXTENDS_COMPONENT:
          handleClassExtendsGenericArray((AClass) aElement);
          break;
        case THROWS:
          handleThrows((AMethod) aElement);
          break;

        case NEW_TYPE_ARGUMENT:
          handleNewTypeArgument((AMethod) aElement);
          break;
        case NEW_TYPE_ARGUMENT_COMPONENT:
          handleNewTypeArgumentGenericArray((AMethod) aElement);
          break;

        case METHOD_TYPE_ARGUMENT:
          throw new Error("METHOD_TYPE_ARGUMENT: to do");
          // break;

        case METHOD_TYPE_ARGUMENT_COMPONENT:
          throw new Error("METHOD_TYPE_ARGUMENT_COMPONENT: to do");
          // break;

        case METHOD_TYPE_PARAMETER:
          throw new Error("METHOD_TYPE_PARAMETER: to do");

        case CLASS_TYPE_PARAMETER:
          handleClassTypeParameter((AClass) aElement);
          break;

          // TODO: ensure all cases covered.
        default:
          // Rather than throw an error here, since a declaration annotation
          // is being used as an extended annotation, just make the
          // annotation and place it in the given aElement as usual.

          // aElement.tlAnnotationsHere.add(makeAnnotation());
          Annotation a = makeAnnotation();
          aElement.tlAnnotationsHere.add(a);
        }
      } else {
        // This is not an extended annotation visitor, so just
        // make the annotation and place it in the given AElement,
        // possibly moving it to a type annotation location instead.

        Annotation a = makeAnnotation();

        if (a.def.isTypeAnnotation() && (aElement instanceof AMethod)) {
          AMethod m = (AMethod) aElement;
          m.returnType.tlAnnotationsHere.add(a);

          // There is not currently a separate location for field/parameter
          // type annotations; they are mixed in with the declaration
          // annotations.  This should be fixed in the future.
          // Also, fields/parameters are just represented as AElement.
          // } else if (a.def.isTypeAnnotation() && (aElement instanceof AField)) {

        } else {
          aElement.tlAnnotationsHere.add(a);
        }
      }
    }

    // The following are utility methods to facilitate creating all the
    // necessary data structures in the scene library.

    /*
     * Returns an annotation, ready to be placed into the scene, from
     *  the information visited.
     */
    public Annotation makeAnnotation() {
      return annotationBuilder.finish();
    }

    /*
     * Returns a LocalLocation for this annotation.
     */
    private LocalLocation makeLocalLocation() {
      int index = xIndexArgs.get(0);
      int length = xLengthArgs.get(0);
      int start = xStartPcArgs.get(0);
      return new LocalLocation(index, start, length);
    }

    /*
     * Returns an InnerTypeLocation for this annotation.
     */
    private InnerTypeLocation makeInnerTypeLocation() {
      return new InnerTypeLocation(xLocationsArgs);
    }

    /*
     * Returns the offset for this annotation.
     */
    private RelativeLocation makeOffset() {
      return RelativeLocation.createOffset(xOffsetArgs.get(0));
    }

    /*
     * Returns the index for this annotation.
     */
    private int makeIndex() {
      return xIndexArgs.get(0);
    }

    /*
     * Returns the bound location for this annotation.
     */
    private BoundLocation makeTypeParameterLocation() {
      if (!xParamIndexArgs.isEmpty()) {
        return new BoundLocation(xParamIndexArgs.get(0), -1);
      } else {
        if (strict) { System.err.println("makeTypeParameterLocation with empty xParamIndexArgs!"); }
        return new BoundLocation(Integer.MAX_VALUE, -1);
      }
    }

    /*
     * Returns the bound location for this annotation.
     * @see #makeTypeParameterLocation()
     */
    private BoundLocation makeBoundLocation() {
      // TODO: Give up on unbounded wildcards for now!
      if (!xParamIndexArgs.isEmpty()) {
        return new BoundLocation(xParamIndexArgs.get(0), xBoundIndexArgs.get(0));
      } else {
        if (strict) { System.err.println("makeBoundLocation with empty xParamIndexArgs!"); }
        return new BoundLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
      }
    }

    private TypeIndexLocation makeTypeIndexLocation() {
      return new TypeIndexLocation(xTypeIndexArgs.get(0));
    }

    /*
     * Creates the inner annotation on aElement.innerTypes.
     */
    private void handleField(ATypeElement aElement) {
      aElement.tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the inner annotation on aElement.innerTypes.
     */
    private void handleFieldGenericArray(ATypeElement aElement) {
      InnerTypeLocation innerTypeLocation = makeInnerTypeLocation();
      AElement aInnerType = aElement.innerTypes.vivify(innerTypeLocation);
      aInnerType.tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the method receiver annotation on aMethod.
     */
    private void handleMethodReceiver(AMethod aMethod) {
      aMethod.receiver.tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the inner method receiver annotation on aMethod.innerTypes.
     */
    private void handleMethodReceiverGenericArray(AMethod aMethod) {
      InnerTypeLocation innerTypeLocation = makeInnerTypeLocation();
      AElement aInnerType = aMethod.receiver.innerTypes.vivify(innerTypeLocation);
      aInnerType.tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the local variable annotation on aMethod.
     */
    private void handleMethodLocalVariable(AMethod aMethod) {
      aMethod.locals.vivify(makeLocalLocation()).tlAnnotationsHere.add(
          makeAnnotation());
    }

    /*
     * Creates the local variable generic/array annotation on aMethod.
     */
    private void handleMethodLocalVariableGenericArray(AMethod aMethod) {
      aMethod.locals.vivify(makeLocalLocation()).type.innerTypes.
        vivify(makeInnerTypeLocation()).tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the object creation annotation on aMethod.
     */
    private void handleMethodObjectCreation(AMethod aMethod) {
      aMethod.news.vivify(makeOffset()).tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the object creation generic/array annotaiton on aMethod.
     */
    private void handleMethodObjectCreationGenericArray(AMethod aMethod) {
      aMethod.news.vivify(makeOffset()).innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeAnnotation());
    }

    private int makeParamIndex() {
      return xParamIndexArgs.get(0);
    }

    /*
     * Creates the method parameter type generic/array annotation on aMethod.
     */
    private void handleMethodParameterType(AMethod aMethod) {
      aMethod.parameters.vivify(makeParamIndex()).type.tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the method parameter type generic/array annotation on aMethod.
     */
    private void handleMethodParameterTypeGenericArray(AMethod aMethod) {
      aMethod.parameters.vivify(makeParamIndex()).type.innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the typecast annotation on aMethod.
     */
    private void handleMethodTypecast(AMethod aMethod) {
     aMethod.typecasts.vivify(makeOffset()).tlAnnotationsHere.add(
         makeAnnotation());
    }

    /*
     * Creates the typecast generic/array annotation on aMethod.
     */
    private void handleMethodTypecastGenericArray(AMethod aMethod) {
      aMethod.typecasts.vivify(makeOffset()).innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the method return type generic/array annotation on aMethod.
     */
    private void handleMethodReturnType(AMethod aMethod) {
      if (trace) { System.out.printf("handleMethodReturnType(%s)%n", aMethod); }
      aMethod.returnType.tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the method return type generic/array annotation on aMethod.
     */
    private void handleMethodReturnTypeGenericArray(AMethod aMethod) {
      if (trace) { System.out.printf("handleMethodReturnTypeGenericArray(%s)%n", aMethod); }
      aMethod.returnType.innerTypes.vivify(makeInnerTypeLocation()).tlAnnotationsHere.add(
          makeAnnotation());
    }

    /*
     * Creates the method instance of annotation on aMethod.
     */
    private void handleMethodInstanceOf(AMethod aMethod) {
      aMethod.instanceofs.vivify(makeOffset()).tlAnnotationsHere.add(
          makeAnnotation());
    }

    /*
     * Creates the method instance of generic/array annotation on aMethod.
     */
    private void handleMethodInstanceOfGenericArray(AMethod aMethod) {
      aMethod.typecasts.vivify(makeOffset()).innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the class type parameter bound annotation on aClass.
     */
    private void handleClassTypeParameter(AClass aClass) {
      aClass.bounds.vivify(makeTypeParameterLocation())
          .tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the class type parameter bound annotation on aClass.
     */
    private void handleClassTypeParameterBound(AClass aClass) {
      aClass.bounds.vivify(makeBoundLocation())
          .tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the class type parameter bound annotation on aClass.
     */
    private void handleClassTypeParameterBoundGenericArray(AClass aClass) {
      aClass.bounds.vivify(makeBoundLocation()).innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the class type parameter bound annotation on aClass.
     */
    private void handleMethodTypeParameterBound(AMethod aMethod) {
      aMethod.bounds.vivify(makeBoundLocation())
          .tlAnnotationsHere.add(makeAnnotation());
    }

    /*
     * Creates the class type parameter bound annotation on aClass.
     */
    private void handleMethodTypeParameterBoundGenericArray(AMethod aMethod) {
      aMethod.bounds.vivify(makeBoundLocation()).innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeAnnotation());
    }

    private void handleClassExtends(AClass aClass) {
      aClass.extendsImplements.vivify(makeTypeIndexLocation())
        .tlAnnotationsHere.add(makeAnnotation());
    }

    private void handleClassExtendsGenericArray(AClass aClass) {
      aClass.extendsImplements.vivify(makeTypeIndexLocation())
        .innerTypes.vivify(makeInnerTypeLocation()).tlAnnotationsHere.add(
            makeAnnotation());
    }

    private void handleThrows(AMethod aMethod) {
      aMethod.throwsException.vivify(makeTypeIndexLocation())
        .tlAnnotationsHere.add(makeAnnotation());
    }

    private void handleNewTypeArgument(AMethod aMethod) {
      //aMethod.news.vivify(makeOffset()).innerTypes.vivify();
          //makeInnerTypeLocation()).tlAnnotationsHere.add(makeAnnotation());
      if (strict) { System.err.println("Unhandled handleNewTypeArgument on aMethod: " + aMethod); }
    }

    private void handleNewTypeArgumentGenericArray(AMethod aMethod) {
      if (strict) { System.err.println("Unhandled handleNewTypeArgumentGenericArray on aMethod: " + aMethod); }
    }

    /*
     * Hook for NestedAnnotationSceneReader; overridden by
     * ArrayAnnotationSceneReader to add an array element instead of a field
     */
    void supplySubannotation(String fieldName, Annotation annotation) {
      annotationBuilder.addScalarField(fieldName,
              new AnnotationAFT(annotation.def()), annotation);
    }

    @Override
    public String toString() {
      return String.format("(AnnotationSceneReader: %s %s %s)",
                           aElement, visible, annotationBuilder);
    }

  }

  /*
   * A NestedAnnotationSceneReader is an AnnotationSceneReader
   * that will read in an entire annotation on a field (of type annotation)
   * of its parent, and once it has fully visited that annotation, it will
   * call its parent annotation builder to include that field, so after
   * its parent constructs and returns this as an AnnotationVisitor
   * (visitAnnotation()), it no longer needs to worry about that field.
   */
  private class NestedAnnotationSceneReader extends AnnotationSceneReader {
    private AnnotationSceneReader parent;
    private String name;
    private String desc;

    public NestedAnnotationSceneReader(AnnotationSceneReader parent,
        String name, String desc) {
      super(desc, parent.visible, parent.aElement);
      if (trace) { System.out.printf("NestedAnnotationSceneReader(%s, %s, %s)%n", parent, name, desc); }
      this.parent = parent;
      this.name = name;
      this.desc = desc;
    }

    @Override
    public void visitEnd() {
      // Do not call super, as that already builds the annotation, causing an exception.
      // super.visitEnd();
      if (trace) { System.out.printf("visitEnd on %s (%s)%n", this, this.getClass()); }
      Annotation a = super.makeAnnotation();
      parent.supplySubannotation(name, a);
    }
  }

  /*
   * An ArrayAnnotationSceneReader is an AnnotationSceneReader
   * that reads all elements of an array field
   * of its parent, and once it has fully visited the array, it will
   * call its parent annotation builder to include that array, so after
   * its parent constructs and returns this as an AnnotationVisitor
   * (visitArray()), it no longer needs to worry about that array.
   *
   * Note that by specification of AnnotationVisitor.visitArray(), the only
   * methods that should be called on this are visit(String name, Object value)
   * and visitEnd().
   */
  // An AnnotationSceneReader reads an annotation.  An
  // ArrayAnnotationSceneReader reads an arbitrary array field, but not an
  // entire annotation.  So why is ArrayAnnotationSceneReader a subclass of
  // AnnotationSceneReader?  Pass ClassAnnotationSceneReader.dummyDesc
  // in the superclass constructor to
  // disable superclass behaviors that would otherwise cause trouble.
  private class ArrayAnnotationSceneReader extends AnnotationSceneReader {
    private AnnotationSceneReader parent;
    private ArrayBuilder arrayBuilder;
    private ScalarAFT elementType;
    private String arrayName;

    // The element type may be unknown when this is called.
    // But AnnotationSceneReader expects to know the element type.
    public ArrayAnnotationSceneReader(
        AnnotationSceneReader parent,
        String fieldName,
        AnnotationFieldType eltType) {
      super(dummyDesc, parent.visible, parent.aElement);
      if (trace) { System.out.printf("ArrayAnnotationSceneReader(%s, %s)%n", parent, fieldName); }
      this.parent = parent;
      this.arrayName = fieldName;
      this.arrayBuilder = null;
    }

    private void prepareForElement(ScalarAFT elementType) {
      if (trace) { System.out.printf("prepareForElement(%s) in %s (%s)%n", elementType, this, this.getClass()); }
      assert elementType != null; // but, does this happen when reading from classfile?
      if (arrayBuilder == null) {
        this.elementType = elementType;
        arrayBuilder = parent.annotationBuilder.beginArrayField(arrayName,
                new ArrayAFT(elementType));
      }
    }

    // There are only so many different array types that are permitted in
    // an annotation.  (I'm not sure how relevant that is here.)
    @Override
    public void visit(String name, Object value) {
      if (trace) { System.out.printf("visit(%s, %s) (%s) in %s (%s)%n", name, value, value.getClass(), this, this.getClass()); }
      ScalarAFT aft;
      if (value.getClass().equals(org.objectweb.asm.Type.class)) {
        // What if it's an annotation?
        aft = ClassTokenAFT.ctaft;
        try {
          value = Class.forName(((org.objectweb.asm.Type) value).getClassName());
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Could not load Class for Type: " + value, e);
        }
      } else {
        Class<?> vc = value.getClass();
        aft = BasicAFT.forType(vc);
        //or: aft = (ScalarAFT) AnnotationFieldType.fromClass(vc, null);
      }
      assert aft != null;
      prepareForElement(aft);
      assert arrayBuilder != null;
      arrayBuilder.appendElement(value);
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
      if (trace) { System.out.printf("visitEnum(%s, %s, %s) in %s (%s)%n", name, desc, value, this, this.getClass()); }
      prepareForElement(new EnumAFT(classDescToName(desc)));
      assert arrayBuilder != null;
      arrayBuilder.appendElement(value);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
      throw new AssertionError("Multidimensional array in annotation!");
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
      if (trace) { System.out.printf("visitAnnotation(%s, %s) in %s (%s)%n", name, desc, this, this.getClass()); }
      // The NASR will regurgitate the name we pass here when it calls
      // supplySubannotation.  Since we ignore the name there, it doesn't
      // matter what name we pass here.
      return new NestedAnnotationSceneReader(this, name, desc);
    }

    @Override
    public void visitEnd() {
      if (trace) { System.out.printf("visitEnd on %s (%s)%n", this, this.getClass()); }
      if (arrayBuilder != null) {
        arrayBuilder.finish();
      } else {
        // This was a zero-element array
        parent.annotationBuilder.addEmptyArrayField(arrayName);
      }
    }

    @Override
    void supplySubannotation(String fieldName, Annotation annotation) {
      prepareForElement(new AnnotationAFT(annotation.def()));
      assert arrayBuilder != null;
      arrayBuilder.appendElement(annotation);
    }
  }

  /*
   * A FieldAnnotationSceneReader is a FieldVisitor that only cares about
   * visiting [extended]annotations.  Attributes are ignored and visitEnd() has
   * no effect.  An AnnotationSceneReader is returned for declaration and type
   * AnnotationVisitors.  The AnnotationSceneReaders have a reference to
   * an ATypeElement that this is visiting, and they will write out
   * all the information to that ATypeElement after visiting each annotation.
   */
  private class FieldAnnotationSceneReader extends EmptyVisitor implements FieldVisitor {

    private String name;
    private String desc;
    private String signature;
    private Object value;
    private AElement aField;

    public FieldAnnotationSceneReader(
        String name,
        String desc,
        String signature,
        Object value,
        AElement aField) {
      this.name = name;
      this.desc = desc;
      this.signature = signature;
      this.value = value;
      this.aField = aField;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      if (trace) { System.out.printf("visitAnnotation(%s, %s) in %s (%s)%n", desc, visible, this, this.getClass()); }
      return new AnnotationSceneReader(desc, visible, aField);
    }

    @Override
    public TypeAnnotationVisitor visitTypeAnnotation(String desc, boolean visible) {
      if (trace) { System.out.printf("visitTypeAnnotation(%s, %s); aField=%s, aField.type=%s in %s (%s)%n", desc, visible, aField, aField.type, this, this.getClass()); }
      return new AnnotationSceneReader(desc, visible, aField.type);
    }
  }

  /*
   * Similarly to FieldAnnotationSceneReader, this is a MethodVisitor that
   * only cares about visiting [extended]annotations.  Attributes are ignored,
   * all code is ignored and visitEnd() has no effect.  An AnnotationSceneReader
   * is returned for declaration and type AnnotationVisitors.  The
   * AnnotationSceneReaders have a reference to an AMethod that this is
   * visiting, and they will write out all the information to that
   * AMethod after visiting each annotation.
   */
  private class MethodAnnotationSceneReader extends EmptyVisitor implements MethodVisitor {

    private String name;
    private String desc;
    private String signature;
    private AElement aMethod;

    public MethodAnnotationSceneReader(String name, String desc, String signature, AElement aMethod) {
      this.name = name;
      this.desc = desc;
      this.signature = signature;
      this.aMethod = aMethod;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      if (trace) { System.out.printf("visitAnnotation(%s, %s) in %s (%s)%n", desc, visible, this, this.getClass()); }
      return visitTypeAnnotation(desc, visible);
    }

    @Override
    public TypeAnnotationVisitor visitTypeAnnotation(String desc, boolean visible) {
      if (trace) { System.out.printf("visitTypeAnnotation(%s, %s) method=%s in %s (%s)%n", desc, visible, aMethod, this, this.getClass()); }
      return new AnnotationSceneReader(desc, visible, aMethod);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
      if (trace) { System.out.printf("visitParameterAnnotation(%s, %s, %s) in %s (%s)%n", parameter, desc, visible, this, this.getClass()); }
      return new AnnotationSceneReader(desc, visible,
              ((AMethod) aMethod).parameters.vivify(parameter));
    }

  }

  public static void printClasspath() {
    System.out.println("\nClasspath:");
    StringTokenizer tokenizer =
      new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator);
    while (tokenizer.hasMoreTokens()) {
      System.out.println("  " + tokenizer.nextToken());
    }
  }

}
