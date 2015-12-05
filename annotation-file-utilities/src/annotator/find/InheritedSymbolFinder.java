package annotator.find;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import annotations.util.JVMNames;
import annotator.Source;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;

/**
 * Provides static access to JVML descriptors of methods for which
 *  implementations are inherited in a given class.
 *
 * @author dbro
 */
public class InheritedSymbolFinder {
  private static Source source = null;

  // map of class (flat) names to sets of JVML method descriptors
  private static Map<String, Set<String>> cache =
      new TreeMap<String, Set<String>>();  // few classes expected

  private InheritedSymbolFinder() {}

  // Needed for obtaining instances of utility classes Types and Elements.
  // TODO: make methods non-static and pass source to constructor instead
  public static void setSource(Source source) {
    if (InheritedSymbolFinder.source != source) {
      InheritedSymbolFinder.source = source;
      cache.clear();
    }
  }

  /**
   * Finds whether a method matching a JVML descriptor has an inherited
   *  implementation in a given class.
   *
   * @param classSymbol symbol table entry for a class
   * @param methodDescriptor JVML descriptor for a method
   * @return whether a matching method exists in the class
   */
  public static boolean isInheritedIn(Symbol.ClassSymbol classSymbol,
      String methodDescriptor) {
    String key = removeJVMReturnType(methodDescriptor);
    return key != null && getInherited(classSymbol).contains(key);
  }

  /**
   * Finds inherited methods defined for a class (given by its symbol
   *  table entry).
   *
   * @param classSymbol symbol table entry for a class
   * @return JVML descriptors of methods in scope of class
   */
  private static Set<String>
  getInherited(final Symbol.ClassSymbol classSymbol) {
    if (source == null) { throw new IllegalStateException("null source"); }

    final Elements elements = source.getElementUtils();
    final Types types = source.getTypeUtils();
    String className = classSymbol.flatName().toString();

    if (elements == null) {
      throw new IllegalStateException("source.getElementUtils() returned null");
    }
    if (types == null) {
      throw new IllegalStateException("source.getTypeUtils() returned null");
    }
    if (cache.containsKey(className)) { return cache.get(className); }

    Set<String> names = new HashSet<String>();
    List<? extends Element> allMembers =
        elements.getAllMembers((TypeElement) classSymbol);
    Set<ExecutableElement> eelems =
        ElementFilter.methodsIn(new HashSet<Element>(allMembers));

    for (ExecutableElement eelem : eelems) {
      if (eelem instanceof Symbol.MethodSymbol) {
        Symbol.MethodSymbol msym = (Symbol.MethodSymbol) eelem;
        if (msym.owner != classSymbol) {
          final String jvmMethodName = JVMNames.getJVMMethodName(msym);
          final String key = removeJVMReturnType(jvmMethodName);
          // no need to match return type; if local method and
          // superclass method both compile, the former overrides the
          // latter
          if (key != null && msym.owner.getKind() == ElementKind.CLASS
              && inHierarchy(msym.owner.type, classSymbol)) {
            names.add(key);
          }
        }
      }
    }
    // cache unmodifiable view of result
    names = names.isEmpty() ? Collections.<String>emptySet()
        : Collections.<String>unmodifiableSet(names);
    cache.put(className, names);
    return names;
  }

  // is ownerType an ancestor of csym?
  private static boolean inHierarchy(Type ownerType,
      Symbol.ClassSymbol csym) {
    for (Type type = csym.type;
        type != null && type.getTag() == TypeTag.CLASS;
        type = ((Type.ClassType) type).supertype_field) {
      if (type == ownerType) { return true; }
    }
    return false;
  }

  // take return type off end of JVML method spec
  private static String removeJVMReturnType(String methodDescriptor) {
    int ix = methodDescriptor.lastIndexOf(')');
    return ix > 0 ? methodDescriptor.substring(0, ix+1) : null;
  }
}
