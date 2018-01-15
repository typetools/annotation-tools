package annotator.find;

import annotator.Main;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plume.UtilMDE;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

public class IsSigMethodCriterion implements Criterion {

  // The context is used for determining the fully qualified name of methods.
  private static class Context {
    public final String packageName;
    public final List<String> imports;
    public Context(String packageName, List<String> imports) {
      this.packageName = packageName;
      this.imports = imports;
    }
  }

  private static final Map<CompilationUnitTree, Context> contextCache = new HashMap<CompilationUnitTree, Context>();

  private final String fullMethodName; // really the full JVML signature, sans return type
  private final String simpleMethodName;
  // list of parameters in Java, not JVML format
  private final List<String> fullyQualifiedParams;
  // in Java, not JVML, format.  may be "void"
  private final String returnType;

  public IsSigMethodCriterion(String methodName) {
    this.fullMethodName = methodName.substring(0, methodName.indexOf(")") + 1);
    this.simpleMethodName = methodName.substring(0, methodName.indexOf("("));
//    this.fullyQualifiedParams = new ArrayList<String>();
//    for (String s : methodName.substring(
//        methodName.indexOf("(") + 1, methodName.indexOf(")")).split(",")) {
//      if (s.length() > 0) {
//        fullyQualifiedParams.add(s);
//      }
//    }
    this.fullyQualifiedParams = new ArrayList<String>();
    try {
      parseParams(
        methodName.substring(methodName.indexOf("(") + 1,
            methodName.indexOf(")")));
    } catch (Exception e) {
      throw new RuntimeException("Caught exception while parsing method: " +
          methodName, e);
    }
    String returnTypeJvml = methodName.substring(methodName.indexOf(")") + 1);
    this.returnType = (returnTypeJvml.equals("V")
                       ? "void"
                       : UtilMDE.fieldDescriptorToBinaryName(returnTypeJvml));
  }

  // params is in JVML format
  private void parseParams(String params) {
    while (params.length() != 0) {
      // nextParam is in JVML format
      String nextParam = readNext(params);
      params = params.substring(nextParam.length());
      fullyQualifiedParams.add(UtilMDE.fieldDescriptorToBinaryName(nextParam));
    }
  }

  // strip a JVML type off a string containing multiple concatenated JVML types
  private String readNext(String restOfParams) {
    String firstChar = restOfParams.substring(0, 1);
    if (isPrimitiveLetter(firstChar)) {
      return firstChar;
    } else if (firstChar.equals("[")) {
      return "[" + readNext(restOfParams.substring(1));
    } else if (firstChar.equals("L")) {
      return "L" + restOfParams.substring(1, restOfParams.indexOf(";") + 1);
    } else {
      throw new RuntimeException("Unknown method params: " + fullMethodName + " with remainder: " + restOfParams);
    }
  }

  // called by isSatisfiedBy(TreePath), will get compilation unit on its own
  private static Context initImports(TreePath path) {
    CompilationUnitTree topLevel = path.getCompilationUnit();
    Context result = contextCache.get(topLevel);
    if (result != null) {
      return result;
    }

    ExpressionTree packageTree = topLevel.getPackageName();
    String packageName;
    if (packageTree == null) {
      packageName = ""; // the default package
    } else {
      packageName = packageTree.toString();
    }

    List<String> imports = new ArrayList<String>();
    for (ImportTree i : topLevel.getImports()) {
      String imported = i.getQualifiedIdentifier().toString();
      imports.add(imported);
    }

    result = new Context(packageName, imports);
    contextCache.put(topLevel, result);
    return result;
  }

  // Abstracts out the inner loop of matchTypeParams.
  // goalType is fully-qualified.
  private boolean matchTypeParam(String goalType, Tree type,
                                 Map<String, String> typeToClassMap,
                                 Context context) {
    String simpleType = type.toString();

    boolean haveMatch = matchSimpleType(goalType, simpleType, context);
    if (!haveMatch) {
      if (!typeToClassMap.isEmpty()) {
        for (Map.Entry<String, String> p : typeToClassMap.entrySet()) {
          simpleType = simpleType.replaceAll("\\b" + p.getKey() + "\\b",
              p.getValue());
          haveMatch = matchSimpleType(goalType, simpleType, context);
          if (!haveMatch) {
            Criteria.dbug.debug("matchTypeParams() => false:%n");
            Criteria.dbug.debug("  type = %s%n", type);
            Criteria.dbug.debug("  simpleType = %s%n", simpleType);
            Criteria.dbug.debug("  goalType = %s%n", goalType);
          }
        }
      }
    }
    return haveMatch;
  }


