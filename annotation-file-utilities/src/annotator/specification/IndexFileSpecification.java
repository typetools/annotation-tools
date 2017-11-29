package annotator.specification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.objectweb.asm.ClassReader;

import plume.FileIOException;
import plume.Pair;
import scenelib.type.DeclaredType;
import scenelib.type.Type;
import scenelib.annotations.Annotation;
import scenelib.annotations.el.ABlock;
import scenelib.annotations.el.AClass;
import scenelib.annotations.el.AElement;
import scenelib.annotations.el.AExpression;
import scenelib.annotations.el.AField;
import scenelib.annotations.el.AMethod;
import scenelib.annotations.el.AScene;
import scenelib.annotations.el.ATypeElement;
import scenelib.annotations.el.ATypeElementWithType;
import scenelib.annotations.el.AnnotationDef;
import scenelib.annotations.el.BoundLocation;
import scenelib.annotations.el.InnerTypeLocation;
import scenelib.annotations.el.LocalLocation;
import scenelib.annotations.el.RelativeLocation;
import scenelib.annotations.el.TypeIndexLocation;
import scenelib.annotations.field.AnnotationFieldType;
import scenelib.annotations.io.ASTPath;
import scenelib.annotations.io.IndexFileParser;
import scenelib.annotations.util.coll.VivifyingMap;
import annotator.find.AnnotationInsertion;
import annotator.find.CastInsertion;
import annotator.find.CloseParenthesisInsertion;
import annotator.find.ConstructorInsertion;
import annotator.find.Criteria;
import annotator.find.GenericArrayLocationCriterion;
import annotator.find.Insertion;
import annotator.find.IntersectionTypeLocationCriterion;
import annotator.find.NewInsertion;
import annotator.find.ReceiverInsertion;
import annotator.scanner.MethodOffsetClassVisitor;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.sun.source.tree.Tree;

public class IndexFileSpecification {
  private final Multimap<Insertion, Annotation> insertionSources =
      LinkedHashMultimap.<Insertion, Annotation>create();
  private final List<Insertion> insertions = new ArrayList<Insertion>();
  /** Is a member of insertions (if non-null). */
  private ConstructorInsertion constructorInsertion = null;
  private final AScene scene;
  private final String indexFileName;

  // If set, do not attempt to read class files with Asm.
  // Mostly for debugging and workarounds.
  public static boolean noAsm = false;

  private static boolean debug = false;

  public IndexFileSpecification(String indexFileName) {
    this.indexFileName = indexFileName;
    scene = new AScene();
  }

  public List<Insertion> parse() throws FileIOException {
    try {
      Map<String, AnnotationDef> annotationDefs =
          IndexFileParser.parseFile(indexFileName, scene);
      Set<String> defKeys = annotationDefs.keySet();
      Set<String> ambiguous = new LinkedHashSet<String>();
      // If a qualified name's unqualified counterpart maps to null in
      // defKeys, it means that the unqualified name is ambiguous and
      // thus should always be qualified.
      for (String key : defKeys) {
        int ix = Math.max(key.lastIndexOf("."), key.lastIndexOf("$"));
        if (ix >= 0) {
          String name = key.substring(ix+1);
          // containsKey() would give wrong result here
          if (annotationDefs.get(name) == null) { ambiguous.add(name); }
        }
      }
      Insertion.setAlwaysQualify(ambiguous);
    } catch (FileIOException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Exception while parsing index file", e);
    }

    if (debug) {
      System.out.printf("Scene parsed from %s:%n", indexFileName);
      System.out.println(scene.unparse());
    }

    parseScene();
//    debug("---------------------------------------------------------");
    return this.insertions;
  }

  public Map<String, Set<String>> annotationImports() {
    return scene.imports;
  }

  public Multimap<Insertion, Annotation> insertionSources() {
    return insertionSources;
  }

  private void addInsertionSource(Insertion ins, Annotation anno) {
    insertionSources.put(ins, anno);
  }

  private static void debug(String s) {
    if (debug) {
      System.out.println(s);
    }
  }

  /*
  private static void debug(String s, Object... args) {
    if (debug) {
      System.out.printf(s, args);
    }
  }
  */

  public AScene getScene() { return scene; }

