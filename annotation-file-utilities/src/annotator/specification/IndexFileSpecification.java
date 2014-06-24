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
import type.DeclaredType;
import type.Type;
import annotations.Annotation;
import annotations.el.ABlock;
import annotations.el.AClass;
import annotations.el.AElement;
import annotations.el.AExpression;
import annotations.el.AField;
import annotations.el.AMethod;
import annotations.el.AScene;
import annotations.el.ATypeElement;
import annotations.el.ATypeElementWithType;
import annotations.el.AnnotationDef;
import annotations.el.BoundLocation;
import annotations.el.InnerTypeLocation;
import annotations.el.LocalLocation;
import annotations.el.RelativeLocation;
import annotations.el.TypeIndexLocation;
import annotations.field.AnnotationFieldType;
import annotations.io.ASTPath;
import annotations.io.IndexFileParser;
import annotations.util.coll.VivifyingMap;
import annotator.find.AnnotationInsertion;
import annotator.find.CastInsertion;
import annotator.find.ConstructorInsertion;
import annotator.find.CloseParenthesisInsertion;
import annotator.find.Criteria;
import annotator.find.GenericArrayLocationCriterion;
import annotator.find.Insertion;
import annotator.find.NewInsertion;
import annotator.find.ReceiverInsertion;
import annotator.scanner.MethodOffsetClassVisitor;

import com.sun.source.tree.Tree;

public class IndexFileSpecification implements Specification {

  private final List<Insertion> insertions = new ArrayList<Insertion>();
  private final AScene scene;
  private final String indexFileName;

  // If set, do not attempt to read class files with Asm.
  // Mostly for debugging and workarounds.
  public static boolean noAsm = false;

  private static boolean debug = false;

  private ConstructorInsertion cons = null;

  public IndexFileSpecification(String indexFileName) {
    this.indexFileName = indexFileName;
    scene = new AScene();
  }