  private boolean matchTypeParams(List<? extends VariableTree> sourceParams,
                                  Map<String, String> typeToClassMap,
                                  Context context) {
    assert sourceParams.size() == fullyQualifiedParams.size();
    for (int i = 0; i < sourceParams.size(); i++) {
      String fullType = fullyQualifiedParams.get(i);
      VariableTree vt = sourceParams.get(i);
      Tree vtType = vt.getType();
      if (! matchTypeParam(fullType, vtType, typeToClassMap, context)) {
        Criteria.dbug.debug(
            "matchTypeParam() => false:%n  i=%d vt = %s%n  fullType = %s%n",
            i, vt, fullType);
        return false;
      }
    }
    return true;
  }


  // simpleType is the name as it appeared in the source code.
  // fullType is fully-qualified.
  // Both are in Java, not JVML, format.
  private boolean matchSimpleType(String fullType, String simpleType, Context context) {
    Criteria.dbug.debug("matchSimpleType(%s, %s, %s)%n",
        fullType, simpleType, context);

    // must strip off generics, is all of this necessary, though?
    // do you ever have generics anywhere but at the end?
    while (simpleType.contains("<")) {
      int bracketIndex = simpleType.lastIndexOf("<");
      String beforeBracket = simpleType.substring(0, bracketIndex);
      String afterBracket = simpleType.substring(simpleType.indexOf(">", bracketIndex) + 1);
      simpleType = beforeBracket + afterBracket;
    }


    // TODO: arrays?

    // first try qualifying simpleType with this package name,
    // then with java.lang
    // then with default package
    // then with all of the imports

    boolean matchable = false;

    if (!matchable) {
      // match with this package name
      String packagePrefix = context.packageName;
      if (packagePrefix.length() > 0) {
        packagePrefix = packagePrefix + ".";
      }
      if (matchWithPrefix(fullType, simpleType, packagePrefix)) {
        matchable = true;
      }
    }

    if (!matchable) {
      // match with java.lang
      if (matchWithPrefix(fullType, simpleType, "java.lang.")) {
        matchable = true;
      }
    }

    if (!matchable) {
      // match with default package
      if (matchWithPrefix(fullType, simpleType, "")) {
        matchable = true;
      }
    }

    // From Java 7 language definition 6.5.5.2 (Qualified Types):
    // If a type name is of the form Q.Id, then Q must be either a type
    // name or a package name.  If Id names exactly one accessible type
    // that is a member of the type or package denoted by Q, then the
    // qualified type name denotes that type.
    if (!matchable) {
      // match with any of the imports
      for (String someImport : context.imports) {
        String importPrefix = null;
        if (someImport.contains("*")) {
          // don't include the * in the prefix, should end in .
          // TODO: this is a real bug due to nonnull, though I discovered it manually
          // importPrefix = someImport.substring(0, importPrefix.indexOf("*"));
          importPrefix = someImport.substring(0, someImport.indexOf("*"));
        } else {
          // if you imported a specific class, you can only use that import
          // if the last part matches the simple type
          String importSimpleType =
            someImport.substring(someImport.lastIndexOf(".") + 1);

          // Remove array brackets from simpleType if it has them
          int arrayBracket = simpleType.indexOf('[');
          String simpleBaseType = simpleType;
          if (arrayBracket > -1) {
            simpleBaseType = simpleType.substring(0, arrayBracket);
          }
          if (!(simpleBaseType.equals(importSimpleType)
              || simpleBaseType.startsWith(importSimpleType + "."))) {
            continue;
          }

          importPrefix = someImport.substring(0, someImport.lastIndexOf(".") + 1);
        }

        if (matchWithPrefix(fullType, simpleType, importPrefix)) {
          matchable = true;
          break; // out of for loop
        }
      }
    }

    return matchable;
  }

  private boolean matchWithPrefix(String fullType, String simpleType, String prefix) {
    return matchWithPrefixOneWay(fullType, simpleType, prefix)
        || matchWithPrefixOneWay(simpleType, fullType, prefix);
  }