  /** Fill in this.insertions with insertion pairs. */
  private void parseScene() {
    debug("parseScene()");

    // Empty criterion to work from.
    CriterionList clist = new CriterionList();

    VivifyingMap<String, AElement> packages = scene.packages;
    for (Map.Entry<String, AElement> entry : packages.entrySet()) {
      parsePackage(clist, entry.getKey(), entry.getValue());
    }

    VivifyingMap<String, AClass> classes = scene.classes;
    for (Map.Entry<String, AClass> entry : classes.entrySet()) {
      String key = entry.getKey();
      AClass clazz = entry.getValue();
      if (key.endsWith(".package-info")) {
        // strip off suffix to get package name
        parsePackage(clist, key.substring(0, key.length()-13), clazz);
      } else {
        parseClass(clist, key, clazz);
      }
    }
  }

  // There is no .class file corresponding to the package-info.java file.
  private void parsePackage(CriterionList clist, String packageName, AElement element) {
    // There is no Tree.Kind.PACKAGE, only Tree.Kind.COMPILATION_UNIT.
    // CompilationUnitTree has getPackageName and getPackageAnnotations
    CriterionList packageClist = clist.add(Criteria.packageDecl(packageName));
    parseElement(packageClist, element);
  }



  /** Fill in this.insertions with insertion pairs.
   * @param className is fully qualified
   */
  private void parseClass(CriterionList clist, String className, AClass clazz) {
    constructorInsertion = null;  // 0 or 1 per class
    if (! noAsm) {
      //  load extra info using asm
      debug("parseClass(" + className + ")");
      try {
        ClassReader classReader = new ClassReader(className);
        MethodOffsetClassVisitor cv = new MethodOffsetClassVisitor(classReader);
        classReader.accept(cv, false);
        debug("Done reading " + className + ".class");
      } catch (IOException e) {
        // If .class file not found, still proceed, in case
        // user only wants method signature annotations.
        // (TODO: It would be better to store which classes could not be
        // found, then issue a warning only if an attempt is made to use
        // the (missing) information.  See
        // https://github.com/typetools/annotation-tools/issues/34 .)
        System.out.println("Warning: IndexFileSpecification did not find classfile for: " + className);
        // throw new RuntimeException("IndexFileSpecification.parseClass: " + e);
      } catch (RuntimeException e) {
        System.err.println("IndexFileSpecification had a problem reading class: " + className);
        throw e;
      } catch (Error e) {
        System.err.println("IndexFileSpecification had a problem reading class: " + className);
        throw e;
      }
    }

    CriterionList clistSansClass = clist;

    clist = clist.add(Criteria.inClass(className, true));
    CriterionList classClist = clistSansClass.add(Criteria.is(Tree.Kind.CLASS, className));
    parseElement(classClist, clazz);

    VivifyingMap<BoundLocation, ATypeElement> bounds = clazz.bounds;
    for (Entry<BoundLocation, ATypeElement> entry : bounds.entrySet()) {
      BoundLocation boundLoc = entry.getKey();
      ATypeElement bound = entry.getValue();
      CriterionList boundList = clist.add(Criteria.classBound(className, boundLoc));
      for (Entry<InnerTypeLocation, ATypeElement> innerEntry : bound.innerTypes.entrySet()) {
        InnerTypeLocation innerLoc = innerEntry.getKey();
        AElement ae = innerEntry.getValue();
        CriterionList innerBoundList = boundList.add(Criteria.atLocation(innerLoc));
        parseElement(innerBoundList, ae);
      }
      CriterionList outerClist = boundList.add(Criteria.atLocation());
      parseElement(outerClist, bound);
    }

    clist = clist.add(Criteria.inClass(className, /*exactMatch=*/ false));

    VivifyingMap<TypeIndexLocation, ATypeElement> extimpl = clazz.extendsImplements;
    for (Entry<TypeIndexLocation, ATypeElement> entry : extimpl.entrySet()) {
      TypeIndexLocation eiLoc = entry.getKey();
      ATypeElement ei = entry.getValue();
      CriterionList eiList = clist.add(Criteria.atExtImplsLocation(className, eiLoc));

      for (Entry<InnerTypeLocation, ATypeElement> innerEntry : ei.innerTypes.entrySet()) {
        InnerTypeLocation innerLoc = innerEntry.getKey();
        AElement ae = innerEntry.getValue();
        CriterionList innerBoundList = eiList.add(Criteria.atLocation(innerLoc));
        parseElement(innerBoundList, ae);
      }
      parseElement(eiList, ei);
    }

    parseASTInsertions(clist, clazz.insertAnnotations, clazz.insertTypecasts);

    for (Map.Entry<String, AField> entry : clazz.fields.entrySet()) {
//      clist = clist.add(Criteria.notInMethod()); // TODO: necessary? what is in class but not in method?
      parseField(clist, entry.getKey(), entry.getValue());
    }
    for (Map.Entry<String, AMethod> entry : clazz.methods.entrySet()) {
      parseMethod(clist, className, entry.getKey(), entry.getValue());
    }
    for (Map.Entry<Integer, ABlock> entry : clazz.staticInits.entrySet()) {
      parseStaticInit(clist, className, entry.getKey(), entry.getValue());
    }
    for (Map.Entry<Integer, ABlock> entry : clazz.instanceInits.entrySet()) {
      parseInstanceInit(clist, className, entry.getKey(), entry.getValue());
    }
    for (Map.Entry<String, AExpression> entry : clazz.fieldInits.entrySet()) {
      parseFieldInit(clist, className, entry.getKey(), entry.getValue());
    }

    debug("parseClass(" + className + "):  done");
  }

