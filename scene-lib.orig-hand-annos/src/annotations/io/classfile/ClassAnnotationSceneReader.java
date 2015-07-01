//This class is a complete ClassVisitor with many hidden classes that do
//the work of reading annotations from a class file and inserting them into
//an AScene.
package annotations.io.classfile;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ExtendedAnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import annotations.Annotation;
import annotations.AnnotationBuilder;
import annotations.AnnotationDef;
import annotations.AnnotationFactory;
import annotations.ArrayBuilder;
import annotations.RetentionPolicy;
import annotations.SimpleAnnotation;
import annotations.TLAnnotation;
import annotations.TLAnnotationDef;
import annotations.TargetType;
import annotations.el.AClass;
import annotations.el.AElement;
import annotations.el.AMethod;
import annotations.el.AScene;
import annotations.el.ATypeElement;
import annotations.el.BoundLocation;
import annotations.el.InnerTypeLocation;
import annotations.el.LocalLocation;
import annotations.field.AnnotationAFT;
import annotations.field.ArrayAFT;
import annotations.field.BasicAFT;
import annotations.field.ClassTokenAFT;
import annotations.field.EnumAFT;
import annotations.field.ScalarAFT;

/**
 * A <code> ClassAnnotationSceneReader </code> is a 
 * {@link org.objectweb.asm.ClassVisitor} that will insert all annotations it
 * encounters while visiting a class into a given {@link AScene}.    
 * <code>A</code> is the type of 
 * {@link annotations.Annotation}s to be inserted into the
 * {@link AScene}, which presently must be a simple name-value mapping 
 * {@link SimpleAnnotation}.  
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
 *  {@link org.objectweb.asm.ClassVisitor} methods should be called.
 * 
 * @param <A> the type of annotations the scene consists of, which is how 
 * the annotations read from the class file should be treated as.
 */