  @Override
  public List<Insertion> parse() throws FileIOException {
    try {
      IndexFileParser.parseFile(indexFileName, scene);
    } catch(FileIOException e) {
      throw e;
    } catch(Exception e) {
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
      parseClass(clist, entry.getKey(), entry.getValue());
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
    cons = null;  // 0 or 1 per class
    if (! noAsm) {
      //  load extra info using asm
      debug("parseClass(" + className + ")");
      try {
        ClassReader classReader = new ClassReader(className);
        MethodOffsetClassVisitor cv = new MethodOffsetClassVisitor();
        classReader.accept(cv, false);
        debug("Done reading " + className + ".class");
      } catch (IOException e) {
        // If .class file not found, still proceed, in case
        // user only wants method signature annotations.
        // (TODO: It would be better to store which classes could not be
        // found, then issue a warning only if an attempt is made to use
        // the (missing) information.  See
        // https://code.google.com/p/annotation-tools/issues/detail?id=34 .)
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
      CriterionList outerClist = eiList.add(Criteria.atLocation());
      parseElement(outerClist, ei);
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
      parseStaticInit(clist, entry.getKey(), entry.getValue());
    }
    for (Map.Entry<Integer, ABlock> entry : clazz.instanceInits.entrySet()) {
      parseInstanceInit(clist, entry.getKey(), entry.getValue());
    }
    for (Map.Entry<String, AExpression> entry : clazz.fieldInits.entrySet()) {
      parseFieldInit(clist, entry.getKey(), entry.getValue());
    }

    debug("parseClass(" + className + "):  done");
  }

  /** Fill in this.insertions with insertion pairs. */
  private void parseField(CriterionList clist, String fieldName, AField field) {
    clist = clist.add(Criteria.field(fieldName));

    // parse declaration annotations
    parseElement(clist, field);

    parseInnerAndOuterElements(clist, field.type);
    parseASTInsertions(clist, field.insertAnnotations, field.insertTypecasts);
  }

  private void parseStaticInit(CriterionList clist, int blockID, ABlock block) {
    clist = clist.add(Criteria.inStaticInit(blockID));
    // the method name argument is not used for static initializers, which are only used
    // in source specifications. Same for instance and field initializers.
    // the empty () are there to prevent the whole string to be removed in later parsing.
    parseBlock(clist, "static init number " + blockID + "()", block);
  }

  private void parseInstanceInit(CriterionList clist, int blockID, ABlock block) {
    clist = clist.add(Criteria.inInstanceInit(blockID));
    parseBlock(clist, "instance init number " + blockID + "()", block);
  }

  // keep the descriptive strings for field initializers and static inits consistent
  // with text used in NewCriterion.

  private void parseFieldInit(CriterionList clist, String fieldName, AExpression exp) {
    clist = clist.add(Criteria.inFieldInit(fieldName));
    parseExpression(clist, "init for field " + fieldName + "()", exp);
  }

  /**
   * Fill in this.insertions with insertion pairs.
   * @param clist The criteria specifying the location of the insertions.
   * @param element Holds the annotations to be inserted.
   * @return A list of the {@link AnnotationInsertion}s that are created.
   */
  private List<Insertion> parseElement(CriterionList clist, AElement element) {
    return parseElement(clist, element, new ArrayList<Insertion>(), false);
  }

  /**
   * Fill in this.insertions with insertion pairs.
   * @param clist The criteria specifying the location of the insertions.
   * @param element Holds the annotations to be inserted.
   * @param isCastInsertion {@code true} if this for a cast insertion, {@code false}
   *          otherwise.
   * @return A list of the {@link AnnotationInsertion}s that are created.
   */
  private List<Insertion> parseElement(CriterionList clist, AElement element,
      boolean isCastInsertion) {
    return parseElement(clist, element, new ArrayList<Insertion>(), isCastInsertion);
  }

  /**
   * Fill in this.insertions with insertion pairs.
   * @param clist The criteria specifying the location of the insertions.
   * @param element Holds the annotations to be inserted.
   * @param add {@code true} if the create {@link AnnotationInsertion}s should
   *         be added to {@link #insertions}, {@code false} otherwise.
   * @return A list of the {@link AnnotationInsertion}s that are created.
   */
  private List<Insertion> parseElement(CriterionList clist, AElement element,
      List<Insertion> innerTypeInsertions) {
    return parseElement(clist, element, innerTypeInsertions, false);
  }

  /**
   * Fill in this.insertions with insertion pairs.
   * @param clist The criteria specifying the location of the insertions.
   * @param element Holds the annotations to be inserted.
   * @param innerTypeInsertions The insertions on the inner type of this
   *         element. This is only used for receiver and "new" insertions.
   *         See {@link ReceiverInsertion} for more details.
   * @param isCastInsertion {@code true} if this for a cast insertion, {@code false}
   *          otherwise.
   * @return A list of the {@link AnnotationInsertion}s that are created.
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
    Set<Pair<String, Boolean>> elementAnnotations = getElementAnnotation(element);
    if (element instanceof ATypeElementWithType && elementAnnotations.isEmpty()) {
      // Still insert even if it's a cast insertion with no outer annotations to
      // just insert a cast, or insert a cast with annotations on the compound
      // types.
      Pair<CastInsertion, CloseParenthesisInsertion> insertions = createCastInsertion(
          ((ATypeElementWithType) element).getType(), null,
          innerTypeInsertions, clist.criteria());
      cast = insertions.a;
      closeParen = insertions.b;
    }

    for (Pair<String,Boolean> p : elementAnnotations) {
      List<Insertion> elementInsertions = new ArrayList<Insertion>();
      String annotationString = p.a;
      Boolean isDeclarationAnnotation = p.b;
      Criteria criteria = clist.criteria();
      GenericArrayLocationCriterion galc = criteria.getGenericArrayLocation();
      if (isOnReceiver(criteria)) {
        if (receiver == null) {
          DeclaredType type = new DeclaredType();
          type.addAnnotation(annotationString);
          receiver = new ReceiverInsertion(type, criteria, innerTypeInsertions);
          elementInsertions.add(receiver);
        } else {
          receiver.getType().addAnnotation(annotationString);
        }
      } else if (isOnNew(criteria)) {
        if (neu == null) {
          DeclaredType type = new DeclaredType();
          type.addAnnotation(annotationString);
          neu = new NewInsertion(type, criteria, innerTypeInsertions);
          elementInsertions.add(neu);
        } else {
          neu.getType().addAnnotation(annotationString);
        }
      } else if (element instanceof ATypeElementWithType) {
        if (cast == null) {
          Pair<CastInsertion, CloseParenthesisInsertion> insertions = createCastInsertion(
              ((ATypeElementWithType) element).getType(), annotationString,
              innerTypeInsertions, criteria);
          cast = insertions.a;
          closeParen = insertions.b;
          elementInsertions.add(cast);
          elementInsertions.add(closeParen);
        } else {
          cast.getType().addAnnotation(annotationString);
        }
      } else {
        Insertion ins = new AnnotationInsertion(annotationString, criteria,
                                      isDeclarationAnnotation);
        debug("parsed: " + ins);
        if (!isCastInsertion) {
            // Annotations on compound types of a cast insertion will be
            // inserted directly on the cast insertion.
            //this.insertions.add(ins);
        		elementInsertions.add(ins);
        }
        annotationInsertions.add(ins);
      }
      this.insertions.addAll(elementInsertions);

      // exclude expression annotations 
      if (criteria.isOnMethod("<init>()V") && !criteria.isOnNew()
          && (galc == null || galc.getLocation().isEmpty())) {
          // TODO: equivalent criteria for ASTPath?
        if (cons == null) {
          DeclaredType type = new DeclaredType(criteria.getClassName());
          cons = new ConstructorInsertion(type, criteria,
              new ArrayList<Insertion>());
          this.insertions.add(cons);
        }
        for (Insertion i : elementInsertions) {
          if (i.getKind() == Insertion.Kind.RECEIVER) {
            cons.addReceiverInsertion((ReceiverInsertion) i);
          } else if (criteria.isOnReturnType()) {
            ((DeclaredType) cons.getType()).addAnnotation(annotationString);
          } else if (isDeclarationAnnotation) {
            cons.addDeclarationInsertion(i);
            i.setInserted(true);
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
    return annotationInsertions;
  }

  private boolean isOnReceiver(Criteria criteria) {
      GenericArrayLocationCriterion galc = criteria.getGenericArrayLocation();
      if (galc == null || galc.getLocation().isEmpty()) {
          ASTPath astPath = criteria.getASTPath();
          if (astPath == null) {
              return criteria.isOnReceiver();
          } else {
              ASTPath.ASTEntry entry = astPath.get(-1);
              return entry.childSelectorIs(ASTPath.ARGUMENT)
                  && entry.getArgument() < 0;
          }
      }
      return false;
  }

  private boolean isOnNew(Criteria criteria) {
      GenericArrayLocationCriterion galc =
          criteria.getGenericArrayLocation();
      if (galc == null || galc.getLocation().isEmpty()) {
          ASTPath astPath = criteria.getASTPath();
          if (astPath == null || astPath.isEmpty()) {
              return criteria.isOnNew();
          } else {
              ASTPath.ASTEntry entry = astPath.get(-1);
              Tree.Kind kind = entry.getTreeKind();
              return (kind == Tree.Kind.NEW_ARRAY
                          || kind == Tree.Kind.NEW_CLASS)
                      && entry.childSelectorIs(ASTPath.TYPE)
                      && entry.getArgument() == 0;
          }
      }
      return false;
  }

  /**
   * Creates the {@link CastInsertion} and {@link CloseParenthesisInsertion}
   * for a cast insertion.
   *
   * @param type The cast type to insert.
   * @param annotationString The annotation on the outermost type, or
   *         {@code null} if none. With no outermost annotation this cast
   *         insertion will either be a cast without any annotations or a cast
   *         with annotations only on the compound types.
   * @param innerTypeInsertions The annotations on the inner types.
   * @param criteria The criteria for the location of this insertion.
   * @return The {@link CastInsertion} and {@link CloseParenthesisInsertion}.
   */
  private Pair<CastInsertion, CloseParenthesisInsertion> createCastInsertion(
      Type type, String annotationString, List<Insertion> innerTypeInsertions,
      Criteria criteria) {
    if (annotationString != null) {
      type.addAnnotation(annotationString);
    }
    Insertion.decorateType(innerTypeInsertions, type);
    CastInsertion cast = new CastInsertion(criteria, type);
    CloseParenthesisInsertion closeParen = new CloseParenthesisInsertion(
        criteria, cast.getSeparateLine());
    return new Pair<CastInsertion, CloseParenthesisInsertion>(cast, closeParen);
  }

  /**
   * Fill in this.insertions with insertion pairs for the outer and inner types.
   * @param clist The criteria specifying the location of the insertions.
   * @param typeElement Holds the annotations to be inserted.
   */
  private void parseInnerAndOuterElements(CriterionList clist, ATypeElement typeElement) {
    parseInnerAndOuterElements(clist, typeElement, false);
  }

  /**
   * Fill in this.insertions with insertion pairs for the outer and inner types.
   * @param clist The criteria specifying the location of the insertions.
   * @param typeElement Holds the annotations to be inserted.
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
  private Set<Pair<String,Boolean>> getElementAnnotation(AElement element) {
    Set<Pair<String,Boolean>> result = new LinkedHashSet<Pair<String,Boolean>>(element.tlAnnotationsHere.size());
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
      result.add(new Pair<String,Boolean>(annotationString,
                                          ! adef.isTypeAnnotation()));
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

    // parse declaration annotations
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
      parseField(paramClist, index.toString(), param);
      parseInnerAndOuterElements(paramClist, param.type);
    }

    // parse insert annotations/typecasts of method
    parseASTInsertions(clist, method.insertAnnotations, method.insertTypecasts);
    parseBlock(clist, methodName, method.body);
  }

  private void parseBlock(CriterionList clist, String methodName, ABlock block) {

    // parse locals of method
    for (Entry<LocalLocation, AField> entry : block.locals.entrySet()) {
      LocalLocation loc = entry.getKey();
      AElement var = entry.getValue();
      CriterionList varClist = clist.add(Criteria.local(methodName, loc));
      // parse declaration annotations
      parseElement(varClist, var);  // TODO: _?_
      parseInnerAndOuterElements(varClist, var.type);
    }

    parseExpression(clist, methodName, block);
  }

  private void parseExpression(CriterionList clist, String methodName, AExpression exp) {
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
  }

  private void parseASTInsertions(CriterionList clist,
      VivifyingMap<ASTPath, ATypeElement> insertAnnotations,
      VivifyingMap<ASTPath, ATypeElementWithType> insertTypecasts) {
    for (Entry<ASTPath, ATypeElement> entry : insertAnnotations.entrySet()) {
      ASTPath astPath = entry.getKey();
      ATypeElement insertAnnotation = entry.getValue();
      CriterionList insertAnnotationClist =
          clist.add(Criteria.astPath(astPath));
      if (insertAnnotation instanceof ATypeElement) {
        parseInnerAndOuterElements(insertAnnotationClist,
            insertAnnotation, true);
      }
    }
    for (Entry<ASTPath, ATypeElementWithType> entry : insertTypecasts.entrySet()) {
      ASTPath astPath = entry.getKey();
      ATypeElementWithType insertTypecast = entry.getValue();
      CriterionList insertTypecastClist = clist.add(Criteria.astPath(astPath));
      parseInnerAndOuterElements(insertTypecastClist, insertTypecast, true);
    }
  }
}