  /** Fill in this.insertions with insertion pairs. */
  private void parseField(CriterionList clist, String fieldName, AField field) {
    // parse declaration annotations
    parseElement(clist.add(Criteria.field(fieldName, true)), field);

    // parse type annotations
    clist = clist.add(Criteria.field(fieldName, false));
    parseInnerAndOuterElements(clist, field.type);
    parseASTInsertions(clist, field.insertAnnotations, field.insertTypecasts);
  }

  private void parseStaticInit(CriterionList clist, String className, int blockID, ABlock block) {
    clist = clist.add(Criteria.inStaticInit(blockID));
    // the method name argument is not used for static initializers, which are only used
    // in source specifications. Same for instance and field initializers.
    // the empty () are there to prevent the whole string to be removed in later parsing.
    parseBlock(clist, className, "static init number " + blockID + "()", block);
  }

  private void parseInstanceInit(CriterionList clist, String className, int blockID, ABlock block) {
    clist = clist.add(Criteria.inInstanceInit(blockID));
    parseBlock(clist, className, "instance init number " + blockID + "()", block);
  }

  // keep the descriptive strings for field initializers and static inits consistent
  // with text used in NewCriterion.

  private void parseFieldInit(CriterionList clist, String className, String fieldName, AExpression exp) {
    clist = clist.add(Criteria.inFieldInit(fieldName));
    parseExpression(clist, className, "init for field " + fieldName + "()", exp);
  }

  /**
   * Fill in this.insertions with insertion pairs.
   * @param clist the criteria specifying the location of the insertions
   * @param element holds the annotations to be inserted
   * @return a list of the {@link AnnotationInsertion}s that are created
   */
  private List<Insertion> parseElement(CriterionList clist, AElement element) {
    return parseElement(clist, element, new ArrayList<Insertion>(), false);
  }

  /**
   * Fill in this.insertions with insertion pairs.
   * @param clist the criteria specifying the location of the insertions
   * @param element holds the annotations to be inserted
   * @param isCastInsertion {@code true} if this for a cast insertion, {@code false}
   *          otherwise.
   * @return a list of the {@link AnnotationInsertion}s that are created
   */
  private List<Insertion> parseElement(CriterionList clist, AElement element,
      boolean isCastInsertion) {
    return parseElement(clist, element, new ArrayList<Insertion>(), isCastInsertion);
  }

  /**
   * Fill in this.insertions with insertion pairs.
   * @param clist the criteria specifying the location of the insertions
   * @param element holds the annotations to be inserted
   * @param add {@code true} if the create {@link AnnotationInsertion}s should
   *         be added to {@link #insertions}, {@code false} otherwise.
   * @return a list of the {@link AnnotationInsertion}s that are created
   */
  private List<Insertion> parseElement(CriterionList clist, AElement element,
      List<Insertion> innerTypeInsertions) {
    return parseElement(clist, element, innerTypeInsertions, false);
  }