public class ClassAnnotationSceneReader<A extends Annotation> 
extends EmptyVisitor {
  // general strategy: 
  // -only "Runtime[In]visible[Extended]Annotations" are supported
  // -use an empty visitor for everything besides annotations, fields and 
  //  methods for those three, use a special visitor that does all the work
  //  and inserts the annotations correctly into the specified AElement<A>

  // The scene into which this class will insert annotations.
  private AScene<A> scene;

  // The AClass that represents this class in scene.
  private AClass<A> aClass;

  // The AnnotationFactory used to create annotations in scene.
  private AnnotationFactory<A> annotationFactory;

  /**
   * Constructs a new <code> ClassAnnotationSceneReader </code> that will
   * insert all the annotations in the class that it visits into
   * <code> scene </code> 
   * 
   * @param scene the annotation scene into which annotations this visits
   *  will be inserted
   * @param annotationFactory the annotation factory to use to generate
   *  annotations
   */
  public ClassAnnotationSceneReader(
      AScene<A> scene, AnnotationFactory<A> annotationFactory) {
    this.scene = scene;
    this.annotationFactory = annotationFactory;
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
    return visitExtendedAnnotation(desc, visible);
  }

  /**
   * @see org.objectweb.asm.commons.EmptyVisitor#visitExtendedAnnotation(java.lang.String, boolean)
   */
  @Override
  public ExtendedAnnotationVisitor visitExtendedAnnotation(String desc, 
      boolean visible) {
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
    ATypeElement<A> aField = aClass.fields.vivify(name);
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
    AMethod<A> aMethod = aClass.methods.vivify(
        name+desc);
    return new MethodAnnotationSceneReader(name, desc, signature, aMethod);
  }

  // unwraps the class name from the given class descriptor
  private static String classDescToName(String desc) {
    return desc.substring(1, desc.length() - 1).replace('/', '.');
  }
    
  /*
   * Most of the complexity behind reading annotations from a class file into
   * a scene is in AnnotationSceneReader, which fully implements the
   * ExtendedAnnotationVisitor interface (and therefore also implements the
   * AnnotationVisitor interface).  It keeps an AElement<A> of the 
   * element into which this should insert the annotations it visits in 
   * a class file.  Thus, constructing an AnnotationSceneReader with an
   * AElement<A> of the write type is sufficient for writing out annotations
   * to that element, which will be done once visitEnd() is called.  Note that
   * for when inner annotations are expected, the aElement passed in must be
   * of the correct form (ATypeElement<A>, or AMethod<A> depending on the
   * target type of the extended annotation).
   */
  private class AnnotationSceneReader implements ExtendedAnnotationVisitor {
    // Implementation strategy:
    // For field values and enums, simply pass the information 
    //  onto annotationBuilder.
    // For arrays, use an ArrayAnnotationBuilder that will
    //  properly call the right annotationBuilder methods on it's visitEnd()
    // For nested annotations, use a NestedAnnotationSceneReader that will
    //  properly call the right annotationBuilder methods on it's visitEnd()
    // For extended information, store all arguments passed in and on
    //  this.visitEnd(), handle all the information based on target type


    // The AElement into which the annotation visited should be inserted.
    protected AElement<A> aElement;

    // Whether or not this annotation is visible at runtime.
    protected boolean visible;

    // The AnnotationBuilder used to create this annotation.
    private AnnotationBuilder<A> annotationBuilder;

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

    /*
     * Constructs a new AnnotationScene reader with the given description and
     * visibility.  Calling visitEnd() will ensure that this writes out the
     * annotation it visits into aElement.
     */
    public AnnotationSceneReader(
        String desc, boolean visible, AElement<A> aElement) {
      this.visible = visible;
      this.aElement = aElement;
      String annoTypeName = classDescToName(desc);
      this.annotationBuilder = annotationFactory.beginAnnotation(annoTypeName);

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
    }

    /*
     * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String, java.lang.Object)
     */
    public void visit(String name, Object value) {
      // BasicAFT.forType(Class) expects int.class instead of Integer.class,
      // and so on for all primitives.  String.class is ok, since it has no
      // primitive type.
      Class c = value.getClass();
      if(c.equals(Boolean.class)) {
        c = boolean.class;
      } else if(c.equals(Byte.class)) {
        c = byte.class;
      } else if(c.equals(Character.class)) {
        c = char.class;
      } else if(c.equals(Short.class)) {
        c = short.class;
      } else if(c.equals(Integer.class)) {
        c = int.class;
      } else if(c.equals(Long.class)) {
        c = long.class;
      } else if(c.equals(Float.class)) {
        c = float.class;
      } else if(c.equals(Double.class)) {
        c = double.class;
      } else if(c.equals(Type.class)) {               
        annotationBuilder.addScalarField(name, ClassTokenAFT.ctaft, value);
      } else if(!c.equals(String.class)) {
        // Only possible type for value is String, in which case c is already
        // String.class, or array of primitive
        c = c.getComponentType();
        ArrayBuilder arrayBuilder = annotationBuilder.beginArrayField(
            name, new ArrayAFT(BasicAFT.forType(c)));
        // value is of type c[], now add in all the elements of the array
        for(Object o : asList(value)) {
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
      Class c = hiddenArray.getClass().getComponentType();
      if(c.equals(boolean.class)) {
        for(boolean o : (boolean[]) hiddenArray) {
          objects.add(o);
        }
      } else if(c.equals(byte.class)) {
        for(byte o : (byte[]) hiddenArray) {
          objects.add(o);
        }
      } else if(c.equals(char.class)) {
        for(char o : (char[]) hiddenArray) {
          objects.add(o);
        }
      } else if(c.equals(short.class)) {
        for(short o : (short[]) hiddenArray) {
          objects.add(o);
        }
      } else if(c.equals(int.class)) {
        for(int o : (int[]) hiddenArray) {
          objects.add(o);
        }
      } else if(c.equals(long.class)) {      
        for(long o : (long[]) hiddenArray) {
          objects.add(o);
        }
      } else if(c.equals(float.class)) {
        for(float o : (float[]) hiddenArray) {
          objects.add(o);
        }
      } else if(c.equals(double.class)) {
        for(double o : (double[]) hiddenArray) {
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
      annotationBuilder.addScalarField(name, new EnumAFT(desc), value);
    }

    /*
     * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
     */
    public AnnotationVisitor visitAnnotation(String name, String desc) {
      return new NestedAnnotationSceneReader(this, name, desc);
    }

    /*
     * @see org.objectweb.asm.AnnotationVisitor#visitArray(java.lang.String)
     */
    public AnnotationVisitor visitArray(String name) {
      return new ArrayAnnotationSceneReader(this, name);
    }

    /*
     * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXTargetType(int)
     */
    public void visitXTargetType(int target_type) {
      xTargetTypeArgs.add(target_type);
    }

    /*
     * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXIndex(int)
     */
    public void visitXIndex(int index) {
      xIndexArgs.add(index);
    }

    /*
     * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXLength(int)
     */
    public void visitXLength(int length) {
      xLengthArgs.add(length);
    }

    /*
     * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXLocation(int)
     */
    public void visitXLocation(int location) {
      xLocationsArgs.add(location);
    }

    /*
     * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXLocationLength(int)
     */
    public void visitXLocationLength(int location_length) {
      xLocationLengthArgs.add(location_length);
    }

    /*
     * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXOffset(int)
     */
    public void visitXOffset(int offset) {
      xOffsetArgs.add(offset);
    }

    /*
     * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXStartPc(int)
     */
    public void visitXStartPc(int start_pc) {
      xStartPcArgs.add(start_pc);
    }

    /*
     * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXBoundIndex(int)
     */
    public void visitXParamIndex(int param_index) {
      xParamIndexArgs.add(param_index);
    }

    /*
     * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitXBoundIndex(int)
     */
    public void visitXBoundIndex(int bound_index) {
      xBoundIndexArgs.add(bound_index);
    }

    /*
     * Visits the end of the annotation, and actually writes out the 
     *  annotation into aElement.
     * 
     * @see org.objectweb.asm.ExtendedAnnotationVisitor#visitEnd()
     */
    public void visitEnd() {
      if(xTargetTypeArgs.size() >= 1) {
        TargetType target = TargetType.values()[xTargetTypeArgs.get(0)];

        // TEMP
        // If the expression used to initialize a field contains annotations
        // on instanceofs, typecasts, or news, the extended compiler enters
        // those annotations on the field.  If we get such an annotation and
        // aElement is a field, skip the annotation for now to avoid crashing.
        switch(target) {
        case FIELD_GENERIC_OR_ARRAY: 
          handleFieldGenericArray((ATypeElement<A>) aElement);
          break;
        case LOCAL_VARIABLE:
          handleMethodLocalVariable((AMethod<A>) aElement);
          break;
        case LOCAL_VARIABLE_GENERIC_OR_ARRAY:
          handleMethodLocalVariableGenericArray((AMethod<A>) aElement);
          break;
        case NEW:
          if (aElement instanceof AMethod)
            handleMethodObjectCreation((AMethod<A>) aElement);
          break;
        case NEW_GENERIC_OR_ARRAY:
          if (aElement instanceof AMethod)
            handleMethodObjectCreationGenericArray((AMethod<A>) aElement);
          break;
        case METHOD_PARAMETER_GENERIC_OR_ARRAY:
          handleMethodParameterTypeGenericArray((AMethod<A>) aElement);
          break;
        case METHOD_RECEIVER:
          handleMethodReceiver((AMethod<A>) aElement);
          break;
        case TYPECAST:
          if (aElement instanceof AMethod)
            handleMethodTypecast((AMethod<A>) aElement);
          break;
        case TYPECAST_GENERIC_OR_ARRAY:
          if (aElement instanceof AMethod)
            handleMethodTypecastGenericArray((AMethod<A>) aElement);
          break;
        case METHOD_RETURN_GENERIC_OR_ARRAY:
          handleMethodReturnTypeGenericArray((AMethod<A>) aElement);
          break;
        case INSTANCEOF:
          if (aElement instanceof AMethod)
            handleMethodInstanceOf((AMethod<A>) aElement);
          break;
        case INSTANCEOF_GENERIC_OR_ARRAY:
          if (aElement instanceof AMethod)
            handleMethodInstanceOfGenericArray((AMethod<A>) aElement);
          break;
        case CLASS_TYPE_PARAMETER_BOUND:
          handleClassTypeParameterBound((AClass<A>) aElement);
          break;
        case CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY:
          handleClassTypeParameterBoundGenericArray((AClass<A>) aElement);
          break;
        case METHOD_TYPE_PARAMETER_BOUND:
          handleMethodTypeParameterBound((AMethod<A>) aElement);
          break;
        case METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY:
          handleMethodTypeParameterBoundGenericArray((AMethod<A>) aElement);
          break;
        default:
          throw new RuntimeException("unknown target type: " + target);
        }
      } else {
        // This is not an extended annotation visitior, so just
        //  make annotation and place it in the given AElement.
        aElement.tlAnnotationsHere.add(makeTLAnnotation());
      }
    }

    // The following are utility methods to facilitate creating all the 
    // necessary data structures in the scene library.

    /*
     * Returns an annotation, ready to be placed into the scene, from 
     *  the information visited.
     */
    public A makeAnnotation() {
      return annotationBuilder.finish();
    }

    /*
     * Returns a top-level annoation, ready to be placed into the scene,
     *  from the information visited.
     */
    private TLAnnotation<A> makeTLAnnotation() {
      A a = makeAnnotation();
      AnnotationDef aDef = a.def();
      RetentionPolicy rPolicy = visible ? 
          RetentionPolicy.RUNTIME : RetentionPolicy.CLASS;
      TLAnnotationDef tlaDef = new TLAnnotationDef(aDef, rPolicy);
      return new TLAnnotation<A>(tlaDef, a);
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
    private int makeOffset() {
      return xOffsetArgs.get(0);
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
    private BoundLocation makeBoundLocation() {
      return new BoundLocation(xParamIndexArgs.get(0), xBoundIndexArgs.get(0));
    }

    /*
     * Creates the inner annotation on aElement.innerTypes.
     */
    private void handleFieldGenericArray(ATypeElement<A> aElement) {
      InnerTypeLocation innerTypeLocation = makeInnerTypeLocation();
      AElement<A> aInnerType = aElement.innerTypes.vivify(innerTypeLocation);
      aInnerType.tlAnnotationsHere.add(makeTLAnnotation());
    }

    /*
     * Creates the method receiver annotation on aMethod.
     */
    private void handleMethodReceiver(AMethod<A> aMethod) {
      aMethod.receiver.tlAnnotationsHere.add(makeTLAnnotation());
    }

    /*
     * Creates the local variable annotation on aMethod.
     */
    private void handleMethodLocalVariable(AMethod<A> aMethod) {
      aMethod.locals.vivify(makeLocalLocation()).tlAnnotationsHere.add(
          makeTLAnnotation());
    }

    /*
     * Creates the local variable generic/array annotation on aMethod.
     */
    private void handleMethodLocalVariableGenericArray(AMethod<A> aMethod) {
      aMethod.locals.vivify(makeLocalLocation()).innerTypes.
        vivify(makeInnerTypeLocation()).tlAnnotationsHere.add(makeTLAnnotation());
    }

    /*
     * Creates the object creation annotation on aMethod.
     */
    private void handleMethodObjectCreation(AMethod<A> aMethod) {
      aMethod.news.vivify(makeOffset()).tlAnnotationsHere.add(makeTLAnnotation());
    }

    /*
     * Creates the object creation generic/array annotaiton on aMethod.
     */
    private void handleMethodObjectCreationGenericArray(AMethod<A> aMethod) {
      aMethod.news.vivify(makeOffset()).innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeTLAnnotation());
    }

    /*
     * Creates the method parameter type generic/array annotation on aMethod.
     */
    private void handleMethodParameterTypeGenericArray(AMethod<A> aMethod) {
      aMethod.parameters.vivify(makeIndex()).innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeTLAnnotation());
    }

    /*
     * Creates the typecast annotation on aMethod.
     */
    private void handleMethodTypecast(AMethod<A> aMethod) {
     aMethod.typecasts.vivify(makeOffset()).tlAnnotationsHere.add(
         makeTLAnnotation());
    }

    /*
     * Creates the typecast generic/array annotation on aMethod.
     */
    private void handleMethodTypecastGenericArray(AMethod<A> aMethod) {
      aMethod.typecasts.vivify(makeOffset()).innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeTLAnnotation());
    }

    /*
     * Creates the method return type generic/array annotation on aMethod.
     */
    private void handleMethodReturnTypeGenericArray(AMethod<A> aMethod) {
      aMethod.innerTypes.vivify(makeInnerTypeLocation()).tlAnnotationsHere.add(
          makeTLAnnotation());
    }
    
    /*
     * Creates the method instance of annotation on aMethod.
     */
    private void handleMethodInstanceOf(AMethod<A> aMethod) {
      aMethod.instanceofs.vivify(makeOffset()).tlAnnotationsHere.add(
          makeTLAnnotation());
    }
    
    /*
     * Creates the method instance of generic/array annotation on aMethod.
     */
    private void handleMethodInstanceOfGenericArray(AMethod<A> aMethod) {
      aMethod.typecasts.vivify(makeOffset()).innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeTLAnnotation());
    }
    
    /*
     * Creates the class type parameter bound annotation on aClass.
     */
    private void handleClassTypeParameterBound(AClass<A> aClass) {
      aClass.bounds.vivify(makeBoundLocation())
          .tlAnnotationsHere.add(makeTLAnnotation());
    }
    
    /*
     * Creates the class type parameter bound annotation on aClass.
     */
    private void handleClassTypeParameterBoundGenericArray(AClass<A> aClass) {
      aClass.bounds.vivify(makeBoundLocation()).innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeTLAnnotation());
    }
    
    /*
     * Creates the class type parameter bound annotation on aClass.
     */
    private void handleMethodTypeParameterBound(AMethod<A> aMethod) {
      aMethod.bounds.vivify(makeBoundLocation())
          .tlAnnotationsHere.add(makeTLAnnotation());
    }
    
    /*
     * Creates the class type parameter bound annotation on aClass.
     */
    private void handleMethodTypeParameterBoundGenericArray(AMethod<A> aMethod) {
      aMethod.bounds.vivify(makeBoundLocation()).innerTypes.vivify(
          makeInnerTypeLocation()).tlAnnotationsHere.add(makeTLAnnotation());
    }
    
    /*
     * Hook for NestedAnnotationSceneReader; overridden by
     * ArrayAnnotationSceneReader to add an array element instead of a field
     */
    void supplySubannotation(String fieldName, A annotation) {
      annotationBuilder.addScalarField(fieldName,
              new AnnotationAFT(annotation.def()), annotation);
    }
  }

  /*
   * A NestedAnnotationSceneReader is an AnnotationSceneReader
   * that will read in an entire annotation on a field (of type annotation)
   * of it's parent, and once it has fully visited that annotation, it will
   * call it's parent annotation builder to include that field, so after
   * it's parent constructs and returns this as an AnnotationVisitor
   * (visitAnnotation()), it no longer needs to wory about that field.
   */
  private class NestedAnnotationSceneReader extends AnnotationSceneReader {
    private AnnotationSceneReader parent;
    private String name;
    private String desc;

    public NestedAnnotationSceneReader(AnnotationSceneReader parent, 
        String name, String desc) {
      super(name, parent.visible, parent.aElement);
      this.parent = parent;
      this.name = name;
      this.desc = desc;
    }

    @Override
    public void visitEnd() {
      super.visitEnd();
      A a = super.makeAnnotation();
      parent.supplySubannotation(name, a);
    }
  }

  /* 
   * An ArrayAnnotationSceneReader is an AnnotationSceneReader
   * that will read in all the values of an array of a field (of type array)
   * of it's parent, and once it has fully visited the array, it will
   * call it's parent annotation builder to include that array, so after
   * it's parent constructs and returns this as an AnnotationVisitor 
   * (visitArray()), it no longer needs to worry about that array.
   * 
   * Note that by specification of AnnotationVisitor.visitArray(), the only 
   * methods that should be called on this are visit(String name, Object value)
   * and visitEnd().
   */
  private class ArrayAnnotationSceneReader extends AnnotationSceneReader {
    private AnnotationSceneReader parent;
    private ArrayBuilder arrayBuilder;
    private ScalarAFT elementType;
    private String arrayName;

    public ArrayAnnotationSceneReader(
        AnnotationSceneReader parent, 
        String name) {
      super(name, parent.visible, parent.aElement);
      this.parent = parent;
      this.arrayName = name;
      this.arrayBuilder = null;
    }

    private void prepareForElement(ScalarAFT elementType) {
      if (arrayBuilder == null) {
        this.elementType = elementType;
        arrayBuilder = parent.annotationBuilder.beginArrayField(arrayName,
                new ArrayAFT(elementType));
      } else {
        if (!this.elementType.equals(elementType))
          throw new RuntimeException("Array contains elements of different types!");
      }
    }

    @Override
    public void visit(String name, Object value) {
      prepareForElement(BasicAFT.forType(value.getClass()));
      arrayBuilder.appendElement(value);
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
      prepareForElement(new EnumAFT(classDescToName(desc)));
      arrayBuilder.appendElement(value);
    }
    
    @Override
    public AnnotationVisitor visitArray(String name) {
      throw new AssertionError("Multidimensional array in annotation!");
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
      // The NASR will regurgitate the name we pass here when it calls
      // supplySubannotation.  Since we ignore the name there, it doesn't
      // matter what name we pass here.
      return new NestedAnnotationSceneReader(this, name, desc);
    }

    @Override
    public void visitEnd() {
      if(arrayBuilder != null) {
        arrayBuilder.finish();
      } else {
        // This was a zero-element array
        parent.annotationBuilder.addEmptyArrayField(arrayName);
      }
    }
    
    @Override
    void supplySubannotation(String fieldName, A annotation) {
      prepareForElement(new AnnotationAFT(annotation.def()));
      arrayBuilder.appendElement(annotation);
    }
  }   

  /*
   * A FieldAnnotationSceneReader is a FieldVisitor that only cares about
   * visiting [extended]annotations.  Attributes are ignored and visitEnd() has
   * no effect.  An AnnotationSceneReader is returned for normal and extended
   * AnnotationVisitors.  The AnnotationSceneReaders have a reference to
   * an ATypeElement<A> that this is visiting, and they will write out
   * all the information to that ATypeElement<A> after visiting each annotation.
   */
  private class FieldAnnotationSceneReader extends EmptyVisitor
  implements FieldVisitor {

    private String name;
    private String desc;
    private String signature;
    private Object value;
    private ATypeElement<A> aField;

    public FieldAnnotationSceneReader(
        String name,
        String desc,
        String signature, 
        Object value, 
        ATypeElement<A> aField) {
      this.name = name;
      this.desc = desc;
      this.signature = signature;
      this.value = value;
      this.aField = aField;
    }

    @Override 
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return visitExtendedAnnotation(desc, visible);
    }

    @Override 
    public ExtendedAnnotationVisitor visitExtendedAnnotation(
        String desc, boolean visible) {
      return new AnnotationSceneReader(desc, visible, aField);
    }
  }

  /*
   * Similarly to FieldAnnotationSceneReader, this is a MethodVisitor that
   * only cares about visiting [extended]annotations.  Attributes are ignored,
   * all code is ignored and visitEnd() has no effect.  An AnnotationSceneReader
   * is returned for normal and extended AnnotationVisitors.  The 
   * AnnotationSceneReaders have a reference to an AMethod<A> that this is
   * visiting, and they will write out all the information to that
   * AMethod<A> after visiting each annotation.
   */
  private class MethodAnnotationSceneReader extends EmptyVisitor
  implements MethodVisitor {

    private String name;
    private String desc;
    private String signature;
    private AElement<A> aMethod;

    public MethodAnnotationSceneReader(
        String name, String desc, String signature, AElement<A> aMethod) {
      this.name = name;
      this.desc = desc;
      this.signature = signature;
      this.aMethod = aMethod;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return visitExtendedAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(
        int parameter, java.lang.String desc, boolean visible) {
      return new AnnotationSceneReader(desc, visible,
              ((AMethod<A>) aMethod).parameters.vivify(parameter));
    }

    @Override
    public ExtendedAnnotationVisitor visitExtendedAnnotation(
        String desc, boolean visible) {
      return new AnnotationSceneReader(desc, visible, aMethod);
    }
  }
}
