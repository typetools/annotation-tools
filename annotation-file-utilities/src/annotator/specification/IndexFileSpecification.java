package annotator.specification;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.objectweb.asm.ClassReader;

import annotations.Annotation;
import annotations.AnnotationFactory;
import annotations.Annotation;
import annotations.el.*;
import annotations.field.AnnotationFieldType;
import annotations.io.IndexFileParser;
import annotations.util.coll.VivifyingMap;
import annotator.find.Criteria;
import annotator.find.Insertion;
import annotator.scanner.MethodOffsetClassVisitor;

import com.sun.source.tree.Tree;

import utilMDE.FileIOException;

public class IndexFileSpecification implements Specification {

  private List<Insertion> insertions = new ArrayList<Insertion>();
  private Properties keywords;
  private AScene scene;
  private String indexFileName;

  private static boolean debug = false;

  public IndexFileSpecification(String indexFileName) {
    this.indexFileName = indexFileName;
    scene = new AScene();
  }

  public List<Insertion> parse() throws FileIOException {
    try {
      IndexFileParser.parseFile(indexFileName, scene);
    } catch(FileIOException e) {
      throw e;
    } catch(Exception e) {
      throw new RuntimeException("Exception while parsing index file", e);
    }

    parseScene();
//    debug("---------------------------------------------------------");
    return this.insertions;
  }

  private static void debug(String s) {
    if (debug) {
      System.out.println(s);
    }
  }

  /** Fill in this.insertions with insertion pairs. */
  private void parseScene() {
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

    //  load extra info using asm
    debug("want extra information for class: " + className);
    try {
      ClassReader classReader = new ClassReader(className);
      MethodOffsetClassVisitor cv = new MethodOffsetClassVisitor();
      classReader.accept(cv, false);
    } catch(IOException e) {
      // If .class file not found, still proceed, in case
      // user only wants method signature annotations.
      System.out.println("Warning: IndexFileSpecification did not find classfile for: " + className);
      // throw new RuntimeException("IndexFileSpecification.parseClass: " + e);
    } catch (RuntimeException e) {
      System.err.println("IndexFileSpecification had a problem reading class: " + className);
      throw e;
    }
    String packageName;
    String classNameUnqualified;
    int lastdot = className.lastIndexOf('.');
    if (lastdot == -1) {
      packageName = "";
      classNameUnqualified = className;
    } else {
      packageName = className.substring(0, lastdot);
      classNameUnqualified = className.substring(lastdot+1);
    }

    CriterionList classClist = clist;
    clist = clist.add(Criteria.inPackage(packageName));
    int dollarpos;
    while ((dollarpos = classNameUnqualified.lastIndexOf('$')) != -1) {
      classClist = classClist.add(Criteria.inClass(classNameUnqualified.substring(0, dollarpos)));
      classNameUnqualified = classNameUnqualified.substring(dollarpos+1);
    }
    classClist = clist.add(Criteria.is(Tree.Kind.CLASS, classNameUnqualified));
    parseElement(classClist, clazz);

    VivifyingMap<BoundLocation, ATypeElement> bounds = clazz.bounds;
    for (Entry<BoundLocation, ATypeElement> entry : bounds.entrySet()) {
      BoundLocation boundLoc = entry.getKey();
      ATypeElement bound = entry.getValue();
      CriterionList boundList = clist.add(Criteria.classBound(className, boundLoc));
      for (Entry<InnerTypeLocation, AElement> innerEntry : bound.innerTypes.entrySet()) {
        InnerTypeLocation innerLoc = innerEntry.getKey();
        AElement ae = innerEntry.getValue();
        CriterionList innerBoundList = boundList.add(Criteria.atLocation(innerLoc));
        parseElement(innerBoundList, ae);
      }
      CriterionList outerClist = boundList.add(Criteria.atLocation());
      parseElement(outerClist, bound);
    }


    // TODO: fix in class criterion to accept fully qualified names?
//    if (className.contains(".")) {
//      className = className.substring(className.lastIndexOf(".") + 1);
//    }
    clist = clist.add(Criteria.inClass(className));

    for (Map.Entry<String, ATypeElement> entry : clazz.fields.entrySet()) {
//      clist = clist.add(Criteria.notInMethod()); // TODO: necessary? what is in class but not in method?
      parseField(clist, entry.getKey(), entry.getValue());
    }
    for (Map.Entry<String, AMethod> entry : clazz.methods.entrySet()) {
      parseMethod(clist, entry.getKey(), entry.getValue());
    }
  }

  /** Fill in this.insertions with insertion pairs. */
  private void parseField(CriterionList clist, String fieldName, ATypeElement field) {
    clist = clist.add(Criteria.field(fieldName));
    parseInnerAndOuterElements(clist, field);
  }