  /**
   * Fill in this.insertions with insertion pairs.
   * @param clist the criteria specifying the location of the insertions
   * @param element holds the annotations to be inserted
   * @param innerTypeInsertions the insertions on the inner type of this
   *         element. This is only used for receiver and "new" insertions.
   *         See {@link ReceiverInsertion} for more details.
   * @param isCastInsertion {@code true} if this for a cast insertion, {@code false}
   *          otherwise.
   * @return a list of the {@link AnnotationInsertion}s that are created
   */
  private List<Insertion> parseElement(CriterionList clist, AElement element,
      List<Insertion> innerTypeInsertions, boolean isCastInsertion) {
    // Use at most one receiver and one cast insertion and add all of the
    // annotations to the one insertion.
    ReceiverInsertion receiver = null;
    NewInsertion neu = null;
    CastInsertion cast = null;
    CloseParenthesisInsertion closeParen = null;
    List<Insertion> annotationInsertions = new ArrayList<Insertion>();
    Set<Pair<String, Annotation>> elementAnnotations = getElementAnnotations(element);
    if (elementAnnotations.isEmpty()) {
      Criteria criteria = clist.criteria();
      if (element instanceof ATypeElementWithType) {
        // Still insert even if it's a cast insertion with no outer
        // annotations to just insert a cast, or insert a cast with
        // annotations on the compound types.
        Pair<CastInsertion, CloseParenthesisInsertion> pair =
            createCastInsertion(((ATypeElementWithType) element).getType(),
                null, innerTypeInsertions, criteria);
        cast = pair.a;
        closeParen = pair.b;
      } else if (!innerTypeInsertions.isEmpty()) {
        if (isOnReceiver(criteria)) {
          receiver = new ReceiverInsertion(new DeclaredType(),
              criteria, innerTypeInsertions);
        } else if (isOnNew(criteria)) {
          neu = new NewInsertion(new DeclaredType(),
              criteria, innerTypeInsertions);
        }
      }
    }

    for (Pair<String, Annotation> p : elementAnnotations) {
      List<Insertion> elementInsertions = new ArrayList<Insertion>();
      String annotationString = p.a;
      Annotation annotation = p.b;
      Criteria criteria = clist.criteria();
      Boolean isDeclarationAnnotation = !annotation.def.isTypeAnnotation()
          || criteria.isOnFieldDeclaration();
      if (noTypePath(criteria) && isOnReceiver(criteria)) {
        if (receiver == null) {
          DeclaredType type = new DeclaredType();
          type.addAnnotation(annotationString);
          receiver = new ReceiverInsertion(type, criteria, innerTypeInsertions);
          elementInsertions.add(receiver);
        } else {
          receiver.getType().addAnnotation(annotationString);
        }
        addInsertionSource(receiver, annotation);
      } else if (noTypePath(criteria) && isOnNew(criteria)) {
        if (neu == null) {
          DeclaredType type = new DeclaredType();
          type.addAnnotation(annotationString);
          neu = new NewInsertion(type, criteria, innerTypeInsertions);
          elementInsertions.add(neu);
        } else {
          neu.getType().addAnnotation(annotationString);
        }
        addInsertionSource(neu, annotation);
      } else if (element instanceof ATypeElementWithType) {
        if (cast == null) {
          Pair<CastInsertion, CloseParenthesisInsertion> insertions = createCastInsertion(
              ((ATypeElementWithType) element).getType(), annotationString,
              innerTypeInsertions, criteria);
          cast = insertions.a;
          closeParen = insertions.b;
          elementInsertions.add(cast);
          elementInsertions.add(closeParen);
          // no addInsertionSource, as closeParen is not explicit in scene
        } else {
          cast.getType().addAnnotation(annotationString);
        }
        addInsertionSource(cast, annotation);
      } else {
        RelativeLocation loc = criteria.getCastRelativeLocation();
        if (loc != null && loc.type_index > 0) {
          criteria.add(new IntersectionTypeLocationCriterion(loc));
        }
        Insertion ins = new AnnotationInsertion(annotationString, criteria,
                                      isDeclarationAnnotation);
        debug("parsed: " + ins);
        if (!isCastInsertion) {
            // Annotations on compound types of a cast insertion will be
            // inserted directly on the cast insertion.
            // this.insertions.add(ins);
            elementInsertions.add(ins);
        }
        annotationInsertions.add(ins);
        addInsertionSource(ins, annotation);
      }
      this.insertions.addAll(elementInsertions);

      // exclude expression annotations
      if (noTypePath(criteria) && isOnNullaryConstructor(criteria)) {
        if (constructorInsertion == null) {
          DeclaredType type = new DeclaredType(criteria.getClassName());
          constructorInsertion = new ConstructorInsertion(type, criteria,
              new ArrayList<Insertion>());
          this.insertions.add(constructorInsertion);
        } else {
          if (annotator.Main.temporaryDebug) {
            System.out.printf("Ignoring criteria=%s because constructorInsertion=%s%n", criteria, constructorInsertion);
          }
        }
        // no addInsertionSource, as constructorInsertion is not explicit in scene
        for (Insertion i : elementInsertions) {
          if (i.getKind() == Insertion.Kind.RECEIVER) {
            constructorInsertion.addReceiverInsertion((ReceiverInsertion) i);
          } else if (criteria.isOnReturnType()) {
            ((DeclaredType) constructorInsertion.getType()).addAnnotation(annotationString);
          } else if (isDeclarationAnnotation) {
            constructorInsertion.addDeclarationInsertion(i);
          } else {
            annotationInsertions.add(i);
          }
        }
      }
      elementInsertions.clear();
    }
    if (receiver != null) {
        this.insertions.add(receiver);
    }
    if (neu != null) {
        this.insertions.add(neu);
    }
    if (cast != null) {
        this.insertions.add(cast);
        this.insertions.add(closeParen);
    }
    if (constructorInsertion != null) {
        constructorInsertion.setInserted(false);
    }
    return annotationInsertions;
  }

