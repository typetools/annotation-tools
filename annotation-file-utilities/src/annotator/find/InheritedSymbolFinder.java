package annotator.find;

import java.util.Collection;
import java.util.HashMap;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.model.JavacElements;

/**
 * Provides static access to JVML descriptors of methods for which
 *  implementations are inherited in a given class.
 *
 * @author dbro
 */
public class InheritedSymbolFinder {
  private static Source source = null;

  // class name -> method index
  private static Map<String, Multimap<String, ExecutableElement>> cache =
      new TreeMap<String, Multimap<String, ExecutableElement>>();

  // keys: class (flat) name + ':' + method (simple) name
  // values: multimap of method (simple) name to matching method elems
  private static Map<String, Multimap<String, ExecutableElement>> cash =
      new HashMap<String, Multimap<String, ExecutableElement>>();

  // initialized once and for all
  private static Multimap<String, ExecutableElement> objectMethods = null;

  private InheritedSymbolFinder() {}

  // Needed for obtaining instances of utility classes Types and Elements.
  // TODO: make methods non-static and pass source to constructor instead.
  public static void setSource(Source source) {
    if (InheritedSymbolFinder.source != source) {
      InheritedSymbolFinder.source = source;
      cache.clear();
      cash.clear();
      if (source != null) {
        // initialize objectMethods
        if (objectMethods == null) {
          TypeElement o =
              source.getElementUtils().getTypeElement("java.lang.Object");
          objectMethods =
              LinkedHashMultimap.<String, ExecutableElement>create();
          for (Element e : o.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD) {
              String key = keyFor((Symbol.MethodSymbol) e);
              objectMethods.put(key, (ExecutableElement) e);
            }
          }
        }
        cash.put("java.lang.Object", objectMethods);
      }
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
    Multimap<String, ExecutableElement> inherited = getInherited(classSymbol);
    int ix = methodDescriptor.indexOf('(');
    String key = methodDescriptor.substring(0, ix);
    Collection<ExecutableElement> elems = inherited.get(key);

    // There may be multiple methods stored under the same key (bare
    // method name), so check each of them.
    // TODO: figure out good way to determine whether method descriptor
    // gives the type of an inherited method.  (Only exact matches work
    // now.)
    if (elems != null) {
      for (ExecutableElement elem : elems) {
        String jvmName = JVMNames.getJVMMethodName(elem);
        int ix0 = methodDescriptor.lastIndexOf(')') + 1;
        int ix1 = jvmName.lastIndexOf(')') + 1;
        String key0 = methodDescriptor.substring(0, ix0);
        String key1 = jvmName.substring(0, ix1);
        if (key0.equals(key1)) { return true; }
      }
    }
    return false;
  }

  /**
   * Finds inherited methods defined for a class (given by its symbol
   *  table entry).
   *
   * @param classSymbol symbol table entry for a class
   * @return JVML descriptors of methods in scope of class
   */
  private static Multimap<String, ExecutableElement>
  getInherited(final Symbol.ClassSymbol classSymbol) {
    if (source == null) { throw new IllegalStateException("null source"); }

    final JavacElements elements = (JavacElements) source.getElementUtils();
    final Types types = source.getTypeUtils();
    String className = classSymbol.flatName().toString();

    if (elements == null) {
      throw new IllegalStateException("source.getElementUtils() returned null");
    }
    if (types == null) {
      throw new IllegalStateException("source.getTypeUtils() returned null");
    }
    if (cache.containsKey(className)) { return cache.get(className); }

    Multimap<String, ExecutableElement> inherited =
        HashMultimap.<String, ExecutableElement>create();
    Multimap<String, ExecutableElement> subElems =
        getInstanceMethods(classSymbol);

    // check pairs of methods (resp. from class and superclass) w/same name
    for (ExecutableElement eelem0 : subElems.values()) {
      Symbol.MethodSymbol msym = (Symbol.MethodSymbol) eelem0;
      if (msym.owner.getKind() == ElementKind.CLASS) {
        String key = keyFor(msym);
        if (msym.owner != classSymbol) {  // otherwise, defined in subclass
          Multimap<String, ExecutableElement> superElems =
              getInstanceMethods((Symbol.ClassSymbol) msym.owner);
          for (ExecutableElement eelem1 : superElems.get(key)) {
            // override => not inherited
            if (elements.overrides(eelem0, eelem1, classSymbol)) { break; }
          }
          inherited.put(key, eelem0);
        }
      }
    }
    // cache unmodifiable view of result
    inherited =
        Multimaps.<String, ExecutableElement>unmodifiableMultimap(inherited);
    cache.put(className, inherited);
    return inherited;
  }

  /**
   * Finds inherited methods defined for a class (given by its symbol
   *  table entry).
   *
   * @param classSymbol symbol table entry for a class
   * @return JVML descriptors of methods in scope of class
   */
  private static Multimap<String, ExecutableElement>
  getInstanceMethods(final Symbol.ClassSymbol classSymbol) {
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
    if (cash.containsKey(className)) { return cash.get(className); }

    Multimap<String, ExecutableElement> names =
        LinkedHashMultimap.<String, ExecutableElement>create();
    List<? extends Element> allMembers =
        elements.getAllMembers((TypeElement) classSymbol);
    Set<ExecutableElement> eelems =
        ElementFilter.methodsIn(new HashSet<Element>(allMembers));

    for (ExecutableElement eelem : eelems) {
      String key = keyFor(eelem);
      names.put(key, eelem);
    }

    // cache unmodifiable view of result
    names = Multimaps.<String, ExecutableElement>unmodifiableMultimap(names);
    cash.put(className, names);
    return names;
  }

  // key for storage in "cash"
  private static String keyFor(ExecutableElement eelem) {
    //String key = JVMNames.getJVMMethodName(eelem);
    //int ix = key.lastIndexOf(')');
    //return key.substring(0, ix+1);
    return eelem.getSimpleName().toString();
  }
}