  // simpleType can be in JVML format ??  Is that really possible?
  private boolean matchWithPrefixOneWay(String fullType, String simpleType,
      String prefix) {

    // maybe simpleType is in JVML format
    String simpleType2 = simpleType.replace("/", ".");

    String fullType2 = fullType.replace("$", ".");

    /* unused String prefix2 = (prefix.endsWith(".")
                      ? prefix.substring(0, prefix.length() - 1)
                      : prefix); */
    boolean b = (fullType2.equals(prefix + simpleType2)
                 // Hacky way to handle the possibility that fulltype is an
                 // inner type but simple type is unqualified.
                 || (fullType.startsWith(prefix)
                     && (fullType.endsWith("$" + simpleType2)
                         || fullType2.endsWith("." + simpleType2))));
    Criteria.dbug.debug("matchWithPrefix(%s, %s, %s) => %b)%n",
        fullType2, simpleType, prefix, b);
    return b;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path, Tree leaf) {
    if (path == null) {
      return false;
    }
    assert path.getLeaf() == leaf;
    return isSatisfiedBy(path);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSatisfiedBy(TreePath path) {
    if (path == null) {
      return false;
    }

    Context context = initImports(path);

    Tree leaf = path.getLeaf();

    if (leaf.getKind() != Tree.Kind.METHOD) {
      Criteria.dbug.debug(
          "IsSigMethodCriterion.isSatisfiedBy(%s) => false: not a METHOD tree%n",
          Main.leafString(path));
      return false;
    }
    // else if ((((JCMethodDecl) leaf).mods.flags & Flags.GENERATEDCONSTR) != 0) {
    //  Criteria.dbug.debug(
    //      "IsSigMethodCriterion.isSatisfiedBy(%s) => false: generated constructor%n",
    //      Main.leafString(path));
    //  return false;
    // }

    MethodTree mt = (MethodTree) leaf;

    if (! simpleMethodName.equals(mt.getName().toString())) {
      Criteria.dbug.debug("IsSigMethodCriterion.isSatisfiedBy => false: Names don't match%n");
      return false;
    }

    List<? extends VariableTree> sourceParams = mt.getParameters();
    if (fullyQualifiedParams.size() != sourceParams.size()) {
      Criteria.dbug.debug("IsSigMethodCriterion.isSatisfiedBy => false: Number of parameters don't match%n");
      return false;
    }

    // now go through all type parameters declared by method
    // and for each one, create a mapping from the type to the
    // first declared extended class, defaulting to Object
    // for example,
    // <T extends Date> void foo(T t)
    //  creates mapping: T -> Date
    // <T extends Date & List> void foo(Object o)
    //  creates mapping: T -> Date
    // <T extends Date, U extends List> foo(Object o)
    //  creates mappings: T -> Date, U -> List
    // <T> void foo(T t)
    //  creates mapping: T -> Object

    Map<String, String> typeToClassMap = new HashMap<String, String>();
    for (TypeParameterTree param : mt.getTypeParameters()) {
      String paramName = param.getName().toString();
      String paramClass = "Object";
      List<? extends Tree> paramBounds = param.getBounds();
      if (paramBounds != null && paramBounds.size() >= 1) {
        Tree boundZero = paramBounds.get(0);
        if (boundZero.getKind() == Tree.Kind.ANNOTATED_TYPE) {
          boundZero = ((AnnotatedTypeTree) boundZero).getUnderlyingType();
        }
        paramClass = boundZero.toString();
      }
      typeToClassMap.put(paramName, paramClass);
    }

    // Do the same for the enclosing class.
    // The type variable might not be from the directly enclosing
    // class, but from a further up class.
    // Go through all enclosing classes and add the type parameters.
    {
      TreePath classpath = path;
      ClassTree ct = enclosingClass(classpath);
      while (ct!=null) {
        for (TypeParameterTree param : ct.getTypeParameters()) {
          String paramName = param.getName().toString();
          String paramClass = "Object";
          List<? extends Tree> paramBounds = param.getBounds();
          if (paramBounds != null && paramBounds.size() >= 1) {
            Tree pb = paramBounds.get(0);
            if (pb.getKind() == Tree.Kind.ANNOTATED_TYPE) {
                pb = ((AnnotatedTypeTree)pb).getUnderlyingType();
            }
            paramClass = pb.toString();
          }
          typeToClassMap.put(paramName, paramClass);
        }
        classpath = classpath.getParentPath();
        ct = enclosingClass(classpath);
      }
    }

    if (! matchTypeParams(sourceParams, typeToClassMap, context)) {
      Criteria.dbug.debug("IsSigMethodCriterion => false: Parameter types don't match%n");
      return false;
    }

    if ((mt.getReturnType() != null) // must be a constructor
        && (! matchTypeParam(returnType, mt.getReturnType(), typeToClassMap, context))) {
      Criteria.dbug.debug("IsSigMethodCriterion => false: Return types don't match%n");
      return false;
    }

    Criteria.dbug.debug("IsSigMethodCriterion.isSatisfiedBy => true%n");
    return true;
  }

  /* This is a copy of the method from the Checker Framework
   * TreeUtils.enclosingClass.
   * We cannot have a dependency on the Checker Framework.
   * TODO: as is the case there, anonymous classes are not handled correctly.
   */
  private static ClassTree enclosingClass(final TreePath path) {
    final Set<Tree.Kind> kinds = EnumSet.of(
            Tree.Kind.CLASS,
            Tree.Kind.ENUM,
            Tree.Kind.INTERFACE,
            Tree.Kind.ANNOTATION_TYPE
    );
    TreePath p = path;

    while (p != null) {
      Tree leaf = p.getLeaf();
      assert leaf != null; /*nninvariant*/
      if (kinds.contains(leaf.getKind())) {
        return (ClassTree) leaf;
      }
      p = p.getParentPath();
    }

    return null;
  }

  @Override
  public Kind getKind() {
    return Kind.SIG_METHOD;
  }

//  public static String getSignature(MethodTree mt) {
//    String sig = mt.getName().toString().trim(); // method name, no parameters
//    sig += "(";
//    boolean first = true;
//    for (VariableTree vt : mt.getParameters()) {
//      if (!first) {
//        sig += ",";
//      }
//      sig += getType(vt.getType());
//      first = false;
//    }
//    sig += ")";
//
//    return sig;
//  }
//
//  private static String getType(Tree t) {
//    if (t.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
//      return getPrimitiveType((PrimitiveTypeTree) t);
//    } else if (t.getKind() == Tree.Kind.IDENTIFIER) {
//      return "L" + ((IdentifierTree) t).getName().toString();
//    } else if (t.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
//      // don't care about generics due to erasure
//      return getType(((ParameterizedTypeTree) t).getType());
//    }
//    throw new RuntimeException("unable to get type of: " + t);
//  }
//
//  private static String getPrimitiveType(PrimitiveTypeTree pt) {
//    TypeKind tk = pt.getPrimitiveTypeKind();
//    if (tk == TypeKind.ARRAY) {
//      return "[";
//    } else if (tk == TypeKind.BOOLEAN) {
//      return "Z";
//    } else if (tk == TypeKind.BYTE) {
//      return "B";
//    } else if (tk == TypeKind.CHAR) {
//      return "C";
//    } else if (tk == TypeKind.DOUBLE) {
//      return "D";
//    } else if (tk == TypeKind.FLOAT) {
//      return "F";
//    } else if (tk == TypeKind.INT) {
//      return "I";
//    } else if (tk == TypeKind.LONG) {
//      return "J";
//    } else if (tk == TypeKind.SHORT) {
//      return "S";
//    }
//
//    throw new RuntimeException("Invalid TypeKind: " + tk);
//  }

  /*
  private boolean isPrimitive(String s) {
    return
      s.equals("boolean") ||
      s.equals("byte") ||
      s.equals("char") ||
      s.equals("double") ||
      s.equals("float") ||
      s.equals("int") ||
      s.equals("long") ||
      s.equals("short");
  }
  */

  private boolean isPrimitiveLetter(String s) {
    return
      s.equals("Z") ||
      s.equals("B") ||
      s.equals("C") ||
      s.equals("D") ||
      s.equals("F") ||
      s.equals("I") ||
      s.equals("J") ||
      s.equals("S");
  }

  /*
  private String primitiveLetter(String s) {
    if (s.equals("boolean")) {
      return "Z";
    } else if (s.equals("byte")) {
      return "B";
    } else if (s.equals("char")) {
      return "C";
    } else if (s.equals("double")) {
      return "D";
    } else if (s.equals("float")) {
      return "F";
    } else if (s.equals("int")) {
      return "I";
    } else if (s.equals("long")) {
      return "J";
    } else if (s.equals("short")) {
      return "S";
    } else {
      throw new RuntimeException("IsSigMethodCriterion: unknown primitive: " + s);
    }
  }
  */

  @Override
  public String toString() {
    return "IsSigMethodCriterion: " + fullMethodName;
  }
}