  private boolean noTypePath(Criteria criteria) {
    GenericArrayLocationCriterion galc =
        criteria.getGenericArrayLocation();
    return galc == null || galc.getLocation().isEmpty();
  }

  public static boolean isOnReceiver(Criteria criteria) {
    ASTPath astPath = criteria.getASTPath();
    if (astPath == null) { return criteria.isOnReceiver(); }
    if (astPath.isEmpty()) { return false; }
    ASTPath.ASTEntry entry = astPath.getLast();
    return entry.childSelectorIs(ASTPath.PARAMETER)
        && entry.getArgument() < 0;
  }

  public static boolean isOnNew(Criteria criteria) {
    ASTPath astPath = criteria.getASTPath();
    if (astPath == null || astPath.isEmpty()) { return criteria.isOnNew(); }
    ASTPath.ASTEntry entry = astPath.getLast();
    Tree.Kind kind = entry.getTreeKind();
    return kind == Tree.Kind.NEW_ARRAY
            && entry.childSelectorIs(ASTPath.TYPE)
            && entry.getArgument() == 0
        || kind == Tree.Kind.NEW_CLASS
            && entry.childSelectorIs(ASTPath.IDENTIFIER);
  }

  private static boolean isOnNullaryConstructor(Criteria criteria) {
    if (criteria.isOnMethod("<init>()V")) {
      ASTPath astPath = criteria.getASTPath();
      if (astPath == null || astPath.isEmpty()) {
        return !criteria.isOnNew();  // exclude expression annotations
      }
      ASTPath.ASTEntry entry = astPath.get(0);
      return entry.getTreeKind() == Tree.Kind.METHOD
          && (entry.childSelectorIs(ASTPath.TYPE) || isOnReceiver(criteria));
    }
    return false;
  }

  /**
   * Creates the {@link CastInsertion} and {@link CloseParenthesisInsertion}
   * for a cast insertion.
   *
   * @param type the cast type to insert
   * @param annotationString the annotation on the outermost type, or
   *         {@code null} if none. With no outermost annotation this cast
   *         insertion will either be a cast without any annotations or a cast
   *         with annotations only on the compound types.
   * @param innerTypeInsertions the annotations on the inner types
   * @param criteria the criteria for the location of this insertion
   * @return the {@link CastInsertion} and {@link CloseParenthesisInsertion}
   */
  private Pair<CastInsertion, CloseParenthesisInsertion> createCastInsertion(
      Type type, String annotationString, List<Insertion> innerTypeInsertions,
      Criteria criteria) {
    if (annotationString != null) {
      type.addAnnotation(annotationString);
    }
    Insertion.decorateType(innerTypeInsertions, type, criteria.getASTPath());
    CastInsertion cast = new CastInsertion(criteria, type);
    CloseParenthesisInsertion closeParen = new CloseParenthesisInsertion(
        criteria, cast.isSeparateLine());
    return new Pair<CastInsertion, CloseParenthesisInsertion>(cast, closeParen);
  }

  /**
   * Fill in this.insertions with insertion pairs for the outer and inner types.
   * @param clist the criteria specifying the location of the insertions
   * @param typeElement holds the annotations to be inserted
   */
  private void parseInnerAndOuterElements(CriterionList clist, ATypeElement typeElement) {
    parseInnerAndOuterElements(clist, typeElement, false);
  }