  /** Fill in this.insertions with insertion pairs. */
  private void parseElement(CriterionList clist, AElement element) {
    for (String annotationString : getElementAnnotation(element)) {
      Insertion ins = new Insertion(annotationString, clist.criteria());
      debug("parsed: " + ins);
      this.insertions.add(ins);
    }
  }

  /** Fill in this.insertions with insertion pairs. */
  private void parseInnerAndOuterElements(CriterionList clist, ATypeElement typeElement) {
    for (Entry<InnerTypeLocation, AElement> innerEntry: typeElement.innerTypes.entrySet()) {
      InnerTypeLocation innerLoc = innerEntry.getKey();
      AElement innerElement = innerEntry.getValue();
      CriterionList innerClist = clist.add(Criteria.atLocation(innerLoc));
      parseElement(innerClist, innerElement);
    }
    CriterionList outerClist = clist.add(Criteria.atLocation());
    parseElement(outerClist, typeElement);
  }

  // Returns a string representation of the annotations at the element.
  private Set<String> getElementAnnotation(AElement element) {
    Set<String> result = new LinkedHashSet<String>(element.tlAnnotationsHere.size());
    for (Annotation tla : element.tlAnnotationsHere) {
      AnnotationDef tldef = tla.def;
      AnnotationDef adef = tldef;
      Annotation sa = tla;
      String annotationName = adef.name;

      String keywordProperty = "@" + annotationName;
      // if (keywords.containsKey(annotationName)) {
      //   keywordProperty = keywords.getProperty(annotationName);
      // }

      String annotationString = keywordProperty;

      if (sa.fieldValues.size() > 0) {
        annotationString += "(";
        boolean first = true;
        for (Entry<String, Object> entry : sa.fieldValues.entrySet()) {
          // parameters of the annotation
          if (!first) {
            annotationString += ", ";
          }
          annotationString += entry.getKey() + "=";
          AnnotationFieldType fieldType = sa.def.fieldTypes.get(entry.getKey());
          assert fieldType != null;
          annotationString += fieldType.format(entry.getValue());
          first = false;
        }
        annotationString += ")";
      }
      // annotationString += " ";
      result.add(annotationString);
    }
    return result;
  }

  private void parseMethod(CriterionList clist, String methodName, AMethod method) {
    // Being "in" a method refers to being somewhere in the
    // method's tree, which includes return types, parameters, receiver, and
    // elements inside the method body.
    clist = clist.add(Criteria.inMethod(methodName));

    // parse receiver
    CriterionList receiverClist = clist.add(Criteria.receiver(methodName));
    parseElement(receiverClist, method.receiver);

    // parse return type
    CriterionList returnClist = clist.add(Criteria.returnType(methodName));
    parseInnerAndOuterElements(returnClist, method.returnType);

    // parse bounds of method
    for (Entry<BoundLocation, ATypeElement> entry : method.bounds.entrySet()) {
      BoundLocation boundLoc = entry.getKey();
      ATypeElement bound = entry.getValue();
      CriterionList boundClist = clist.add(Criteria.methodBound(methodName, boundLoc));
      parseInnerAndOuterElements(boundClist, bound);
    }

    // parse parameters of method
    for (Entry<Integer, ATypeElement> entry : method.parameters.entrySet()) {
      Integer index = entry.getKey();
      ATypeElement param = entry.getValue();
      CriterionList paramClist = clist.add(Criteria.param(methodName, index));
      parseInnerAndOuterElements(paramClist, param);
    }

    // parse locals of method
    for (Entry<LocalLocation, ATypeElement> entry : method.locals.entrySet()) {
      LocalLocation loc = entry.getKey();
      ATypeElement var = entry.getValue();
      CriterionList varClist = clist.add(Criteria.local(methodName, loc));
      parseInnerAndOuterElements(varClist, var);
    }

    // parse typecasts of method
    for (Entry<Integer, ATypeElement> entry : method.typecasts.entrySet()) {
      Integer offset = entry.getKey();
      ATypeElement cast = entry.getValue();
      CriterionList castClist = clist.add(Criteria.cast(methodName, offset));
      parseInnerAndOuterElements(castClist, cast);
    }

    // parse news (object creation) of method
    for (Entry<Integer, ATypeElement> entry : method.news.entrySet()) {
      Integer offset = entry.getKey();
      ATypeElement newObject = entry.getValue();
      CriterionList newClist = clist.add(Criteria.newObject(methodName, offset));
      parseInnerAndOuterElements(newClist, newObject);
    }

    // parse instanceofs of method
    for (Entry<Integer, ATypeElement> entry : method.instanceofs.entrySet()) {
      Integer offset = entry.getKey();
      ATypeElement instanceOf = entry.getValue();
      CriterionList instanceOfClist = clist.add(Criteria.instanceOf(methodName, offset));
      parseInnerAndOuterElements(instanceOfClist, instanceOf);
    }
  }
}