  /**
   * Fill in this.insertions with insertion pairs for the outer and inner types.
   * @param clist the criteria specifying the location of the insertions
   * @param typeElement holds the annotations to be inserted
   * @param isCastInsertion {@code true} if this for a cast insertion, {@code false}
   *          otherwise.
   */
  private void parseInnerAndOuterElements(CriterionList clist,
      ATypeElement typeElement, boolean isCastInsertion) {
    List<Insertion> innerInsertions = new ArrayList<Insertion>();
    for (Entry<InnerTypeLocation, ATypeElement> innerEntry: typeElement.innerTypes.entrySet()) {
      InnerTypeLocation innerLoc = innerEntry.getKey();
      AElement innerElement = innerEntry.getValue();
      CriterionList innerClist = clist.add(Criteria.atLocation(innerLoc));
      innerInsertions.addAll(parseElement(innerClist, innerElement, isCastInsertion));
    }
    CriterionList outerClist = clist;
    if (!isCastInsertion) {
      // Cast insertion is never on an existing type.
      outerClist = clist.add(Criteria.atLocation());
    }
    parseElement(outerClist, typeElement, innerInsertions);
  }

  // Returns a string representation of the annotations at the element.
  private Set<Pair<String, Annotation>>
  getElementAnnotations(AElement element) {
    Set<Pair<String, Annotation>> result =
        new LinkedHashSet<Pair<String, Annotation>>(
            element.tlAnnotationsHere.size());
    for (Annotation a : element.tlAnnotationsHere) {
      AnnotationDef adef = a.def;
      String annotationString = "@" + adef.name;

      if (a.fieldValues.size() == 1 && a.fieldValues.containsKey("value")) {
        annotationString += "(" + formatFieldValue(a, "value") + ")";
      } else if (a.fieldValues.size() > 0) {
        annotationString += "(";
        boolean first = true;
        for (String field : a.fieldValues.keySet()) {
          // parameters of the annotation
          if (!first) {
            annotationString += ", ";
          }
          annotationString += field + "=" + formatFieldValue(a, field);
          first = false;
        }
        annotationString += ")";
      }
      // annotationString += " ";
      result.add(new Pair<String, Annotation>(annotationString, a));
    }
    return result;
  }

  private String formatFieldValue(Annotation a, String field) {
    AnnotationFieldType fieldType = a.def.fieldTypes.get(field);
    assert fieldType != null;
    return fieldType.format(a.fieldValues.get(field));
  }

  private void parseMethod(CriterionList clist, String className, String methodName, AMethod method) {
    // Being "in" a method refers to being somewhere in the
    // method's tree, which includes return types, parameters, receiver, and
    // elements inside the method body.
    clist = clist.add(Criteria.inMethod(methodName));

    // parse declaration annotations, fill in this.insertions
    parseElement(clist, method);

    // parse receiver
    CriterionList receiverClist = clist.add(Criteria.receiver(methodName));
    parseInnerAndOuterElements(receiverClist, method.receiver.type);

    // parse return type
    CriterionList returnClist = clist.add(Criteria.returnType(className, methodName));
    parseInnerAndOuterElements(returnClist, method.returnType);

    // parse bounds of method
    for (Entry<BoundLocation, ATypeElement> entry : method.bounds.entrySet()) {
      BoundLocation boundLoc = entry.getKey();
      ATypeElement bound = entry.getValue();
      CriterionList boundClist = clist.add(Criteria.methodBound(methodName, boundLoc));
      parseInnerAndOuterElements(boundClist, bound);
    }

    // parse parameters of method
    for (Entry<Integer, AField> entry : method.parameters.entrySet()) {
      Integer index = entry.getKey();
      AField param = entry.getValue();
      CriterionList paramClist = clist.add(Criteria.param(methodName, index));
      // parse declaration annotations
      // parseField(paramClist, index.toString(), param);
      parseInnerAndOuterElements(paramClist, param.type);
    }

    // parse insert annotations/typecasts of method
    parseASTInsertions(clist, method.insertAnnotations, method.insertTypecasts);

    // parse body of method
    parseBlock(clist, className, methodName, method.body);
  }

  private void parseBlock(CriterionList clist, String className, String methodName, ABlock block) {
    // parse locals of method
    for (Entry<LocalLocation, AField> entry : block.locals.entrySet()) {
      LocalLocation loc = entry.getKey();
      AElement var = entry.getValue();
      CriterionList varClist = clist.add(Criteria.local(methodName, loc));
      // parse declaration annotations
      parseElement(varClist, var);
      parseInnerAndOuterElements(varClist, var.type);
    }

    parseExpression(clist, className, methodName, block);
  }

  private void parseExpression(CriterionList clist, String className, String methodName, AExpression exp) {
    // parse typecasts of method
    for (Entry<RelativeLocation, ATypeElement> entry : exp.typecasts.entrySet()) {
      RelativeLocation loc = entry.getKey();
      ATypeElement cast = entry.getValue();
      CriterionList castClist = clist.add(Criteria.cast(methodName, loc));
      parseInnerAndOuterElements(castClist, cast);
    }

    // parse news (object creation) of method
    for (Entry<RelativeLocation, ATypeElement> entry : exp.news.entrySet()) {
      RelativeLocation loc = entry.getKey();
      ATypeElement newObject = entry.getValue();
      CriterionList newClist = clist.add(Criteria.newObject(methodName, loc));
      parseInnerAndOuterElements(newClist, newObject);
    }

    // parse instanceofs of method
    for (Entry<RelativeLocation, ATypeElement> entry : exp.instanceofs.entrySet()) {
      RelativeLocation loc = entry.getKey();
      ATypeElement instanceOf = entry.getValue();
      CriterionList instanceOfClist = clist.add(Criteria.instanceOf(methodName, loc));
      parseInnerAndOuterElements(instanceOfClist, instanceOf);
    }

    // parse member references of method
    for (Entry<RelativeLocation, ATypeElement> entry : exp.refs.entrySet()) {
      RelativeLocation loc = entry.getKey();
      ATypeElement ref = entry.getValue();
      CriterionList instanceOfClist =
          clist.add(Criteria.memberReference(methodName, loc));
      parseInnerAndOuterElements(instanceOfClist, ref);
    }

    // parse method invocations of method
    for (Entry<RelativeLocation, ATypeElement> entry : exp.calls.entrySet()) {
      RelativeLocation loc = entry.getKey();
      ATypeElement call = entry.getValue();
      CriterionList instanceOfClist =
          clist.add(Criteria.methodCall(methodName, loc));
      parseInnerAndOuterElements(instanceOfClist, call);
    }

    // parse lambda expressions of method
    for (Entry<RelativeLocation, AMethod> entry : exp.funs.entrySet()) {
      RelativeLocation loc = entry.getKey();
      AMethod lambda = entry.getValue();
      CriterionList lambdaClist = clist.add(Criteria.lambda(methodName, loc));
      parseLambdaExpression(className, methodName, lambda, lambdaClist);
    }
  }

  private void parseLambdaExpression(String className, String methodName,
      AMethod lambda, CriterionList clist) {
    for (Entry<Integer, AField> entry : lambda.parameters.entrySet()) {
      Integer index = entry.getKey();
      AField param = entry.getValue();
      CriterionList paramClist = clist.add(Criteria.param("(anonymous)", index));
      parseInnerAndOuterElements(paramClist, param.type);
      parseASTInsertions(paramClist, param.insertAnnotations, param.insertTypecasts);
    }
    parseBlock(clist, className, methodName, lambda.body);
  }

  private void parseASTInsertions(CriterionList clist,
      VivifyingMap<ASTPath, ATypeElement> insertAnnotations,
      VivifyingMap<ASTPath, ATypeElementWithType> insertTypecasts) {
    for (Entry<ASTPath, ATypeElement> entry : insertAnnotations.entrySet()) {
      ASTPath astPath = entry.getKey();
      ATypeElement insertAnnotation = entry.getValue();
      CriterionList insertAnnotationClist =
          clist.add(Criteria.astPath(astPath));
      parseInnerAndOuterElements(insertAnnotationClist,
          insertAnnotation, true);
    }
    for (Entry<ASTPath, ATypeElementWithType> entry : insertTypecasts.entrySet()) {
      ASTPath astPath = entry.getKey();
      ATypeElementWithType insertTypecast = entry.getValue();
      CriterionList insertTypecastClist = clist.add(Criteria.astPath(astPath));
      parseInnerAndOuterElements(insertTypecastClist, insertTypecast, true);
    }
  }
}
